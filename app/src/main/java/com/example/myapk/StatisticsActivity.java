package com.example.myapk;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    TextView tvTotalIn, tvTotalInAmount, tvTotalOut, tvTotalOutAmount, tvRevenue, tvCost, tvProfit, tvInCount, tvOutCount;
    ListView listViewDailyStats;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("统计报表");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTotalIn = findViewById(R.id.tv_total_in);
        tvTotalInAmount = findViewById(R.id.tv_total_in_amount);
        tvTotalOut = findViewById(R.id.tv_total_out);
        tvTotalOutAmount = findViewById(R.id.tv_total_out_amount);
        tvRevenue = findViewById(R.id.tv_revenue);
        tvCost = findViewById(R.id.tv_cost);
        tvProfit = findViewById(R.id.tv_profit);
        tvInCount = findViewById(R.id.tv_in_count);
        tvOutCount = findViewById(R.id.tv_out_count);
        listViewDailyStats = findViewById(R.id.list_view_daily_stats);
        dbHelper = new DBHelper(this);

        loadStatistics();
        loadDailyStatistics();
    }

    private void loadStatistics() {
        Map<String, Object> stats = dbHelper.getStatistics();
        tvTotalIn.setText("总入库数量：" + stats.get("total_in"));
        tvTotalInAmount.setText("总入库金额：" + String.format("%.2f", (double) stats.getOrDefault("total_in_amount", 0.0)));
        tvTotalOut.setText("总出库数量：" + stats.get("total_out"));
        tvTotalOutAmount.setText("总出库金额：" + String.format("%.2f", (double) stats.getOrDefault("total_out_amount", 0.0)));
        tvRevenue.setText("营业额：" + String.format("%.2f", (double) stats.getOrDefault("revenue", 0.0)));
        tvCost.setText("成本：" + String.format("%.2f", (double) stats.getOrDefault("cost", 0.0)));
        tvProfit.setText("利润：" + String.format("%.2f", (double) stats.getOrDefault("profit", 0.0)));
        tvInCount.setText("入库次数：" + stats.get("in_count"));
        tvOutCount.setText("出库次数：" + stats.get("out_count"));
    }

    private void loadDailyStatistics() {
        List<Map<String, Object>> dailyStats = dbHelper.getStatisticsByDate();
        List<Map<String, Object>> listItems = new ArrayList<>();

        for (Map<String, Object> dayStats : dailyStats) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", "日期：" + dayStats.get("date"));
            item.put("details", "入库：" + dayStats.get("in_quantity") + " 件 - " + String.format("%.2f", (double) dayStats.get("in_amount")) + " 元\n" +
                    "出库：" + dayStats.get("out_quantity") + " 件 - " + String.format("%.2f", (double) dayStats.get("out_amount")) + " 元\n" +
                    "营业额：" + String.format("%.2f", (double) dayStats.get("revenue")) + " 元\n" +
                    "成本：" + String.format("%.2f", (double) dayStats.get("cost")) + " 元\n" +
                    "利润：" + String.format("%.2f", (double) dayStats.get("profit")) + " 元");
            listItems.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listItems,
                android.R.layout.two_line_list_item,
                new String[] {"date", "details"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        listViewDailyStats.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
