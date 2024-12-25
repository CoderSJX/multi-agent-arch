package site.nullpointer.mss.ai.domain.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Enterprise {
    private List<Cluster> clusters;
    @JsonProperty("creation_date")
    private long creationDate;

    private String id;
    @JsonProperty("last_update")
    private long lastUpdate;
    private String name;
}

