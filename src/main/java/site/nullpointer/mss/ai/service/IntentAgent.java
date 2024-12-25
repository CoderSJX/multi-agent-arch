package site.nullpointer.mss.ai.service;

import com.alibaba.fastjson2.JSON;
import site.nullpointer.mss.ai.domain.agent.AgentDispatchResult;
import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.functioncall.IntentParams;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(4)
public class IntentAgent {
    @Resource(name = "intentAgentOptions")
    private AgentOptions options;

    @Value("${business.llm.api-key}")
    private String apiKey;
    @Value("${business.llm.model}")
    private String model;
    @Value("${business.llm.url}")
    private String url;

    public AgentDispatchResult parse(String history) {

        OpenAiApi api = new OpenAiApi(url, apiKey);
        List<OpenAiApi.FunctionTool> functionTools = options.getFunctionTools();
        OpenAiApi.FunctionTool functionTool = functionTools.get(0);
        OpenAiApi.FunctionTool.Function function = functionTool.function();
        String intentFunction = function.name();
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().withModel(model).withTemperature(0.0).withTools(functionTools).withProxyToolCalls(true).build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, chatOptions);
        String systemPrompt = options.getSystemPrompt();
        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        List<Message> messageList = new ArrayList<>();
        messageList.add(systemMessage);
        UserMessage userMessage = new UserMessage(history);
        messageList.add(userMessage);
        Prompt prompt = new Prompt(messageList);

        org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
        AssistantMessage message = response.getResult().getOutput();
        List<AssistantMessage.ToolCall> toolCalls = message.getToolCalls();
        AssistantMessage.ToolCall call = toolCalls.get(0);
        String name = call.name();
        String arguments = call.arguments();
        if (name.equals(intentFunction)) {
            IntentParams intentParams = JSON.parseObject(arguments, IntentParams.class);
            String businessType = intentParams.getBusinessType();

            return AgentDispatchResult.builder().agentType(businessType).build();
        }

        return null;
    }

}
