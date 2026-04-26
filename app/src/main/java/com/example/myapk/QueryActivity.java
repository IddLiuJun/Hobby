package com.example.myapk;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class QueryActivity extends AppCompatActivity {
    EditText edtCode;
    Button btnSearch;
    TextView tvResult;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("商品查询");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        edtCode = findViewById(R.id.edt_code);
        btnSearch = findViewById(R.id.btn_search);
        tvResult = findViewById(R.id.tv_result);
        dbHelper = new DBHelper(this);

        btnSearch.setOnClickListener(v -> {
            String code = edtCode.getText().toString();
            if (code.isEmpty()) {
                tvResult.setText("请输入商品编码");
                return;
            }
            int count = dbHelper.getGoodsCount(code);
            if (count == -1) {
                tvResult.setText("无此商品");
            } else {
                tvResult.setText("库存数量：" + count);
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}