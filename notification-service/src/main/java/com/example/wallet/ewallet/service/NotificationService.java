package com.example.wallet.ewallet.service;

import com.example.wallet.ewallet.CommonConstants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    @Autowired
    SimpleMailMessage simpleMailMessage;

    @Autowired
    JavaMailSender javaMailSender;

    @KafkaListener(topics = CommonConstants.TRANSACTION_COMPLETED_TOPIC, groupId = "group123")
    public void sendNotification(String message) throws ParseException {
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        // get message data
        String email = (String) data.get("email");
        String emailMessage = (String) data.get("message");

        simpleMailMessage.setFrom("dummygfg36@rediffmail.com");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setText(emailMessage);
        simpleMailMessage.setSubject("E-Wallet Payment Updates");

        javaMailSender.send(simpleMailMessage);
    }


}
