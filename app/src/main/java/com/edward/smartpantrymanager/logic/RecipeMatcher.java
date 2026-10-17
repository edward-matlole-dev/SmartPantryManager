package com.edward.smartpantrymanager.logic;

import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMatcher {

    public static class MatchResult {
        public final Recipe recipe;
        public final boolean fullyMatched;
        public final int missingCount;
        public final List<String> missingIngredients;

        public MatchResult(Recipe recipe, boolean fullyMatched, int missingCount, List<String> missingIngredients) {
            this.recipe = recipe;
            this.fullyMatched = fullyMatched;
            this.missingCount = missingCount;
            this.missingIngredients = missingIngredients;
        }
    }

    /**
     * Normalizes an ingredient name for comparison:
     * - lowercase
     * - trimmed
     * - naive singular/plural handling (strips a trailing "es" or "s"
     *   unless doing so would leave an unreasonably short word)
     */
    public static String normalize(String name) {
        if (name == null) return "";
        String result = name.trim().toLowerCase();

        if (result.endsWith("es") && result.length() > 4) {
            result = result.substring(0, result.length() - 2);
        } else if (result.endsWith("s") && !result.endsWith("ss") && result.length() > 3) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Builds a lookup of normalized pantry ingredient name -> total quantity available.
     * If the same ingredient appears multiple times (e.g. two separate pantry entries),
     * quantities are summed, as long as the units match (we do not convert between units).
     */
    private static Map<String, Map<String, Double>> buildPantryLookup(List<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> lookup = new HashMap<>();

        for (PantryItem item : pantryItems) {
            String key = normalize(item.getName());
            String unit = item.getUnit() != null ? item.getUnit().trim().toLowerCase() : "";

            lookup.putIfAbsent(key, new HashMap<>());
            Map<String, Double> unitMap = lookup.get(key);
            double existing = unitMap.getOrDefault(unit, 0.0);
            unitMap.put(unit, existing + item.getQuantity());
        }

        return lookup;
    }

    /**
     * Checks a single recipe against the pantry.
     * A recipe only matches if EVERY ingredient is present in at least the required quantity.
     */
    public static MatchResult checkRecipe(Recipe recipe, List<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> pantryLookup = buildPantryLookup(pantryItems);
        List<String> missing = new ArrayList<>();

        for (RecipeIngredient required : recipe.getIngredients()) {
            String key = normalize(required.getIngredientName());
            String unit = required.getUnit() != null ? required.getUnit().trim().toLowerCase() : "";

            Map<String, Double> unitMap = pantryLookup.get(key);
            double available = (unitMap != null) ? unitMap.getOrDefault(unit, 0.0) : 0.0;

            if (available < required.getQuantityRequired()) {
                missing.add(required.getIngredientName());
            }
        }

        boolean fullyMatched = missing.isEmpty();
        return new MatchResult(recipe, fullyMatched, missing.size(), missing);
    }

    /** Returns only recipes where every ingredient is satisfied (the strict suggestions list). */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            MatchResult result = checkRecipe(recipe, pantryItems);
            if (result.fullyMatched) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /** Optional stretch feature: recipes missing exactly 1 ingredient. */
    public static List<MatchResult> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<MatchResult> almostThere = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            MatchResult result = checkRecipe(recipe, pantryItems);
            if (!result.fullyMatched && result.missingCount == 1) {
                almostThere.add(result);
            }
        }
        return almostThere;
    }
}