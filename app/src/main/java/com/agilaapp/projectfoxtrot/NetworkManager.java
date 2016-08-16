package com.agilaapp.projectfoxtrot;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkManager {
    private static NetworkManager sInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    public static synchronized NetworkManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NetworkManager(context);
        }
        return sInstance;
    }

    private NetworkManager(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
