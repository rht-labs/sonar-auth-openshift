package com.rhc.sonarqube.auth.openshift;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.google.api.client.util.Key;

/**
 * OpenShiftProviderInfo
 */
public class OpenShiftProviderInfo extends DefaultApi20 {

    @Key
    public String issuer;  //console url 3.11

    @Key
    public String authorization_endpoint; //issuer + auth

    @Key
    public String token_endpoint; //issuer + token

    @Override
    public String toString() {
        return "OpenShiftProviderInfo: issuer: " + issuer + " auth ep: "
                + authorization_endpoint + " token ep: " + token_endpoint;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return token_endpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return authorization_endpoint;
    }
}