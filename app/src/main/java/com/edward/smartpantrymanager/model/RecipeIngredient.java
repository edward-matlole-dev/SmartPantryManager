package com.edward.smartpantrymanager.model;

public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String ingredientName;
    private double quantityRequired;
    private String unit;

    public RecipeIngredient() {}

    public RecipeIngredient(String ingredientName, double quantityRequired, String unit) {
        this.ingredientName = ingredientName;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public double getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(double quantityRequired) { this.quantityRequired = quantityRequired; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}