package com.emz.pathfindervolunteer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.emz.pathfindervolunteer.Models.Users;
import com.emz.pathfindervolunteer.Utils.UserHelper;
import com.emz.pathfindervolunteer.Utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.rw.velocity.Velocity;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getName();

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggler;
    private NavigationView navigationView;
    private ProgressBar progressBar;
    private Switch onlineSwitch;
    private GoogleMap mMap;

    private UserHelper usrHelper;
    private Utils utils;
    private Users user;

    private ScheduledExecutorService executorService;
    private LocationManager lm;
    private Location location;
    private static Double longitude;
    private static Double latitude;

    private boolean online = false;
    private CircleImageView navProPic;
    private TextView navEMailText, navNameText;
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindView();

        utils = new Utils(this);
        usrHelper = new UserHelper(this);

        authCheck();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void authCheck() {
        if(usrHelper.getLoginStatus()){
            loadUserData();
        }else{
            startMainMenuActivity();
        }
    }

    private void startMainMenuActivity() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        Velocity.get(utils.MAIN_URL+"getUserDetail/"+usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        user = response.deserialize(Users.class);
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                } else {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    private void markMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
            return;
        }

        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else {
            LocationListener ln = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, ln);
        }

        LatLng latlng = new LatLng(latitude, longitude);
        if(myMarker != null){
            myMarker.setPosition(latlng);
        }else{
            myMarker = mMap.addMarker(new MarkerOptions().position(latlng).title("You're Here"));
            myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkersmall));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        scheduleRandomUpdates();
        markMap();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
        progressBar.setVisibility(View.GONE);
    }

    private void scheduleRandomUpdates() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                (MainActivity.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        markMap();
                        double newLat = latitude;
                        double newLng = longitude;
                        if(online){
                            updateLocation(newLat, newLng, usrHelper.getUserId());
                        }
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateLocation(double lat, double lng, String userId) {
        Velocity.post(utils.SET_LOCATION_URL)
                .withFormData("lat", String.valueOf(lat))
                .withFormData("lng", String.valueOf(lng))
                .withFormData("id", usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        Log.d(TAG, "LOCATION UPDATED");
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        Log.e(TAG, "LOCATION UPDATE ERROR");
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.nav_logout:
                onActionLogoutClicked();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void onActionLogoutClicked() {
        usrHelper.deleteSession();
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void bindView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggler = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggler);
        toggler.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);

        navProPic = navHeaderView.findViewById(R.id.navProfilePic);
        navEMailText = navHeaderView.findViewById(R.id.navEmailText);
        navNameText = navHeaderView.findViewById(R.id.navNameText);

        progressBar = findViewById(R.id.main_activity_progressBar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onlineSwitch = findViewById(R.id.online_switch);
        onlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    onlineDuty();
                }else{
                    offlineDuty();
                }
            }
        });
    }

    private void offlineDuty() {
        Velocity.post(utils.SET_DUTY_URL+"/offline/")
                .withFormData("id", usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
            @Override
            public void onVelocitySuccess(Velocity.Response response) {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();
                boolean status = jsonObject.get("status").getAsBoolean();

                if(status){
                    onlineSwitch.setText(R.string.offline_text);
                    onlineSwitch.setChecked(false);
                    online = false;
                }else{
                    onlineSwitch.setText(R.string.online_text);
                    onlineSwitch.setChecked(true);
                    online = true;
                }
            }

            @Override
            public void onVelocityFailed(Velocity.Response response) {
                onlineSwitch.setText(R.string.online_text);
                onlineSwitch.setChecked(true);
                online = true;
            }
        });
    }

    private void onlineDuty() {
        Velocity.post(utils.SET_DUTY_URL+"/online/")
                .withFormData("id", usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
            @Override
            public void onVelocitySuccess(Velocity.Response response) {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();
                boolean status = jsonObject.get("status").getAsBoolean();

                if(status){
                    onlineSwitch.setText(R.string.online_text);
                    onlineSwitch.setChecked(true);
                    online = true;
                }else{
                    onlineSwitch.setText(R.string.offline_text);
                    onlineSwitch.setChecked(false);
                    online = false;
                }
            }

            @Override
            public void onVelocityFailed(Velocity.Response response) {
                onlineSwitch.setText(R.string.offline_text);
                onlineSwitch.setChecked(false);
                online = false;
            }
        });
    }
}
