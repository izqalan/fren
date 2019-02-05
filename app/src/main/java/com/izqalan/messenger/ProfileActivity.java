package com.izqalan.messenger;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference userDatabse;
    private DatabaseReference friendRequestDB;
    private DatabaseReference friendDatabase; // for user friends list
    private FirebaseUser currentUser;
    private DatabaseReference notificationDB;
    private DatabaseReference rootRef;

    private TextView profileGender;
    private TextView profileLocation;
    private TextView profileName;
    private TextView profileBio;
    private ImageView profileImage;
    private Button declineBtn;
    private Button requestBtn;

    // Tabs
    private TabLayout newTabLAyout;
    private ViewPager vp;
    private ProfilePagerAdapter profilePagerAdapter;

    private String friendship;
    private static final String TAG = "ProfileActivity";

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

        profileGender = findViewById(R.id.profile_gender);
        profileLocation = findViewById(R.id.profile_location);
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
        notificationDB = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootRef = FirebaseDatabase.getInstance().getReference();

        userDatabse.keepSynced(true); // stores local copy and sync on data change

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.tiny_button_push);

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
                if(dataSnapshot.exists()) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String bio = dataSnapshot.child("bio").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String home = dataSnapshot.child("home").getValue().toString();

                    if (gender.equals("Male")){
                        profileGender.setTextColor(Color.BLUE);
                    }else if(gender.equals("Female")){
                        profileGender.setTextColor(Color.RED);
                    }
                    profileGender.setText(gender);

                    if(home.equals("unspecified")){
                       profileLocation.setVisibility(View.GONE);
                    }

                    profileLocation.setText(home);
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

                }
                // Live DB checker

                // for sender
                friendRequestDB.child(currentUser.getUid()).child("sent").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)){
                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if(req_type.equals("sent")){
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


                // for receiver
                friendRequestDB.child(currentUser.getUid()).child("received").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){
                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if (req_type.equals("received")){
                                friendship = "req_received";
                                declineBtn.setVisibility(View.VISIBLE);
                                requestBtn.setText("Accept friend request");

                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Log.d(TAG, "friendship: is "+ friendship );
                // check friendship status for the button
                friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)){
                            friendship = "friend";
                            requestBtn.setText("Unfriend");
                            declineBtn.setVisibility(View.GONE);
                            Log.d(TAG, "friendship: is "+ friendship );
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
                    Map receivedMap = new HashMap();
                    receivedMap.put("Friend_req/"+currentUser.getUid()+"/sent/"+uid, null);
                    receivedMap.put("Friend_req/"+uid+"/received/"+currentUser.getUid(), null);

                    rootRef.updateChildren(receivedMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null)
                            {
                                declineBtn.setVisibility(View.GONE);
                                requestBtn.setEnabled(true);
                                friendship = "not_friend";
                                requestBtn.setText("Send friend request");
                            }
                        }
                    });

                }
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mp.start();
                requestBtn.setEnabled(false);

                // SEND FRIEND REQUEST
                if(friendship.equals("not_friend")){

                    Map requestMap = new HashMap();
                    requestMap.put(currentUser.getUid()+"/sent/"+uid+"/request_type", "sent");
                    requestMap.put(uid+"/received/"+currentUser.getUid()+"/request_type", "received");

                    // update children run 2 queries (requestMap) at a time. improve performance
                    friendRequestDB.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            // notify the user
                            /*
                             * create a db table for notification.
                             * for the notification script function stored in Firebase function.
                             * the js will read the notification db when data on change.
                             * the script will send the notification
                             * */

                            HashMap<String, String> notificationData = new HashMap<>();
                            notificationData.put("from", currentUser.getUid());

                            notificationDB.child(uid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.e("Notification", "Sent");
                                }

                            });

                            // make cancel request button
                            requestBtn.setEnabled(true);
                            friendship = "req_sent";
                            requestBtn.setText("Cancel request");
                        }
                    });

                }

                // CANCEL FRIEND REQUEST
                if(friendship.equals("req_sent")){

                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_req/"+currentUser.getUid()+"/sent/"+uid, null);
                    cancelMap.put("Friend_req/"+uid+"/received/"+currentUser.getUid(), null);

                    rootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){

                                requestBtn.setEnabled(true);
                                friendship = "not_friend";
                                requestBtn.setText("Send friend request");

                            }
                        }
                    });

                }

                // WHEN USER HAVE FRIEND REQUEST
                if (friendship.equals("req_received")){

                    // save friendship relation into user database
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();

                    // add friend request
                    friendsMap.put("Friends/"+currentUser.getUid()+"/"+uid+"/date", currentDate); // save into user1 db
                    friendsMap.put("Friends/"+uid+"/"+currentUser.getUid()+"/date", currentDate); // save into user2 db

                    // remove friend_req from friendRequestDB
                    friendsMap.put("Friend_req/"+currentUser.getUid()+"/received/"+uid, null);
                    friendsMap.put("Friend_req/"+uid+"/sent/"+currentUser.getUid(), null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){
                                requestBtn.setEnabled(true);
                                friendship = "friend";
                                declineBtn.setVisibility(View.GONE);
                                requestBtn.setText("Unfriend");

                            }
                        }
                    });

                }

                // when user unfriend
                if (friendship.equals("friend")){

                    Log.d(TAG, "friendship: is "+ friendship );

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid()+ "/"+uid,null);
                    unfriendMap.put("Friends/"+ uid +"/"+currentUser.getUid(), null);

                    rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null){
                                requestBtn.setEnabled(true);
                                friendship = "not_friend";
                                Log.d(TAG, "friendship: is "+ friendship );
                                requestBtn.setText("Send friend request");
                            }

                        }
                    });


                }

            }
        });

    }
}
