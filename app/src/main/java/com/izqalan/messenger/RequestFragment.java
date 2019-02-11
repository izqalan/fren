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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View mainView;
    private RecyclerView requestList;
    private RecyclerView collabRequestList;
    private ImageButton approveBtn;
    private ImageButton rejectBtn;

    private FirebaseAuth mAuth;

    private DatabaseReference friendRequestDatabase;
    private DatabaseReference userDatabse;
    private DatabaseReference postsDatabase;
    private DatabaseReference collabReq;
    private DatabaseReference collabReqRecycler;
    private DatabaseReference rootRef;

    private FirebaseRecyclerAdapter<Requests, RequestHolder> adapter;
    private FirebaseRecyclerAdapter<Requests, RequestHolder> collabAdapter;
    private String currentUserId;
    String foodname;

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
                .child(currentUserId).child("received");

        rootRef = FirebaseDatabase.getInstance().getReference();
        userDatabse = FirebaseDatabase.getInstance().getReference().child("Users");
        collabReq = FirebaseDatabase.getInstance().getReference().child("Collab_req");
        collabReqRecycler = FirebaseDatabase.getInstance().getReference().child("Collab_req").child(currentUserId).child("received");
        postsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

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

                // the buttons cannot be instantiate inside onCreate method
                // because the view haven't inflate/load any layout into the fragment yet
                approveBtn = holder.v.findViewById(R.id.request_approve);
                rejectBtn = holder.v.findViewById(R.id.request_decline);


                // Friend_req/currentUID/position UID
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

                                            approveBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    Log.d(TAG, "CLICKED APPROVE BTUUNO");
                                                    // save friendship relation into user database
                                                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                                    Map<String, Object> friendsMap = new HashMap<>();

                                                    Log.d(TAG, "currentuid: "+currentUserId+ " userId: "+ user_id);

                                                    // remove friend_req from friendRequestDB
                                                    friendsMap.put("/Friend_req/"+currentUserId+"/received/"+user_id+"/request_type", null);
                                                    friendsMap.put("/Friend_req/"+user_id+"/sent/"+currentUserId+"/request_type", null);

                                                    // add friend request
                                                    friendsMap.put("Friends/"+currentUserId+"/"+user_id+"/date", currentDate); // save into user1 db
                                                    friendsMap.put("Friends/"+user_id+"/"+currentUserId+"/date", currentDate); // save into user2 db


                                                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                            if (databaseError != null){

                                                                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                            }

                                                        }
                                                    });



                                                }
                                            });
                                            rejectBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    Map<String, Object> receivedMap = new HashMap<>();
                                                    receivedMap.put("Friend_req/"+currentUserId+"/received/"+user_id+"/request_type", null);
                                                    receivedMap.put("Friend_req/"+user_id+"/sent/"+currentUserId+"/request_type", null);

                                                    rootRef.updateChildren(receivedMap, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                            if (!databaseError.equals(null)){

                                                                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                            }                                                        }
                                                    });


                                                }
                                            });



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
                // inflate layout i want to use into recyclerView
                View view =  LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.request_list_layout, viewGroup, false);


                return new RequestHolder(view);
            }
        };

        FirebaseRecyclerOptions<Requests> collabOptions =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(collabReqRecycler, Requests.class)
                        .build();


        collabAdapter = new FirebaseRecyclerAdapter<Requests, RequestHolder>(collabOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestHolder holder, final int position, @NonNull Requests model) {

                Log.d(TAG, "pos: "+position);
                final String reqId = getRef(position).getKey();
                Log.d(TAG, "collabreq requestId: "+ reqId);

                // the buttons cannot be instantiate inside onCreate method
                // because the view haven't inflate/load any layout into the fragment yet
                approveBtn = holder.v.findViewById(R.id.request_approve);
                rejectBtn = holder.v.findViewById(R.id.request_decline);


                // FirebaseDatabase.getInstance().getReference().child("Collab_req").child(currentUserId).child("received");
                collabReqRecycler.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Log.d(TAG, "before snaps "+dataSnapshot.getRef().getPath().toString()+ " ref "+ dataSnapshot.child(reqId).getChildren());

                        if (dataSnapshot.exists()){

                            final String postId = dataSnapshot.child("post_id").getValue().toString();
                            Log.d(TAG, "received "+postId);
                            final String sender = dataSnapshot.child("sender").getValue().toString();

                            // ValueEventListener cannot be nested into another eventValueListener
                            // asynchronous protocol
                            postsDatabase.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        foodname = dataSnapshot.child("foodname").getValue().toString();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            userDatabse.child(sender).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    final String name = dataSnapshot.child("name").getValue().toString();
                                    String image = dataSnapshot.child("thumb_image").getValue().toString();



                                    holder.setThumbImage(image);
                                    holder.setName(name);
                                    holder.setMessage("Wants to collab with you on "+ foodname);


                                    approveBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Toast.makeText(getContext(), "Approved" + name, Toast.LENGTH_SHORT).show();

                                            final Map<String, Object> collabRequest = new HashMap<>();

                                            collabRequest.put("/collab/"+sender+"/timestamp", ServerValue.TIMESTAMP);

                                            postsDatabase.child(postId).updateChildren(collabRequest, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                    if(databaseError == null){

                                                        final Map<String, Object> nMap = new HashMap<String, Object>();
                                                        Log.d(TAG, "removing request"+postId);
                                                        nMap.put("/"+reqId+"/post_id", null);
                                                        nMap.put("/"+reqId+"/request_type", null);
                                                        nMap.put("/"+reqId+"/sender", null);

                                                        collabReqRecycler.updateChildren(nMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                                if (databaseError == null){

                                                                    DatabaseReference collabReqSent = FirebaseDatabase.getInstance().getReference()
                                                                            .child("Collab_req").child(sender).child("sent");
                                                                    Log.d(TAG, "sender/"+sender);

                                                                    final Map<String, Object> sentMap = new HashMap<>();
                                                                    Log.d(TAG, "removing request"+postId);
                                                                    sentMap.put("/"+postId+"/request_id", null);
                                                                    sentMap.put("/"+postId+"/owner", null);
                                                                    sentMap.put("/"+postId+"/request_type", null);

                                                                    collabReq.child(sender).child("sent").updateChildren(sentMap, new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                            Toast.makeText(getContext(), "Request removed", Toast.LENGTH_SHORT).show();

                                                                        }
                                                                    });

                                                                }


                                                            }
                                                        });



                                                        // made this for user joined / posts history
                                                        Map<String, Object> eventMap = new HashMap<String, Object>();

                                                        eventMap.put("/events/"+postId+"/created", ServerValue.TIMESTAMP);

                                                        userDatabse.child(sender).updateChildren(eventMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {



                                                            }
                                                        });


                                                    }

                                                }
                                            });


                                        }
                                    });
                                    rejectBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Toast.makeText(getContext(), "Rejected", Toast.LENGTH_SHORT).show();
                                            Map<String, Object> nMap = new HashMap<>();
                                            Log.d(TAG, "removing request"+postId);
                                            nMap.put("/"+reqId+"/post_id", null);
                                            nMap.put("/"+reqId+"/request_type", null);
                                            nMap.put("/"+reqId+"/sender", null);
                                            collabReq.child(currentUserId).child("received").updateChildren(nMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                    Toast.makeText(getContext(), "Request removed", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }
                                    });


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
                // inflate layout i want to use into recyclerView
                // in this fragment(getContext()) inflate layout.view(R.layout.id)
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
