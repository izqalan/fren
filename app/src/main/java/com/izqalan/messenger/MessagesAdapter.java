package com.izqalan.messenger;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>{

    private List<Messages> mMessagesList;
    private DatabaseReference userDatabase;
    private FirebaseAuth mAuth;

    public MessagesAdapter(List<Messages> messageIn) {

        mAuth = FirebaseAuth.getInstance();
        this.mMessagesList = messageIn;

    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        return new MessagesViewHolder(v);

    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        public TextView timeText;
        public TextView messageText;
        public TextView displayNameText;

        public MessagesViewHolder(View view){
            super (view);
            messageText = view.findViewById(R.id.text_content);
            displayNameText = view.findViewById(R.id.display_name);
            timeText = view.findViewById(R.id.time_stamp);

        }


    }

    @Override
    public void onBindViewHolder(final MessagesViewHolder viewHolder, int i) {

        String currentUID = mAuth.getCurrentUser().getUid();

        // all the messages stored inside messagelist<array list>
        // get messages by using index i
        Messages c = mMessagesList.get(i);

        String fromUser = c.getFrom();
        Log.e("From User", fromUser);

        // do not use currentUID. .equals() can only pass Objects
        if(fromUser.equals(mAuth.getCurrentUser().getUid())){

            // change the layout
            // this only change the layout based on the Text (viewHolder.messageText)
            // not the actual message layout

            viewHolder.messageText.setBackgroundResource(R.drawable.message_bubble_white);
            viewHolder.messageText.setGravity(Gravity.RIGHT);
            viewHolder.messageText.setTextColor(Color.BLACK);

        }else {

            // change layout
            viewHolder.messageText.setBackgroundResource(R.drawable.message_bubble);
            viewHolder.messageText.setTextColor(Color.WHITE);

        }

        String z = c.getMessage();
        // TODO: Remove this before demo
        Log.e("FUCK", z);

        viewHolder.messageText.setText(c.getMessage());
        viewHolder.displayNameText.setText(c.getName());

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

}