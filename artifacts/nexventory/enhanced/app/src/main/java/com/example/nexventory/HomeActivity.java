package com.example.nexventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private InventoryRepository inventoryRepository;
    private TextView totalItemsHomeText;
    private TextView lowStockHomeText;
    private TextView outOfStockHomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).requireSignIn()) { finish(); return; }
        setContentView(R.layout.activity_home);

        inventoryRepository = new InventoryRepository(this);
        totalItemsHomeText = findViewById(R.id.totalItemsHomeText);
        lowStockHomeText = findViewById(R.id.lowStockHomeText);
        outOfStockHomeText = findViewById(R.id.outOfStockHomeText);

        Button viewInventoryButton = findViewById(R.id.viewInventoryButton);
        Button smsNotificationsButton = findViewById(R.id.smsNotificationsButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        viewInventoryButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MainActivity.class)));

        smsNotificationsButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));
        logoutButton.setOnClickListener(v -> new SessionManager(this).signOut());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inventoryRepository == null) return;
        DatabaseTasks.run(this, () -> new int[]{inventoryRepository.total(),inventoryRepository.lowStock(),inventoryRepository.outOfStock()}, counts -> {
            totalItemsHomeText.setText("Total Items: " + counts[0]);
            lowStockHomeText.setText("Low Stock: " + counts[1]);
            outOfStockHomeText.setText("Out of Stock: " + counts[2]);
        });
    }
}
