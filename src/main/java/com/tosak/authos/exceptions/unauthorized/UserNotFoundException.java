package com.tosak.authos.exceptions.unauthorized;

public class UserNotFoundException extends HttpUnauthorizedException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
