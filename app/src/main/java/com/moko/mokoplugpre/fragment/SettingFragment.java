package com.moko.mokoplugpre.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mokoplugpre.activity.DeviceInfoActivity;
import com.moko.mokoplugpre.databinding.FragmentSettingBinding;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    private FragmentSettingBinding mBind;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        activity.runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (responseType) {
                    case MokoSupport.NOTIFY_FUNCTION_ENERGY:
                        int electricityConstant = MokoSupport.getInstance().electricityConstant;
                        long total = MokoSupport.getInstance().eneryTotal;
                        float consumption = total * 1.0f / electricityConstant;
                        String energyConsumption = MokoUtils.getDecimalFormat("0.##").format(consumption);
                        mBind.tvEnergyConsumption.setText(energyConsumption);
                        break;
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSettingBinding.inflate(inflater, container, false);
        mBind.tvDeviceName.setText(MokoSupport.getInstance().advName);
        mBind.tvAdvInterval.setText(String.valueOf(MokoSupport.getInstance().advInterval));
        mBind.tvOverloadValue.setText(String.valueOf(MokoSupport.getInstance().overloadTopValue));
        mBind.tvEnergySavedInterval.setText(String.valueOf(MokoSupport.getInstance().energySavedInterval));
        mBind.tvEnergySavedPercent.setText(String.valueOf(MokoSupport.getInstance().energySavedPercent));
        int electricityConstant = MokoSupport.getInstance().electricityConstant;
        long total = MokoSupport.getInstance().eneryTotal;
        float consumption = total * 1.0f / electricityConstant;
        String energyConsumption = MokoUtils.getDecimalFormat("0.##").format(consumption);
        mBind.tvEnergyConsumption.setText(energyConsumption);
        activity = (DeviceInfoActivity) getActivity();
        EventBus.getDefault().register(this);
        return mBind.getRoot();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public void resetEnergyTotal() {
        mBind.tvEnergyConsumption.setText("0");
    }

    public void setDeviceName(String deviceName) {
        mBind.tvDeviceName.setText(deviceName);
    }

    public void setAdvInterval(int advInterval) {
        mBind.tvAdvInterval.setText(String.valueOf(advInterval));
    }

    public void setOverloadTopValue(int overloadTopValue) {
        mBind.tvOverloadValue.setText(String.valueOf(overloadTopValue));
    }

    public void setEnergySavedInterval(int energySavedInterval) {
        mBind.tvEnergySavedInterval.setText(String.valueOf(energySavedInterval));
    }

    public void setEnergySavedPercent(int energySavedPercent) {
        mBind.tvEnergySavedPercent.setText(String.valueOf(energySavedPercent));
    }
}
