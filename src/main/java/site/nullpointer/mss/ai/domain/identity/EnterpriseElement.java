package site.nullpointer.mss.ai.domain.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseElement {
    private List<Cluster> clusters;
    private String code;
    @JsonProperty("creation_date")
    private long creationDate;
    private long id;
    private String name;
    private String state;
}