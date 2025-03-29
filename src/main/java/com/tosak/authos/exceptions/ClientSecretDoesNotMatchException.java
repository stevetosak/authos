package com.tosak.authos.exceptions;

public class ClientSecretDoesNotMatchException extends RuntimeException {
    public ClientSecretDoesNotMatchException(String message) {
        super(message);
    }
}
