package com.databo3.gateway.exception;

public class UserInBlackListException extends RuntimeException {
    private static final String MESSAGE = "user in blackList : ";
    public UserInBlackListException(String message) {
        super(MESSAGE + message);
    }
}