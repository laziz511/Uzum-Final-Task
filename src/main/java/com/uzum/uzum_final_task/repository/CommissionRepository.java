package com.uzum.uzum_final_task.repository;

import com.uzum.uzum_final_task.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    Optional<Commission> findByFromAndTo(String from, String to);
}
