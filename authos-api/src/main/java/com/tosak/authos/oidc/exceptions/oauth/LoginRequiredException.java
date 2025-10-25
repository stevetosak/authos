package com.tosak.authos.oidc.exceptions.oauth;

public class LoginRequiredException extends OAuthException {
    private static final String code = "login_required";

    public LoginRequiredException(String redirectUri,String state) {
        super(code,redirectUri,state);
    }
}
