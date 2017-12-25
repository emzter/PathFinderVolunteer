package com.emz.pathfindervolunteer.Utils;

import android.content.Context;
import android.util.Log;

import com.rw.velocity.Velocity;

import java.text.DecimalFormat;
import java.util.Locale;

public class Utils {
    public final String LOGIN_URL = "https://www.pathfinder.in.th/volunteer/login/";
    public final String REGISTER_URL = "https://www.pathfinder.in.th/volunteer/register/";
    public final String SET_DUTY_URL = "https://www.pathfinder.in.th/volunteer/duty/";
    public final String SET_LOCATION_URL = "https://www.pathfinder.in.th/volunteer/setLocation/";
    public final String SET_TOKEN_URL = "https://www.pathfinder.in.th/volunteer/setToken/";
    public final String UTILITIES_URL = "https://www.pathfinder.in.th/utilities/";
    public final String MAIN_URL = "https://www.pathfinder.in.th/volunteer/";
    public final String PROFILEPIC_URL = "https://www.pathfinder.in.th/uploads/volunteer_image/";

    private Context context;

    public Utils(Context current){
        this.context = current;
    }

    public void sendRegistrationToServer(String refreshedToken, Context context) {
        final String TAG = "MyFirebaseIDService";

        UserHelper usrHelper = new UserHelper(context);

        if(usrHelper.getLoginStatus()){
            String id = usrHelper.getUserId();
            Velocity.post(SET_TOKEN_URL)
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

    public String distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        float newdistane = (float) (distance * meterConversion);

        Log.d("DISTANCE", "DISTANCE: "+newdistane);

        return new DecimalFormat("#.00").format(newdistane);
    }
}
