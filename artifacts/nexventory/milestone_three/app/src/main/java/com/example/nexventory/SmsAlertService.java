package com.example.nexventory;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

/** Checks both app choice and Android permission before an optional alert. */
public final class SmsAlertService {
    private final Context context;

    public SmsAlertService(Context context) { this.context = context; }

    public String attemptLowStockAlert(String itemName, int quantity, int minimumStock) {
        if (quantity >= minimumStock) return null;
        SharedPreferences settings = context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE);
        if (!settings.getBoolean("sms_enabled", false)) return "Inventory saved. SMS alerts are off.";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) return "Inventory saved. SMS permission is missing.";
        String phone = settings.getString("phone_number", "");
        if (phone == null || phone.trim().isEmpty()) return "Inventory saved. Add an SMS phone number.";
        try {
            SmsManager.getDefault().sendTextMessage(phone, null,
                    "Nexventory Alert: " + itemName + " needs restocking. Quantity: " + quantity + "; minimum: " + minimumStock + ".", null, null);
            return "Inventory saved. SMS send requested; delivery is not confirmed.";
        } catch (RuntimeException error) {
            return "Inventory saved, but the SMS request failed.";
        }
    }
}
