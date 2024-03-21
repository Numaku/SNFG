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
import com.example.socialnetworkforgamer.model.FindFriends;
import com.example.socialnetworkforgamer.model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private ActivityFriendsBinding binding;
    private RecyclerView friendList;
    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String onlineUserID;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        friendList = binding.allFriendLists;

        mToolbar = findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendList.setLayoutManager(linearLayoutManager);

        //Hiển thị danh sách bạn bè
        DisplayAllFriends();

        binding.requestText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent requestIntent = new Intent(FriendsActivity.this, RequestActivity.class);
                    startActivity(requestIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd, MM, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        usersRef.child(onlineUserID).child("userstate")
                .updateChildren(currentStateMap);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateUserStatus("online");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        updateUserStatus("offline");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        updateUserStatus("offline");
//    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsRef, Friends.class)
                        .build();
        FirebaseRecyclerAdapter<Friends, friendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull friendsViewHolder holder, int position, @NonNull Friends model) {
                        holder.setDate(model.getDate());

                        final String userID = getRef(position).getKey();

                        usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
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

                                    //lựa chọn option với friend
                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CharSequence options[] = new CharSequence[]{
                                                userName + "'s Profile", "Send Message"
                                            };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this, androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog);
                                            builder.setTitle("Select Option");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if(which == 0){
                                                        Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                        profileIntent.putExtra("visit_user_id", userID);
                                                        startActivity(profileIntent);
                                                    }
                                                    if (which == 1){
                                                        Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id", userID);
                                                        chatIntent.putExtra("userName", userName);
                                                       // chatIntent.putExtra("profileImage", profileImage);
                                                        startActivity(chatIntent);
                                                    }
                                                }
                                            });
                                            builder.show();
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
                    public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                        return new friendsViewHolder(view);
                    }
                };
        friendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView statusView;
        public friendsViewHolder(@NonNull View itemView) {
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
        public void setDate(String date){
            TextView friendDate = mView.findViewById(R.id.all_user_status);
            friendDate.setText("Friend Since: " + date);
        }
    }
}