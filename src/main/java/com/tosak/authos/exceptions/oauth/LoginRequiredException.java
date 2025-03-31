package com.tosak.authos.exceptions.oauth;

public class LoginRequiredException extends OAuthException {
    private static final String code = "login_required";

    public LoginRequiredException(String redirectUri,String state) {
        super(code,redirectUri,state);
    }
}
