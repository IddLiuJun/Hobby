package com.example.myapk;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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

public class DailySummaryActivity extends AppCompatActivity {
    private DatePicker datePicker;
    private EditText edtProductName, edtProductCode, edtInQuantity, edtOutQuantity, edtPrice, edtSpec, edtCustomer;
    private Spinner spinnerOperationType, spinnerCategory;
    private ListView listViewSummaries;
    private Button btnAdd, btnSelectRange, btnShowAll;
    private TextView tvDateRange;
    private DBHelper dbHelper;
    private List<Map<String, Object>> summariesList = new ArrayList<>();
    private String currentDate;
    private String startDate = null;
    private String endDate = null;
    private boolean isRangeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("每日汇总录入");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        datePicker = findViewById(R.id.date_picker);
        edtProductName = findViewById(R.id.edt_product_name);
        edtProductCode = findViewById(R.id.edt_product_code);
        edtInQuantity = findViewById(R.id.edt_in_quantity);
        edtOutQuantity = findViewById(R.id.edt_out_quantity);
        edtPrice = findViewById(R.id.edt_price);
        edtSpec = findViewById(R.id.edt_spec);
        edtCustomer = findViewById(R.id.edt_customer);
        spinnerOperationType = findViewById(R.id.spinner_operation_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        listViewSummaries = findViewById(R.id.list_view_summaries);
        btnAdd = findViewById(R.id.btn_add);
        btnSelectRange = findViewById(R.id.btn_select_range);
        btnShowAll = findViewById(R.id.btn_show_all);
        tvDateRange = findViewById(R.id.tv_date_range);
        dbHelper = new DBHelper(this);

        currentDate = getCurrentDate();
        datePicker.init(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, month, dayOfMonth) -> {
                    currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (!isRangeMode) {
                        loadSummariesForDate(currentDate);
                    }
                });

        String[] operationTypes = {"入库", "出库"};
        ArrayAdapter<String> opAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, operationTypes);
        spinnerOperationType.setAdapter(opAdapter);

        String[] categories = {"进货", "销售", "退货", "报损", "其他"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        loadSummariesForDate(currentDate);

        btnAdd.setOnClickListener(v -> addSummary());

        btnSelectRange.setOnClickListener(v -> showDateRangePicker());

        btnShowAll.setOnClickListener(v -> {
            isRangeMode = false;
            startDate = null;
            endDate = null;
            tvDateRange.setText("当前显示：全部记录");
            loadAllSummaries();
        });

        listViewSummaries.setOnItemLongClickListener((parent, view, position, id) -> {
            Map<String, Object> item = summariesList.get(position);
            showDeleteDialog((int) item.get("id"));
            return true;
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private void loadSummariesForDate(String date) {
        summariesList = dbHelper.getDailySummariesByDate(date);
        updateListView();
    }

    private void loadAllSummaries() {
        summariesList = dbHelper.getAllDailySummaries();
        updateListView();
    }

    private void loadSummariesByRange(String start, String end) {
        summariesList = dbHelper.getDailySummariesByDateRange(start, end);
        updateListView();
    }

    private void updateListView() {
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (Map<String, Object> summary : summariesList) {
            Map<String, Object> item = new HashMap<>();
            String details = summary.get("product_name") + " [" + summary.get("product_code") + "]\n" +
                    "入库:" + summary.get("in_quantity") + " 出库:" + summary.get("out_quantity") + "\n" +
                    "单价:¥" + summary.get("price") + " 金额:¥" + summary.get("total_amount") + "\n" +
                    "类型:" + summary.get("operation_type") + " 分类:" + summary.get("category");
            item.put("info", details);
            listItems.add(item);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(
                this,
                listItems,
                android.R.layout.simple_list_item_1,
                new String[]{"info"},
                new int[]{android.R.id.text1}
        );
        listViewSummaries.setAdapter(simpleAdapter);
    }

    private void addSummary() {
        String productName = edtProductName.getText().toString().trim();
        String productCode = edtProductCode.getText().toString().trim();
        String inQuantityStr = edtInQuantity.getText().toString().trim();
        String outQuantityStr = edtOutQuantity.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String operationType = spinnerOperationType.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (productName.isEmpty() || productCode.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        int inQuantity = inQuantityStr.isEmpty() ? 0 : Integer.parseInt(inQuantityStr);
        int outQuantity = outQuantityStr.isEmpty() ? 0 : Integer.parseInt(outQuantityStr);
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的价格", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inQuantity == 0 && outQuantity == 0) {
            Toast.makeText(this, "入库或出库数量至少填写一个", Toast.LENGTH_SHORT).show();
            return;
        }

        if (price <= 0) {
            Toast.makeText(this, "价格必须大于0", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.addDailySummary(productName, productCode, inQuantity, outQuantity, price, operationType, category, currentDate);
        if (success) {
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            clearInputs();
            if (isRangeMode) {
                loadSummariesByRange(startDate, endDate);
            } else {
                loadSummariesForDate(currentDate);
            }
        } else {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputs() {
        edtProductName.setText("");
        edtProductCode.setText("");
        edtInQuantity.setText("");
        edtOutQuantity.setText("");
        edtPrice.setText("");
        edtSpec.setText("");
        edtCustomer.setText("");
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
                tvDateRange.setText("当前显示：" + startDate + " 至 " + endDate);
                loadSummariesByRange(startDate, endDate);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteDialog(int id) {
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除这条汇总记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (dbHelper.deleteDailySummary(id)) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                        if (isRangeMode) {
                            loadSummariesByRange(startDate, endDate);
                        } else {
                            loadSummariesForDate(currentDate);
                        }
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}