package com.emz.pathfindervolunteer.Utils;

import android.content.Context;
import android.util.Log;

import com.rw.velocity.Velocity;

public class Utils {
    public final String LOGIN_URL = "https://www.pathfinder.in.th/volunteer/login/";
    public final String REGISTER_URL = "https://www.pathfinder.in.th/volunteer/register/";
    public final String UTILITIES_URL = "https://www.pathfinder.in.th/utilities/";

    private Context context;

    public Utils(Context current){
        this.context = current;
    }

    public void sendRegistrationToServer(String refreshedToken, Context context) {
        final String TAG = "MyFirebaseIDService";

        UserHelper usrHelper = new UserHelper(context);

        if(usrHelper.getLoginStatus()){
            String id = usrHelper.getUserId();
            Velocity.post(UTILITIES_URL+"newtoken")
                    .withFormData("id", id)
                    .withFormData("token", refreshedToken)
                    .connect(new Velocity.ResponseListener() {
                        @Override
                        public void onVelocitySuccess(Velocity.Response response) {
                            Log.d(TAG, response.body);
                            Log.d(TAG, "Registered Token");
                        }

                        @Override
                        public void onVelocityFailed(Velocity.Response response) {
                            Log.e(TAG, response.body);
                            Log.e(TAG, "Failed to Registered Token");
                        }
                    });
        }
    }
}
