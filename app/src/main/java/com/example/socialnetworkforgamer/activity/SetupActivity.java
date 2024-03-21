package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, Country;
    private Button SaveButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private Uri imageUri;

    String currentUserId;
    final static int Gallery_pick = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);



        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile image");

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        Country = findViewById(R.id.setup_country);
        SaveButton = findViewById(R.id.setup_button);
        ProfileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        //lưu thông tin cơ bản người dùng nhập
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        //tải avatar từ máy người dùng
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("profile image")){
                        String image = snapshot.child("profile image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else{

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //lấy ảnh từ thư viện của máy
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null){

//            loadingBar.setTitle("Profile Image");
//            loadingBar.setMessage("Please wait...");
//            loadingBar.show();
//            loadingBar.setCanceledOnTouchOutside(true);

            imageUri = data.getData();
            // Set the selected image to the CircleImageView
            //ProfileImage.setImageURI(imageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ProfileImage.setImageBitmap(bitmap);

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
                                    UsersRef.child("profile image").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(SetupActivity.this, "Updated Profile Image", Toast.LENGTH_SHORT).show();
                                                        //loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                                                        //loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý khi không thể lấy URL của hình ảnh từ Storage
                                    Toast.makeText(SetupActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });


            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //hàm xử lý lưu thông tin cơ bản
    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = Country.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please enter fullname", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please enter country", Toast.LENGTH_SHORT).show();
        }
        else if(imageUri == null){
            Toast.makeText(this, "Please choose your avatar", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "Hello, nice to meet you!");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationship", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
        SendUserToLoginActivity();
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SetupActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}