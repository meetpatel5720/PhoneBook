package com.meet.phonebook.Services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.meet.phonebook.Activity.MainActivity;
import com.meet.phonebook.Activity.UpdateActivity;
import com.meet.phonebook.Misc.Constants;
import com.meet.phonebook.Utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.meet.phonebook.Misc.Constants.DATABASE_URL;
import static com.meet.phonebook.Misc.Constants.IS_DATABASE_UPDATED;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Constants.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String databaseUrl = data.getString("dataurl");
            String timestamp = data.getString("timestamp");
//            JSONObject payload = data.getJSONObject("payload");

            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "dataurl: " + databaseUrl);
//            Log.e(TAG, "payload: " + payload.toString());
            Log.e(TAG, "timestamp: " + timestamp);

            Intent resultIntent = new Intent(getApplicationContext(), UpdateActivity.class);
            resultIntent.putExtra("dataUrl", databaseUrl);
            showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(IS_DATABASE_UPDATED,false).apply();
            sharedPreferences.edit().putString(DATABASE_URL,databaseUrl).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.e(TAG, "sendRegistrationToServer: " + token);
    }

}
