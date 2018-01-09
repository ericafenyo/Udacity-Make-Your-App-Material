package com.example.xyzreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by eric on 04/01/2018
 */

public class HelperMethods {

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static final String PREF_TEXT_SIZE = "PREF_TEXT_SIZE";



    public static void makeToast(Context context, Object message){
        Toast.makeText(context,String.valueOf(message),Toast.LENGTH_SHORT).show();
    }

    public static void storeTextPreferences(Context context, String key, int value) {
        preferences = context.getSharedPreferences(PREF_TEXT_SIZE, MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getTextPreferences(Context context, String key, int defaultValue) {
        preferences = context.getSharedPreferences(PREF_TEXT_SIZE, MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        assert connectivityManager != null;
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void makeSnack(View view, String message){
        Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show();
    }
}
