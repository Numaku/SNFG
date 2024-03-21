package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private CircleImageView navProfileImage;
    private TextView navUsername;
    private ImageButton addNewPostButton;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    String currentUserID;
    boolean likeCheck = false;
//    private FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Phan quyen
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        //Toolbar
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        addNewPostButton = findViewById(R.id.add_new_post_button);

        //cai dat giao dien
        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_closer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation_view);

        postList = findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navView.findViewById(R.id.nav_profile_image);
        navUsername = navView.findViewById(R.id.nav_user_full_name);

        //hiển thị username, avatar
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("username")){
                        String username = snapshot.child("username").getValue().toString();
                        navUsername.setText(username);
                    }

                    if(snapshot.hasChild("profile image")){
                        String image = snapshot.child("profile image").getValue().toString();
                        Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                    else {

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //chọn tính năng trên navigation bar
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        //thêm bài viết
        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        //hiển thị bài viết
        DisplayAllUserPosts();
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
            currentStateMap.put("time", saveCurrentTime);
            currentStateMap.put("date", saveCurrentDate);
            currentStateMap.put("type", state);

        UsersRef.child(currentUserID).child("userstate")
                .updateChildren(currentStateMap);
    }


    //hiển thị posts của người dùng
    private void DisplayAllUserPosts() {


        Query sortPostsDescendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(sortPostsDescendingOrder, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model) {
//
                        final String postKey = getRef(position).getKey();

                        holder.username.setText(String.valueOf(model.getUsername()));
                        holder.postDate.setText(String.valueOf(model.getDate()));
                        holder.postDescription.setText(String.valueOf(model.getDescription()));
                        holder.postTime.setText(String.valueOf(model.getTime()));
                        Glide.with(getApplicationContext()).load(model.getProfileimage()).into(holder.profileImage);
                        Glide.with(getApplicationContext()).load(model.getPostimage()).into(holder.postImage);

                        holder.setLikeButtonStatus(postKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.commentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                commentIntent.putExtra("PostKey", postKey);
                                startActivity(commentIntent);
                            }
                        });

                        holder.likeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likeCheck = true;

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(likeCheck == true){
                                            if (snapshot.child(postKey).hasChild(currentUserID)){
                                                LikesRef.child(postKey).child(currentUserID).removeValue();
                                                likeCheck = false;
                                            }
                                            else {
                                                LikesRef.child(postKey).child(currentUserID).setValue(true);
                                                likeCheck = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_posts_layout, parent, false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }
                };
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }



    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView username,postDate,postTime,postDescription,likeCount;
        CircleImageView profileImage;
        ImageView postImage;
        ImageButton likeButton, commentButton;
        int countLikes;
        String currentUserID;
        DatabaseReference likeRef;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.post_profile_username);
            postDate = itemView.findViewById(R.id.post_date);
            postTime = itemView.findViewById(R.id.post_time);
            postDescription = itemView.findViewById(R.id.post_desc);
            profileImage = itemView.findViewById(R.id.post_profile_image);
            postImage = itemView.findViewById(R.id.post_image);
            likeCount = itemView.findViewById(R.id.like_count);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);

            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String postKey){
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(currentUserID)){
                        countLikes = (int) snapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource(R.drawable.baseline_favorite_24);
                        if(countLikes >1){
                            likeCount.setText((countLikes)+ " likes");
                        }
                        else {
                            likeCount.setText((countLikes)+" like");
                        }

                    }
                    else {
                        countLikes = (int) snapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                        if(countLikes >1){
                            likeCount.setText((countLikes)+ " likes");
                        }
                        else {
                            likeCount.setText((countLikes)+" like");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //nếu người dùng đã login thành công thì start từ MainActivity
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }else {
            Log.d("MainActivity", "User is not null, checking existence...");
            CheckUserExistence();
        }

        if (!isNetworkAvailable()) {
            toggleProgressBarVisibility(true);
            Toast.makeText(this, "Please check your internet connection...", Toast.LENGTH_LONG).show();
        } else {
            DisplayAllUserPosts();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    //kiểm tra
    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(current_user_id)){
                    SendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Phương thức để hiển thị hoặc ẩn ProgressBar
    private void toggleProgressBarVisibility(boolean show) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    //điều huớng
    private void SendUserToSetUpActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingActivity(){
        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(settingIntent);
    }

    private void SendUserToFriendsActivity(){
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToProfileActivity(){
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void SendUserToFindFriendsActivity(){
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void SendUserToMessagesActivity(){
        Intent messgesIntent = new Intent(MainActivity.this, MessagesActivity.class);
        startActivity(messgesIntent);
    }

    private void SendUserToRequestActivity(){
        Intent requestIntent = new Intent(MainActivity.this, RequestActivity.class);
        startActivity(requestIntent);
    }


    //chon chuc nang cua app
    private void UserMenuSelector(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.nav_post){
            SendUserToPostActivity();
        }
        else if (itemId == R.id.nav_profile){
            SendUserToProfileActivity();
        }
        else if (itemId == R.id.nav_home){
            //SendUserToRequestActivity();
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        }
        else if (itemId == R.id.nav_friends){
            SendUserToFriendsActivity();
        }
        else if (itemId == R.id.nav_find_friend){
            SendUserToFindFriendsActivity();
        }
        else if (itemId == R.id.nav_messages){
           SendUserToMessagesActivity();
        }
        else if (itemId == R.id.nav_settings){
            SendUserToSettingActivity();
        }
        else if (itemId == R.id.nav_logout){
            mAuth.signOut();
            updateUserStatus("offline");
            SendUserToLoginActivity();
        }
    }
}