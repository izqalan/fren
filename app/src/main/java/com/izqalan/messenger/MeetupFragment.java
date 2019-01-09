package com.izqalan.messenger;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeetupFragment extends Fragment {

    private View mainView;

    private TextView addressLineView;
    private ImageButton mapsBtn;

    private Double lat, lgn;
    private String addressLine;
    private String postId;
    private String uri;

    private DatabaseReference postDatabase;

    private static final String TAG = "MeetupFragment";

    public MeetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_meetup, container, false);

        addressLineView = mainView.findViewById(R.id.map_card_address);
        mapsBtn = mainView.findViewById(R.id.goto_maps_btn);

        postId = getActivity().getIntent().getStringExtra("post_id");

        Log.d(TAG, "PostId: " + postId);

        postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
        postDatabase.keepSynced(true);

        postDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    Log.d(TAG, "datasnapshot exists");
                    addressLine = dataSnapshot.child("address").getValue().toString();
                    lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    lgn = Double.parseDouble(dataSnapshot.child("lgn").getValue().toString());
                    addressLineView.setText(addressLine);
                    // open google maps and drop pin
                    uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f", lat, lgn,lat, lgn);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        return mainView;
    }

}
