package com.agilaapp.projectfoxtrot;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

public class NetworkManager {
    private static NetworkManager sInstance = new NetworkManager();
    private OkHttpClient mClient;

    public static synchronized NetworkManager getInstance() {
        if (sInstance == null) {
            sInstance = new NetworkManager();
        }
        return sInstance;
    }

    private NetworkManager() {
        mClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();
    }

    public OkHttpClient getOkHttpClient (){
        return mClient;
    }
}
