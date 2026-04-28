package com.example.dianciguanli.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BatteryManager.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BATTERY = "battery";
    private static final String TABLE_RECORD = "record";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createBatteryTable = "CREATE TABLE " + TABLE_BATTERY + " (" +
                "id TEXT PRIMARY KEY, " +
                "model TEXT, " +
                "specification TEXT, " +
                "batch TEXT, " +
                "quantity INTEGER, " +
                "create_time TEXT, " +
                "update_time TEXT)";
        db.execSQL(createBatteryTable);

        String createRecordTable = "CREATE TABLE " + TABLE_RECORD + " (" +
                "id TEXT PRIMARY KEY, " +
                "battery_id TEXT, " +
                "type INTEGER, " +
                "quantity INTEGER, " +
                "operator TEXT, " +
                "create_time TEXT)";
        db.execSQL(createRecordTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BATTERY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
        onCreate(db);
    }

    public long insertBattery(Battery battery) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", battery.getId());
        values.put("model", battery.getModel());
        values.put("specification", battery.getSpecification());
        values.put("batch", battery.getBatch());
        values.put("quantity", battery.getQuantity());
        values.put("create_time", battery.getCreateTime());
        values.put("update_time", battery.getUpdateTime());
        long result = db.insert(TABLE_BATTERY, null, values);
        db.close();
        return result;
    }

    public int updateBattery(Battery battery) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("model", battery.getModel());
        values.put("specification", battery.getSpecification());
        values.put("batch", battery.getBatch());
        values.put("quantity", battery.getQuantity());
        values.put("update_time", battery.getUpdateTime());
        int result = db.update(TABLE_BATTERY, values, "id = ?", new String[]{battery.getId()});
        db.close();
        return result;
    }

    public Battery getBatteryById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BATTERY, null, "id = ?", new String[]{id}, null, null, null);
        Battery battery = null;
        if (cursor.moveToFirst()) {
            battery = new Battery();
            battery.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            battery.setModel(cursor.getString(cursor.getColumnIndexOrThrow("model")));
            battery.setSpecification(cursor.getString(cursor.getColumnIndexOrThrow("specification")));
            battery.setBatch(cursor.getString(cursor.getColumnIndexOrThrow("batch")));
            battery.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
            battery.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow("create_time")));
            battery.setUpdateTime(cursor.getString(cursor.getColumnIndexOrThrow("update_time")));
        }
        cursor.close();
        db.close();
        return battery;
    }

    public List<Battery> getAllBatteries() {
        List<Battery> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BATTERY, null, null, null, null, null, "update_time DESC");
        while (cursor.moveToNext()) {
            Battery battery = new Battery();
            battery.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            battery.setModel(cursor.getString(cursor.getColumnIndexOrThrow("model")));
            battery.setSpecification(cursor.getString(cursor.getColumnIndexOrThrow("specification")));
            battery.setBatch(cursor.getString(cursor.getColumnIndexOrThrow("batch")));
            battery.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
            battery.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow("create_time")));
            battery.setUpdateTime(cursor.getString(cursor.getColumnIndexOrThrow("update_time")));
            list.add(battery);
        }
        cursor.close();
        db.close();
        return list;
    }

    public int deleteBattery(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BATTERY, "id = ?", new String[]{id});
        db.close();
        return result;
    }

    public long insertRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", record.getId());
        values.put("battery_id", record.getBatteryId());
        values.put("type", record.getType());
        values.put("quantity", record.getQuantity());
        values.put("operator", record.getOperator());
        values.put("create_time", record.getCreateTime());
        long result = db.insert(TABLE_RECORD, null, values);
        db.close();
        return result;
    }

    public List<Record> getRecordsByDate(String date) {
        List<Record> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECORD, null, "create_time LIKE ?", new String[]{date + "%"}, null, null, "create_time DESC");
        while (cursor.moveToNext()) {
            Record record = new Record();
            record.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            record.setBatteryId(cursor.getString(cursor.getColumnIndexOrThrow("battery_id")));
            record.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
            record.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
            record.setOperator(cursor.getString(cursor.getColumnIndexOrThrow("operator")));
            record.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow("create_time")));
            list.add(record);
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Record> getAllRecords() {
        List<Record> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECORD, null, null, null, null, null, "create_time DESC");
        while (cursor.moveToNext()) {
            Record record = new Record();
            record.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            record.setBatteryId(cursor.getString(cursor.getColumnIndexOrThrow("battery_id")));
            record.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
            record.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
            record.setOperator(cursor.getString(cursor.getColumnIndexOrThrow("operator")));
            record.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow("create_time")));
            list.add(record);
        }
        cursor.close();
        db.close();
        return list;
    }
}