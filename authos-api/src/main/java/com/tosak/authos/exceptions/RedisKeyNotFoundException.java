package com.tosak.authos.exceptions;

public class RedisKeyNotFoundException extends RuntimeException {
    public RedisKeyNotFoundException(String message) {
        super(message);
    }
}
