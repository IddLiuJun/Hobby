package com.example.myapk;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import java.util.Map;

public class OperationLogsActivity extends AppCompatActivity {
    ListView listView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_logs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("操作日志");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.list_view);
        dbHelper = new DBHelper(this);

        loadLogs();
    }

    private void loadLogs() {
        List<Map<String, Object>> logsList = dbHelper.getOperationLogs();
        SimpleAdapter adapter = new SimpleAdapter(
                this, logsList, android.R.layout.simple_list_item_2,
                new String[]{"operation_time", "product_name"},
                new int[]{android.R.id.text1, android.R.id.text2}) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                Map<String, Object> log = logsList.get(position);
                text1.setText(log.get("operation_time").toString());
                text2.setText(log.get("operation_type") + ": " + log.get("product_name") + " (" + log.get("quantity") + ")");
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}