package com.example.myapk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesStatisticsActivity extends AppCompatActivity {
    private TextView tvTodayRevenue, tvMonthRevenue, tvTotalRevenue, tvTotalCost, tvTotalProfit, tvDateRange;
    private ListView listViewMonthly;
    private Button btnSelectRange, btnReset;
    private DBHelper dbHelper;
    private String startDate = null;
    private String endDate = null;
    private boolean isRangeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("营业额统计");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTodayRevenue = findViewById(R.id.tv_today_revenue);
        tvMonthRevenue = findViewById(R.id.tv_month_revenue);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvTotalProfit = findViewById(R.id.tv_total_profit);
        tvDateRange = findViewById(R.id.tv_date_range);
        listViewMonthly = findViewById(R.id.list_view_monthly);
        btnSelectRange = findViewById(R.id.btn_select_range);
        btnReset = findViewById(R.id.btn_reset);
        dbHelper = new DBHelper(this);

        loadStatistics();

        btnSelectRange.setOnClickListener(v -> showDateRangePicker());

        btnReset.setOnClickListener(v -> {
            isRangeMode = false;
            startDate = null;
            endDate = null;
            tvDateRange.setText("全部时间汇总");
            loadStatistics();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        if (isRangeMode && startDate != null && endDate != null) {
            loadStatisticsByRange();
        } else {
            loadAllStatistics();
        }
    }

    private void loadAllStatistics() {
        Map<String, Object> stats = dbHelper.getSalesStatistics();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String currentMonth = today.substring(0, 7);

        double todayRevenue = 0;
        double monthRevenue = 0;

        List<Map<String, Object>> allSummaries = dbHelper.getAllDailySummaries();
        for (Map<String, Object> summary : allSummaries) {
            String date = (String) summary.get("summary_date");
            String type = (String) summary.get("operation_type");
            double amount = (double) summary.get("total_amount");

            if (type.equals("出库")) {
                if (date.equals(today)) {
                    todayRevenue += amount;
                }
                if (date.startsWith(currentMonth)) {
                    monthRevenue += amount;
                }
            }
        }

        tvTodayRevenue.setText(String.format("¥%.2f", todayRevenue));
        tvMonthRevenue.setText(String.format("¥%.2f", monthRevenue));
        tvTotalRevenue.setText(String.format("¥%.2f", (double) stats.getOrDefault("total_revenue", 0.0)));
        tvTotalCost.setText(String.format("¥%.2f", (double) stats.getOrDefault("total_cost", 0.0)));
        tvTotalProfit.setText(String.format("¥%.2f", (double) stats.getOrDefault("total_profit", 0.0)));

        loadMonthlyStatistics();
    }

    private void loadStatisticsByRange() {
        Map<String, Object> rangeStats = dbHelper.getSalesStatisticsByDateRange(startDate, endDate);
        double revenue = (double) rangeStats.getOrDefault("revenue", 0.0);
        double cost = (double) rangeStats.getOrDefault("cost", 0.0);

        tvDateRange.setText(startDate + " 至 " + endDate);
        tvTodayRevenue.setText("¥0.00");
        tvMonthRevenue.setText("¥0.00");
        tvTotalRevenue.setText(String.format("¥%.2f", revenue));
        tvTotalCost.setText(String.format("¥%.2f", cost));
        tvTotalProfit.setText(String.format("¥%.2f", revenue - cost));

        loadMonthlyStatisticsByRange();
    }

    private void loadMonthlyStatistics() {
        List<Map<String, Object>> monthlyStats = dbHelper.getSalesStatisticsByMonth();
        List<Map<String, Object>> listItems = new ArrayList<>();

        for (Map<String, Object> monthStat : monthlyStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStat.get("month") + "月");
            double revenue = (double) monthStat.get("revenue");
            double cost = (double) monthStat.get("cost");
            double profit = revenue - cost;
            item.put("details", "营业额: ¥" + String.format("%.2f", revenue) +
                    " | 成本: ¥" + String.format("%.2f", cost) +
                    " | 毛利: ¥" + String.format("%.2f", profit));
            listItems.add(item);
        }

        if (listItems.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("month", "暂无数据");
            emptyItem.put("details", "请先在每日汇总中录入数据");
            listItems.add(emptyItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listItems,
                android.R.layout.two_line_list_item,
                new String[]{"month", "details"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        listViewMonthly.setAdapter(adapter);
    }

    private void loadMonthlyStatisticsByRange() {
        List<Map<String, Object>> allSummaries;
        if (isRangeMode && startDate != null && endDate != null) {
            allSummaries = dbHelper.getDailySummariesByDateRange(startDate, endDate);
        } else {
            allSummaries = dbHelper.getAllDailySummaries();
        }

        Map<String, Map<String, Double>> monthlyData = new HashMap<>();

        for (Map<String, Object> summary : allSummaries) {
            String date = (String) summary.get("summary_date");
            String month = date.substring(0, 7);
            String type = (String) summary.get("operation_type");
            double amount = (double) summary.get("total_amount");

            if (!monthlyData.containsKey(month)) {
                monthlyData.put(month, new HashMap<>());
                monthlyData.get(month).put("revenue", 0.0);
                monthlyData.get(month).put("cost", 0.0);
            }

            if (type.equals("出库")) {
                monthlyData.get(month).put("revenue", monthlyData.get(month).get("revenue") + amount);
            } else if (type.equals("入库")) {
                monthlyData.get(month).put("cost", monthlyData.get(month).get("cost") + amount);
            }
        }

        List<Map<String, Object>> listItems = new ArrayList<>();
        List<String> sortedMonths = new ArrayList<>(monthlyData.keySet());
        java.util.Collections.sort(sortedMonths, java.util.Collections.reverseOrder());

        for (String month : sortedMonths) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month + "月");
            double revenue = monthlyData.get(month).get("revenue");
            double cost = monthlyData.get(month).get("cost");
            double profit = revenue - cost;
            item.put("details", "营业额: ¥" + String.format("%.2f", revenue) +
                    " | 成本: ¥" + String.format("%.2f", cost) +
                    " | 毛利: ¥" + String.format("%.2f", profit));
            listItems.add(item);
        }

        if (listItems.isEmpty()) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("month", "暂无数据");
            emptyItem.put("details", "该时间段内没有数据");
            listItems.add(emptyItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listItems,
                android.R.layout.two_line_list_item,
                new String[]{"month", "details"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        listViewMonthly.setAdapter(adapter);
    }

    private void showDateRangePicker() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 10, 20, 10);

        // 开始日期选择器
        TextView tvStart = new TextView(this);
        tvStart.setText("开始日期");
        tvStart.setTextSize(16);
        tvStart.setTextColor(getResources().getColor(R.color.text_primary));
        layout.addView(tvStart);

        DatePicker datePickerStart = new DatePicker(this);
        datePickerStart.setCalendarViewShown(true);
        datePickerStart.setSpinnersShown(false);
        layout.addView(datePickerStart);

        // 结束日期选择器
        TextView tvEnd = new TextView(this);
        tvEnd.setText("结束日期");
        tvEnd.setTextSize(16);
        tvEnd.setTextColor(getResources().getColor(R.color.text_primary));
        tvEnd.setPadding(0, 16, 0, 0);
        layout.addView(tvEnd);

        DatePicker datePickerEnd = new DatePicker(this);
        datePickerEnd.setCalendarViewShown(true);
        datePickerEnd.setSpinnersShown(false);
        layout.addView(datePickerEnd);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择日期范围");
        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            int startYear = datePickerStart.getYear();
            int startMonth = datePickerStart.getMonth();
            int startDay = datePickerStart.getDayOfMonth();
            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", startYear, startMonth + 1, startDay);

            int endYear = datePickerEnd.getYear();
            int endMonth = datePickerEnd.getMonth();
            int endDay = datePickerEnd.getDayOfMonth();
            endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", endYear, endMonth + 1, endDay);

            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                isRangeMode = true;
                loadStatisticsByRange();
            } else {
                Toast.makeText(this, "请选择完整的日期范围", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}