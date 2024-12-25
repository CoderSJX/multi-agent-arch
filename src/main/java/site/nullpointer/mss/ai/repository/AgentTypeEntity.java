package site.nullpointer.mss.ai.repository;

import jakarta.persistence.*;
import lombok.Data;


import java.io.Serial;
import java.io.Serializable;


@Data
@Entity
@Table(name = "agents")
public class AgentTypeEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 2672553622864930471L;

    @Id

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "enterprise_id")
    private String enterpriseId;
    @Column(name = "is_public")
    private Boolean isPublic;
    //智能体的业务类别
    @Column(name = "type")
    private String type;
    //系统提示词
    @Column(name = "system_prompt",length = 10000)
    private String systemPrompt;
    //温度
    @Column(name = "temperature")
    private Double temperature;
    //智能体描述，仅作说明，无实际作用
    @Column(name = "description")
    private String description;
    //智能体注册的方法
    @Column(name = "function_tools",length = 1000000)
    private String functionTools;


}
