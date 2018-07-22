package com.infobox.hasnat.ume.ume.Peoples;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button sendFriendRequest_Button, declineFriendRequest;
    private TextView profileName, profileStatus;
    private ImageView profileImage;

    private DatabaseReference userDatabaseReference;

    private DatabaseReference friendRequestReference;
    private FirebaseAuth mAuth;
    private String CURRENT_STATE;

    public String receiver_userID; // Visited profile's id
    public String senderID; // Owner ID

    private DatabaseReference friendsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid(); // GET SENDER ID

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");


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
                                if (dataSnapshot.exists()){
                                    // if in database has these data then, execute conditions below
                                    if (dataSnapshot.hasChild(receiver_userID)){
                                        String requestType = dataSnapshot.child(receiver_userID)
                                                .child("request_type").getValue().toString();

                                        if (requestType.equals("sent")){
                                            CURRENT_STATE = "request_sent";
                                            sendFriendRequest_Button.setText("Cancel Friend Request");

                                        } else if (requestType.equals("received")){
                                            CURRENT_STATE = "request_received";
                                            sendFriendRequest_Button.setText("Accept Friend Request");
                                        }
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


        /** Send / Cancel / Accept >> Friend request mechanism */
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
                }

            }
        });


    }

    private void acceptFriendRequest() {
        //
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String friendshipDate = currentDate.format(myCalendar.getTime());

        friendsDatabaseReference.child(senderID).child(receiver_userID).setValue(friendshipDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        friendsDatabaseReference.child(receiver_userID).child(senderID).setValue(friendshipDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        /**
                                         *  because of accepting friend request,
                                         *  there have no more request them. So, for delete these node
                                         */
                                        friendRequestReference.child(senderID).child(receiver_userID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            // delete from, receiver >> sender > values
                                                            friendRequestReference.child(receiver_userID).child(senderID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @SuppressLint("SetTextI18n")
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                // after deleting data, just set button attributes
                                                                                sendFriendRequest_Button.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendFriendRequest_Button.setText("Unfriend This Person");
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
