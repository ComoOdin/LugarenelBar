package com.matic.lugarenelbar;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.Constants;


import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matic on 03/04/2016.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;




    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "Lugar en el Bar";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification(
                        "Deleted messages on server: " + extras.toString(), null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {

                //Toast.makeText(GcmIntentService.this, "llego mensaje", Toast.LENGTH_SHORT).show();

                String action = extras.getString("action");

                if (Constants.ACTION_REGISTER_USER.equals(action)) {


                    Log.i(TAG, "Recibida confirmacion de register user");


                } else if (Constants.ACTION_UPDATE.equals(action)) {


                    Log.i(TAG, "Recibida confirmacion de update");

                } else if (Constants.ACTION_DISPLAY.equals(action)) {

                    Toast.makeText(GcmIntentService.this, extras.getString("com.matic.lugarenelbar.message.message"), Toast.LENGTH_LONG).show();


                    sendNotification(extras.getString("com.matic.lugarenelbar.message.message"), extras);

                    Intent i = new Intent(action);
                    i.putExtra("com.matic.lugarenelbar.message.message", extras.getString("com.matic.lugarenelbar.message.message"));
                    i.putExtra("com.matic.lugarenelbar.message.title", extras.getString("com.matic.lugarenelbar.message.title"));
                    i.putExtra("com.matic.lugarenelbar.message.link", extras.getString("com.matic.lugarenelbar.message.link"));
                    i.putExtra("com.matic.lugarenelbar.message.date", extras.getString("com.matic.lugarenelbar.message.date"));
                    sendBroadcast(i);

                    Log.i(TAG, "Received: " + extras.toString());

                } else if (Constants.ACTION_DATA_BARES.equals(action)) {

                    List<Bar> listaBares = new LinkedList<>();

                    String[] bares = extras.getString("com.matic.lugarenelbar.message.message").split(Constants.OBJECT_SEPARATOR);
                    for (String s : bares){
                        String[] barString = s.split(Constants.FIELD_SEPARATOR);
                        Bar nuevoBar = new Bar(Integer.parseInt(barString[0]), barString[1], barString[2], barString[3], barString[4], barString[5], Integer.parseInt(barString[6]), Double.parseDouble(barString[7]), Double.parseDouble(barString[8]), barString[9], null, Integer.parseInt(barString[11]));
                        listaBares.add(nuevoBar);
                    }

                    // En este punto la linkedlist esta cargada con todos los bares recibidos

                    // Esto envia los datos de los bares hacia MainActivity
                    intent.setClass(getApplicationContext(), MainActivity.class);
                    intent.putExtra("bares", extras.getString("com.matic.lugarenelbar.message.message"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.i(Constants.TAG, "Mandando datos de bares a MainActivity");
                    getApplicationContext().startActivity(intent);


                    // Esto genera la notificacion
                    /*
                    String mensaje = "Llegaron datos de " + listaBares.size() + " bares: ";
                    for (Bar bar : listaBares) {
                        mensaje += bar.getNombreFantasia() + ", ";
                    }
                    sendNotification(mensaje, null);
                    */
                } else if (Constants.ACTION_REGISTER_BAR.equals(action)) {
                    String mensaje = extras.getString("message");

                    // envia a NuevoBarActivity la respuesta a la insercion de bares
                    intent.setClass(getApplicationContext(), NuevoBarActivity.class);
                    intent.putExtra("respuestaInsercion", mensaje);
                    intent.putExtra("ip", extras.getString("ip"));
                    intent.putExtra("port", extras.getString("port"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.i(Constants.TAG, "Mandando respuesta de insercion de bares a NuevoBarActivity");
                    getApplicationContext().startActivity(intent);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    @SuppressLint("NewApi")
    private void sendNotification(String msg, Bundle bundle) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo_milocation)
                        .setContentTitle("Lugar en el Bar!")
                        .setContentText(msg);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


        /*
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        resultIntent.putExtras(bundle);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification;
       // Uri sound = Uri.parse("android.resource://" + getPackageName() + "/"
         //       + R.raw.notificationsound);


        notification = new Notification.Builder(this)

                .setStyle(new Notification.BigTextStyle().bigText(msg))
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentText(msg)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.logo_mylocation)
                .setLights(Color.YELLOW, 500, 500)
                .build();
        notification.defaults = 0;
        notificationManager.notify(0, notification);
        */
    }
}
