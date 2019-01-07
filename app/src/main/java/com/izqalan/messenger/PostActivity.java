package com.izqalan.messenger;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private RecyclerView avatarList;
    private DatabaseReference collabDatabase;
    private DatabaseReference postDatabase;
    private DatabaseReference userDatabase;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference requestCollab;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    private ImageView postImage;
    private TextView postName;
    private Button collabBtn;

    private static final String TAG = "PostActivity: ";
    private String ownerId;
    private String requestCode = "req_null";

    private TabLayout newTabLAyout;
    private ViewPager vp;
    private PostPagerAdapter postPagerAdapter;
    private String requestId;
    private String maxCollab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final String postId = getIntent().getStringExtra("post_id");
        final String uid = getIntent().getStringExtra("uid");
        ownerId = getIntent().getStringExtra("user_id");
        maxCollab = getIntent().getStringExtra("maxCollab");

        Log.d(TAG, "post_id:"+postId);

        mAuth = FirebaseAuth.getInstance();
        final String currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        collabDatabase = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).child("collab");
        postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
        requestCollab = FirebaseDatabase.getInstance().getReference().child("Collab_req");
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

        // tabs
        vp = findViewById(R.id.post_tab_pager);
        postPagerAdapter = new PostPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(postPagerAdapter);

        // connect tabs pager with fragment stuff
        newTabLAyout = findViewById(R.id.post_tabs);
        newTabLAyout.setupWithViewPager(vp);



        postImage = findViewById(R.id.post_image);
        postName = findViewById(R.id.post_name);
        collabBtn = findViewById(R.id.collab_btn);

        avatarList = findViewById(R.id.avatar_list);
        avatarList.setHasFixedSize(true);
        avatarList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));



        if (currentUserID.equals(ownerId)){
            collabBtn.setVisibility(View.GONE);
        }


        Log.d(TAG, currentUserID+"/"+ownerId);



        requestCollab.child(currentUserID).child("sent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!currentUserID.equals(ownerId))
                {

                    if (dataSnapshot.hasChild(postId)) {
                        Log.d(TAG, "postId: " + postId + " owner: " + ownerId);
                        String req_type = dataSnapshot.child(postId).child("request_type").getValue().toString();

                        requestId = dataSnapshot.child(postId).child("request_id").getValue().toString();

                        if (req_type.equals("sent")) {
                            requestCode = "req_sent";
                            collabBtn.setText("cancel");
                        }

                        Log.d(TAG, "request_type: " + req_type);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "requestcode-postid: "+postId);
        postDatabase.child("collab").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d(TAG, "requestcode: "+ requestCode+ " "+ currentUserID);
                if (dataSnapshot.hasChild(currentUserID)){

                    requestCode = "approved";
                    collabBtn.setText("Leave party");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "requestcode: "+ requestCode);

        // on sending request to collab with other users
        collabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // leaving party
                if (requestCode.equals("approved")){

                    Map request = new HashMap();

                    request.put("/"+ postId+ "/collab" +currentUserID+"/timestamp", null);

                    postDatabase.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {


                            if (databaseError == null){

                                userDatabase.child(currentUserID).child("events").child(postId).child("created").removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        postDatabase.child("collab").child(currentUserID).removeValue();
                                        requestCode = "req_null";
                                        finish();

                                    }
                                });


                            }


                        }
                    });


                }


                // sending request
                if (requestCode.equals("req_null")){

                    DatabaseReference reqId = requestCollab.child(ownerId).push();
                    String requestId = reqId.getKey();

                    Map request = new HashMap();

                    request.put("/"+ownerId+"/received/"+requestId+"/request_type", "sent");
                    request.put("/"+ownerId+"/received/"+requestId+"/post_id", postId);
                    request.put("/"+ownerId+"/received/"+requestId+"/sender", currentUserID);

                    request.put("/"+currentUserID+"/sent/"+postId+"/request_type", "sent");
                    request.put("/"+currentUserID+"/sent/"+postId+"/request_id", requestId);
                    request.put("/"+currentUserID+"/sent/"+postId+"/owner", ownerId);

//                    request.put("/"+postId+"/"+ownerId+"/request_type", "received");
//                    request.put("/"+postId+"/owner", ownerId);

                    requestCollab.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){

                                requestCode = "req_sent";


                            }
                        }
                    });

                }

                // when cancel request
                if (requestCode.equals("req_sent")){

                    Log.d(TAG, "pressing cancel button");


                    Map request = new HashMap();

                    request.put("/"+ownerId+"/received/"+requestId+"/request_type", null);
                    request.put("/"+ownerId+"/received/"+requestId+"/post_id", null);
                    request.put("/"+ownerId+"/received/"+requestId+"/sender", null);

                    request.put("/"+currentUserID+"/sent/"+postId+"/request_type", null);
                    request.put("/"+currentUserID+"/sent/"+postId+"/request_id", null);
                    request.put("/"+currentUserID+"/sent/"+postId+"/owner", null);
//                    request.put("/"+postId+"/owner", null);

                    requestCollab.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){

                                requestCode = "req_null";
                                collabBtn.setText("Collab");


                            }
                        }
                    });

                }

                // OP view of the button
//                if (requestCode.equals("req_received")){
//
//                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
//
//                    Map request = new HashMap();
//
//                    request.put("Posts/"+postId+"/collab/"+uid+"/date", currentDate);
//
//                    request.put("Collab_req/"+currentUserID+"/"+postId+"/"+uid, null);
//                    request.put("Collab_req/"+uid+"/"+postId+"/"+currentUserID, null);
//
//                    rootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//
//                            if (databaseError == null){
//                                requestCode = "req_approved";
//                            }
//                        }
//                    });
//
//
//                }

            }
        });

        postDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String foodName = dataSnapshot.child("foodname").getValue().toString();
                maxCollab = dataSnapshot.child("maxCollabNum").getValue().toString();
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



                userDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
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
