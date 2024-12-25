package site.nullpointer.mss.ai.controller;


import com.alibaba.fastjson2.JSON;
import site.nullpointer.mss.ai.domain.agent.Agent;
import site.nullpointer.mss.ai.domain.assistant.ChatRequest;
import site.nullpointer.mss.ai.domain.identity.Profile;
import site.nullpointer.mss.ai.service.AssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/chat")
@Slf4j
//Web端助理的Controller
public class AssistantController {
    @Autowired
    private AssistantService assistantService;


    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> completions(@RequestBody ChatRequest request, ServerWebExchange exchange) {
        String userInfo = exchange.getAttribute("userInfo");
        beforeCompletion(request,exchange);
        String enterpriseId = exchange.getAttribute("enterpriseId");
        String content = request.getContent();
        Map<String, Object> extra = request.getExtra();
        log.info("接收到web用户消息：" + content);
        Profile userProfile = JSON.parseObject(userInfo, Profile.class);
        Agent agent = assistantService.getAgent(userProfile, enterpriseId);
        return assistantService.handleUserMessage(content, extra, userProfile, enterpriseId,agent);

    }
    public void beforeCompletion(ChatRequest request, ServerWebExchange exchange) {
        String userInfo = exchange.getAttribute("userInfo");
        String enterpriseId = exchange.getAttribute("enterpriseId");
        String content = request.getContent();
        Map<String, Object> extra = request.getExtra();

        Profile userProfile = JSON.parseObject(userInfo, Profile.class);

        assistantService.saveMessage(content,extra,userProfile,enterpriseId);
    }



}