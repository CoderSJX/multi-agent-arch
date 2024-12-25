package site.nullpointer.mss.ai.domain.llm;

import lombok.Data;

@Data
public class FunctionCallResult {
    private boolean cardMessage;
    private boolean directReturn;
    private String cardTypeId;
    private String content;
    private Object data;
    private String lastMessage;
    private boolean nextFinish;
}
