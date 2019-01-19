package com.izqalan.messenger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;

    private CircleImageView avatar;
    private TextView displayname;
    private TextView nbio;
    private TextView homeLocation;
    private TextView genderView;
    private Button changeAvatar;
    private Button updateBio;
    private ImageButton editGenderBtn;
    private ImageButton editNameBtn;
    private ImageButton editHomeLocationBtn;

    private ProgressDialog progressDialog;

    // Firebase storage ref
    private StorageReference firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        avatar = findViewById(R.id.settings_avatar);
        displayname = findViewById(R.id.settings_name);
        nbio = findViewById(R.id.settings_bio);
        changeAvatar = findViewById(R.id.change_avatar_btn);
        updateBio = findViewById(R.id.settings_bio_btn);

        homeLocation = findViewById(R.id.settings_location);
        genderView = findViewById(R.id.settings_gender);


        editNameBtn = findViewById(R.id.settings_name_btn);
        editGenderBtn = findViewById(R.id.settings_gender_btn);
        editHomeLocationBtn = findViewById(R.id.settings_location_btn);



        firebaseStorage = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userDatabase.keepSynced(true); // stores local copy of data





        // addValueEventListener Listenting to query of databse
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String bio = dataSnapshot.child("bio").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String home = dataSnapshot.child("home").getValue().toString();

                    if (gender.equals("Male")) {
                        genderView.setTextColor(Color.BLUE);
                    }else{
                        genderView.setTextColor(Color.RED);
                    }
                    genderView.setText(gender);
                    homeLocation.setText(home);
                    displayname.setText(name);
                    nbio.setText(bio);
                    //
                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(avatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                // when image haven't store on disk, picasso look out for image.
                                Picasso.get()
                                        .load(image)
                                        .placeholder(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .into(avatar);
                            }
                        });

                    } else {
                        Picasso.get().load(R.drawable.default_avatar).into(avatar);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inputDialog("Change Name", displayname, "name");

            }
        });

        editHomeLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDialog("Location", homeLocation, "home");
            }
        });

        editGenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] gender = {"Male", "Female"};

                AlertDialog.Builder genderDialog = new AlertDialog.Builder(SettingsActivity.this);
                genderDialog.setTitle("What is your gender");
                genderDialog.setItems(gender, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        progressDialog = new ProgressDialog(SettingsActivity.this);
                        progressDialog.setTitle("Saving Changes");
                        progressDialog.setMessage("Please wait a moment");
                        progressDialog.show();


                        userDatabase.child("gender").setValue(gender[i]).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SettingsActivity.this, "saved",
                                            Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    progressDialog.hide();
                                    Toast.makeText(SettingsActivity.this,
                                            "Oops.. Something went wrong", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });

                    }
                });
                genderDialog.show();

            }
        });

        updateBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String uid = currentUser.getUid();
                userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                inputDialog("New Bio", nbio, "bio");

            }
        });

        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // third-party img cropper
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });
    }


    // instead of passing String userDatabaseKey, it is better to pass Query of the database ref
    public void inputDialog(String title, TextView textView, final String userDatabaseKey){

        // input dialog builder
        final EditText input = new EditText(SettingsActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(title);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String oldStuff = textView.getText().toString();
        // set old bio into param
        input.setText(oldStuff);
        // get input value
        builder.setView(input);

        // make button
        builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                final String ninput = input.getText().toString();
                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait a moment");
                progressDialog.show();

                // send to firebase stuff

                userDatabase.child(userDatabaseKey).setValue(ninput).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "saved", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            progressDialog.hide();
                            Toast.makeText(SettingsActivity.this, "Oops.. Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }

        });

        // cancel btn
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }


    // THIRD-PARTY IMG CROPPER
  @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Please wait a moment");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                File thumb_uri = new File(resultUri.getPath());

                // set image name as user id
                final String userid = currentUser.getUid();

                // image compression
                Bitmap thumbnail = null;
                try {
                    thumbnail = new Compressor(this)
                            .setMaxHeight(250)
                            .setMaxWidth(250)
                            .setQuality(65)
                            .compressToBitmap(thumb_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_data = baos.toByteArray();




                final StorageReference filepath = firebaseStorage.child("avatar").child(userid+".jpg");
                final StorageReference thumb_filepath = firebaseStorage.child("avatar").child("thumb").child(userid+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_data);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                                    // save url into firebase storage PROBLEM HERE
                                    if (task.isSuccessful()){
                                        final Map update_map = new HashMap();
                                        // get url
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String downloadUrl = uri.toString();
                                                Log.e("downloadUrl", downloadUrl);
//                                                update_map.put("image", downloadUrl);
//                                                TODO: Bad solution but it work. need to review again
                                                userDatabase.child("image").setValue(downloadUrl);

                                            }


                                        });

                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String thumb_dowloadUrl = uri.toString();
                                                Log.e("thumb_dowloadUrl", thumb_dowloadUrl);
//                                                update_map.put("thumb_image", thumb_dowloadUrl);
//                                              TODO: Bad solution but it work. need to review again
                                                userDatabase.child("thumb_image").setValue(thumb_dowloadUrl);


                                            }
                                        });

                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();


//                                        userDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener() {
//                                            @Override
//                                            public void onComplete(@NonNull Task task) {
//                                                if(task.isSuccessful()){
//                                                    progressDialog.dismiss();
//                                                    Toast.makeText(SettingsActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();
//
//                                                }
//                                            }
//                                        });


                                    }
                                }
                            });

                        }
                    }
                });







//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//
//                        if(task.isSuccessful()){
//
//                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    String downloadUrl = uri.toString();
//                                    Log.e("DOWNLOAD URL",downloadUrl);
//
//
//                                    userDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                            if(task.isSuccessful()){
//                                                progressDialog.dismiss();
//                                                Toast.makeText(SettingsActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();
//
//                                            }
//                                        }
//                                    });
//                                }
//
//                            });
//
//
////                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_data);
////
////                            final String downloadUrl =task.getResult().getStorage().getDownloadUrl().toString();
////                            Log.e("Download Url", downloadUrl);
////
////
////
////                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
////                                @Override
////                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
////
////                                    String thumb_downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
////                                    Log.e("Thumb_downloadUrl", thumb_downloadUrl);
////
////                                    if(task.isSuccessful()){
////                                        Map update_map = new HashMap();
////                                        update_map.put("image", downloadUrl);
////                                        update_map.put("thumb_image", thumb_downloadUrl);
////
////                                        userDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
////                                            @Override
////                                            public void onComplete(@NonNull Task<Void> task) {
////                                                if(task.isSuccessful()){
////                                                    progressDialog.dismiss();
////                                                    Toast.makeText(SettingsActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();
////
////                                                }
////                                            }
////                                        });
////                                    }
//
////                              .getDownloadUrl() is an Asynchronous call. need a listener before the url can pass around
//
//
//
//
//                        }else {
//                            progressDialog.dismiss();
//                            Toast.makeText(SettingsActivity.this,"Error", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
