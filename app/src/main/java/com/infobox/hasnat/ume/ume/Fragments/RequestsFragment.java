package com.infobox.hasnat.ume.ume.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infobox.hasnat.ume.ume.Model.Requests;
import com.infobox.hasnat.ume.ume.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View view;
    private RecyclerView request_list;
    private Context context;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    String user_UId;
    private DatabaseReference userDatabaseReference;

    // for accept and cancel mechanism
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference friendReqDatabaseReference;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_requests, container, false);

        request_list = view.findViewById(R.id.requestList);
        request_list.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        user_UId = mAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(user_UId);

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendReqDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setStackFromEnd(true);
        request_list.setHasFixedSize(true);
        request_list.setLayoutManager(linearLayoutManager);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>
                (
                        Requests.class,
                        R.layout.request_single,
                        RequestsFragment.RequestsViewHolder.class,
                        databaseReference
                ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {
                final String user_id_list = getRef(position).getKey();


                // handling accept/cancel button
                DatabaseReference getTypeReference = getRef(position).child("request_type").getRef();
                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String requestType = dataSnapshot.getValue().toString();

                            ImageView re_icon = viewHolder.view.findViewById(R.id.receivedIcon);
                            ImageView se_icon = viewHolder.view.findViewById(R.id.sentIcon);
                            final ImageView verified_icon = viewHolder.view.findViewById(R.id.verifiedIcon);
                            verified_icon.setVisibility(View.GONE);

                            if (requestType.equals("received")){
                                re_icon.setVisibility(View.VISIBLE);
                                se_icon.setVisibility(View.GONE);
                                userDatabaseReference.child(user_id_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userVerified = dataSnapshot.child("verified").getValue().toString();
                                        final String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String user_status = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserThumbPhoto(userThumbPhoto, getContext());
                                        viewHolder.setUserStatus(user_status);
                                        if (userVerified.contains("true")){
                                            verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] =  new CharSequence[]{"Accept Request", "Cancel Request"};

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if (which == 0){
                                                            Calendar myCalendar = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("EEEE, dd MMM, yyyy");
                                                            final String friendshipDate = currentDate.format(myCalendar.getTime());

                                                            friendsDatabaseReference.child(user_UId).child(user_id_list).child("date").setValue(friendshipDate)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            friendsDatabaseReference.child(user_id_list).child(user_UId).child("date").setValue(friendshipDate)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            /**
                                                                                             *  because of accepting friend request,
                                                                                             *  there have no more request them. So, for delete these node
                                                                                             */
                                                                                            friendReqDatabaseReference.child(user_UId).child(user_id_list).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                // delete from users friend_requests node, receiver >> sender > values
                                                                                                                friendReqDatabaseReference.child(user_id_list).child(user_UId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()){
                                                                                                                                    // after deleting data
                                                                                                                                    Snackbar.make(view, "This person is now your friend", 1000).show();

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


                                                        if (which == 1){
                                                            //for cancellation, delete data from user nodes
                                                            // delete from, sender >> receiver > values
                                                            friendReqDatabaseReference.child(user_UId).child(user_id_list).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                // delete from, receiver >> sender > values
                                                                                friendReqDatabaseReference.child(user_id_list).child(user_UId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    //Toast.makeText(getActivity(), "Cancel Request", Toast.LENGTH_SHORT).show();
                                                                                                    Snackbar.make(view, "Canceled Request", 1000).show();

                                                                                                }
                                                                                            }

                                                                                        });

                                                                            }
                                                                        }

                                                                    });
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

                            if (requestType.equals("sent")){
                                re_icon.setVisibility(View.GONE);
                                se_icon.setVisibility(View.VISIBLE);
                                userDatabaseReference.child(user_id_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userVerified = dataSnapshot.child("verified").getValue().toString();
                                        final String userThumbPhoto = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String user_status = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserThumbPhoto(userThumbPhoto, getContext());
                                        viewHolder.setUserStatus(user_status);
                                        if (userVerified.contains("true")){
                                            verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] =  new CharSequence[]{"Cancel Sent Request"};

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0){
                                                            //for cancellation, delete data from user nodes
                                                            // delete from, sender >> receiver > values
                                                            friendReqDatabaseReference.child(user_UId).child(user_id_list).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                // delete from, receiver >> sender > values
                                                                                friendReqDatabaseReference.child(user_id_list).child(user_UId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Snackbar.make(view, "Cancel Sent Request", 1000).show();

                                                                                                }
                                                                                            }

                                                                                        });

                                                                            }
                                                                        }

                                                                    });
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
                        } // ENDS datasnapshot exists
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



            }
        };

        request_list.setAdapter(firebaseRecyclerAdapter);
    }





    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View view;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setUserName(String userName) {
            TextView Name = view.findViewById(R.id.r_profileName);
            Name.setText(userName);
        }
        public void setUserThumbPhoto(final String userThumbPhoto, final Context context) {
            final CircleImageView thumb_photo = view.findViewById(R.id.r_profileImage);
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

        public void setUserStatus(String user_status) {
            TextView Status = view.findViewById(R.id.r_profileStatus);
            Status.setText(user_status);
        }
    }

}
