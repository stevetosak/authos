package com.tosak.authos.oidc.exceptions;

public class TokenEndpointException extends OAuth2Exception {

    public TokenEndpointException(OidcErrorCode error, String errorDescription, String redirectUri, String state) {
        super(error, errorDescription, redirectUri, state);
    }
    public TokenEndpointException(OidcErrorCode error, String redirectUri, String state) {
        super(error,redirectUri,state);
    }
    public TokenEndpointException(OidcErrorCode error, String errorDescription) {
        super(error,errorDescription);
    }
    public TokenEndpointException(OidcErrorCode error) {
        super(error);
    }
}
