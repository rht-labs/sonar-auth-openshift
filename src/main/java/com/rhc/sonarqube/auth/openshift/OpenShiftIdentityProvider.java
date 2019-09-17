package com.rhc.sonarqube.auth.openshift;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;

import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;

import io.kubernetes.client.util.Config;

import org.sonar.api.server.authentication.OAuth2IdentityProvider;

import com.google.api.client.util.SecurityUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ServerSide
public class OpenShiftIdentityProvider implements OAuth2IdentityProvider {

	public static final String KEY = "openshift";
	private String todoCallback = "";
	private boolean initializedOpenshift = true;

	private final OpenShiftScribeApi scribeApi;
	private final OpenShiftConfiguration config;

	static final Logger LOGGER = Logger.getLogger(OpenShiftIdentityProvider.class.getName());

	public OpenShiftIdentityProvider(OpenShiftConfiguration config, OpenShiftScribeApi scribeApi) {
		this.config = config;
		this.scribeApi = scribeApi;

		try {
			if(isEnabled()) {
				initOpenShift();
			} else {
				LOGGER.info("AuthOpenShiftPlugin is disabled");
			}
		} catch (Exception ex) {
			initializedOpenshift = false;
			LOGGER.severe("AuthOpenShiftPlugin failed to initialize. Disabling...");
		}
		
		LOGGER.fine("OpenShift API " + scribeApi.toString());
	}

