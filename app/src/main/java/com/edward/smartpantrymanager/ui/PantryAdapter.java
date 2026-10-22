package com.edward.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.edward.smartpantrymanager.R;
import com.edward.smartpantrymanager.SettingsActivity;
import com.edward.smartpantrymanager.logic.ExpiryUtils;
import com.edward.smartpantrymanager.model.PantryItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("0.##");

    public interface OnItemActionListener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private List<PantryItem> allItems;
    private List<PantryItem> items;
    private final OnItemActionListener listener;
    private String query = "";
    private boolean expiringOnly;

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.allItems = new ArrayList<>(items);
        this.items = new ArrayList<>(items);
        this.listener = listener;
        setHasStableIds(true);
    }

    public void updateItems(List<PantryItem> newItems) {
        this.allItems = new ArrayList<>(newItems);
        applyFilters();
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        applyFilters();
    }

    public void setExpiringOnly(boolean expiringOnly) {
        this.expiringOnly = expiringOnly;
        applyFilters();
    }

    public int getTotalItemCount() {
        return allItems.size();
    }

    private void applyFilters() {
        List<PantryItem> filtered = new ArrayList<>();
        for (PantryItem item : allItems) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.getDefault());
            boolean matchesQuery = query.isEmpty() || name.contains(query);
            boolean matchesExpiry = !expiringOnly || (item.getExpiryDate() != null
                    && !item.getExpiryDate().isEmpty() && isExpiringSoon(item.getExpiryDate()));
            if (matchesQuery && matchesExpiry) filtered.add(item);
        }
        this.items = filtered;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.nameText.setText(item.getName());
        holder.initialText.setText(getInitial(item.getName()));

        String quantityText = QUANTITY_FORMAT.format(item.getQuantity())
                + " " + (item.getUnit() != null ? item.getUnit() : "");
        holder.quantityText.setText(quantityText);

        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            boolean alertsOn = SettingsActivity.isExpiryAlertsEnabled(holder.itemView.getContext());
            ExpiryUtils.Status expiryStatus = ExpiryUtils.getStatus(item.getExpiryDate());
            boolean soon = alertsOn && isExpiringSoon(item.getExpiryDate());

            if (soon) {
                holder.expiryText.setText(getExpiryText(holder, item.getExpiryDate(), expiryStatus));
                holder.expiryText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                        R.color.pantry_expiry));
            } else {
                holder.expiryText.setText(holder.itemView.getContext().getString(
                        R.string.expiry_format, item.getExpiryDate()));
                holder.expiryText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                        R.color.pantry_muted));
            }
            holder.expiryText.setVisibility(View.VISIBLE);
        } else {
            holder.expiryText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(item);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    /** True if the date is today, within the next 3 days, or already past. */
    public static boolean isExpiringSoon(String expiry) {
        return ExpiryUtils.isExpiringSoon(expiry);
    }

    private String getExpiryText(PantryViewHolder holder, String expiryDate,
                                 ExpiryUtils.Status status) {
        switch (status) {
            case EXPIRED:
                return holder.itemView.getContext().getString(R.string.expired_format, expiryDate);
            case TODAY:
                return holder.itemView.getContext().getString(R.string.expires_today);
            case TOMORROW:
                return holder.itemView.getContext().getString(R.string.expires_tomorrow);
            default:
                return holder.itemView.getContext().getString(
                        R.string.expiry_soon_format, expiryDate);
        }
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView initialText;
        TextView nameText;
        TextView quantityText;
        TextView expiryText;
        ImageButton deleteButton;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            initialText = itemView.findViewById(R.id.itemInitialText);
            nameText = itemView.findViewById(R.id.itemNameText);
            quantityText = itemView.findViewById(R.id.itemQuantityText);
            expiryText = itemView.findViewById(R.id.itemExpiryText);
            deleteButton = itemView.findViewById(R.id.deleteItemButton);
        }
    }
}
