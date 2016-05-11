package com.beppeben.cook4.utils.net;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

//class allowing to use self-signed certificates
public class MyTrustManager implements X509TrustManager {

    private X509TrustManager defaultTrustManager;
    private X509TrustManager localTrustManager;

    public MyTrustManager(KeyStore localKeyStore) {
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf;
        TrustManagerFactory tmf1;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf1 = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(localKeyStore);
            tmf1.init((KeyStore) null);
            localTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            defaultTrustManager = (X509TrustManager) tmf1.getTrustManagers()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            localTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {

            defaultTrustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
