package com.agilaapp.projectfoxtrot;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SearchPlacesBoundService extends Service {
    private static final String TAG = SearchPlacesBoundService.class.getSimpleName();
    //  private final IBinder mBinder = new LocalBinder();

    //    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void printHelloWorld() {
        Log.d(TAG, "printHelloWorld: ");
    }

//    public class LocalBinder extends Binder{
//        SearchPlacesBoundService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return SearchPlacesBoundService.this;
//        }
//    }
}
