package com.model.feign;

import com.knowledge.bean.Knowledge;
import com.model.feign.fallback.KnowledgeFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(value = "service-knowledge", fallback = KnowledgeFeignClientFallback.class) //Feign客户端
public interface KnowledgeFeignClient {

    //1、标注在Controller上，是接收这样的请求
    //2、标注在FeignClient上，是发送这样的请求
    @GetMapping("/knowledge/search")
    List<Knowledge> findByQuestion(@RequestParam("q") String question);

    @GetMapping("/knowledge/create")
    Knowledge createByService(@RequestParam("question") String question,
                              @RequestParam("answer") String answer);
}

