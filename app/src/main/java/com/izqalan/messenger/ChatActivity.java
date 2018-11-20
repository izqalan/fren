package com.izqalan.messenger;

import android.app.ActionBar;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolBar;
    private TextView nameView;
    private CircleImageView profileImage;

    private EditText messageField;
    private ImageButton sendBtn;
    private ImageButton addImageBtn;

    private String chatUser;
    private String username;

    private DatabaseReference usersDatabase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // firebase instantiation
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // get value from previous intent
        chatUser = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("name");
        final String thumb_image = getIntent().getStringExtra("avatar");

        // Action bar & toolbar
        chatToolBar = findViewById(R.id.chat_tool_bar);
        setActionBar(chatToolBar);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null); // render layout

        actionBar.setCustomView(actionBarView);

        // chat_custom_bar layout
        nameView = findViewById(R.id.display_name);
        profileImage = findViewById(R.id.chat_avatar);

        // this activity layout
        messageField = findViewById(R.id.msgText);
        sendBtn = findViewById(R.id.add_image_btn);
        sendBtn = findViewById(R.id.send_btn);


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

        rootRef.child("Chat").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(chatUser)){


                    Map chatMap = new HashMap();
                    chatMap.put("seen", false);
                    chatMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map userMap = new HashMap();
                    userMap.put("Chat/"+mAuth.getCurrentUser().getUid()+"/"+chatUser, chatMap);
                    userMap.put("Chat/"+chatUser+"/"+mAuth.getCurrentUser().getUid(), chatMap);

                    rootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {


                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                messageField.setText(null);

            }
        });

    }

    private void sendMessage() {

        String msg = messageField.getText().toString();

        // if the text field is not empty then it can send the message
        if(!TextUtils.isEmpty(msg)){

            String current_user_ref = "Messages/"+mAuth.getCurrentUser().getUid()+"/"+chatUser;
            String chat_user_ref = "Messages/"+chatUser+"/"+mAuth.getCurrentUser().getUid();

            // create Id for messages. sama concept macam notiofication cuma present data differently.
            DatabaseReference pushMessage = rootRef.child("Messages").child(mAuth.getCurrentUser().getUid())
                    .child(chatUser).push();

            String pushId = pushMessage.getKey();

            // values of the /Message/currID/uid/msgID
            Map msgValMap = new HashMap();
            msgValMap.put("message", msg);
            msgValMap.put("seen", false);
            msgValMap.put("type", "text");
            msgValMap.put("time", ServerValue.TIMESTAMP);

            // store msg Id into table for each users
            Map storeMsgMap = new HashMap();
            storeMsgMap.put(current_user_ref+"/"+pushId, msgValMap);
            storeMsgMap.put(chat_user_ref+"/"+pushId, msgValMap);

            rootRef.updateChildren(storeMsgMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.e("Chat_LOG: ", databaseError.getMessage());
                    }
                }


            });



        }

    }
}
