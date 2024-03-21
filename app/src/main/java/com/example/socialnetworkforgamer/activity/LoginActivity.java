package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetworkforgamer.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private ImageView googleSigninButton;
    private EditText userEmail, userPassword;
    private TextView newAccount, fogetPass;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private Boolean emailCheck;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        newAccount =findViewById(R.id.register_acc_link);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        googleSigninButton = findViewById(R.id.google_signin_button);
        fogetPass = findViewById(R.id.forget_pass_link);
        loadingBar = new ProgressDialog(this);

        newAccount.setPaintFlags(newAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //sang trang tạo mới account
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });
        
        //quên password
        fogetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPassActivity.class));
            }
        });

        //login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
    }

//    private void signIn(){
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN){
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//            }
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    //check tk, mk
    private void AllowUserToLogin(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Logging");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                SendUserToMainActivity();
                                //SendUserToTestActivity();
                                Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                Toast.makeText(LoginActivity.this, "Account or password are wrong!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

//    private void SendUserToTestActivity() {
//        Intent mainIntent = new Intent(LoginActivity.this, testActivity.class);
//        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(mainIntent);
//        finish();
//    }

//    private void VerifyEmailAddress(){
//        FirebaseUser user = mAuth.getCurrentUser();
//        emailCheck = user.isEmailVerified();
//
//        if(emailCheck){
//
//        }
//        else {
//            Toast.makeText(this, "Please verify your email!", Toast.LENGTH_SHORT).show();
//
//        }
//    }

    //điều hướng
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void goToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        //finish();
    }
}