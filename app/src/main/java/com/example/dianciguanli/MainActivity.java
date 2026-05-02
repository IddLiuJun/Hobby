package com.example.dianciguanli;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.dianciguanli.ui.activity.AddBatteryActivity;
import com.example.dianciguanli.ui.fragment.InventoryFragment;
import com.example.dianciguanli.ui.fragment.ScanFragment;
import com.example.dianciguanli.ui.fragment.StatisticsFragment;
import com.example.dianciguanli.utils.DatabaseBackupUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> importFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, new InventoryFragment())
                    .commit();
        }

        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri zipUri = result.getData().getData();
                        if (zipUri != null) {
                            importDatabase(zipUri);
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(this, AddBatteryActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_backup) {
            showBackupDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBackupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_backup, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Button btnExport = dialogView.findViewById(R.id.btnExport);
        Button btnImport = dialogView.findViewById(R.id.btnImport);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnExport.setOnClickListener(v -> {
            dialog.dismiss();
            exportDatabase();
        });

        btnImport.setOnClickListener(v -> {
            dialog.dismiss();
            openFilePicker();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void exportDatabase() {
        DatabaseBackupUtil.exportDatabase(this, new DatabaseBackupUtil.BackupCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "备份成功:\n" + filePath, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        importFileLauncher.launch(intent);
    }

    private void importDatabase(Uri zipUri) {
        DatabaseBackupUtil.importDatabase(this, zipUri, new DatabaseBackupUtil.RestoreCallback() {
            @Override
            public void onSuccess(int batteryCount, int recordCount) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "导入成功: " + batteryCount + " 节电池, " + recordCount + " 条记录", Toast.LENGTH_LONG).show();
                    refreshCurrentFragment();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (currentFragment instanceof InventoryFragment) {
            ((InventoryFragment) currentFragment).refreshData();
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_inventory) {
            fragment = new InventoryFragment();
        } else if (itemId == R.id.navigation_scan) {
            fragment = new ScanFragment();
        } else if (itemId == R.id.navigation_statistics) {
            fragment = new StatisticsFragment();
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}