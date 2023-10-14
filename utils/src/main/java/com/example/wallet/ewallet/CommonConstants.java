package com.example.wallet.ewallet;

public class CommonConstants {

    // kafka related constants

    public static final String TRANSACTION_CREATION_TOPIC = "transaction_created";
    public static final String TRANSACTION_COMPLETED_TOPIC = "transaction_completed";
    public static final String TRANSACTION_UPDATE_TOPIC = "transaction_updated";
    public static final String USER_CREATION_TOPIC = "user_created";
    public static final String USER_CREATION_TOPIC_IDENTIFIER_KEY = "userIdentifier";
    public static final String USER_CREATION_TOPIC_IDENTIFIER_VALUE = "identifierValue";
    public static final String USER_CREATION_TOPIC_USERID = "userId";
    public static final String USER_CREATION_TOPIC_PHONE_NUMBER = "phoneNumber";
    public static final String WALLET_UPDATED_TOPIC = "wallet_updated";


    // data constants
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String AMOUNT = "amount";
    public static final String TRANSACTION_ID = "transactionId";

    public static final String WALLET_UPDATE_STATUS="walletUpdateStatus";

    public static final String EMAIL = "email";

    public static final String SENDER_MESSAGE = "senderMessage";
    public static final String RECEIVER_MESSAGE = "receiverMessage";


}
