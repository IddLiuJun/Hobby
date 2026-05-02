package com.example.dianciguanli.ui.fragment;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dianciguanli.R;
import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.DBHelper;
import com.example.dianciguanli.utils.FirebaseManager;
import com.example.dianciguanli.utils.QRCodeUtils;

import java.util.List;

public class InventoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private BatteryAdapter adapter;
    private DBHelper dbHelper;
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DBHelper(getContext());
        firebaseManager = FirebaseManager.getInstance(getContext());
        loadData();
        return view;
    }

    private void loadData() {
        List<Battery> batteryList = dbHelper.getAllBatteries();
        adapter = new BatteryAdapter(batteryList);
        recyclerView.setAdapter(adapter);
    }

    public void refreshData() {
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void showBatteryDetailDialog(Battery battery, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_battery_detail, null);
        builder.setView(dialogView);

        ImageView ivQRCode = dialogView.findViewById(R.id.ivQRCode);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvBatteryDetail = dialogView.findViewById(R.id.tvBatteryDetail);
        Button btnSaveToAlbum = dialogView.findViewById(R.id.btnSaveToAlbum);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        tvDialogTitle.setText(battery.getModel());
        tvBatteryDetail.setText(
            "规格: " + battery.getSpecification() + "\n" +
            "批次: " + battery.getBatch() + "\n" +
            "库存: " + battery.getQuantity() + "\n" +
            "ID: " + battery.getId()
        );

        String qrContent = battery.getId() + "|" + battery.getModel() + "|" + battery.getSpecification() + "|" + battery.getBatch();
        Bitmap qrBitmap = QRCodeUtils.generateQRCode(qrContent, 400, 400);
        Bitmap qrWithText = null;
        if (qrBitmap != null) {
            String shortId = battery.getId().length() > 8 ? battery.getId().substring(0, 8) : battery.getId();
            qrWithText = QRCodeUtils.addTextToQRCode(qrBitmap, "编号: " + shortId);
            Bitmap borderedBitmap = QRCodeUtils.addWhiteBorder(qrWithText, 20);
            ivQRCode.setImageBitmap(borderedBitmap);
        }

        AlertDialog dialog = builder.create();

        final Bitmap finalQrBitmap = qrWithText;
        btnSaveToAlbum.setOnClickListener(v -> {
            if (finalQrBitmap != null) {
                String fileName = "电池二维码_" + battery.getModel() + "_" + System.currentTimeMillis() + ".png";
                boolean success = MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    finalQrBitmap,
                    fileName,
                    "电池型号：" + battery.getModel()
                ) != null;
                if (success) {
                    Toast.makeText(getContext(), "二维码已保存到相册", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("确认删除")
                .setMessage("确定要删除 \"" + battery.getModel() + "\" 吗？")
                .setPositiveButton("删除", (dialog1, which) -> {
                    dbHelper.deleteBattery(battery.getId());
                    firebaseManager.deleteBattery(battery.getId());
                    loadData();
                    Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private class BatteryAdapter extends RecyclerView.Adapter<BatteryAdapter.ViewHolder> {
        private List<Battery> batteryList;

        public BatteryAdapter(List<Battery> batteryList) {
            this.batteryList = batteryList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_battery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Battery battery = batteryList.get(position);
            holder.tvModel.setText(battery.getModel());
            holder.tvSpec.setText(battery.getSpecification());
            holder.tvBatch.setText("批次: " + battery.getBatch());
            holder.tvQuantity.setText("库存: " + battery.getQuantity());

            holder.itemView.setOnClickListener(v -> {
                showBatteryDetailDialog(battery, holder.getAdapterPosition());
            });
        }

        @Override
        public int getItemCount() {
            return batteryList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvModel, tvSpec, tvBatch, tvQuantity;

            public ViewHolder(View itemView) {
                super(itemView);
                tvModel = itemView.findViewById(R.id.tvModel);
                tvSpec = itemView.findViewById(R.id.tvSpec);
                tvBatch = itemView.findViewById(R.id.tvBatch);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
            }
        }
    }
}