package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.rw.velocity.Velocity;

import io.nlopez.smartlocation.OnLocationUpdatedListener;

import de.hdodenhof.circleimageview.CircleImageView;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, OnLocationUpdatedListener {

    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout drawer;
    private TextView navNameText, navEMailText;
    private ProgressBar progressBar;
    private CircleImageView navProPic;
    private Switch onlineSwitch;

    private UserHelper usrHelper;
    private Utils utils;
    private Users user;

    private GoogleMap mMap;
    private Marker myMarker;

    private boolean online = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this);
        usrHelper = new UserHelper(this);
        bindView();
        authCheck();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        progressBar.setVisibility(View.GONE);
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(" ");

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggler = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggler);
        toggler.syncState();

        progressBar = findViewById(R.id.main_activity_progressBar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);

        navProPic = navHeaderView.findViewById(R.id.navProfilePic);
        navEMailText = navHeaderView.findViewById(R.id.navEmailText);
        navNameText = navHeaderView.findViewById(R.id.navNameText);

        onlineSwitch = findViewById(R.id.online_switch);
        onlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    onlineSwitch.setText(R.string.online_text);
                    onlineDuty();
                }else{
                    onlineSwitch.setText(R.string.offline_text);
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
                            user.setOnline(0);
                        }else{
                            onlineSwitch.setText(R.string.online_text);
                            onlineSwitch.setChecked(true);
                            online = true;
                            user.setOnline(1);
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        onlineSwitch.setText(R.string.online_text);
                        onlineSwitch.setChecked(true);
                        online = true;
                        user.setOnline(1);
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
                            user.setOnline(1);
                        }else{
                            onlineSwitch.setText(R.string.offline_text);
                            onlineSwitch.setChecked(false);
                            online = false;
                            user.setOnline(0);
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        onlineSwitch.setText(R.string.offline_text);
                        onlineSwitch.setChecked(false);
                        online = false;
                        user.setOnline(0);
                    }
                });
    }

    private void updateLocation(LatLng latlng) {
        Velocity.post(utils.SET_LOCATION_URL)
                .withFormData("lat", String.valueOf(latlng.latitude))
                .withFormData("lng", String.valueOf(latlng.longitude))
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

    private void markMap() {
        Location location = SmartLocation.with(this).location().getLastLocation();
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        if(myMarker != null){
            myMarker.setPosition(current);
        }else{
            myMarker = mMap.addMarker(new MarkerOptions().position(current));
            myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkersmall));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 16.0f));

        if(online){
            updateLocation(current);
        }
    }

    private void authCheck() {
        Log.d(TAG, "LOGIN: "+usrHelper.getLoginStatus());
        Log.d(TAG, "LOGIN-ID: "+usrHelper.getUserId());
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
                        Log.d(TAG, "USER: "+response.body);
                        user = response.deserialize(Users.class);
                        setupView();
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {

                    }
                });
    }

    private void setupView() {
        navEMailText.setText(user.getEmail());
        navNameText.setText(user.getName());
        Glide.with(navProPic.getContext()).load(utils.PROFILEPIC_URL+user.getProPic()).apply(RequestOptions.centerCropTransform().error(R.drawable.defaultprofilepicture)).into(navProPic);
        if(user.getOnline() == 0){
            online = false;
        }else if(user.getOnline() == 1){
            online = true;
        }
        onlineSwitch.setChecked(online);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(SmartLocation.with(this).location().state().locationServicesEnabled()){
            SmartLocation.with(this)
                    .location(new LocationGooglePlayServicesWithFallbackProvider(this))
                    .start(this);
            Log.d(TAG, "HAVELOCATIONSERVICE");
        }else{
            locationServiceUnavailabled();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SmartLocation.with(this)
                .location()
                .stop();
    }

    @Override
    public void onLocationUpdated(Location location) {
        markMap();
    }

    private void locationServiceUnavailabled() {
        Log.d(TAG, "NOLOCATIONSERVICE");
    }
}
