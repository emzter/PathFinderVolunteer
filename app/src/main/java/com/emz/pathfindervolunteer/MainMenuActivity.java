package com.emz.pathfindervolunteer;

import android.app.ActivityOptions;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CONSTANT) {
            this.finish();
        }
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
        Intent loginActivity = new Intent(MainMenuActivity.this, SignInActivity.class);
        startActivityForResult(loginActivity, ACTIVITY_CONSTANT);
    }

    private void startRegisterActivity() {
        Intent loginActivity = new Intent(MainMenuActivity.this, SignUpActivity.class);
        startActivityForResult(loginActivity, ACTIVITY_CONSTANT);
    }

}
