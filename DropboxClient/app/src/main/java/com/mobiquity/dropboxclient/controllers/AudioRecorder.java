package com.mobiquity.dropboxclient.controllers;

/**
 * Created by shanki on 4/17/15.
 * This class provides the interface to record an audio clip
 * and to start uploading it to the dropbox
 * It also supports audio playback
 */

import java.io.File;

import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.mobiquity.dropboxclient.MainActivity;
import com.mobiquity.dropboxclient.R;
import com.mobiquity.dropboxclient.utils.Utils;

public class AudioRecorder extends Activity {

    public DropboxAPI<AndroidAuthSession> androidAuthSessionDropboxAPI;
    ImageView imageView;
    String outputFile = null;
    private MediaRecorder myAudioRecorder;
    private Button startButton, stopButton, playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_activity);
        startButton = (Button) findViewById(R.id.button1);
        stopButton = (Button) findViewById(R.id.button2);
        playButton = (Button) findViewById(R.id.button3);
        imageView = (ImageView) findViewById(R.id.mic_image);
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

        stopButton.setEnabled(false);
        playButton.setEnabled(false);
        imageView.setVisibility(View.INVISIBLE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Client Test/"
                + timestamp + ".3gp";

        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);


    }

    public void start(View view) {
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (Exception e) {
            Utils.showToast(AudioRecorder.this, "" + e);
        }
        startButton.setEnabled(false);
        imageView.setVisibility(View.VISIBLE);
        stopButton.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

    }

    public void stop(View view) {
        try {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
            stopButton.setEnabled(false);
            playButton.setEnabled(true);
            File toUpload = new File(outputFile);
            UploadDropbox uploadDropbox = new UploadDropbox(AudioRecorder.this, androidAuthSessionDropboxAPI, toUpload);
            uploadDropbox.execute();
            Utils.showToast(AudioRecorder.this, "Success!! Uploading audio.");
        } catch (Exception e) {
            Utils.showToast(AudioRecorder.this, "" + e);
        }
        imageView.setVisibility(View.INVISIBLE);

    }

    public void play(View view) {

        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(outputFile);
            m.prepare();
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();

    }

}