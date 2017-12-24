package com.emz.pathfindervolunteer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.emz.pathfindervolunteer.Models.OrderUser;
import com.emz.pathfindervolunteer.Models.Orders;
import com.emz.pathfindervolunteer.Utils.Ui;
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

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback, OnLocationUpdatedListener {

    private static final String TAG = TrackingActivity.class.getSimpleName();

    @BindView(R.id.order_name)
    TextView nameTv;

    @BindView(R.id.order_profile_pic)
    CircleImageView proPic;

    private double latitude, longitude;
    private Orders orders;
    private OrderUser orderUser;

    private Location currentLocation;

    private GoogleMap mMap;
    private Utils utils;
    private Ui ui;
    private UserHelper usrHelper;
    private ScheduledExecutorService orderExcutorService;
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        ButterKnife.bind(this);
        utils = new Utils(this);
        ui = new Ui(this);
        usrHelper = new UserHelper(this);

        if (getIntent().getExtras() != null) {
            orders = (Orders) getIntent().getExtras().get("orders");
            orderUser = (OrderUser) getIntent().getExtras().get("orderUser");

            setupView();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Intent returnIntent = new Intent(this, MainActivity.class);
            startActivity(returnIntent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    @Override
    protected void onStop() {
        super.onStop();
        SmartLocation.with(this)
                .location()
                .stop();
    }

    private void setupView() {
        trackOrder();
        nameTv.setText(orderUser.getFullName());
        Glide.with(this).load(utils.PROFILEPIC_URL + orderUser.getProPic()).into(proPic);
    }

    private void updateLocation(LatLng myLocation) {
        Velocity.post(utils.SET_LOCATION_URL)
                .withFormData("lat", String.valueOf(myLocation.latitude))
                .withFormData("lng", String.valueOf(myLocation.longitude))
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

    private void trackOrder() {
        orderExcutorService = Executors.newSingleThreadScheduledExecutor();
        orderExcutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Velocity.get(utils.MAIN_URL+"checkOrderStatus/"+orders.getId())
                                .connect(new Velocity.ResponseListener() {
                                    @Override
                                    public void onVelocitySuccess(Velocity.Response response) {
                                        JsonParser parser = new JsonParser();
                                        JsonObject jsonObj = parser.parse(response.body).getAsJsonObject();

                                        boolean status = jsonObj.get("status").getAsBoolean();
                                        if(!status){
                                            if(orderExcutorService != null){
                                                orderExcutorService.shutdownNow();
                                            }
                                            int now = jsonObj.get("now").getAsInt();
                                            if(now == 4){
                                                new MaterialDialog.Builder(TrackingActivity.this)
                                                        .title("Sorry! The order has been cancelled.")
                                                        .content("The order has been cancelled.")
                                                        .positiveText("OK")
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                Intent returnIntent = new Intent(TrackingActivity.this, MainActivity.class);
                                                                startActivity(returnIntent);
                                                                finish();
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }

                                        Log.d(TAG, "VOLUNTEER-TRACKING: Status Checking");
                                    }

                                    @Override
                                    public void onVelocityFailed(Velocity.Response response) {
                                        //TODO: Connect Failed
                                    }
                                });
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng targetLocation = new LatLng(orders.getLat(), orders.getLng());
        Log.d(TAG, "TargetLatLng: " + targetLocation);
        Marker targetMaker = mMap.addMarker(new MarkerOptions().position(targetLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 16.0f));
    }

    public void markMap(){
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        if(myMarker != null){
            myMarker.setPosition(myLocation);
        }else{
            myMarker = mMap.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkersmall)));
        }

        updateLocation(myLocation);

        Log.d(TAG, "MyLatLng: " + myLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callUser();
                }else{
                    //TODO: Show dialog for no permission grant
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @OnClick(R.id.btn_call)
    public void callUser() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + orderUser.getTelephone()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }else{
            startActivity(intent);
        }
    }

    @OnClick(R.id.btn_message)
    public void messageUser(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + orderUser.getTelephone()));
        startActivity(intent);
    }

    @OnClick(R.id.btn_direction)
    public void getDirection(){
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+orders.getLat()+","+orders.getLng());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @OnClick(R.id.btn_cancel)
    public void cancelOrder(){
        new MaterialDialog.Builder(this)
                .title(R.string.cancel_order_dialog_title)
                .content(R.string.cancel_order_dialog_content)
                .negativeText(R.string.no_btn)
                .positiveText(R.string.yes_btn)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final MaterialDialog md = new MaterialDialog.Builder(TrackingActivity.this)
                                .title(R.string.progress_dialog_title)
                                .content(R.string.calceling)
                                .progress(true, 0)
                                .cancelable(false)
                                .show();

                        Velocity.post(utils.MAIN_URL+"setOrderStatus/"+orders.getId())
                                .withFormData("status", "4")
                                .withFormData("volunteer_id", usrHelper.getUserId())
                                .connect(new Velocity.ResponseListener() {
                                    @Override
                                    public void onVelocitySuccess(Velocity.Response response) {
                                        JsonParser parser = new JsonParser();
                                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                                        boolean status = jsonObject.get("status").getAsBoolean();
                                        if(status){
                                            if(orderExcutorService != null){
                                                orderExcutorService.shutdownNow();
                                            }

                                            Intent returnIntent = new Intent(TrackingActivity.this, MainActivity.class);
                                            startActivity(returnIntent);
                                            finish();
                                        }else{
                                            View v = findViewById(R.id.tracking_view);
                                            ui.createSnackbar(v, getString(R.string.cant_cancel_order_message));
                                        }

                                        md.dismiss();
                                    }

                                    @Override
                                    public void onVelocityFailed(Velocity.Response response) {
                                        //TODO: show dialog cant connect
                                    }
                                });
                    }
                })
                .show();
    }

    @OnClick(R.id.btn_pickup)
    public void pickupUser(){

        final MaterialDialog md = new MaterialDialog.Builder(TrackingActivity.this)
                .title(R.string.progress_dialog_title)
                .content(R.string.starting_order)
                .progress(true, 0)
                .cancelable(false)
                .show();

        Velocity.post(utils.MAIN_URL+"setOrderStatus/"+orders.getId())
                .withFormData("status", "2")
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();
                        if(status){
                            if(orderExcutorService != null){
                                orderExcutorService.shutdownNow();
                            }

                            Intent intent = new Intent(TrackingActivity.this, OnDutyActivity.class);
                            intent.putExtra("orders", orders);
                            intent.putExtra("orderUser", orderUser);
                            startActivity(intent);
                            finish();
                        }else{
                            View v = findViewById(R.id.tracking_view);
                            ui.createSnackbar(v, getString(R.string.cant_cancel_order_message));
                        }

                        md.dismiss();
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        //TODO: show dialog cant connect
                    }
                });
    }

    @Override
    public void onLocationUpdated(Location location) {
        currentLocation = location;
        markMap();
    }
}
