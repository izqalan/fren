package com.izqalan.messenger;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * kind of the same as friends fragment
 */
public class ChatFragment extends Fragment {

    // layout
    private RecyclerView chatListView;
    private View mainView;
    private FirebaseRecyclerAdapter adapter;

    // firebase database
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabase;
    private DatabaseReference chatDatabase;
    private DatabaseReference messageDatabase;



    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_chat, container, false);

        chatListView = mainView.findViewById(R.id.chat_list);

        final String currentUid = mAuth.getInstance().getCurrentUser().getUid();
//        chatDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUid);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        messageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUid);

        // Recyclerview settings
        chatListView.setHasFixedSize(true);
        chatListView.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter();

        return mainView;
    }

    private void bindAdapter()
    {

        // sort by time
//        Query convQuery = messageDatabase.orderByChild("timestamp");

        // builder
        FirebaseRecyclerOptions<Conversation> options =
                new FirebaseRecyclerOptions.Builder<Conversation>()
                .setQuery(messageDatabase, Conversation.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Conversation, ConViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConViewHolder holder, int position, @NonNull Conversation model) {

                final String list_user_id = getRef(position).getKey();

                // limit the message show to the latest message on the preview
//                Query lastMessageQuery = messageDatabase.child(list_user_id).limitToLast(1);
                Query lastMessageQuery = messageDatabase.child(list_user_id);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data);
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

                // this we get name, avatar & last messages from listed uid
                userDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(username);
                        holder.setThumbImage(thumbImage);

                        holder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("user_id", list_user_id);
                                intent.putExtra("name", username);
                                intent.putExtra("avatar", thumbImage);
                                startActivity(intent);

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
            public ConViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_list_layout, viewGroup, false);
                return new ConViewHolder(v);
            }
        };

        // this line of code will build the view
        chatListView.setAdapter(adapter);
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


    public static class ConViewHolder extends RecyclerView.ViewHolder{

        View v;


        public  ConViewHolder(View itemView){
            super(itemView);
            v = itemView;
        }

        public void setName(String name){
            TextView usernameView = v.findViewById(R.id.username);
            usernameView.setText(name);
        }

        public void setMessage(String message){
            TextView messageView = v.findViewById(R.id.user_bio);
            messageView.setText(message);

            messageView.setTypeface(messageView.getTypeface(), Typeface.BOLD);
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
