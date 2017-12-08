package com.emz.pathfindervolunteer.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserHelper {
    private Context context;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;

    private static String prefsName = "CUser";
    private static int prefsMode = 0;

    public UserHelper(Context context){
        this.context = context;
        this.sharedPrefs = this.context.getSharedPreferences(prefsName, prefsMode);
        this.editor = sharedPrefs.edit();
    }

    public void createSession(String uid){
        editor.putBoolean("LoginStatus", true);
        editor.putString("user_id", uid);

        editor.commit();
    }

    public void deleteSession(){
        editor.clear();
        editor.commit();
    }

    public boolean getLoginStatus(){
        return sharedPrefs.getBoolean("LoginStatus", false);
    }

    public String getUserId(){
        return sharedPrefs.getString("user_id", null);
    }
}
