package com.edward.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.db.RecipeDao;
import com.edward.smartpantrymanager.logic.RecipeMatcher;
import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.ui.AlmostThereAdapter;
import com.edward.smartpantrymanager.ui.InsetsHelper;
import com.edward.smartpantrymanager.ui.RecipeAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows two clearly separated lists:
 *  1. Suggested Recipes - strict matches only (every ingredient available).
 *  2. Almost There - recipes missing exactly one ingredient (bonus feature).
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener, AlmostThereAdapter.OnRecipeClickListener {

    private PantryDao pantryDao;
    private RecipeDao recipeDao;

    private RecipeAdapter suggestedAdapter;
    private AlmostThereAdapter almostThereAdapter;

    private RecyclerView suggestedRecycler, almostThereRecycler;
    private View noRecipesText, noAlmostThereText;
    private TextView recipeMatchCountText, suggestedCountText, almostThereCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        InsetsHelper.applySystemBarInsets(this);

        pantryDao = new PantryDao(this);
        recipeDao = new RecipeDao(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        suggestedRecycler = findViewById(R.id.recipeRecyclerView);
        almostThereRecycler = findViewById(R.id.almostThereRecyclerView);
        noRecipesText = findViewById(R.id.noRecipesText);
        noAlmostThereText = findViewById(R.id.noAlmostThereText);
        recipeMatchCountText = findViewById(R.id.recipeMatchCountText);
        suggestedCountText = findViewById(R.id.suggestedCountText);
        almostThereCountText = findViewById(R.id.almostThereCountText);

        suggestedRecycler.setLayoutManager(new LinearLayoutManager(this));
        almostThereRecycler.setLayoutManager(new LinearLayoutManager(this));

        suggestedAdapter = new RecipeAdapter(new ArrayList<>(), this);
        almostThereAdapter = new AlmostThereAdapter(new ArrayList<>(), this);
        suggestedRecycler.setAdapter(suggestedAdapter);
        almostThereRecycler.setAdapter(almostThereAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions(); // recalculated every time the screen becomes visible
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = pantryDao.getAll();
        List<Recipe> allRecipes = recipeDao.getAllRecipesWithIngredients();

        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);
        List<RecipeMatcher.MatchResult> almostThere =
                RecipeMatcher.getAlmostThereRecipes(allRecipes, pantry);

        suggestedAdapter.updateRecipes(suggested);
        almostThereAdapter.updateResults(almostThere);

        recipeMatchCountText.setText(getResources().getQuantityString(
                R.plurals.recipe_ready_count, suggested.size(), suggested.size()));
        suggestedCountText.setText(String.valueOf(suggested.size()));
        almostThereCountText.setText(String.valueOf(almostThere.size()));

        showEmptyState(suggested.isEmpty(), suggestedRecycler, noRecipesText);
        showEmptyState(almostThere.isEmpty(), almostThereRecycler, noAlmostThereText);
    }

    private void showEmptyState(boolean isEmpty, View list, View emptyMessage) {
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        startActivity(RecipeDetailActivity.createIntent(this, recipe.getId()));
    }
}
