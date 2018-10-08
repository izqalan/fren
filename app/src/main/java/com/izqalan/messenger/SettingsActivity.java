package com.izqalan.messenger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;

    private CircleImageView avatar;
    private TextView displayname;
    private TextView nbio;
    private Button changeAvatar;
    private Button updateBio;

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

        firebaseStorage = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        // addValueEventListener Listenting to query of databse
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String bio = dataSnapshot.child("bio").getValue().toString();
                String tumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayname.setText(name);
                nbio.setText(bio);
                //
                Picasso.get().load(image).into(avatar);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String uid = currentUser.getUid();
                userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                // input dialog builder
                final EditText input = new EditText(SettingsActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("New Bio");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                String oldBio = nbio.getText().toString();
                // set old bio into param
                input.setText(oldBio);
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

                        userDatabase.child("bio").setValue(ninput).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });

        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                // open up gallery to choose avi
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//
//                startActivityForResult(Intent.createChooser(intent, "Select an image"),1);

//                // third-party img cropper
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });
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

                // make random string for stored image
                final String userid = currentUser.getUid();

                final StorageReference filepath = firebaseStorage.child("avatar").child(userid+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                        if(task.isSuccessful()){

                            // .getDownloadUrl() is an Asynchronous call. need a listener before the url can pass around

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    Log.e("DOWNLOAD URL",downloadUrl);
                                    userDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                                }
                            });


                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,"Error", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
