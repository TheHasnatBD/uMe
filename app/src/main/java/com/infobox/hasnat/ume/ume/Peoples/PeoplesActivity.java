package com.infobox.hasnat.ume.ume.Peoples;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.infobox.hasnat.ume.ume.R;
import com.infobox.hasnat.ume.ume.Models.AllPeoplesRecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeoplesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView peoples_list;
    private DatabaseReference peoplesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peoples);

        toolbar = findViewById(R.id.people_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Peoples");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup recycler view
        peoples_list = (RecyclerView)findViewById(R.id.userLIst);
        peoples_list.setHasFixedSize(true);
        peoples_list.setLayoutManager(new LinearLayoutManager(this));

        peoplesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        peoplesDatabaseReference.keepSynced(true); // for offline


    }

    /**
     *  FirebaseUI for Android — UI Bindings for Firebase
     *
     *  Library link- https://github.com/firebase/FirebaseUI-Android
     */
    @Override
    protected void onStart() {

        super.onStart();


        FirebaseRecyclerAdapter<AllPeoplesRecyclerView, peoplesViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllPeoplesRecyclerView, peoplesViewHolder>
                (
                        AllPeoplesRecyclerView.class,
                        R.layout.all_peoples_profile_display,
                        peoplesViewHolder.class,
                        peoplesDatabaseReference
                ) {
            @Override
            protected void populateViewHolder(peoplesViewHolder viewHolder, AllPeoplesRecyclerView model, final int position) {

                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());


                /**
                 *  on list >> clicking item, then, go to single user profile
                 */
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id = getRef(position).getKey();

                        Intent intent = new Intent(PeoplesActivity.this, ProfileActivity.class);
                        intent.putExtra("visitUserId", visit_user_id);
                        startActivity(intent);
                    }
                });


            }
        };
        peoples_list.setAdapter(firebaseRecyclerAdapter);

    }

    public static class peoplesViewHolder extends RecyclerView.ViewHolder{

        View view;

        public peoplesViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setUser_name(String user_name) {
            TextView name = (TextView)view.findViewById(R.id.all_user_name);
            name.setText(user_name);
        }
        public void setUser_status(String user_status) {
            TextView status = (TextView)view.findViewById(R.id.all_user_status);
            status.setText(user_status);
        }
        public void setUser_thumb_image(final Context applicationContext, final String user_thumb_image) {

            final CircleImageView thumb_image = (CircleImageView)view.findViewById(R.id.all_user_profile_img);

            if(!thumb_image.equals("default_image")) { // default image condition for new user
                Picasso.get()
                        .load(user_thumb_image)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(thumb_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(user_thumb_image)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(thumb_image);
                            }
                        });
            }
        }




    }
}
