package com.izqalan.messenger;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView friendList;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference friendDatabase;
    private DatabaseReference userDatabase;
    private FirebaseAuth mAuth;

    private View mainView;

    private String currentUserId;


    public FriendsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // must return which layout to inflate
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);



        friendList =  mainView.findViewById(R.id.friends_list);

        /*
        * show friends list based on individual user friends list
        * */
        final String uid = getActivity().getIntent().getStringExtra("user_id");
        currentUserId = uid;
        if(currentUserId == null){
            currentUserId = mAuth.getInstance().getCurrentUser().getUid();
        }

        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        friendDatabase.keepSynced(true);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        // RecyclerView Settings
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter();

        return mainView;

    }

    public void bindAdapter(){
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendDatabase, Friends.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                final String user_id = getRef(position).getKey();

                userDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String username = dataSnapshot.child("name").getValue().toString();
                        final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        String bio = dataSnapshot.child("bio").getValue().toString();



                        holder.setName(username);
                        holder.setBio(bio);
                        holder.setThumbImage(thumbImage);

                        holder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final CharSequence option[] = new CharSequence[]{"Open profile", "Send message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // check array of option[]
                                        if (i == 0){

                                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                                            intent.putExtra("user_id", user_id);
                                            startActivity(intent);

                                        }
                                        if(i == 1){

                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("user_id", user_id);
                                            intent.putExtra("name", username);
                                            intent.putExtra("avatar", thumbImage);
                                            startActivity(intent);


                                        }
                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_list_layout, viewGroup, false);

                return new FriendsViewHolder(view);

            }
        };

        // this line of code will build the view
        friendList.setAdapter(adapter);

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

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View v;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setName(String name){
            TextView usernameView = v.findViewById(R.id.username);
            usernameView.setText(name);
        }

        public void setBio(String bio){
            TextView bioView = v.findViewById(R.id.user_bio);
            bioView.setText(bio);
        }

        public  void setThumbImage(final String thumb_image){
            final CircleImageView user_avatar = v.findViewById(R.id.user_avatar);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(user_avatar, new Callback() {
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
                            .into(user_avatar);

                }
            });


        }
    }
}
