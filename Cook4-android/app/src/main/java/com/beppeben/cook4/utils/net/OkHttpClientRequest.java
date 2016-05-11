//copied and slightly adapted from https://gist.github.com/dodgex/6048213

package com.beppeben.cook4.utils.net;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

final class OkHttpClientRequest implements ClientHttpRequest {
    private final HttpURLConnection connection;
    private final HttpHeaders headers = new HttpHeaders();

    private boolean executed = false;

    private GZIPOutputStream compressedBody;
    private OutputStream body;

    public OkHttpClientRequest(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public OutputStream getBody() throws IOException {
        if (this.body == null) {
            writeHeaders(headers);
            this.connection.connect();
            this.body = this.connection.getOutputStream();
        }
        if (shouldCompress()) {
            return getCompressedBody(this.body);
        } else {
            return this.body;
        }
    }

    @Override
    public HttpHeaders getHeaders() {
        return (this.executed ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
    }

    @Override
    public URI getURI() {
        try {
            return this.connection.getURL().toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Could not get HttpURLConnection URI: " + ex.getMessage(), ex);
        }
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(this.connection.getRequestMethod());
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        try {
            if (this.body != null) {
                this.body.close();
            } else {
                writeHeaders(headers);
                this.connection.connect();
            }
        } catch (IOException ex) {
            // ignore
        }

        this.executed = true;
        return new OkHttpClientResponse(connection);
    }

    private boolean shouldCompress() {
        List<ContentCodingType> contentCodingTypes = this.headers.getContentEncoding();
        for (ContentCodingType contentCodingType : contentCodingTypes) {
            if (contentCodingType.equals(ContentCodingType.GZIP)) {
                return true;
            }
        }
        return false;
    }

    private OutputStream getCompressedBody(OutputStream body) throws IOException {
        if (this.compressedBody == null) {
            this.compressedBody = new GZIPOutputStream(body);
        }
        return this.compressedBody;
    }

    private void writeHeaders(HttpHeaders headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            for (String headerValue : entry.getValue()) {
                this.connection.addRequestProperty(headerName, headerValue);
            }
        }
    }
}