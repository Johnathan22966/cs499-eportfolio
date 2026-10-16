package com.example.nexventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ScrollView;
import android.view.View;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InventoryRepository inventoryRepository;
    private SmsAlertService smsAlertService;
    private LinearLayout inventoryList;
    private EditText itemNameInput;
    private EditText itemSkuInput;
    private EditText itemQuantityInput;
    private EditText minimumStockInput;
    private Spinner stockFilterSpinner;
    private Spinner sortOrderSpinner;
    private TextView resultCountText;
    private boolean screenReady;
    private EditText searchBar;
    private ScrollView scrollView;
    private View itemForm;
    private TextView totalItemsText;
    private TextView lowStockText;
    private TextView outOfStockText;
    private TextView noResultsText;
    private TextView alertSummaryText;
    private TextView selectedItemText;
    private Button updateItemButton;
    private Button deleteSelectedButton;
    private int selectedItemId = -1;
    private LinearLayout selectedRow;
    private SearchMode searchMode = SearchMode.SKU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).requireSignIn()) { finish(); return; }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inventoryRepository = new InventoryRepository(this);
        smsAlertService = new SmsAlertService(this);
        inventoryList = findViewById(R.id.inventoryList);
        itemNameInput = findViewById(R.id.itemNameInput);
        itemSkuInput = findViewById(R.id.itemSkuInput);
        itemQuantityInput = findViewById(R.id.itemQuantityInput);
        minimumStockInput = findViewById(R.id.minimumStockInput);
        stockFilterSpinner = findViewById(R.id.stockFilterSpinner);
        sortOrderSpinner = findViewById(R.id.sortOrderSpinner);
        resultCountText = findViewById(R.id.resultCountText);
        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { loadInventoryGrid(); }
            public void afterTextChanged(Editable s) {}
        });
        totalItemsText = findViewById(R.id.totalItemsText);
        lowStockText = findViewById(R.id.lowStockText);
        outOfStockText = findViewById(R.id.outOfStockText);
        noResultsText = findViewById(R.id.noResultsText);
        alertSummaryText = findViewById(R.id.alertSummaryText);
        selectedItemText = findViewById(R.id.selectedItemText);
        TabLayout searchModeTabs = findViewById(R.id.searchModeTabs);
        searchModeTabs.addTab(searchModeTabs.newTab().setText("SKU"));
        searchModeTabs.addTab(searchModeTabs.newTab().setText("Item Number"));
        searchModeTabs.addTab(searchModeTabs.newTab().setText("Product Name"));
        searchModeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                searchMode = SearchMode.values()[tab.getPosition()];
                searchBar.setHint(searchMode == SearchMode.SKU ? "Search by SKU"
                        : searchMode == SearchMode.ITEM_NUMBER ? "Search by item number" : "Search by product name");
                searchBar.setText("");
                loadInventoryGrid();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        Button homeButton = findViewById(R.id.homeButton);
        Button searchButton = findViewById(R.id.searchButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button alertsButton = findViewById(R.id.alertsButton);
        Button addItemButton = findViewById(R.id.addItemButton);
        updateItemButton = findViewById(R.id.updateItemButton);
        updateItemButton.setEnabled(false);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        deleteSelectedButton.setEnabled(false);
        Button loadSamplesButton = findViewById(R.id.loadSamplesButton);
        Button removeSamplesButton = findViewById(R.id.removeSamplesButton);
        scrollView = findViewById(R.id.scrollView);
        itemForm = findViewById(R.id.addItemFolder);

        addItemButton.setOnClickListener(v -> addInventoryItem());
        updateItemButton.setOnClickListener(v -> updateInventoryItem());
        deleteSelectedButton.setOnClickListener(v -> {
            if (selectedItemId != -1) deleteInventoryItem(selectedItemId);
        });
        loadSamplesButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Load sample inventory?")
                .setMessage("Add up to 15 networking supplies labeled SAMPLE-NET. Existing records will stay unchanged.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Load", (dialog, which) -> {
                    try {
                        int added = inventoryRepository.addSampleInventory();
                        searchBar.setText("");
                        loadInventoryGrid();
                        Toast.makeText(this, added == 0 ? "Sample items are already loaded"
                                : "Added " + added + " sample items", Toast.LENGTH_SHORT).show();
                    } catch (RuntimeException error) {
                        Toast.makeText(this, "Unable to load sample items", Toast.LENGTH_SHORT).show();
                    }
                }).show());
        removeSamplesButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Remove sample items?")
                .setMessage("Only records with the 15 original SAMPLE-NET SKUs will be removed. Your other inventory will stay saved.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Remove", (dialog, which) -> {
                    int removed = inventoryRepository.removeSampleInventory();
                    clearInputs();
                    searchBar.setText("");
                    loadInventoryGrid();
                    Toast.makeText(this, "Removed " + removed + " sample items", Toast.LENGTH_SHORT).show();
                }).show());

        homeButton.setOnClickListener(v -> finish());
        searchButton.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
        settingsButton.setOnClickListener(v -> openNotificationScreen());
        alertsButton.setOnClickListener(v -> openNotificationScreen());

        configureSpinner(stockFilterSpinner, new String[]{"All stock", "Needs restocking", "Out of stock", "Low stock", "Good stock"});
        configureSpinner(sortOrderSpinner, new String[]{"Name A to Z", "Quantity low to high", "Restock priority"});
        findViewById(R.id.restockButton).setOnClickListener(v -> {
            searchBar.setText("");
            stockFilterSpinner.setSelection(InventoryAlgorithms.StockFilter.NEEDS_RESTOCK.ordinal());
            sortOrderSpinner.setSelection(InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY.ordinal());
            loadInventoryGrid();
        });
        screenReady = true;
        loadInventoryGrid();
    }

    private void configureSpinner(Spinner spinner, String[] choices) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { loadInventoryGrid(); }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void addInventoryItem() {
        String name = itemNameInput.getText().toString().trim();
        String sku = itemSkuInput.getText().toString().trim();
        String quantityText = itemQuantityInput.getText().toString().trim();

        if (!isInventoryInputValid(name, sku, quantityText)) {
            return;
        }

        int quantity;
        try { quantity = InputRules.quantity(quantityText); }
        catch (IllegalArgumentException error) { itemQuantityInput.setError(error.getMessage()); return; }
        int minimumStock;
        try { minimumStock = InputRules.minimumStock(minimumStockInput.getText().toString().trim()); }
        catch (IllegalArgumentException error) { minimumStockInput.setError(error.getMessage()); return; }
        if (inventoryRepository.add(name, sku, quantity, minimumStock)) {
            Toast.makeText(this, "Inventory item added", Toast.LENGTH_SHORT).show();
            showSmsResult(name, quantity, minimumStock);
            clearInputs();
            loadInventoryGrid();
        } else {
            Toast.makeText(this, inventoryRepository.skuExists(sku, -1)
                    ? "SKU already exists. Use Update to change this item."
                    : "Unable to save item. Try again.", Toast.LENGTH_SHORT).show();
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

        int quantity;
        try { quantity = InputRules.quantity(quantityText); }
        catch (IllegalArgumentException error) { itemQuantityInput.setError(error.getMessage()); return; }
        int minimumStock;
        try { minimumStock = InputRules.minimumStock(minimumStockInput.getText().toString().trim()); }
        catch (IllegalArgumentException error) { minimumStockInput.setError(error.getMessage()); return; }
        if (inventoryRepository.update(selectedItemId, name, sku, quantity, minimumStock)) {
            Toast.makeText(this, "Inventory item updated", Toast.LENGTH_SHORT).show();
            showSmsResult(name, quantity, minimumStock);
            clearInputs();
            loadInventoryGrid();
        } else {
            Toast.makeText(this, inventoryRepository.skuExists(sku, selectedItemId)
                    ? "SKU already belongs to another item."
                    : "Unable to update item. Try again.", Toast.LENGTH_SHORT).show();
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
        if (!screenReady) return;
        selectedRow = null;
        inventoryList.removeAllViews();

        List<InventoryItem> items = inventoryRepository.select(searchBar.getText().toString(), searchMode,
                InventoryAlgorithms.StockFilter.values()[stockFilterSpinner.getSelectedItemPosition()],
                InventoryAlgorithms.SortOrder.values()[sortOrderSpinner.getSelectedItemPosition()]);
        resultCountText.setText("Showing " + items.size() + " matching item(s)");
        noResultsText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        for (InventoryItem item : items) {
            addInventoryRow(item);
        }

        int total = inventoryRepository.total();
        int low = inventoryRepository.lowStock();
        int out = inventoryRepository.outOfStock();
        totalItemsText.setText("Total Items: " + total);
        lowStockText.setText("Low Stock: " + low);
        outOfStockText.setText("Out of Stock: " + out);
        int restock = inventoryRepository.restockCount();
        alertSummaryText.setText(restock == 0 ? "No items below their restock target."
                : restock + " item(s) below their restock target. Tap Show Restock List to prioritize them.");
    }

    private void addInventoryRow(InventoryItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(12, 12, 12, 12);
        row.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.bottomMargin = 12;
        row.setLayoutParams(layout);

        TextView nameView = createCell(item.getName());
        nameView.setTextSize(16);
        TextView detailsView = createCell("Item #: " + item.getId() + "  •  SKU: " + item.getSku()
                + "\nQuantity: " + item.getQuantity() + "  •  Status: " + item.getStatus()
                + "\nMinimum: " + item.getMinimumStock() + "  •  Shortage: " + item.getShortage());
        row.addView(nameView);
        row.addView(detailsView);
        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription("Select " + item.getName() + " for editing or deletion");
        row.setOnClickListener(v -> selectItemForEditing(item, row));
        inventoryList.addView(row);
    }

    private TextView createCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private void selectItemForEditing(InventoryItem item, LinearLayout row) {
        if (selectedRow != null) selectedRow.setBackgroundColor(Color.WHITE);
        selectedRow = row;
        row.setBackgroundColor(Color.rgb(217, 230, 255));
        selectedItemId = item.getId();
        updateItemButton.setEnabled(true);
        deleteSelectedButton.setEnabled(true);
        selectedItemText.setText("Editing item #" + item.getId());
        itemNameInput.setError(null);
        itemSkuInput.setError(null);
        itemQuantityInput.setError(null);
        minimumStockInput.setError(null);
        itemNameInput.setText(item.getName());
        itemSkuInput.setText(item.getSku());
        itemQuantityInput.setText(String.valueOf(item.getQuantity()));
        minimumStockInput.setText(String.valueOf(item.getMinimumStock()));
        itemForm.post(() -> scrollView.smoothScrollTo(0, itemForm.getTop()));
        Toast.makeText(this, "Editing " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    private void deleteInventoryItem(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete inventory item?")
                .setMessage("This removes the item from saved inventory.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (inventoryRepository.delete(id)) {
                        Toast.makeText(this, "Inventory item deleted", Toast.LENGTH_SHORT).show();
                        clearInputs();
                        loadInventoryGrid();
                    } else {
                        Toast.makeText(this, "Unable to delete item", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void clearInputs() {
        selectedItemId = -1;
        updateItemButton.setEnabled(false);
        deleteSelectedButton.setEnabled(false);
        selectedItemText.setText("Adding a new item");
        itemNameInput.setError(null);
        itemSkuInput.setError(null);
        itemQuantityInput.setError(null);
        minimumStockInput.setError(null);
        if (selectedRow != null) selectedRow.setBackgroundColor(Color.WHITE);
        selectedRow = null;
        itemNameInput.setText("");
        itemSkuInput.setText("");
        itemQuantityInput.setText("");
        minimumStockInput.setText("5");
    }

    private void showSmsResult(String itemName, int quantity, int minimumStock) {
        String result = smsAlertService.attemptLowStockAlert(itemName, quantity, minimumStock);
        if (result != null) Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private void openNotificationScreen() {
        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
        startActivity(intent);
    }
}
