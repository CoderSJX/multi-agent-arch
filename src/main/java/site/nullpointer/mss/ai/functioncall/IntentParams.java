package site.nullpointer.mss.ai.functioncall;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IntentParams {

    @JsonPropertyDescription("业务分类")
    private String businessType;

}