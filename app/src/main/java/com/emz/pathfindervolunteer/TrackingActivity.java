package com.emz.pathfindervolunteer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = TrackingActivity.class.getSimpleName();

    @BindView(R.id.order_name)
    TextView nameTv;

    @BindView(R.id.order_distance)
    TextView distanceTv;

    @BindView(R.id.order_profile_pic)
    CircleImageView proPic;

    private double latitude, longitude;
    private Orders orders;
    private OrderUser orderUser;

    private Marker myMarker, targetMaker;
    private GoogleMap mMap;
    private LatLng myLocation, targetLocation;
    private Utils utils;
    private Ui ui;
    private UserHelper usrHelper;

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
            latitude = getIntent().getExtras().getDouble("currentLat");
            longitude = getIntent().getExtras().getDouble("currentLng");

            setupView();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Intent returnIntent = new Intent(this, MainActivity.class);
            startActivity(returnIntent);
            finish();
        }
    }

    private void setupView() {
        Location myLoc = new Location("");
        myLoc.setLatitude(latitude);
        myLoc.setLongitude(longitude);

        Location targetLoc = new Location("");
        targetLoc.setLatitude(orders.getLat());
        targetLoc.setLongitude(orders.getLng());

        String distance = String.format(Locale.ENGLISH, "%.2f", myLoc.distanceTo(targetLoc) / 1000);

        nameTv.setText(orderUser.getFullName());
        distanceTv.setText(distance);
        Glide.with(this).load(utils.PROFILEPIC_URL + orderUser.getProPic()).into(proPic);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        myLocation = new LatLng(latitude, longitude);
        targetLocation = new LatLng(orders.getLat(), orders.getLng());

        Log.d(TAG, "MyLatLng: " + myLocation);
        Log.d(TAG, "TargetLatLng: " + targetLocation);

        myMarker = mMap.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.mymarkersmall)));
        targetMaker = mMap.addMarker(new MarkerOptions().position(targetLocation));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 16.0f));
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
}
