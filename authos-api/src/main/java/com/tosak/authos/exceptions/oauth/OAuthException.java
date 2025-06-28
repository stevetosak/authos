package com.tosak.authos.exceptions.oauth;

// TODO samo so redirect uri
public class OAuthException extends RuntimeException {
    public final String code;
    public final String redirectUri;
    public final String state;

    public OAuthException(String code,String redirectUri,String state) {
        this.code = code;
        this.redirectUri = redirectUri;
        this.state = state;
    }
}
