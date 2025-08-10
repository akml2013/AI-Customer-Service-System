package com.knowledge.controller;

import com.knowledge.bean.Knowledge;
import com.knowledge.security.SecurityValidator;
import com.knowledge.service.KnowledgeService;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);
    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private SecurityValidator securityValidator;

    @PostMapping
    public ResponseEntity<Knowledge> create(@RequestBody Knowledge knowledge) {
        return ResponseEntity.ok(knowledgeService.save(knowledge));
    }

    @GetMapping("/create")
    public Knowledge createByService(@RequestParam("question") String question,
                                     @RequestParam("answer") String answer) {
        log.info("createByService {} {}",question, answer);
        return knowledgeService.saveByService(question, answer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Knowledge> update(
            @PathVariable Long id,
            @RequestBody Knowledge knowledge
    ) {
        return ResponseEntity.ok(knowledgeService.update(id, knowledge));
    }

    @PutMapping("/{id}/answer")
    public ResponseEntity<Void> updateAnswer(
            @PathVariable Long id,
            @RequestBody String answer
    ) {
        knowledgeService.updateAnswer(id, answer);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws NotFoundException {
        knowledgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Knowledge> findByQuestion(
            @RequestParam("q") String question
    ) {
        log.info("findByQuestion {}",question);
        return knowledgeService.findByQuestion(question);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Knowledge> findById(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Knowledge>> findAll() {
        return ResponseEntity.ok(knowledgeService.findAll());
    }


    @GetMapping("/all")
    public ResponseEntity<List<Knowledge>> getAllKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Auth-Token") String token) {
        // 验证用户ID和令牌
        if (!securityValidator.validateAdminToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Knowledge> knowledgeList = knowledgeService.findAll();
            return ResponseEntity.ok(knowledgeList);
        } catch (Exception e) {
            // 记录错误日志
            log.error("获取知识库列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateKnowledge(@RequestParam("file") MultipartFile file,
                                                  @RequestHeader("X-User-Id") Long userId,
                                                  @RequestHeader("X-Auth-Token") String token) {
        // 验证用户ID和令牌
        if (!securityValidator.validateAdminToken(userId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 1. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("未选择上传文件");
        }

        // 2. 验证文件类型是否为TXT
        String contentType = file.getContentType();
        if (!"text/plain".equals(contentType) && !file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().body("仅支持TXT格式的文件");
        }

        // 3. 处理文件内容
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 解析TXT文件内容
            List<Knowledge> knowledgeItems = parseTxtFile(reader);

            // 4. 保存更新到知识库
            for(Knowledge knowledge : knowledgeItems) {
                knowledgeService.save(knowledge);
            }

            // 5. 返回成功响应
            return ResponseEntity.ok("知识库更新成功");

        } catch (IOException e) {
            log.error("文件读取错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件处理错误: " + e.getMessage());
        }
    }

    private List<Knowledge> parseTxtFile(BufferedReader reader)
            throws IOException {

        List<Knowledge> knowledgeItems = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }

            // 按照第一个冒号分割问题部分和回答部分
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                // 没有冒号的行视为格式错误
                throw new IOException(
                        String.format("第 %d 行格式错误: 未找到分隔符 ':'", lineNumber));
            }

            // 提取问题和回答
            String question = line.substring(0, colonIndex).trim();
            String answer = line.substring(colonIndex + 1).trim();

            // 检查问题和回答是否有效
            if (question.isEmpty()) {
                throw new IOException(
                        String.format("第 %d 行格式错误: 问题部分不能为空", lineNumber));
            }
            if (answer.isEmpty()) {
                throw new IOException(
                        String.format("第 %d 行格式错误: 回答部分不能为空", lineNumber));
            }

            // 创建知识库项并添加到列表
            Knowledge knowledge = new Knowledge();
            knowledge.setQuestion(question);
            knowledge.setAnswer(answer);
            knowledgeItems.add(knowledge);
        }

        // 检查是否至少有一个有效的知识项
        if (knowledgeItems.isEmpty()) {
            throw new IOException("TXT文件未包含有效的知识项");
        }

        return knowledgeItems;
    }
}
