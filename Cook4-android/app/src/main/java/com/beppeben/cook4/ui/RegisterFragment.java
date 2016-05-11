package com.beppeben.cook4.ui;


import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.TCActivity;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.Globals;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONObject;

import java.util.Arrays;

public class RegisterFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, OnClickListener,
        ResultCallback<LoadPeopleResult>, GraphRequest.GraphJSONObjectCallback, FacebookCallback<LoginResult> {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int RC_SIGN_IN = 0;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignUpEmail, btnConfirmSignInEmail;
    private LinearLayout btnSignInGoogle, btnSignInFacebook, btnSignInEmail;
    private View emailPanel;
    private EditText emailText, passText;
    private String username;
    private String email;
    private String password;
    private String loginMethod;
    private Boolean passConfirmed;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private boolean switched = false;
    private EmailValidator validator;
    private CallbackManager fbCallbackManager;
    private TextView tcText, forgotPassText, signInEmailText;
    private CheckBox acceptTCBox;
    private C4User me;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        Globals.init(getActivity());
        fbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbCallbackManager, this);
        validator = EmailValidator.getInstance();
        me = Globals.getMe(getActivity());

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = sharedPref.edit();

        passConfirmed = sharedPref.getBoolean("pass_confirmed", false);
        username = me.getName();
        email = me.getEmail();
        loginMethod = me.getLoginMethod() != null ? me.getLoginMethod() : "";

        if (!checkPlayServices()) return;
        initializeGoogleApiClient(false);
        if (loginToFbIfBadToken()) return;
        if ((loginMethod.equals("google") && email != null) ||
                (loginMethod.equals("email") && passConfirmed) ||
                (loginMethod.equals("facebook") && email != null)) {
            switchToMain();
            switched = true;
        }
    }

    private boolean loginToFbIfBadToken() {
        if (getActivity().getIntent().getExtras() != null) {
            String forceFBLogin = getActivity().getIntent().getExtras().getString("FORCE_FB_LOGIN");
            if (forceFBLogin != null && forceFBLogin.equals("ok")) {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
                return true;
            }
        }
        return false;
    }

    private void initializeGoogleApiClient(boolean force) {
        if (!force && !loginMethod.equals("google")) return;
        Globals.mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("resuming");
        initializeGoogleApiClient(false);
        if (switched) {
            getActivity().finish();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_register, container, false);

        btnSignInGoogle = (LinearLayout) root.findViewById(R.id.sign_in_google);
        btnSignInFacebook = (LinearLayout) root.findViewById(R.id.sign_in_facebook);
        btnSignInEmail = (LinearLayout) root.findViewById(R.id.sign_in_email);
        emailPanel = (View) root.findViewById(R.id.email_panel);
        btnConfirmSignInEmail = (Button) root.findViewById(R.id.btn_sign_in_email);
        btnSignUpEmail = (Button) root.findViewById(R.id.btn_sign_up_email);
        emailText = (EditText) root.findViewById(R.id.email);
        passText = (EditText) root.findViewById(R.id.password);
        tcText = (TextView) root.findViewById(R.id.tc);
        forgotPassText = (TextView) root.findViewById(R.id.forgot_password);
        signInEmailText = (TextView) root.findViewById(R.id.sign_in_email_text);
        acceptTCBox = (CheckBox) root.findViewById(R.id.tc_accept);
        btnSignInGoogle.setOnClickListener(this);
        btnSignInFacebook.setOnClickListener(this);
        btnSignUpEmail.setOnClickListener(this);
        btnConfirmSignInEmail.setOnClickListener(this);
        btnSignInEmail.setOnClickListener(this);
        emailPanel.setOnClickListener(this);
        tcText.setOnClickListener(this);
        forgotPassText.setOnClickListener(this);

        String email = sharedPref.getString("email_text", "");
        String password = sharedPref.getString("password_text", "");
        emailText.setText(email);
        passText.setText(password);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return root;
    }

    public void onStart() {
        super.onStart();
        if (loginMethod != null && loginMethod.equals("google") && email != null) {
            Globals.mGoogleApiClient.connect();
        }
    }

    @Override
    public void onCompleted(JSONObject object, GraphResponse graphResponse) {
        String fid = object.optString("id");
        email = object.optString("email");
        if (username == null) username = object.optString("name");
        if (email == null || email.equals("")) {
            Toast.makeText(getActivity(), "Could not find a valid email address", Toast.LENGTH_LONG).show();
            return;
        }
        editor.putString("fb_id", fid);
        editor.commit();
        me.setLoginMethod("facebook");
        me.setName(username);
        me.setEmail(email);

        if (getActivity() != null && !switched) switchToMain();
        getActivity().finish();
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        loginMethod = "google";
        me.setLoginMethod("google");

        //tries to fix a strange bug on some phones
        if (!Globals.mGoogleApiClient.isConnected()) {
            Globals.mGoogleApiClient.connect();
            return;
        }

        if (Plus.PeopleApi.getCurrentPerson(Globals.mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(Globals.mGoogleApiClient);
            if (username == null) username = currentPerson.getDisplayName();
            email = Plus.AccountApi.getAccountName(Globals.mGoogleApiClient);
        }

        me.setName(username);
        me.setEmail(email);

        if (getActivity() != null && !switched) switchToMain();
        if (getActivity() != null) getActivity().finish();
    }

    private void switchToMain() {
        Intent i = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) i.putExtras(b);
        startActivity(i);
        if (!loginMethod.equals("google")) {
            getActivity().finish();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), getActivity(), 0).show();
            return;
        }
        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked) {
                resolveSignInError();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int arg0) {
        Globals.mGoogleApiClient.connect();
    }


    private void signInWithEmail() {
        email = emailText.getText().toString();
        password = passText.getText().toString();
        if (email.equals("") || !validator.isValid(email)) {
            Toast.makeText(getActivity(), getString(R.string.insert_valid_email), Toast.LENGTH_LONG).show();
            return;
        }
        if (password.equals("")) {
            Toast.makeText(getActivity(), getString(R.string.insert_password), Toast.LENGTH_LONG).show();
            return;
        }

        editor.putString("password_text", password);
        editor.putString("email_text", email);
        editor.commit();

        loginMethod = "email";
        me.setLoginMethod("email");
        me.setPassword(password);
        me.setEmail(email);
        switchToMain();
    }


    @Override
    public void onClick(View v) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        switch (v.getId()) {

            case R.id.sign_in_email:
                if (emailPanel.getVisibility() == View.VISIBLE) {
                    emailPanel.setVisibility(View.GONE);
                    btnSignInFacebook.setVisibility(View.VISIBLE);
                    btnSignInGoogle.setVisibility(View.VISIBLE);
                    signInEmailText.setText(getString(R.string.login_with_email_btn));
                } else {
                    emailPanel.setVisibility(View.VISIBLE);
                    btnSignInFacebook.setVisibility(View.GONE);
                    btnSignInGoogle.setVisibility(View.GONE);
                    signInEmailText.setText(getString(R.string.back_to_login));
                }
                break;

            case R.id.sign_in_google:
                if (!acceptTCBox.isChecked()) {
                    Toast.makeText(getActivity(), getString(R.string.error_accept_TC), Toast.LENGTH_LONG).show();
                    return;
                }
                initializeGoogleApiClient(true);
                Globals.mGoogleApiClient.connect();
                mSignInClicked = true;
                signInWithGplus();
                break;

            case R.id.sign_in_facebook:
                if (!acceptTCBox.isChecked()) {
                    Toast.makeText(getActivity(), getString(R.string.error_accept_TC), Toast.LENGTH_LONG).show();
                    return;
                }
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
                break;

            case R.id.btn_sign_up_email:
                fragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.container, SignUpFragment.newInstance(false)).commit();
                break;

            case R.id.btn_sign_in_email:
                if (!acceptTCBox.isChecked()) {
                    Toast.makeText(getActivity(), getString(R.string.error_accept_TC), Toast.LENGTH_LONG).show();
                    return;
                }
                signInWithEmail();
                break;

            case R.id.forgot_password:
                fragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.container, SignUpFragment.newInstance(true)).commit();
                break;

            case R.id.tc:
                Intent i = new Intent(getActivity(), TCActivity.class);
                startActivity(i);
                break;
        }
    }

    private void signInWithGplus() {
        if (!Globals.mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(getActivity(), RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                Globals.mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != getActivity().RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;
            if (!Globals.mGoogleApiClient.isConnecting()) {
                Globals.mGoogleApiClient.connect();
            }
        }
        fbCallbackManager.onActivityResult(requestCode, responseCode, data);
    }

    @Override
    public void onResult(LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                Log.e(getClass().getSimpleName(), "People: " + ((Integer) personBuffer.getCount()).toString());
                for (int i = 0; i < count; i++) {
                    Log.d(getClass().getSimpleName(), "Display name: " + personBuffer.get(i).getDisplayName());
                }
            } finally {
                personBuffer.close();
            }
        } else {
            Log.e(getClass().getSimpleName(), "Error requesting visible circles: " + peopleData.getStatus());
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        editor.putString("fb_token", loginResult.getAccessToken().getToken().toString());
        editor.commit();
        GraphRequest.newMeRequest(loginResult.getAccessToken(), this).executeAsync();
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(FacebookException e) {
        Toast.makeText(getActivity(), "Problems logging in: " + e.toString(), Toast.LENGTH_LONG).show();
        LoginManager.getInstance().logOut();
    }
}
