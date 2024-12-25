package site.nullpointer.mss.ai.domain.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
//服务端给用户的返回体
public class ChatResponse {
    //纯文本内容。
    private String content;
    //当前处理此问题的智能体ID
    private String agentID;
    //纯数据内容，例如：表单中的各个变量、按钮等
    private Object data;
    //服务端的处理状态，例如：问题分析中、调用xxx方法中、已完成、知识检索中、出错了。
    //analyzing,functioning,completed,searching,error,dispatch
    private Status status;
    //组件设计，例如：表单的组件和布局
    private Object design;

}
