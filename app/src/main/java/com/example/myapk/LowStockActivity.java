package com.example.myapk;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import java.util.Map;

public class LowStockActivity extends AppCompatActivity {
    ListView listView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("库存预警");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.list_view);
        dbHelper = new DBHelper(this);

        loadLowStockGoods();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            List<Map<String, Object>> list = dbHelper.getLowStockGoods();
            Map<String, Object> goods = list.get(position);
            int goodsId = (int) goods.get("id");
            Intent intent = new Intent(this, ProductEditActivity.class);
            intent.putExtra("goods_id", goodsId);
            startActivity(intent);
        });
    }

    private void loadLowStockGoods() {
        List<Map<String, Object>> lowStockList = dbHelper.getLowStockGoods();
        SimpleAdapter adapter = new SimpleAdapter(
                this, lowStockList, android.R.layout.simple_list_item_2,
                new String[]{"name", "code"},
                new int[]{android.R.id.text1, android.R.id.text2}) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                Map<String, Object> goods = lowStockList.get(position);
                text1.setText(goods.get("name") + " (库存:" + goods.get("count") + "/" + goods.get("min_stock") + ")");
                text2.setText("编码:" + goods.get("code"));
                text1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
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

    @Override
    protected void onResume() {
        super.onResume();
        loadLowStockGoods();
    }
}