package com.beppeben.cook4.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Image;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.ImageCache;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class GridViewImageAdapter extends BaseAdapter {

    final static int MAX_IMAGES = 6;
    private Activity activity;
    private int imageWidth;
    private C4Image cover;
    private Long coverId;
    private GridViewImageAdapter adapter;
    public static File photoFile;
    private List<Long> picIds;
    private List<Long> idsToRemove = new ArrayList<Long>();
    private List<C4Image> images = new ArrayList<C4Image>();

    public GridViewImageAdapter(Activity activity, int imageWidth, List<Long> picIds, Long coverId) {
        this.coverId = coverId;
        this.activity = activity;
        this.imageWidth = imageWidth;
        this.picIds = picIds;
    }

    public void setAdapter(GridViewImageAdapter adapter, boolean downloadimages) {
        this.adapter = adapter;
        if (!downloadimages) return;
        if (picIds != null) {
            HashSet<Long> picIdsNoDuplicates = new HashSet<Long>(picIds);
            picIds = new ArrayList<Long>(picIdsNoDuplicates);
            for (Long id : picIds) {
                byte[] imgArray = ImageCache.get(id);
                if (imgArray != null) addImage(imgArray, id);
                else new GetImageTask(id).execute();
            }
        }
    }

    public void addImage(Uri uri) {
        Bitmap smallImage = PhotoUtils.decodeUri(uri, true, Globals.SMALL_IMG_SIZE, Globals.SMALL_IMG_SIZE, activity);
        C4Image myImage = new C4Image();
        myImage.setSmallBmp(smallImage);
        myImage.setUri(uri);
        images.add(myImage);
        adapter.notifyDataSetChanged();
        new CompressTask(myImage).execute();
    }

    @Override
    public int getCount() {
        return Math.min(MAX_IMAGES, images.size() + 1);
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout relView;
        if (position == images.size()) {
            relView = (RelativeLayout)
                    LayoutInflater.from(activity).inflate(R.layout.add_image_layout, null);
            relView.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
            relView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final MainActivity act = (MainActivity) activity;
                    PopupMenu popup = new PopupMenu(act, arg0);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem arg0) {
                            switch (arg0.getItemId()) {
                                case R.id.action_gallery:
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    act.startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                                    break;
                                case R.id.action_camera:
                                    dispatchTakePictureIntent();
                                    break;
                            }
                            return false;
                        }
                    });
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.photomenu, popup.getMenu());
                    popup.show();
                }
            });

        } else {
            relView = (RelativeLayout)
                    LayoutInflater.from(activity).inflate(R.layout.image_layout, null);
            ImageView imageView = (ImageView) relView.findViewById(R.id.picture);
            ImageView crossView = (ImageView) relView.findViewById(R.id.cross);

            C4Image myImage = images.get(position);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            relView.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
            imageView.setImageBitmap(myImage.getSmallBmp());

            if (myImage == cover) {
                relView.setBackgroundColor(Color.YELLOW);
            }
            if (cover == null) cover = myImage;

            imageView.setOnClickListener(new OnImageClickListener(myImage));
            crossView.setOnClickListener(new OnImageClickListener(myImage));
        }
        return relView;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Log.e("GridViewAdapter", ex.getLocalizedMessage(), ex);
                LogsToServer.send(ex);
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                activity.startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File file = new File(storageDir, imageFileName + ".jpg");
        MediaScannerConnection.scanFile(activity,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        return file;
    }

    class OnImageClickListener implements OnClickListener {

        C4Image myImage;

        public OnImageClickListener(C4Image myImage) {
            this.myImage = myImage;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.picture:
                    cover = myImage;
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.cross:
                    images.remove(myImage);
                    if (myImage.getId() != null) idsToRemove.add(myImage.getId());
                    if (cover == myImage) cover = null;
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    public List<C4Image> getImagesToUpload() {
        List<C4Image> result = new ArrayList<C4Image>();
        C4Image coverImg = null;
        for (int i = 0; i < images.size(); i++) {
            C4Image myImage = images.get(i);
            if (myImage.getId() == null) {
                result.add(myImage);
                if (myImage == cover) coverImg = myImage;
            }
        }
        //Put cover in the first place
        if (coverImg != null) {
            result.set(result.indexOf(coverImg), result.get(0));
            result.set(0, coverImg);
        }
        return result;
    }

    public List<Long> getIdsToRemove() {
        return idsToRemove;
    }

    public Long getCoverId() {
        if (cover != null && cover.getId() != null) return cover.getId();
        else return -1L;
    }

    private class GetImageTask extends AsyncTask<Void, Void, C4Image> {

        private final String LOG_TAG = GetImageTask.class.getName();

        private Long id;

        public GetImageTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected C4Image doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            C4Image image = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String path = "image/" + "id=" + id + "&small=true";
                ResponseEntity<C4Image> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Image>(context.getDefaultHeaders()), C4Image.class);
                image = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting image", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(C4Image image) {
            if (image != null) {
                byte[] imgArray = image.getSmallImage();
                ImageCache.put(id, imgArray);
                addImage(imgArray, id);
            }
        }
    }

    private void addImage(byte[] imgArray, Long id) {
        Bitmap imgBmp = PhotoUtils.decodeArray(imgArray, null, null, false);
        C4Image myImage = new C4Image();
        myImage.setSmallBmp(imgBmp);
        myImage.setSmallImage(imgArray);
        myImage.setId(id);
        images.add(myImage);
        if (coverId == id) cover = myImage;
        adapter.notifyDataSetChanged();
    }

    private class CompressTask extends AsyncTask<Void, Void, String> {

        private C4Image myImage;

        CompressTask(C4Image myImage) {
            this.myImage = myImage;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                PhotoUtils.compressFromUri(myImage, activity);
            } catch (Exception e) {
                LogsToServer.send(e);
                return null;
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String resp) {
            if (activity == null) return;
            if (resp == null) {
                Toast.makeText(activity, activity.getString(R.string.problems_uploading_image), Toast.LENGTH_SHORT).show();
                images.remove(myImage);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }
}