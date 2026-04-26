package com.example.myapk;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "WarehouseDB";
    private static final int DB_VERSION = 4;
    private static final String TABLE_GOODS = "goods";
    private static final String TABLE_LOGS = "operation_logs";
    private static final String TABLE_WAREHOUSES = "warehouses";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_USER = "user";
    private static final String TABLE_DAILY_SUMMARY = "daily_summary";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createWarehousesTable = "CREATE TABLE " + TABLE_WAREHOUSES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT UNIQUE)";
        db.execSQL(createWarehousesTable);

        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT UNIQUE)";
        db.execSQL(createCategoriesTable);

        String createGoodsTable = "CREATE TABLE " + TABLE_GOODS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "code TEXT UNIQUE,"
                + "count INTEGER,"
                + "price REAL,"
                + "remark TEXT,"
                + "warehouse_id INTEGER DEFAULT 1,"
                + "category_id INTEGER DEFAULT 1,"
                + "min_stock INTEGER DEFAULT 0,"
                + "FOREIGN KEY (warehouse_id) REFERENCES " + TABLE_WAREHOUSES + "(id),"
                + "FOREIGN KEY (category_id) REFERENCES " + TABLE_CATEGORIES + "(id))";
        db.execSQL(createGoodsTable);

        String createLogsTable = "CREATE TABLE " + TABLE_LOGS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "operation_time TEXT,"
                + "product_code TEXT,"
                + "product_name TEXT,"
                + "quantity INTEGER,"
                + "price REAL,"
                + "total REAL,"
                + "operation_type TEXT)";
        db.execSQL(createLogsTable);

        String createUserTable = "CREATE TABLE " + TABLE_USER + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "password TEXT)";
        db.execSQL(createUserTable);

        String createDailySummaryTable = "CREATE TABLE " + TABLE_DAILY_SUMMARY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "product_name TEXT,"
                + "product_code TEXT,"
                + "in_quantity INTEGER DEFAULT 0,"
                + "out_quantity INTEGER DEFAULT 0,"
                + "price REAL,"
                + "total_amount REAL,"
                + "operation_type TEXT,"
                + "category TEXT,"
                + "spec TEXT,"
                + "customer TEXT,"
                + "summary_date TEXT)";
        db.execSQL(createDailySummaryTable);

        insertDefaultData(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        ContentValues warehouseValues = new ContentValues();
        warehouseValues.put("name", "默认仓库");
        db.insert(TABLE_WAREHOUSES, null, warehouseValues);

        ContentValues categoryValues = new ContentValues();
        categoryValues.put("name", "默认分类");
        db.insert(TABLE_CATEGORIES, null, categoryValues);

        ContentValues userValues = new ContentValues();
        userValues.put("password", "");
        db.insert(TABLE_USER, null, userValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WAREHOUSES + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LOGS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "operation_time TEXT,"
                    + "product_code TEXT,"
                    + "product_name TEXT,"
                    + "quantity INTEGER,"
                    + "operation_type TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "password TEXT)");

            db.execSQL("ALTER TABLE " + TABLE_GOODS + " ADD COLUMN warehouse_id INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_GOODS + " ADD COLUMN category_id INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_GOODS + " ADD COLUMN min_stock INTEGER DEFAULT 0");

            insertDefaultData(db);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_GOODS + " ADD COLUMN price REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_LOGS + " ADD COLUMN price REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_LOGS + " ADD COLUMN total REAL DEFAULT 0");
        }
        if (oldVersion < 4) {
            // 先检查表是否存在
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_DAILY_SUMMARY});
            boolean tableExists = cursor.moveToFirst();
            cursor.close();
            
            if (tableExists) {
                // 检查表是否有category字段
                cursor = db.rawQuery("PRAGMA table_info(" + TABLE_DAILY_SUMMARY + ")", null);
                boolean hasCategory = false;
                boolean hasSpec = false;
                boolean hasCustomer = false;
                
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(1);
                    if ("category".equals(columnName)) {
                        hasCategory = true;
                    } else if ("spec".equals(columnName)) {
                        hasSpec = true;
                    } else if ("customer".equals(columnName)) {
                        hasCustomer = true;
                    }
                }
                cursor.close();
                
                // 添加缺少的字段
                if (!hasCategory) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN category TEXT");
                }
                if (!hasSpec) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN spec TEXT");
                }
                if (!hasCustomer) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN customer TEXT");
                }
            } else {
                // 创建新表
                db.execSQL("CREATE TABLE " + TABLE_DAILY_SUMMARY + " ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "product_name TEXT,"
                        + "product_code TEXT,"
                        + "in_quantity INTEGER DEFAULT 0,"
                        + "out_quantity INTEGER DEFAULT 0,"
                        + "price REAL,"
                        + "total_amount REAL,"
                        + "operation_type TEXT,"
                        + "category TEXT,"
                        + "spec TEXT,"
                        + "customer TEXT,"
                        + "summary_date TEXT)");
            }
        }
    }

    public boolean addGoods(String name, String code, int count, double price, String remark) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("code", code);
        values.put("count", count);
        values.put("price", price);
        values.put("remark", remark);

        long result = db.insert(TABLE_GOODS, null, values);
        if (result != -1) {
            addOperationLog(code, name, count, price, "入库");
        }
        return result != -1;
    }

    public int getGoodsCount(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT count FROM " + TABLE_GOODS + " WHERE code=?",
                new String[]{code});
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        cursor.close();
        return -1;
    }

    public double getGoodsPrice(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT price FROM " + TABLE_GOODS + " WHERE code=?",
                new String[]{code});
        if (cursor.moveToFirst()) {
            double price = cursor.getDouble(0);
            cursor.close();
            return price;
        }
        cursor.close();
        return 0;
    }

    public boolean updateCount(String code, int newCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("count", newCount);
        int result = db.update(TABLE_GOODS, values, "code=?", new String[]{code});
        return result > 0;
    }

    public boolean doOut(String code, int outCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        int currentCount = getGoodsCount(code);
        if (currentCount == -1 || outCount > currentCount) {
            return false;
        }

        int newCount = currentCount - outCount;
        ContentValues values = new ContentValues();
        values.put("count", newCount);
        int result = db.update(TABLE_GOODS, values, "code=?", new String[]{code});

        if (result > 0) {
            String name = getGoodsName(code);
            double price = getGoodsPrice(code);
            addOperationLog(code, name, outCount, price, "出库");
        }
        return result > 0;
    }

    public String getGoodsName(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + TABLE_GOODS + " WHERE code=?",
                new String[]{code});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        cursor.close();
        return "";
    }

    public List<Map<String, Object>> getAllGoods() {
        List<Map<String, Object>> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name, code, count, price, remark, min_stock FROM " + TABLE_GOODS, null);

        while (cursor.moveToNext()) {
            Map<String, Object> goods = new HashMap<>();
            goods.put("id", cursor.getInt(0));
            goods.put("name", cursor.getString(1));
            goods.put("code", cursor.getString(2));
            goods.put("count", cursor.getInt(3));
            goods.put("price", cursor.getDouble(4));
            goods.put("remark", cursor.getString(5));
            goods.put("min_stock", cursor.getInt(6));
            goodsList.add(goods);
        }
        cursor.close();
        return goodsList;
    }

    public List<Map<String, Object>> searchGoods(String keyword) {
        List<Map<String, Object>> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name, code, count, price, remark, min_stock FROM " + TABLE_GOODS +
                " WHERE name LIKE ? OR code LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"});

        while (cursor.moveToNext()) {
            Map<String, Object> goods = new HashMap<>();
            goods.put("id", cursor.getInt(0));
            goods.put("name", cursor.getString(1));
            goods.put("code", cursor.getString(2));
            goods.put("count", cursor.getInt(3));
            goods.put("price", cursor.getDouble(4));
            goods.put("remark", cursor.getString(5));
            goods.put("min_stock", cursor.getInt(6));
            goodsList.add(goods);
        }
        cursor.close();
        return goodsList;
    }

    public Map<String, Object> getGoodsById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name, code, count, price, remark, min_stock FROM " + TABLE_GOODS + " WHERE id=?",
                new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            Map<String, Object> goods = new HashMap<>();
            goods.put("id", cursor.getInt(0));
            goods.put("name", cursor.getString(1));
            goods.put("code", cursor.getString(2));
            goods.put("count", cursor.getInt(3));
            goods.put("price", cursor.getDouble(4));
            goods.put("remark", cursor.getString(5));
            goods.put("min_stock", cursor.getInt(6));
            cursor.close();
            return goods;
        }
        cursor.close();
        return null;
    }

    public boolean updateGoods(int id, String name, String code, int count, double price, String remark, int minStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("code", code);
        values.put("count", count);
        values.put("price", price);
        values.put("remark", remark);
        values.put("min_stock", minStock);

        int result = db.update(TABLE_GOODS, values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteGoods(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_GOODS, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public static final String DATE_FORMAT = "yyyy年MM月dd日 HH:mm";

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(date);
    }

    public static String formatDateTime(String dateStr) {
        if (dateStr == null) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(dateStr);
            return formatDateTime(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }

    public void addOperationLog(String code, String name, int quantity, double price, String operationType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("operation_time", getCurrentDateTime());
        values.put("product_code", code);
        values.put("product_name", name);
        values.put("quantity", quantity);
        values.put("price", price);
        values.put("total", price * quantity);
        values.put("operation_type", operationType);
        db.insert(TABLE_LOGS, null, values);
    }

    public List<Map<String, Object>> getOperationLogs() {
        List<Map<String, Object>> logsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, operation_time, product_code, product_name, quantity, price, total, operation_type FROM " + TABLE_LOGS +
                " ORDER BY operation_time DESC", null);

        while (cursor.moveToNext()) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", cursor.getInt(0));
            log.put("operation_time", cursor.getString(1));
            log.put("product_code", cursor.getString(2));
            log.put("product_name", cursor.getString(3));
            log.put("quantity", cursor.getInt(4));
            log.put("price", cursor.getDouble(5));
            log.put("total", cursor.getDouble(6));
            log.put("operation_type", cursor.getString(7));
            logsList.add(log);
        }
        cursor.close();
        return logsList;
    }

    public boolean checkPassword(String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT password FROM " + TABLE_USER + " WHERE id=1", null);

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();
            return storedPassword.equals(password);
        }
        cursor.close();
        return false;
    }

    public boolean setPassword(String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", password);
        int result = db.update(TABLE_USER, values, "id=1", null);
        return result > 0;
    }

    public List<Map<String, Object>> getLowStockGoods() {
        List<Map<String, Object>> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, name, code, count, price, remark, min_stock FROM " + TABLE_GOODS +
                " WHERE count <= min_stock AND min_stock > 0", null);

        while (cursor.moveToNext()) {
            Map<String, Object> goods = new HashMap<>();
            goods.put("id", cursor.getInt(0));
            goods.put("name", cursor.getString(1));
            goods.put("code", cursor.getString(2));
            goods.put("count", cursor.getInt(3));
            goods.put("price", cursor.getDouble(4));
            goods.put("remark", cursor.getString(5));
            goods.put("min_stock", cursor.getInt(6));
            goodsList.add(goods);
        }
        cursor.close();
        return goodsList;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor inCursor = db.rawQuery(
                "SELECT COALESCE(SUM(quantity), 0), COALESCE(SUM(total), 0), COUNT(*) FROM " + TABLE_LOGS + " WHERE operation_type = '入库'",
                null);
        if (inCursor.moveToFirst()) {
            stats.put("total_in", inCursor.getInt(0));
            stats.put("total_in_amount", inCursor.getDouble(1));
            stats.put("in_count", inCursor.getInt(2));
            inCursor.close();
        }

        Cursor outCursor = db.rawQuery(
                "SELECT COALESCE(SUM(quantity), 0), COALESCE(SUM(total), 0), COUNT(*) FROM " + TABLE_LOGS + " WHERE operation_type = '出库'",
                null);
        if (outCursor.moveToFirst()) {
            stats.put("total_out", outCursor.getInt(0));
            stats.put("total_out_amount", outCursor.getDouble(1));
            stats.put("out_count", outCursor.getInt(2));
            outCursor.close();
        }

        double totalInAmount = (double) stats.getOrDefault("total_in_amount", 0.0);
        double totalOutAmount = (double) stats.getOrDefault("total_out_amount", 0.0);
        stats.put("revenue", totalOutAmount);
        stats.put("cost", totalInAmount);
        stats.put("profit", totalOutAmount - totalInAmount);

        return stats;
    }

    public List<Map<String, Object>> getStatisticsByDate() {
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 按日期分组统计
        Cursor cursor = db.rawQuery(
                "SELECT DATE(operation_time) as date, " +
                "SUM(CASE WHEN operation_type = '入库' THEN quantity ELSE 0 END) as in_quantity, " +
                "SUM(CASE WHEN operation_type = '入库' THEN total ELSE 0 END) as in_amount, " +
                "SUM(CASE WHEN operation_type = '出库' THEN quantity ELSE 0 END) as out_quantity, " +
                "SUM(CASE WHEN operation_type = '出库' THEN total ELSE 0 END) as out_amount " +
                "FROM " + TABLE_LOGS + " " +
                "GROUP BY DATE(operation_time) " +
                "ORDER BY date DESC",
                null);

        while (cursor.moveToNext()) {
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", cursor.getString(0));
            dayStats.put("in_quantity", cursor.getInt(1));
            dayStats.put("in_amount", cursor.getDouble(2));
            dayStats.put("out_quantity", cursor.getInt(3));
            dayStats.put("out_amount", cursor.getDouble(4));
            dayStats.put("revenue", cursor.getDouble(4));
            dayStats.put("cost", cursor.getDouble(2));
            dayStats.put("profit", cursor.getDouble(4) - cursor.getDouble(2));
            dailyStats.add(dayStats);
        }
        cursor.close();
        return dailyStats;
    }

    public boolean addDailySummary(String productName, String productCode, int inQuantity, int outQuantity, double price, String operationType, String category, String summaryDate) {
        return addDailySummary(productName, productCode, inQuantity, outQuantity, price, operationType, category, "", "", summaryDate);
    }

    public boolean addDailySummary(String productName, String productCode, int inQuantity, int outQuantity, double price, String operationType, String category, String spec, String customer, String summaryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_name", productName);
        values.put("product_code", productCode);
        values.put("in_quantity", inQuantity);
        values.put("out_quantity", outQuantity);
        values.put("price", price);
        values.put("total_amount", price * (inQuantity + outQuantity));
        values.put("operation_type", operationType);
        values.put("category", category);
        values.put("spec", spec);
        values.put("customer", customer);
        values.put("summary_date", summaryDate);

        long result = db.insert(TABLE_DAILY_SUMMARY, null, values);
        return result != -1;
    }

    public boolean updateDailySummary(int id, String productName, String productCode, int inQuantity, int outQuantity, double price, String operationType, String category, String spec, String customer, String summaryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_name", productName);
        values.put("product_code", productCode);
        values.put("in_quantity", inQuantity);
        values.put("out_quantity", outQuantity);
        values.put("price", price);
        values.put("total_amount", price * (inQuantity + outQuantity));
        values.put("operation_type", operationType);
        values.put("category", category);
        values.put("spec", spec);
        values.put("customer", customer);
        values.put("summary_date", summaryDate);

        int result = db.update(TABLE_DAILY_SUMMARY, values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteDailySummary(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DAILY_SUMMARY, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    private void ensureDailySummaryTableStructure() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 先检查表是否存在
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_DAILY_SUMMARY});
            boolean tableExists = cursor.moveToFirst();
            cursor.close();
            
            if (tableExists) {
                // 检查表是否有category字段
                cursor = db.rawQuery("PRAGMA table_info(" + TABLE_DAILY_SUMMARY + ")", null);
                boolean hasCategory = false;
                boolean hasSpec = false;
                boolean hasCustomer = false;
                
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(1);
                    if ("category".equals(columnName)) {
                        hasCategory = true;
                    } else if ("spec".equals(columnName)) {
                        hasSpec = true;
                    } else if ("customer".equals(columnName)) {
                        hasCustomer = true;
                    }
                }
                cursor.close();
                
                // 添加缺少的字段
                if (!hasCategory) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN category TEXT");
                }
                if (!hasSpec) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN spec TEXT");
                }
                if (!hasCustomer) {
                    db.execSQL("ALTER TABLE " + TABLE_DAILY_SUMMARY + " ADD COLUMN customer TEXT");
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }

    public List<Map<String, Object>> getDailySummariesByDate(String date) {
        ensureDailySummaryTableStructure();
        List<Map<String, Object>> summaries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, category, spec, customer, summary_date FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date = ? ORDER BY id DESC",
                    new String[]{date});

            while (cursor.moveToNext()) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", cursor.getInt(0));
                summary.put("product_name", cursor.getString(1));
                summary.put("product_code", cursor.getString(2));
                summary.put("in_quantity", cursor.getInt(3));
                summary.put("out_quantity", cursor.getInt(4));
                summary.put("price", cursor.getDouble(5));
                summary.put("total_amount", cursor.getDouble(6));
                summary.put("operation_type", cursor.getString(7));
                try {
                    summary.put("category", cursor.getString(8));
                } catch (Exception e) {
                    summary.put("category", "");
                }
                try {
                    summary.put("spec", cursor.getString(9));
                } catch (Exception e) {
                    summary.put("spec", "");
                }
                try {
                    summary.put("customer", cursor.getString(10));
                } catch (Exception e) {
                    summary.put("customer", "");
                }
                summary.put("summary_date", cursor.getString(11));
                summaries.add(summary);
            }
        } catch (Exception e) {
            // 如果查询失败，尝试使用旧表结构
            try {
                cursor = db.rawQuery(
                        "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, summary_date FROM " + TABLE_DAILY_SUMMARY +
                        " WHERE summary_date = ? ORDER BY id DESC",
                        new String[]{date});

                while (cursor.moveToNext()) {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", cursor.getInt(0));
                    summary.put("product_name", cursor.getString(1));
                    summary.put("product_code", cursor.getString(2));
                    summary.put("in_quantity", cursor.getInt(3));
                    summary.put("out_quantity", cursor.getInt(4));
                    summary.put("price", cursor.getDouble(5));
                    summary.put("total_amount", cursor.getDouble(6));
                    summary.put("operation_type", cursor.getString(7));
                    summary.put("category", "");
                    summary.put("spec", "");
                    summary.put("customer", "");
                    summary.put("summary_date", cursor.getString(8));
                    summaries.add(summary);
                }
            } catch (Exception e2) {
                // 忽略错误，返回空列表
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return summaries;
    }

    public List<Map<String, Object>> getAllDailySummaries() {
        ensureDailySummaryTableStructure();
        List<Map<String, Object>> summaries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, category, spec, customer, summary_date FROM " + TABLE_DAILY_SUMMARY +
                    " ORDER BY summary_date DESC, id DESC", null);

            while (cursor.moveToNext()) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", cursor.getInt(0));
                summary.put("product_name", cursor.getString(1));
                summary.put("product_code", cursor.getString(2));
                summary.put("in_quantity", cursor.getInt(3));
                summary.put("out_quantity", cursor.getInt(4));
                summary.put("price", cursor.getDouble(5));
                summary.put("total_amount", cursor.getDouble(6));
                summary.put("operation_type", cursor.getString(7));
                try {
                    summary.put("category", cursor.getString(8));
                } catch (Exception e) {
                    summary.put("category", "");
                }
                try {
                    summary.put("spec", cursor.getString(9));
                } catch (Exception e) {
                    summary.put("spec", "");
                }
                try {
                    summary.put("customer", cursor.getString(10));
                } catch (Exception e) {
                    summary.put("customer", "");
                }
                summary.put("summary_date", cursor.getString(11));
                summaries.add(summary);
            }
        } catch (Exception e) {
            // 如果查询失败，尝试使用旧表结构
            try {
                cursor = db.rawQuery(
                        "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, summary_date FROM " + TABLE_DAILY_SUMMARY +
                        " ORDER BY summary_date DESC, id DESC", null);

                while (cursor.moveToNext()) {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", cursor.getInt(0));
                    summary.put("product_name", cursor.getString(1));
                    summary.put("product_code", cursor.getString(2));
                    summary.put("in_quantity", cursor.getInt(3));
                    summary.put("out_quantity", cursor.getInt(4));
                    summary.put("price", cursor.getDouble(5));
                    summary.put("total_amount", cursor.getDouble(6));
                    summary.put("operation_type", cursor.getString(7));
                    summary.put("category", "");
                    summary.put("spec", "");
                    summary.put("customer", "");
                    summary.put("summary_date", cursor.getString(8));
                    summaries.add(summary);
                }
            } catch (Exception e2) {
                // 忽略错误，返回空列表
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return summaries;
    }

    public List<Map<String, Object>> getDailySummariesByDateRange(String startDate, String endDate) {
        ensureDailySummaryTableStructure();
        List<Map<String, Object>> summaries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, category, spec, customer, summary_date FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date >= ? AND summary_date <= ? ORDER BY summary_date DESC, id DESC",
                    new String[]{startDate, endDate});

            while (cursor.moveToNext()) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", cursor.getInt(0));
                summary.put("product_name", cursor.getString(1));
                summary.put("product_code", cursor.getString(2));
                summary.put("in_quantity", cursor.getInt(3));
                summary.put("out_quantity", cursor.getInt(4));
                summary.put("price", cursor.getDouble(5));
                summary.put("total_amount", cursor.getDouble(6));
                summary.put("operation_type", cursor.getString(7));
                try {
                    summary.put("category", cursor.getString(8));
                } catch (Exception e) {
                    summary.put("category", "");
                }
                try {
                    summary.put("spec", cursor.getString(9));
                } catch (Exception e) {
                    summary.put("spec", "");
                }
                try {
                    summary.put("customer", cursor.getString(10));
                } catch (Exception e) {
                    summary.put("customer", "");
                }
                summary.put("summary_date", cursor.getString(11));
                summaries.add(summary);
            }
        } catch (Exception e) {
            // 如果查询失败，尝试使用旧表结构
            try {
                cursor = db.rawQuery(
                        "SELECT id, product_name, product_code, in_quantity, out_quantity, price, total_amount, operation_type, summary_date FROM " + TABLE_DAILY_SUMMARY +
                        " WHERE summary_date >= ? AND summary_date <= ? ORDER BY summary_date DESC, id DESC",
                        new String[]{startDate, endDate});

                while (cursor.moveToNext()) {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", cursor.getInt(0));
                    summary.put("product_name", cursor.getString(1));
                    summary.put("product_code", cursor.getString(2));
                    summary.put("in_quantity", cursor.getInt(3));
                    summary.put("out_quantity", cursor.getInt(4));
                    summary.put("price", cursor.getDouble(5));
                    summary.put("total_amount", cursor.getDouble(6));
                    summary.put("operation_type", cursor.getString(7));
                    summary.put("category", "");
                    summary.put("spec", "");
                    summary.put("customer", "");
                    summary.put("summary_date", cursor.getString(8));
                    summaries.add(summary);
                }
            } catch (Exception e2) {
                // 忽略错误，返回空列表
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return summaries;
    }

    public Map<String, Object> getSalesStatisticsByDateRange(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor todayCursor = null;
        try {
            todayCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date >= ? AND summary_date <= ? AND operation_type = '出库'",
                    new String[]{startDate, endDate});
            if (todayCursor.moveToFirst()) {
                stats.put("revenue", todayCursor.getDouble(0));
            }
        } finally {
            if (todayCursor != null) {
                todayCursor.close();
            }
        }

        Cursor costCursor = null;
        try {
            costCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date >= ? AND summary_date <= ? AND operation_type = '入库'",
                    new String[]{startDate, endDate});
            if (costCursor.moveToFirst()) {
                stats.put("cost", costCursor.getDouble(0));
            }
        } finally {
            if (costCursor != null) {
                costCursor.close();
            }
        }

        double revenue = (double) stats.getOrDefault("revenue", 0.0);
        double cost = (double) stats.getOrDefault("cost", 0.0);
        stats.put("profit", revenue - cost);

        return stats;
    }

    public Map<String, Object> getSalesStatistics() {
        ensureDailySummaryTableStructure();
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String currentMonth = today.substring(0, 7);

        Cursor todayCursor = null;
        try {
            todayCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0), COALESCE(SUM(in_quantity), 0), COALESCE(SUM(out_quantity), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date = ? AND operation_type = '出库'",
                    new String[]{today});
            if (todayCursor.moveToFirst()) {
                stats.put("today_revenue", todayCursor.getDouble(0));
            }
        } catch (Exception e) {
            stats.put("today_revenue", 0.0);
        } finally {
            if (todayCursor != null) {
                todayCursor.close();
            }
        }

        Cursor monthCursor = null;
        try {
            monthCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE summary_date LIKE ? AND operation_type = '出库'",
                    new String[]{currentMonth + "%"});
            if (monthCursor.moveToFirst()) {
                stats.put("month_revenue", monthCursor.getDouble(0));
            }
        } catch (Exception e) {
            stats.put("month_revenue", 0.0);
        } finally {
            if (monthCursor != null) {
                monthCursor.close();
            }
        }

        Cursor totalCursor = null;
        try {
            totalCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE operation_type = '出库'",
                    null);
            if (totalCursor.moveToFirst()) {
                stats.put("total_revenue", totalCursor.getDouble(0));
            }
        } catch (Exception e) {
            stats.put("total_revenue", 0.0);
        } finally {
            if (totalCursor != null) {
                totalCursor.close();
            }
        }

        Cursor costCursor = null;
        try {
            costCursor = db.rawQuery(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM " + TABLE_DAILY_SUMMARY +
                    " WHERE operation_type = '入库'",
                    null);
            if (costCursor.moveToFirst()) {
                stats.put("total_cost", costCursor.getDouble(0));
            }
        } catch (Exception e) {
            stats.put("total_cost", 0.0);
        } finally {
            if (costCursor != null) {
                costCursor.close();
            }
        }

        double revenue = (double) stats.getOrDefault("total_revenue", 0.0);
        double cost = (double) stats.getOrDefault("total_cost", 0.0);
        stats.put("total_profit", revenue - cost);

        return stats;
    }

    public List<Map<String, Object>> getSalesStatisticsByMonth() {
        ensureDailySummaryTableStructure();
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT substr(summary_date, 1, 7) as month, " +
                    "SUM(CASE WHEN operation_type = '出库' THEN total_amount ELSE 0 END) as revenue, " +
                    "SUM(CASE WHEN operation_type = '入库' THEN total_amount ELSE 0 END) as cost " +
                    "FROM " + TABLE_DAILY_SUMMARY + " " +
                    "GROUP BY substr(summary_date, 1, 7) " +
                    "ORDER BY month DESC",
                    null);

            while (cursor.moveToNext()) {
                Map<String, Object> monthStats = new HashMap<>();
                monthStats.put("month", cursor.getString(0));
                monthStats.put("revenue", cursor.getDouble(1));
                monthStats.put("cost", cursor.getDouble(2));
                monthStats.put("profit", cursor.getDouble(1) - cursor.getDouble(2));
                monthlyStats.add(monthStats);
            }
        } catch (Exception e) {
            // 忽略错误，返回空列表
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return monthlyStats;
    }

    public boolean exportExcel(Context context) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("库存数据");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("商品名称");
            headerRow.createCell(1).setCellValue("商品编码");
            headerRow.createCell(2).setCellValue("库存数量");
            headerRow.createCell(3).setCellValue("单价");
            headerRow.createCell(4).setCellValue("备注");

            // 填充数据
            List<Map<String, Object>> goodsList = getAllGoods();
            for (int i = 0; i < goodsList.size(); i++) {
                Map<String, Object> goods = goodsList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue((String) goods.get("name"));
                row.createCell(1).setCellValue((String) goods.get("code"));
                row.createCell(2).setCellValue((int) goods.get("count"));
                row.createCell(3).setCellValue((double) goods.get("price"));
                row.createCell(4).setCellValue((String) goods.get("remark"));
            }

            // 保存文件
            String fileName = "库存数据_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            File file = new File(context.getExternalFilesDir(null), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "库存数据导出");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "分享文件"));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
