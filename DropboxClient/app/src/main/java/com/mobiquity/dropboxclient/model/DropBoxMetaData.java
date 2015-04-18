package com.mobiquity.dropboxclient.model;

/**
 * Created by shanki on 4/17/15.
 * Model for the DropBox data
 */

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class DropBoxMetaData implements Parcelable {

    String currentPath;
    String currentTitle;


    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public String getTitle() {
        return currentTitle;
    }

    public void setTitle(String title) {
        this.currentTitle = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}