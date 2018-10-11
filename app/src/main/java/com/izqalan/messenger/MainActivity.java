package com.izqalan.messenger;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    private ViewPager vp;
    private nPagerAdapter newPagerAdapter;

    private TabLayout newTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // firebase stuff
        mAuth = FirebaseAuth.getInstance();

        // define instances for toolbar layout
        toolbar = findViewById(R.id.main_toolbar);
        setActionBar(toolbar);
        getActionBar().setTitle("TBD Messenger");

        // tabs
        vp = findViewById(R.id.tabs_pager);
        newPagerAdapter = new nPagerAdapter(getSupportFragmentManager());

        vp.setAdapter(newPagerAdapter);

        newTabLayout = findViewById(R.id.main_tabs);
        newTabLayout.setupWithViewPager(vp);

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
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case (R.id.main_logout_btn):
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
}