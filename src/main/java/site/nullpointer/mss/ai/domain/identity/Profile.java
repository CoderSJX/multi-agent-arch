package site.nullpointer.mss.ai.domain.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Profile {

    private Enterprise enterprise;
    private List<EnterpriseElement> enterprises;
    @JsonProperty("first_name")
    private String firstName;
    private String id;

    @JsonProperty("last_name")
    private String lastName;

    private String mail;
    private String phone;
}
