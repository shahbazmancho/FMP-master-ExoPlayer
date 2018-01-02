package com.dozydroid.fmp.utilities;

/**
 * Created by MIRSAAB on 11/8/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Kamran on 8/1/2015.
 */
public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;


    // Editor for Shared preferences
    SharedPreferences.Editor editor;


    // Context
    Context _context;

    // Temp Context
    Context _tempContext;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "FMP";

//    // All Shared Preferences Keys
//    private static final String IS_LOGIN = "IsLoggedIn";
//
//    // TEMP login status
//    private static final String IS_TEMP_LOGIN = "IsLoggedIn";
//
//    // User name (make variable public to access from outside)
//    public static final String KEY = "name";
//
//    // User id (make variable public to access from outside)
//    public static final String KEY_ID = "0";

    public static final String KEY_PATH = "path";
    public static final String KEY_TITLE = "title";
    public static final String KEY_VIEW = "view";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void saveRecent(String path, String title){
        editor.putString(KEY_PATH, path);
        editor.putString(KEY_TITLE, title);
        editor.commit();
    }

    public void setViewMode(String view){
        editor.putString(KEY_VIEW, view);
        editor.commit();
    }

    public String getViewMode(){
        return pref.getString(KEY_VIEW, "");
    }

    public HashMap<String, String> getRecent(){
        HashMap<String, String> recentVideo = new HashMap<String, String>();

        recentVideo.put(KEY_PATH, pref.getString(KEY_PATH, null));
        recentVideo.put(KEY_TITLE, pref.getString(KEY_TITLE, null));

        return recentVideo;
    }
}