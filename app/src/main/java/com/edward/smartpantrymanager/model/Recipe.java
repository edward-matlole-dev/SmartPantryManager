package com.edward.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String preparationSteps;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {}

    public Recipe(long id, String name, String preparationSteps) {
        this.id = id;
        this.name = name;
        this.preparationSteps = preparationSteps;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPreparationSteps() { return preparationSteps; }
    public void setPreparationSteps(String preparationSteps) { this.preparationSteps = preparationSteps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
    public void addIngredient(RecipeIngredient ingredient) { this.ingredients.add(ingredient); }
}