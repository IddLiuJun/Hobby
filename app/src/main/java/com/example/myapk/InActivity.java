package com.example.myapk;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class InActivity extends AppCompatActivity {
    EditText edtName, edtCode, edtCount, edtPrice, edtRemark;
    Button btnSave;
    DBHelper dbHelper;
    boolean isRemarkExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("商品入库");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtName = findViewById(R.id.edt_name);
        edtCode = findViewById(R.id.edt_code);
        edtCount = findViewById(R.id.edt_count);
        edtPrice = findViewById(R.id.edt_price);
        edtRemark = findViewById(R.id.edt_remark);
        btnSave = findViewById(R.id.btn_save);
        Button btnScan = findViewById(R.id.btn_scan);
        LinearLayout layoutRemarkToggle = findViewById(R.id.layout_remark_toggle);
        TextView tvRemarkIndicator = findViewById(R.id.tv_remark_indicator);
        dbHelper = new DBHelper(this);

        layoutRemarkToggle.setOnClickListener(v -> {
            isRemarkExpanded = !isRemarkExpanded;
            if (isRemarkExpanded) {
                edtRemark.setVisibility(View.VISIBLE);
                tvRemarkIndicator.setText("▲");
            } else {
                edtRemark.setVisibility(View.GONE);
                tvRemarkIndicator.setText("▼");
            }
        });

        btnScan.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, ScanActivity.class), 100);
        });

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String code = edtCode.getText().toString().trim();
            String countStr = edtCount.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String remark = edtRemark.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty() || countStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "请填写完整信息（*为必填项）", Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            double price;
            try {
                count = Integer.parseInt(countStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "数量或价格格式错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count <= 0 || price <= 0) {
                Toast.makeText(this, "数量和价格必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = dbHelper.addGoods(name, code, count, price, remark);
            if (ok) {
                Toast.makeText(this, "入库成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "编码已存在", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra("SCAN_RESULT");
            edtCode.setText(code);
        }
    }
}