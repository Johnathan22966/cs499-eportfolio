package com.example.nexventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private TextView totalItemsHomeText;
    private TextView lowStockHomeText;
    private TextView outOfStockHomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        databaseHelper = new DatabaseHelper(this);
        totalItemsHomeText = findViewById(R.id.totalItemsHomeText);
        lowStockHomeText = findViewById(R.id.lowStockHomeText);
        outOfStockHomeText = findViewById(R.id.outOfStockHomeText);

        Button viewInventoryButton = findViewById(R.id.viewInventoryButton);
        Button smsNotificationsButton = findViewById(R.id.smsNotificationsButton);

        viewInventoryButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MainActivity.class)));

        smsNotificationsButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        totalItemsHomeText.setText("Total Items: " + databaseHelper.getTotalItems());
        lowStockHomeText.setText("Low Stock: " + databaseHelper.getLowStockCount());
        outOfStockHomeText.setText("Out of Stock: " + databaseHelper.getOutOfStockCount());
    }
}
