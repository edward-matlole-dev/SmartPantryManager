package com.edward.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.ui.PantryAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.edward.smartpantrymanager.ui.InsetsHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private View filterEmptyContainer;
    private View pantrySearchLayout;
    private TextView pantryCountText;
    private TextInputEditText pantrySearchInput;
    private MaterialButtonToggleGroup filterGroup;
    private FloatingActionButton addItemFab;

    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InsetsHelper.applySystemBarInsets(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pantryDao = new PantryDao(this);

        recyclerView = findViewById(R.id.pantryRecyclerView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        filterEmptyContainer = findViewById(R.id.pantryFilterEmptyContainer);
        pantryCountText = findViewById(R.id.pantryCountText);
        pantrySearchInput = findViewById(R.id.pantrySearchInput);
        pantrySearchLayout = findViewById(R.id.pantrySearchLayout);
        filterGroup = findViewById(R.id.pantryFilterGroup);
        addItemFab = findViewById(R.id.addItemFab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(pantryDao.getAll(), this);
        recyclerView.setAdapter(adapter);

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> refreshPantryList()
        );

        addItemFab.setOnClickListener(v -> launchAddItem());
        findViewById(R.id.emptyAddButton).setOnClickListener(v -> launchAddItem());

        pantrySearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setQuery(s.toString());
                updateEmptyState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        filterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            adapter.setExpiringOnly(checkedId == R.id.filterExpiringButton);
            updateEmptyState();
        });

        updateEmptyState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPantryList();
    }

    private void refreshPantryList() {
        List<PantryItem> items = pantryDao.getAll();
        adapter.updateItems(items);
        updateEmptyState();
    }

    private void updateEmptyState() {
        int itemCount = adapter.getTotalItemCount();
        pantryCountText.setText(getResources().getQuantityString(
                R.plurals.pantry_item_count, itemCount, itemCount));
        boolean isEmpty = itemCount == 0;
        emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        pantrySearchLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        filterGroup.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        boolean noFilteredResults = !isEmpty && adapter.getItemCount() == 0;
        recyclerView.setVisibility(isEmpty || noFilteredResults ? View.GONE : View.VISIBLE);
        filterEmptyContainer.setVisibility(noFilteredResults ? View.VISIBLE : View.GONE);
        if (noFilteredResults) {
            TextView message = findViewById(R.id.pantryFilterEmptyText);
            boolean hasQuery = pantrySearchInput.getText() != null
                    && pantrySearchInput.getText().length() > 0;
            message.setText(hasQuery ? R.string.no_search_results : R.string.no_expiring_items);
        }
    }

    private void launchAddItem() {
        addEditLauncher.launch(new Intent(this, AddEditItemActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_suggested) {
            startActivity(new Intent(this, SuggestedRecipesActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = AddEditItemActivity.createEditIntent(this, item.getId());
        addEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        int deleted = pantryDao.delete(item.getId());
        if (deleted == 0) {
            Toast.makeText(this, getString(R.string.delete_failed, item.getName()), Toast.LENGTH_SHORT).show();
            return;
        }

        refreshPantryList();

        // Use the screen root: deleting the final row hides the RecyclerView, which otherwise
        // leaves Snackbar without a visible parent on some devices.
        Snackbar.make(findViewById(R.id.main), getString(R.string.item_deleted, item.getName()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    pantryDao.restore(item);
                    refreshPantryList();
                })
                .setAnchorView(addItemFab)
                .show();
    }
}
