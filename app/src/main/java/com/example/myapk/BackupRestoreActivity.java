package com.example.myapk;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class BackupRestoreActivity extends AppCompatActivity {
    Button btnBackup, btnRestore;
    DBHelper dbHelper;
    ActivityResultLauncher<Intent> backupLauncher, restoreLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("数据备份");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnBackup = findViewById(R.id.btn_backup);
        btnRestore = findViewById(R.id.btn_restore);
        Button btnExportExcel = findViewById(R.id.btn_export_excel);
        dbHelper = new DBHelper(this);

        backupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            backupDatabase(uri);
                        }
                    }
                });

        restoreLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            restoreDatabase(uri);
                        }
                    }
                });

        btnBackup.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_TITLE, "WarehouseDB_backup.db");
            backupLauncher.launch(intent);
        });

        btnRestore.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            restoreLauncher.launch(intent);
        });

        btnExportExcel.setOnClickListener(v -> {
            boolean success = dbHelper.exportExcel(this);
            if (success) {
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void backupDatabase(Uri uri) {
        try {
            File dbFile = getDatabasePath("WarehouseDB");
            FileInputStream fis = new FileInputStream(dbFile);
            FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fis.close();
            fos.close();
            Toast.makeText(this, "备份成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "备份失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreDatabase(Uri uri) {
        try {
            File dbFile = getDatabasePath("WarehouseDB");
            FileOutputStream fos = new FileOutputStream(dbFile);
            FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fis.close();
            fos.close();
            Toast.makeText(this, "恢复成功，请重启APP", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "恢复失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}