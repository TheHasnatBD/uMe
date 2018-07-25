package com.infobox.hasnat.ume.ume.WelcomeSlide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fabric
        Fabric.with(this, new Crashlytics());

        startActivity(new Intent(WelcomeActivity.this, IntroActivity.class));
        finish();

    }

}
