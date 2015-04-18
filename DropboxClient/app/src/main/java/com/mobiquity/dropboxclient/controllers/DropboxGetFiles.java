package com.mobiquity.dropboxclient.controllers;

/**
 * Created by shanki on 4/17/15.
 * This class just gets the list of files in
 * the working directory from the dropbox and
 * loads the list adapter
 */

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.mobiquity.dropboxclient.model.DropBoxMetaData;
import com.mobiquity.dropboxclient.utils.Utils;
import com.mobiquity.dropboxclient.views.DropboxListItems;

public class DropboxGetFiles extends AsyncTask<Void, Void, ArrayList<DropBoxMetaData>> {

    DropboxListItems dropboxListItems;
    ProgressDialog progressDialog;
    ArrayList<DropBoxMetaData> itemList;
    private String currentPath;
    private DropboxAPI<?> androidAuthSessionDropboxAPI;
    private Context currentContext;

    public DropboxGetFiles(Context context, DropboxListItems listItems,
                           ProgressDialog progressDialog,
                           DropboxAPI<AndroidAuthSession> mdropboxApi, String path) {
        super();
        this.currentContext = context;
        this.androidAuthSessionDropboxAPI = mdropboxApi;
        this.currentPath = path;
        this.dropboxListItems = listItems;
        this.progressDialog = progressDialog;

    }

    @Override
    protected ArrayList<DropBoxMetaData> doInBackground(Void... params) {

        try {
            Entry dirent = androidAuthSessionDropboxAPI.metadata(currentPath, 1000, null, true, null);
            if (!dirent.isDir || dirent.contents == null) {
                Utils.showToast(currentContext, "Dropbox Empty");
                return null;
            }

            itemList = new ArrayList<>();
            for (Entry ent : dirent.contents) {
                    DropBoxMetaData dropBoxMetaData = new DropBoxMetaData();
                    dropBoxMetaData.setTitle(ent.fileName());
                    dropBoxMetaData.setCurrentPath(ent.path);
                    itemList.add(dropBoxMetaData);
            }

            if (itemList.size() == 0) {
                Utils.showToast(currentContext,"No items in Directory");
                return null;
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Loading Data...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(ArrayList<DropBoxMetaData> result) {
        super.onPostExecute(result);
        progressDialog.dismiss();
        dropboxListItems.setupData(result);
    }

}
