package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityPersonProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {

    private ActivityPersonProfileBinding binding;
    private DatabaseReference friendRequestRef, userRef, friendsRef;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID, CURRENT_STATE, saveCurrentDate;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mToolbar = findViewById(R.id.person_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Person Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        IntializeFields();

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String myProfileImage = snapshot.child("profile image").getValue().toString();
                    String myUserName = snapshot.child("username").getValue().toString();
                    String myFullName = snapshot.child("fullname").getValue().toString();
                    String myStatus = snapshot.child("status").getValue().toString();
                    String myDoB = snapshot.child("dob").getValue().toString();
                    String myCountry = snapshot.child("country").getValue().toString();
                    String myGender = snapshot.child("gender").getValue().toString();

                    Glide.with(getApplicationContext()).load(myProfileImage).placeholder(R.drawable.profile).into(binding.personProfileImage);
                    binding.personProfileUsername.setText("@" + myUserName);
                    binding.personProfileFullname.setText(myFullName);
                    binding.personProfileStatus.setText(myStatus);
                    binding.personProfileDob.setText("DOB: " + myDoB);
                    binding.personProfileCountry.setText("Country: "+ myCountry);
                    binding.personProfileGender.setText("Gender: " + myGender);

                    MaintenanceOfButton();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
        binding.personDeclineFriendRequest.setEnabled(false);

        if(!senderUserID.equals(receiverUserID)){
            binding.personSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.personSendFriendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friend")){
                        SendFriendRequest(); //xử lý gửi lời mời kết bạn
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest(); //xử lý hủy lời mời kết bạn
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest(); //xử lý đồng ý kết bạn
                    }
                    if(CURRENT_STATE.equals("friend")){
                        UnfriendFriend(); //xử lý hủy kết bạn
                    }
                }
            });
        }
        else {
            binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
            binding.personSendFriendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void UnfriendFriend() {
        friendsRef.child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                binding.personSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                binding.personSendFriendRequest.setText("Add Friend");

                                                binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                binding.personDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        friendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                friendRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        binding.personSendFriendRequest.setEnabled(true);
                                                                                        CURRENT_STATE = "friend";
                                                                                        binding.personSendFriendRequest.setText("Unfriend");

                                                                                        binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                        binding.personDeclineFriendRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                binding.personSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                binding.personSendFriendRequest.setText("Add Friend");

                                                binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                binding.personDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintenanceOfButton() {
        friendRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserID)){
                    String request_type = snapshot.child(receiverUserID).child("requesttype").getValue().toString();

                    if (request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        binding.personSendFriendRequest.setText("Cancel Friend Request");

                        binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                        binding.personDeclineFriendRequest.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        CURRENT_STATE = "request_received";
                        binding.personSendFriendRequest.setText("Accept Friend");

                        binding.personDeclineFriendRequest.setVisibility(View.VISIBLE);
                        binding.personDeclineFriendRequest.setEnabled(true);

                        binding.personDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                else {
                    friendsRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserID)){
                                CURRENT_STATE = "friend";
                                binding.personSendFriendRequest.setText("Unfriend");

                                binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                binding.personDeclineFriendRequest.setEnabled(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendFriendRequest(){
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .child("requesttype").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("requesttype").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                binding.personSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                binding.personSendFriendRequest.setText("Cancel Friend Request");
                                                binding.personDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                binding.personDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void IntializeFields() {
        CURRENT_STATE = "not_friend";
    }

}