package com.tosak.authos.oidc.exceptions;

// TODO samo so redirect uri
public class OAuth2Exception extends RuntimeException {
    public final OidcErrorCode error;
    public final String errorDescription;
    public final String redirectUri;
    public final String state;

    public OAuth2Exception(OidcErrorCode error, String errorDescription, String redirectUri, String state) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.redirectUri = redirectUri;
        this.state = state;
    }
    public OAuth2Exception(OidcErrorCode error, String redirectUri, String state) {
        this.error = error;
        this.errorDescription = "";
        this.redirectUri = redirectUri;
        this.state = state;
    }
    public OAuth2Exception(OidcErrorCode error) {
        this.error = error;
        this.errorDescription = "";
        this.redirectUri = null;
        this.state = null;
    }
    public OAuth2Exception(OidcErrorCode error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.redirectUri = null;
        this.state = null;
    }

}
