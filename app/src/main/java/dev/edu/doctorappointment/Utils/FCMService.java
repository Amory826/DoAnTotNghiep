package dev.edu.doctorappointment.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Home.MessActivity;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "appointment_notifications";
    private static final String CHANNEL_NAME = "Appointment Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for appointment updates";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "From: " + message.getFrom());

        // Check if message contains a data payload
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
            sendNotification(message.getData().get("title"), message.getData().get("body"));
        }

        // Check if message contains a notification payload
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            sendNotification(message.getNotification().getTitle(), message.getNotification().getBody());
        }
    }

    private void sendRegistrationToServer(String token) {
        String userId = new UserData(this).getData("id");
        String userType = new UserData(this).getData("userType");
        
        if (userId != null && !userId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userType)
                    .child(userId)
                    .setValue(token)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Token saved successfully");
                        } else {
                            Log.e(TAG, "Failed to save token", task.getException());
                        }
                    });
        }
    }

    private void sendNotification(String title, String messageBody) {
        createNotificationChannel();

        Intent intent = new Intent(this, MessActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo2)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
} 