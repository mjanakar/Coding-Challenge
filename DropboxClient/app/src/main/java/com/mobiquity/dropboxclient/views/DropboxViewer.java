package com.mobiquity.dropboxclient.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.mobiquity.dropboxclient.controllers.DownloadDropbox;
import com.mobiquity.dropboxclient.MainActivity;
import com.mobiquity.dropboxclient.R;

/**
 * Created by shanki on 4/18/15.
 * This activity provides the appropriate views to the DownloadDropbox
 * class to update the View with relevant contents.
 */
public class DropboxViewer extends Activity {

    private final static int IMAGE_MODE = 0;
    private final static int VIDEO_MODE = 1;
    private final static int TEXT_MODE = 2;
    public DropboxAPI<AndroidAuthSession> authSessionDropboxAPI;
    VideoView videoView;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_viewer);
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
        String path = getIntent().getStringExtra("ItemPath");
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

        authSessionDropboxAPI = new DropboxAPI<>(session);
        if (path.endsWith("jpg")) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams vp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            600);
            vp.gravity = Gravity.CENTER;
            imageView.setLayoutParams(vp);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            layout.addView(imageView);
            DownloadDropbox downloadDropbox = new DownloadDropbox(DropboxViewer.this, authSessionDropboxAPI, path, imageView, "/", IMAGE_MODE);
            downloadDropbox.execute();
        }
        if (path.endsWith("mp4") || path.endsWith("3gp")) {
            if (mediaController == null) {
                mediaController = new MediaController(DropboxViewer.this);
            }
            videoView = new VideoView(this);
            LinearLayout.LayoutParams vp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            videoView.setLayoutParams(vp);
            layout.addView(videoView);
            videoView.setMediaController(mediaController);
            DownloadDropbox downloadDropbox = new DownloadDropbox(DropboxViewer.this, authSessionDropboxAPI, path, videoView, "/", VIDEO_MODE);
            downloadDropbox.execute();
        }
        if (path.endsWith("txt")) {
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams vp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            vp.gravity = Gravity.CENTER;
            textView.setLayoutParams(vp);
            textView.setTextColor(Color.BLACK);
            layout.addView(textView);
            DownloadDropbox downloadDropbox = new DownloadDropbox(DropboxViewer.this, authSessionDropboxAPI, path, textView, "/", TEXT_MODE);
            downloadDropbox.execute();
        }

    }
}
