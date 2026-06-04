package org.reward.repository;

import org.reward.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerIdAndTransactionDateBetween(
            Long customerId,
            LocalDate start,
            LocalDate end
    );
}