package com.infobox.hasnat.ume.ume.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.Chat.ChatActivity;
import com.infobox.hasnat.ume.ume.Model.Friends;
import com.infobox.hasnat.ume.ume.R;
import com.infobox.hasnat.ume.ume.Utils.UserLastSeenTime;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View view;
    private RecyclerView chat_list;

    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;

    String current_user_id;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chats, container, false);

        chat_list = view.findViewById(R.id.chatList);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        chat_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chat_list.setLayoutManager(linearLayoutManager);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> recyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsDatabaseReference, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, ChatsVH> adapter = new FirebaseRecyclerAdapter<Friends, ChatsVH>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsVH holder, int position, @NonNull Friends model) {
                final String userID = getRef(position).getKey();
                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("user_name").getValue().toString();
                            final String userPresence = dataSnapshot.child("active_now").getValue().toString();
                            final String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();

                            if (!userThumbPhoto.equals("default_image")) { // default image condition for new user
                                Picasso.get()
                                        .load(userThumbPhoto)
                                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(holder.user_photo, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get()
                                                        .load(userThumbPhoto)
                                                        .placeholder(R.drawable.default_profile_image)
                                                        .into(holder.user_photo);
                                            }
                                        });
                            }
                            holder.user_name.setText(userName);

                            //active status
                            holder.active_icon.setVisibility(View.GONE);
                            if (userPresence.contains("true")) {
                                holder.user_presence.setText("Active now");
                                holder.active_icon.setVisibility(View.VISIBLE);
                            } else {
                                holder.active_icon.setVisibility(View.GONE);
                                UserLastSeenTime lastSeenTime = new UserLastSeenTime();
                                long last_seen = Long.parseLong(userPresence);
                                String lastSeenOnScreenTime = lastSeenTime.getTimeAgo(last_seen, getContext());
                                Log.e("lastSeenTime", lastSeenOnScreenTime);
                                if (lastSeenOnScreenTime != null) {
                                    holder.user_presence.setText(lastSeenOnScreenTime);
                                }
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // user active status validation
                                    if (dataSnapshot.child("active_now").exists()) {

                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("visitUserId", userID);
                                        chatIntent.putExtra("userName", userName);
                                        startActivity(chatIntent);

                                    } else {
                                        userDatabaseReference.child(userID).child("active_now")
                                                .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("visitUserId", userID);
                                                chatIntent.putExtra("userName", userName);
                                                startActivity(chatIntent);
                                            }
                                        });


                                    }
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_single_profile_display, viewGroup, false);
                return new ChatsVH(view);
            }
        };

        chat_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsVH extends RecyclerView.ViewHolder{
        TextView user_name, user_presence;
        CircleImageView user_photo;
        ImageView active_icon;
        public ChatsVH(View itemView) {
            super(itemView);
            user_name = itemView.findViewById(R.id.all_user_name);
            user_photo = itemView.findViewById(R.id.all_user_profile_img);
            user_presence = itemView.findViewById(R.id.all_user_status);
            active_icon = itemView.findViewById(R.id.activeIcon);
        }
    }


}
