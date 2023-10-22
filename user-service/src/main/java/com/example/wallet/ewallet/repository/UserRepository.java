package com.example.wallet.ewallet.repository;

import com.example.wallet.ewallet.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {


    User findByPhoneNumber(String phoneNumber);
}
