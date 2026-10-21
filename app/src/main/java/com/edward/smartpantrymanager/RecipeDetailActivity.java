package com.edward.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edward.smartpantrymanager.db.RecipeDao;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;
import com.edward.smartpantrymanager.ui.InsetsHelper;

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

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new RecipeDao(this).getRecipeById(recipeId);

        if (recipe == null) {
            nameText.setText("Recipe not found");
            return;
        }

        nameText.setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            sb.append("- ")
                    .append(ri.getQuantityRequired())
                    .append(" ")
                    .append(ri.getUnit())
                    .append(" ")
                    .append(ri.getIngredientName())
                    .append("\n");
        }
        ingredientsText.setText(sb.toString().trim());
        stepsText.setText(recipe.getPreparationSteps());
    }

    public static Intent createIntent(Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        return intent;
    }
}