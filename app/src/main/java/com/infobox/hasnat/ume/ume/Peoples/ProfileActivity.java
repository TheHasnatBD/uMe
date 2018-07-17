package com.infobox.hasnat.ume.ume.Peoples;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button sendFriendRequest_Button, declineFriendRequest;
    private TextView profileName, profileStatus;
    private ImageView profileImage;

    private DatabaseReference userDatabaseReference;

    private DatabaseReference friendRequestReference;
    private FirebaseAuth mAuth;
    private String CURRENT_STATE;

    private String receiver_userID; // Visited profile's id
    private String senderID; // Owner ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid(); // GET SENDER ID


        /**
         * Set Home Activity Toolbar Name
         */
        mToolbar = (Toolbar)findViewById(R.id.single_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiver_userID = getIntent().getExtras().get("visitUserId").toString();

        sendFriendRequest_Button = (Button)findViewById(R.id.visitUserFrndRqstSendButton);
        declineFriendRequest = (Button)findViewById(R.id.visitUserFrndRqstDeclineButton);
        profileName = (TextView)findViewById(R.id.visitUserProfileName);
        profileStatus = (TextView)findViewById(R.id.visitUserProfileStatus);
        profileImage = (ImageView)findViewById(R.id.visit_user_profile_image);

        CURRENT_STATE = "not_friends";

        /**
         * Load every single users data
         */
        userDatabaseReference.child(receiver_userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                // for fixing dynamic cancel button
                friendRequestReference.child(senderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(receiver_userID)){
                                    String requestType = dataSnapshot.child(receiver_userID)
                                            .child("request_type").getValue().toString();

                                    if (requestType.equals("sent")){
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequest_Button.setText("Cancel Friend Request");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /** Send Friend request mechanism */
        sendFriendRequest_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendFriendRequest_Button.setEnabled(false);
                if (CURRENT_STATE.equals("not_friends")){
                    sendFriendRequest();
                }

            }
        });


    }

    private void sendFriendRequest() {
        friendRequestReference.child(senderID).child(receiver_userID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendRequestReference.child(receiver_userID).child(senderID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                sendFriendRequest_Button.setEnabled(true);

                                                CURRENT_STATE = "request_sent";
                                                sendFriendRequest_Button.setText("Cancel Friend Request");
                                            }
                                        }
                                    });


                        }



                    }
                });


    }


}
