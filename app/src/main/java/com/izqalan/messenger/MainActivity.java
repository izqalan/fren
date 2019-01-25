package com.izqalan.messenger;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private ViewPager vp;
    private nPagerAdapter newPagerAdapter;

    private TabLayout newTabLayout;

    private String currentUser;

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // firebase stuff
        mAuth = FirebaseAuth.getInstance();


        // define instances for toolbar layout
        toolbar = findViewById(R.id.main_toolbar);
        setActionBar(toolbar);
        getActionBar().setTitle("Collab Kitchen");



        // tabs TABS disini !!
        vp = findViewById(R.id.tabs_pager);
        newPagerAdapter = new nPagerAdapter(getSupportFragmentManager());

        vp.setAdapter(newPagerAdapter);

        newTabLayout = findViewById(R.id.main_tabs);
        newTabLayout.setupWithViewPager(vp);

        // Floating action button
        fab = findViewById(R.id.main_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // checking permission & google services compatibility
                if(isServicesOK()) {
                    Intent intent = new Intent(MainActivity.this, EditPostActivity.class);
                    startActivity(intent);
                }
            }
        });
    }



    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){

            gotoStart();
        }


    }

    private void gotoStart() {
        Intent intent = new Intent(MainActivity.this, OnBoarding.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // include menu on the toolbar from /menu/main_menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }


    // when option from toolbar is selected
    // the 3 dots on the top right
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case (R.id.main_logout_btn):
                currentUser = mAuth.getCurrentUser().getUid();
                DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(currentUser).child("device_token");

                try {
                    userDatabase.removeValue();
                }catch (Exception e){
                    Log.e(TAG, e.getMessage());
                }
                FirebaseAuth.getInstance().signOut();
                gotoStart();
                break;

            case (R.id.main_settings_btn):
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;

            case (R.id.main_all_btn):
                Intent Uintent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(Uintent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}