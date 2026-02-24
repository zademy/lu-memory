package com.zademy.lu_memory.repositorys;

import com.zademy.lu_memory.entitys.PromptEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PromptRepository extends JpaRepository<PromptEntity, UUID> {

    @Query("""
            select p from PromptEntity p
            where (:topicKey is null or p.topicKey = :topicKey)
            order by p.createdAt desc
            """)
    List<PromptEntity> findRecentByTopicKey(String topicKey, Pageable pageable);
}
