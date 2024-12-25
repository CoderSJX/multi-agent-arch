package site.nullpointer.mss.ai.functioncall;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class HandleToOtherParams {
    @JsonPropertyDescription("用户的问题")
    private String question;
}
