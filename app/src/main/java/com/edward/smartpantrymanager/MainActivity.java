package com.edward.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.edward.smartpantrymanager.ui.InsetsHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private TextView pantryCountText;

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
        pantryCountText = findViewById(R.id.pantryCountText);
        FloatingActionButton addItemFab = findViewById(R.id.addItemFab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(pantryDao.getAll(), this);
        recyclerView.setAdapter(adapter);

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> refreshPantryList()
        );

        addItemFab.setOnClickListener(v -> launchAddItem());
        findViewById(R.id.emptyAddButton).setOnClickListener(v -> launchAddItem());

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
        int itemCount = adapter.getItemCount();
        pantryCountText.setText(getResources().getQuantityString(
                R.plurals.pantry_item_count, itemCount, itemCount));
        boolean isEmpty = itemCount == 0;
        emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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

        Snackbar.make(recyclerView, getString(R.string.item_deleted, item.getName()), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    pantryDao.restore(item);
                    refreshPantryList();
                })
                .setAnchorView(R.id.addItemFab)
                .show();
    }
}
