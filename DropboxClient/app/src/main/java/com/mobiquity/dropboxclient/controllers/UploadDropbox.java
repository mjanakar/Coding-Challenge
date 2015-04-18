package com.mobiquity.dropboxclient.controllers;

/**
 * Created by shanki on 4/17/15.
 * This method uploads the given file to dropbox.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.mobiquity.dropboxclient.utils.Utils;

public class UploadDropbox extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> dropboxAPI;
    private Context currentContext;
    private File file;
    private long mFileLen;
    private ProgressDialog progressDialog;
    private DropboxAPI.UploadRequest uploadRequest;

    public UploadDropbox(Context context, DropboxAPI<?> mDBApi, File file) {
        this.currentContext = context.getApplicationContext();
        this.dropboxAPI = mDBApi;
        this.file = file;
        mFileLen = file.length();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage("Uploading " + file.getName());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            uploadRequest = dropboxAPI.putFileOverwriteRequest(file.getName(), fileInputStream, file.length(),
                    new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });
            if (uploadRequest != null) {
                uploadRequest.upload();
                return true;
            }
            return true;
        } catch (Exception e) {
            Utils.showToast(currentContext, "" + e);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result) {
            Utils.showToast(currentContext, "Uploaded Successfully!");
        } else {
            Utils.showToast(currentContext, "Failed to upload");
        }
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / mFileLen + 0.5);
        progressDialog.setProgress(percent);
    }
}