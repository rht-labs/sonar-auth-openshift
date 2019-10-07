package com.rhc.sonarqube.auth.openshift;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Provide a way to circumvent certificate verifcation. Not meant for production use
 * @author mcanoy
 *
 */
public class TrustAllHttpClient {
	static final Logger LOGGER = Logger.getLogger(TrustAllHttpClient.class.getName());

	
	public static OkHttpClient instance() {
		
		final TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					
			        @Override
			        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			        }

			        @Override
			        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			        }

			        @Override
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			          return new java.security.cert.X509Certificate[]{};
			        }
			    }
			};
		
		SSLContext trustAllSslContext = null;
		
		try {
	        trustAllSslContext = SSLContext.getInstance("SSL");
	        trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	    } catch (NoSuchAlgorithmException | KeyManagementException e) {
	        throw new RuntimeException(e);
	    }
		
		final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();
		
		LOGGER.warning("Using the trustAllSslClient is highly discouraged and should not be used in Production!");
		
		OkHttpClient okClient = new OkHttpClient.Builder()
	    	.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0])
	    	.hostnameVerifier(new HostnameVerifier() {
	      @Override
	      public boolean verify(String hostname, SSLSession session) {
	        return true;
	      }
	    }).build();
		
		return okClient;
	}
}
