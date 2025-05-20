package dev.edu.doctorappointment.Model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {
    private static final String TAG = "SendNotification";
    private final String userFCMToken;
    private final String title;
    private final String body;
    private final Context context;
    private final String postUrl = "https://fcm.googleapis.com/v1/projects/doctor-appointment-f5d32/messages:send";

    public SendNotification(String userFCMToken, String title, String body, Context context) {
        this.userFCMToken = userFCMToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    public void SendNotifications() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObject = new JSONObject();
        try {
            JSONObject messageObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", title);
            notificationObject.put("body", body);
            messageObject.put("token", userFCMToken);
            messageObject.put("notification", notificationObject);
            mainObject.put("message", messageObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObject,
                    response -> Log.d(TAG, "Notification sent successfully"),
                    error -> Log.e(TAG, "Error sending notification: " + error.getMessage())) {
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Accesstoken accessToken = new Accesstoken();
                    String accessKey = accessToken.getAccessToken();
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer " + accessKey);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating notification JSON: " + e.getMessage());
        }
    }
}
