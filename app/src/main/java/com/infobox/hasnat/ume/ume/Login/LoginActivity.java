package com.infobox.hasnat.ume.ume.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.infobox.hasnat.ume.ume.Home.MainActivity;
import com.infobox.hasnat.ume.ume.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";


    private EditText userEmail, userPassword;
    private Button loginButton;
    private TextView linkSingUp;


    private ProgressDialog progressDialog;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        userEmail = (EditText)findViewById(R.id.inputEmail);
        userPassword = (EditText)findViewById(R.id.inputPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        linkSingUp = (TextView)findViewById(R.id.linkSingUp);
        progressDialog = new ProgressDialog(this);

        //redirect to register activity
        linkSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d( TAG, "onClick: go to Register Activity");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });


        /**
         * Login Button with Firebase
         */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                loginUserAccount(email, password);
            }
        });
    }

    private void loginUserAccount(String email, String password) {
        //just validation
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Your email is not valid.", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6){
            Toast.makeText(this,"May be your password had minimum 6 numbers of character.", Toast.LENGTH_SHORT).show();
        } else {

            //progress bar
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);



            // after validation checking, log in user a/c
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                // these lines for taking device token for notification
                                String userUID = mAuth.getCurrentUser().getUid();
                                String userDeiceToken = FirebaseInstanceId.getInstance().getToken();
                                userDatabaseReference.child(userUID).child("device_token").setValue(userDeiceToken)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                            } else {
                                Toast.makeText(LoginActivity.this, "Your email and password may be incorrect. Please check & try again.", Toast.LENGTH_SHORT).show();

                            }

                            progressDialog.dismiss();

                        }
                    });
        }
    }

}