package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnDutyActivity extends AppCompatActivity {

    private static final String TAG = OnDutyActivity.class.getSimpleName();
    @BindView(R.id.order_name)
    TextView nameTv;
    @BindView(R.id.order_status)
    TextView statusTv;
    @BindView(R.id.order_profile_pic)
    CircleImageView proPic;

    private Utils utils;
    private Ui ui;

    private Orders orders;
    private OrderUser orderUser;
    private UserHelper usrHelper;

    private ScheduledExecutorService orderExcutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_duty);

        ButterKnife.bind(this);
        utils = new Utils(this);
        ui = new Ui(this);
        usrHelper = new UserHelper(this);

        if (getIntent().getExtras() != null) {
            orders = (Orders) getIntent().getExtras().get("orders");
            orderUser = (OrderUser) getIntent().getExtras().get("orderUser");

            setupView();
        } else {
            Intent returnIntent = new Intent(this, MainActivity.class);
            startActivity(returnIntent);
            finish();
        }
    }

    private void setupView() {
        trackOrder();
        nameTv.setText(orderUser.getFullName());
        statusTv.setText(R.string.on_duty);
        Glide.with(this).load(utils.PROFILEPIC_URL + orderUser.getProPic()).into(proPic);
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
                                            int now = jsonObj.get("now").getAsInt();
                                            if(now == 4){
                                                orderExcutorService.shutdownNow();
                                                new MaterialDialog.Builder(OnDutyActivity.this)
                                                        .title("Sorry! The order has been cancelled.")
                                                        .content("The order has been cancelled.")
                                                        .positiveText("OK")
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                Intent returnIntent = new Intent(OnDutyActivity.this, MainActivity.class);
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

    @OnClick(R.id.complete_button)
    public void completeJob(){
        if(orderExcutorService != null){
            orderExcutorService.shutdownNow();
        }
        statusTv.setText(R.string.completing);
        final MaterialDialog md = new MaterialDialog.Builder(OnDutyActivity.this)
                .title(R.string.progress_dialog_title)
                .content(R.string.completing)
                .progress(true, 0)
                .cancelable(false)
                .show();

        Velocity.post(utils.MAIN_URL+"setOrderStatus/"+orders.getId())
                .withFormData("status", "3")
                .withFormData("volunteer_id", usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();
                        if(status){
                            Intent returnIntent = new Intent(OnDutyActivity.this, MainActivity.class);
                            startActivity(returnIntent);
                            finish();
                        }else{
                            View v = findViewById(R.id.tracking_view);
                            ui.createSnackbar(v, getString(R.string.error_completing_order));
                        }

                        md.dismiss();
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        //TODO: show dialog cant connect
                    }
                });
    }

    @OnClick(R.id.cancel_btn)
    public void cancelJob(){
        if(orderExcutorService != null){
            orderExcutorService.shutdownNow();
        }
        new MaterialDialog.Builder(this)
                .title(R.string.cancel_order_dialog_title)
                .content(R.string.cancel_order_dialog_content)
                .negativeText(R.string.no_btn)
                .positiveText(R.string.yes_btn)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final MaterialDialog md = new MaterialDialog.Builder(OnDutyActivity.this)
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
                                            Intent returnIntent = new Intent(OnDutyActivity.this, MainActivity.class);
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
}
