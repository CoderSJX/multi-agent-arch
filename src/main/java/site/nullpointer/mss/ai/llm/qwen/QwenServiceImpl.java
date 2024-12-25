package site.nullpointer.mss.ai.llm.qwen;


import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.ToolCallHelper;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.domain.assistant.ChatResponse;
import site.nullpointer.mss.ai.domain.assistant.Status;
import site.nullpointer.mss.ai.domain.identity.Profile;
import site.nullpointer.mss.ai.domain.llm.FunctionCallRequest;
import site.nullpointer.mss.ai.domain.llm.FunctionCallResult;
import site.nullpointer.mss.ai.functioncall.FunctionCallConstants;
import site.nullpointer.mss.ai.llm.LLMService;
import site.nullpointer.mss.ai.service.AgentManager;
import site.nullpointer.mss.ai.service.CardFetcher;
import site.nullpointer.mss.ai.service.FunctionCallService;

import java.util.*;
import java.util.function.Function;


@Slf4j
@Service
@ConditionalOnProperty(prefix = "business.llm", name = "model-series", havingValue = "qwen")
public class QwenServiceImpl implements LLMService {

    private ToolCallHelper toolCallHelper = new ToolCallHelper();

    @Autowired
    private FunctionCallService functionCallService;
    @Autowired
    private CardFetcher cardFetcher;
    @Autowired
    private AgentManager agentManager;
    @Value("${business.llm.api-key}")
    private String apiKey;
    @Value("${business.llm.model}")
    private String model;
    @Value("${business.llm.url}")
    private String url;

