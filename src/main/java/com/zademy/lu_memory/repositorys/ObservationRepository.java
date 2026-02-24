package com.zademy.lu_memory.repositorys;

import com.zademy.lu_memory.entitys.ObservationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObservationRepository extends JpaRepository<ObservationEntity, UUID> {

    Optional<ObservationEntity> findByIdAndDeletedFalse(UUID id);

    long countByDeletedFalse();

    long countByDeletedTrue();

    boolean existsByTopicKeyAndDeletedFalse(String topicKey);

    @Query("""
            select o from ObservationEntity o
            where o.deleted = false
              and (:topicKey is null or o.topicKey = :topicKey)
            order by o.createdAt desc
            """)
    List<ObservationEntity> findRecentByTopicKey(String topicKey, Pageable pageable);

    @Query("""
            select o from ObservationEntity o
            where o.deleted = false
              and o.createdAt between :from and :to
            order by o.createdAt asc
            """)
    List<ObservationEntity> findTimeline(Instant from, Instant to, Pageable pageable);
}
