package com.ttsandroid.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.ttsandroid.domain.ChargingGate;

public final class AndroidChargingGate implements ChargingGate {
    private final Context context;

    public AndroidChargingGate(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean isCharging() {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) {
            return false;
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
