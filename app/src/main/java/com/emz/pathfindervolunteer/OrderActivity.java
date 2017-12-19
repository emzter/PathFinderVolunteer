package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emz.pathfindervolunteer.Models.OrderUser;
import com.emz.pathfindervolunteer.Models.Orders;
import com.emz.pathfindervolunteer.Utils.UserHelper;
import com.emz.pathfindervolunteer.Utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

public class OrderActivity extends AppCompatActivity {

    private final String TAG = OrderActivity.class.getSimpleName();

    @BindView(R.id.order_profile_pic)
    CircleImageView proPic;

    @BindView(R.id.order_name)
    TextView nameTv;

    @BindView(R.id.order_distance)
    TextView distanceTv;

    @BindView(R.id.order_email)
    TextView emailTv;

    @BindView(R.id.order_tel)
    TextView telTv;

    @BindView(R.id.order_geo_title)
    TextView geoCodeTitleTv;

    @BindView(R.id.order_geo_subtitle)
    TextView getGeoCodeSubtitleTv;

    @BindView(R.id.accept_btn_cd)
    TextView acceptCdTv;

    @BindView(R.id.order_detail)
    RelativeLayout orderDetail;

    @BindView(R.id.order_activity_progressBar)
    ProgressBar progressBar;

    private Orders orders;
    private Utils utils;
    private OrderUser orderUser;

    private Location location;
    private UserHelper usrHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ButterKnife.bind(this);
        utils = new Utils(this);
        usrHelper = new UserHelper(this);

        orderDetail.setVisibility(View.GONE);

        if(getIntent().getExtras() != null){
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            orders = (Orders) bundle.getSerializable("order");
            location = (Location) bundle.get("location");
            loadUserDetail();
        }else{
            Intent returnIntent = getIntent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = getIntent();
        setResult(RESULT_CANCELED, returnIntent);
        super.onBackPressed();
    }

    private void setupView() {
        Location targetLoc = new Location("");
        targetLoc.setLatitude(orders.getLat());
        targetLoc.setLongitude(orders.getLng());

        String distance = String.format(Locale.ENGLISH,"%.2f", targetLoc.distanceTo(location) / 1000);

        Log.d(TAG, "GEOCODING: "+distance);

        nameTv.setText(orderUser.getFullName());
        emailTv.setText(orderUser.getEmail());
        telTv.setText(orderUser.getTelephone());
        distanceTv.setText(distance);
        Glide.with(this).load(utils.PROFILEPIC_URL+orderUser.getProPic()).into(proPic);


        SmartLocation.with(this)
                .geocoding()
                .reverse(targetLoc, new OnReverseGeocodingListener() {
                    @Override
                    public void onAddressResolved(Location location, List<Address> list) {
                        geoCodeTitleTv.setText(list.get(0).getThoroughfare());
                        getGeoCodeSubtitleTv.setText(list.get(0).getAddressLine(0));
                    }
                });

        orderDetail.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                acceptCdTv.setText(String.valueOf((l - 100) / 1000));
            }

            @Override
            public void onFinish() {
                acceptCdTv.setText(String.valueOf(0));
                Intent returnIntent = getIntent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }.start();
    }

    private void loadUserDetail(){
        Velocity.get(utils.UTILITIES_URL+"getProfile/"+orders.getUserId())
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        orderUser = response.deserialize(OrderUser.class);
                        setupView();
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        Log.e("TEST", String.valueOf(R.string.no_internet_connection));
                    }
                });
    }

    @OnClick(R.id.accept_btn)
    public void acceptOrder(){
        Velocity.post(utils.MAIN_URL+"setOrderStatus/"+orders.getId())
                .withFormData("volunteer_id", usrHelper.getUserId())
                .withFormData("status", "1")
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();
                        if(status){
                            Intent returnIntent = getIntent();
                            returnIntent.putExtra("order", orders);
                            returnIntent.putExtra("orderuser", orderUser);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }else{
                            //TODO: Show dialog if not success
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        //TODO: Can't Connect to the Server
                    }
                });
    }
}