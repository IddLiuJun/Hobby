package com.example.dianciguanli.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dianciguanli.R;
import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.DBHelper;
import com.example.dianciguanli.data.Record;
import com.example.dianciguanli.utils.DateUtils;

import java.util.List;

public class StatisticsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecordAdapter adapter;
    private DBHelper dbHelper;
    private TextView tvTotalIn, tvTotalOut, tvTodayIn, tvTodayOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvTotalIn = view.findViewById(R.id.tvTotalIn);
        tvTotalOut = view.findViewById(R.id.tvTotalOut);
        tvTodayIn = view.findViewById(R.id.tvTodayIn);
        tvTodayOut = view.findViewById(R.id.tvTodayOut);
        dbHelper = new DBHelper(getContext());
        loadData();
        return view;
    }

    private void loadData() {
        List<Record> recordList = dbHelper.getAllRecords();
        adapter = new RecordAdapter(recordList);
        recyclerView.setAdapter(adapter);
        calculateStatistics();
    }

    private void calculateStatistics() {
        List<Record> allRecords = dbHelper.getAllRecords();
        List<Battery> allBatteries = dbHelper.getAllBatteries();

        int totalIn = 0, totalOut = 0;
        int todayIn = 0, todayOut = 0;
        String today = DateUtils.getCurrentDate();

        for (Record record : allRecords) {
            if (record.getType() == Record.TYPE_IN) {
                totalIn += record.getQuantity();
                if (record.getCreateTime().startsWith(today)) {
                    todayIn += record.getQuantity();
                }
            } else {
                totalOut += record.getQuantity();
                if (record.getCreateTime().startsWith(today)) {
                    todayOut += record.getQuantity();
                }
            }
        }

        int totalStock = 0;
        for (Battery battery : allBatteries) {
            totalStock += battery.getQuantity();
        }

        tvTotalIn.setText("累计入库: " + totalIn);
        tvTotalOut.setText("累计出库: " + totalOut);
        tvTodayIn.setText("今日入库: " + todayIn);
        tvTodayOut.setText("今日出库: " + todayOut);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
        private List<Record> recordList;

        public RecordAdapter(List<Record> recordList) {
            this.recordList = recordList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Record record = recordList.get(position);
            Battery battery = dbHelper.getBatteryById(record.getBatteryId());
            String model = battery != null ? battery.getModel() : "未知";

            holder.tvType.setText(record.getType() == Record.TYPE_IN ? "入库" : "出库");
            holder.tvType.setTextColor(record.getType() == Record.TYPE_IN ? 0xFF22C55E : 0xFFEF4444);
            holder.tvModel.setText("型号: " + model);
            holder.tvQuantity.setText("数量: " + record.getQuantity());
            holder.tvTime.setText(DateUtils.formatDateTime(record.getCreateTime()));
        }

        @Override
        public int getItemCount() {
            return recordList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvModel, tvQuantity, tvTime;

            public ViewHolder(View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvType);
                tvModel = itemView.findViewById(R.id.tvModel);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}