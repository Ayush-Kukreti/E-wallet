package com.example.wallet.ewallet.service;

import com.example.wallet.ewallet.CommonConstants;
import com.example.wallet.ewallet.UserIdentifier;
import com.example.wallet.ewallet.WalletUpdateStatus;
import com.example.wallet.ewallet.model.Wallet;
import com.example.wallet.ewallet.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @KafkaListener(topics = CommonConstants.USER_CREATION_TOPIC, groupId = "group123")
    public void createWallet(String message) throws ParseException {

        JSONObject data = (JSONObject) new JSONParser().parse(message) ;

        String phoneNumber = (String) data.get(CommonConstants.USER_CREATION_TOPIC_PHONE_NUMBER);
        Long userId = (Long) data.get(CommonConstants.USER_CREATION_TOPIC_USERID);
        String identifierValue = (String) data.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_VALUE);
        String userIdentifier = (String) data.get(CommonConstants.USER_CREATION_TOPIC_IDENTIFIER_KEY);

        Wallet wallet = Wallet.builder()
                .phoneNumber(phoneNumber)
                .userId(userId)
                .userIdentifier(UserIdentifier.valueOf(userIdentifier))
                .identifierValue(identifierValue)
                .balance(25.0)
                .build();

        walletRepository.save(wallet);
    }

    @KafkaListener(topics = CommonConstants.TRANSACTION_CREATION_TOPIC, groupId = "group123")
    public void updatedWalletForTransaction(String message) throws ParseException, JsonProcessingException {
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        // retrieve the info
        String sender = (String)data.get("sender");
        String receiver = (String)data.get("receiver");
        Double amount = (Double)data.get("amount");
        String transactionId = (String)data.get("transactionId");

        // validate the sender and receiver, whether they exist or not
        Wallet senderWallet = walletRepository.findByPhoneNumber(sender);
        Wallet receiverWallet = walletRepository.findByPhoneNumber(receiver);

        // now we have to send the msg back to txn whether the wallet update is success or failed.
        // prepare the JSONObject, which we will be sending back to txn
        // now wallet will act here as consumer as well as producer.
        JSONObject walletUpdateMessage = new JSONObject();
        walletUpdateMessage.put("sender", sender);
        walletUpdateMessage.put("receiver", receiver);
        walletUpdateMessage.put("transactionId", transactionId);
        walletUpdateMessage.put("amount", amount);

        // if invalid request, produce an event
        if(senderWallet == null || receiverWallet == null || senderWallet.getBalance() < amount){
            //TODO: send more precise message in transaction
            // "invalid_request_message"
//                String invalidRequestMessage;
//                if(senderWallet == null){
//                    invalidRequestMessage = "sender do not exist...";
//                }else if ( receiverWallet == null){
//                    invalidRequestMessage = "receiver do not exist...";
//                }else{
//                    invalidRequestMessage = "sender do not have enough balance...";
//                }
//                walletUpdateMessage.put("invalid_request_message",invalidRequestMessage);




            walletUpdateMessage.put("walletUpdateStatus", WalletUpdateStatus.FAILED);
            kafkaTemplate.send(CommonConstants.WALLET_UPDATED_TOPIC,objectMapper.writeValueAsString(walletUpdateMessage));
            return;
        }

        // if receiver and sender is validated then we need to perform update wallet operation
        walletRepository.updateWallet(sender,0-amount); // TODO: try sending "-amount" only
        walletRepository.updateWallet(receiver,amount);

        walletUpdateMessage.put("walletUpdateStatus",WalletUpdateStatus.SUCCESS);
        //produce an event to mention wallet is updated
        kafkaTemplate.send(CommonConstants.WALLET_UPDATED_TOPIC,objectMapper.writeValueAsString(walletUpdateMessage));
    }
}
