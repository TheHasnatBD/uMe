package com.infobox.hasnat.ume.ume.LoginReg;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.infobox.hasnat.ume.ume.R;

import es.dmoral.toasty.Toasty;

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
    private FirebaseUser user;

    private DatabaseReference storeDefaultDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "on Create : started");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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
                final String name = registerUserFullName.getText().toString();
                final String email = registerUserEmail.getText().toString();
                final String mobile = registerUserMobileNo.getText().toString();
                String password = registerUserPassword.getText().toString();
                String confirmPassword = confirmRegisterUserPassword.getText().toString();

                // pass input parameter through this Method
                registerAccount(name, email, mobile, password, confirmPassword);
            }
        });
        progressDialog = new ProgressDialog(myContext);
    }// ending onCreate



    private void registerAccount(final String name, final String email, final String mobile, String password, String confirmPassword) {

        //Validation for empty fields
        if (TextUtils.isEmpty(name)) {
            Toasty.error(myContext, "Your name is required.", Toast.LENGTH_SHORT).show();
        } else if (name.length() < 3 || name.length() > 60){
            Toasty.error(myContext, "Your name should be 3 to 50 numbers of characters.", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(email)){
            Toasty.error(myContext, "Your email is required.", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toasty.error(myContext, "Your email is not valid.", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(mobile)){
            Toasty.error(myContext, "Your mobile number is required.", Toast.LENGTH_SHORT).show();
        } else if (mobile.length() < 11){
            Toasty.error(myContext, "Mobile number should be min 11 characters.", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)){
            Toasty.error(myContext, "Please fill this password field", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6){
            Toasty.error(myContext, "Create a password at least 6 characters long.", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(confirmPassword)){
            Toasty.warning(myContext, "Please retype in password field", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)){
            Toasty.error(myContext, "Your password don't match with your confirm password", Toast.LENGTH_SHORT).show();

        } else {
            //NOw ready to create a user a/c
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                // get and link storage
                                String current_userID =  mAuth.getCurrentUser().getUid();
                                storeDefaultDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(current_userID);

                                storeDefaultDatabaseReference.child("user_name").setValue(name);
                                storeDefaultDatabaseReference.child("verified").setValue("false");
                                storeDefaultDatabaseReference.child("search_name").setValue(name.toLowerCase());
                                storeDefaultDatabaseReference.child("user_mobile").setValue(mobile);
                                storeDefaultDatabaseReference.child("user_email").setValue(email);
                                storeDefaultDatabaseReference.child("created_at").setValue(ServerValue.TIMESTAMP);
                                storeDefaultDatabaseReference.child("user_status").setValue("Hi, I'm a new uMe user");
                                storeDefaultDatabaseReference.child("user_image").setValue("default_image"); // Original image
                                storeDefaultDatabaseReference.child("device_token").setValue(deviceToken);
                                storeDefaultDatabaseReference.child("user_thumb_image").setValue("default_image")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    // SENDING VERIFICATION EMAIL TO THE REGISTERED USER'S EMAIL
                                                    user = mAuth.getCurrentUser();
                                                    if (user != null){
                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toasty.success(myContext, "Registered successfully. Verification email has been sent. Please verify email", Toast.LENGTH_LONG).show();

                                                                            mAuth.signOut();

                                                                            Intent mainIntent =  new Intent(myContext, LoginActivity.class);
                                                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(mainIntent);
                                                                            finish();
                                                                        } else {
                                                                            mAuth.signOut();
                                                                        }
                                                                    }
                                                                });
                                                    }

                                                }
                                            }
                                        });

                            } else {
                                String message = task.getException().getMessage();
                                Toasty.error(myContext, "Error occurred : " + message, Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();

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
