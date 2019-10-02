package com.rhc.sonarqube.auth.openshift;

import org.sonar.api.server.ServerSide;

import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.logging.Logger;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;


@ServerSide
public class OpenShiftScribeApi extends DefaultApi20 {
	
	static final Logger LOGGER = Logger.getLogger(OpenShiftIdentityProvider.class.getName());

	private String wellKnownDefaultUrl = "https://openshift.default.svc";
	private String wellKnownURI = "/.well-known/oauth-authorization-server";

    public String issuer;  //console url 3.11
    public String authorization_endpoint; //issuer + auth
	public String token_endpoint; //issuer + token
	
	public OpenShiftScribeApi() {
		try {
			getWellKnown();
		} catch (IOException io) {
			LOGGER.severe("Unable to get well known oauth2 information");
			throw new IllegalStateException(io);
		}
	}

	private void getWellKnown() throws IOException {
		LOGGER.fine("Getting well known");
		Config.defaultClient();
		OkHttpClient ok = Config.defaultClient().getHttpClient();

		Request okrequest = new Request.Builder().url(wellKnownDefaultUrl + wellKnownURI)
			.addHeader("Accept", "application/json").build();

		com.squareup.okhttp.Response okresponse = ok.newCall(okrequest).execute();
		String json = okresponse.body().string();
		LOGGER.fine(String.format("Well known response: %s", json));

		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		issuer = jsonObject.get("issuer").getAsString();
		authorization_endpoint = jsonObject.get("authorization_endpoint").getAsString();
		token_endpoint = jsonObject.get("token_endpoint").getAsString();

	}

    @Override
    public String toString() {
        return "OpenShiftProviderInfo: issuer: " + issuer + " auth ep: "
                + authorization_endpoint + " token ep: " + token_endpoint;
    }

    @Override
    public String getAccessTokenEndpoint() {
		LOGGER.fine("getting access token endpint " + token_endpoint);
        return token_endpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
		LOGGER.fine("getting auth endpint " + authorization_endpoint);
        return authorization_endpoint;
    }

}
