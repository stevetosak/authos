package com.tosak.authos.exceptions;

public class RedisKeyExpiredException extends RuntimeException {
    public RedisKeyExpiredException(String message) {
        super(message);
    }
}
