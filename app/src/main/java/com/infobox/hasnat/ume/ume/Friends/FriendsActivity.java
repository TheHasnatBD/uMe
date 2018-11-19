package com.infobox.hasnat.ume.ume.Friends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.infobox.hasnat.ume.ume.Model.ProfileInfo;
import com.infobox.hasnat.ume.ume.Profile.ProfileActivity;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
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


        // Setup recycler view
        friend_list_RV = findViewById(R.id.friendList);
        friend_list_RV.setHasFixedSize(true);
        friend_list_RV.setLayoutManager(new LinearLayoutManager(this));

        showPeopleList();
    }

    /**
     *  FirebaseUI for Android — UI Bindings for Firebase
     *
     *  Library link- https://github.com/firebase/FirebaseUI-Android
     */
    private void showPeopleList(){
        FirebaseRecyclerOptions<Friends> recyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsDatabaseReference, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsVH> recyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsVH>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsVH holder, int position, @NonNull Friends model) {
                holder.date.setText("Friendship date -\n" + model.getDate());

                final String userID = getRef(position).getKey();

                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String active_status = dataSnapshot.child("active_now").getValue().toString();

                        // online active status
                        holder.active_icon.setVisibility(View.GONE);
                        if (active_status.contains("active_now")){
                            holder.active_icon.setVisibility(View.VISIBLE);
                        } else {
                            holder.active_icon.setVisibility(View.GONE);
                        }

                        holder.name.setText(userName);
                        Picasso.get()
                                .load(userThumbPhoto)
                                .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                .placeholder(R.drawable.default_profile_image)
                                .into(holder.profile_thumb);


                        //click item, 2 options in a dialogue will be appear
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] =  new CharSequence[]{"Send Message", userName+"'s profile"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){
                                            // user active status validation
                                            if (dataSnapshot.child("active_now").exists()){

                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("visitUserId", userID);
                                                chatIntent.putExtra("userName", userName);
                                                startActivity(chatIntent);

                                            } else {
                                                userDatabaseReference.child(userID).child("active_now")
                                                        .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                        chatIntent.putExtra("visitUserId", userID);
                                                        chatIntent.putExtra("userName", userName);
                                                        startActivity(chatIntent);
                                                    }
                                                });


                                            }

                                        }

                                        if (which == 1){
                                            Intent profileIntent = new Intent(FriendsActivity.this, ProfileActivity.class);
                                            profileIntent.putExtra("visitUserId", userID);
                                            startActivity(profileIntent);
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });


            }

            @NonNull
            @Override
            public FriendsVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_single_profile_display, viewGroup, false);
                return new FriendsVH(view);
            }
        };

        friend_list_RV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    public static class FriendsVH extends RecyclerView.ViewHolder{
        public TextView name;
        TextView date;
        CircleImageView profile_thumb;
        ImageView active_icon;

        public FriendsVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.all_user_name);
            date = itemView.findViewById(R.id.all_user_status);
            profile_thumb = itemView.findViewById(R.id.all_user_profile_img);
            active_icon = itemView.findViewById(R.id.activeIcon);
        }
    }


}
