package com.beppeben.cook4.utils.net;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class OkHttpFactory implements ClientHttpRequestFactory {

    private final OkHttpClient client = new OkHttpClient();
    private OkUrlFactory factory;
    private int connectTimeout = -1;
    private int readTimeout = -1;

    public OkHttpFactory() {
        configureClient();
    }

    private void configureClient() {
        client.setSslSocketFactory(SslUtils.factory);
        client.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        factory = new OkUrlFactory(client);
    }

    @Override
    public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
        final HttpURLConnection connection = factory.open(uri.toURL());
        prepareConnection(connection, httpMethod.name());
        return new OkHttpClientRequest(connection);
    }


    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (this.connectTimeout >= 0) {
            connection.setConnectTimeout(this.connectTimeout);
        }
        if (this.readTimeout >= 0) {
            connection.setReadTimeout(this.readTimeout);
        }
        connection.setDoInput(true);
        if ("GET".equals(httpMethod)) {
            connection.setInstanceFollowRedirects(true);
        } else {
            connection.setInstanceFollowRedirects(false);
        }
        if ("PUT".equals(httpMethod) || "POST".equals(httpMethod)) {
            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }
        connection.setRequestMethod(httpMethod);
    }
} 