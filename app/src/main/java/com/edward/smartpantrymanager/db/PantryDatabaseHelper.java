package com.edward.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // pantry_items columns
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // recipes columns
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "preparation_steps";

    // recipe_ingredients columns
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "quantity_required";
    public static final String COL_RI_UNIT = "unit";

    private static PantryDatabaseHelper instance;

    public static synchronized PantryDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PantryDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PantryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY_ITEMS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Seeds the recipes table with 18 starter recipes on first run.
     * This will be filled in fully in the next step.
     */
    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                "1. Crack eggs into a bowl and whisk.\n2. Melt butter in a pan over medium heat.\n3. Pour in eggs, stir gently until soft curds form.\n4. Season and serve.",
                new Object[][]{
                        {"egg", 2.0, "pcs"},
                        {"butter", 1.0, "tbsp"},
                        {"milk", 30.0, "ml"}
                });

        insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Place cheese between unbuttered sides.\n3. Grill in a pan until golden on both sides.",
                new Object[][]{
                        {"bread", 2.0, "slice"},
                        {"cheese", 1.0, "slice"},
                        {"butter", 1.0, "tbsp"}
                });

        insertRecipe(db, "Tomato Pasta",
                "1. Boil pasta until al dente.\n2. Saute garlic in olive oil.\n3. Add chopped tomato, simmer 5 minutes.\n4. Toss with pasta and serve.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"tomato", 2.0, "pcs"},
                        {"garlic", 1.0, "clove"},
                        {"olive oil", 2.0, "tbsp"}
                });

        insertRecipe(db, "Vegetable Fried Rice",
                "1. Heat oil in a wok.\n2. Add chopped carrot and onion, stir-fry.\n3. Add cooked rice and soy sauce.\n4. Stir-fry until heated through.",
                new Object[][]{
                        {"rice", 300.0, "g"},
                        {"carrot", 1.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"soy sauce", 2.0, "tbsp"}
                });

        insertRecipe(db, "Banana Pancakes",
                "1. Mash banana in a bowl.\n2. Mix in flour, egg, and milk to form batter.\n3. Cook spoonfuls on a hot greased pan until golden.",
                new Object[][]{
                        {"banana", 1.0, "pcs"},
                        {"flour", 100.0, "g"},
                        {"egg", 1.0, "pcs"},
                        {"milk", 100.0, "ml"}
                });

        insertRecipe(db, "Chicken Sandwich",
                "1. Slice cooked chicken.\n2. Spread butter on bread.\n3. Layer chicken and lettuce between bread slices.",
                new Object[][]{
                        {"bread", 2.0, "slice"},
                        {"chicken", 100.0, "g"},
                        {"lettuce", 1.0, "leaf"},
                        {"butter", 1.0, "tbsp"}
                });

        insertRecipe(db, "Cheese Omelette",
                "1. Whisk eggs with a splash of milk.\n2. Pour into a hot buttered pan.\n3. Sprinkle cheese on top, fold, and serve.",
                new Object[][]{
                        {"egg", 3.0, "pcs"},
                        {"cheese", 1.0, "slice"},
                        {"butter", 1.0, "tbsp"},
                        {"milk", 20.0, "ml"}
                });

        insertRecipe(db, "Garlic Butter Rice",
                "1. Melt butter in a pan.\n2. Saute minced garlic until fragrant.\n3. Stir in cooked rice and season to taste.",
                new Object[][]{
                        {"rice", 250.0, "g"},
                        {"garlic", 2.0, "clove"},
                        {"butter", 2.0, "tbsp"}
                });

        insertRecipe(db, "Simple Salad",
                "1. Chop lettuce and tomato.\n2. Slice onion thinly.\n3. Toss together with olive oil.",
                new Object[][]{
                        {"lettuce", 2.0, "leaf"},
                        {"tomato", 1.0, "pcs"},
                        {"onion", 0.5, "pcs"},
                        {"olive oil", 1.0, "tbsp"}
                });

        insertRecipe(db, "Milk Toast",
                "1. Toast the bread.\n2. Warm milk slightly.\n3. Spread butter on toast and drizzle warm milk over, sprinkle sugar.",
                new Object[][]{
                        {"bread", 2.0, "slice"},
                        {"milk", 100.0, "ml"},
                        {"butter", 1.0, "tbsp"},
                        {"sugar", 1.0, "tbsp"}
                });

        insertRecipe(db, "Egg Fried Rice",
                "1. Scramble eggs in a hot wok.\n2. Add cooked rice and soy sauce.\n3. Stir-fry until combined and heated through.",
                new Object[][]{
                        {"rice", 300.0, "g"},
                        {"egg", 2.0, "pcs"},
                        {"soy sauce", 2.0, "tbsp"}
                });

        insertRecipe(db, "Carrot Soup",
                "1. Saute chopped onion and garlic.\n2. Add chopped carrot and water, simmer until soft.\n3. Blend until smooth and season.",
                new Object[][]{
                        {"carrot", 3.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 1.0, "clove"}
                });

        insertRecipe(db, "Cheesy Garlic Bread",
                "1. Slice bread and spread with butter and minced garlic.\n2. Top with cheese.\n3. Grill until cheese melts and bread is crisp.",
                new Object[][]{
                        {"bread", 4.0, "slice"},
                        {"cheese", 2.0, "slice"},
                        {"garlic", 2.0, "clove"},
                        {"butter", 2.0, "tbsp"}
                });

        insertRecipe(db, "Banana Milkshake",
                "1. Add banana and milk to a blender.\n2. Add sugar to taste.\n3. Blend until smooth and serve chilled.",
                new Object[][]{
                        {"banana", 2.0, "pcs"},
                        {"milk", 250.0, "ml"},
                        {"sugar", 2.0, "tbsp"}
                });

        insertRecipe(db, "Chicken Fried Rice",
                "1. Stir-fry diced chicken until cooked.\n2. Add chopped carrot and onion, cook until soft.\n3. Add rice and soy sauce, stir-fry until combined.",
                new Object[][]{
                        {"chicken", 150.0, "g"},
                        {"rice", 300.0, "g"},
                        {"carrot", 1.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"soy sauce", 2.0, "tbsp"}
                });

        insertRecipe(db, "Onion Omelette",
                "1. Saute chopped onion until soft.\n2. Whisk eggs and pour over onion in the pan.\n3. Cook until set, fold and serve.",
                new Object[][]{
                        {"egg", 2.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"butter", 1.0, "tbsp"}
                });

        insertRecipe(db, "Buttered Pasta",
                "1. Boil pasta until al dente.\n2. Toss hot pasta with butter and minced garlic.\n3. Season and serve.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"butter", 2.0, "tbsp"},
                        {"garlic", 1.0, "clove"}
                });

        insertRecipe(db, "Lettuce Chicken Wrap",
                "1. Slice cooked chicken.\n2. Lay lettuce leaves flat.\n3. Fill with chicken and roll up tightly.",
                new Object[][]{
                        {"lettuce", 3.0, "leaf"},
                        {"chicken", 100.0, "g"}
                });
    }

    private void insertRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(COL_RI_RECIPE_ID, recipeId);
            ingredientValues.put(COL_RI_INGREDIENT_NAME, (String) ingredient[0]);
            ingredientValues.put(COL_RI_QUANTITY, (Double) ingredient[1]);
            ingredientValues.put(COL_RI_UNIT, (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
    }
}