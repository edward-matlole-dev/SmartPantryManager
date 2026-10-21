package com.edward.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.edward.smartpantrymanager.model.PantryItem;
import com.edward.smartpantrymanager.model.Recipe;
import com.edward.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule (Section 2.3 of the brief). */
public class RecipeMatcherTest {

    // ------------------------------------------------------------ helpers

    private Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe r = new Recipe(1, name, "steps");
        for (RecipeIngredient ri : ingredients) r.addIngredient(ri);
        return r;
    }

    private RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private PantryItem have(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private Recipe scrambledEggs() {
        return recipe("Scrambled Eggs",
                need("egg", 2, "pcs"), need("butter", 1, "tbsp"), need("milk", 30, "ml"));
    }

    private List<Recipe> suggested(Recipe recipe, PantryItem... pantry) {
        return RecipeMatcher.getSuggestedRecipes(
                Arrays.asList(recipe), new ArrayList<>(Arrays.asList(pantry)));
    }

    // ------------------------------------------------- the strict rule

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 2, "pcs"), have("butter", 1, "tbsp"), have("milk", 30, "ml"));
        assertEquals(1, result.size());
    }

    @Test
    public void oneIngredientMissing_recipeIsExcluded() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 2, "pcs"), have("butter", 1, "tbsp"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void insufficientQuantity_recipeIsExcluded() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 2, "pcs"), have("butter", 1, "tbsp"), have("milk", 20, "ml"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void moreThanRequired_recipeIsSuggested() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 12, "pcs"), have("butter", 5, "tbsp"), have("milk", 500, "ml"));
        assertEquals(1, result.size());
    }

    @Test
    public void emptyPantry_noSuggestions() {
        assertTrue(suggested(scrambledEggs()).isEmpty());
    }

    @Test
    public void duplicatePantryEntries_areSummed() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 1, "pcs"), have("egg", 1, "pcs"),
                have("butter", 1, "tbsp"), have("milk", 30, "ml"));
        assertEquals(1, result.size());
    }

    // ------------------------------------------- name messiness (plurals)

    @Test
    public void pluralPantryName_matchesSingularRecipeName() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("Eggs", 2, "pcs"), have("Butter", 1, "tbsp"), have("Milk", 30, "ml"));
        assertEquals(1, result.size());
    }

    @Test
    public void caseAndWhitespace_areIgnored() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("  EGG ", 2, "pcs"), have("bUtTeR", 1, "tbsp"), have("milk", 30, "ml"));
        assertEquals(1, result.size());
    }

    @Test
    public void tomatoesMatchesTomato() {
        assertEquals(RecipeMatcher.normalize("tomato"), RecipeMatcher.normalize("Tomatoes"));
    }

    @Test
    public void applesMatchesApple() {
        assertEquals(RecipeMatcher.normalize("apple"), RecipeMatcher.normalize("apples"));
    }

    @Test
    public void juicesMatchesJuice() {
        assertEquals(RecipeMatcher.normalize("juice"), RecipeMatcher.normalize("juices"));
    }

    @Test
    public void berriesMatchesBerry() {
        assertEquals(RecipeMatcher.normalize("berry"), RecipeMatcher.normalize("berries"));
    }

    @Test
    public void multiWordNames_singulariseLastWord() {
        assertEquals("soy sauce", RecipeMatcher.normalize("  Soy   Sauces "));
    }

    @Test
    public void wordsThatAreAlreadySingular_areNotMangled() {
        assertEquals("hummus", RecipeMatcher.normalize("hummus"));
        assertEquals("swiss", RecipeMatcher.normalize("swiss"));
        assertEquals("cheese", RecipeMatcher.normalize("cheese"));
    }

    // ---------------------------------------------------- unit handling

    @Test
    public void litresSatisfyMillilitres() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 2, "pcs"), have("butter", 1, "tbsp"), have("milk", 0.03, "l"));
        assertEquals(1, result.size());
    }

    @Test
    public void tooFewLitres_isExcluded() {
        List<Recipe> result = suggested(scrambledEggs(),
                have("egg", 2, "pcs"), have("butter", 1, "tbsp"), have("milk", 0.02, "l"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void kilogramsSatisfyGrams() {
        Recipe friedRice = recipe("Fried Rice", need("rice", 300, "g"));
        assertEquals(1, suggested(friedRice, have("rice", 1, "kg")).size());
    }

    @Test
    public void unitAliases_areRecognised() {
        Recipe friedRice = recipe("Fried Rice", need("rice", 300, "g"));
        assertEquals(1, suggested(friedRice, have("rice", 500, "Grams")).size());
    }

    @Test
    public void incompatibleUnits_doNotMatch() {
        Recipe pasta = recipe("Pasta", need("pasta", 200, "g"));
        assertTrue(suggested(pasta, have("pasta", 200, "ml")).isEmpty());
    }

    // -------------------------------------------------- "Almost There"

    @Test
    public void almostThere_listsRecipesMissingExactlyOne() {
        List<RecipeMatcher.MatchResult> result = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(scrambledEggs()),
                Arrays.asList(have("egg", 2, "pcs"), have("butter", 1, "tbsp")));
        assertEquals(1, result.size());
        assertEquals("milk", result.get(0).missingIngredients.get(0));
    }

    @Test
    public void almostThere_excludesRecipesMissingTwoOrMore() {
        List<RecipeMatcher.MatchResult> result = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(scrambledEggs()),
                Arrays.asList(have("egg", 2, "pcs")));
        assertTrue(result.isEmpty());
    }

    @Test
    public void almostThere_neverContainsFullyMatchedRecipes() {
        List<RecipeMatcher.MatchResult> result = RecipeMatcher.getAlmostThereRecipes(
                Arrays.asList(scrambledEggs()),
                Arrays.asList(have("egg", 2, "pcs"), have("butter", 1, "tbsp"),
                        have("milk", 30, "ml")));
        assertTrue(result.isEmpty());
    }

    @Test
    public void checkRecipe_reportsFullMatchFlag() {
        RecipeMatcher.MatchResult result = RecipeMatcher.checkRecipe(scrambledEggs(),
                Arrays.asList(have("egg", 2, "pcs")));
        assertFalse(result.fullyMatched);
        assertEquals(2, result.missingCount);
    }
}