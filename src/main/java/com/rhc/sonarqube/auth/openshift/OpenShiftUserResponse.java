package com.rhc.sonarqube.auth.openshift;

import java.util.List;

import com.google.gson.Gson;


public class OpenShiftUserResponse {
	public List<String> groups;
	public MetaData metadata;
	
	public OpenShiftUserResponse() {
		
	}
	
	public static OpenShiftUserResponse create(String json) {
		return new Gson().fromJson(json, OpenShiftUserResponse.class);
	}
	
	public String getUserName() {
		if(metadata != null) {
			return metadata.name;
		}
		
		return null;
	}
	
	public boolean isMemberOf(String groupName) {
		if(groups == null) {
			return false;
		}
		
		return groups.contains(groupName);
	}
	
	public class MetaData {
		public String name;
		
		public String toString() {
			return (name);
		}
	}
	
	public String toString() {
		if(metadata == null) {
			return "OpenShiftUserResponse - username not set";
		}
		
		return new StringBuilder("OpenShiftUserResponse: name: ").append(metadata).append(" groups: ").append(groups).toString(); 
	}
	
	public static void main( ) {
		
		
	}

}
