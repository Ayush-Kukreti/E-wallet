package com.example.wallet.ewallet;

import com.example.wallet.ewallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {


    User findByPhoneNumber(String phoneNumber);
}
