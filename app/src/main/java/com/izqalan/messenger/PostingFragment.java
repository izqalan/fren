package com.izqalan.messenger;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostingFragment extends Fragment {

    private FirebaseAuth mAuth;

    private View mainView;
    private RecyclerView postList;
    private CardView postCard;

    private FirebaseRecyclerAdapter adapter;

    private String currentUserId;

    private DatabaseReference postDatabase;

    private String TAG = "PostingFragment: ";


    public PostingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_posting, container, false);

        postList = mainView.findViewById(R.id.post_list);
        postCard = mainView.findViewById(R.id.post_card);

        // passed from mainActivity
        final String uid = getActivity().getIntent().getStringExtra("user_id");
        currentUserId = uid;
        if(currentUserId == null){
            currentUserId = mAuth.getInstance().getCurrentUser().getUid();
        }

        postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        postList.setHasFixedSize(true);
        postList.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter();


        return  mainView;
    }

    private void bindAdapter() {

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postDatabase, Posts.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, final int position, @NonNull Posts model) {

                final String post_id = getRef(position).getKey();
                Log.d(TAG, "post_id: "+ post_id);

                postDatabase.child(post_id).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            final String foodName = dataSnapshot.child("foodname").getValue().toString();
                            final String image = dataSnapshot.child("image").getValue().toString();
                            final String date = dataSnapshot.child("date").getValue().toString();
                            final String time = dataSnapshot.child("time").getValue().toString();
                            final String owner = dataSnapshot.child("owner").getValue().toString();
                            final String address = dataSnapshot.child("address").getValue().toString();


                            Log.d(TAG, image);
                            holder.setFoodName(foodName);
                            holder.setImage(image);
                            holder.setDate(date);
                            holder.setTime(time);

                            if (address.length() > 50) {
                                String shortAddr = address.substring(0, 50) + "...";
                                holder.setAddress(shortAddr);
                            } else {
                                holder.setAddress(address);
                            }


                            holder.v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // go to new page that shows more detail about the event
                                    Intent intent = new Intent(getContext(), PostActivity.class);
                                    intent.putExtra("user_id", owner);
                                    intent.putExtra("uid", currentUserId);
                                    intent.putExtra("post_id", post_id);
                                    intent.putExtra("image", image);

                                    startActivity(intent);

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.posts_list_layout, viewGroup, false);

                return new PostViewHolder(v);

            }
        };

        postList.setAdapter(adapter);

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

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        View v;

        public void setImage(final String image){

            final ImageView foodImageView = v.findViewById(R.id.post_image);
            // picasso load here

            Picasso.get().load(image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(foodImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    // when image haven't store on disk, picasso look out for image.
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(foodImageView);

                }
            });

        }

        // load identifiers from db

        public void setFoodName(String foodName){
            TextView foodNameView = v.findViewById(R.id.food_name);
            foodNameView.setText(foodName);
        }

        public void setDate(String date){
            TextView foodDate = v.findViewById(R.id.food_date);
            foodDate.setText(date);
        }

        public void setTime(String time){
            TextView foodTime = v.findViewById(R.id.food_time);
            foodTime.setText(time);
        }

        public void setAddress(String address){
            TextView foodAddress = v.findViewById(R.id.address);
            foodAddress.setText(address);
        }

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
        }
    }

}
