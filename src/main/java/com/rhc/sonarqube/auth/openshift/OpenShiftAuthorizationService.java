package com.rhc.sonarqube.auth.openshift;

public class OpenShiftAuthorizationService {

	
	public static OpenShiftAuthorizationService instance() {
		OpenShiftAuthorizationService service = new OpenShiftAuthorizationService();
		
		return service;
	}
}
