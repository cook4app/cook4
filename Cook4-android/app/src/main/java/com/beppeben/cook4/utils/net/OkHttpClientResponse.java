//copied and slightly adapted from https://gist.github.com/dodgex/6048213

package com.beppeben.cook4.utils.net;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.zip.GZIPInputStream;

final class OkHttpClientResponse implements ClientHttpResponse {

    private final HttpURLConnection connection;
    private static final String AUTH_ERROR = "Received authentication challenge is null";
    private static final String AUTH_ERROR_JELLY_BEAN = "No authentication challenges found";
    private static final String PROXY_AUTH_ERROR = "Received HTTP_PROXY_AUTH (407) code while not using proxy";
    private HttpHeaders headers;
    private GZIPInputStream compressedBody;

    public OkHttpClientResponse(HttpURLConnection connection) {
        this.connection = connection;
    }

    private int handleIOException(IOException ex) throws IOException {
        if (AUTH_ERROR.equals(ex.getMessage()) || AUTH_ERROR_JELLY_BEAN.equals(ex.getMessage())) {
            return HttpStatus.UNAUTHORIZED.value();
        } else if (PROXY_AUTH_ERROR.equals(ex.getMessage())) {
            return HttpStatus.PROXY_AUTHENTICATION_REQUIRED.value();
        } else {
            throw ex;
        }
    }

    @Override
    public InputStream getBody() throws IOException {
        InputStream errorStream = this.connection.getErrorStream();
        InputStream body = (errorStream != null ? errorStream : this.connection.getInputStream());

        if (isCompressed()) {
            return getCompressedBody(body);
        }
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = new HttpHeaders();
            // Header field 0 is the status line for most HttpURLConnections, but not on GAE
            String name = this.connection.getHeaderFieldKey(0);
            if (StringUtils.hasLength(name)) {
                this.headers.add(name, this.connection.getHeaderField(0));
            }
            int i = 1;
            while (true) {
                name = this.connection.getHeaderFieldKey(i);
                if (!StringUtils.hasLength(name)) {
                    break;
                }
                this.headers.add(name, this.connection.getHeaderField(i));
                i++;
            }
        }
        return this.headers;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        try {
            return this.connection.getResponseCode();
        } catch (IOException ex) {
            return handleIOException(ex);
        }
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.valueOf(getRawStatusCode());
    }

    @Override
    public String getStatusText() throws IOException {
        try {
            return this.connection.getResponseMessage();
        } catch (IOException ex) {
            return HttpStatus.valueOf(handleIOException(ex)).getReasonPhrase();
        }
    }

    @Override
    public void close() {
        if (this.compressedBody != null) {
            try {
                this.compressedBody.close();
            } catch (IOException e) {
                // ignore
            }
        }
        this.connection.disconnect();
    }

    private boolean isCompressed() {
        List<ContentCodingType> contentCodingTypes = this.getHeaders().getContentEncoding();
        for (ContentCodingType contentCodingType : contentCodingTypes) {
            if (contentCodingType.equals(ContentCodingType.GZIP)) {
                return true;
            }
        }
        return false;
    }

    private InputStream getCompressedBody(InputStream body) throws IOException {
        if (this.compressedBody == null) {
            this.compressedBody = new GZIPInputStream(body);
        }
        return this.compressedBody;
    }
}