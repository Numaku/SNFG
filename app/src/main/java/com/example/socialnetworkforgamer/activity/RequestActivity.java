package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.databinding.ActivityFriendsBinding;
import com.example.socialnetworkforgamer.databinding.ActivityRequestBinding;
import com.example.socialnetworkforgamer.model.FriendRequests;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestActivity extends AppCompatActivity {

    private ActivityRequestBinding binding;
    private RecyclerView requestList;
    private DatabaseReference friendsRequestRef, usersRef;
    private FirebaseAuth mAuth;
    private String onlineUserID;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestList = binding.allRequestLists;

        mToolbar = findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        friendsRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(onlineUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        requestList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        requestList.setLayoutManager(linearLayoutManager);
        
        DisplayAllRequestList();

        binding.friendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestIntent = new Intent(RequestActivity.this, FriendsActivity.class);
                startActivity(requestIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

    }

    private void DisplayAllRequestList() {
        FirebaseRecyclerOptions<FriendRequests> options =
                new FirebaseRecyclerOptions.Builder<FriendRequests>()
                        .setQuery(friendsRequestRef, FriendRequests.class)
                        .build();

        FirebaseRecyclerAdapter<FriendRequests, friendRequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FriendRequests, friendRequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull friendRequestViewHolder holder, int position, @NonNull FriendRequests model) {
                        final String userID = getRef(position).getKey();
                        holder.setRequesttype(model.getRequesttype());

                        usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    final String userName = snapshot.child("username").getValue().toString();
                                    final String profileImage = snapshot.child("profile image").getValue().toString();

                                    holder.setUsername(userName);
                                    holder.setProfileimage(profileImage, getApplicationContext());
                                }
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent profileIntent = new Intent(RequestActivity.this, PersonProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", userID);
                                        startActivity(profileIntent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public friendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                        return new friendRequestViewHolder(view);
                    }
                };
        requestList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendRequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public friendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String username){
            TextView userName = mView.findViewById(R.id.all_user_username);
            userName.setText(username);
        }
        public void setProfileimage(String profileimage, Context context){
            CircleImageView profileImage = mView.findViewById(R.id.all_user_image);
            Glide.with(context).load(profileimage).into(profileImage);
        }
        public void setRequesttype(String requesttype){
            TextView friendDate = mView.findViewById(R.id.all_user_status);
            friendDate.setText("You was " + requesttype +" an request");
        }
    }
}