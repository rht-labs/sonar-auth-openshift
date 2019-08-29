package com.rhc.sonarqube.auth.openshift;

import java.util.ArrayList;
import java.util.List;
import com.google.api.client.util.Key;
import com.google.gson.Gson;

public class OpenShiftSubjectAccessReviewRequest {

    public static final String SUBJECT_ACCESS_REVIEW = "SubjectAccessReview";
    public static final String API_VERSION = "v1";
    public static final String RESOURCE = "sonarqube";

    public OpenShiftSubjectAccessReviewRequest() {
    	
         kind = SUBJECT_ACCESS_REVIEW;
         apiVersion = API_VERSION;
         namespace = null;
         verb = null;
         resourceAPIGroup = "";
         resourceAPIVersion = "";
         resource = RESOURCE;
         resourceName = "";
         content = null;
         user = "";
         groups = new ArrayList<String>();
         scopes = new ArrayList<String>();       
    }
    
    public static String createJsonRequest(String verb, String namespace) {
        OpenShiftSubjectAccessReviewRequest req = new OpenShiftSubjectAccessReviewRequest();
        req.verb = verb;
        req.namespace = namespace;

        return new Gson().toJson(req);
    }
    
    @Key
    public String kind;

    @Key
    public String apiVersion;

    @Key
    public String namespace;

    @Key
    public String verb;

    @Key
    public String resourceAPIGroup;

    @Key
    public String resourceAPIVersion;

    @Key
    public String resource;

    @Key
    public String resourceName;

    @Key
    public String content;

    @Key
    public String user;

    @Key
    public List<String> groups;

    @Key
    public List<String> scopes;
}














