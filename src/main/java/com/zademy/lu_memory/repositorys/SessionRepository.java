package com.zademy.lu_memory.repositorys;

import com.zademy.lu_memory.entitys.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    long countByStatus(String status);

    List<SessionEntity> findTop10ByStatusOrderByStartedAtDesc(String status);
}
