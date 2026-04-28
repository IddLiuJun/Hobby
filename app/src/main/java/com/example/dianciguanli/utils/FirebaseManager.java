package com.example.dianciguanli.utils;

import android.content.Context;

import com.example.dianciguanli.data.Battery;
import com.example.dianciguanli.data.Record;

public class FirebaseManager {
    private static FirebaseManager instance;
    private boolean initialized = false;

    private FirebaseManager(Context context) {
        try {
            Class.forName("com.google.firebase.FirebaseApp");
            initialized = true;
        } catch (ClassNotFoundException e) {
            initialized = false;
        }
    }

    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context.getApplicationContext());
        }
        return instance;
    }

    public void syncBattery(Battery battery) {
        if (!initialized) return;
    }

    public void syncRecord(Record record) {
        if (!initialized) return;
    }
}