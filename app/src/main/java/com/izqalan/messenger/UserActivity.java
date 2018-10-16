package com.izqalan.messenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private RecyclerView UserList;
    private Toolbar toolbar;
    private DatabaseReference userDatabase;
    private FirebaseRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.user_toolbar);
        setActionBar(toolbar);
        getActionBar().setTitle("Users");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        UserList = findViewById(R.id.user_list);
        UserList.setHasFixedSize(true);
        UserList.setLayoutManager(new LinearLayoutManager(this));

        bindAdapter();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void bindAdapter(){
        // I have no idea what i wrote. thanks stackoverflow.
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(userDatabase, Users.class )
                        .build();

        // UserViewHolder holds data of users, userid being the key
        adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {

                holder.setName(model.getName());
                holder.setBio(model.getBio());
                holder.setThumbImage(model.getImage());

                // get user id from the list
                final String user_id = getRef(position).getKey();

                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // send value of id to another page
                        Intent intent = new Intent(UserActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", user_id );
                        startActivity(intent);

                    }
                });

            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_list_layout, viewGroup, false);

                return new UserViewHolder(view);
            }
        };

        UserList.setAdapter(adapter);

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View v;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;

        }

        public void setName(String name){
            TextView usernameView = v.findViewById(R.id.username);
            usernameView.setText(name);
        }

        public void setBio(String bio){
            TextView bioView = v.findViewById(R.id.user_bio);
            bioView.setText(bio);
        }

        public  void setThumbImage(String thumb_image){
            CircleImageView user_avatar = v.findViewById(R.id.user_avatar);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(user_avatar);

        }


    }
}
