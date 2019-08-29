package com.rhc.sonarqube.auth.openshift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;

import com.github.scribejava.core.builder.api.OAuth2SignatureType;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.rhc.sonarqube.auth.openshift.test.MockHttpClient;

/**
 * OpenShiftIdentityProviderTest
 */
@RunWith(MockitoJUnitRunner.class)

public class OpenShiftIdentityProviderTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

    @Mock
    private OpenShiftScribeApi scribeApi;
    
    @Mock
    private OAuth20Service oauth2Service;
    
    @Mock
    private HttpClient httpClient;
    
    private OAuth20Service oauth2Service2;
    
    @Mock
    private 
    OAuth2IdentityProvider.InitContext context;
    
    private OpenShiftIdentityProvider testProvider;

    private MapSettings settings = new MapSettings(new PropertyDefinitions((OpenShiftConfiguration.definitions())));
    private OpenShiftConfiguration config = new OpenShiftConfiguration(new ConfigurationBridge(settings));
    
    @Before
    public void preTest() throws Exception {
    	
    	MockitoAnnotations.initMocks(this);
        settings.setProperty("sonar.auth.openshift.isEnabled", true);
        settings.setProperty("kubernetes.service.account.dir", "src/test/resources");
        settings.setProperty("kubernetes.service", "http://127.0.0.1/");
        
        OAuthConfig c = new OAuthConfig(config.getClientId(), config.getNamespace(), "http://local/callback/", null,
        		null, null, "code", null, null, new MockHttpClient());
        
        oauth2Service2 = new OAuth20Service(scribeApi, c);
        
        when(scribeApi.createService(any())).thenReturn(oauth2Service2);
        
        when(scribeApi.getSignatureType()).thenReturn(OAuth2SignatureType.BEARER_AUTHORIZATION_REQUEST_HEADER_FIELD);
        
//        doReturn(new Response(200, "message", new HashMap<String, String>(), new FileInputStream("src/test/resources/service_account_user.json")))
//		.when(oauth2Service).execute(argThat(req -> req.getUrl().equals("http://127.0.0.1/oapi/v1/users/~")));
//        
//        doReturn(new Response(200, "message1", new HashMap<String, String>(), new FileInputStream("src/test/resources/route.json")))
//		.when(oauth2Service).execute(argThat(req -> req.getUrl().equals("http://127.0.0.1/apis/route.openshift.io/v1/namespaces/sqube/routes/sonarqube")));
        testProvider = new OpenShiftIdentityProvider(config, scribeApi);
               
    }

    @Test
    public void checkFields() {
    	
        assertEquals("openshift", testProvider.getKey());
        assertEquals("OpenShift", testProvider.getName());
        assertEquals("/static/authopenshift/openshift.svg", testProvider.getDisplay().getIconPath());
        assertEquals("#666666", testProvider.getDisplay().getBackgroundColor());
    }
    
    @Test
    public void isEnabled() {
    	settings.setProperty("sonar.auth.openshift.isEnabled", true);
    	assertTrue(testProvider.isEnabled());
    	settings.setProperty("sonar.auth.openshift.isEnabled", false);
    	assertFalse(testProvider.isEnabled());
    }
    
    @Test
    public void init() {
    	settings.setProperty("sonar.auth.openshift.isEnabled", true);
        when(context.generateCsrfState()).thenReturn("state");
        when(scribeApi.getAuthorizationUrl(any(), any())).thenCallRealMethod();
        when(scribeApi.getAuthorizationBaseUrl()).thenReturn("http://localhost/authurl");

        testProvider.init(context);

        verify(context).redirectTo("http://localhost/authurl?response_type=code&client_id=system%3Aserviceaccount%3Asqube%3Anull&redirect_uri=http%3A%2F%2Flocal%2Fcallback%2F");

    }
    
    @Test
    public void failToInitWhenDisabled() {
    	settings.setProperty("sonar.auth.openshift.isEnabled", false);

    	thrown.expect(IllegalStateException.class);
    	thrown.expectMessage("OpenShift authentication is disabled");	
    	testProvider.init(context);
    }
}