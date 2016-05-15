package com.beppeben.cook4.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.RegistrationActivity;
import com.beppeben.cook4.domain.C4Image;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.ChatUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.ImageCache;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileFragment extends MyFragment implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String GRAPH_BASE = "https://graph.facebook.com/v2.3/";

    private Button btnChangeName, btnChangeDescription, btnSignOut, btnUserInfo, paypalButton;
    private TextView txtName, txtEmail, txtPrivilege, txtRemoveAccount;
    private RelativeLayout photoContainer;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Bitmap smallImage;
    private ProgressDialog progressDialog;
    private C4User me;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = sharedPref.edit();
        me = Globals.getMe(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_profile, container, false);

        btnChangeName = (Button) root.findViewById(R.id.btn_changename);
        btnChangeDescription = (Button) root.findViewById(R.id.btn_changedescription);
        btnUserInfo = (Button) root.findViewById(R.id.btn_user_info);
        btnSignOut = (Button) root.findViewById(R.id.btn_sign_out);
        txtRemoveAccount = (TextView) root.findViewById(R.id.txt_remove_account);
        paypalButton = (Button) root.findViewById(R.id.btn_paypal);
        txtName = (TextView) root.findViewById(R.id.txtName);
        txtEmail = (TextView) root.findViewById(R.id.txtEmail);
        txtPrivilege = (TextView) root.findViewById(R.id.txtPrivilege);
        photoContainer = (RelativeLayout) root.findViewById(R.id.photocontainer);
        btnSignOut.setOnClickListener(this);
        txtRemoveAccount.setOnClickListener(this);
        btnUserInfo.setOnClickListener(this);
        btnChangeName.setOnClickListener(this);
        btnChangeDescription.setOnClickListener(this);
        paypalButton.setOnClickListener(this);

        txtName.setText(me.getName());
        txtEmail.setText(me.getEmail());
        Utils.showPrivilege(me, txtPrivilege, false, getActivity());

        if (me.getPrivilege() == null || me.getPrivilege().isEmpty() || me.getPrivilege().equals("pending")) {
            paypalButton.setVisibility(View.GONE);
        }

        if (smallImage != null) putImage();
        else {
            if (me.getPhotoId() == null) setAddImage();
            else new GetImageTask(me.getPhotoId()).execute();
        }

        return root;
    }

    private void setAddImage() {
        RelativeLayout addImageView = (RelativeLayout)
                LayoutInflater.from(getActivity()).inflate(R.layout.add_image_layout, null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addImageView.setLayoutParams(params);
        addImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.choose_profile_pic)), 10);
            }
        });
        photoContainer.removeAllViews();
        photoContainer.addView(addImageView);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fragmentManager = getChildFragmentManager();
        switch (v.getId()) {

            case R.id.btn_user_info:
                fragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.profile_root, UserInfoFragment.newInstance(me)).commit();
                break;

            case R.id.btn_changename:
                final EditText newNameText = new EditText(getActivity());
                newNameText.setMaxLines(1);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.change_user_name))
                        .setMessage(getString(R.string.enter_new_username) + ":")
                        .setView(newNameText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newName = newNameText.getText().toString();
                                if (me.getId() != null)
                                    new ChangeNameTask(getActivity(), me.getId(), newName).execute();
                                else
                                    Toast.makeText(getActivity(), "Problems: null id", Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;

            case R.id.btn_changedescription:
                final EditText descriptionText = new EditText(getActivity());
                String descString = me.getDescription();
                if (descString != null) descriptionText.setText(descString);
                descriptionText.setMaxLines(10);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.tell_people_about_you))
                        .setMessage(getString(R.string.enter_description) + ":")
                        .setView(descriptionText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newDescription = descriptionText.getText().toString();
                                if (me.getId() != null)
                                    new ChangeDescriptionTask(getActivity(), me.getId(), newDescription).execute();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;

            case R.id.btn_paypal:
                final EditText paypalText = new EditText(getActivity());
                paypalText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                String payEmail = me.getPayEmail();
                if (payEmail != null) paypalText.setText(payEmail);
                paypalText.setMaxLines(1);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.paypal_button))
                        .setMessage(getString(R.string.paypal_selling) + ":")
                        .setView(paypalText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Utils.hideKeyboard(getActivity());
                                String newEmail = paypalText.getText().toString();
                                if (me.getId() != null) {
                                    new Utils.SetPaypalTask(getActivity(), me, newEmail, null).execute();
                                }
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;

            case R.id.btn_sign_out:
                resetDefaultLogin();
                break;

            case R.id.txt_remove_account:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.remove_account))
                        .setMessage(getString(R.string.remove_account_warning))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new RemoveAccountTask(getActivity(), me.getId()).execute();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;
        }
    }

    private void resetDefaultLogin() {
        String loginMethod = me.getLoginMethod();
        String fb_token = sharedPref.getString("fb_token", "");
        String fb_id = sharedPref.getString("fb_id", "");

        editor.putString("fb_token", null);
        editor.putString("fb_id", null);
        editor.putBoolean("pass_confirmed", false);
        editor.commit();

        Globals.reset(getActivity(), true);

        new ChatUtils(getActivity()).resetConversations();

        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.logging_out) + "...");

        if (loginMethod != null && loginMethod.equals("google")) revokeGplusAccess();
        else if (loginMethod != null && loginMethod.equals("facebook"))
            new RevokeFacebookAccessTask(fb_id, fb_token).execute();
        else switchToReg();
    }

    private void switchToReg() {
        progressDialog.dismiss();
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        startActivity(intent);
        MainActivity act = Globals.mainActivity;
        if (act != null) act.finish();
        getActivity().finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (progressDialog != null) progressDialog.dismiss();
    }

    private void revokeGplusAccess() {
        GoogleApiClient mGoogleApiClient = Globals.mGoogleApiClient;
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(getClass().getSimpleName(), "User access revoked!");
                            switchToReg();
                        }

                    });
        } else {
            //connect before revoking access
            Globals.mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
            Globals.mGoogleApiClient.connect();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        if (responseCode == getActivity().RESULT_OK && requestCode == 10 && null != data) {
            smallImage = PhotoUtils.decodeUri(data.getData(), true, Globals.SMALL_IMG_SIZE, Globals.SMALL_IMG_SIZE, getActivity());
            putImage();
            C4Image newImg = new C4Image();
            newImg.setSmallBmp(smallImage);
            newImg.setUri(data.getData());
            new CompressAndSendTask(newImg).execute();
        }
    }

    private void putImage() {
        if (smallImage == null) {
            setAddImage();
            return;
        }
        RelativeLayout relView = (RelativeLayout)
                LayoutInflater.from(getActivity()).inflate(R.layout.image_layout, null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        relView.setLayoutParams(params);
        ImageView imageView = (ImageView) relView.findViewById(R.id.picture);
        ImageView crossView = (ImageView) relView.findViewById(R.id.cross);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(smallImage);
        photoContainer.removeAllViews();
        photoContainer.addView(relView);
        crossView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (me.getPhotoId() != null) {
                    setAddImage();
                    new DeleteImageTask(me.getPhotoId()).execute();
                }
            }

        });
    }

    @Override
    public void update(boolean redownload) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        revokeGplusAccess();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        switchToReg();
    }

    private class CompressAndSendTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = CompressAndSendTask.class.getName();

        private C4Image myImage;

        CompressAndSendTask(C4Image myImage) {
            this.myImage = myImage;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String respId = null;
            try {
                PhotoUtils.compressFromUri(myImage, getActivity());
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "userimage/user=" + me.getId();
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Image>(myImage, context.getDefaultHeaders()), String.class);
                respId = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return respId;
        }

        @Override
        protected void onPostExecute(String respId) {
            if (getActivity() == null) return;
            if (respId != null) {
                me.setPhotoId(Long.parseLong(respId));
                Toast.makeText(getActivity(), getString(R.string.image_uploaded_successfully), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_uploading_image), Toast.LENGTH_LONG).show();
                setAddImage();
            }
        }
    }

    private class DeleteImageTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = DeleteImageTask.class.getName();

        private Long id;

        DeleteImageTask(Long id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "deleting user image");
                String path = "image/id=" + id;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<C4Image>(context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                Toast.makeText(getActivity(), getString(R.string.image_deleted_successfully), Toast.LENGTH_LONG).show();
                me.setPhotoId(null);
                smallImage = null;
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_deleting_image), Toast.LENGTH_LONG).show();
                putImage();
            }
        }
    }

    private class GetImageTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = GetImageTask.class.getName();

        private Long id;

        public GetImageTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            C4Image image = null;
            byte[] imgArray = ImageCache.get(id);
            if (imgArray != null) {
                smallImage = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                return null;
            }
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d("test", "getting user image");
                String path = "image/" + "id=" + id + "&small=true";
                ResponseEntity<C4Image> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Image>(context.getDefaultHeaders()), C4Image.class);
                image = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting image", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }
            if (image == null) {
                return null;
            }
            imgArray = image.getSmallImage();
            ImageCache.put(id, imgArray);
            smallImage = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);

            return null;
        }

        @Override
        protected void onPostExecute(Void par) {
            if (!isAdded()) return;
            putImage();
        }
    }

    public class ChangeNameTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = ChangeNameTask.class.getName();
        private ProgressDialog progressDialog;
        private Context context;
        private Long id;
        private String name;

        public ChangeNameTask(Context context, Long id, String name) {
            super();
            this.context = context;
            this.id = id;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, getString(R.string.wait), getString(R.string.changing_username) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String newName = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "changename/id=" + id + "&name=" + name;
                ResponseEntity<String> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<String>(context.getDefaultHeaders()), String.class);
                newName = userEntity.getBody();

                Log.d(LOG_TAG, "Getting new name for id " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting new name", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return newName;
        }

        @Override
        protected void onPostExecute(String newName) {
            progressDialog.dismiss();
            if (newName != null) {
                me.setName(newName);
                txtName.setText(newName);
                editor.putString("username", newName);
                editor.commit();
                Toast.makeText(context, getString(R.string.username_changed_successfully), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, getString(R.string.problems_changing_username), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ChangeDescriptionTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = ChangeNameTask.class.getName();
        private ProgressDialog progressDialog;
        private Context context;
        private Long id;
        private String description;

        public ChangeDescriptionTask(Context context, Long id, String description) {
            super();
            this.context = context;
            this.id = id;
            this.description = description;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, getString(R.string.wait), getString(R.string.changing_description) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "userdescription/id=" + id;
                ResponseEntity<String> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<String>(description, context.getDefaultHeaders()), String.class);
                response = userEntity.getBody();
                Log.d(LOG_TAG, "Getting new name for id " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response != null && response.equals("OK")) {
                me.setDescription(description);
                Toast.makeText(context, getString(R.string.description_changed_successfully), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, getString(R.string.problems_changing_description), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class RemoveAccountTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = RemoveAccountTask.class.getName();
        private ProgressDialog progressDialog;
        private Context context;
        private Long id;

        public RemoveAccountTask(Context context, Long id) {
            super();
            this.context = context;
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, getString(R.string.wait), context.getString(R.string.removing_account) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "user/" + id;
                ResponseEntity<String> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<String>(context.getDefaultHeaders()), String.class);
                response = userEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response != null) {
                if (response.equals("OK")) {
                    Toast.makeText(context, context.getString(R.string.account_removed_successfully), Toast.LENGTH_LONG).show();
                    resetDefaultLogin();
                    return;
                } else if (response.equals("ERROR_PENDING_TRANSACTION")) {
                    Toast.makeText(context, context.getString(R.string.cannot_remove_account_transactions), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(context, context.getString(R.string.problems_removing_account), Toast.LENGTH_LONG).show();
        }
    }

    public class RevokeFacebookAccessTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = RevokeFacebookAccessTask.class.getName();
        private String id;
        private String token;

        public RevokeFacebookAccessTask(String id, String token) {
            super();
            this.id = id;
            this.token = token;
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = GRAPH_BASE + id + "/permissions?access_token=" + token;
            String resp = "";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("DELETE");
                //int responseCode = con.getResponseCode();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String resp) {
            switchToReg();
        }
    }
}
