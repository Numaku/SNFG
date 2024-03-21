package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton selectPostImage;
    private Button postButton;
    private EditText postDecsription;
    private static final int Gallery_pick = 1;
    private Uri ImageUri;
    private String description;
    private StorageReference postImageRef;
    private DatabaseReference userRef, postRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserID;
    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        selectPostImage = findViewById(R.id.select_post_image);
        postButton = findViewById(R.id.post_button);
        postDecsription = findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        postImageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePost();
            }
        });
    }

    private void validatePost() {
        description = postDecsription.getText().toString();
        if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please write something and retry!", Toast.LENGTH_SHORT).show();
        }
        else if(ImageUri == null){
            Toast.makeText(this, "Add image pls", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Posting");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToStorge();
        }
    }

    private void StoringImageToStorge() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Random random = new Random();
        char randomChar = (char) ('a' + random.nextInt(26));

        postRandomName = saveCurrentDate + saveCurrentTime + randomChar;

        StorageReference filePath = postImageRef.child("Post Image").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    // Lấy đường dẫn URL của hình ảnh từ Firebase Storage
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            //Toast.makeText(PostActivity.this, "Upload image success", Toast.LENGTH_SHORT).show();
                            SavingPostToDatabase();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void SavingPostToDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countPosts = snapshot.getChildrenCount();
                }
                else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName = snapshot.child("username").getValue().toString();
                    String userProfileImage = snapshot.child("profile image").getValue().toString();

                    HashMap<String, Object> postMap = new HashMap<>();
                        postMap.put("uid", currentUserID);
                        postMap.put("date", saveCurrentDate);
                        postMap.put("time", saveCurrentTime);
                        postMap.put("description", description);
                        postMap.put("postimage", downloadUrl);
                        postMap.put("profileimage", userProfileImage);
                        postMap.put("username", userName);
                        postMap.put("counter", countPosts);
                    postRef.child(currentUserID + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "Posting Successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                Toast.makeText(PostActivity.this, "Error, Please try again!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            selectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);

    }
}