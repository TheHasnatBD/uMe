package com.infobox.hasnat.ume.ume.ProfileSetting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import xyz.hasnat.sweettoast.SweetToast;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profile_settings_image;
    private TextView display_status, updatedMsg, recheckGender;
    private ImageView editPhotoIcon, editStatusBtn;
    private EditText display_name, display_email, user_phone, user_profession, user_nickname;
    private RadioButton maleRB, femaleRB;

    private Button saveInfoBtn;

    private DatabaseReference getUserDatabaseReference;
    private FirebaseAuth mAuth;
    private StorageReference mProfileImgStorageRef;
    private StorageReference thumb_image_ref;

    private final static int GALLERY_PICK_CODE = 1;
    Bitmap thumb_Bitmap = null;

    private ProgressDialog progressDialog;
    private String selectedGender = "", profile_download_url, profile_thumb_download_url;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        getUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        getUserDatabaseReference.keepSynced(true); // for offline

        mProfileImgStorageRef = FirebaseStorage.getInstance().getReference().child("profile_image");
        thumb_image_ref = FirebaseStorage.getInstance().getReference().child("thumb_image");

        profile_settings_image = findViewById(R.id.profile_img);
        display_name = findViewById(R.id.user_display_name );
        user_nickname = findViewById(R.id.user_nickname );
        user_profession = findViewById(R.id.profession);
        display_email = findViewById(R.id.userEmail);
        user_phone = findViewById(R.id.phone);
        display_status = findViewById(R.id.userProfileStatus);
        editPhotoIcon = findViewById(R.id.editPhotoIcon);
        saveInfoBtn = findViewById(R.id.saveInfoBtn);
        editStatusBtn = findViewById(R.id.statusEdit);
        updatedMsg = findViewById(R.id.updatedMsg);

        recheckGender = findViewById(R.id.recheckGender);
        recheckGender.setVisibility(View.VISIBLE);


        maleRB = findViewById(R.id.maleRB);
        femaleRB = findViewById(R.id.femaleRB);


        Toolbar toolbar = findViewById(R.id.profile_settings_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);

        // Retrieve data from database
        getUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // retrieve data from db
                String name = dataSnapshot.child("user_name").getValue().toString();
                String nickname = dataSnapshot.child("user_nickname").getValue().toString();
                String profession = dataSnapshot.child("user_profession").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String email = dataSnapshot.child("user_email").getValue().toString();
                String phone = dataSnapshot.child("user_mobile").getValue().toString();
                String gender = dataSnapshot.child("user_gender").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                display_status.setText(status);

                display_name.setText(name);
                display_name.setSelection(display_name.getText().length());

                user_nickname.setText(nickname);
                user_nickname.setSelection(user_nickname.getText().length());

                user_profession.setText(profession);
                user_profession.setSelection(user_profession.getText().length());

                user_phone.setText(phone);
                user_phone.setSelection(user_phone.getText().length());

                display_email.setText(email);


                if(!image.equals("default_image")){ // default image condition for new user
                    Picasso.get()
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE) // for offline
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .into(profile_settings_image);
                }

                if (gender.equals("Male")){
                    maleRB.setChecked(true);
                } else if (gender.equals("Female")){
                    femaleRB.setChecked(true);
                } else {
                    //maleRB.setChecked(false);
                    //femaleRB.setChecked(false);
                    return;
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /** Change profile photo from GALLERY */
        editPhotoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, GALLERY_PICK_CODE);
            }
        });

        /** Edit information */
        saveInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = display_name.getText().toString();
                String uNickname = user_nickname.getText().toString();
                String uPhone = user_phone.getText().toString();
                String uProfession = user_profession.getText().toString();

                saveInformation(uName, uNickname, uPhone, uProfession, selectedGender);
            }
        });

        /** Edit STATUS */
        editStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String previous_status = display_status.getText().toString();

                Intent statusUpdateIntent = new Intent(SettingsActivity.this, StatusUpdateActivity.class);
                // previous status from db
                statusUpdateIntent.putExtra("ex_status", previous_status);
                startActivity(statusUpdateIntent);
            }
        });

        // hide soft keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    } // Ending onCrate

    // Gender Radio Button
    public void selectedGenderRB(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.maleRB:
                if (checked){
                    selectedGender = "Male";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
            case R.id.femaleRB:
                if (checked){
                    selectedGender = "Female";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
        }
    }


    private void saveInformation(String uName, String uNickname, String uPhone, String uProfession, String uGender) {
        if (uGender.length()<1){
            recheckGender.setTextColor(Color.RED);
            //Toasty.info(this, "To save changes, please recheck your GENDER", 1000).show();
        } else if (TextUtils.isEmpty(uName)){
            SweetToast.error(this, "Oops! your name can't be empty");
        } else if (uName.length()<3 || uName.length()>40){
            SweetToast.warning(this, "Your name should be 3 to 40 numbers of characters");
        } else if (TextUtils.isEmpty(uPhone)){
            SweetToast.error(this, "Your mobile number is required.");
        } else if (uPhone.length()<11){
            SweetToast.warning(this, "Sorry! your mobile number is too short");
        } else {
            getUserDatabaseReference.child("user_name").setValue(uName);
            getUserDatabaseReference.child("user_nickname").setValue(uNickname);
            getUserDatabaseReference.child("search_name").setValue(uName.toLowerCase());
            getUserDatabaseReference.child("user_profession").setValue(uProfession);
            getUserDatabaseReference.child("user_mobile").setValue(uPhone);
            getUserDatabaseReference.child("user_gender").setValue(uGender)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            updatedMsg.setVisibility(View.VISIBLE);

                            new Timer().schedule(new TimerTask(){
                                public void run() {
                                    SettingsActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            updatedMsg.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }, 1500);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** Cropping image functionality
         * Library Link- https://github.com/ArthurHub/Android-Image-Cropper
         * */
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                final Uri resultUri = result.getUri();

                File thumb_filePath_Uri = new File(resultUri.getPath());

                final String user_id = mAuth.getCurrentUser().getUid();

                /**
                 * compress image using compressor library
                 * link - https://github.com/zetbaitsu/Compressor
                 * */
                try{
                    thumb_Bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(45)
                            .compressToBitmap(thumb_filePath_Uri);
                } catch (IOException e){
                    e.printStackTrace();
                }


                // firebase storage for uploading the cropped image
                final StorageReference filePath = mProfileImgStorageRef.child(user_id + ".jpg");

                UploadTask uploadTask = filePath.putFile(resultUri);
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task){
                        if (!task.isSuccessful()){
                            SweetToast.error(SettingsActivity.this, "Profile Photo Error: " + task.getException().getMessage());
                            //throw task.getException();
                        }
                        profile_download_url = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            //Toasty.info(SettingsActivity.this, "Your profile photo is uploaded successfully.", Toast.LENGTH_SHORT).show();
                            // retrieve the stored image as profile photo
                            profile_download_url = task.getResult().toString();
                            Log.e("tag", "profile url: "+profile_download_url);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            thumb_Bitmap.compress(Bitmap.CompressFormat.JPEG, 45, outputStream);
                            final byte[] thumb_byte = outputStream.toByteArray();

                            // firebase storage for uploading the cropped and compressed image
                            final StorageReference thumb_filePath = thumb_image_ref.child(user_id + "jpg");
                            UploadTask thumb_uploadTask = thumb_filePath.putBytes(thumb_byte);

                            Task<Uri> thumbUriTask = thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task){
                                    if (!task.isSuccessful()){
                                        SweetToast.error(SettingsActivity.this, "Thumb Image Error: " + task.getException().getMessage());
                                    }
                                    profile_thumb_download_url = thumb_filePath.getDownloadUrl().toString();
                                    return thumb_filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    profile_thumb_download_url = task.getResult().toString();
                                    Log.e("tag", "thumb url: "+profile_thumb_download_url);
                                    if (task.isSuccessful()){
                                        Log.e("tag", "thumb profile updated");

                                        HashMap<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("user_image", profile_download_url);
                                        update_user_data.put("user_thumb_image", profile_thumb_download_url);

                                        getUserDatabaseReference.updateChildren(new HashMap<String, Object>(update_user_data))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.e("tag", "thumb profile updated");
                                                        progressDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("tag", "for thumb profile: "+ e.getMessage());
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }

                                }
                            });



                            ////
                            /*
                            thumb_uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toasty.warning(SettingsActivity.this,"Error occurred!! "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (taskSnapshot != null){
                                        profile_thumb_download_url = String.valueOf(taskSnapshot.getMetadata().getReference().getDownloadUrl());
                                        Log.e("tag", "profile_thumb_download_url: "+ profile_thumb_download_url);

                                        HashMap<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("user_image", profile_download_url);
                                        update_user_data.put("user_thumb_image", profile_thumb_download_url);

                                        getUserDatabaseReference.updateChildren(update_user_data)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("tag", "for thumb profile: "+ e.getMessage());
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e("tag", "thumb profile updated");
                                                progressDialog.dismiss();
                                                //Toasty.success(SettingsActivity.this,"Profile photo is updated successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }
                            }); */

                            /* addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    profile_thumb_download_url = task.getResult().toString();
                                    if (task.isSuccessful()){
                                         HashMap<String, Object> update__user_data = new HashMap<>();
                                        update__user_data.put("user_image", profile_download_url);
                                        update__user_data.put("user_thumb_ima  ge", profile_thumb_download_url);


                                        getUserDatabaseReference.updateChildren(update__user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        progressDialog.dismiss();
                                                        //Toasty.success(SettingsActivity.this,"Profile photo is updated successfully.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });
                            */
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
                // handling more event
                SweetToast.info(SettingsActivity.this,"Image cropping failed.");
            }
        }

    }


}
