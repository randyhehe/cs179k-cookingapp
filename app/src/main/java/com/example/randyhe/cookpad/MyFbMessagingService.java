package com.example.randyhe.cookpad;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Tia on 3/5/2018.
 */

public class MyFbMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "com.example.randyhe.cookpad.ID";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size() > 0){
            Map<String, String> payload = remoteMessage.getData();
            sendMessageNotification(payload, remoteMessage);
        }
    }

    /**
     * Build a push notification for a chat message
     */
    private void sendMessageNotification(Map<String, String> payload, RemoteMessage rm){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Cookpad Channel",
                NotificationManager.IMPORTANCE_HIGH);

        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.cooklogo);
        builder.setContentTitle(payload.get("title"));
        builder.setContentText(rm.getData().get("message"));

        String type = payload.get("type"); //get type of notif

        if (type.equals("follow")) {
            String person = payload.get("person");
            Intent resultIntent = new Intent(this, ProfileActivity.class);
            resultIntent.putExtra("ID", person);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPI);
        }

        if (type.equals("book")) {
            Intent resultIntent = new Intent(this, HomeActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPI);
        }

        notificationManager.notify(0, builder.build());

    }


    private int buildNotificationId(String id){
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }

}

