package com.izqalan.messenger;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private RecyclerView avatarList;
    private DatabaseReference collabDatabase;
    private DatabaseReference postDatabase;
    private FirebaseRecyclerAdapter adapter;

    private ImageView postImage;
    private TextView postName;

    private static final String TAG = "PostActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final String postId = getIntent().getStringExtra("post_id");

        Log.d(TAG, "post_id:"+postId);

        collabDatabase = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).child("collab");
        postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
        postDatabase.keepSynced(true);
        collabDatabase.keepSynced(true);
        // make status bar transparent
        // get the current attributes of the current windows
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        // include desired attributes
        attr.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        // overwrite current attributes
        getWindow().setAttributes(attr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }

        postImage = findViewById(R.id.post_image);
        postName = findViewById(R.id.post_name);

        avatarList = findViewById(R.id.avatar_list);
        avatarList.setHasFixedSize(true);
        avatarList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        postDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String foodName = dataSnapshot.child("foodname").getValue().toString();
                String maxCollab = dataSnapshot.child("maxCollabNum").getValue().toString();
                final String thumb_image =  dataSnapshot.child("thumb_image").getValue().toString();

                postName.setText(foodName);
                Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(postImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        // when image haven't store on disk, picasso look out for image.
                        Picasso.get()
                                .load(thumb_image)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(postImage);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Log.d(TAG, "trace Start");
        bindAdapter();

    }

    private void bindAdapter() {

        Log.d(TAG, "trace1");

        FirebaseRecyclerOptions<Collab> options =
                new FirebaseRecyclerOptions.Builder<Collab>()
                        .setQuery(collabDatabase, Collab.class )
                        .build();

        Log.d(TAG, "trace2");
        adapter =  new FirebaseRecyclerAdapter<Collab, AvatarViewHolder >(options) {

            @Override
            protected void onBindViewHolder(@NonNull final AvatarViewHolder holder, int position, @NonNull final Collab model) {

                final String user_id = getRef(position).getKey();

                collabDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                            Log.d(TAG, "thumb_image: "+thumb_image);
                            holder.setThumbImage(thumb_image);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                // get user id from the list

                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // send value of id to another page
                        Intent intent = new Intent(PostActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", user_id );
                        startActivity(intent);

                    }
                });

                Log.d(TAG, "trace4");

            }

            @NonNull
            @Override
            public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                Log.d(TAG, "trace4");
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.avatar_list, viewGroup, false);
                return new AvatarViewHolder(view);
            }
        };

        Log.d(TAG, "trace5");
        avatarList.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class AvatarViewHolder extends RecyclerView.ViewHolder{

        View v;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
        }

        public  void setThumbImage(String thumb_image){
            CircleImageView user_avatar = v.findViewById(R.id.collab_avatar);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(user_avatar);

        }


    }
}
