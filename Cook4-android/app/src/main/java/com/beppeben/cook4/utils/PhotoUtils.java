package com.beppeben.cook4.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.beppeben.cook4.R;
import com.beppeben.cook4.SliderActivity;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4Image;
import com.beppeben.cook4.domain.C4Item;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PhotoUtils {

    public static int ntasks = 0;

    public static Bitmap decodeArray(byte[] imgArray, Integer targetW, Integer targetH, boolean lowquality) {
        if (imgArray == null) return null;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        int scaleFactor = 1;
        if (targetW != null && targetH != null) {
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imgArray, 0,
                    imgArray.length, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            try {
                scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inInputShareable = true;
        bmOptions.inPurgeable = true;
        if (lowquality) {
            bmOptions.inDensity = 5;
            bmOptions.inTargetDensity = 2;
        }

        return BitmapFactory.decodeByteArray(imgArray, 0,
                imgArray.length, bmOptions);
    }

    public static Bitmap decodeUri(Uri uri, boolean resize, int WIDTH, int HEIGHT, Context ctx) {
        ParcelFileDescriptor parcelFD;
        FileDescriptor imageSource;
        try {
            parcelFD = ctx.getContentResolver().openFileDescriptor(uri, "r");
            imageSource = parcelFD.getFileDescriptor();
        } catch (FileNotFoundException e) {
            Log.d("decodeUri", "file not found");
            LogsToServer.send(e);
            return null;
        }

        BitmapFactory.Options o = new BitmapFactory.Options();
        if (!resize) return BitmapFactory.decodeFileDescriptor(imageSource, null, o);
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(imageSource, null, o);

        final int REQUIRED_WIDTH = WIDTH;
        final int REQUIRED_HIGHT = HEIGHT;
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
            scale *= 2;

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inInputShareable = true;
        o2.inPurgeable = true;

        Bitmap bmp = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

        if (parcelFD != null)
            try {
                parcelFD.close();
            } catch (IOException e) {
            }

        try {
            String path = getRealPathFromURI(uri, ctx);
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            LogsToServer.send(e);
        }
        return bmp;
    }

    public static String getRealPathFromURI(Uri contentURI, Context context) {
        String path = contentURI.getPath();
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        } catch (Exception e) {
            return path;
        }
        return path;
    }

    public static void compressFromUri(C4Image myImage, Context ctx) {
        Bitmap bigBmp = myImage.getBigBmp();
        if (bigBmp == null) {
            bigBmp = PhotoUtils.decodeUri(myImage.getUri(), true, Globals.BIG_IMG_SIZE, Globals.BIG_IMG_SIZE, ctx);
        }
        Bitmap smallBmp = myImage.getSmallBmp();
        if (smallBmp == null) {
            smallBmp = PhotoUtils.decodeUri(myImage.getUri(), true, Globals.SMALL_IMG_SIZE, Globals.SMALL_IMG_SIZE, ctx);
        }

        ByteArrayOutputStream bigOs = new ByteArrayOutputStream();
        bigBmp.compress(Bitmap.CompressFormat.JPEG, Globals.HIGH_QUALITY, bigOs);
        ByteArrayOutputStream smallOs = new ByteArrayOutputStream();
        smallBmp.compress(Bitmap.CompressFormat.JPEG, Globals.HIGH_QUALITY, smallOs);

        myImage.setBigImage(bigOs.toByteArray());
        myImage.setSmallImage(smallOs.toByteArray());
    }

    public static void setPhoto(Context ctx, ImageView photoView, C4Dish dish, C4Item item) {
        if (item != null) photoView.setTag(item.getId());
        else photoView.setTag(dish.getId());

        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setImageBitmap(null);

        Bitmap imgBmp = dish.getImgBmp();

        if (imgBmp != null) {
            photoView.setImageBitmap(imgBmp);
        } else if (dish.getCoverId() != null) {
            byte[] imgArray = ImageCache.get(dish.getCoverId());
            if (imgArray != null) {
                if (item != null) {
                    imgBmp = decodeArray(imgArray, null, null, false);
                } else {
                    imgBmp = decodeArray(imgArray, 100, 100, false);
                }
                photoView.setImageBitmap(imgBmp);
                dish.setImgBmp(imgBmp);
            } else {
                if (item != null)
                    new GetImageTask(photoView, item).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                else new GetImageTask(photoView, dish).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }

        List<Long> picIds = dish.getPicIds();
        if (picIds == null) {
            photoView.setClickable(false);
            return;
        }
        final long[] picIdsArray = new long[picIds.size()];
        for (int i = 0; i < picIds.size(); i++) picIdsArray[i] = picIds.get(i);
        final Context finctx = ctx;
        if (picIds != null) {
            photoView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(finctx, SliderActivity.class);
                    i.putExtra("ids", picIdsArray);
                    finctx.startActivity(i);

                }

            });
        }
    }

    public static void setPhoto(Context ctx, ImageView photoView, C4User user) {
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setImageBitmap(null);
        photoView.setImageDrawable(null);
        Long id = user.getPhotoId();

        Bitmap imgBmp = user.getImgBmp();
        if (imgBmp != null) {
            photoView.setImageBitmap(imgBmp);
        } else if (id != null) {
            byte[] imgArray = ImageCache.get(id);
            if (imgArray != null) {
                imgBmp = decodeArray(imgArray, 100, 100, false);
                photoView.setImageBitmap(imgBmp);
                user.setImgBmp(imgBmp);
            } else new GetImageTask(photoView, user).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            photoView.setImageDrawable(ctx.getResources().getDrawable(R.drawable.blankphoto));
            return;
        }

        List<Long> picIds = new ArrayList<Long>();
        picIds.add(id);
        final long[] picIdsArray = new long[picIds.size()];
        for (int i = 0; i < picIds.size(); i++) picIdsArray[i] = picIds.get(i);
        final Context finctx = ctx;

        photoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(finctx, SliderActivity.class);
                i.putExtra("ids", picIdsArray);
                finctx.startActivity(i);
            }
        });
    }

    public static class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

        private final String LOG_TAG = GetImageTask.class.getName();
        private WeakReference<ImageView> viewRef;
        private C4Item item;
        private C4Dish dish;
        private C4User user;
        private Long id;
        boolean thumb;

        public GetImageTask(ImageView view, C4Item item) {
            super();
            this.viewRef = new WeakReference<ImageView>(view);
            this.item = item;
            this.dish = item.getDish();
            this.id = dish.getCoverId();
            this.thumb = false;
        }

        public GetImageTask(ImageView view, C4Dish dish) {
            super();
            this.viewRef = new WeakReference<ImageView>(view);
            this.dish = dish;
            this.id = dish.getCoverId();
            this.thumb = true;
        }

        public GetImageTask(ImageView view, C4User user) {
            super();
            this.viewRef = new WeakReference<ImageView>(view);
            this.id = user.getPhotoId();
            this.user = user;
            this.thumb = true;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            ntasks++;
            HttpContext context = HttpContext.getInstance();
            C4Image image = null;
            byte[] imgArray = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String path = "image/" + "id=" + id + "&small=true";
                ResponseEntity<C4Image> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Image>(context.getDefaultHeaders()), C4Image.class);
                image = responseEntity.getBody();
                if (image != null) imgArray = image.getSmallImage();
                else return null;
                ImageCache.put(id, imgArray);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                Log.e(LOG_TAG, "failed to get image id " + id, e);
                return null;
            }
            if (thumb) {
                return decodeArray(imgArray, 100, 100, false);
            } else {
                return decodeArray(imgArray, null, null, false);
            }
        }

        @Override
        protected void onPostExecute(Bitmap imgBmp) {
            ntasks--;
            if (imgBmp == null) return;
            ImageView view;
            if (viewRef == null || (view = viewRef.get()) == null) return;
            Long origId = (Long) view.getTag();
            if (item != null && !item.getId().equals(origId)) return;
            if (item == null && dish != null && !dish.getId().equals(origId)) return;
            view.setImageBitmap(imgBmp);
            if (dish != null) dish.setImgBmp(imgBmp);
            if (user != null) user.setImgBmp(imgBmp);
        }
    }
}
