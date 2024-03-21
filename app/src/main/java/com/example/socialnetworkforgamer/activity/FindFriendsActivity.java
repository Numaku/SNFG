package com.example.socialnetworkforgamer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.base.BaseActivity;
import com.example.socialnetworkforgamer.databinding.ActivityFindFriendsBinding;
import com.example.socialnetworkforgamer.model.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private ActivityFindFriendsBinding binding;
    private Toolbar mToolbar;
    private DatabaseReference allUserRef;
    private FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        allUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.searchList.setHasFixedSize(true);
        binding.searchList.setLayoutManager(new LinearLayoutManager(this));

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriend(query);
                return true; // indicate that the event has been handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as user types
                searchFriend(newText);
                return true; // indicate that the event has been handled
            }
        });
    }

    private void searchFriend(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            if (adapter != null) {
                adapter.stopListening();
            }
            binding.searchList.setAdapter(null);
            return;
        }
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(allUserRef.orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff"), FindFriends.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
                Glide.with(getApplicationContext()).load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.imageView);
                holder.userName.setText(String.valueOf(model.getUsername()));
                holder.status.setText(String.valueOf(model.getStatus()));
                //click vào xem profile người tìm kiếm
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id );
                        startActivity(profileIntent);
                    }
                });
            }
            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }

        };

        binding.searchList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, status;
        CircleImageView imageView;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.all_user_username);
            imageView = itemView.findViewById(R.id.all_user_image);
            status = itemView.findViewById(R.id.all_user_status);
        }

    }
}
