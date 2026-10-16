package com.example.nexventory;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private TextView statusText;
    private EditText phoneNumberInput;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).requireSignIn()) { finish(); return; }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        preferences = getSharedPreferences("notification_preferences", Context.MODE_PRIVATE);
        statusText = findViewById(R.id.smsStatusText);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        Button savePhoneButton = findViewById(R.id.savePhoneButton);
        Button grantPermissionButton = findViewById(R.id.grantPermissionButton);
        Button denyPermissionButton = findViewById(R.id.denyPermissionButton);
        Button backToInventoryButton = findViewById(R.id.backToInventoryButton);

        phoneNumberInput.setText(preferences.getString("phone_number", ""));
        updateStatusText();

        savePhoneButton.setOnClickListener(v -> savePhoneNumber());

        grantPermissionButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_REQUEST_CODE);
            } else {
                preferences.edit().putBoolean("sms_enabled", true).apply();
                updateStatusText();
            }
        });

        denyPermissionButton.setOnClickListener(v -> {
            preferences.edit().putBoolean("sms_enabled", false).apply();
            updateStatusText();
        });

        backToInventoryButton.setOnClickListener(v -> finish());
    }

    private void savePhoneNumber() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        preferences.edit().putString("phone_number", phoneNumber).apply();
        Toast.makeText(this, "SMS phone number saved", Toast.LENGTH_SHORT).show();
        updateStatusText();
    }

    private void updateStatusText() {
        if (!preferences.getBoolean("sms_enabled", false)) {
            statusText.setText("Status: SMS alerts are off in Nexventory.");
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            statusText.setText("Status: SMS notifications are enabled. Low inventory alerts can be sent.");
        } else {
            statusText.setText("Status: SMS permission has not been granted yet. The app will still work without SMS alerts.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                preferences.edit().putBoolean("sms_enabled", true).apply();
                updateStatusText();
            } else {
                preferences.edit().putBoolean("sms_enabled", false).apply();
                updateStatusText();
            }
        }
    }
}
