package com.rhc.sonarqube.auth.openshift.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest.ResponseConverter;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

public class MockHttpClient implements HttpClient{

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			byte[] bodyContents, OAuthAsyncRequestCallback<T> callback, ResponseConverter<T> converter) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			String bodyContents, OAuthAsyncRequestCallback<T> callback, ResponseConverter<T> converter) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			File bodyContents, OAuthAsyncRequestCallback<T> callback, ResponseConverter<T> converter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			byte[] bodyContents) throws InterruptedException, ExecutionException, IOException {
		
		if(completeUrl.contentEquals("http://127.0.0.1/oapi/v1/users/~")) {
			return new Response(200, "message", new HashMap<String, String>(), 
					new FileInputStream("src/test/resources/service_account_user.json"));
		} else if(completeUrl.contentEquals("http://127.0.0.1/apis/route.openshift.io/v1/namespaces/sqube/routes/sonarqube")) {
			return new Response(200, "message1", new HashMap<String, String>(), 
					new FileInputStream("src/test/resources/route.json"));
		}
		
		return null;
	}

	@Override
	public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			String bodyContents) throws InterruptedException, ExecutionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl,
			File bodyContents) throws InterruptedException, ExecutionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
