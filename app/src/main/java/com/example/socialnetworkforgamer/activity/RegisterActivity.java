package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button RegisterButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private TextView haveAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        UserConfirmPassword = findViewById(R.id.register_confirm_password);
        RegisterButton = findViewById(R.id.register_button);
        loadingBar = new ProgressDialog(this);
        haveAcc = findViewById(R.id.have_account);

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

        haveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

    }

    //kiểm tra nếu người dùng nhập các thông tin căn bản
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        //loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }


    //xử lý tạo account
    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter your email!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter your password!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Confirm your password!", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Password don't match with confirm password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToSetUpActivity();

                                Toast.makeText(RegisterActivity.this, "Account was created!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToSetUpActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}