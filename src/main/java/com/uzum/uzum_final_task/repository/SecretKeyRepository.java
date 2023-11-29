package com.uzum.uzum_final_task.repository;

import com.uzum.uzum_final_task.entity.SecretKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretKeyRepository extends JpaRepository<SecretKey, Long> {
    Optional<SecretKey> findByKeyValue(String secretKey);
}