    @Override
    public Flux<ChatResponse> stream(List<Message> messageList, Profile userProfile, Map<String, Object> extra, String enterpriseId, AgentOptions options, String agentId) {
        //agentOptions中包含了这个agent中拥有的方法描述。
        List<OpenAiApi.FunctionTool> functionTools = options.getFunctionTools();
        OpenAiApi api = new OpenAiApi(url, apiKey);
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().withModel(model).withTemperature(0.0).withTools(functionTools).withProxyToolCalls(true).build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, chatOptions);
        return processToolCall(chatModel, messageList, Set.of(OpenAiApi.ChatCompletionFinishReason.TOOL_CALLS.name(), OpenAiApi.ChatCompletionFinishReason.STOP.name()), toolCall -> handleToolCall(toolCall, userProfile, extra, enterpriseId), agentId);
    }


    private Flux<ChatResponse> processToolCall(OpenAiChatModel chatModel, final List<Message> messageList, Set<String> finishReasons, Function<AssistantMessage.ToolCall, FunctionCallResult> customFunction, String agentId) {
        try {

            Prompt prompt = new Prompt(messageList);
            Flux<org.springframework.ai.chat.model.ChatResponse> chatResponses = chatModel.stream(prompt);
            //如果是纯文本的回复，就用个stringbuffer来积累，用于下一次大模型对话中的MessageList，
            final StringBuffer sb = new StringBuffer();
            return chatResponses.flatMap(chatResponse -> {
                //判断是不是funtioncall
                boolean isToolCall = toolCallHelper.isToolCall(chatResponse, finishReasons);

                if (isToolCall) {

                    Optional<Generation> toolCallGeneration = chatResponse.getResults().stream().filter(g -> !CollectionUtils.isEmpty(g.getOutput().getToolCalls())).findFirst();


                    AssistantMessage assistantMessage = toolCallGeneration.get().getOutput();
                    log.info("web助理大模型返回：" + JSON.toJSONString(assistantMessage));
                    List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

                    List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
                    AssistantMessage.ToolCall toolCall = null;
                    toolCall = toolCalls.get(toolCalls.size() - 1);

                    String arguments = toolCall.arguments();

                    int lastIndexOf = arguments.lastIndexOf("{");
                    arguments = arguments.substring(lastIndexOf);
                    toolCall = new AssistantMessage.ToolCall(toolCall.id(), toolCall.type(), toolCall.name(), arguments);

                    String assistantMessageContent = assistantMessage.getContent();
                    assistantMessage = new AssistantMessage(assistantMessageContent, new HashMap<>(), Arrays.asList(toolCall));
                    //如果是正常的functioncall调用，就直接调用。
                    FunctionCallResult functionResponse = customFunction.apply(toolCall);
                    //tips 这一步至关重要，方法调用的结果直接影响大模型的判断。
                    String responseContent = functionResponse.getContent();
                    toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), ModelOptionsUtils.toJsonString(responseContent)));


                    ToolResponseMessage toolMessageResponse = new ToolResponseMessage(toolResponses, Map.of());
                    //加入历史列表。
                    messageList.add(assistantMessage);

                    messageList.add(toolMessageResponse);
                    log.info("web用户当前会话历史：" + JSON.toJSONString(messageList));
                    //判断是不是卡片返回，如果是的话，在这里要返回卡片数据和卡片模版
                    //因为卡片是给用户看的，大模型不需要，所以不必给大模型返回。
                    boolean isCardMessage = functionResponse.isCardMessage();
                    if (isCardMessage) {
                        String content = functionResponse.getContent();
                        String cardTypeId = functionResponse.getCardTypeId();
                        Object cardDesc = cardFetcher.getCardDescById(cardTypeId);
                        Object data = functionResponse.getData();
                        agentManager.updateAgentInstanceById(agentId, messageList);
                        //这里可以选择是否清空会话历史
                        //agentManager.deleteAgentInstanceById(agentId);
                        return Flux.just(ChatResponse.builder().status(Status.COMPLETED).content(content).data(data).design(cardDesc).build());
                    }
                    //如果是纯文本的内容，那就应该大模型返回了，大模型会继续分析这个调用结果。
                    //这个场景通常是是RAG的过程或者查询类的，需要大模型进一步的分析的。
                    agentManager.updateAgentInstanceById(agentId, messageList);
                    return processToolCall(chatModel, messageList, finishReasons, customFunction, agentId);
                }
                //这里就是不需要functioncall的处理，纯文本的回复。
                //todo 这里出现过无法停止的回答，还没有定位到原因。
                Generation generation = chatResponse.getResults().get(0);
                String content = generation.getOutput().getContent();
                AssistantMessage message = generation.getOutput();
                ChatGenerationMetadata metadata = generation.getMetadata();
                String finishReason = metadata.getFinishReason();
                sb.append(message.getContent());
                if ("STOP".equals(finishReason)) {
                    AssistantMessage assistantMessage = new AssistantMessage(sb.toString());
                    messageList.add(assistantMessage);
                    agentManager.updateAgentInstanceById(agentId, messageList);
                    return Flux.just(ChatResponse.builder().content(content).status(Status.COMPLETED).build());
                }
                if (StringUtils.isEmpty(content)) {
                    return Flux.empty();
                }

                return Flux.just(ChatResponse.builder().content(content).status(Status.REPLYING).build());

            });
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return Flux.error(e);
        }

    }

    //所有的functioncall都在此处理，具体是远程调用，还是在本地处理，由你决定
    private FunctionCallResult handleToolCall(AssistantMessage.ToolCall toolCall, Profile userProfile, Map<String, Object> extras, String enterpriseId) {


        if (toolCall.type().equals(FunctionCallConstants.FUNCTION)) {
            String arguments = toolCall.arguments();
            String name = toolCall.name();
            FunctionCallRequest request = FunctionCallRequest.builder().functionName(name).functionArguments(arguments).userProfile(userProfile).extras(extras).enterpriseId(enterpriseId).build();
            FunctionCallResult callResult = functionCallService.apply(request);
            return callResult;

        }
        return null;
    }


    @Override
    public org.springframework.ai.chat.model.ChatResponse call(List<Message> messageList) {
        OpenAiApi api = new OpenAiApi(url, apiKey);

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().withModel(model).withTemperature(0.0).withProxyToolCalls(true).build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, chatOptions);

        Prompt prompt = new Prompt(messageList);

        org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
        log.info(response.toString());

        return response;

    }


}