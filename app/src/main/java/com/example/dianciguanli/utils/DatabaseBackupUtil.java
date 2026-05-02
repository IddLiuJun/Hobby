package com.example.dianciguanli.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.DBHelper;
import com.example.dianciguanli.data.Record;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DatabaseBackupUtil {
    private static final String BACKUP_FOLDER = "BatteryBackup";
    private static final String BATTERIES_FILE = "batteries.json";
    private static final String RECORDS_FILE = "records.json";

    public interface BackupCallback {
        void onSuccess(String filePath);
        void onError(String message);
    }

    public interface RestoreCallback {
        void onSuccess(int batteryCount, int recordCount);
        void onError(String message);
    }

    public static void exportDatabase(Context context, BackupCallback callback) {
        try {
            DBHelper dbHelper = new DBHelper(context);
            List<Battery> batteries = dbHelper.getAllBatteries();
            List<Record> records = dbHelper.getAllRecords();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFileName = "battery_backup_" + timestamp + ".zip";

            File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File backupFile = new File(backupDir, backupFileName);

            File tempBatteriesFile = new File(context.getCacheDir(), BATTERIES_FILE);
            File tempRecordsFile = new File(context.getCacheDir(), RECORDS_FILE);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            FileOutputStream fos = new FileOutputStream(tempBatteriesFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            gson.toJson(batteries, writer);
            writer.close();

            fos = new FileOutputStream(tempRecordsFile);
            writer = new OutputStreamWriter(fos, "UTF-8");
            gson.toJson(records, writer);
            writer.close();

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupFile));
            ZipEntry batteryEntry = new ZipEntry(BATTERIES_FILE);
            zipOut.putNextEntry(batteryEntry);

            FileInputStream fis = new FileInputStream(tempBatteriesFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
            fis.close();
            zipOut.closeEntry();

            ZipEntry recordEntry = new ZipEntry(RECORDS_FILE);
            zipOut.putNextEntry(recordEntry);
            fis = new FileInputStream(tempRecordsFile);
            while ((length = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
            fis.close();
            zipOut.closeEntry();
            zipOut.close();

            tempBatteriesFile.delete();
            tempRecordsFile.delete();

            callback.onSuccess(backupFile.getAbsolutePath());
        } catch (Exception e) {
            callback.onError("导出失败: " + e.getMessage());
        }
    }

    public static void importDatabase(Context context, Uri zipUri, RestoreCallback callback) {
        try {
            DBHelper dbHelper = new DBHelper(context);
            int batteryCount = 0;
            int recordCount = 0;

            ZipInputStream zipIn = new ZipInputStream(context.getContentResolver().openInputStream(zipUri));
            ZipEntry entry;

            StringBuilder batteriesJson = new StringBuilder();
            StringBuilder recordsJson = new StringBuilder();

            while ((entry = zipIn.getNextEntry()) != null) {
                if (BATTERIES_FILE.equals(entry.getName())) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        batteriesJson.append(line);
                    }
                    reader.close();
                } else if (RECORDS_FILE.equals(entry.getName())) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        recordsJson.append(line);
                    }
                    reader.close();
                }
                zipIn.closeEntry();
            }
            zipIn.close();

            Gson gson = new Gson();

            if (batteriesJson.length() > 0) {
                Type batteryListType = new TypeToken<List<Battery>>(){}.getType();
                List<Battery> batteries = gson.fromJson(batteriesJson.toString(), batteryListType);
                if (batteries != null) {
                    for (Battery battery : batteries) {
                        Battery existing = dbHelper.getBatteryById(battery.getId());
                        if (existing == null) {
                            dbHelper.insertBattery(battery);
                            batteryCount++;
                        } else {
                            dbHelper.updateBattery(battery);
                        }
                    }
                }
            }

            if (recordsJson.length() > 0) {
                Type recordListType = new TypeToken<List<Record>>(){}.getType();
                List<Record> records = gson.fromJson(recordsJson.toString(), recordListType);
                if (records != null) {
                    for (Record record : records) {
                        dbHelper.insertRecord(record);
                        recordCount++;
                    }
                }
            }

            callback.onSuccess(batteryCount, recordCount);
        } catch (Exception e) {
            callback.onError("导入失败: " + e.getMessage());
        }
    }

    public static File getBackupDirectory(Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);
    }
}