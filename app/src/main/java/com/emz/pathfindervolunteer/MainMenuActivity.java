package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ACTIVITY_CONSTANT = 0;

    private Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                startLoginActivity();
                break;
            case R.id.registerBtn:
                startRegisterActivity();
                break;
        }
    }

    private void startLoginActivity() {
        startActivityForResult(new Intent(MainMenuActivity.this, SignInActivity.class), ACTIVITY_CONSTANT);
    }

    private void startRegisterActivity() {
    }


}
