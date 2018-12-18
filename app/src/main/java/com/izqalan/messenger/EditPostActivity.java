package com.izqalan.messenger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    private EditText foodName;
    private EditText description;
    private TextView location;
    private Button editDate;
    private Button editTime;
    private EditText maxCollab;
    private FloatingActionButton createPostBtn;

    private ProgressDialog progressDialog;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private DatabaseReference postsDatabase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String pushId;
    private String currentUser;


    private String addressLine;
    private Double lat, lgn;
    private String date;
    private String time;
    private String maxCollabNum;

    private String TAG = "EditPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        foodName = findViewById(R.id.food_name);
        description = findViewById(R.id.desc);
        location = findViewById(R.id.location);
        editDate = findViewById(R.id.edit_date);
        editTime = findViewById(R.id.edit_time);
        maxCollab = findViewById(R.id.num_collab);
        createPostBtn = findViewById(R.id.save_post_btn);
        progressDialog = new ProgressDialog(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        postsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUser = mAuth.getCurrentUser().getUid();


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(EditPostActivity.this, MapsActivity.class);
                startActivityForResult(intent, 1);


            }
        });

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // set current date when open dialog
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditPostActivity.this,
                        android.R.style.Theme_Material_Light_Dialog_MinWidth, dateSetListener,
                        year, month, day);

                datePickerDialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                date = day + "/" + month + "/" + year;
                editDate.setText(date);
            }
        };

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(EditPostActivity.this,
                        android.R.style.Theme_Material_Light_Dialog_MinWidth, timeSetListener, hour, minute,
                        android.text.format.DateFormat.is24HourFormat(getApplicationContext()));

                timePickerDialog.show();
            }
        });

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                time = hour + ":" + min;
                editTime.setText(time);
            }
        };

        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userPostRef = "Posts/"+currentUser;

                if (pushId == null){
                    DatabaseReference pushPosts = postsDatabase.child(currentUser).push();
                    pushId = pushPosts.getKey();

                }

                String food_name = foodName.getText().toString();
                String desc = description.getText().toString();
                maxCollabNum = maxCollab.getText().toString();

                // TODO: add image
                Map postVal = new HashMap();
                postVal.put("foodname", food_name);
                postVal.put("desc", desc);
                postVal.put("date", date);
                postVal.put("time", time);
                postVal.put("maxCollabNum", maxCollabNum);
                postVal.put("address", addressLine);
                postVal.put("lat", lat);
                postVal.put("lgn", lgn);

                Map storeMap = new HashMap();

                storeMap.put(userPostRef+"/"+pushId, postVal);


                rootRef.updateChildren(storeMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if (databaseError != null){
                            Log.e(TAG, databaseError.getMessage());
                        }
                        if (databaseError == null){

                            progressDialog.setTitle("Creating post");
                            progressDialog.setMessage("Please wait a moment wile we creating your post");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            Toast.makeText(EditPostActivity.this,"Post created",Toast.LENGTH_LONG).show();
                            finish();
                        }

                    }
                });

            }
        });

        //TODO: Add custom food image & pls redesign



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1)
        {

            if (resultCode == Activity.RESULT_OK)
            {

                addressLine = data.getStringExtra("addressLine");
                lat = data.getDoubleExtra("lat", 0);
                lgn = data.getDoubleExtra("lgn", 0);
                location.setText(addressLine);

                Log.d(TAG, "Location (EditPost): " + addressLine);
                Log.d(TAG, "Location (EditPost): LatLgn" + lat + lgn );
            }
            else {
                Log.d(TAG, "Location (EditPost): " + addressLine);
                Log.d(TAG, "Location (EditPost): LatLgn: " + lat + lgn );
            }
        }


    }
}
