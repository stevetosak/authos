package com.tosak.authos.exceptions.unauthorized;

public class HttpUnauthorizedException extends RuntimeException {
    public HttpUnauthorizedException(String message) {
        super(message);
    }
}
