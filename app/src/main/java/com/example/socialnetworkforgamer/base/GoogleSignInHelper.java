package com.example.socialnetworkforgamer.base;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;

public class GoogleSignInHelper {
    FirebaseAuth mAuth;
    Context mContext;

    public GoogleSignInHelper(Context context){
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
    }



}
