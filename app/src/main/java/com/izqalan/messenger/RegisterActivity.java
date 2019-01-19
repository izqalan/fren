package com.izqalan.messenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText username;
    private EditText email;
    private EditText password;
    private Button registerBtn;
    private Toolbar toolbar;

    private ProgressDialog regProgress;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //include toolbar
        toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setActionBar(toolbar);
        getActionBar().setTitle("Create Account");
        // send to parent activity. parent activity can be set on android manifest
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        // set form fields
        username = findViewById(R.id.reg_name);
        email = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_password);
        registerBtn = findViewById(R.id.reg_create_btn);

        regProgress = new ProgressDialog(this);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get value from fields
                String reg_name = username.getText().toString();
                String reg_email = email.getText().toString();
                String reg_password = password.getText().toString();

                if (TextUtils.isEmpty(reg_name)){
                    username.setError("Username cannot be empty");
                }
                if (TextUtils.isEmpty(reg_email)){
                    email.setError("Invalid email");
                }
                if (TextUtils.isEmpty(reg_password) || reg_password.length() < 6){
                    password.setError("Password must be at least 6 characters");
                }

                // null checking
                if(!TextUtils.isEmpty(reg_name) || !TextUtils.isEmpty(reg_email) || !TextUtils.isEmpty(reg_password)){
                    regProgress.setTitle("Registering user");
                    regProgress.setMessage("Please wait a moment wile we setting up your account");
                    regProgress.setCanceledOnTouchOutside(false);
                    regProgress.show();

                    registerUser(reg_name, reg_email, reg_password);
                }




            }
        });
    }

    private void registerUser(final String reg_name, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // get input and store into firebase realtime db for my use
                            database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            HashMap<String, String>userMap = new HashMap<>();
                            userMap.put("name", reg_name);
                            userMap.put("bio", "Hi there, I'm using TBD Messenger.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);
                            userMap.put("home", "unspecified");
                            userMap.put("gender", "unspecified");

                            database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        // dismiss dialog we created ealier
                                        Toast.makeText(RegisterActivity.this, "Willkommen!", Toast.LENGTH_SHORT).show();
                                        regProgress.dismiss();

                                        // go to mainActivity
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            regProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

}
