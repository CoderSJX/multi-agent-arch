package site.nullpointer.mss.ai.domain.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Cluster {
    @JsonProperty("base_url")
    private String baseUrl;
    @JsonProperty("cluster_id")
    private String clusterId;
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("service_version")
    private String serviceVersion;
}
