package com.example.socialnetworkforgamer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.databinding.ActivityOptionBinding;

public class OptionActivity extends AppCompatActivity {

    private ActivityOptionBinding binding;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mToolbar = findViewById(R.id.option_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.changePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToChangePass();
            }
        });

        binding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditProfile();
            }
        });
    }

    private void SendUserToEditProfile() {
        Intent editIntent = new Intent(OptionActivity.this, SettingActivity.class);
        startActivity(editIntent);
    }

    private void SendUserToChangePass() {
        Intent changeIntent = new Intent(OptionActivity.this, ResetPassActivity.class);
        startActivity(changeIntent);
    }
}