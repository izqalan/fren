package com.izqalan.messenger;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference userDatabse;
    private DatabaseReference friendRequestDB;
    private DatabaseReference friendDatabase; // for user friends list
    private FirebaseUser currentUser;

    private TextView profileName;
    private TextView profileBio;
    private ImageView profileImage;
    private Button declineBtn;
    private Button requestBtn;

    private TabLayout newTabLAyout;

    private ViewPager vp;
    private ProfilePagerAdapter profilePagerAdapter;

    private String friendship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // MAKE status bar transparent
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        getWindow().setAttributes(attributes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }


        friendship = "neutral";

        vp = findViewById(R.id.tabs_pager);
        profilePagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager());
        vp.setAdapter(profilePagerAdapter);

        // connect tabs pager with fragment stuff
        newTabLAyout = findViewById(R.id.profile_tabs);
        newTabLAyout.setupWithViewPager(vp);


        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileBio = findViewById(R.id.profile_bio);
        requestBtn = findViewById(R.id.add_btn);
        declineBtn = findViewById(R.id.decline_btn);

        // String uid is not the actual user uid.
        final String uid = getIntent().getStringExtra("user_id");

        // firebase instances
        userDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        friendRequestDB = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        userDatabse.keepSynced(true); // stores local copy and sync on data change

        // USER CANNOT SEND REQUEST or DECLINE TO SELF
        if(uid.equals(currentUser.getUid())){
            requestBtn.setVisibility(View.GONE);
            declineBtn.setVisibility(View.GONE);
        }

        // hide decline btn for all
        declineBtn.setVisibility(View.GONE);

        userDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String bio = dataSnapshot.child("bio").getValue().toString();

                profileName.setText(name);
                profileBio.setText(bio);

                // retrieve image offline first
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
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
                                .into(profileImage);

                    }
                });

//                if(!image.equals("default")){
//                    Picasso.get().load(image).into(profileImage);
//
//                }
//                else {
//                    Picasso.get().load(R.drawable.default_avatar).into(profileImage);
//                }

                // Live DB checker

                friendRequestDB.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){
                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if (req_type.equals("received")){
                                friendship = "req_received";
                                declineBtn.setVisibility(View.VISIBLE);
                                requestBtn.setText("Accept friend request");

                            }else if(req_type.equals("sent")){
                                friendship = "req_sent";
                                declineBtn.setVisibility(View.GONE);
                                requestBtn.setText("cancel request");
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // check friendship status for the button
                friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)){
                            friendship = "friend";
                            requestBtn.setText("Unfriend");
                            declineBtn.setVisibility(View.GONE);

                        }
                        // i did this because
                        else if(!dataSnapshot.hasChild(uid) && friendship.equals("neutral")){
                            friendship = "not_friend";
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (friendship.equals("req_received"))
                {
                    friendRequestDB.child(currentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendRequestDB.child(uid).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    declineBtn.setVisibility(View.GONE);
                                    requestBtn.setEnabled(true);
                                    friendship = "not_friend";
                                    requestBtn.setText("Send friend request");
                                }
                            });
                        }
                    });
                }
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestBtn.setEnabled(false);

                // SEND FRIEND REQUEST
                if(friendship.equals("not_friend")){

                    friendRequestDB.child(currentUser.getUid()).child(uid).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                friendRequestDB.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // make cancel request button
                                        requestBtn.setEnabled(true);
                                        friendship = "req_sent";
                                        requestBtn.setText("Cancel request");


                                        //Toast.makeText(ProfileActivity.this,"Request has been sent", Toast.LENGTH_SHORT ).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"Oops.. something went wrong", Toast.LENGTH_SHORT ).show();

                            }

                        }
                    });

                }

                // CANCEL FRIEND REQUEST
                if(friendship.equals("req_sent")){

                    // remove friend request from actor / current user
                    friendRequestDB.child(currentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // remove current user friend request from the other user
                            friendRequestDB.child(uid).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    requestBtn.setEnabled(true);
                                    friendship = "not_friend";
                                    requestBtn.setText("Send friend request");


                                }
                            });
                        }
                    });
                }

                // WHEN USER HAVE FRIEND REQUEST
                if (friendship.equals("req_received")){

                    // save friendship relation into user database
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    // save into user1 db
                    friendDatabase.child(currentUser.getUid()).child(uid).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // save into user2 db
                            friendDatabase.child(uid).child(currentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    // remove friend request from actor / current user friendRequestDB
                                    friendRequestDB.child(currentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            // remove current user friend request from the other user
                                            friendRequestDB.child(uid).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    requestBtn.setEnabled(true);
                                                    friendship = "friend";
                                                    declineBtn.setVisibility(View.GONE);
                                                    requestBtn.setText("Unfriend");


                                                }
                                            });
                                        }
                                    });
                                }
                            });

                        }
                    });
                }

                // when user unfriend
                if (friendship.equals("friend")){

                    friendDatabase.child(currentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(uid).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    requestBtn.setEnabled(true);
                                    friendship = "not_friend";
                                    requestBtn.setText("Send friend request");
                                }
                            });
                        }
                    });
                }

            }
        });

    }
}
