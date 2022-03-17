package com.moko.mokoplugpre.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;
import com.moko.mokoplugpre.activity.DeviceInfoActivity;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    @BindView(R2.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R2.id.tv_adv_interval)
    TextView tvAdvInterval;
    @BindView(R2.id.tv_overload_value)
    TextView tvOverloadValue;
    @BindView(R2.id.tv_energy_saved_interval)
    TextView tvEnergySavedInterval;
    @BindView(R2.id.tv_energy_saved_percent)
    TextView tvEnergySavedPercent;
    @BindView(R2.id.tv_energy_consumption)
    TextView tvEnergyConsumption;

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
                        tvEnergyConsumption.setText(energyConsumption);
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
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        tvDeviceName.setText(MokoSupport.getInstance().advName);
        tvAdvInterval.setText(String.valueOf(MokoSupport.getInstance().advInterval));
        tvOverloadValue.setText(String.valueOf(MokoSupport.getInstance().overloadTopValue));
        tvEnergySavedInterval.setText(String.valueOf(MokoSupport.getInstance().energySavedInterval));
        tvEnergySavedPercent.setText(String.valueOf(MokoSupport.getInstance().energySavedPercent));
        int electricityConstant = MokoSupport.getInstance().electricityConstant;
        long total = MokoSupport.getInstance().eneryTotal;
        float consumption = total * 1.0f / electricityConstant;
        String energyConsumption = MokoUtils.getDecimalFormat("0.##").format(consumption);
        tvEnergyConsumption.setText(energyConsumption);
        activity = (DeviceInfoActivity) getActivity();
        EventBus.getDefault().register(this);
        return view;
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
        tvEnergyConsumption.setText("0");
    }

    public void setDeviceName(String deviceName) {
        tvDeviceName.setText(deviceName);
    }

    public void setAdvInterval(int advInterval) {
        tvAdvInterval.setText(String.valueOf(advInterval));
    }

    public void setOverloadTopValue(int overloadTopValue) {
        tvOverloadValue.setText(String.valueOf(overloadTopValue));
    }

    public void setEnergySavedInterval(int energySavedInterval) {
        tvEnergySavedInterval.setText(String.valueOf(energySavedInterval));
    }

    public void setEnergySavedPercent(int energySavedPercent) {
        tvEnergySavedPercent.setText(String.valueOf(energySavedPercent));
    }
}
