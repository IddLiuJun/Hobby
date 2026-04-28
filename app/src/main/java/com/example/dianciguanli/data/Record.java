package com.example.dianciguanli.data;

import java.io.Serializable;

public class Record implements Serializable {
    public static final int TYPE_IN = 1;
    public static final int TYPE_OUT = 2;

    private String id;
    private String batteryId;
    private int type;
    private int quantity;
    private String operator;
    private String createTime;

    public Record() {
    }

    public Record(String batteryId, int type, int quantity, String operator, String createTime) {
        this.batteryId = batteryId;
        this.type = type;
        this.quantity = quantity;
        this.operator = operator;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(String batteryId) {
        this.batteryId = batteryId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}