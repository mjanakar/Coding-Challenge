package com.mobiquity.dropboxclient.views;

/**
 * Created by shanki on 4/17/15.
 * This class creates a list view
 * of all the items available in the dropbox.
 */
import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.mobiquity.dropboxclient.controllers.DropboxGetFiles;
import com.mobiquity.dropboxclient.MainActivity;
import com.mobiquity.dropboxclient.adapters.MyAdapter;
import com.mobiquity.dropboxclient.R;
import com.mobiquity.dropboxclient.model.DropBoxMetaData;

public class DropboxListItems extends Activity {

    public static final String SECRET = "ACCESS_SECRET";
    public DropboxAPI<AndroidAuthSession> androidAuthSessionDropboxAPI;
    ListView listView;
    ProgressDialog mProgressDialog;
    MyAdapter adapter;
    ArrayList<DropBoxMetaData> dropBoxMetaDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        listView = (ListView) findViewById(R.id.listView1);
        String path = getIntent().getStringExtra("Path");

        AppKeyPair appKeyPair = new AppKeyPair(getResources().getString(R.string.KEY), getResources().getString(R.string.SECRET));
        AndroidAuthSession session;
        session = new AndroidAuthSession(appKeyPair);
        SharedPreferences prefs = getSharedPreferences(
                MainActivity.NAME, 0);
        String key = prefs.getString(MainActivity.KEY, null);
        String secret = prefs.getString(MainActivity.SECRET, null);
        if (key == null || secret == null || key.length() == 0
                || secret.length() == 0) {
            return;
        } else {
            session.setOAuth2AccessToken(secret);
        }

        androidAuthSessionDropboxAPI = new DropboxAPI<>(session);
        new DropboxGetFiles(DropboxListItems.this, this, mProgressDialog, androidAuthSessionDropboxAPI, path).execute();
    }

    public void setupData(ArrayList<DropBoxMetaData> files) {
        final ArrayList<DropBoxMetaData> titles = new ArrayList<DropBoxMetaData>();
        for (DropBoxMetaData str : files) {
            titles.add(str);
        }

        adapter = new MyAdapter(DropboxListItems.this, R.layout.listrow,
                files, androidAuthSessionDropboxAPI);
        listView.setAdapter(adapter);

        dropBoxMetaDatas = new ArrayList<>();
        dropBoxMetaDatas = files;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String path = dropBoxMetaDatas.get(position).getCurrentPath();
                Intent i = new Intent(getApplicationContext(), DropboxViewer.class);
                i.putExtra("oauth_key", SECRET);
                i.putExtra("ItemPath", path);
                startActivity(i);
            }
        });

    }
}