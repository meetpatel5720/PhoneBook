package com.meet.phonebook.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.meet.phonebook.Data.PersonData;
import com.meet.phonebook.Misc.Constants;
import com.meet.phonebook.Misc.DatabaseHelper;
import com.meet.phonebook.R;
import com.meet.phonebook.Utils.NotificationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.meet.phonebook.Misc.Constants.IS_FIRST_RUN;
import static com.meet.phonebook.Misc.Constants.IS_NUMBER_VERIFIED;
import static com.meet.phonebook.Misc.Constants.IS_SUBSCRIBED;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;
import static com.meet.phonebook.Misc.Constants.SIM_SERIAL;
import static com.meet.phonebook.Misc.Constants.SIM_SLOT;
import static com.meet.phonebook.Misc.Constants.TOPIC_GLOBAL;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 1;
    public SharedPreferences sharedPreferences;

    TextView textView;
    ProgressBar progressBar;

    public ArrayList<PersonData> personDataList = new ArrayList<>();
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    int simSlot = 0;

    public DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.verification_progressBar);

        simSlot = sharedPreferences.getInt(SIM_SLOT, 0);

        try {
            databaseHelper = new DatabaseHelper(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        personDataList = databaseHelper.getAllData();

        validateNumber(getSimIICId());

        subscribeToTopic();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (Objects.requireNonNull(intent.getAction()).equals(Constants.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    subscribeToTopic();
                } else if (intent.getAction().equals(Constants.PUSH_NOTIFICATION)) {
                    String messageStr = intent.getStringExtra("message");
//                    message.setText(messageStr);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public void subscribeToTopic() {
        if (!sharedPreferences.getBoolean(IS_SUBSCRIBED, false)) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(IS_SUBSCRIBED, true).apply();
        }
    }

    private void validateNumber(String simIICId) {
        boolean isSimIICValidate = false;
        boolean isValidate = false;

        if (Objects.equals(sharedPreferences.getString(SIM_SERIAL, null), getSimIICId())) {
            isSimIICValidate = true;
        }

        for (PersonData personData : personDataList) {
            if (personData.getMobileNo() == sharedPreferences.getLong("mobileNo", 0)) {
                isValidate = true;
                break;
            }
        }

        Log.d("isValidate", String.valueOf(isValidate));

        if (isValidate && isSimIICValidate) {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            intent.putParcelableArrayListExtra("personDataList", personDataList);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(IS_NUMBER_VERIFIED, false).apply();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Your mobile number cannot verify");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
            alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finishAffinity();
                    System.exit(0);
                }
            });
            alertDialog.show();
        }
    }

    public String getSimIICId() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
            SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlot);

            if (subscriptionInfo != null) {
                return String.valueOf(subscriptionInfo.getIccId().isEmpty() ? 0 : subscriptionInfo.getIccId());
            } else
                return null;

        } else
            return null;
    }
}
