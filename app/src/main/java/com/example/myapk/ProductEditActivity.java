package com.example.myapk;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Map;

public class ProductEditActivity extends AppCompatActivity {
    EditText edtName, edtCode, edtCount, edtPrice, edtRemark, edtMinStock;
    Button btnSave, btnDelete;
    DBHelper dbHelper;
    int goodsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("编辑商品");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtName = findViewById(R.id.edt_name);
        edtCode = findViewById(R.id.edt_code);
        edtCount = findViewById(R.id.edt_count);
        edtPrice = findViewById(R.id.edt_price);
        edtRemark = findViewById(R.id.edt_remark);
        edtMinStock = findViewById(R.id.edt_min_stock);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        dbHelper = new DBHelper(this);

        goodsId = getIntent().getIntExtra("goods_id", -1);
        if (goodsId == -1) {
            finish();
            return;
        }

        loadGoods();

        btnSave.setOnClickListener(v -> saveGoods());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadGoods() {
        Map<String, Object> goods = dbHelper.getGoodsById(goodsId);
        if (goods != null) {
            edtName.setText((String) goods.get("name"));
            edtCode.setText((String) goods.get("code"));
            edtCount.setText(String.valueOf(goods.get("count")));
            edtPrice.setText(String.valueOf(goods.get("price")));
            edtRemark.setText((String) goods.get("remark"));
            edtMinStock.setText(String.valueOf(goods.get("min_stock")));
        }
    }

    private void saveGoods() {
        String name = edtName.getText().toString().trim();
        String code = edtCode.getText().toString().trim();
        String countStr = edtCount.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String remark = edtRemark.getText().toString().trim();
        String minStockStr = edtMinStock.getText().toString().trim();

        if (name.isEmpty() || code.isEmpty() || countStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "请填写完整", Toast.LENGTH_SHORT).show();
            return;
        }

        int count, minStock = 0;
        double price;
        try {
            count = Integer.parseInt(countStr);
            price = Double.parseDouble(priceStr);
            minStock = Integer.parseInt(minStockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbHelper.updateGoods(goodsId, name, code, count, price, remark, minStock);
        if (ok) {
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除此商品吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    boolean ok = dbHelper.deleteGoods(goodsId);
                    if (ok) {
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                        finish();
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
