package com.infobox.hasnat.ume.ume.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.Chat.ChatActivity;
import com.infobox.hasnat.ume.ume.Models.Chats;
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

        chat_list = (RecyclerView) view.findViewById(R.id.chatList);

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

        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> friendsRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_peoples_profile_display,
                        ChatsFragment.ChatsViewHolder.class,
                        friendsDatabaseReference
                ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position) {

                final String user_id_list = getRef(position).getKey();

                userDatabaseReference.child(user_id_list).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot==null){
                            return;
                        }
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                        //String user_status = dataSnapshot.child("user_status").getValue().toString();

                        // online active status
                        if (dataSnapshot.hasChild("active_now")){
                            String active_status = dataSnapshot.child("active_now").getValue().toString();
                            viewHolder.setActiveUser(active_status);
                            viewHolder.setUserActiveTimeStatus(active_status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setUserThumbPhoto(userThumbPhoto, getContext());

                        //active status
                        viewHolder.m_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // user active status validation
                                if (dataSnapshot.child("active_now").exists()){

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visitUserId", user_id_list);
                                    chatIntent.putExtra("userName", userName);
                                    startActivity(chatIntent);

                                } else {
                                    userDatabaseReference.child(user_id_list).child("active_now")
                                            .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visitUserId", user_id_list);
                                            chatIntent.putExtra("userName", userName);
                                            startActivity(chatIntent);
                                        }
                                    });


                                }

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        chat_list.setAdapter(friendsRecyclerAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View m_view;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            m_view = itemView;
        }


        public void setUserName(String userName){
            TextView user_name = (TextView)m_view.findViewById(R.id.all_user_name);
            user_name.setText(userName);
        }

        public void setUserThumbPhoto(final String userThumbPhoto, final Context ctx){

            final CircleImageView thumb_photo = (CircleImageView)m_view.findViewById(R.id.all_user_profile_img);

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


        //set green icon for active user
        public void setActiveUser(String activeUser) {
            ImageView active_image =  m_view.findViewById(R.id.activeIcon);
            if (activeUser.equals("true")){
                active_image.setVisibility(View.VISIBLE);
            } else {
                active_image.setVisibility(View.INVISIBLE);
            }
        }


        public void setUserActiveTimeStatus(String active_status) {
            TextView u_status = m_view.findViewById(R.id.all_user_status);
            //u_status.setText(active_status);

            //active status
            if (active_status.contains("true")){
                u_status.setText("Active now");
            } else {

                UserLastSeenTime lastSeenTime = new UserLastSeenTime();
                long last_seen = Long.parseLong(active_status);

                //String lastSeenOnScreenTime = lastSeenTime.getTimeAgo(last_seen).toString();
                String lastSeenOnScreenTime = lastSeenTime.getTimeAgo(last_seen, m_view.getContext()).toString();
                Log.e("lastSeenTime", lastSeenOnScreenTime);

                if (lastSeenOnScreenTime != null){
                    u_status.setText(lastSeenOnScreenTime);
                }
            }

        }
    }
}
