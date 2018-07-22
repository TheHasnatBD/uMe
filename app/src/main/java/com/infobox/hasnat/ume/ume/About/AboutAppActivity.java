package com.infobox.hasnat.ume.ume.About;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.infobox.hasnat.ume.ume.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutAppActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private Button gitBtn, InstaBtn, TwBtn, LinBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        /**
         * Set Home Activity Toolbar Name
         */
        mToolbar = (Toolbar)findViewById(R.id.about_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gitBtn = (Button)findViewById(R.id.git_btn);
        InstaBtn = (Button)findViewById(R.id.insta_btn);
        LinBtn = (Button)findViewById(R.id.lin_btn);
        TwBtn = (Button)findViewById(R.id.tw_btn);

        //methods


        // 4 buttons
        // git button
        gitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/TheHasnatBD");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // instagram button
        InstaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://instagram.com/TheHasnatBD");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Linkedin button
        LinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://linkedin.com/in/TheHasnatBD");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //twitter button
        TwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://twitter.com/TheHasnatBD");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


    }

}
