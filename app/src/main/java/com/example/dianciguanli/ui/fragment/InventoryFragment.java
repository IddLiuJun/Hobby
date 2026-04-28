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

import java.util.List;

public class InventoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private BatteryAdapter adapter;
    private DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DBHelper(getContext());
        loadData();
        return view;
    }

    private void loadData() {
        List<Battery> batteryList = dbHelper.getAllBatteries();
        adapter = new BatteryAdapter(batteryList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
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