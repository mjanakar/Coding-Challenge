package com.mobiquity.dropboxclient.model;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mobiquity.dropboxclient.MyApplication;

/**
 * Created by shanki on 4/17/15.
 */
public class VolleySingleton {
    private static VolleySingleton volleySingleton = null;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;

    private VolleySingleton() {
        requestQueue = Volley.newRequestQueue(MyApplication.getAppContext());
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private LruCache<String, Bitmap> cache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getInstance() {
        if (volleySingleton == null) {
            volleySingleton = new VolleySingleton();
        }
        return volleySingleton;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
