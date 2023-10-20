package com.example.wallet.ewallet;

import com.example.wallet.ewallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class UserApplication implements CommandLineRunner {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // we are creating a user only, we are just calling it transaction user,
        // which have trx related permission, need to run one time only
//        User txnService = User.builder()
//                .phoneNumber("txn_service")
//                .password(passwordEncoder.encode("txn123"))
//                .authorities(UserConstants.SERVICE_AUTHORITY)
//                .email("txn@gmail.com")
//                .userIdentifier(UserIdentifier.SERVICE_ID)
//                .identifierValue("txn123")
//                .build();
//        userRepository.save(txnService);
    }
}
