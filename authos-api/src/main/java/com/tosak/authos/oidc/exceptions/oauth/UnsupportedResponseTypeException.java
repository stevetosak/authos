package com.tosak.authos.oidc.exceptions.oauth;

public class UnsupportedResponseTypeException extends RuntimeException {
    public UnsupportedResponseTypeException(String message) {
        super(message);
    }
}
