package com.meet.phonebook.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.meet.phonebook.R;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.meet.phonebook.Misc.Constants.IS_NUMBER_VERIFIED;
import static com.meet.phonebook.Misc.Constants.MOBILE_NO;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;
import static com.meet.phonebook.Misc.Constants.SIM_SERIAL;
import static com.meet.phonebook.Misc.Constants.SIM_SLOT;

public class VerificationActivity extends AppCompatActivity {

    TextView textView;
    EditText otpEditText;
    ProgressBar progressBar;
    Button verifyButton;

    String mobileNo;
    String verificationId;
    int simSlot;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        mAuth = FirebaseAuth.getInstance();

        textView = findViewById(R.id.enter_otp_text);
        otpEditText = findViewById(R.id.otp_edit_text);
        progressBar = findViewById(R.id.wait_for_otp_progress);
        verifyButton = findViewById(R.id.verify_otp_button);

        mobileNo = getIntent().getStringExtra("mobileNo");
        simSlot = Objects.requireNonNull(getIntent().getExtras()).getInt("simSlot", 0);
        sendVerificationCode(mobileNo);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpEditText.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    otpEditText.setError("Enter valid code");
                    otpEditText.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                textView.setText("Verifying OTP");
                verifyVerificationCode(code);
            }
        });


    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Log.e("code sent", verificationId);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                textView.setText("Verifying OTP");
                otpEditText.setText(code);
                progressBar.setVisibility(View.VISIBLE);
                verifyVerificationCode(code);
            }
            Log.e("code not sent", "error sending code");
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyVerificationCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(VerificationActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("mobileNo", mobileNo);
                    intent.putExtra("simSlot", simSlot);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(IS_NUMBER_VERIFIED, true).apply();
                    getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit().putString(SIM_SERIAL, getSimIICId()).apply();
                    getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit().putInt(SIM_SLOT, simSlot).apply();
                    getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit().putLong(MOBILE_NO, Long.parseLong(mobileNo)).apply();
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(VerificationActivity.this, "Invalid OTP", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
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
