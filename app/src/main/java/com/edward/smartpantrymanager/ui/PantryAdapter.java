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
import com.edward.smartpantrymanager.model.PantryItem;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("0.##");

    public interface OnItemActionListener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private List<PantryItem> items;
    private final OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<PantryItem> newItems) {
        this.items = newItems;
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
            boolean soon = alertsOn && isExpiringSoon(item.getExpiryDate());

            if (soon) {
                holder.expiryText.setText(holder.itemView.getContext().getString(
                        R.string.expiry_soon_format, item.getExpiryDate()));
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

    /** True if the date is today, within the next 3 days, or already past. */
    private boolean isExpiringSoon(String expiry) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            fmt.setLenient(false);
            Date expiryDate = fmt.parse(expiry);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffDays = (expiryDate.getTime() - today.getTimeInMillis()) / (24L * 60 * 60 * 1000);
            return diffDays <= 3;
        } catch (ParseException e) {
            return false;
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
