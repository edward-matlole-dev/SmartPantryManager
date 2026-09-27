package com.edward.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.edward.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryDao {

    private final PantryDatabaseHelper dbHelper;

    public PantryDao(Context context) {
        dbHelper = PantryDatabaseHelper.getInstance(context);
    }

    public long insert(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PantryDatabaseHelper.COL_PANTRY_NAME, item.getName());
        values.put(PantryDatabaseHelper.COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(PantryDatabaseHelper.COL_PANTRY_UNIT, item.getUnit());
        values.put(PantryDatabaseHelper.COL_PANTRY_EXPIRY, item.getExpiryDate());
        return db.insert(PantryDatabaseHelper.TABLE_PANTRY_ITEMS, null, values);
    }

    public int update(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PantryDatabaseHelper.COL_PANTRY_NAME, item.getName());
        values.put(PantryDatabaseHelper.COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(PantryDatabaseHelper.COL_PANTRY_UNIT, item.getUnit());
        values.put(PantryDatabaseHelper.COL_PANTRY_EXPIRY, item.getExpiryDate());
        return db.update(PantryDatabaseHelper.TABLE_PANTRY_ITEMS, values,
                PantryDatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(PantryDatabaseHelper.TABLE_PANTRY_ITEMS,
                PantryDatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PantryDatabaseHelper.TABLE_PANTRY_ITEMS,
                null, null, null, null, null,
                PantryDatabaseHelper.COL_PANTRY_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            items.add(mapCursorToItem(cursor));
        }
        cursor.close();

        return items;
    }

    public PantryItem getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        PantryItem item = null;

        Cursor cursor = db.query(
                PantryDatabaseHelper.TABLE_PANTRY_ITEMS,
                null,
                PantryDatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            item = mapCursorToItem(cursor);
        }
        cursor.close();

        return item;
    }

    private PantryItem mapCursorToItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_PANTRY_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_PANTRY_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_PANTRY_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_PANTRY_UNIT)));
        item.setExpiryDate(cursor.getString(cursor.getColumnIndexOrThrow(PantryDatabaseHelper.COL_PANTRY_EXPIRY)));
        return item;
    }
}