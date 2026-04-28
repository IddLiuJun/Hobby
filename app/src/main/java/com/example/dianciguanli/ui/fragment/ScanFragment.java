package com.example.dianciguanli.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dianciguanli.R;
import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.DBHelper;
import com.example.dianciguanli.data.Record;
import com.example.dianciguanli.utils.DateUtils;
import com.example.dianciguanli.utils.FirebaseManager;
import com.example.dianciguanli.utils.IDUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanFragment extends Fragment {
    private Button btnScanIn, btnScanOut;
    private TextView tvResult;
    private DBHelper dbHelper;
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        btnScanIn = view.findViewById(R.id.btnScanIn);
        btnScanOut = view.findViewById(R.id.btnScanOut);
        tvResult = view.findViewById(R.id.tvResult);
        dbHelper = new DBHelper(getContext());
        firebaseManager = FirebaseManager.getInstance(getContext());

        btnScanIn.setOnClickListener(v -> scanQRCode(Record.TYPE_IN));
        btnScanOut.setOnClickListener(v -> scanQRCode(Record.TYPE_OUT));

        return view;
    }

    private void scanQRCode(int type) {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(false);
        integrator.setPrompt(type == Record.TYPE_IN ? "扫描入库二维码" : "扫描出库二维码");
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContent = result.getContents();
            String[] parts = qrContent.split("\\|");
            if (parts.length >= 1) {
                String batteryId = parts[0];
                showQuantityDialog(batteryId);
            } else {
                Toast.makeText(getContext(), "二维码格式错误", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showQuantityDialog(String batteryId) {
        Battery battery = dbHelper.getBatteryById(batteryId);
        if (battery == null) {
            Toast.makeText(getContext(), "未找到该电池信息", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_quantity, null);
        builder.setView(dialogView);

        TextView tvBatteryInfo = dialogView.findViewById(R.id.tvBatteryInfo);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Button btnIn = dialogView.findViewById(R.id.btnIn);
        Button btnOut = dialogView.findViewById(R.id.btnOut);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvBatteryInfo.setText("型号: " + battery.getModel() + "\n规格: " + battery.getSpecification() + "\n批次: " + battery.getBatch() + "\n当前库存: " + battery.getQuantity());

        AlertDialog dialog = builder.create();

        btnIn.setOnClickListener(v -> {
            int quantity = parseQuantity(etQuantity.getText().toString());
            if (quantity > 0) {
                performStockOperation(battery, quantity, Record.TYPE_IN);
                dialog.dismiss();
            }
        });

        btnOut.setOnClickListener(v -> {
            int quantity = parseQuantity(etQuantity.getText().toString());
            if (quantity > 0) {
                if (quantity <= battery.getQuantity()) {
                    performStockOperation(battery, quantity, Record.TYPE_OUT);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "库存不足", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private int parseQuantity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "请输入有效数量", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    private void performStockOperation(Battery battery, int quantity, int type) {
        if (type == Record.TYPE_IN) {
            battery.setQuantity(battery.getQuantity() + quantity);
        } else {
            battery.setQuantity(battery.getQuantity() - quantity);
        }
        battery.setUpdateTime(DateUtils.getCurrentDateTime());
        dbHelper.updateBattery(battery);
        firebaseManager.syncBattery(battery);

        Record record = new Record(battery.getId(), type, quantity, "操作员", DateUtils.getCurrentDateTime());
        record.setId(IDUtils.generateId());
        dbHelper.insertRecord(record);
        firebaseManager.syncRecord(record);

        String message = type == Record.TYPE_IN ? "入库成功" : "出库成功";
        tvResult.setText(message + "\n型号: " + battery.getModel() + "\n数量: " + quantity);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}