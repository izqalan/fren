package com.izqalan.messenger;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class Messenger extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         *Firebase apps automatically handle temporary network interruptions.
         * Cached data is available while offline and Firebase resends any writes
         * when network connectivity is restored.
         * */

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /*
        * Picasso offline
        * */




    }
}
