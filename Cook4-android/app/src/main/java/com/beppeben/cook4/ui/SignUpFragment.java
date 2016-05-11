package com.beppeben.cook4.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4SignUpRequest;
import com.beppeben.cook4.utils.GcmUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

public class SignUpFragment extends Fragment implements OnClickListener {

    private EditText userText, passText, passConText, emailText;
    private String username, password, passrep, email;
    private Button signIn;
    private EmailValidator validator;
    private boolean forgotpass;

    public SignUpFragment() {
    }

    public static SignUpFragment newInstance(boolean forgotpass) {
        SignUpFragment fragment = new SignUpFragment();
        fragment.forgotpass = forgotpass;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        validator = EmailValidator.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_signup, container, false);

        userText = (EditText) root.findViewById(R.id.username);
        emailText = (EditText) root.findViewById(R.id.email);
        passText = (EditText) root.findViewById(R.id.password);
        passConText = (EditText) root.findViewById(R.id.rep_password);
        signIn = (Button) root.findViewById(R.id.signin);
        signIn.setOnClickListener(this);
        if (forgotpass) {
            userText.setVisibility(View.GONE);
            signIn.setText(getString(R.string.reset_password));
        }

        return root;
    }

    @Override
    public void onClick(View arg0) {

        switch (arg0.getId()) {
            case R.id.signin:
                username = userText.getText().toString();
                password = passText.getText().toString();
                passrep = passConText.getText().toString();
                email = emailText.getText().toString();

                if (username.equals("") && !forgotpass) {
                    Toast.makeText(getActivity(), getString(R.string.insert_username), Toast.LENGTH_LONG).show();
                    break;
                }
                if (password.equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.choose_password), Toast.LENGTH_LONG).show();
                    break;
                }
                if (passrep.equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.repeat_password), Toast.LENGTH_LONG).show();
                    break;
                }
                if (!password.equals(passrep)) {
                    Toast.makeText(getActivity(), getString(R.string.passwords_notequal), Toast.LENGTH_LONG).show();
                    break;
                }
                if (email.equals("") || !validator.isValid(email)) {
                    Toast.makeText(getActivity(), getString(R.string.insert_valid_email), Toast.LENGTH_LONG).show();
                    break;
                }

                new SignUpQuery().execute();
        }
    }

    class SignUpQuery extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;
        private String LOG_TAG = "SignUpQueryTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.signing_up) + "...");
        }

        protected String doInBackground(String... urls) {

            if (!GcmUtils.registerGCM(getActivity().getApplicationContext())) return null;

            C4SignUpRequest request = new C4SignUpRequest(username, password, email, Globals.getMe(getActivity()).getRegid());
            request.setLanguage(Locale.getDefault().getDisplayLanguage(Locale.UK));
            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "signup/";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4SignUpRequest>(request, context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
                Log.d(LOG_TAG, "Sending sign up query for " + request.getUsername());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return response;
        }

        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response == null || getActivity() == null) return;
            Utils.hideKeyboard(getActivity());
            if (response.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.signup_successful), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();

            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_signing_up), Toast.LENGTH_LONG).show();
            }
        }
    }
}

