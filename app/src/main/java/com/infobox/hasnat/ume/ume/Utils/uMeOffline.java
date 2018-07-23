package com.infobox.hasnat.ume.ume.Utils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class uMeOffline extends Application{

    @Override
    public void onCreate() {

        super.onCreate();

        //  all strings >> load offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //  all images >> load offline
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso builtPicasso = builder.build();
        builtPicasso.setIndicatorsEnabled(true);
        builtPicasso.setLoggingEnabled(true);

        Picasso.setSingletonInstance(builtPicasso);

    }
}
