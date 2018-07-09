package com.infobox.hasnat.ume.ume.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.infobox.hasnat.ume.ume.Home.MainActivity;
import com.infobox.hasnat.ume.ume.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private Context myContext = RegisterActivity.this;


    private EditText
            registerUserFullName,
            registerUserEmail,
            registerUserMobileNo,
            registerUserPassword,
            confirmRegisterUserPassword;

    private Button registerUserButton;
    private ProgressDialog progressDialog;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "on Create : started");

        mAuth = FirebaseAuth.getInstance();

        registerUserFullName = (EditText)findViewById(R.id.registerFullName);
        registerUserEmail = (EditText)findViewById(R.id.registerEmail);
        registerUserMobileNo = (EditText)findViewById(R.id.registerMobileNo);
        registerUserPassword = (EditText)findViewById(R.id.registerPassword);
        confirmRegisterUserPassword = (EditText)findViewById(R.id.confirm_registerPassword);

        //Working with Create A/C Button Or, Register a/c
        registerUserButton = (Button) findViewById(R.id.resisterButton);
        registerUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = registerUserFullName.getText().toString();
                String email = registerUserEmail.getText().toString();
                String mobile = registerUserMobileNo.getText().toString();
                String password = registerUserPassword.getText().toString();
                String confirmPassword = confirmRegisterUserPassword.getText().toString();

                // pass input parameter through this Method
                registerAccount(name, email, mobile, password, confirmPassword);
            }
        });
        progressDialog = new ProgressDialog(myContext);
    }

    private void registerAccount(String name, String email, String mobile, String password, String confirmPassword) {

        //Validation for empty fields
        if (TextUtils.isEmpty(name)){
            Toast.makeText(myContext,"Your name is required.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)){
            Toast.makeText(myContext,"Your email is required.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mobile)){
            Toast.makeText(myContext,"Your mobile no is required.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)){
            Toast.makeText(myContext,"Please fill this password field", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(myContext,"Please retype this password field", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)){
            Toast.makeText(myContext,"Your password don't match your confirm password", Toast.LENGTH_SHORT).show();
        } else {

            //NOw ready to create a user a/c
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(myContext,"You are authenticated successfully.", Toast.LENGTH_SHORT).show();

                                Intent mainIntent =  new Intent(myContext, LoginActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                                progressDialog.dismiss();

                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(myContext,"Error occurred : " + message, Toast.LENGTH_LONG).show();

                                progressDialog.dismiss();
                            }
                        }
                    });

            //config progressbar
            progressDialog.setTitle("Creating new account");
            progressDialog.setMessage("Please wait a moment....");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);


        }
    }
}
