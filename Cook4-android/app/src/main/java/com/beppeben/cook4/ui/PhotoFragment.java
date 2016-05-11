package com.beppeben.cook4.ui;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Image;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PhotoFragment extends Fragment {

    private Long imgId;
    private ImageView imgDisplay;
    private byte[] img;

    public PhotoFragment() {
    }

    public static PhotoFragment newInstance(Long id) {
        PhotoFragment frag = new PhotoFragment();
        frag.setImgId(id);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            imgId = savedInstanceState.getLong("imgId");
            img = savedInstanceState.getByteArray("img");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout viewLayout = (RelativeLayout) inflater.inflate(R.layout.fullscreen_image_layout, null);
        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);

        Bitmap image = PhotoUtils.decodeArray(img, Utils.getScreenDim(getActivity(), true),
                Utils.getScreenDim(getActivity(), false), true);
        if (image != null) {
            imgDisplay.setImageBitmap(image);
            return viewLayout;
        }

        if (imgId != null) {
            new GetImageTask(imgId).execute();
        }

        return viewLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("imgId", imgId);
        savedInstanceState.putByteArray("img", img);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setImgId(Long imgId) {
        this.imgId = imgId;
    }

    private class GetImageTask extends AsyncTask<Void, Void, byte[]> {

        private final String LOG_TAG = GetImageTask.class.getName();

        private Long id;

        public GetImageTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            C4Image image = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "image/" + "id=" + id + "&small=false";
                ResponseEntity<C4Image> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Image>(context.getDefaultHeaders()), C4Image.class);
                image = responseEntity.getBody();

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting image", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                if (getActivity() != null)
                    LogsToServer.send(e);
            }

            byte[] imgArray = image.getBigImage();
            return imgArray;
        }

        @Override
        protected void onPostExecute(byte[] imgArray) {
            if (imgArray != null) {
                Log.d(LOG_TAG, "Received image id " + imgId);
                img = imgArray;
                Bitmap image = PhotoUtils.decodeArray(img, Utils.getScreenDim(getActivity(), true),
                        Utils.getScreenDim(getActivity(), false), true);
                imgDisplay.setImageBitmap(image);
            }
        }
    }

}

