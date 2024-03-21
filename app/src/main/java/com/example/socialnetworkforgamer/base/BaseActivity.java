package com.example.socialnetworkforgamer.base;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    private DatabaseReference UsersRef;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private static int activeActivitiesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isFinishing()) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    protected void updateUserStatus(String status) {
        // Thực hiện mã để cập nhật trạng thái người dùng ở đây.
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map<String, Object> currentStateMap = new HashMap<>();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", status);

        UsersRef.child(currentUserID).child("userstate")
                .updateChildren(currentStateMap);
    }
}
