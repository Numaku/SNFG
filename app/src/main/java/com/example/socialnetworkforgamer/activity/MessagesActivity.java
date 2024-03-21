package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityFriendsBinding;
import com.example.socialnetworkforgamer.databinding.ActivityMessagesBinding;
import com.example.socialnetworkforgamer.model.Friends;
import com.example.socialnetworkforgamer.model.Messages;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private RecyclerView messageList;
    private DatabaseReference friendsRef, usersRef, messagesRef;
    private FirebaseAuth mAuth;
    private String onlineUserID;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageList = binding.allMessageLists;

        mToolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(onlineUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(linearLayoutManager);

        //Hiển thị danh sách bạn bè
        DisplayAllMessage();
    }

    private void DisplayAllMessage() {

        FirebaseRecyclerOptions<Messages> options =
                new FirebaseRecyclerOptions.Builder<Messages>()
                        .setQuery(messagesRef, Messages.class)
                        .build();
        FirebaseRecyclerAdapter<Messages, messageViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Messages, messageViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull messageViewHolder holder, int position, @NonNull Messages model) {


                        final String userID = getRef(position).getKey();


                        usersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    final String userName = snapshot.child("username").getValue().toString();
                                    final String profileImage = snapshot.child("profile image").getValue().toString();
                                    final String type;

                                    if(snapshot.hasChild("userstate")){
                                        type = snapshot.child("userstate").child("type").getValue().toString();
                                        if (type.equals("online")){
                                            holder.statusView.setImageResource(R.drawable.baseline_circle_online);
                                        }
                                        else {
                                            holder.statusView.setImageResource(R.drawable.baseline_circle_24);
                                        }
                                    }
                                    holder.setUsername(userName);
                                    holder.setProfileimage(profileImage, getApplicationContext());
                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(MessagesActivity.this, ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userID);
                                            chatIntent.putExtra("userName", userName);
                                            // chatIntent.putExtra("profileImage", profileImage);
                                            startActivity(chatIntent);
                                        }
                                    });


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                        return new messageViewHolder(view);
                    }
                };
        messageList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class messageViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView statusView;
        public messageViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            statusView = itemView.findViewById(R.id.all_user_state_icon);
        }
        public void setUsername(String username){
            TextView userName = mView.findViewById(R.id.all_user_username);
            userName.setText(username);
        }
        public void setProfileimage(String profileimage, Context context){
            CircleImageView profileImage = mView.findViewById(R.id.all_user_image);
            Glide.with(context).load(profileimage).into(profileImage);
        }
        public void setMessage(String message){
            TextView friendDate = mView.findViewById(R.id.all_user_status);
            friendDate.setText("Friend Since: " + message);
        }
    }


}