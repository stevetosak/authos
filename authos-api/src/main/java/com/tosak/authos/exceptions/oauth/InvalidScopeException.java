package com.tosak.authos.exceptions.oauth;

public class InvalidScopeException extends OAuthException {
    private static final String code = "invalid_scope";
    public InvalidScopeException(String redirectUri,String state) {
        super(code,redirectUri,state);
    }
}
