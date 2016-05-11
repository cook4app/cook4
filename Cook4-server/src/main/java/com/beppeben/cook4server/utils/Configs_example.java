package com.beppeben.cook4server.utils;

public final class Configs_example {

    //base URL for all APIs
    public static final String BASE_URL = "http://cook4.ddns.net/services/";

    //for redirects after email confirmation
    public static final String WELCOME_PAGE = "http://www.cook4app.com/welcome-to-cook4/";

    //payment configs
    public static final String PAYPAL_MODE = "live";
    public static final String PAYPAL_ACCOUNT = "myaccount@gmail.com";
    public static final Double FEE_RATE = 0.15;
    public static final Double FEE_REFUND = 0.06;
    public static final int KEY_EXPIRY_MINUTES = 5;
    public static final String PAYPAL_API_USER = "myapi.example.com";
    public static final String PAYPAL_API_PASS = "ABCDEF";
    public static final String PAYPAL_API_SIGNATURE = "ABCDEF";
    public static final String PAYPAL_APP_ID = "APP-ABCDEF";

    //for user authentication
    public static final String GOOGLE_CLIENT_ID = "ABCDEF.apps.googleusercontent.com";
    public static final String FB_GRAPH_BASE = "https://graph.facebook.com/v2.3/";
    public static final String FB_APP_ID = "ABCDEF";
    public static final String FB_APP_SECRET = "ABCDEF";

    //email used for sending logs/notifications
    public static final String EMAIL_ACCOUNT = "info@gmx.com";
    public static final String EMAIL_PASS = "mypass";
    public static final String SMTP_HOST = "mail.gmx.com";
    public static final String SMTP_PORT = "465";

    //email address receiving debugging messages
    public static final String EMAIL_DEBUG = "debug@gmail.com";

    //email address of the managing group
    public static final String EMAIL_TEAM = "hello@cook4app.com";

    //key for sending GCM notifications
    public static final String GCM_KEY = "ABCDEF";

    //googla maps key for dashboard
    public static final String MAPS_KEY = "ABCDEF";

    //clients running a lower version will be asked to update
    public static final int MIN_ANDROID_VERSION = 42;

    //reputation penalty for cancelling a transaction
    public static final float CANCELLATION_PENALTY = 10;
}
