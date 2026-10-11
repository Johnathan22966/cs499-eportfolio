package com.example.nexventory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOW_STOCK_THRESHOLD = 5;

    private DatabaseHelper databaseHelper;
    private TableLayout inventoryTable;
    private EditText itemNameInput;
    private EditText itemSkuInput;
    private EditText itemQuantityInput;
    private TextView totalItemsText;
    private TextView lowStockText;
    private TextView outOfStockText;
    private int selectedItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        inventoryTable = findViewById(R.id.inventoryTable);
        itemNameInput = findViewById(R.id.itemNameInput);
        itemSkuInput = findViewById(R.id.itemSkuInput);
        itemQuantityInput = findViewById(R.id.itemQuantityInput);
        totalItemsText = findViewById(R.id.totalItemsText);
        lowStockText = findViewById(R.id.lowStockText);
        outOfStockText = findViewById(R.id.outOfStockText);

        Button homeButton = findViewById(R.id.homeButton);
        Button searchButton = findViewById(R.id.searchButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button alertsButton = findViewById(R.id.alertsButton);
        Button addItemButton = findViewById(R.id.addItemButton);
        Button updateItemButton = findViewById(R.id.updateItemButton);
        ScrollView scrollView = findViewById(R.id.scrollView);

        addItemButton.setOnClickListener(v -> addInventoryItem());
        updateItemButton.setOnClickListener(v -> updateInventoryItem());

        homeButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HomeActivity.class)));
        searchButton.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
        settingsButton.setOnClickListener(v -> openNotificationScreen());
        alertsButton.setOnClickListener(v -> openNotificationScreen());

        loadInventoryGrid();
    }

    private void addInventoryItem() {
        String name = itemNameInput.getText().toString().trim();
        String sku = itemSkuInput.getText().toString().trim();
        String quantityText = itemQuantityInput.getText().toString().trim();

        if (!isInventoryInputValid(name, sku, quantityText)) {
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        if (databaseHelper.addInventoryItem(name, sku, quantity)) {
            Toast.makeText(this, "Inventory item added", Toast.LENGTH_SHORT).show();
            sendLowStockSmsIfNeeded(name, quantity);
            clearInputs();
            loadInventoryGrid();
        } else {
            Toast.makeText(this, "SKU already exists. Use Update to change this item.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateInventoryItem() {
        if (selectedItemId == -1) {
            Toast.makeText(this, "Select Edit on an item first", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = itemNameInput.getText().toString().trim();
        String sku = itemSkuInput.getText().toString().trim();
        String quantityText = itemQuantityInput.getText().toString().trim();

        if (!isInventoryInputValid(name, sku, quantityText)) {
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        if (databaseHelper.updateInventoryItem(selectedItemId, name, sku, quantity)) {
            Toast.makeText(this, "Inventory item updated", Toast.LENGTH_SHORT).show();
            sendLowStockSmsIfNeeded(name, quantity);
            clearInputs();
            loadInventoryGrid();
        } else {
            Toast.makeText(this, "Unable to update item", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInventoryInputValid(String name, String sku, String quantityText) {
        if (name.isEmpty() || sku.isEmpty() || quantityText.isEmpty()) {
            Toast.makeText(this, "Enter item name, SKU, and quantity", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadInventoryGrid() {
        // Keep the header row and rebuild the database-backed grid rows below it.
        while (inventoryTable.getChildCount() > 1) {
            inventoryTable.removeViewAt(1);
        }

        List<InventoryItem> items = databaseHelper.getAllInventoryItems();
        for (InventoryItem item : items) {
            addInventoryRow(item);
        }

        totalItemsText.setText("Total Items: " + databaseHelper.getTotalItems());
        lowStockText.setText("Low Stock: " + databaseHelper.getLowStockCount());
        outOfStockText.setText("Out of Stock: " + databaseHelper.getOutOfStockCount());
    }

    private void addInventoryRow(InventoryItem item) {
        TableRow row = new TableRow(this);

        TextView nameView = createCell(item.getName());
        TextView quantityView = createCell(String.valueOf(item.getQuantity()));
        TextView statusView = createCell(item.getStatus());

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setOnClickListener(v -> selectItemForEditing(item));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> deleteInventoryItem(item.getId()));

        row.addView(nameView);
        row.addView(quantityView);
        row.addView(statusView);
        row.addView(editButton);
        row.addView(deleteButton);
        inventoryTable.addView(row);
    }

    private TextView createCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private void selectItemForEditing(InventoryItem item) {
        selectedItemId = item.getId();
        itemNameInput.setText(item.getName());
        itemSkuInput.setText(item.getSku());
        itemQuantityInput.setText(String.valueOf(item.getQuantity()));
        Toast.makeText(this, "Editing " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    private void deleteInventoryItem(int id) {
        if (databaseHelper.deleteInventoryItem(id)) {
            Toast.makeText(this, "Inventory item deleted", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadInventoryGrid();
        }
    }

    private void clearInputs() {
        selectedItemId = -1;
        itemNameInput.setText("");
        itemSkuInput.setText("");
        itemQuantityInput.setText("");
    }

    private void sendLowStockSmsIfNeeded(String itemName, int quantity) {
        if (quantity > LOW_STOCK_THRESHOLD) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Low stock saved. SMS permission is not enabled.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("notification_preferences", Context.MODE_PRIVATE);
        String phoneNumber = preferences.getString("phone_number", "");
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Low stock saved. Add a phone number in SMS settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send the low-stock alert only when permission and a phone number are available.
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null,
                "Nexventory Alert: " + itemName + " is low. Current quantity: " + quantity + ".",
                null, null);
        Toast.makeText(this, "Low stock SMS alert sent", Toast.LENGTH_SHORT).show();
    }

    private void openNotificationScreen() {
        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
        startActivity(intent);
    }
}
