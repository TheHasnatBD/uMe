package com.infobox.hasnat.ume.ume.ProfileSetting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infobox.hasnat.ume.ume.R;
import es.dmoral.toasty.Toasty;

public class StatusUpdateActivity extends AppCompatActivity {

    private static final String TAG = "StatusUpdateActivity";

    private Toolbar mToolbar;
    private EditText status_from_input;
    private ProgressDialog progressDialog;

    private DatabaseReference statusDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        statusDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        status_from_input = findViewById(R.id.input_status);
        progressDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.update_status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // back on previous activity
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick : navigating back to 'SettingsActivity.class' ");
                finish();
            }
        });

        /**
         * retrieve previous profile status from SettingsActivity
         */
        String previousStatus = getIntent().getExtras().get("ex_status").toString();
        status_from_input.setText(previousStatus);
        status_from_input.setSelection(status_from_input.getText().length());
    } //ending onCreate

    // tool bar Status update done- menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.update_status_done_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.status_update_done){
            String new_status = status_from_input.getText().toString();
            changeProfileStatus(new_status);
        }
        return true;
    }

    private void changeProfileStatus(String new_status) {
        if (TextUtils.isEmpty(new_status)){
            Toasty.warning(getApplicationContext(), "Please write something about status", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage("Updating status...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            statusDatabaseReference.child("user_status").setValue(new_status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                finish();
                            } else {
                                Toasty.warning(getApplicationContext(), "Error occurred: failed to update.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}
