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
import android.widget.EditText;

import com.emz.pathfindervolunteer.Utils.Ui;
import com.emz.pathfindervolunteer.Utils.UserHelper;
import com.emz.pathfindervolunteer.Utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText emailTv, passTv, cpassTv, fnameTv, lnameTv;
    private String email, pass, cpass, fname, lname;
    private Button registerBtn;
    private Toolbar toolbar;

    private Utils utils;
    private Ui ui;

    private boolean valid;
    private UserHelper usrHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        overridePendingTransition( R.anim.trans_left_in, R.anim.trans_left_out);

        Velocity.initialize(3);

        utils = new Utils(this);
        ui = new Ui(this);
        usrHelper = new UserHelper(this);

        authCheck();

        toolbar = findViewById(R.id.toolbar);
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
            case R.id.btn_register:
                register();
                break;
        }
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

    private void bindView() {
        emailTv = findViewById(R.id.register_input_email);
        passTv = findViewById(R.id.register_input_password);
        cpassTv = findViewById(R.id.register_input_confirm_password);
        fnameTv = findViewById(R.id.register_input_name);
        lnameTv = findViewById(R.id.register_input_lastname);
        registerBtn = findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(this);
    }

    private void register() {
        convertRegisterInfo();

        if (!validate()) {
            onRegisterFailed(0);
            return;
        }

        registerBtn.setEnabled(false);
        ui.createProgressDialog(getString(R.string.registering));

        registerUser(fname, lname, email, pass);
    }

    private void convertRegisterInfo() {
        email = emailTv.getText().toString();
        pass = passTv.getText().toString();
        cpass = cpassTv.getText().toString();
        fname = fnameTv.getText().toString();
        lname = lnameTv.getText().toString();
    }

    private void registerUser(String fname, String lname, String email, String pass) {
        Velocity.post(utils.REGISTER_URL)
                .withFormData("email",email)
                .withFormData("fname",fname)
                .withFormData("lname",lname)
                .withFormData("password",pass)
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();

                        if(status){
                            onRegisterSuccess();
                        }else{
                            int error = jsonObject.get("error").getAsInt();
                            onRegisterFailed(error);
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {
                        onRegisterFailed(2);
                    }
                });
    }

    private void authUser(String email, String password) {
        Velocity.post(utils.LOGIN_URL)
                .withFormData("email", email)
                .withFormData("password", password)
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();

                        if(status){
                            String uid = jsonObject.get("uid").getAsString();
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

    private boolean validate(){
        valid = true;

        checkEmail();
        checkName();
        checkPassword();

        return valid;
    }

    private void checkEmail(){
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTv.setError(getString(R.string.email_error));
            valid = false;
        } else {
            emailTv.setError(null);
        }
    }

    private void checkPassword(){
        if (pass.isEmpty() || pass.length() < 8) {
            passTv.setError(getString(R.string.password_error));
            valid = false;
        } else {
            passTv.setError(null);
        }

        if(!Objects.equals(cpass, pass)){
            cpassTv.setError(getString(R.string.confirm_password_error));
            valid = false;
        } else {
            cpassTv.setError(null);
        }
    }

    private void checkName(){
        if (fname.isEmpty()){
            fnameTv.setError(getString(R.string.no_name_enter_error));
            valid = false;
        } else {
            fnameTv.setError(null);
        }

        if (lname.isEmpty()){
            lnameTv.setError(getString(R.string.no_lastname_enter_error));
            valid = false;
        } else {
            lnameTv.setError(null);
        }
    }

    private void onRegisterFailed(int stage) {
        View view = findViewById(R.id.register_view);

        if(stage == 0){
            ui.createSnackbar(view, getString(R.string.something_went_wrong));
        }else if(stage == 1){
            ui.createSnackbar(view, getString(R.string.email_already_used));
        }else if(stage == 2){
            ui.createSnackbar(view, getString(R.string.connection_error));
        }

        registerBtn.setEnabled(true);
        ui.dismissProgressDialog();
    }

    private void onRegisterSuccess(){
        ui.dismissProgressDialog();
        ui.createProgressDialog(getString(R.string.authenticating));
        authUser(email, pass);
    }

    private void onLoginSuccess(String uid){
        ui.dismissProgressDialog();
        usrHelper.createSession(uid);
        startMainActivity();
    }

    private void startMainActivity() {
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
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
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        finish();
    }
}
