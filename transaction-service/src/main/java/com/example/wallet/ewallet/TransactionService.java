package com.example.wallet.ewallet.service;

import com.example.wallet.ewallet.CommonConstants;
import com.example.wallet.ewallet.config.TransactionStatus;
import com.example.wallet.ewallet.WalletUpdateStatus;
import com.example.wallet.ewallet.model.Transaction;
import com.example.wallet.ewallet.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService implements UserDetailsService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // somehow this method have to return UserDetails
        // we need to get the user from userService, we need to validate the user

        JSONObject requestedUser = getUserFromUserService(username);
        // I need to convert requested User into User available from the security class since we do not have any user in transaction
        // we need 3 things: username, password, authorities
        List<LinkedHashMap<String, String>> requestedAuthorities = (List<LinkedHashMap<String, String>>) requestedUser.get("authorities");

        List<SimpleGrantedAuthority> authorities = requestedAuthorities.stream()
                .map(x -> x.get("authority"))
                .map(x -> new SimpleGrantedAuthority(x))
                .collect(Collectors.toList());
        return new User((String) requestedUser.get("username"), (String) requestedUser.get("password"), authorities);
    }

    // TODO: we can improve this method to provide a proper message,
    //  giving a transaction ID in return does not tell whether the money tranfer failed or success
    public String initiateTransaction(String sender, String receiver, String purpose, Double amount) throws JsonProcessingException {
        log.info("details for the transaction are sender: {}, receiver: {}, purpose: {}, amount: {} ", sender, receiver, purpose, amount);

        //TODO: can we validate the sender and receiver here itself ?
        // I think we should fail early no need to pass kafka msg if not valid request
        // before or after saving the transaction

        // TODO: can two methods become kafkaListener

        // save the transaction
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .purpose(purpose)
                .transactionStatus(TransactionStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())
                .build();

        transactionRepository.save(transaction);

        // once transaction is saved, now we need to call wallet to perform the transaction

        // send the kafka message for wallet
        JSONObject txnInitiatedMessage = new JSONObject();
        txnInitiatedMessage.put(CommonConstants.SENDER, sender);
        txnInitiatedMessage.put(CommonConstants.RECEIVER, receiver);
        txnInitiatedMessage.put(CommonConstants.AMOUNT, amount);
        txnInitiatedMessage.put(CommonConstants.TRANSACTION_ID, transaction.getTransactionId());

        kafkaTemplate.send(CommonConstants.TRANSACTION_CREATION_TOPIC,objectMapper.writeValueAsString(txnInitiatedMessage));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = CommonConstants.WALLET_UPDATED_TOPIC, groupId = "group123")
    public void updateTxn(String message) throws ParseException, JsonProcessingException {
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        //retrieve the data
        String sender = (String)data.get(CommonConstants.SENDER);
        String receiver = (String)data.get(CommonConstants.RECEIVER);
        String transactionId = (String)data.get(CommonConstants.TRANSACTION_ID);
        Double amount = (Double)data.get(CommonConstants.AMOUNT);

        // irrespective of walletUpdateStatus we need to have the email of sender to send the failed/success mail
        JSONObject senderObj = getUserFromUserService(sender);
        String senderEmail = (String)senderObj.get(CommonConstants.EMAIL);


        // WalletUpdateStatus walletUpdateStatus = (WalletUpdateStatus) data.get(CommonConstants.WALLET_UPDATE_STATUS);
        WalletUpdateStatus walletUpdateStatus = WalletUpdateStatus.valueOf( (String)data.get(CommonConstants.WALLET_UPDATE_STATUS) );

        // based on walletUpdateStatus:
        // failed: then the notification will be sent only to the sender.
        // success: then notification will be sent to sender and receiver both

        // here we are creating 2 separate events for sender mail and receiver mail even for success transaction

        // 1st update the transactionstatus in transaction entity
        if(walletUpdateStatus == WalletUpdateStatus.SUCCESS){
            transactionRepository.updateTransaction(transactionId,TransactionStatus.SUCCESS);
        }else{
            transactionRepository.updateTransaction(transactionId,TransactionStatus.FAILED);
        }

        // creating and sending the kafka event for the sender, irrespective of success/fail
        // prepare a sender message
        String senderMessage = "Hi, your transaction with id: " + transactionId + " got " + walletUpdateStatus + "!";
        JSONObject senderTxnUpdateMessage = new JSONObject();
        senderTxnUpdateMessage.put(CommonConstants.EMAIL,senderEmail);
        senderTxnUpdateMessage.put(CommonConstants.SENDER_MESSAGE,senderMessage);
        // the below values are not useful since we already have then in the senderMessage itself
        //        senderTxnUpdateMessage.put(CommonConstants.TRANSACTION_ID,transactionId);
        //        senderTxnUpdateMessage.put(CommonConstants.WALLET_UPDATE_STATUS,walletUpdateStatus);
        kafkaTemplate.send(CommonConstants.TRANSACTION_COMPLETED_TOPIC,objectMapper.writeValueAsString(senderTxnUpdateMessage));

        // now consider that it was a success transaction, that means now receiver will also receive a message
        String receiverEmail = null;
        if(walletUpdateStatus == WalletUpdateStatus.SUCCESS) {
            JSONObject receiverObj = getUserFromUserService(receiver);
            receiverEmail = (String) receiverObj.get(CommonConstants.EMAIL);
            // prepare a receiver message
            String receiverMessage = "Hi, you have received Rs. " + amount + " from "
                    + sender + " in your wallet linked with phone number " + receiver;
            JSONObject receiverTxnUpdateMessage = new JSONObject();
            receiverTxnUpdateMessage.put(CommonConstants.EMAIL,receiverEmail);
            receiverTxnUpdateMessage.put(CommonConstants.RECEIVER_MESSAGE,receiverMessage);
            kafkaTemplate.send(CommonConstants.TRANSACTION_COMPLETED_TOPIC,objectMapper.writeValueAsString(receiverTxnUpdateMessage));
        }

    }

    // to validate the user and get its details from the user-service
    private JSONObject getUserFromUserService(String username) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setBasicAuth("txn_service","txn123");
        HttpEntity request = new HttpEntity(httpHeaders);

        return restTemplate.
                exchange("http://localhost:6001/admin/user/"+username, HttpMethod.GET,request, JSONObject.class).getBody();

    }
}
