package com.tosak.authos.oidc.exceptions.internal;

public class RedisKeyNotFoundException extends RuntimeException {
    public RedisKeyNotFoundException(String message) {
        super(message);
    }
}
