package site.nullpointer.mss.ai.service;

import com.alibaba.fastjson2.JSON;
import site.nullpointer.mss.ai.domain.agent.Agent;
import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.repository.AgentTypeEntity;
import site.nullpointer.mss.ai.repository.AgentTypeRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(-1)
public class AgentManager {

    private final Map<String, AgentOptions> agentOptionsMap = new ConcurrentHashMap<>();
    private final Map<String, Agent> agentInstanceMap = new ConcurrentHashMap<>();
    // agent计时器，超过15分钟，自动过期
    private final Map<String, Long> agentCounter = new ConcurrentHashMap<>();

    @Autowired
    private AgentTypeRepository agentTypeRepository;

    @PostConstruct
    public void initialize() {
        loadAgentType();
    }

    public AgentOptions getAgentByType(String agentType) {
        return agentOptionsMap.get("AGENT_TYPE:" + agentType);
    }

    public Agent getAgentByAgentId(String agentId) {
        // 检查并清理过期的agent实例
        checkAndRemoveExpiredAgent(agentId);

        return agentInstanceMap.get(agentId);
    }

    public Agent createAgentInstance(String agentId, Agent agent) {
        String key = agentId;
        agentInstanceMap.put(key, agent);
        resetTimer(key); // 创建实例时重置计时器
        return agent;
    }

    public Agent updateAgentInstanceById(String agentId, List<Message> messageList) {
        String key = agentId;
        Agent agentInstance = agentInstanceMap.get(key);
        if (agentInstance != null) {
            agentInstance.setMessageList(messageList);
            agentInstanceMap.put(key, agentInstance);
            resetTimer(key); // 更新实例时重置计时器
            return agentInstance;
        } else {
            return null;
        }
    }

    public void deleteAgentInstanceById(String agentId) {
        String key = agentId;
        agentInstanceMap.remove(key);
        agentCounter.remove(key); // 从计时器映射中移除
    }

    private void resetTimer(String agentId) {
        agentCounter.put(agentId, System.currentTimeMillis());
    }

    private void checkAndRemoveExpiredAgent(String agentId) {
        long currentTime = System.currentTimeMillis();
        long lastAccessTime = agentCounter.getOrDefault(agentId, 0L);
        long expireDuration = 15 * 1000; // 15分钟转换为毫秒

        if (currentTime - lastAccessTime > expireDuration) {
            // 如果实例已过期，则从所有映射中移除它
            deleteAgentInstanceById(agentId);
        }
    }

    public void loadAgentType() {
        List<AgentTypeEntity> agents = agentTypeRepository.findAll();
        for (AgentTypeEntity agent : agents) {
            String type = agent.getType();
            Boolean isPublic = agent.getIsPublic();
            String description = agent.getDescription();
            Double temperature = agent.getTemperature();
            String enterpriseId = agent.getEnterpriseId();
            String functionTools = agent.getFunctionTools();
            String systemPrompt = agent.getSystemPrompt();

            AgentOptions agentOptions = new AgentOptions();
            agentOptions.setType(type);
            agentOptions.setDescription(description);
            agentOptions.setSystemPrompt(systemPrompt);
            if (StringUtils.isNotEmpty(functionTools)) {
                List<OpenAiApi.FunctionTool> toolList = JSON.parseArray(functionTools, OpenAiApi.FunctionTool.class);
                agentOptions.setFunctionTools(toolList);
            }
            agentOptions.setTemperature(temperature);
            agentOptionsMap.put("AGENT_TYPE:" + agent.getType(), agentOptions);
        }
    }
}