package dev.edu.doctorappointment.Utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.edu.doctorappointment.Model.SendNotification;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void sendAppointmentNotification(String userId, String title, String message, String userType, Context context) {
        // Get the FCM token for the user
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("UserTokens")
                .child(userType)
                .child(userId);

        tokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String token = dataSnapshot.getValue(String.class);
                if (token != null && !token.isEmpty()) {
                    Log.d(TAG, "Sending notification to user: " + userId);
                    sendFCMNotification(token, title, message, context);
                } else {
                    Log.e(TAG, "No FCM token found for user: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting FCM token", databaseError.toException());
            }
        });
    }

    private static void sendFCMNotification(String token, String title, String message, Context context) {
        SendNotification sendNotification = new SendNotification(token, title, message, context);
        sendNotification.SendNotifications();
    }
} 