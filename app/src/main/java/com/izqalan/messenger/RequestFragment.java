package com.izqalan.messenger;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View mainView;
    private RecyclerView requestList;
    private RecyclerView collabRequestList;

    private FirebaseAuth mAuth;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference userDatabse;
    private DatabaseReference collabReq;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseRecyclerAdapter collabAdapter;
    private String currentUserId;

    private static final String TAG = "RequestFragment";
    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_request, container, false);

        requestList = mainView.findViewById(R.id.request_list);
        collabRequestList = mainView.findViewById(R.id.collab_request_list);


        currentUserId = getActivity().getIntent().getStringExtra("user_id");

        if(currentUserId == null){
            currentUserId = mAuth.getInstance().getCurrentUser().getUid();
        }

        mAuth = FirebaseAuth.getInstance();
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req")
                .child(currentUserId);
        userDatabse = FirebaseDatabase.getInstance().getReference().child("Users");
        collabReq = FirebaseDatabase.getInstance().getReference().child("Collab_req").child(currentUserId).child("received");

        //recyclerview settings for friends request
        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        //recyclerview settings for collab request
        collabRequestList.setHasFixedSize(true);
        collabRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter();

        return mainView;
    }

    private void bindAdapter() {


        FirebaseRecyclerOptions<Requests> friendsOptions =
                new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(friendRequestDatabase, Requests.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Requests, RequestHolder>(friendsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestHolder holder, int position, @NonNull Requests model) {

//                Friend_req/currentUID/position UID
                final String user_id = getRef(position).getKey();
                Log.d(TAG, "userid: "+user_id);
                friendRequestDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {

                            String req_type = dataSnapshot.child("request_type").getValue().toString();
                            Log.d(TAG, req_type);

                            if (req_type.equals("received")) {

                                userDatabse.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            final String name = dataSnapshot.child("name").getValue().toString();
                                            final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                                            Log.d(TAG, name);
                                            Log.d(TAG, user_id);

                                            // create a view
                                            holder.setName(name);
                                            holder.setThumbImage(thumb_image);
                                            holder.setMessage("Wants to be your friend");

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });



            }


            @NonNull
            @Override
            public RequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                // inflate view / layout for recycler
                View view =  LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.request_list_layout, viewGroup, false);
                return new RequestHolder(view);
            }
        };

        FirebaseRecyclerOptions<Requests> collabOptions =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(collabReq, Requests.class)
                        .build();


        collabAdapter = new FirebaseRecyclerAdapter<Requests, RequestHolder>(collabOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestHolder holder, final int position, @NonNull Requests model) {

                Log.d(TAG, "pos: "+position);
                final String reqId = getRef(position).getKey();
                Log.d(TAG, "collabreq requestId: "+ reqId);


                // FirebaseDatabase.getInstance().getReference().child("Collab_req").child(currentUserId);
                collabReq.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Log.d(TAG, "before snaps "+dataSnapshot.getRef().getPath().toString()+ " ref "+ dataSnapshot.child(reqId).getChildren());

                        if (dataSnapshot.exists()){

                            String postId = dataSnapshot.child("post_id").getValue().toString();
                            Log.d(TAG, "received "+postId);
                            String sender = dataSnapshot.child("sender").getValue().toString();

                            userDatabse.child(sender).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                                    holder.setThumbImage(image);
                                    holder.setName(name);
                                    holder.setMessage("Wants to collab with you at "+reqId);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public RequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view =  LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.request_list_layout, viewGroup, false);
                return new RequestHolder(view);
            }
        };

        collabRequestList.setAdapter(collabAdapter);
        // REEEEEE SELALU LUPA SET ADAPTER!!
        requestList.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();

        adapter.startListening();
        collabAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        adapter.stopListening();
        collabAdapter.stopListening();
    }

    public static class RequestHolder extends RecyclerView.ViewHolder{

        View v;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setName(String name){
            TextView usernameView = v.findViewById(R.id.request_name);
            usernameView.setText(name);
        }

        public  void setThumbImage(String thumb_image){
            CircleImageView user_avatar = v.findViewById(R.id.request_avatar);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(user_avatar);

        }

        public void setMessage(String message){
            TextView msgView = v.findViewById(R.id.request_type);
            msgView.setText(message);
        }

    }

}
