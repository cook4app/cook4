package com.beppeben.cook4;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.beppeben.cook4.ui.RegisterFragment;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;
import com.beppeben.cook4.utils.net.SslUtils;

import java.util.List;

public class RegistrationActivity extends ActionBarActivity {

    public RegisterFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Utils.setExceptionHandler();
        setHttpContext();
        frag = RegisterFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, frag).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (frag == null) return;
        frag.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = frag.getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void setHttpContext() {
        try {
            SslUtils.setSelfSignedCertSSLContext(getAssets());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), " Error creating SSLContext: ", e);
        }
        HttpContext.removeInstance();
        HttpContext context = HttpContext.getInstance();
        context.setBaseUrl(getString(R.string.cook4_url));
        Log.d(getClass().getSimpleName(), "Http context set");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Globals.saveMe(this);
        super.onPause();
    }

}
