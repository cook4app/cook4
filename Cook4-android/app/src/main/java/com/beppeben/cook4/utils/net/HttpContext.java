// adapted from https://github.com/m-reza-rahman/javaee-mobile

package com.beppeben.cook4.utils.net;

import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class HttpContext {

    private static HttpContext instance = null;

    private String baseUrl;

    private HttpHeaders defaultHeaders = new HttpHeaders();

    private HttpContext() {
    }

    public static void removeInstance() {
        instance = null;
    }

    public static HttpContext getInstance() {
        if (instance == null) {
            instance = new HttpContext();
        }
        return instance;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HttpHeaders getDefaultHeaders() {
        return defaultHeaders;
    }

    public RestTemplate getDefaultRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.setRequestFactory(new OkHttpFactory());
        return restTemplate;
    }

    public static RestTemplate getRestTemplate() {
        return getInstance().getDefaultRestTemplate();
    }

}
