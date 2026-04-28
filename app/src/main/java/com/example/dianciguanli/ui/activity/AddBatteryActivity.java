package com.example.dianciguanli.ui.activity;
import android.provider.MediaStore;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.dianciguanli.R;
import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.DBHelper;
import com.example.dianciguanli.utils.DateUtils;
import com.example.dianciguanli.utils.FirebaseManager;
import com.example.dianciguanli.utils.IDUtils;
import com.example.dianciguanli.utils.QRCodeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddBatteryActivity extends AppCompatActivity {
    private EditText etModel, etSpec, etBatch, etQuantity;
    private Button btnGenerate, btnSave, btnExport;
    private ImageView ivQRCode;
    private DBHelper dbHelper;
    private FirebaseManager firebaseManager;
    private Bitmap qrBitmap;
    private Battery currentBattery;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_battery);

        etModel = findViewById(R.id.etModel);
        etSpec = findViewById(R.id.etSpec);
        etBatch = findViewById(R.id.etBatch);
        etQuantity = findViewById(R.id.etQuantity);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnSave = findViewById(R.id.btnSave);
        btnExport = findViewById(R.id.btnExport);
        ivQRCode = findViewById(R.id.ivQRCode);

        dbHelper = new DBHelper(this);
        firebaseManager = FirebaseManager.getInstance(this);

        btnGenerate.setOnClickListener(v -> generateQRCode());
        btnSave.setOnClickListener(v -> saveBattery());
        btnExport.setOnClickListener(v -> exportQRCode());

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void generateQRCode() {
        String model = etModel.getText().toString().trim();
        String spec = etSpec.getText().toString().trim();
        String batch = etBatch.getText().toString().trim();

        if (model.isEmpty() || spec.isEmpty() || batch.isEmpty()) {
            Toast.makeText(this, "请填写完整的电池信息", Toast.LENGTH_SHORT).show();
            return;
        }

        String batteryId = IDUtils.generateId();
        String qrContent = batteryId + "|" + model + "|" + spec + "|" + batch;
        qrBitmap = QRCodeUtils.generateQRCode(qrContent, 400, 400);

        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            currentBattery = new Battery(model, spec, batch, 0, DateUtils.getCurrentDateTime(), DateUtils.getCurrentDateTime());
            currentBattery.setId(batteryId);
            Toast.makeText(this, "二维码生成成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "二维码生成失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBattery() {
        if (currentBattery == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityStr = etQuantity.getText().toString().trim();
        int quantity = 0;
        if (!quantityStr.isEmpty()) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效数量", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentBattery.setQuantity(quantity);
        dbHelper.insertBattery(currentBattery);
        firebaseManager.syncBattery(currentBattery);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
private void exportQRCode() {
    if (qrBitmap == null) {
        Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show();
        return;
    }

    // 保存到 系统相册（相机位置）
    String fileName = "电池二维码_" + currentBattery.getModel() + "_" + System.currentTimeMillis() + ".png";
    
    // 插入系统相册，所有手机都能立刻看到
    boolean success = MediaStore.Images.Media.insertImage(
        getContentResolver(),
        qrBitmap,
        fileName,
        "电池型号：" + currentBattery.getModel()
    ) != null;

    if (success) {
        Toast.makeText(this, "✅ 二维码已保存到【手机相册】", Toast.LENGTH_LONG).show();
    } else {
        Toast.makeText(this, "❌ 保存失败，请开启存储权限", Toast.LENGTH_SHORT).show();
    }
}
}