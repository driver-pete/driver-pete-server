package com.otognan.driverpete;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class UnsafeHttpsClient {

	public static HttpClient createUnsafeClient() {
		try {			
	        SSLContextBuilder builder = new SSLContextBuilder();
	        // trust self signed certificate
	        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
	        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
	                builder.build(),
	                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        HttpClient httpClient = HttpClients.custom()
	                .setSSLSocketFactory(sslConnectionSocketFactory).build();

			return httpClient;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
