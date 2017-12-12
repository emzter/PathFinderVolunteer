package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emz.pathfindervolunteer.Utils.Ui;
import com.emz.pathfindervolunteer.Utils.UserHelper;
import com.emz.pathfindervolunteer.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private TextView emailTv, passTv;
    private Button loginBtn;
    private String email, pass;

    private Utils utils;
    private Ui ui;

    private boolean valid;
    private UserHelper usrHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        overridePendingTransition( R.anim.trans_left_in, R.anim.trans_left_out);

        utils = new Utils(this);
        ui = new Ui(this);
        usrHelper = new UserHelper(this);

        authCheck();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bindView();
    }

    private void authCheck() {
        if(usrHelper.getLoginStatus()){
            startMainActivity();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void bindView() {
        emailTv = findViewById(R.id.login_input_email);
        passTv = findViewById(R.id.login_input_password);
        loginBtn = findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = getIntent();
        setResult(RESULT_CANCELED, returnIntent);

        super.onBackPressed();

        overridePendingTransition( R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition( R.anim.trans_right_in, R.anim.trans_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void login() {

        email = emailTv.getText().toString();
        pass = passTv.getText().toString();

        loginBtn.setEnabled(false);

        if (!validate()) {
            onLoginFailed(0);
            return;
        }

        ui.createProgressDialog(getString(R.string.authenticating));

        Velocity.post(utils.LOGIN_URL)
                .withFormData("email", email)
                .withFormData("password", pass)
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();

                        if(status){
                            String uid = jsonObject.get("uid").getAsString();
                            Log.d(TAG, "LOGIN SUCCESS");
                            onLoginSuccess(uid);
                        }else{
                            int error = jsonObject.get("error").getAsInt();
                            onLoginFailed(error);
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        onLoginFailed(2);
                    }
                });
    }

    private void onLoginSuccess(String uid){
        ui.dismissProgressDialog();
        usrHelper.createSession(uid);
        startMainActivity();
    }

    private void startMainActivity() {
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    private void onLoginFailed(int stage) {
        View view = findViewById(R.id.login_view);
        if (stage == 0) {
            ui.createSnackbar(view, getString(R.string.auth_failed));
        } else if (stage == 1) {
            ui.createSnackbar(view, getString(R.string.no_user_found));
        } else if (stage == 2) {
            ui.createSnackbar(view, getString(R.string.connection_error));
        }
        ui.dismissProgressDialog();
        loginBtn.setEnabled(true);
    }

    private boolean validate() {
        valid = true;

        checkEmail();
        checkPassword();

        return valid;
    }

    private void checkPassword() {
        if (pass.isEmpty() || pass.length() < 8) {
            passTv.setError(getString(R.string.password_error));
            valid = false;
        } else {
            passTv.setError(null);
        }
    }

    private void checkEmail() {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTv.setError(getString(R.string.email_error));
            valid = false;
        } else {
            emailTv.setError(null);
        }
    }
}