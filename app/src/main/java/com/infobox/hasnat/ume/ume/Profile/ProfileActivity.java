package com.infobox.hasnat.ume.ume.Profile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.infobox.hasnat.ume.ume.Model.ProfileInfo;
import com.infobox.hasnat.ume.ume.ProfileSetting.SettingsActivity;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Picasso;

import java.io.NotActiveException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button sendFriendRequest_Button, declineFriendRequest_Button;
    private TextView profileName, profileStatus, u_work, go_my_profile;
    private ImageView profileImage, verified_icon;

    private DatabaseReference userDatabaseReference;

    private DatabaseReference friendRequestReference;
    private FirebaseAuth mAuth;
    private String CURRENT_STATE;

    public String receiver_userID; // Visited profile's id
    public String senderID; // Owner ID

    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference notificationDatabaseReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendRequestReference.keepSynced(true); // for offline

        mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid(); // GET SENDER ID

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendsDatabaseReference.keepSynced(true); // for offline

        notificationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("notifications");
        notificationDatabaseReference.keepSynced(true); // for offline


        /**
         * Set Home Activity Toolbar Name
         */
        mToolbar = findViewById(R.id.single_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // back on previous activity
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag", "onClick : navigating back to back activity ");
                finish();
            }
        });

        receiver_userID = getIntent().getExtras().get("visitUserId").toString();

        sendFriendRequest_Button = findViewById(R.id.visitUserFrndRqstSendButton);
        declineFriendRequest_Button = findViewById(R.id.visitUserFrndRqstDeclineButton);
        profileName = findViewById(R.id.visitUserProfileName);
        profileStatus = findViewById(R.id.visitUserProfileStatus);
        verified_icon = findViewById(R.id.visit_verified_icon);
        profileImage = findViewById(R.id.visit_user_profile_image);
        u_work = findViewById(R.id.visit_work);
        go_my_profile = findViewById(R.id.go_my_profile);

        verified_icon.setVisibility(View.INVISIBLE);

        CURRENT_STATE = "not_friends";

        /**
         * Load every single users data
         */
        userDatabaseReference.child(receiver_userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String nickname = dataSnapshot.child("user_nickname").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String profession = dataSnapshot.child("user_profession").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                String verified = dataSnapshot.child("verified").getValue().toString();

                if (nickname.isEmpty()){
                    profileName.setText(name);
                } else {
                    String full_name = name +" ("+nickname+")";
                    profileName.setText(full_name);
                }


                if (profession.length() > 2){
                    u_work.setText("  " + profession);
                } if (profession.equals("")){
                    u_work.setText("  Not provided yet");
                }

                profileStatus.setText(status);
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                if (verified.contains("true")){
                    verified_icon.setVisibility(View.VISIBLE);
                }

                // for fixing dynamic cancel / friend / unfriend / accept button
                friendRequestReference.child(senderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // if in database has these data then, execute conditions below
                                if (dataSnapshot.hasChild(receiver_userID)) {
                                    String requestType = dataSnapshot.child(receiver_userID)
                                            .child("request_type").getValue().toString();

                                    if (requestType.equals("sent")) {
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequest_Button.setText("Cancel Friend Request");

                                        declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                        declineFriendRequest_Button.setEnabled(false);

                                    } else if (requestType.equals("received")) {
                                        CURRENT_STATE = "request_received";
                                        sendFriendRequest_Button.setText("Accept Friend Request");

                                        declineFriendRequest_Button.setVisibility(View.VISIBLE);
                                        declineFriendRequest_Button.setEnabled(true);


                                        declineFriendRequest_Button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                declineFriendRequest();
                                            }
                                        });

                                    }

                                } else {

                                friendsDatabaseReference.child(senderID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    if (dataSnapshot.hasChild(receiver_userID)){
                                                        CURRENT_STATE = "friends";
                                                        sendFriendRequest_Button.setText("Unfriend This Person");

                                                        declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                        declineFriendRequest_Button.setEnabled(false);

                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
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

        declineFriendRequest_Button.setVisibility(View.GONE);
        declineFriendRequest_Button.setEnabled(false);

        /** Send / Cancel / Accept / Unfriend >> request mechanism */
        if (!senderID.equals(receiver_userID)){ // condition for current owner / sender id
            sendFriendRequest_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest_Button.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){
                        sendFriendRequest();

                    } else if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest();

                    } else if (CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest();

                    } else if (CURRENT_STATE.equals("friends")){
                        unfriendPerson();

                    }

                }
            });
        } else {
            sendFriendRequest_Button.setVisibility(View.INVISIBLE);
            declineFriendRequest_Button.setVisibility(View.INVISIBLE);
            go_my_profile.setVisibility(View.VISIBLE);
            go_my_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }



    } // ending OnCreate

    private void declineFriendRequest() {
        //for declination, delete data from friends_request nodes
        // delete from, sender >> receiver > values
        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            // delete from, receiver >> sender > values
                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // after deleting data, just set button attributes
                                                sendFriendRequest_Button.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequest_Button.setText("Send Friend Request");

                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                declineFriendRequest_Button.setEnabled(false);

                                            }
                                        }

                                    });

                        }
                    }

                });
    }

    private void unfriendPerson() {
        //for unfriend, delete data from friends nodes
        // delete from, sender >> receiver > values
        friendsDatabaseReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            friendsDatabaseReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            sendFriendRequest_Button.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            sendFriendRequest_Button.setText("Send Friend Request");

                                            declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                            declineFriendRequest_Button.setEnabled(false);

                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        //
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("EEEE, dd MMM, yyyy");
        final String friendshipDate = currentDate.format(myCalendar.getTime());

        friendsDatabaseReference.child(senderID).child(receiver_userID).child("date").setValue(friendshipDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        friendsDatabaseReference.child(receiver_userID).child(senderID).child("date").setValue(friendshipDate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        /**
                                         *  because of accepting friend request,
                                         *  there have no more request them. So, for delete these node
                                         */
                                        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            // delete from users friend_requests node, receiver >> sender > values
                                                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                // after deleting data, just set button attributes
                                                                                sendFriendRequest_Button.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendFriendRequest_Button.setText("Unfriend This Person");

                                                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                                                declineFriendRequest_Button.setEnabled(false);
                                                                            }
                                                                        }

                                                                    });

                                                        }
                                                    }

                                                }); //

                                    }
                                });
                    }
                });
    }



    private void cancelFriendRequest() {
        //for cancellation, delete data from user nodes
        // delete from, sender >> receiver > values
        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            // delete from, receiver >> sender > values
                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // after deleting data, just set button attributes
                                                sendFriendRequest_Button.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequest_Button.setText("Send Friend Request");

                                                declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                declineFriendRequest_Button.setEnabled(false);

                                            }
                                        }

                                    });

                        }
                    }

                });

    }



    private void sendFriendRequest() {
        // insert or, put data to >> sender >> receiver >> request_type >> sent
        friendRequestReference.child(senderID).child(receiver_userID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            // change or, put data to >> receiver >> sender>> request_type >> received
                            friendRequestReference.child(receiver_userID).child(senderID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                //Request notification mechanism
                                                HashMap<String, String> notificationData = new HashMap<String, String>();
                                                notificationData.put("from", senderID);
                                                notificationData.put("type", "request");

                                                notificationDatabaseReference.child(receiver_userID).push().setValue(notificationData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    // Request main mechanism
                                                                    sendFriendRequest_Button.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    sendFriendRequest_Button.setText("Cancel Friend Request");

                                                                    declineFriendRequest_Button.setVisibility(View.INVISIBLE);
                                                                    declineFriendRequest_Button.setEnabled(false);
                                                                }

                                                            }
                                                        });

                                            }
                                        }
                                    });


                        }



                    }
                });


    }


}
