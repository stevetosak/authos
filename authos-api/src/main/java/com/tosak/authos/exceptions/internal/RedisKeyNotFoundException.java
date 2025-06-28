package com.tosak.authos.exceptions.internal;

public class RedisKeyNotFoundException extends RuntimeException {
    public RedisKeyNotFoundException(String message) {
        super(message);
    }
}