	private void initOpenShift() throws Exception {

		try {
			setKeystore();
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Problem setting up ssl", ex);
			throw ex;
		}

		try {
			getServiceAccountName();
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Problem getting service account", ex);
			throw ex;
		}

		try {
			todoCallback = this.callbackBaseUrl();
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Problem getting callback url (based on pod's service)", ex);
			throw ex;
		}
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "OpenShift";
	}

	@Override
	public Display getDisplay() {
		return Display.builder().setIconPath("/static/authopenshift/openshift.svg")
				.setBackgroundColor(config.getButtonColor()).build();
	}

	@Override
	public boolean isEnabled() {
		return initializedOpenshift && config.isEnabled();
	}

	@Override
	public boolean allowsUsersToSignUp() {
		return true;
	}

	@Override
	public void init(InitContext context) {
		try {
			String state = context.generateCsrfState();

			OAuth20Service scribe = newScribeBuilder().scope(config.getDefaultScope()).state(state).build(scribeApi);
			String url = scribe.getAuthorizationUrl();
			LOGGER.fine("Redirect:" + url);
			context.redirectTo(url);
		} catch (IOException e) {
			LOGGER.severe("Unable to read/write client id and/or client secret from service account.");
			throw new IllegalStateException("Unable to complete init", e);
		}
	}

	@Override
	public void callback(CallbackContext context) {
		try {
			LOGGER.fine("callback!");
			onCallback(context);
		} catch (IOException | InterruptedException | ExecutionException e) {
			throw new IllegalStateException(e);
		}
	}

	private void onCallback(CallbackContext context) throws InterruptedException, ExecutionException, IOException {
		context.verifyCsrfState();
		HttpServletRequest request = context.getRequest();
		OAuth20Service scribe = newScribeBuilder().build(scribeApi);
		String code = request.getParameter("code");
		OAuth2AccessToken accessToken = scribe.getAccessToken(code);

		Set<String> sonarRoles = findSonarRoles(scribe, accessToken);

		LOGGER.fine(String.format("Roles %s", sonarRoles));

		String user = getOpenShiftUser(scribe, accessToken);
		UserIdentity userIdentity = UserIdentity.builder().setGroups(sonarRoles).setProviderLogin(user)
				.setLogin(String.format("%s@%s", user, KEY)).setName(user).build();

		context.authenticate(userIdentity);
		context.redirectToRequestedPage();
	}

	private Set<String> findSonarRoles(OAuth20Service scribe, OAuth2AccessToken accessToken)
			throws IOException, InterruptedException, ExecutionException {
		Response response = null;

		HashSet<String> sonarRoles = new HashSet<String>();
		String namespace = config.getNamespace();

		OAuthRequest request = new OAuthRequest(Verb.POST, config.getSarURI());
		request.addHeader("Content-Type", "application/json");
		scribe.signRequest(accessToken, request);

		for (String accessRole : config.getSARGroups().keySet()) {
			String json = OpenShiftSubjectAccessReviewRequest.createJsonRequest(accessRole, namespace);
			LOGGER.fine("SAR body " + json);
			request.setPayload(json);

			response = scribe.execute(request);

			if (response.isSuccessful()) {
				if (new JsonParser().parse(response.getBody()).getAsJsonObject().get("allowed").getAsBoolean()) {
					sonarRoles.add(config.getSARGroups().get(accessRole));
				}
			} else {
				LOGGER.warning(String.format("SAR Request failed with response message %s", response.getBody()));
			}
		}
		return sonarRoles;
	}

	/**
	 * Get the sonarqube base url by accessing the OpenShift route currently
	 * hard-coded to sonarqube Jenkins does this by making the service name
	 * available to the pod in the env vars
	 * 
	 * @return
	 * @throws IOException
	 */
	private String callbackBaseUrl() throws IOException {
		OAuth20Service scribe = newScribeBuilder().build(scribeApi);
		OAuth2AccessToken token = new OAuth2AccessToken(config.getClientSecret());

		OAuthRequest request = new OAuthRequest(Verb.GET, config.getRouteURL(config.getNamespace()));
		LOGGER.info("Route URL: " + request.getUrl());
		scribe.signRequest(token, request);
		Response response;
		try {
			response = scribe.execute(request);

			if (!response.isSuccessful()) {
				throw new IllegalStateException(
						String.format("Failed to get callback '%s'. HTTP code: %s, response: %s.",
								config.getRouteURL(config.getNamespace()), response.getCode(), response.getBody()));
			}

			String json = response.getBody();
			JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject().get("spec").getAsJsonObject();
			String host = jsonObject.get("host").getAsString();
			host = jsonObject.getAsJsonObject().has("tls") ? "https://" + host : "http://" + host;

			LOGGER.info("Callback Host: " + host);

			return host + "/oauth2/callback/openshift";
		} catch (InterruptedException | ExecutionException ex) {
			throw new IllegalStateException("Faild to get callback url", ex);
		}
	}

	/**
	 * This should be consistent through the lifecycle of the running pod. Called
	 * during object construcion only
	 * 
	 * @throws IOException
	 */
	private void getServiceAccountName() throws IOException {
		OAuth20Service scribe = newScribeBuilder().build(scribeApi);
		OAuth2AccessToken token = new OAuth2AccessToken(config.getClientSecret());
		try {
			String userName = getOpenShiftUser(scribe, token);
			if (userName.indexOf(":") >= 0) {
				config.setServicAccountName(userName.substring(userName.lastIndexOf(":") + 1));
				LOGGER.info(String.format("Service account name '%s'", config.getServiceAccountName()));
			}
		} catch (ExecutionException | InterruptedException e) {
			throw new IllegalStateException("Unable to figure out service account name", e);
		}

	}

	private String getOpenShiftUser(OAuth20Service scribe, OAuth2AccessToken accessToken)
			throws IOException, ExecutionException, InterruptedException {
		OAuthRequest request = new OAuthRequest(Verb.GET, config.getUserURI());
		scribe.signRequest(accessToken, request);
		Response response = scribe.execute(request);

		if (!response.isSuccessful()) {
			throw new IllegalStateException(String.format("Failed to get user '%s'. status: %s, response: %s.",
					config.getUserURI(), response.getCode(), response.getBody()));
		}
		String json = response.getBody();
		LOGGER.fine("User response body ===== " + json);
		String userName = new JsonParser().parse(json).getAsJsonObject().get("metadata").getAsJsonObject().get("name")
				.getAsString();
		return userName;
	}

	private ServiceBuilder newScribeBuilder() throws IOException {
		if (!isEnabled()) {
			throw new IllegalStateException("OpenShift authentication is disabled.");
		}

		return new ServiceBuilder(config.getClientId()).apiSecret(config.getClientSecret()).callback(todoCallback);
	}

	private void setKeystore() throws IOException, GeneralSecurityException {
		KeyStore keyStore = SecurityUtils.getDefaultKeyStore();
		try {
			LOGGER.fine("Keystore size " + keyStore.size());
		} catch (Exception ex) {
			keyStore.load(null);
		}

		FileInputStream fis = new FileInputStream(new File(config.getOpenShiftServiceAccountDirectory(), "ca.crt"));
		SecurityUtils.loadKeyStoreFromCertificates(keyStore, SecurityUtils.getX509CertificateFactory(), fis);
		LOGGER.fine("Keystore loaded");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
		tmf.init(keyStore);
		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(Config.defaultClient().getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		SSLContext.setDefault(ssl);
		LOGGER.fine("SSL context set");
	}
}
