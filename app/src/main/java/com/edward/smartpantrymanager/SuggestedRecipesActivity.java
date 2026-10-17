package com.edward.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.db.RecipeDao;
import com.edward.smartpantrymanager.logic.RecipeMatcher;
import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.ui.RecipeAdapter;

import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView noRecipesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        pantryDao = new PantryDao(this);
        recipeDao = new RecipeDao(this);

        recyclerView = findViewById(R.id.recipeRecyclerView);
        noRecipesText = findViewById(R.id.noRecipesText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new java.util.ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = pantryDao.getAll();
        List<Recipe> allRecipes = recipeDao.getAllRecipesWithIngredients();
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);

        adapter.updateRecipes(suggested);

        if (suggested.isEmpty()) {
            noRecipesText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noRecipesText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        // Recipe Detail screen is built in the next step
        Toast.makeText(this, "Detail screen coming next: " + recipe.getName(),
                Toast.LENGTH_SHORT).show();
    }
}