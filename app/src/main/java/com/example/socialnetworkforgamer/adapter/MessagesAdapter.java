package com.example.socialnetworkforgamer.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter (List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        // Set lại dữ liệu cho các view holder
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.INVISIBLE);

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String image = snapshot.child("profile image").getValue(String.class);
                        // Lấy activity từ context
                        Activity activity = (Activity) holder.receiverProfileImage.getContext();
                        if (!activity.isDestroyed() && !activity.isFinishing()) {
                            Glide.with(activity)
                                    .load(image)
                                    .placeholder(R.drawable.profile)
                                    .into(holder.receiverProfileImage);
                        }
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi xảy ra trong database
            }

        });

        if (fromMessageType.equals("text")){
            if (fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setGravity(Gravity.LEFT);
                holder.senderMessageText.setText(messages.getMessage());
            }
            else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setGravity(Gravity.LEFT);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}