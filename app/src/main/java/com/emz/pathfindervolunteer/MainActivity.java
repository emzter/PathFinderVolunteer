package com.emz.pathfindervolunteer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.emz.pathfindervolunteer.Models.Orders;
import com.emz.pathfindervolunteer.Models.Users;
import com.emz.pathfindervolunteer.Models.VolunteerCategory;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.nlopez.smartlocation.OnLocationUpdatedListener;

import de.hdodenhof.circleimageview.CircleImageView;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, OnLocationUpdatedListener {

    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout drawer;
    private TextView navNameText;
    private TextView navEMailText;
    private ProgressBar progressBar;
    private CircleImageView navProPic;
    private Switch onlineSwitch;

    private UserHelper usrHelper;
    private Utils utils;
    private Users user;

    private GoogleMap mMap;
    private Marker myMarker;

    private boolean online = false;
    private double latitude;
    private double longitude;
    private List<VolunteerCategory> volCatList;
    private LinkedHashMap<Integer, Orders> orderLists;

    private ScheduledExecutorService orderExcutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        volCatList = new ArrayList<>();

        utils = new Utils(this);
        usrHelper = new UserHelper(this);
        orderLists = new LinkedHashMap<>();

        bindView();
        authCheck();
        getAllCat();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
        if(SmartLocation.with(this).location().state().locationServicesEnabled()){
            Log.d(TAG, "LocationService: Found");
            SmartLocation.with(this)
                    .location()
                    .config(LocationParams.NAVIGATION)
                    .start(this);
        }else{
            Log.e(TAG, "LocationService: None");
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

    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        markMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_settings:
                onActionSettingsClicked();
                break;
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

    private void onActionSettingsClicked() {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivity(intent);
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
        ArrayList<String> options = new ArrayList<>();
        for (VolunteerCategory vol : volCatList) {
            options.add(vol.getName());
        }
        new MaterialDialog.Builder(this)
                .title("GO ONLINE")
                .cancelable(false)
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onlineSwitch.setChecked(false);
                    }
                })
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        setOnline(position);
                    }
                })
                .show();
    }

    private void setOnline(final int position) {
        Velocity.post(utils.SET_DUTY_URL+"/online/")
                .withFormData("id", usrHelper.getUserId())
                .withFormData("category", String.valueOf(volCatList.get(position).getId()))
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
                            user.setCategory(volCatList.get(position).getId());
                            user.setOnline(1);
                            checkForOrder();
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

    private void checkForOrder() {
//        ORDER STATUS (0: Placing, 1: Accepted, 2: On Duty, 3: Completed, 4: Canceled)
        orderExcutorService = Executors.newSingleThreadScheduledExecutor();
        orderExcutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Velocity.get(utils.MAIN_URL+"getAllOrder"+user.getCategory())
                                .connect(new Velocity.ResponseListener() {
                                    @Override
                                    public void onVelocitySuccess(Velocity.Response response) {
                                        Gson gson = new Gson();
                                        JsonParser parser = new JsonParser();
                                        JsonArray jsonArray = parser.parse(response.body).getAsJsonArray();

                                        for (int i = 0; i < jsonArray.size(); i++) {
                                            JsonElement mJson = jsonArray.get(i);
                                            Orders order = gson.fromJson(mJson, Orders.class);
                                            orderLists.put(order.getId(), order);
                                        }

                                        //TODO: Show order
                                    }

                                    @Override
                                    public void onVelocityFailed(Velocity.Response response) {

                                    }
                                });
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
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
        latitude = location != null ? location.getLatitude() : 0;
        longitude = location != null ? location.getLongitude() : 0;
        LatLng current = new LatLng(latitude, longitude);
        Log.d(TAG, "CURRENTLOCATION: "+current);
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

    private void locationServiceUnavailabled() {
        new MaterialDialog.Builder(this)
                .title("Use Google's Location Services?")
                .content("Let Google help apps determine location. This means sending anonymous location data to Google, even when no apps are running.")
                .positiveText("Agree")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .negativeText("Disagree")
                .show();
    }

    private void getAllCat() {
        Velocity.get(utils.MAIN_URL+"getAllCat")
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        Gson gson = new Gson();
                        JsonParser parser = new JsonParser();
                        JsonArray jsonArray = parser.parse(response.body).getAsJsonArray();

                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonElement mJson = jsonArray.get(i);
                            VolunteerCategory cat = gson.fromJson(mJson, VolunteerCategory.class);
                            volCatList.add(cat);
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        //TODO: Can't Connect to the Server
                    }
                });
    }
}
