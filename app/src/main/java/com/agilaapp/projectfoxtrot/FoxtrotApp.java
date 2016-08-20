package com.agilaapp.projectfoxtrot;

import android.app.Application;

import com.facebook.stetho.Stetho;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class FoxtrotApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
