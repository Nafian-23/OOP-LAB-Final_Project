package com.foodapp.service;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
