package com.infobox.hasnat.ume.ume;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profile_settings_image;
    private TextView display_name, display_status, display_email, changePhotoLInk;

    private Button updateStatusBtn;

    private DatabaseReference getUserDatabaseReference;
    private FirebaseAuth mAuth;
    private StorageReference mProfileImgStorageRef;

    private final static int GALLERY_PICK_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        getUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        mProfileImgStorageRef = FirebaseStorage.getInstance().getReference().child("profile_image");

        profile_settings_image = (CircleImageView)findViewById(R.id.profile_img);
        display_name = (TextView)findViewById(R.id.user_display_name);
        display_email = (TextView)findViewById(R.id.userEmail);
        display_status = (TextView)findViewById(R.id.userProfileStatus);
        changePhotoLInk = (TextView)findViewById(R.id.changeProfileImageLink);
        updateStatusBtn = (Button)findViewById(R.id.updateStatus);



        // Retrieve data from database
        getUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // retrieve data from db
                String name = dataSnapshot.child("user_name").getValue().toString();
                String email = dataSnapshot.child("user_email").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();


                display_name.setText(name);
                display_email.setText(email);
                display_status.setText(status);

                // Picasso LIBRARY
                Picasso.get().load(image).into(profile_settings_image);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /** Change profile photo from GALLERY */
        changePhotoLInk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** Cropping image functionality
         *
         * Library Link- https://github.com/ArthurHub/Android-Image-Cropper
         * */
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                // firebase storage for uploading the cropped image

                String user_id = mAuth.getCurrentUser().getUid();
                StorageReference filePath = mProfileImgStorageRef.child(user_id + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                       if (task.isSuccessful()){
                           Toast.makeText(SettingsActivity.this,"Your profile photo is uploaded successfully.", Toast.LENGTH_SHORT).show();

                           // retrieve the stored image as profile photo
                           String download_url = task.getResult().getDownloadUrl().toString();
                           getUserDatabaseReference.child("user_image").setValue(download_url)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {

                                           Toast.makeText(SettingsActivity.this,"profile photo is updated successfully.", Toast.LENGTH_SHORT).show();

                                       }
                                   });

                       } else {
                           Toast.makeText(SettingsActivity.this,"Error occurred!! Failed to upload profile photo.", Toast.LENGTH_SHORT).show();
                       }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
