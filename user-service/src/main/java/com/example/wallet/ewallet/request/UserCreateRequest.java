package com.example.wallet.ewallet;


import com.example.wallet.ewallet.model.User;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber; // we can make it as the username

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String dob;

    private String country;

    @NotNull
    UserIdentifier userIdentifier;

    @NotBlank
    private String identifierValue;

    public User buildUser() {
       return  User.builder()
               .name(name)
               .phoneNumber(phoneNumber)
               .password(password)
               .email(email)
               .userIdentifier(userIdentifier)
               .identifierValue(identifierValue)
               .dob(dob)
               .country(country)
               .build();
    }
}
