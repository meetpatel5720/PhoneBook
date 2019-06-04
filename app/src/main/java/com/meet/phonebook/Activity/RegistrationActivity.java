package com.meet.phonebook.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.meet.phonebook.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.meet.phonebook.Misc.Constants.IS_FIRST_RUN;
import static com.meet.phonebook.Misc.Constants.IS_NUMBER_VERIFIED;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;

public class RegistrationActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 1;

    Button validateButton;
    TextView textView, textView2;
    EditText mobileNoEditText;
    Spinner simChoiceSpinner;
    ProgressBar progressBar;

    ArrayList<String> simOperators = new ArrayList<>();
    ArrayAdapter<String> simOperatorsAdapter;
    HashMap<Integer, String> map = new HashMap<Integer, String>();

    String simOperatorName;
    int simSlot = 0;

    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(IS_NUMBER_VERIFIED, false)) {
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            setContentView(R.layout.activity_registration);
            textView = findViewById(R.id.textView);
            mobileNoEditText = findViewById(R.id.mobile_no_editText);
            simChoiceSpinner = findViewById(R.id.sim_select_spinner);
            validateButton = findViewById(R.id.validateButton);

            getSimOperators();
            simOperatorsAdapter = new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_list_item_1, simOperators);
            simOperatorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            simChoiceSpinner.setAdapter(simOperatorsAdapter);

            simChoiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    simOperatorName = parent.getItemAtPosition(position).toString();
                    for (Map.Entry<Integer, String> entry : map.entrySet()) {
                        if (entry.getValue().equals(simOperatorName)) {
                            simSlot = entry.getKey();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(RegistrationActivity.this, "Please select sim operator", Toast.LENGTH_LONG).show();
                }
            });
            validateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mobile = mobileNoEditText.getText().toString().trim();

                    if (mobile.isEmpty() || mobile.length() < 10) {
                        mobileNoEditText.setError("Enter a valid mobile");
                        mobileNoEditText.requestFocus();
                        return;
                    }

                    Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                    intent.putExtra("mobileNo", mobile);
                    intent.putExtra("simSlot", simSlot);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                }
            });
        }
    }

    public void getSimOperators() {
        simOperators.clear();
        map.clear();
        if (checkPhoneStatePermission()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
                List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                if (subsInfoList != null){
                    for (SubscriptionInfo subscriptionInfo : subsInfoList) {
                        map.put(subscriptionInfo.getSimSlotIndex(), subscriptionInfo.getCarrierName().toString());
                        simOperators.add(subscriptionInfo.getCarrierName().toString());
                    }
                }
            }
        } else {
            requestPhoneStatePermission();
        }
    }

    public boolean checkPhoneStatePermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(RegistrationActivity.this, Manifest.permission.READ_PHONE_STATE)) {
            new AlertDialog.Builder(RegistrationActivity.this)
                    .setTitle("Permission need")
                    .setMessage("This app require phone state permission")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finishAffinity();
                System.exit(0);
            } else {
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }
    }
}
