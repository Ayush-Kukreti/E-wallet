package com.example.wallet.ewallet;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    private String name;
    @Column(unique = true, nullable = false)
    private String phoneNumber; // we can make it as the username

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String dob;

    private String country;

    @Enumerated(value = EnumType.STRING)
    UserIdentifier userIdentifier;

    @Column(unique = true, nullable = false)
    private String identifierValue;

    private String authorities;

    @CreationTimestamp
    private Date createdOn;

    @UpdateTimestamp
    private Date updatedOn;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String [] allAuthorities = authorities.split(UserConstants.AUTHORITIES_DELIMITER);
        return Arrays.stream(allAuthorities)
                .map(x -> new SimpleGrantedAuthority(x)).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
