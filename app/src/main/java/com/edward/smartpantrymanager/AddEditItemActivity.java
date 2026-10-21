package com.edward.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.ui.InsetsHelper;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private PantryDao pantryDao;
    private long itemId = -1; // -1 means "add mode"
    private PantryItem existingItem = null;

    private TextInputEditText nameInput, quantityInput, expiryInput;
    private android.widget.AutoCompleteTextView unitInput;
    private TextView errorText, screenTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        InsetsHelper.applySystemBarInsets(this);

        pantryDao = new PantryDao(this);

        screenTitleText = findViewById(R.id.screenTitleText);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        expiryInput = findViewById(R.id.expiryInput);

        unitInput.setAdapter(new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.unit_options)));

        expiryInput.setOnClickListener(v -> showDatePicker());

        errorText = findViewById(R.id.errorText);
        Button saveButton = findViewById(R.id.saveButton);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

        if (itemId != -1) {
            // Edit mode - load existing item and pre-fill the form
            existingItem = pantryDao.getById(itemId);
            if (existingItem != null) {
                screenTitleText.setText("Edit Ingredient");
                nameInput.setText(existingItem.getName());
                quantityInput.setText(String.valueOf(existingItem.getQuantity()));
                unitInput.setText(existingItem.getUnit(), false);
                expiryInput.setText(existingItem.getExpiryDate());
            }
        }

        saveButton.setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String quantityText = quantityInput.getText() != null ? quantityInput.getText().toString().trim() : "";
        String unit = unitInput.getText() != null ? unitInput.getText().toString().trim() : "";
        String expiry = expiryInput.getText() != null ? expiryInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            showError("Ingredient name is required.");
            return;
        }

        if (quantityText.isEmpty()) {
            showError("Quantity is required.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                showError("Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Quantity must be a valid number.");
            return;
        }

        if (unit.isEmpty()) {
            showError("Please choose a unit.");
            return;
        }

        if (!expiry.isEmpty() && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Expiry date must be in format yyyy-MM-dd.");
            return;
        }

        PantryItem item = (existingItem != null) ? existingItem : new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiry.isEmpty() ? null : expiry);

        if (existingItem != null) {
            pantryDao.update(item);
        } else {
            pantryDao.insert(item);
        }

        setResult(RESULT_OK);
        finish();
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            expiryInput.setText(date);
        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(android.view.View.VISIBLE);
    }

    /** Static helper to build the correct Intent for editing an existing item. */
    public static Intent createEditIntent(android.content.Context context, long itemId) {
        Intent intent = new Intent(context, AddEditItemActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }
}