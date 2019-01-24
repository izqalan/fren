package com.izqalan.messenger;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChecklistFragment extends Fragment {

    private View mainView;
    private RecyclerView checkList;
    private DatabaseReference checklistDatabase;
    private FirebaseRecyclerAdapter<ListItem, ListViewHolder> adapter;

    private String postId;

    private static final String TAG = "ChecklistFragment";


    public ChecklistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_checklist, container, false);

        postId = Objects.requireNonNull(getActivity()).getIntent().getStringExtra("post_id");

        Log.d(TAG, "postId: "+ postId);

        checklistDatabase = FirebaseDatabase.getInstance().getReference().child("Posts")
                .child(postId).child("checklist");

        checkList = mainView.findViewById(R.id.check_list);
        checkList.setHasFixedSize(true);
        checkList.setLayoutManager(new LinearLayoutManager(getContext()));

        bindAdapter();

        return mainView;

    }

    public void bindAdapter() {

        Log.d(TAG, "bindAdapter()");


        FirebaseRecyclerOptions<ListItem> options =
                new FirebaseRecyclerOptions.Builder<ListItem>()
                .setQuery(checklistDatabase, ListItem.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<ListItem, ListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListViewHolder holder, final int position, @NonNull ListItem model) {

                final String itemId = getRef(position).getKey();
                Log.d(TAG, "itemId: "+ itemId);

                final ArrayList<String> idMap = new ArrayList<>();
                idMap.add(getRef(position).getKey());

                FragmentActivity i = getActivity();

                i.getIntent().putExtra("idMap", idMap);

                checklistDatabase.child(itemId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String val = dataSnapshot.child("item").getValue().toString();
                            holder.setItem(val);
                            Log.d(TAG, "item: "+ val);


                        }
                        else {

                            holder.setItem("No listed item");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Log.d(TAG, "idMap len new: "+ idMap.size());

            }


            @NonNull
            @Override
            public ListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_list, viewGroup, false);

                return new ListViewHolder(view);
            }
        };

        checkList.setAdapter(adapter);

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

    public static class ListViewHolder extends RecyclerView.ViewHolder{

        View v;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            ImageButton removeBtn = v.findViewById(R.id.remove_list_btn);
            removeBtn.setVisibility(View.GONE);
        }

        public void setItem(String item){
            TextView itemListView = v.findViewById(R.id.item);
            itemListView.setText(item);
        }
    }

}
