package com.izqalan.messenger;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;

    private AutoCompleteTextView searchText;
    private ImageView gpsImg;

    final String TAG = "Maps Activity";

    // set base location
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(3.1390029999999998, 101.686855), new LatLng(3.1390029999999998, 101.686855)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchText = findViewById(R.id.location_search);
        gpsImg = findViewById(R.id.ic_gps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

    }
    // required by google places API
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void init(){

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this,
                Places.getGeoDataClient(this), LAT_LNG_BOUNDS, null);

        // set adapter to auto complete textview

        searchText.setAdapter(placeAutocompleteAdapter);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                // must exclude keyEvent.getAction() == keyEvent.ACTION_DOWN
                // it will run geoLocate twice
                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == keyEvent.KEYCODE_ENTER)
                {
                    // execute search method
                    geoLocate();
                }

                return false;
            }
        });

        // Do we need this?
        gpsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void geoLocate(){

        Log.d(TAG, "geolocating: ");

        String search = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(search, 1);
        }catch (IOException e){
            Log.e(TAG, "IOExecption: " + e.getMessage());
        }

        if (list.size() > 0){
            // get the first position from the list
            final Address address = list.get(0);

            Log.d(TAG, "Found Location: "+ address.toString());


            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    15f, address.getAddressLine(0));

            // popup dialog to confirm meetup location

            final AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setTitle("Set Meetup Location");
            builder.setMessage(address.getAddressLine(0));

            builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // save location into firebase
                    String addressLine = address.getAddressLine(0);
                    Double Lat = address.getLatitude();
                    Double Lgn = address.getLongitude();
                    Log.d(TAG, "Location address: "+addressLine+ " "+ Lat+" "+Lgn);

                    // can be accessed in EditPostActivity
                    Intent intent = new Intent();
                    intent.putExtra("addressLine", addressLine);
                    intent.putExtra("lat", Lat);
                    intent.putExtra("lgn", Lgn);
                    setResult(Activity.RESULT_OK, intent);

                    finish();
                }
            });

            builder.show();


        }

    }

    private void moveCamera(LatLng latLng, float zoom, String title){

        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        // do not place pin on my current location
        if(!title.equals("My Location")){
            // place a pin onto the map
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

//        mMap.setMyLocationEnabled(true);

        // need permission handling to use init() here
//        init();

    }


}
