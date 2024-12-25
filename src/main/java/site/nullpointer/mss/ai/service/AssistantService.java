package site.nullpointer.mss.ai.service;

import com.alibaba.fastjson2.JSON;
import org.springframework.ai.chat.messages.SystemMessage;
import site.nullpointer.mss.ai.domain.agent.Agent;
import site.nullpointer.mss.ai.domain.agent.AgentDispatchResult;
import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.domain.identity.Profile;
import site.nullpointer.mss.ai.llm.LLMService;
import site.nullpointer.mss.ai.repository.UserMessageEntity;
import site.nullpointer.mss.ai.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Order(100)
public class AssistantService {

    @Autowired
    private IntentAgent intentAgent;

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private LLMService llmService;

    public Flux<String> handleUserMessage(String content, Map<String, Object> extra, Profile userProfile, String enterpriseId, Agent agent) {
        return processWithExistingAgent(agent, content, extra, userProfile, enterpriseId);

    }


    private Flux<String> processWithExistingAgent(Agent agent, String question, Map<String, Object> extra, Profile userProfile, String enterpriseId) {
        AgentOptions options = agent.getOptions();
        List<Message> messageList = agent.getMessageList();
        UserMessage userMessage = new UserMessage(question);
        messageList.add(userMessage);

        return llmService.stream(messageList, userProfile, extra, enterpriseId, options, agent.getId()).flatMap(response -> Flux.just(JSON.toJSONString(response)));
    }

    public void saveMessage(String content, Map<String, Object> extra, Profile userProfile, String enterpriseId) {
        UserMessageEntity message = new UserMessageEntity();
        message.setContent(content);
        String extraJSON = JSON.toJSONString(extra);
        message.setExtra(extraJSON);
        message.setEnterpriseId(enterpriseId);
        String userId = userProfile.getId();
        message.setUserId(userId);
        messageRepository.save(message);
    }

    public Agent getAgent(Profile userProfile, String enterpriseId) {
        String id = userProfile.getId();

        List<UserMessageEntity> userMessageEntityList = messageRepository.findTop5ByUserIdAndEnterpriseIdOrderByCreateDateDesc(id, enterpriseId);
        StringBuffer sb = new StringBuffer();
        if (!userMessageEntityList.isEmpty()) {
            sb.append("用户当前的会话历史为：");
            for (int i = 0; i < userMessageEntityList.size(); i++) {
                String content = userMessageEntityList.get(i).getContent();
                sb.append(i + ". " + content + "\n");
            }

        }

        AgentDispatchResult agentDispatchResult = intentAgent.parse(sb.toString());
        String agentType = agentDispatchResult.getAgentType();
        //agentId和用户强关联
        String agentId = "AGENT_INSTANCE:USER_ID:" + id + ":AGENT_TYPE:" + agentType;
        Agent agent = agentManager.getAgentByAgentId(agentId);
        if (agent == null) {
            AgentOptions agentOptions = agentManager.getAgentByType(agentType);
            agent = new Agent();
            String systemPrompt = agentOptions.getSystemPrompt();
            List<Message> messageList = new ArrayList<>();
            messageList.add(new SystemMessage(systemPrompt));
            agent.setMessageList(messageList);
            agent.setOptions(agentOptions);
            agent.setId(agentId);
            agentManager.createAgentInstance(agentId, agent);
            return agent;
        } else {
            return agent;
        }
    }
}






