package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startLoginActivity();
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