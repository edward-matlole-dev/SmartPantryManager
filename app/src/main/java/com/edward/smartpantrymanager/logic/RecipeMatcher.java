package com.edward.smartpantrymanager.logic;

import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business logic of the app: decides which recipes can be cooked from the pantry.
 *
 * Strict rule: a recipe qualifies ONLY if every ingredient is present in at least the
 * required quantity. Matching tolerates case, whitespace, singular/plural names and
 * compatible units (e.g. kg vs g, litres vs ml).
 */
public class RecipeMatcher {

    /** Tolerance for floating point comparison, e.g. 0.03 l * 1000 = 30.000000000000004 ml. */
    private static final double EPSILON = 1e-9;

    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    private static final Map<String, String> BASE_UNITS = new HashMap<>();
    private static final Map<String, Double> BASE_FACTORS = new HashMap<>();

    static {
        // Spelling variations -> canonical unit
        alias("pcs", "pc", "piece", "pieces");
        alias("g", "gram", "grams", "gr");
        alias("kg", "kilogram", "kilograms", "kgs");
        alias("ml", "milliliter", "milliliters", "millilitre", "millilitres");
        alias("l", "liter", "liters", "litre", "litres");
        alias("tbsp", "tablespoon", "tablespoons", "tbs");
        alias("tsp", "teaspoon", "teaspoons");
        alias("cup", "cups");
        alias("slice", "slices");
        alias("clove", "cloves");
        alias("leaf", "leaves");

        // Canonical unit -> base unit and conversion factor.
        // Mass converts to grams, volume converts to millilitres.
        base("g", "g", 1);
        base("kg", "g", 1000);
        base("ml", "ml", 1);
        base("l", "ml", 1000);
        base("tsp", "ml", 5);
        base("tbsp", "ml", 15);
        base("cup", "ml", 240);
    }

    private static void alias(String canonical, String... names) {
        UNIT_ALIASES.put(canonical, canonical);
        for (String name : names) {
            UNIT_ALIASES.put(name, canonical);
        }
    }

    private static void base(String unit, String baseUnit, double factor) {
        BASE_UNITS.put(unit, baseUnit);
        BASE_FACTORS.put(unit, factor);
    }

    public static class MatchResult {
        public final Recipe recipe;
        public final boolean fullyMatched;
        public final int missingCount;
        public final List<String> missingIngredients;

        public MatchResult(Recipe recipe, boolean fullyMatched, int missingCount,
                           List<String> missingIngredients) {
            this.recipe = recipe;
            this.fullyMatched = fullyMatched;
            this.missingCount = missingCount;
            this.missingIngredients = missingIngredients;
        }
    }

    // ---------------------------------------------------------------- names

    /**
     * Normalises an ingredient name for comparison: lowercase, trimmed, single spaces,
     * and the last word reduced to its singular form ("Tomatoes" -> "tomato").
     */
    public static String normalize(String name) {
        if (name == null) return "";
        String cleaned = name.trim().toLowerCase().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) return "";

        int lastSpace = cleaned.lastIndexOf(' ');
        String prefix = lastSpace >= 0 ? cleaned.substring(0, lastSpace + 1) : "";
        return prefix + singularize(cleaned.substring(lastSpace + 1));
    }

    /** Simple rule-based singular form. Not full NLP, but covers common food words. */
    private static String singularize(String word) {
        if (word.length() <= 3) return word;

        if (word.length() > 4 && word.endsWith("ies")) {                 // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") || word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);                   // tomatoes -> tomato
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);                   // apples -> apple
        }
        return word;                                                       // hummus, swiss stay
    }

    // ---------------------------------------------------------------- units

    private static String canonicalUnit(String unit) {
        if (unit == null) return "";
        String cleaned = unit.trim().toLowerCase();
        if (cleaned.endsWith(".")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        String canonical = UNIT_ALIASES.get(cleaned);
        return canonical != null ? canonical : cleaned;
    }

    /** The unit that quantities are compared in (e.g. kg -> g, tbsp -> ml). */
    private static String baseUnitOf(String unit) {
        String canonical = canonicalUnit(unit);
        String base = BASE_UNITS.get(canonical);
        return base != null ? base : canonical;
    }

    /** Converts a quantity into its base unit. */
    private static double toBaseAmount(String unit, double quantity) {
        Double factor = BASE_FACTORS.get(canonicalUnit(unit));
        return quantity * (factor != null ? factor : 1.0);
    }

    // -------------------------------------------------------------- matching

    /**
     * Builds: normalised ingredient name -> (base unit -> total quantity available).
     * Duplicate pantry entries for the same ingredient are summed.
     */
    private static Map<String, Map<String, Double>> buildPantryLookup(List<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> lookup = new HashMap<>();

        for (PantryItem item : pantryItems) {
            String key = normalize(item.getName());
            String unit = baseUnitOf(item.getUnit());
            double amount = toBaseAmount(item.getUnit(), item.getQuantity());

            lookup.putIfAbsent(key, new HashMap<>());
            Map<String, Double> unitMap = lookup.get(key);
            unitMap.put(unit, unitMap.getOrDefault(unit, 0.0) + amount);
        }
        return lookup;
    }

    private static MatchResult checkRecipe(Recipe recipe, Map<String, Map<String, Double>> pantryLookup) {
        List<String> missing = new ArrayList<>();

        for (RecipeIngredient required : recipe.getIngredients()) {
            Map<String, Double> unitMap = pantryLookup.get(normalize(required.getIngredientName()));
            double available = (unitMap != null)
                    ? unitMap.getOrDefault(baseUnitOf(required.getUnit()), 0.0) : 0.0;
            double needed = toBaseAmount(required.getUnit(), required.getQuantityRequired());

            if (available + EPSILON < needed) {
                missing.add(required.getIngredientName());
            }
        }
        return new MatchResult(recipe, missing.isEmpty(), missing.size(), missing);
    }

    /** Checks a single recipe against the pantry. */
    public static MatchResult checkRecipe(Recipe recipe, List<PantryItem> pantryItems) {
        return checkRecipe(recipe, buildPantryLookup(pantryItems));
    }

    /** Strict suggestions: only recipes where EVERY ingredient is satisfied. */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> lookup = buildPantryLookup(pantryItems);
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (checkRecipe(recipe, lookup).fullyMatched) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /** Bonus list: recipes missing exactly ONE ingredient. Never mixed with strict suggestions. */
    public static List<MatchResult> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        Map<String, Map<String, Double>> lookup = buildPantryLookup(pantryItems);
        List<MatchResult> almostThere = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            MatchResult result = checkRecipe(recipe, lookup);
            if (!result.fullyMatched && result.missingCount == 1) {
                almostThere.add(result);
            }
        }
        return almostThere;
    }
}