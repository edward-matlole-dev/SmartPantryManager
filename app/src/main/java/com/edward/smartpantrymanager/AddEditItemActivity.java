package com.edward.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.logic.ExpiryUtils;
import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.ui.InsetsHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private PantryDao pantryDao;
    private long itemId = -1; // -1 means "add mode"
    private PantryItem existingItem = null;

    private TextInputEditText nameInput, quantityInput, expiryInput;
    private android.widget.AutoCompleteTextView unitInput;
    private TextView errorText, screenTitleText;
    private TextInputLayout nameInputLayout, quantityInputLayout, unitInputLayout, expiryInputLayout;
    private Button clearExpiryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        InsetsHelper.applySystemBarInsets(this);

        pantryDao = new PantryDao(this);

        screenTitleText = findViewById(R.id.screenTitleText);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.add_ingredient_title);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        expiryInput = findViewById(R.id.expiryInput);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        quantityInputLayout = findViewById(R.id.quantityInputLayout);
        unitInputLayout = findViewById(R.id.unitInputLayout);
        expiryInputLayout = findViewById(R.id.expiryInputLayout);
        clearExpiryButton = findViewById(R.id.clearExpiryButton);

        unitInput.setAdapter(new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.unit_options)));

        expiryInput.setOnClickListener(v -> showDatePicker());
        clearExpiryButton.setOnClickListener(v -> {
            expiryInput.setText("");
            expiryInputLayout.setError(null);
            clearExpiryButton.setVisibility(android.view.View.GONE);
        });

        errorText = findViewById(R.id.errorText);
        Button saveButton = findViewById(R.id.saveButton);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

        if (itemId != -1) {
            // Edit mode - load existing item and pre-fill the form
            existingItem = pantryDao.getById(itemId);
            if (existingItem != null) {
                screenTitleText.setText(R.string.edit_ingredient_title);
                toolbar.setTitle(R.string.edit_ingredient_title);
                nameInput.setText(existingItem.getName());
                quantityInput.setText(String.valueOf(existingItem.getQuantity()));
                unitInput.setText(existingItem.getUnit(), false);
                expiryInput.setText(existingItem.getExpiryDate());
                clearExpiryButton.setVisibility(existingItem.getExpiryDate() != null
                        && !existingItem.getExpiryDate().isEmpty()
                        ? android.view.View.VISIBLE : android.view.View.GONE);
            }
        }

        saveButton.setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        clearInputErrors();

        String name = nameInput.getText() != null
                ? nameInput.getText().toString().trim().replaceAll("\\s+", " ") : "";
        String quantityText = quantityInput.getText() != null ? quantityInput.getText().toString().trim() : "";
        String unit = unitInput.getText() != null ? unitInput.getText().toString().trim() : "";
        String expiry = expiryInput.getText() != null ? expiryInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            showFieldError(nameInputLayout, nameInput, R.string.ingredient_required);
            return;
        }

        if (quantityText.isEmpty()) {
            showFieldError(quantityInputLayout, quantityInput, R.string.quantity_required);
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
                showFieldError(quantityInputLayout, quantityInput, R.string.quantity_positive);
                return;
            }
        } catch (NumberFormatException e) {
            showFieldError(quantityInputLayout, quantityInput, R.string.quantity_invalid);
            return;
        }

        if (unit.isEmpty()) {
            showFieldError(unitInputLayout, unitInput, R.string.unit_required);
            return;
        }

        if (!expiry.isEmpty() && !ExpiryUtils.isValidDate(expiry)) {
            showFieldError(expiryInputLayout, expiryInput, R.string.expiry_invalid);
            return;
        }

        PantryItem item = (existingItem != null) ? existingItem : new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiry.isEmpty() ? null : expiry);

        boolean saved;
        if (existingItem != null) {
            saved = pantryDao.update(item) > 0;
        } else {
            saved = pantryDao.insert(item) != -1;
        }

        if (!saved) {
            showError(getString(R.string.save_failed));
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            expiryInput.setText(date);
            expiryInputLayout.setError(null);
            clearExpiryButton.setVisibility(android.view.View.VISIBLE);
        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(android.view.View.VISIBLE);
    }

    private void showFieldError(TextInputLayout inputLayout, android.view.View input, int messageRes) {
        String message = getString(messageRes);
        inputLayout.setError(message);
        input.requestFocus();
        errorText.setVisibility(android.view.View.GONE);
    }

    private void clearInputErrors() {
        nameInputLayout.setError(null);
        quantityInputLayout.setError(null);
        unitInputLayout.setError(null);
        expiryInputLayout.setError(null);
        errorText.setVisibility(android.view.View.GONE);
    }

    /** Static helper to build the correct Intent for editing an existing item. */
    public static Intent createEditIntent(android.content.Context context, long itemId) {
        Intent intent = new Intent(context, AddEditItemActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }
}
