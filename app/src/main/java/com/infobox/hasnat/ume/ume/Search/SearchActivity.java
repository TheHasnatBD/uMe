package com.infobox.hasnat.ume.ume.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.infobox.hasnat.ume.ume.Models.AllPeoplesRecyclerView;
import com.infobox.hasnat.ume.ume.Peoples.ProfileActivity;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText serachInput;
    private ImageView backButton;
    private ImageButton searchBtn;

    private RecyclerView peoples_list;
    private DatabaseReference peoplesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // appbar / toolbar
        toolbar = findViewById(R.id.search_appbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.appbar_search, null);
        actionBar.setCustomView(view);

        serachInput = findViewById(R.id.serachInput);
        backButton = findViewById(R.id.backButton);
        searchBtn = findViewById(R.id.searchBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = serachInput.getText().toString();

                if (TextUtils.isEmpty(searchString)){
                    Toast.makeText(SearchActivity.this, "Write a name", Toast.LENGTH_SHORT).show();
                }

                searchPeopleProfile(searchString);

            }
        });


        // Setup recycler view
        peoples_list = findViewById(R.id.SearchList);
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
    private void searchPeopleProfile(String searchString) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

        Query searchQuery = peoplesDatabaseReference.orderByChild("user_name")
                .startAt(searchString).endAt(searchString + "\uf8ff");

        FirebaseRecyclerAdapter<AllPeoplesRecyclerView, peoplesViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllPeoplesRecyclerView, peoplesViewHolder>
                (
                        AllPeoplesRecyclerView.class,
                        R.layout.all_peoples_profile_display,
                        peoplesViewHolder.class,
                        searchQuery
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

                        Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
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
            TextView name = view.findViewById(R.id.all_user_name);
            name.setText(user_name);
        }
        public void setUser_status(String user_status) {
            TextView status = view.findViewById(R.id.all_user_status);
            status.setText(user_status);
        }
        public void setUser_thumb_image(final Context applicationContext, final String user_thumb_image) {

            final CircleImageView thumb_image = view.findViewById(R.id.all_user_profile_img);

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
