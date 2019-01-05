package com.izqalan.messenger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditPostActivity extends AppCompatActivity {

    private EditText foodName;
    private EditText description;
    private TextView location;
    private Button editDate;
    private Button editTime;
    private EditText maxCollab;
    private FloatingActionButton createPostBtn;
    private CircleImageView foodImg;

    private ProgressDialog progressDialog;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private DatabaseReference postsDatabase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference firebaseStorage;

    private String pushId;


    private String addressLine;
    private Double lat, lgn;
    private String date;
    private String time;
    private String maxCollabNum;

    private String TAG = "EditPostActivity";
    private String currentUser = mAuth.getCurrentUser().getUid();
    private String downloadUrl;
    private String thumb_downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        foodImg = findViewById(R.id.food_img);
        foodName = findViewById(R.id.food_name);
        description = findViewById(R.id.desc);
        location = findViewById(R.id.address);
        editDate = findViewById(R.id.edit_date);
        editTime = findViewById(R.id.edit_time);
        maxCollab = findViewById(R.id.num_collab);
        createPostBtn = findViewById(R.id.save_post_btn);
        progressDialog = new ProgressDialog(this);

        rootRef = FirebaseDatabase.getInstance().getReference();
        postsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        firebaseStorage = FirebaseStorage.getInstance().getReference();

        if (pushId == null){
            DatabaseReference pushPosts = postsDatabase.child(currentUser).push();
            pushId = pushPosts.getKey();

        }





        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(EditPostActivity.this, MapsActivity.class);
                // listen for result from another intent
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

                String userPostRef = "Posts/";

                if (pushId == null){
                    DatabaseReference pushPosts = postsDatabase.child(currentUser).push();
                    pushId = pushPosts.getKey();

                }

                String food_name = foodName.getText().toString();
                String desc = description.getText().toString();
                maxCollabNum = maxCollab.getText().toString();
                String current_user_thumb = rootRef.child("Users").child(currentUser).child("thumb_image").toString();

                Map postVal = new HashMap();
                postVal.put("owner", currentUser);
                postVal.put("foodname", food_name);
                postVal.put("desc", desc);
                postVal.put("date", date);
                postVal.put("time", time);
                postVal.put("maxCollabNum", maxCollabNum);
                postVal.put("address", addressLine);
                postVal.put("lat", lat);
                postVal.put("lgn", lgn);
                postVal.put("image", downloadUrl);
                postVal.put("thumb_image", thumb_downloadUrl);
                postVal.put("timestamp", ServerValue.TIMESTAMP);

                if (postVal.get("image") == null){
                    postVal.put("image", "default");
                    postVal.put("thumb_image", "default");
                }

                // TODO: Bug! cannot stores Owner as collaborators
                Map storeMap = new HashMap();

                storeMap.put(userPostRef+pushId, postVal);
//                storeMap.put("Posts/"+pushId+"/collab/"+currentUser+"/thumb_image", current_user_thumb);

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

                        }
                        finish();
                    }
                });

            }
        });

        //TODO: Add custom food image & pls redesign

        foodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(EditPostActivity.this);
            }
        });


    }

    // listen result from MapActivity
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

        // image crop activity listener
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK)
            {
                progressDialog = new ProgressDialog(EditPostActivity.this);
                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Please wait a moment");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                // uri location of your image
                Uri resultUri = result.getUri();
                File thumb_uri = new File(resultUri.getPath());

                // make random string for stored image
                final String imgId = pushId;

                // image compression before push to storage
                Bitmap thumbnail = null;
                try{
                    thumbnail = new Compressor(this)
                            .setMaxHeight(600)
                            .setMaxWidth(600)
                            .setQuality(70)
                            .compressToBitmap(thumb_uri);
                }catch (IOException e){
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_data = baos.toByteArray();


                final StorageReference filepath = firebaseStorage.child("posts").child(imgId+".jpg");
                final StorageReference thumb_filepath = firebaseStorage.child("posts").child("thumb").child(imgId+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            // firebase uploadtask interface
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_data);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if(task.isSuccessful())
                                    {
                                        final Map update_map = new HashMap();

                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                        {

                                            @Override
                                            public void onSuccess(Uri uri)
                                            {
                                                downloadUrl = uri.toString();

                                                Log.e("downloadUrl", downloadUrl);
//                                                update_map.put("image", downloadUrl);
//                                                TODO: Bad solution but it work. need to review again
//                                                postsDatabase.child(currentUser).child(pushId).child("image").setValue(downloadUrl);

                                                // load the image
                                                Picasso.get().load(downloadUrl).into(foodImg, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        // TODO: Pls add default food image
                                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.default_avatar)
                                                                .error(R.drawable.default_avatar).into(foodImg);
                                                    }
                                                });

                                            }
                                        });

                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                thumb_downloadUrl = uri.toString();
                                                Log.e("downloadUrl", thumb_downloadUrl);
//                                                postsDatabase.child(currentUser).child(pushId).child("thumb_image").setValue(thumb_downloadUrl);

                                            }
                                        });


                                        progressDialog.dismiss();
                                        Toast.makeText(EditPostActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();


                                    }

                                }
                            });

                        }

                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.getMessage();
                error.getStackTrace();
            }

        }


    }
}
