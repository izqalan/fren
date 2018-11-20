package com.izqalan.messenger;

import android.app.ActionBar;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolBar;
    private TextView nameView;
    private CircleImageView profileImage;

    private String chatUser;
    private String username;

    private DatabaseReference usersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        chatUser = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("name");
        final String thumb_image = getIntent().getStringExtra("avatar");

        chatToolBar = findViewById(R.id.chat_tool_bar);
        setActionBar(chatToolBar);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);



        actionBar.setCustomView(actionBarView);

        nameView = findViewById(R.id.display_name);
        profileImage = findViewById(R.id.chat_avatar);

        nameView.setText(username);

        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
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
                        .into(profileImage);

            }
        });




    }
}
