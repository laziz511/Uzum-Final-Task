package com.uzum.uzum_final_task.repository;

import com.uzum.uzum_final_task.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCurrencyName(String currencyName);
}
