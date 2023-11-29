package com.uzum.uzum_final_task.repository;

import com.uzum.uzum_final_task.entity.ConversionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionLogRepository extends JpaRepository<ConversionLog, Long> {
}

