package com.example.wallet.ewallet.service;

import com.example.wallet.ewallet.CommonConstants;
import com.example.wallet.ewallet.User;
import com.example.wallet.ewallet.constants.UserConstants;
import com.example.wallet.ewallet.repository.UserRepository;
import com.example.wallet.ewallet.request.UserCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public User loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {

        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public void create(UserCreateRequest userCreateRequest) throws JsonProcessingException {
        User user = userCreateRequest.buildUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthorities(UserConstants.USER_AUTHORITY);

        userRepository.save(user);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommonConstants.USER_CREATION_TOPIC_USERID, user.getId());
        jsonObject.put(CommonConstants.USER_CREATION_TOPIC_PHONE_NUMBER, user.getPhoneNumber());
        jsonObject.put(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY,user.getUserIdentifier());
        jsonObject.put(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE, user.getIdentifierValue());

        kafkaTemplate.send(CommonConstants.USER_CREATION_TOPIC, objectMapper.writeValueAsString(jsonObject));
    }


    public List<User> getAll() {
        return  userRepository.findAll();
    }

}
