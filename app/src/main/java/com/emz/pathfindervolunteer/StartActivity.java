package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.emz.pathfindervolunteer.Utils.UserHelper;

public class StartActivity extends AppCompatActivity {

    private UserHelper usrHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        usrHelper = new UserHelper(this);

        authCheck();
    }

    private void authCheck() {
        if(usrHelper.getLoginStatus()){
            startMainActivity();
        }else{
            startLoginActivity();
        }
    }

    private void startMainActivity() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }

    private void startLoginActivity() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, MainMenuActivity.class));
                finish();
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }
}