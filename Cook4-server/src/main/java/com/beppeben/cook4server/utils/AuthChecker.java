package com.beppeben.cook4server.utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import static com.beppeben.cook4server.utils.Configs.*;

public class AuthChecker {

    private final String mAudience;
    private final GoogleIdTokenVerifier mVerifier;
    private final JsonFactory mJFactory;
    private String mProblem = "ERROR_AUTH_FAILED";

    private static final Logger logger = Logger
            .getLogger(AuthChecker.class.getName());

    public AuthChecker() {
        mAudience = GOOGLE_CLIENT_ID;
        mJFactory = new GsonFactory();
        mVerifier = new GoogleIdTokenVerifier(new NetHttpTransport(), mJFactory);
    }

    public Boolean googleCheck(String tokenString, String email) {
        if (tokenString == null) {
            logger.log(Level.INFO, "null google token");
            return false;
        }

        try {
            JsonWebSignature jws = JsonWebSignature.parser(mJFactory).setPayloadClass(Payload.class).parse(tokenString);
            GoogleIdToken token = new GoogleIdToken(jws.getHeader(), (Payload) jws.getPayload(), jws.getSignatureBytes(), jws.getSignedContentBytes()) {
                public boolean verify(GoogleIdTokenVerifier verifier)
                        throws GeneralSecurityException, IOException {
                    try {
                        return verifier.verify(this);
                    } catch (java.security.SignatureException e) {
                        return false;
                    }
                }
            };

            if (mVerifier.verify(token)) {
                GoogleIdToken.Payload tempPayload = token.getPayload();
                if (!tempPayload.getAudience().equals(mAudience)) {
                    logger.log(Level.INFO, "wrong token audience: {0}", tempPayload.getAudience());
                    return false;
                }
                if (!tempPayload.getEmailVerified()) {
                    logger.log(Level.INFO, "email verified: {0}", tempPayload.getEmailVerified());
                    return false;
                }
                if (!tempPayload.getEmail().equals(email)) {
                    logger.log(Level.INFO, "token email: {0}", tempPayload.getEmail());
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "exception parsing google token");
            logger.log(Level.INFO, e.getMessage());
            return false;
        }

        return false;
    }

    public Boolean facebookCheck(String token, String email) {
        boolean result = false;

        String url = FB_GRAPH_BASE + "debug_token?input_token=" + token
                + "&access_token=" + FB_APP_ID + "|" + FB_APP_SECRET;
        String json = null;

        try {
            json = Utils.sendHttpGet(url);
        } catch (Exception e) {
        }

        if (json == null) {
            return false;
        }
        JSONObject jsonObject = new JSONObject(json);
        String id = jsonObject.getJSONObject("data").optString("user_id");
        if (id.equals("")) {
            return false;
        }

        url = FB_GRAPH_BASE + id + "?input_token=" + token
                + "&access_token=" + FB_APP_ID + "|" + FB_APP_SECRET;
        json = null;
        try {
            json = Utils.sendHttpGet(url);
        } catch (Exception e) {
        }
        if (json == null) {
            return false;
        }
        jsonObject = new JSONObject(json);
        String em = jsonObject.optString("email");

        result = em.equals(email);

        mProblem = "BAD_FB_TOKEN";

        return result;
    }

    public Boolean check(String tokenString, String email, String method) {
        if (method.equals("google")) {
            return googleCheck(tokenString, email);
        } else if (method.equals("facebook")) {
            return facebookCheck(tokenString, email);
        } else {
            return null;
        }
    }

    public String problem() {
        return mProblem;
    }

}
