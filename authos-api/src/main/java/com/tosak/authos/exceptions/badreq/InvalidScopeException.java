package com.tosak.authos.exceptions.badreq;

public class InvalidScopeException extends HttpBadRequestException {
    public InvalidScopeException(String message) {
        super(message);
    }
}
