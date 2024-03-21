package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityProfileBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseReference profileUserRef, friendsRef, likesRef, postsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String myProfileImage, myUserName, myFullName, myStatus, myCountry, myDoB, myGender, postID;
    private Toolbar mToolbar;
    private int countFriends = 0, countLiked = 0, countPosts = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mToolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");




        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    countFriends = (int) snapshot.getChildrenCount();
                    binding.friendCount.setText(Integer.toString(countFriends));
                }
                else {
                    binding.friendCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    countPosts = (int) snapshot.getChildrenCount();
                                    postID = snapshot.getChildren().toString();
                                    binding.postsCount.setText(Integer.toString(countPosts));

                                }
                                else {
                                    binding.postsCount.setText("error");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        //hiển thị thông tin người dùng
        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    myProfileImage = snapshot.child("profile image").getValue().toString();
                    myUserName = snapshot.child("username").getValue().toString();
                    myFullName = snapshot.child("fullname").getValue().toString();
                    myStatus = snapshot.child("status").getValue().toString();
                    myDoB = snapshot.child("dob").getValue().toString();
                    myCountry = snapshot.child("country").getValue().toString();
                    myGender = snapshot.child("gender").getValue().toString();

                    Glide.with(getApplicationContext()).load(myProfileImage).placeholder(R.drawable.profile).into(binding.myProfileImage);
                    binding.myProfileUsername.setText("@" + myUserName);
                    binding.myProfileFullname.setText(myFullName);
                    binding.myProfileStatus.setText(myStatus);
                    binding.myProfileDob.setText("DOB: " + myDoB);
                    binding.myProfileCountry.setText("Country: "+ myCountry);
                    binding.myProfileGender.setText("Gender: " + myGender);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countLiked = 0;
                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : likeSnapshot.getChildren()) { // Thay vì snapshot, bạn nên sử dụng likeSnapshot ở đây
                        Boolean value = childSnapshot.getValue(Boolean.class); // Sử dụng Boolean.class thay vì boolean để tránh lỗi
                        if (value != null && value) { // Kiểm tra giá trị có null không trước khi kiểm tra giá trị true
                            countLiked++;
                        }
                    }
                }
                binding.likedCount.setText(String.valueOf(countLiked)); // Chuyển đổi countLiked thành chuỗi trước khi hiển thị
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //vào phần edit
        binding.profileEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingActivity();
            }
        });

    }

    private void SendUserToMyProfileActivity() {
    }

    private void SendUserToSettingActivity() {
        Intent settingIntent = new Intent(ProfileActivity.this, SettingActivity.class);
        //settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }
}