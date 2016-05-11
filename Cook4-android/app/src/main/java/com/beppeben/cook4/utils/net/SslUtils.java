package com.beppeben.cook4.utils.net;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SslUtils {

    private static final String LOG_TAG = "Login class";
    public static SSLSocketFactory factory;

    public static void setSelfSignedCertSSLContext(AssetManager assets)
            throws Exception {
        // Load self-signed cert from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = assets.open("self-signed-cert");
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);

        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        SSLContext.setDefault(context);

        MyTrustManager myTrustManager = new MyTrustManager(keyStore);
        TrustManager[] tms = new TrustManager[]{myTrustManager};

        context.init(null, tms, null);

        factory = context.getSocketFactory();

        HttpsURLConnection.setDefaultSSLSocketFactory(factory);

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        });

        Log.d(LOG_TAG, "SSLContext set successfully");
    }

}
