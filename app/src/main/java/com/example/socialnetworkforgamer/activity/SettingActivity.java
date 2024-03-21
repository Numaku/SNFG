package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityClickPostBinding;
import com.example.socialnetworkforgamer.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    private Toolbar mToolbar;
    private DatabaseReference settingUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String myProfileImage, myUserName, myFullName, myStatus, myCountry, myDoB, myGender;
    final static int Gallery_pick = 1;
    private Uri imageUri;
    private StorageReference UserProfileImageRef;


    ArrayAdapter<String> adapterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile image");


        mToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).into(binding.settingProfileImage);
                    binding.settingUsername.setText(myUserName);
                    binding.settingFullname.setText(myFullName);
                    binding.settingStatus.setText(myStatus);
                    binding.settingDob.setText(myDoB);
                    binding.settingCountry.setText(myCountry);
                    binding.settingGender.setText(myGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.settingAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccInfo();
            }
        });



        binding.settingProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null){

            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.settingProfileImage.setImageBitmap(bitmap);

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(SetupActivity.this, "Store profile image successful", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    settingUserRef.child("profile image").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        updatePostImage();
                                                        Toast.makeText(SettingActivity.this, "Updated Profile Image", Toast.LENGTH_SHORT).show();
                                                        //loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingActivity.this, message, Toast.LENGTH_SHORT).show();
                                                        //loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý khi không thể lấy URL của hình ảnh từ Storage
                                    Toast.makeText(SettingActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SettingActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void updatePostImage() {
        //Bai 33
    }

    private void ValidateAccInfo() {
        String username = binding.settingUsername.getText().toString();
        String fullname = binding.settingFullname.getText().toString();
        String status = binding.settingStatus.getText().toString();
        String country = binding.settingCountry.getText().toString();
        String gender = binding.settingGender.getText().toString();
        String dob = binding.settingDob.getText().toString();


        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter your username...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please enter your fullname...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please enter your status...", Toast.LENGTH_SHORT).show();
        }
        else {
            UpdateAccInfo(username, fullname, status, country, dob, gender);
        }
    }

    private void UpdateAccInfo(String username, String fullname, String status, String country, String dob, String gender) {
        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("status", status);
            userMap.put("country", country);
            //userMap.put("gender", gender);
            userMap.put("dob", dob);
        settingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    //SendUserToMainActivity();
                    Toast.makeText(SettingActivity.this, "Account information was updated!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SettingActivity.this, "Error! Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent settingIntent = new Intent(SettingActivity.this, SettingActivity.class);
        //settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }
}