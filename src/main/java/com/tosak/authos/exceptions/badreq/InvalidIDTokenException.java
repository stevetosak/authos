package com.tosak.authos.exceptions.badreq;

public class InvalidIDTokenException extends HttpBadRequestException {
    public InvalidIDTokenException(String message) {
        super(message);
    }
}
