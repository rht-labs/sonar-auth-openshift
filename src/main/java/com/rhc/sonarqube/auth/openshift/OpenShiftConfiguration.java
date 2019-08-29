package com.rhc.sonarqube.auth.openshift;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.String.valueOf;
import static org.sonar.api.PropertyType.BOOLEAN;
import static org.sonar.api.PropertyType.STRING;

import java.io.BufferedReader;
import java.io.File;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Splitter;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.server.ServerSide;

@ServerSide
public class OpenShiftConfiguration {
	static final Logger LOGGER = Logger.getLogger(OpenShiftConfiguration.class.getName());

	private final Configuration config;
	private static final String DEFAULT_SCOPE = "user:info user:check-access";
	private static final String DEFAULT_GROUPS = "admin=sonar-administrators,edit=sonar-users,view=sonar-users";
	private static final String DEFAULT_SERVICEACCOUNT_DIRECTORY = "/run/secrets/kubernetes.io/serviceaccount";
	
	private static final String SERVICEACCOUNT_DIRECTORY_KEY = "kubernetes.service.account.dir";
    
    private static final String USER_URI = "oapi/v1/users/~";
	private static final String SAR_URI = "oapi/v1/subjectaccessreviews";
	private static final String ROUTE_URI = "%sapis/route.openshift.io/v1/namespaces/%s/routes/%s";
	private static final String CATEGORY = "OpenShift-Auth";
	private static final String SUBCATEGORY = "Authentication";
	
	private static final String WEB_URL = "sonar.auth.openshift.webUrl";
	private static final String API_URL = "kubernetes.service";
	private static final String IS_ENABLED = "sonar.auth.openshift.isEnabled";
	private static final String BUTTON_COLOR = "sonar.auth.openshift.button.color";	
	private static final String NAMESPACE = "namespace";
	private static final String TOKEN = "token";
	private static final String CA_CRT = "ca.crt";
	
	private static final String CLIENT_ID = "system:serviceaccount:%s:%s";
	
	
	private String serviceAccountName;
	
	public OpenShiftConfiguration(Configuration config) {
		 this.config = config;
	}	
	
	public String getCert() {
		return CA_CRT;
	}	
		
	public String getSarURI() {
		return getApiURL() + SAR_URI;
	}
	
	public String getUserURI() {
		return getApiURL() + USER_URI;
	}
	
	public String getDefaultScope() {
		return DEFAULT_SCOPE;
	}
	
	public String getServiceAccountName() {
		return this.serviceAccountName;
	}
	
	public void setServicAccountName(String serviceAccountName) {
		this.serviceAccountName = serviceAccountName;
	}
	
	public String getNamespace() throws IOException {  
		return serviceAccountBufferReader(NAMESPACE);
	}
	
	public boolean isEnabled() {
		return config.getBoolean(IS_ENABLED).orElse(true);
	}
		
	public String getOpenShiftServiceAccountDirectory() {		
		return config.get(SERVICEACCOUNT_DIRECTORY_KEY).orElse(DEFAULT_SERVICEACCOUNT_DIRECTORY);
	}

	public boolean getAllowUsersToSignUp() {
		return false;
	}

	public Map<String, String> getSARGroups() {
		String mapAsString = config.get("sonar.auth.openshift.sar.groups").orElse(DEFAULT_GROUPS);
		return Splitter.on(",").withKeyValueSeparator("=").split(mapAsString);
	}
	
	public String getApiURL() {
		LOGGER.fine("API url: " + config.get(API_URL).orElse("API url not set"));
		return config.get(API_URL).orElse(null);
	}
		
	public String getClientId() throws IOException {		
		return String.format(CLIENT_ID, getNamespace(), getServiceAccountName());
	  }

	public String getClientSecret() throws IOException {	 
	     return serviceAccountBufferReader(TOKEN);
	}
	
	public String getButtonColor() {	
		return config.get(BUTTON_COLOR).orElse("#666666");
	}

	public String getRouteURL(String namespace) {
		return String.format(ROUTE_URI, getApiURL(), namespace, "sonarqube");
	}
	
	public static List<PropertyDefinition> definitions() {
	    int index = 1;
	    return Arrays.asList(
	      PropertyDefinition.builder(IS_ENABLED)
	        .name("Login enabled")
	        .description("Enable OpenShift users to login. Value is ignored and treated as default if "
	        		+ "client ID and client secret cannot be defined.")
	        .category(CATEGORY)
	        .subCategory(SUBCATEGORY)
	        .type(BOOLEAN)
	        .defaultValue(valueOf(false))
	        .index(index++)
	        .build(),
	        PropertyDefinition.builder(BUTTON_COLOR)
	        .name("Login button color")
	        .description("Set the hex color of the login button. Default is grey")
	        .category(CATEGORY)
	        .subCategory(SUBCATEGORY)
	        .defaultValue("#666666")
	        .type(STRING)
	        .index(index++)
	        .build(),       
	      PropertyDefinition.builder(API_URL)
	        .name("The API url for an OpenShift instance.")
	        .description("The API url for an OpenShift instance. By default this plugin will look it up.")
	        .category(CATEGORY)
	        .subCategory(SUBCATEGORY)
	        .type(STRING)
	        .index(index++)
	        .build(),
	      PropertyDefinition.builder(WEB_URL)
	        .name("The WEB url for a OpenShift instance.")
	        .description("The Web url for an OpenShift instance. By default this plugin will determine the value")
	        .category(CATEGORY)
	        .subCategory(SUBCATEGORY)
	        .type(STRING)
	        .index(index)
	        .build());	      
	  }
	 
	 private String serviceAccountBufferReader(String directory) throws FileNotFoundException, IOException{				            
		    BufferedReader bufferReader = new BufferedReader(
					new FileReader(new File(getOpenShiftServiceAccountDirectory(), directory)));	        
		    String id = bufferReader.readLine();          	    
		    bufferReader.close();		    
			return id;
	 }	
}
