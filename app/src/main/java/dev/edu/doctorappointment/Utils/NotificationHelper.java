package dev.edu.doctorappointment.Utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.edu.doctorappointment.Model.SendNotification;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void sendAppointmentNotification(String userId, String title, String message, String userType, Context context) {
        Log.d(TAG, "Starting to send notification to " + userType + " with ID: " + userId);
        
        // Get all FCM tokens for the user
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("UserTokens")
                .child(userType)
                .child(userId);

        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> tokens = new ArrayList<>();
                    
                    // Get all tokens for this user
                    for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                        String token = tokenSnapshot.getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            tokens.add(token);
                            Log.d(TAG, "Found token: " + token);
                        }
                    }

                    if (!tokens.isEmpty()) {
                        Log.d(TAG, "Found " + tokens.size() + " devices for " + userType + ": " + userId);
                        
                        // Use AtomicInteger to track successful notifications
                        AtomicInteger successCount = new AtomicInteger(0);
                        AtomicInteger failureCount = new AtomicInteger(0);
                        
                        // Send notification to each device
                        for (String token : tokens) {
                            try {
                                SendNotification sendNotification = new SendNotification(token, title, message, context);
                                sendNotification.SendNotifications();
                                successCount.incrementAndGet();
                                Log.d(TAG, "Notification sent successfully to token: " + token);
                            } catch (Exception e) {
                                failureCount.incrementAndGet();
                                Log.e(TAG, "Failed to send notification to token: " + token, e);
                            }
                        }
                        
                        // Log final results
                        Log.d(TAG, String.format("Notification delivery results for %s %s: %d successful, %d failed",
                                userType, userId, successCount.get(), failureCount.get()));
                    } else {
                        Log.e(TAG, "No valid FCM tokens found for " + userType + ": " + userId);
                    }
                } else {
                    Log.e(TAG, "No FCM tokens found for " + userType + ": " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error getting FCM tokens for " + userType + ": " + userId, databaseError.toException());
            }
        });
    }

    private static void sendFCMNotification(String token, String title, String message, Context context) {
        try {
            SendNotification sendNotification = new SendNotification(token, title, message, context);
            sendNotification.SendNotifications();
            Log.d(TAG, "Notification sent successfully to token: " + token);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send notification to token: " + token, e);
        }
    }
} 