package com.tosak.authos.exceptions;

public class InvalidAuthorizationCodeCredentials extends RuntimeException {
    public InvalidAuthorizationCodeCredentials(String message) {
        super(message);
    }
}
