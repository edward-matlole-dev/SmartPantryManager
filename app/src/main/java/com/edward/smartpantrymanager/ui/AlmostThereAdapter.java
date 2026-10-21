package com.edward.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.R;
import com.edward.smartpantrymanager.logic.RecipeMatcher;
import com.edward.smartpantrymanager.model.Recipe;

import java.util.List;

/** Shows recipes that are missing exactly one ingredient, with the missing item named. */
public class AlmostThereAdapter extends RecyclerView.Adapter<AlmostThereAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    private List<RecipeMatcher.MatchResult> results;
    private final OnRecipeClickListener listener;

    public AlmostThereAdapter(List<RecipeMatcher.MatchResult> results, OnRecipeClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    public void updateResults(List<RecipeMatcher.MatchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeMatcher.MatchResult result = results.get(position);

        holder.nameText.setText(result.recipe.getName());
        holder.infoText.setText("Missing: " + result.missingIngredients.get(0));
        holder.infoText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.pantry_accent));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClicked(result.recipe);
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView infoText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.recipeNameText);
            infoText = itemView.findViewById(R.id.recipeIngredientCountText);
        }
    }
}