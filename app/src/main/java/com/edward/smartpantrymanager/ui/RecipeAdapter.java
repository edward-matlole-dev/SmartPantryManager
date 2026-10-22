package com.edward.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.R;
import com.edward.smartpantrymanager.model.Recipe;

import java.util.List;
import java.util.Locale;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
        setHasStableIds(true);
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.nameText.setText(recipe.getName());
        holder.initialText.setText(getInitial(recipe.getName()));

        int count = recipe.getIngredients().size();
        String countText = holder.itemView.getResources().getQuantityString(
                R.plurals.ingredient_count, count, count);
        holder.countText.setText(holder.itemView.getContext().getString(
                R.string.recipe_ready_to_cook, countText));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClicked(recipe);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    @Override
    public long getItemId(int position) {
        return recipes.get(position).getId();
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView initialText;
        TextView nameText;
        TextView countText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            initialText = itemView.findViewById(R.id.recipeInitialText);
            nameText = itemView.findViewById(R.id.recipeNameText);
            countText = itemView.findViewById(R.id.recipeIngredientCountText);
        }
    }
}
