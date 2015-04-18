package com.mobiquity.dropboxclient.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shanki on 4/18/15.
 * Provides utility functions for the application
 */
public class Utils {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
    }

    public static Bitmap decodeImage(Uri uri, Context context, int reqWidth, int reqHeight) {

        Bitmap bm = null;

        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri), null, options);
            options.inSampleSize = calcImageExcess(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bm;
    }

    public static int calcImageExcess(BitmapFactory.Options options, int reqWidth,
                               int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

}
