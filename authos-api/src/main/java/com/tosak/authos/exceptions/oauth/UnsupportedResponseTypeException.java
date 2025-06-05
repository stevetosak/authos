package com.tosak.authos.exceptions.oauth;

public class UnsupportedResponseTypeException extends RuntimeException {
    public UnsupportedResponseTypeException(String message) {
        super(message);
    }
}
