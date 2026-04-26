package com.example.myapk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CardView cardIn, cardOut, cardInventory, cardWarning, cardStats, cardSummary, cardSales, cardLogs, cardBackup, cardAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cardIn = findViewById(R.id.card_in);
        cardOut = findViewById(R.id.card_out);
        cardInventory = findViewById(R.id.card_inventory);
        cardBackup = findViewById(R.id.card_backup);
        cardLogs = findViewById(R.id.card_logs);
        cardWarning = findViewById(R.id.card_warning);
        cardStats = findViewById(R.id.card_stats);
        cardSummary = findViewById(R.id.card_summary);
        cardSales = findViewById(R.id.card_sales);
        cardAbout = findViewById(R.id.card_about);

        cardIn.setOnClickListener(v -> startActivity(new Intent(this, InActivity.class)));
        cardOut.setOnClickListener(v -> startActivity(new Intent(this, OutActivity.class)));
        cardInventory.setOnClickListener(v -> startActivity(new Intent(this, AllInventoryActivity.class)));
        cardBackup.setOnClickListener(v -> startActivity(new Intent(this, BackupRestoreActivity.class)));
        cardLogs.setOnClickListener(v -> startActivity(new Intent(this, OperationLogsActivity.class)));
        cardWarning.setOnClickListener(v -> startActivity(new Intent(this, LowStockActivity.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        cardSummary.setOnClickListener(v -> startActivity(new Intent(this, DailySummaryActivity.class)));
        cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesStatisticsActivity.class)));
        cardAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));

        // 检查是否首次打开，显示欢迎弹窗
        checkFirstTime();
    }

    private void checkFirstTime() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String lastShownDate = prefs.getString("last_dialog_date", "");

        if (!today.equals(lastShownDate)) {
            showWelcomeDialog();
            prefs.edit().putString("last_dialog_date", today).apply();
        }
    }

    private String getTimeBasedGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 9) {
            return "早上好，新的一天开始了！";
        } else if (hour >= 9 && hour < 12) {
            return "上午好，工作加油！";
        } else if (hour >= 12 && hour < 14) {
            return "中午好，记得吃午饭哦！";
        } else if (hour >= 14 && hour < 18) {
            return "下午好，辛苦了！";
        } else if (hour >= 18 && hour < 22) {
            return "晚上好，忙碌一天了，休息一下吧！";
        } else {
            return "夜深了，早点休息哦！";
        }
    }

    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_welcome, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        TextView tvGreeting = dialogView.findViewById(R.id.tv_greeting);
        tvGreeting.setText(getTimeBasedGreeting());

        Button btnIknow = dialogView.findViewById(R.id.btn_iknow);
        btnIknow.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}