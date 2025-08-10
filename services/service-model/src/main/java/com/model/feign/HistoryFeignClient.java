package com.model.feign;

import com.model.feign.fallback.HistoryFeignClientFallback;
import com.session.bean.QAPair;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "service-history", fallback = HistoryFeignClientFallback.class) //Feign客户端
public interface HistoryFeignClient {

    //1、标注在Controller上，是接收这样的请求
    //2、标注在FeignClient上，是发送这样的请求
    @GetMapping("/history/context")
    List<QAPair> getQAPairsBySessionId(@RequestParam("sessionId") String sessionId);

    @PostMapping("/history/answer")
    QAPair saveAnswer(@RequestParam("qaPairId") Long qaPairId,
                             @RequestParam("answer") String answer);
}
