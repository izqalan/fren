package com.izqalan.messenger;

import android.app.ActionBar;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolBar;
    private TextView nameView;
    private CircleImageView profileImage;
    private SwipeRefreshLayout swipeRefreshLayout;

    private EditText messageField;
    private ImageButton sendBtn;
    private ImageButton addImageBtn;

    private String chatUser;
    private String username;
    private String username2;
    private String currentUID;
    // number of message should load
    private static final int LIMIT = 7;
    private int page_number = 1;

    private DatabaseReference usersDatabase;
    private DatabaseReference rootRef;
    private DatabaseReference messageDatabase;
    private FirebaseAuth mAuth;

    // message list here
    private RecyclerView messagesList;
    private final List<Messages> msgList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessagesAdapter messagesAdapter;
    private TextView messageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // firebase instantiation
        mAuth = FirebaseAuth.getInstance();
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        rootRef = FirebaseDatabase.getInstance().getReference();


        // get value from previous intent
        chatUser = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("name");
        currentUID = mAuth.getCurrentUser().getUid();
        final String thumb_image = getIntent().getStringExtra("avatar");

        // Action bar & toolbar
        swipeRefreshLayout = findViewById(R.id.swipe_message_layout);
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


        messageText = findViewById(R.id.text_content);

        // chat layout
        messagesAdapter = new MessagesAdapter(msgList);

        messagesList = findViewById(R.id.messages_list);
        linearLayout = new LinearLayoutManager(this);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayout);
        messagesList.setAdapter(messagesAdapter);

        loadMessages();


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

        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username2 = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            // http://sapandiwakar.in/pull-to-refresh-for-android-recyclerview-or-any-other-vertically-scrolling-view/

            @Override
            public void onRefresh() {
                // refresh / load new item
                refreshItems();

            }
        });

    }

    private void refreshItems(){
        // pagination stuff
        page_number++;
        // if msgList is not clear it will show the same previous messages
        // because it will load the latest 7 messages
//        msgList.clear();
        msgPos = 0;

        loadMoreMessages();
        onItemsLoadComplete();
    }
    private void onItemsLoadComplete(){

        // stop refreshing animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadMoreMessages(){

        DatabaseReference messageRef = rootRef.child("Messages").child(mAuth.getCurrentUser().getUid()).child(chatUser);

        // load latest messages and end it at specific message id
        Query messageQuery = messageRef.orderByKey().endAt(lastMsgKey).limitToLast(LIMIT);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                String x = message.getMessage().toString();
                Log.e("MESSAGE_GET: ", x);

                msgList.add(msgPos++, message);
                if (msgPos ==1)
                {
                    lastMsgKey = dataSnapshot.getKey();

                }
                messagesAdapter.notifyDataSetChanged();
                // don't scroll to the bottom
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

    // I don't quite understand this logic but it work
    // something something to do with array, recycler position & get last key value
    // for reference for the next message to load
    private String lastMsgKey = "";
    private int msgPos = 0;

    private void loadMessages() {


        rootRef.child("Messages").child(mAuth.getCurrentUser().getUid()).child(chatUser).limitToLast(page_number * LIMIT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                Messages message = dataSnapshot.getValue(Messages.class);

                String x = message.getMessage().toString();
                Log.e("MESSAGE_GET: ", x);

                msgList.add(msgPos++, message);

                if (msgPos ==1)
                {
                    lastMsgKey = dataSnapshot.getKey();

                }
                messagesAdapter.notifyDataSetChanged();

                // RecyclerView automatically got to the bottom
                // msgList stores messages in array
                // actual size of array - 1 =  size of array including 0
                // thus it scroll to the latest value of array
                // https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work#27063152
                messagesList.scrollToPosition(msgList.size() - 1);

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


            Log.e("name is", username2);


            // values of the /Message/currID/uid/msgID
            Map msgValMap = new HashMap();
            msgValMap.put("message", msg);
            msgValMap.put("from", currentUID);
            msgValMap.put("name", username2);
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
