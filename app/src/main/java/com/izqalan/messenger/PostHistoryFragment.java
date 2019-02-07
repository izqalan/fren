package com.izqalan.messenger;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostHistoryFragment extends Fragment {

	private FirebaseRecyclerAdapter adapter;
	private RecyclerView postHistoryList;
	private ImageView emptyDataSetImage;

	private FirebaseAuth mAuth;

	private DatabaseReference postHistory;
	private DatabaseReference postDatabase;


	private String currentUserId;

	private static final String TAG = "PostHistoryFragment";

	public PostHistoryFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View mainView = inflater.inflate(R.layout.fragment_post_history, container, false);

		currentUserId = mAuth.getInstance().getCurrentUser().getUid();
		final String uid = getActivity().getIntent().getStringExtra("user_id");

		postHistory = FirebaseDatabase.getInstance().getReference().child("History").child(uid);
		postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

		emptyDataSetImage = mainView.findViewById(R.id.empty_data_set);

		postHistoryList = mainView.findViewById(R.id.post_history_list);
		postHistoryList.setHasFixedSize(true);
		postHistoryList.setLayoutManager(new LinearLayoutManager(getContext()));

		bindAdapter();

		return mainView;
	}

	private void bindAdapter() {

		Query historyQuery = postHistory.orderByChild("timestamp");

		FirebaseRecyclerOptions<Posts> options =
				new FirebaseRecyclerOptions.Builder<Posts>()
				.setQuery(historyQuery, Posts.class)
				.build();

		adapter = new FirebaseRecyclerAdapter<Posts, PostingFragment.PostViewHolder>(options) {
			@Override
			protected void onBindViewHolder(@NonNull final PostingFragment.PostViewHolder holder, int position, @NonNull Posts model) {

				final String post_id = getRef(position).getKey();


				postDatabase.child(post_id).addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


						if(dataSnapshot.exists()) {

                            emptyDataSetImage.setVisibility(View.GONE);
							// Objects.requireNonNull() throws nullpo exception
							// https://stackoverflow.com/questions/29864642/is-objects-requirenonnull-less-efficient-than-the-old-way#29864679
							final String foodName = Objects.requireNonNull(dataSnapshot.child("foodname").getValue()).toString();
							final String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
							final String date = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
							final String time = Objects.requireNonNull(dataSnapshot.child("time").getValue()).toString();
							final String owner = Objects.requireNonNull(dataSnapshot.child("owner").getValue()).toString();
							final String address = Objects.requireNonNull(dataSnapshot.child("address").getValue()).toString();


							Log.d(TAG, image);
							holder.setFoodName(foodName);
							holder.setImage(image);
							holder.setDate(date);
							holder.setTime(time);

							if (address.length() > 50) {
								String shortAddr = address.substring(0, 50) + "...";
								holder.setAddress(shortAddr);
							} else {
								holder.setAddress(address);
							}


							holder.v.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {

									// go to new page that shows more detail about the event
									Intent intent = new Intent(getContext(), PostActivity.class);
									intent.putExtra("user_id", owner);
									intent.putExtra("uid", currentUserId);
									intent.putExtra("post_id", post_id);
									intent.putExtra("image", image);

									startActivity(intent);

								}
							});

						}


					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}

				});

			}


			@NonNull
			@Override
			public PostingFragment.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

				View v = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.posts_list_layout, viewGroup,false);

				return new PostingFragment.PostViewHolder(v);
			}
		};

		postHistoryList.setAdapter(adapter);


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

}
