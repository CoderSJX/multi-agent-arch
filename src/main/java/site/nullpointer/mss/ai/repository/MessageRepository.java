package site.nullpointer.mss.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<UserMessageEntity, String> {

    // 根据 userId和 enterpriseId 查找最新的5条消息，按 createDate 降序排列
    List<UserMessageEntity> findTop5ByUserIdAndEnterpriseIdOrderByCreateDateDesc(String userId, String enterpriseId);
}