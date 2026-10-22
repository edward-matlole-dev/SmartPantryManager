package com.edward.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edward.smartpantrymanager.db.PantryDao;
import com.edward.smartpantrymanager.db.RecipeDao;
import com.edward.smartpantrymanager.logic.RecipeMatcher;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;
import com.edward.smartpantrymanager.ui.InsetsHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        InsetsHelper.applySystemBarInsets(this);

        TextView nameText = findViewById(R.id.detailRecipeName);
        TextView ingredientsText = findViewById(R.id.detailIngredients);
        TextView stepsText = findViewById(R.id.detailSteps);
        TextView pantryStatusTitle = findViewById(R.id.detailPantryStatusTitle);
        TextView pantryStatusText = findViewById(R.id.detailPantryStatusText);
        View pantryStatusCard = findViewById(R.id.detailPantryStatusCard);
        Chip ingredientCount = findViewById(R.id.detailIngredientCount);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new RecipeDao(this).getRecipeById(recipeId);

        if (recipe == null) {
            nameText.setText(R.string.recipe_not_found);
            ingredientCount.setVisibility(android.view.View.GONE);
            ingredientsText.setText("");
            stepsText.setText("");
            pantryStatusTitle.setText("");
            pantryStatusText.setText("");
            pantryStatusCard.setVisibility(View.GONE);
            return;
        }

        nameText.setText(recipe.getName());
        int count = recipe.getIngredients().size();
        ingredientCount.setText(getResources().getQuantityString(
                R.plurals.ingredient_count, count, count));

        StringBuilder sb = new StringBuilder();
        DecimalFormat quantityFormat = new DecimalFormat("0.##");
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(getString(R.string.recipe_ingredient_line,
                    quantityFormat.format(ri.getQuantityRequired()),
                    ri.getUnit(), ri.getIngredientName()));
        }
        ingredientsText.setText(sb);
        stepsText.setText(recipe.getPreparationSteps());
        showPantryStatus(recipe, pantryStatusTitle, pantryStatusText);
    }

    /** Explains the strict match result without ever treating an incomplete recipe as ready. */
    private void showPantryStatus(Recipe recipe, TextView title, TextView message) {
        RecipeMatcher.MatchResult matchResult = RecipeMatcher.checkRecipe(recipe,
                new PantryDao(this).getAll());

        if (matchResult.fullyMatched) {
            title.setText(R.string.recipe_ready_status);
            message.setText(R.string.recipe_ready_status_detail);
            return;
        }

        int missingCount = matchResult.missingCount;
        title.setText(getResources().getQuantityString(
                R.plurals.recipe_missing_status, missingCount, missingCount));
        message.setText(getString(R.string.recipe_missing_status_detail,
                TextUtils.join(", ", matchResult.missingIngredients)));
    }

    public static Intent createIntent(Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        return intent;
    }
}
