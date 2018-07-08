package com.infobox.hasnat.ume.ume.Welcome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.infobox.hasnat.ume.ume.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Splash Time and 1st activity
        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000); // 4 secs

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intend = new Intent(getApplicationContext(), IntroActivity.class);
                    startActivity(intend);
                    finish();
                }
            }
        };
        myThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
