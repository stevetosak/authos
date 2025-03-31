package com.tosak.authos.exceptions.badreq;

public class PromptParseException extends HttpBadRequestException {
    public PromptParseException(String message) {
        super(message);
    }
}
