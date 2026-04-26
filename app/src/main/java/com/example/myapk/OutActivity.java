package com.example.myapk;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class OutActivity extends AppCompatActivity {
    EditText edtCode, edtOutCount;
    Button btnDoOut;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("商品出库");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtCode = findViewById(R.id.edt_code);
        edtOutCount = findViewById(R.id.edt_out_count);
        btnDoOut = findViewById(R.id.btn_do_out);
        Button btnScan = findViewById(R.id.btn_scan);
        dbHelper = new DBHelper(this);

        btnScan.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, ScanActivity.class), 100);
        });

        btnDoOut.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String outStr = edtOutCount.getText().toString().trim();

            if (code.isEmpty() || outStr.isEmpty()) {
                Toast.makeText(this, "请填写完整", Toast.LENGTH_SHORT).show();
                return;
            }

            int out;
            try {
                out = Integer.parseInt(outStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "数量格式错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if (out <= 0) {
                Toast.makeText(this, "出库数量必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }

            int now = dbHelper.getGoodsCount(code);
            if (now == -1) {
                Toast.makeText(this, "商品不存在", Toast.LENGTH_SHORT).show();
            } else if (out > now) {
                Toast.makeText(this, "库存不足", Toast.LENGTH_SHORT).show();
            } else {
                boolean ok = dbHelper.doOut(code, out);
                if (ok) {
                    Toast.makeText(this, "出库成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
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