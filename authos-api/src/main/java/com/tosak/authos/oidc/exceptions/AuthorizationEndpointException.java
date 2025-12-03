package com.tosak.authos.oidc.exceptions;

public class AuthorizationEndpointException extends OAuth2Exception {

    public AuthorizationEndpointException(OidcErrorCode error, String errorDescription, String redirectUri, String state,String nonce) {
        super(error, errorDescription, redirectUri, state,nonce);
    }
    public AuthorizationEndpointException(OidcErrorCode error, String errorDescription, String redirectUri, String state) {
        super(error, errorDescription, redirectUri, state);
    }
    public AuthorizationEndpointException(OidcErrorCode error, String redirectUri, String state) {
        super(error,redirectUri,state);
    }
    public AuthorizationEndpointException(OidcErrorCode error, String errorDescription) {
        super(error,errorDescription);
    }
    public AuthorizationEndpointException(OidcErrorCode errorCode){
        super(errorCode);
    }
}
