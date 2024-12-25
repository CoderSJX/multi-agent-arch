package site.nullpointer.mss.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentTypeRepository extends JpaRepository<AgentTypeEntity, String> {
    // 可以在这里定义一些特定的查询方法
}
