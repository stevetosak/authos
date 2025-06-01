package com.tosak.authos.exceptions;

public class MissingLogoutParameterException extends RuntimeException {
    public MissingLogoutParameterException(String message) {
        super(message);
    }
}
