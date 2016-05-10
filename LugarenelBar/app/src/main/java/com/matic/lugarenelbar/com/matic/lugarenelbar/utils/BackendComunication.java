package com.matic.lugarenelbar.com.matic.lugarenelbar.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;
import com.matic.lugarenelbar.Bar;
import com.matic.lugarenelbar.MainActivity;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Guillermo on 30/4/2016.
 */
public class BackendComunication {


    static final Random random = new Random();
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    String regid;
    String SENDER_ID = "582261806720";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "0.9";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    private static class BackendComunicationLoader {

        private static final BackendComunication INSTANCE = new BackendComunication();
    }

    private BackendComunication() {
        if (BackendComunicationLoader.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
        //userManager = UserManager.getInstance();
        //ccsClient = SmackCcsClient.getInstance();
        //assert null != ccsClient;
    }

    public static BackendComunication getInstance() {
        return BackendComunicationLoader.INSTANCE;
    }

      /*
    METODOS PARA REGISTRARSE CON GCM Y ENVIAR EL REGISTRO AL SERVIDOR
     */

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground(final Activity activity) {
        if (checkPlayServices(activity)) {
            gcm = GoogleCloudMessaging.getInstance(activity);
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";

                    context = activity.getApplicationContext();

                    if (regid == null || regid.isEmpty()) {
                        regid = BackendComunication.getInstance().getRegistrationId(activity, context);
                    }

                    try {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(context);
                        }
                        regid = gcm.register(SENDER_ID);
                        msg = "Device registered, registration ID=" + regid;

                        storeRegistrationId(activity, context, regid);

                        // Should send the registration ID to your server over
                        // HTTP, so it can use GCM/HTTP or CCS to send messages
                        // to your app.
                        sendRegistrationIdToBackend();

                    } catch (IOException ex) {
                        msg = "Failed to register: "
                                + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.i(Constants.TAG, msg);
                }
            }.execute(null, null, null);
        } else {
            Log.i(Constants.TAG, "No valid Google Play Services APK found.");
        }
    }


    /**
     * Gets the current registration ID for application on GCM service, if there
     * is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId(Activity activity, Context context) {
        final SharedPreferences prefs = getGcmPreferences(activity, context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Constants.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Constants.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app.
     */
    public void sendRegistrationIdToBackend() {
        try {
            Bundle data = new Bundle();
            data.putString("message", "Registration ID");
            data.putString("name", regid);
            data.putString("action", Constants.ACTION_REGISTER_USER);

            String id = Integer.toString(msgId.incrementAndGet());

            Log.i("send", "mandando accion " + Constants.ACTION_REGISTER_USER);

            gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Failed to send registration id to backend.");
        }
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    public void storeRegistrationId(Activity activity, Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(activity, context);
        int appVersion = getAppVersion(context);
        Log.i(Constants.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Constants.TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */
    public SharedPreferences getGcmPreferences(Activity activity, Context context) {
        // Persist the registration ID in shared preferences
        return activity.getSharedPreferences(activity.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }



    public void enviarNuevoBar(Bar nuevoBar) {
        try {
            Bundle data = new Bundle();
            data.putString("message", "nuevo bar");
            data.putString(Constants.MESSAGE, nuevoBar.toString());
            data.putString("action", Constants.ACTION_REGISTER_BAR);

            String id = Integer.toString(msgId.incrementAndGet());

            Log.i("send", "mandando nuevo bar");

            gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            Log.e(Constants.TAG, "No se pudo enviar nuevo bar.");
        }
    }

    public void borrarBar(String nombreLegal) {

    }

    public void borrarBar(LatLng position) {
        if (null == position) return;
        try {
            Bundle data = new Bundle();
            data.putString("message", "borrar bar");
            data.putString(Constants.DATA_LATITUD, "" + position.latitude);
            data.putString(Constants.DATA_LONGITUD, "" + position.longitude);
            data.putString("action", Constants.ACTION_DELETE_BAR);

            String id = Integer.toString(msgId.incrementAndGet());

            Log.i("send", "mandando bar a borrar");

            gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
        } catch (IOException e) {
            Log.e(Constants.TAG, "No se pudo enviar bar a borrar.");
        }
    }

}
