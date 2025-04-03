package com.tosak.authos.exceptions.unauthorized;

public class InvalidPPIDHashException extends HttpUnauthorizedException {
    public InvalidPPIDHashException(String message) {
        super(message);
    }
}
