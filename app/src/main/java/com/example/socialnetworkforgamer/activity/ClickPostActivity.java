package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityClickPostBinding;
import com.example.socialnetworkforgamer.databinding.ActivityMainBinding;
import com.example.socialnetworkforgamer.model.Comments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClickPostActivity extends AppCompatActivity {

    private ActivityClickPostBinding binding;
    private String postKey, currentUserID, databaseUserID, commentKey;
    private String description, image, profileimage, date, time, username, like, hashtag;
    private DatabaseReference clickPostRef, userRef, postRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClickPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mToolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        binding.commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.commentList.setLayoutManager(linearLayoutManager);

        postKey = getIntent().getExtras().get("PostKey").toString();
        commentKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    description = snapshot.child("description").getValue().toString();
                    image = snapshot.child("postimage").getValue().toString();
                    profileimage = snapshot.child("profileimage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    date = snapshot.child("date").getValue().toString();
                    time = snapshot.child("time").getValue().toString();
                    databaseUserID = snapshot.child("uid").getValue().toString();
                    hashtag = snapshot.child("hashtag").getValue().toString();

                    binding.clickPostDesc.setText(String.valueOf(description));
                    binding.clickPostProfileUsername.setText(username);
                    Glide.with(getApplicationContext()).load(image).into(binding.clickPostImage);
                    Glide.with(getApplicationContext()).load(profileimage).into(binding.clickPostProfileImage);
                    binding.clickPostDate.setText(date);
                    binding.clickPostTime.setText(time);
                    binding.clickPostHashtag.setText(hashtag);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //lựa chon edit or delete
        binding.option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpOption();
            }
        });

        //gửi comment
        binding.postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            username = snapshot.child("username").getValue().toString();
                            profileimage = snapshot.child("profile image").getValue().toString();
                            validateComment(username, profileimage);

                            binding.commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter<Comments, CommentViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comments model) {
                        holder.userName.setText(String.valueOf(model.getUsername()));
                        holder.commentTime.setText(String.valueOf(model.getTime()));
                        holder.commentDate.setText(String.valueOf(model.getDate()));
                        holder.comment.setText(String.valueOf(model.getComment()));
                        if (!isFinishing()) {
                            Glide.with(getApplicationContext()).load(image).into(binding.clickPostImage);
                            Glide.with(getApplicationContext()).load(model.getProfileimage()).into(holder.profileImage);
                        }



                    }

                    @NonNull
                    @Override
                    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                        return new CommentViewHolder(view);
                    }
                };

        // Set adapter to your RecyclerView
        binding.commentList.setAdapter(firebaseRecyclerAdapter);

        // Start listening for data changes
        firebaseRecyclerAdapter.startListening();
    }


    public static class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView userName, commentDate, commentTime, comment;
        CircleImageView profileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.comment_username);
            commentDate = itemView.findViewById(R.id.comment_date);
            commentTime = itemView.findViewById(R.id.comment_time);
            comment = itemView.findViewById(R.id.comment_comment);
            profileImage = itemView.findViewById(R.id.comment_profile);

        }
    }
    private void validateComment(String username, String profileimage){
        String commentText = binding.commentInput.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            // Hiển thị toast chỉ khi trường nhập liệu trống
            Toast.makeText(this, "Please write your comment!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Xử lý gửi bình luận nếu trường nhập liệu không trống
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            Random random = new Random();
            char randomChar = (char) ('a' + random.nextInt(26));

            final String randomKey = currentUserID + saveCurrentTime + saveCurrentTime + randomChar + randomChar;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", currentUserID);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", username);
            commentMap.put("profileimage", profileimage);

            postRef.child(randomKey).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        //Toast.makeText(ClickPostActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ClickPostActivity.this, "Error! Please try again later!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void popUpOption() {

        PopupMenu popupMenu = new PopupMenu(this, binding.option);
        popupMenu.inflate(R.menu.pop_up_option);

        // Định nghĩa trình nghe sự kiện để xử lý khi một mục được chọn trong PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Lấy ID của mục được chọn
                int id = item.getItemId();

//                if (!currentUserID.equals(databaseUserID)) {
//                    Toast.makeText(ClickPostActivity.this, "You are not authorized to edit or delete this post", Toast.LENGTH_SHORT).show();
//                    return false;
//                }
                // Xử lý tương ứng với ID của mục được chọn
                if (id == R.id.delete_post) {
                    showDeleteConfirmationDialog();
                    return true;
                } else if (id == R.id.edit_post) {
                    editCurrentPost();
                }
                // Trả về false nếu không có sự kiện nào được xử lý
                return false;
            }
        });

            if (!currentUserID.equals(databaseUserID)) {
                Menu menu = popupMenu.getMenu();
                menu.findItem(R.id.delete_post).setEnabled(false);
                menu.findItem(R.id.edit_post).setEnabled(false);
            }
        // Hiển thị PopupMenu
        popupMenu.show();
    }
    private void editCurrentPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        //dialog.getWindow().setBackgroundDrawableResource(getResources().getColor(R.c));
        dialog.setCanceledOnTouchOutside(false);
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this post?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Thực hiện xóa bài viết nếu người dùng chọn "Yes"
                        DeleteCurrentPost();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Đóng hộp thoại nếu người dùng chọn "No"
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void DeleteCurrentPost() {
        clickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}