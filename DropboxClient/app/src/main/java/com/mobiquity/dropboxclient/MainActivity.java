package com.mobiquity.dropboxclient;
/**
 * Created by shanki on 4/16/15.
 * This class is the main UI of the
 * application doing several functions.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.mobiquity.dropboxclient.controllers.AudioRecorder;
import com.mobiquity.dropboxclient.controllers.UploadDropbox;
import com.mobiquity.dropboxclient.utils.Utils;
import com.mobiquity.dropboxclient.views.DropboxListItems;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    final static public String NAME = "Client Test";
    public static final String KEY = "ACCESS_KEY";
    public static final String SECRET = "ACCESS_SECRET";
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;
    private static String path;
    public final String PHOTO_DIR = "";
    public final String PATH_KEY = "Path";
    public DropboxAPI<AndroidAuthSession> authSessionDropboxAPI;
    Button loginButton, cameraButton, noteButton, listButton, audioButton, videoButton;
    ImageView imageView;
    private LocationManager locationManager;
    private Uri fileUri;
    private boolean isUserLoggedIn;
    private String currentCity = "unknown";
    private NetworkInfo networkInfo;
    private double latitude = 0.0f, longitude = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.login_button);
        cameraButton = (Button) findViewById(R.id.capture_button);
        noteButton = (Button) findViewById(R.id.note_button);
        audioButton = (Button) findViewById(R.id.audio_button);
        listButton = (Button) findViewById(R.id.view_button);
        videoButton = (Button) findViewById(R.id.video_button);
        imageView = (ImageView) findViewById(R.id.imageView2);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        loggedIn(false);
        AppKeyPair appKeyPair = new AppKeyPair(getResources().getString(R.string.KEY), getResources().getString(R.string.SECRET));

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuthenticationKeys(session);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Warning")
                .setMessage("Enable Location on your Device")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        authSessionDropboxAPI = new DropboxAPI<>(session);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isUserLoggedIn) {
                    logOutUser();
                } else {
                    authSessionDropboxAPI.getSession().startOAuth2Authentication(MainActivity.this);
                }
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(isUserLoggedIn))
                    Utils.showToast(MainActivity.this, "Please Login before capturing");
                else {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        new AsyncTaskGps().execute(locationManager);
                    else
                        alert.show();
                }
            }
        });

        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(isUserLoggedIn))
                    Utils.showToast(MainActivity.this, "Please Login before taking note");
                else
                    takeNote();
            }
        });

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(isUserLoggedIn))
                    Utils.showToast(MainActivity.this, "Please LogIn before recording");
                else {
                    Intent intent = new Intent(MainActivity.this, AudioRecorder.class);
                    intent.putExtra(PATH_KEY, PHOTO_DIR);
                    intent.putExtra("oauth_key", SECRET);
                    startActivity(intent);
                }
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(isUserLoggedIn))
                    Utils.showToast(MainActivity.this, "Please LogIn before recording");
                else
                    captureVideo();
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(isUserLoggedIn))
                    Utils.showToast(MainActivity.this, "Please LogIn before viewing");
                else {
                    Intent intent = new Intent(MainActivity.this, DropboxListItems.class);
                    intent.putExtra(PATH_KEY, PHOTO_DIR);
                    intent.putExtra("oauth_key", SECRET);
                    startActivity(intent);
                }
            }
        });
    }

    private void takeNote() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(MainActivity.this);
        alert.setMessage("Note");
        alert.setTitle("Enter Your Text File Contents");
        alert.setView(edittext);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String myText = edittext.getText().toString();
                if (myText.length() > 0) {
                    try {
                        File noteFile = new File(Environment.getExternalStorageDirectory() + "/Client Test/" + DateFormat.getDateTimeInstance().format(new Date()) + ".txt");
                        FileWriter writer = new FileWriter(noteFile);
                        writer.append(myText);
                        writer.flush();
                        writer.close();
                        UploadDropbox uploadDropbox = new UploadDropbox(MainActivity.this, authSessionDropboxAPI, noteFile);
                        uploadDropbox.execute();
                    } catch (IOException e) {
                        Utils.showToast(MainActivity.this, "Failed creating file");
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, 1);
    }

    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, 2);

    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Client Test");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + currentCity + "_" + Utils.getTimeStamp() + ".jpg");
            path = mediaStorageDir.getPath() + File.separator + "IMG_"
                    + currentCity + "_" + Utils.getTimeStamp() + ".jpg";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID" + "_" + Utils.getTimeStamp() + ".mp4");
            path = mediaStorageDir.getPath() + File.separator + "VID"
                    + "_" + Utils.getTimeStamp() + ".mp4";
        } else
            return null;
        return mediaFile;
    }

    private void loggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
        loginButton.setText(userLoggedIn ? "Logout" : "Log in");

    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = authSessionDropboxAPI.getSession();

        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                storeAuthenticationKeys(session);
                loggedIn(true);
            } catch (IllegalStateException e) {
            }
        }
    }

    private void loadAuthenticationKeys(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(NAME, 0);
        String key = prefs.getString(KEY, null);
        String secret = prefs.getString(SECRET, null);
        if (key == null || secret == null || key.length() == 0
                || secret.length() == 0) {
            return;
        } else {
            session.setOAuth2AccessToken(secret);
        }
    }

    private void storeAuthenticationKeys(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(KEY, "oauth2:");
            edit.putString(SECRET, oauth2AccessToken);
            edit.commit();
            return;
        }
    }

    private void logOutUser() {
        authSessionDropboxAPI.getSession().unlink();
        SharedPreferences prefs = getSharedPreferences(NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
        loggedIn(false);
    }

    /* Called when the image is successfully loaded */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_TYPE_IMAGE && resultCode == RESULT_OK) {
            MainActivity.this.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                    + path)));
            if (networkInfo.isConnected()) {
                File fileToUpload = new File(path);
                geoTagging(path);
                UploadDropbox uploadDropbox = new UploadDropbox(MainActivity.this, authSessionDropboxAPI, fileToUpload);
                uploadDropbox.execute();
            } else
                Utils.showToast(MainActivity.this, "No Network!");
        }
        if (requestCode == MEDIA_TYPE_VIDEO && resultCode == RESULT_OK) {
            MainActivity.this.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                    + path)));
            if (networkInfo.isConnected()) {
                File fileToUpload = new File(path);
                UploadDropbox uploadDropbox = new UploadDropbox(MainActivity.this, authSessionDropboxAPI, fileToUpload);
                uploadDropbox.execute();
            } else
                Utils.showToast(MainActivity.this, "NoNetwork!");
        } else
            return;
    }

    private void getCity() {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale
                .getDefault());
        List<Address> addresses = Collections.emptyList();
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.isEmpty()) {
        } else {
            if (addresses.size() > 0) {
                currentCity = addresses.get(0).getLocality();
            }
        }
    }

    public void geoTagging(String filename) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(filename);
            int num1Lat = (int) Math.floor(latitude);
            int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
            double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

            int num1Lon = (int) Math.floor(longitude);
            int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
            double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000");


            if (latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class AsyncTaskGps extends AsyncTask implements LocationListener {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setIcon(android.R.drawable.ic_dialog_alert);
            this.dialog.setTitle("Please Wait");
            this.dialog.setMessage("If GPS enabled may take longer to load\nAcquiring Location...");
            this.dialog.show();
        }

        @Override
        protected Object doInBackground(Object... arg0) {
            LocationManager lm = (LocationManager) arg0[0];
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.getBestProvider(criteria, true);
            Looper.prepare();
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            else
                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            Looper.loop();
            dialog.dismiss();
            captureImage();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

        }

        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            getCity();
            Looper.myLooper().quit();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }
}
