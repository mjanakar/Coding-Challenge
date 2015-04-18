package com.mobiquity.dropboxclient.adapters;

/**
 * Created by shanki on 4/17/15.
 * This class creates the adapter for the ListItems class.
 */

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.mobiquity.dropboxclient.R;
import com.mobiquity.dropboxclient.model.DropBoxMetaData;
import com.mobiquity.dropboxclient.model.VolleySingleton;

public class MyAdapter extends ArrayAdapter<DropBoxMetaData> {


    Context currentContext;
    int currentResource;
    ArrayList<DropBoxMetaData> dropBoxMetaDatas;
    private DropboxAPI<?> dropboxAPI;
    private VolleySingleton mVolleySingleton;
    private ImageLoader mImageLoader;

    public MyAdapter(Context context, int resource, ArrayList<DropBoxMetaData> objects, DropboxAPI<AndroidAuthSession> mdropboxApi) {
        super(context, resource, objects);
        this.currentContext = context;
        this.currentResource = resource;
        this.dropBoxMetaDatas = objects;
        this.dropboxAPI = mdropboxApi;
        mVolleySingleton = VolleySingleton.getInstance();
        mImageLoader = mVolleySingleton.getImageLoader();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(currentResource, parent, false);
        }
        DropBoxMetaData dropBoxMetaData = dropBoxMetaDatas.get(position);
        TextView desc = (TextView) convertView.findViewById(R.id.imagetext);
        desc.setText(dropBoxMetaData.getTitle());
        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.imageView);
        if (dropBoxMetaData.getTitle().endsWith("jpg") || dropBoxMetaData.getTitle().endsWith("jpeg"))
            thumbnail.setImageResource(R.drawable.camera);
        else if (dropBoxMetaData.getTitle().endsWith("txt"))
            thumbnail.setImageResource(R.drawable.notepad);
        else if (dropBoxMetaData.getTitle().endsWith("3gp"))
            thumbnail.setImageResource(R.drawable.mic);
        else if (dropBoxMetaData.getTitle().endsWith("mp4"))
            thumbnail.setImageResource(R.drawable.video);
        else
            thumbnail.setImageResource(android.R.drawable.ic_dialog_alert);
        return convertView;
    }

}