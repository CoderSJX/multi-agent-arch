package site.nullpointer.mss.ai.llm;


import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.domain.assistant.ChatResponse;
import site.nullpointer.mss.ai.domain.identity.Profile;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface LLMService {
    Flux<ChatResponse> stream(List<Message> messageList, Profile userProfile, Map<String, Object> extra, String enterpriseId, AgentOptions options, String agentId) ;

    org.springframework.ai.chat.model.ChatResponse call(List<Message> history);
}
