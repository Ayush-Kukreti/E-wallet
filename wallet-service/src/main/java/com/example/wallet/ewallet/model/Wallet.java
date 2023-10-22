package com.example.wallet.ewallet.model;


import com.example.wallet.ewallet.UserIdentifier;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Long userId;

    private String phoneNumber;

    private double balance;

    @Enumerated(value = EnumType.STRING)
    private UserIdentifier userIdentifier;

    private String identifierValue;
}