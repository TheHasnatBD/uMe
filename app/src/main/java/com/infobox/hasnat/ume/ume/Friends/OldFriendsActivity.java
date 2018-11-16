package com.infobox.hasnat.ume.ume.Friends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.Chat.ChatActivity;
import com.infobox.hasnat.ume.ume.Model.Friends;
import com.infobox.hasnat.ume.ume.Profile.ProfileActivity;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class OldFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinner;
    private RecyclerView friend_list_RV;

    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        toolbar = findViewById(R.id.friends_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        friendsDatabaseReference.keepSynced(true); // for offline

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabaseReference.keepSynced(true); // for offline


        spinner = findViewById(R.id.spinner);
        String[] categoryName = getResources().getStringArray(R.array.spinerViewPeople);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item ,categoryName);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String catName = adapterView.getItemAtPosition(i).toString().toLowerCase();
                showPeopleList(catName);
                Log.e("tag", "spinner: "+catName);
                //Toast.makeText(FriendsActivity.this, catName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Setup recycler view
        friend_list_RV = findViewById(R.id.friendList);
        friend_list_RV.setHasFixedSize(true);
        friend_list_RV.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     *  FirebaseUI for Android — UI Bindings for Firebase
     *
     *  Library link- https://github.com/firebase/FirebaseUI-Android
     */
    private void showPeopleList(String catName) {
        Query query = null;
        if (catName.equals("default")){
            query = friendsDatabaseReference.orderByValue();
            Log.e("tag", "spinner: default");
        } else if (catName.equals("name")){
            query = friendsDatabaseReference.orderByChild("user_name");
            Log.e("tag", "spinner: name");
        } else if (catName.equals("date")){
            query = friendsDatabaseReference.orderByChild("created_at");
            Log.e("tag", "spinner: date");
        }

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_single_profile_display,
                        FriendsViewHolder.class,
                        query
                ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, final int position) {

                viewHolder.setDate(model.getDate());

                final String user_id_list = getRef(position).getKey();

                userDatabaseReference.child(user_id_list).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();

                        // online active status
                        if (dataSnapshot.hasChild("active_now")){
                            String active_status = dataSnapshot.child("active_now").getValue().toString();
                            viewHolder.setActiveUser(active_status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setUserThumbPhoto(userThumbPhoto, OldFriendsActivity.this);

                        //click item, 2 options in a dialogue will be appear
                        viewHolder.m_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] =  new CharSequence[]{"Send Message", userName+"'s profile"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(OldFriendsActivity.this);
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){
                                            // user active status validation
                                            if (dataSnapshot.child("active_now").exists()){

                                                Intent chatIntent = new Intent(OldFriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("visitUserId", user_id_list);
                                                chatIntent.putExtra("userName", userName);
                                                startActivity(chatIntent);

                                            } else {
                                                userDatabaseReference.child(user_id_list).child("active_now")
                                                        .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent chatIntent = new Intent(OldFriendsActivity.this, ChatActivity.class);
                                                        chatIntent.putExtra("visitUserId", user_id_list);
                                                        chatIntent.putExtra("userName", userName);
                                                        startActivity(chatIntent);
                                                    }
                                                });


                                            }

                                        }

                                        if (which == 1){
                                            Intent profileIntent = new Intent(OldFriendsActivity.this, ProfileActivity.class);
                                            profileIntent.putExtra("visitUserId", user_id_list);
                                            startActivity(profileIntent);
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        friend_list_RV.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View m_view;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            m_view = itemView;
        }

        public void setDate(String date){
            TextView friendshipDate = m_view.findViewById(R.id.all_user_status);
            friendshipDate.setText("Friend since: \n" + date);
        }

        public void setUserName(String userName){
            TextView user_name = m_view.findViewById(R.id.all_user_name);
            user_name.setText(userName);
        }

        public void setUserThumbPhoto(final String userThumbPhoto, final Context ctx){

            final CircleImageView thumb_photo = m_view.findViewById(R.id.all_user_profile_img);

            if(!thumb_photo.equals("default_image")) { // default image condition for new user

                Picasso.get()
                        .load(userThumbPhoto)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(thumb_photo, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(userThumbPhoto)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(thumb_photo);
                            }
                        });
            }

        }


        public void setActiveUser(String activeUser) {

            ImageView active_image =  m_view.findViewById(R.id.activeIcon);
            if (activeUser.equals("true")){
                active_image.setVisibility(View.VISIBLE);

            } else {
                active_image.setVisibility(View.INVISIBLE);

            }

        }


    }
}
