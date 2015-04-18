package com.mobiquity.dropboxclient.controllers;

/**
 * Created by shanki on 4/17/15.
 * This class downloads the selected item from the list view and
 * displays it in an appropriate manner.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.mobiquity.dropboxclient.utils.Utils;


public class DownloadDropbox extends AsyncTask<Void, Long, Boolean> {

    private final static String IMAGE_FILE_NAME = "Cached_Image.png";
    private final static String VIDEO_FILE_NAME = "Cached_Video.mp4";
    private final static String TEXT_FILE_NAME = "Cached_Text.txt";

    private final static int IMAGE_MODE = 0;
    private final static int VIDEO_MODE = 1;
    private final static int TEXT_MODE = 2;

    private final ProgressDialog mDialog;
    private Context currentContext;
    private DropboxAPI<?> androidAuthSessionDropboxAPI;
    private String currentPath, currentFileName;
    private View currentView;
    private Drawable drawable;
    private Bitmap bitmap;
    private FileOutputStream fileOutputStream;
    private boolean isCanceled;
    private Long fileLength;
    private String errorMessage;
    private int currentFlag;
    private String tempPath;
    private float latitude, longitude;

    public DownloadDropbox(Context context, DropboxAPI<?> api,
                           String filename, View view, String dropboxPath, int flag) {
        currentContext = context.getApplicationContext();
        androidAuthSessionDropboxAPI = api;
        currentPath = dropboxPath;
        currentView = view;
        currentFileName = filename;
        currentFlag = flag;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading...");
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel",
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isCanceled = true;
                        errorMessage = "Canceled";
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                });

        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (isCanceled) {
                return false;
            }

            Entry dirent = androidAuthSessionDropboxAPI.metadata(currentPath, 1000, null, true, null);

            if (!dirent.isDir || dirent.contents == null) {
                errorMessage = "Dropbox Empty";
                return false;
            }

            ArrayList<Entry> thumbnails = new ArrayList<Entry>();
            for (Entry ent : dirent.contents) {
                if (ent.thumbExists) {
                    thumbnails.add(ent);
                }
            }

            if (isCanceled) {
                return false;
            }

            if (thumbnails.size() == 0) {
                errorMessage = "No pictures in that directory";
                return false;
            }
            int index = (int) (Math.random() * thumbnails.size());
            Entry entry = thumbnails.get(index);
            String path = currentFileName;
            fileLength = entry.bytes;
            String cachePath = "";
            if ((currentFlag == IMAGE_MODE))
                cachePath = currentContext.getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
            else if (currentFlag == VIDEO_MODE)
                cachePath = currentContext.getCacheDir().getAbsolutePath() + "/" + VIDEO_FILE_NAME;
            else
                cachePath = currentContext.getCacheDir().getAbsolutePath() + "/" + TEXT_FILE_NAME;
            try {
                fileOutputStream = new FileOutputStream(cachePath);
            } catch (FileNotFoundException e) {
                errorMessage = "Cannot store file";
                return false;
            }
            androidAuthSessionDropboxAPI.getFile(path, null, fileOutputStream, null);
            if (isCanceled) {
                return false;
            }
            if (currentFlag == IMAGE_MODE) {
                Uri uri = Uri.fromFile(new File(cachePath));
                bitmap = Utils.decodeImage(uri, currentContext, currentView.getWidth(), currentView.getHeight());
                //drawable = Drawable.createFromPath(cachePath);
            }
            else
                tempPath = cachePath;
            if (currentFlag == IMAGE_MODE)
                parseExif(cachePath);
            return true;

        } catch (Exception e) {
        }
        return false;
    }

    private void parseExif(String cachePath) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(cachePath);
            Location location = new Location("");
            float[] latLong = new float[2];

            if (exif.getLatLong(latLong)) {
                location.setLatitude(latLong[0]);
                location.setLongitude(latLong[1]);
                latitude = latLong[0];
                longitude = latLong[1];
            }
        } catch (IOException e) {
            latitude = 0.0f;
            longitude = 0.0f;
            errorMessage = "Cannot read EXIF data";
        }

    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / fileLength + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result && currentFlag == IMAGE_MODE) {
            if(bitmap != null)
                ((ImageView) currentView).setImageBitmap(bitmap);
            Utils.showToast(currentContext, "Latitude: " + latitude + "\nLongitude: " + longitude);
        } else if (result && currentFlag == VIDEO_MODE) {
            ((VideoView) currentView).setVideoPath(tempPath);
            currentView.requestFocus();
            ((VideoView) currentView).start();
        } else if (result && currentFlag == TEXT_MODE) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader in;

            try {
                in = new BufferedReader(new FileReader(new File(tempPath)));
                while ((line = in.readLine()) != null) stringBuilder.append(line);
                ((TextView) currentView).setText(stringBuilder.toString());
            } catch (Exception e) {
                Utils.showToast(currentContext, "Cannot read from file");
            }

        } else {
            Utils.showToast(currentContext, errorMessage);
        }
    }


}