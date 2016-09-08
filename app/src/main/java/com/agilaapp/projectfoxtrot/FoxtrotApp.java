package com.agilaapp.projectfoxtrot;

import android.app.Application;
import android.content.Intent;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class FoxtrotApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);

        Intent i = new Intent(this, SearchPlacesService.class);
        startService(i);
    }
}
