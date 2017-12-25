package com.emz.pathfindervolunteer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.emz.pathfindervolunteer.Models.Users;
import com.emz.pathfindervolunteer.Utils.Ui;
import com.emz.pathfindervolunteer.Utils.UserHelper;
import com.emz.pathfindervolunteer.Utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rw.velocity.Velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    private CircleImageView profilePic;
    private EditText nameEt, lastnameEt;
    private Button updateBtn;

    private Users user;
    private Utils utils;
    private UserHelper usrHelper;
    private Ui ui;

    private View mainView;

    File myImageFile;

    boolean hasfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        utils = new Utils(this);
        usrHelper = new UserHelper(this);
        ui = new Ui(this);

        bindView();

        loadProfile();
    }

    private void bindView() {
        profilePic = findViewById(R.id.profilePic);
        nameEt = findViewById(R.id.name_input);
        lastnameEt = findViewById(R.id.lastname_input);
        updateBtn = findViewById(R.id.btn_update_profile);
        mainView = findViewById(R.id.update_profile_root_view);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openChooserWithGallery(EditProfileActivity.this, "Choose your picture", 0);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    private void updateProfile() {
        final MaterialDialog md = new MaterialDialog.Builder(EditProfileActivity.this)
                .title(R.string.progress_dialog_title)
                .content(R.string.completing)
                .progress(true, 0)
                .cancelable(false)
                .show();

        String firstname = nameEt.getText().toString();
        String lastname = lastnameEt.getText().toString();

        InputStream inputStream = null;

        if(myImageFile != null){
            hasfile = true;
            try {
                inputStream = new FileInputStream(myImageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(myImageFile != null){

            String fileName = myImageFile.getName();
            Log.d(TAG, "UPDATEWITHFILE: "+fileName);

            Velocity.upload(utils.MAIN_URL+"editProfilePic/"+usrHelper.getUserId()+"/"+fileName)
                    .setUploadSource("file", "image/*", myImageFile.getAbsolutePath())
                    .connect(new Velocity.ResponseListener() {
                        @Override
                        public void onVelocitySuccess(Velocity.Response response) {
                            md.dismiss();

                            Log.d(TAG, "UPDATEWITHFILE: "+response.body);

                            JsonParser parser = new JsonParser();
                            JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                            boolean status = jsonObject.get("status").getAsBoolean();
                            if(status){
                                ui.createSnackbar(mainView, "Upload Success!");
                                hasfile = false;
                            }
                        }

                        @Override
                        public void onVelocityFailed(Velocity.Response response) {
                            md.dismiss();

                            Log.e(TAG, "UPDATEWITHFILE: "+response.body);
                            Log.e(TAG, "UPDATEWITHFILE: "+response.requestMethod);
                            Log.e(TAG, "UPDATEWITHFILE: ERROR");
                        }
                    });
        }

        Velocity.post(utils.MAIN_URL+"editProfile/"+usrHelper.getUserId())
                .withFormData("firstname", firstname)
                .withFormData("lastname", lastname)
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        if(!hasfile){
                            md.dismiss();
                        }

                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response.body).getAsJsonObject();

                        boolean status = jsonObject.get("status").getAsBoolean();
                        if(status){
                            ui.createSnackbar(mainView, "Update Success!");
                        }
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {

                        if(!hasfile){
                            md.dismiss();
                        }

                        Log.e(TAG, "UPDATENOFILE: "+response.requestUrl);
                        Log.e(TAG, "UPDATENOFILE: "+response.body);
                        Log.e(TAG, "UPDATENOFILE: "+response.requestBody);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                Glide.with(EditProfileActivity.this).load(imageFile).into(profilePic);
                myImageFile = imageFile;
            }
        });
    }

    private void loadProfile(){
        Velocity.get(utils.MAIN_URL+"getUserDetail/"+usrHelper.getUserId())
                .connect(new Velocity.ResponseListener() {
                    @Override
                    public void onVelocitySuccess(Velocity.Response response) {
                        Log.d(TAG, "USER: "+response.body);
                        user = response.deserialize(Users.class);
                        setupView();
                    }

                    @Override
                    public void onVelocityFailed(Velocity.Response response) {

                    }
                });
    }

    private void setupView() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameEt.setText(user.getFirstName());
        lastnameEt.setText(user.getLastName());

        Glide.with(this).load(utils.PROFILEPIC_URL+user.getProPic()).apply(RequestOptions.centerCropTransform().error(R.drawable.defaultprofilepicture)).into(profilePic);

    }
}
