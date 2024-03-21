package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.databinding.ActivityResetPassBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassActivity extends AppCompatActivity {

    private ActivityResetPassBinding binding;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mToolbar = findViewById(R.id.forget_pass_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        mAuth = FirebaseAuth.getInstance();

        //xử lý nút send
        binding.restetSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = binding.resetPassEmail.getText().toString();
                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPassActivity.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                }
                else {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPassActivity.this, "Send successfully! Please check your Email Account...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassActivity.this, LoginActivity.class));
                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPassActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}