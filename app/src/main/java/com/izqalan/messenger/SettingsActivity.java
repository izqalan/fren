package com.izqalan.messenger;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;

    private CircleImageView avatar;
    private TextView displayname;
    private TextView nbio;
    private Button changeAvatar;
    private Button updateBio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        avatar = findViewById(R.id.settings_avatar);
        displayname = findViewById(R.id.settings_name);
        nbio = findViewById(R.id.settings_bio);
        changeAvatar = findViewById(R.id.change_avatar_btn);
        updateBio = findViewById(R.id.settings_bio_btn);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        // addValueEventListener Listenting to query of databse
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String bio = dataSnapshot.child("bio").getValue().toString();
                String tumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayname.setText(name);
                nbio.setText(bio);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
