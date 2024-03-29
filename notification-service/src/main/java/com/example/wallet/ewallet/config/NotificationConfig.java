package com.example.wallet.ewallet.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class NotificationConfig {

    @Bean
    Properties getPropertiesForConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return properties;
    }

    ConsumerFactory getConsumerFactory() {
        return new DefaultKafkaConsumerFactory(getPropertiesForConsumer());
    }

    // for email we need SimpleMailMessage
    @Bean
    SimpleMailMessage getMailMessage(){ return new SimpleMailMessage();}

    //for mail sender we need to set config
    // TODO: find a mail server which can be used to send the mail, google mail will not work
    @Bean
    JavaMailSender getMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.rediffmail.com");
        javaMailSender.setPort(25); // smtp port
        javaMailSender.setUsername("dummygfg36@rediffmail.com");
        javaMailSender.setPassword("GFG@java_36");

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.smtp.starttls.enable",true);
        properties.put("mail.debug",true);

        return javaMailSender;
    }


}
