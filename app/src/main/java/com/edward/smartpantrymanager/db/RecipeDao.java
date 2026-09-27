package com.edward.smartpantrymanager.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class RecipeDao {

    private final PantryDatabaseHelper dbHelper;

    public RecipeDao(Context context) {
        dbHelper = PantryDatabaseHelper.getInstance(context);
    }

    /** Returns all recipes, each with its ingredients populated. */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PantryDatabaseHelper.TABLE_RECIPES,
                null, null, null, null, null,
                PantryDatabaseHelper.COL_RECIPE_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            Recipe recipe = new Recipe();
            recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_ID)));
            recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_NAME)));
            recipe.setPreparationSteps(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_STEPS)));
            recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
            recipes.add(recipe);
        }
        cursor.close();

        return recipes;
    }

    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Recipe recipe = null;

        Cursor cursor = db.query(
                PantryDatabaseHelper.TABLE_RECIPES,
                null,
                PantryDatabaseHelper.COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            recipe = new Recipe();
            recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_ID)));
            recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_NAME)));
            recipe.setPreparationSteps(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RECIPE_STEPS)));
            recipe.setIngredients(getIngredientsForRecipe(db, recipeId));
        }
        cursor.close();

        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        Cursor cursor = db.query(
                PantryDatabaseHelper.TABLE_RECIPE_INGREDIENTS,
                null,
                PantryDatabaseHelper.COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null
        );

        while (cursor.moveToNext()) {
            RecipeIngredient ri = new RecipeIngredient();
            ri.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RI_ID)));
            ri.setRecipeId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RI_RECIPE_ID)));
            ri.setIngredientName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RI_INGREDIENT_NAME)));
            ri.setQuantityRequired(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RI_QUANTITY)));
            ri.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_RI_UNIT)));
            ingredients.add(ri);
        }
        cursor.close();

        return ingredients;
    }
}