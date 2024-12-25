package site.nullpointer.mss.ai.repository;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_message")
public class UserMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "content", length = 10000)
    private String content;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "enterprise_id")
    private String enterpriseId;
    @Column(name = "create_date")
    private String createDate;
    @Column(name = "extra", length = 100000)
    private String extra;

}
