package com.example.socialnetworkforgamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialnetworkforgamer.R;
import com.example.socialnetworkforgamer.activity.MainActivity;
import com.example.socialnetworkforgamer.model.Posts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {
    private List<Posts> postsList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    Context context;

    public PostsAdapter(Context context, List<Posts> postsList) {
        this.postsList = postsList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_posts_layout, parent, false);
        PostsViewHolder viewHolder = new PostsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
        Posts posts = postsList.get(position);
        if (posts == null){
            return;
        }
        holder.username.setText(String.valueOf(posts.getUsername()));
        holder.postDate.setText(String.valueOf(posts.getDate()));
        holder.postDescription.setText(String.valueOf(posts.getDescription()));
        holder.postTime.setText(String.valueOf(posts.getTime()));
        Picasso.get().load(posts.getProfileimage()).into(holder.profileImage);
        Picasso.get().load(posts.getPostimage()).into(holder.postImage);
        holder.hashtag.setText(String.valueOf(posts.getHashtag()));
    }

    @Override
    public int getItemCount() {
        if (postsList != null){
            return postsList.size();
        }
        return 0;
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder {

        TextView username,postDate,postTime,postDescription,likeCount,hashtag;
        CircleImageView profileImage;
        ImageView postImage;
        ImageButton likeButton, commentButton;
        int countLikes;
        String currentUserID;
        DatabaseReference likeRef;
        public PostsViewHolder(@NonNull View itemView) {
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
            hashtag = itemView.findViewById(R.id.hashtag);


            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }
}
