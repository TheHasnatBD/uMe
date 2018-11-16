package com.infobox.hasnat.ume.ume.ForgotPassword;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.infobox.hasnat.ume.ume.LoginReg.LoginActivity;
import com.infobox.hasnat.ume.ume.LoginReg.RegisterActivity;
import com.infobox.hasnat.ume.ume.R;

import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class ForgotPassActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText forgotEmail;
    private Button resetPassButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        mToolbar = findViewById(R.id.fp_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        auth = FirebaseAuth.getInstance();

        forgotEmail = findViewById(R.id.forgotEmail);
        resetPassButton = findViewById(R.id.resetPassButton);
        resetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = forgotEmail.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toasty.error(ForgotPassActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toasty.error(ForgotPassActivity.this,"Email format is not valid.", Toast.LENGTH_SHORT).show();
                } else {
                    // send email to reset password
                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                emailSentSuccessPopUp();

                                // LAUNCH activity after certain time period
                                new Timer().schedule(new TimerTask(){
                                    public void run() {
                                        ForgotPassActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                auth.signOut();

                                                Intent mainIntent =  new Intent(ForgotPassActivity.this, LoginActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                                Toasty.info(ForgotPassActivity.this, "Please check your email.", Toast.LENGTH_LONG).show();

                                            }
                                        });
                                    }
                                }, 8000);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(ForgotPassActivity.this, "Oops!! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void emailSentSuccessPopUp() {
        // Custom Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassActivity.this);
        View view = LayoutInflater.from(ForgotPassActivity.this).inflate(R.layout.register_success_popup, null);
        TextView successMessage = view.findViewById(R.id.successMessage);
        successMessage.setText("Password reset link has been sent successfully.\nPlease check your email. Thank You.");
        builder.setCancelable(true);

        builder.setView(view);
        builder.show();
    }

}
