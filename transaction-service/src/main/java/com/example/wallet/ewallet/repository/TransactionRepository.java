package com.example.wallet.ewallet.repository;

import com.example.wallet.ewallet.config.TransactionStatus;
import com.example.wallet.ewallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

@Transactional
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Modifying
    @Query("Update Transaction t set t.transactionStatus = ?2 where t.transactionId = ?1")
    void updateTransaction(String transactionId, TransactionStatus transactionStatus);
}
