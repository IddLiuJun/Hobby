package com.example.myapk;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import java.util.Map;

public class AllInventoryActivity extends AppCompatActivity {
    ListView listView;
    EditText edtSearch;
    Button btnSearch;
    DBHelper dbHelper;
    List<Map<String, Object>> goodsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("全部库存");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.list_view);
        edtSearch = findViewById(R.id.edt_search);
        btnSearch = findViewById(R.id.btn_search);
        dbHelper = new DBHelper(this);

        loadGoods();

        // 添加文本变化监听器，实现动态搜索
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadGoods();
                } else {
                    searchGoods(keyword);
                }
            }
        });

        btnSearch.setOnClickListener(v -> {
            String keyword = edtSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                loadGoods();
            } else {
                searchGoods(keyword);
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> goods = goodsList.get(position);
            int goodsId = (int) goods.get("id");
            Intent intent = new Intent(this, ProductEditActivity.class);
            intent.putExtra("goods_id", goodsId);
            startActivity(intent);
        });
    }

    private void loadGoods() {
        goodsList = dbHelper.getAllGoods();
        updateListView();
    }

    private void searchGoods(String keyword) {
        goodsList = dbHelper.searchGoods(keyword);
        updateListView();
    }

    private void updateListView() {
        SimpleAdapter adapter = new SimpleAdapter(
                this, goodsList, android.R.layout.simple_list_item_2,
                new String[]{"name", "code"},
                new int[]{android.R.id.text1, android.R.id.text2}) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                Map<String, Object> goods = goodsList.get(position);
                int count = (int) goods.get("count");
                int minStock = (int) goods.get("min_stock");
                text1.setText(goods.get("name") + " (库存:" + count + ")");
                text2.setText("编码:" + goods.get("code") + " 备注:" + goods.get("remark"));
                if (minStock > 0 && count <= minStock) {
                    text1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    text1.setTextColor(getResources().getColor(android.R.color.black));
                }
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
        loadGoods();
    }
}