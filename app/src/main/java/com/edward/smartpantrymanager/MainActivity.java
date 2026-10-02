package com.edward.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateText;

    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pantryDao = new PantryDao(this);

        recyclerView = findViewById(R.id.pantryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        FloatingActionButton addItemFab = findViewById(R.id.addItemFab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(pantryDao.getAll(), this);
        recyclerView.setAdapter(adapter);

        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> refreshPantryList()
        );

        addItemFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditItemActivity.class);
            addEditLauncher.launch(intent);
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
        if (adapter.getItemCount() == 0) {
            emptyStateText.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            emptyStateText.setVisibility(android.view.View.GONE);
            recyclerView.setVisibility(android.view.View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = AddEditItemActivity.createEditIntent(this, item.getId());
        addEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        pantryDao.delete(item.getId());
        refreshPantryList();
        Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
    }
}