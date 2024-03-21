package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.adapter.MessagesAdapter;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityChatBinding;
import com.example.socialnetworkforgamer.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity {

    private Toolbar mToolbar;
    private RecyclerView userMessageLists;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private ActivityChatBinding binding;

    private String messageReceiverID, messageReceiverName, messageSenderID;
    private String saveCurrentDate, saveCurrentTime;
    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().getString("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().getString("userName").toString();
        //messageReceiverImage = getIntent().getExtras().getString("profileImage").toString();


        CustomActionBar();


        DisplayReceiverInfo();

        //click vào nút send
        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hàm xử lý nút send
                SendMessage();
            }
        });

        FetchMessages();

    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()){
                            Messages messages = snapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessage() {
        String messageText = binding.inputMessage.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please write something to your friend!", Toast.LENGTH_SHORT).show();
        }
        else {
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody);
                messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageTextBody);

                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            //Toast.makeText(ChatActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            binding.inputMessage.setText("");
                            hideKeyBoard();
                        }
                        else {
                            String message = task.getException().getMessage();
                            Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            binding.inputMessage.setText("");
                        }
                    }

                });
            rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Di chuyển RecyclerView đến vị trí cuối cùng
                        userMessageLists.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý khi có lỗi
                }
            });
        }
    }

    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void DisplayReceiverInfo() {
        receiverName.setText(String.valueOf(messageReceiverName));
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String type = snapshot.child("userstate").child("type").getValue().toString();
                    final String profileImage = snapshot.child("profile image").getValue().toString();
                    final String lastdate = snapshot.child("userstate").child("date").getValue().toString();
                    final String lasttime = snapshot.child("userstate").child("time").getValue().toString();

                    if(type.equals("online")){
                        userLastSeen.setText("online");
                        userLastSeen.setTextColor(getResources().getColor(R.color.online));
                    }
                    else {
                        userLastSeen.setText("last seen: "+ lasttime + "  " + lastdate);
                        userLastSeen.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    Glide.with(getApplicationContext()).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CustomActionBar() {

        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.custom_profile_image);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageLists = binding.messageUserLists;
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageLists.setHasFixedSize(true);
        userMessageLists.setLayoutManager(linearLayoutManager);
        userMessageLists.setAdapter(messagesAdapter);

        //userMessageLists.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
    }

}