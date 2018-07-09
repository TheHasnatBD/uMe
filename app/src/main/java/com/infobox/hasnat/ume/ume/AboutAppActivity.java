package com.infobox.hasnat.ume.ume;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.infobox.hasnat.ume.ume.R;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        /**
         * Set Home Activity Toolbar Name
         */
        Toolbar mToolbar = (Toolbar)findViewById(R.id.about_page_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("About");
    }
}
