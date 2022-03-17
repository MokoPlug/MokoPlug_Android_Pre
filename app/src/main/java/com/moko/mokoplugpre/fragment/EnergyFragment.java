package com.moko.mokoplugpre.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.activity.DeviceInfoActivity;
import com.moko.mokoplugpre.adapter.EnergyListAdapter;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.EnergyInfo;
import com.moko.support.pre.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EnergyFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = EnergyFragment.class.getSimpleName();
    @BindView(R.id.rg_energy)
    RadioGroup rgEnergy;
    @BindView(R.id.tv_energy_total)
    TextView tvEnergyTotal;
    @BindView(R.id.tv_duration)
    TextView tvDuration;
    @BindView(R.id.tv_unit)
    TextView tvUnit;
    @BindView(R.id.rv_energy)
    RecyclerView rvEnergy;
    @BindView(R.id.rb_daily)
    RadioButton rbDaily;
    @BindView(R.id.rb_monthly)
    RadioButton rbMonthly;
    private EnergyListAdapter adapter;

    private DeviceInfoActivity activity;

    public EnergyFragment() {
    }

    public static EnergyFragment newInstance() {
        EnergyFragment fragment = new EnergyFragment();
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
                        if (rbDaily.isChecked()) {
                            int electricityConstant = MokoSupport.getInstance().electricityConstant;
                            int total = MokoSupport.getInstance().eneryTotalToday;
                            float totalToday = total * 1.0f / electricityConstant;
                            String eneryTotalToday = MokoUtils.getDecimalFormat("0.##").format(totalToday);
                            tvEnergyTotal.setText(eneryTotalToday);
                            List<EnergyInfo> energyHistoryToday = MokoSupport.getInstance().energyHistoryToday;
                            adapter.replaceData(energyHistoryToday);
                        } else {
                            int electricityConstant = MokoSupport.getInstance().electricityConstant;
                            int total = MokoSupport.getInstance().eneryTotalMonthly;
                            float totalMonthly = total * 1.0f / electricityConstant;
                            String eneryTotalMonthly = MokoUtils.getDecimalFormat("0.##").format(totalMonthly);
                            tvEnergyTotal.setText(eneryTotalMonthly);
                            List<EnergyInfo> energyHistory = MokoSupport.getInstance().energyHistory;
                            adapter.replaceData(energyHistory);
                        }
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
        View view = inflater.inflate(R.layout.fragment_energy, container, false);
        ButterKnife.bind(this, view);
        int electricityConstant = MokoSupport.getInstance().electricityConstant;
        int total = MokoSupport.getInstance().eneryTotalToday;
        float totalToday = total * 1.0f / electricityConstant;
        String eneryTotalToday = MokoUtils.getDecimalFormat("0.##").format(totalToday);
        tvEnergyTotal.setText(eneryTotalToday);
        Calendar calendar = Calendar.getInstance();
        String time = MokoUtils.calendar2strDate(calendar, "HH");
        String date = MokoUtils.calendar2strDate(calendar, "MM-dd");
        tvDuration.setText(String.format("00:00 to %s:00,%s", time, date));
        List<EnergyInfo> energyInfos = MokoSupport.getInstance().energyHistoryToday;
        if (energyInfos != null) {
            adapter = new EnergyListAdapter();
            adapter.replaceData(energyInfos);
            adapter.openLoadAnimation();
            rvEnergy.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvEnergy.setAdapter(adapter);
        }
        activity = (DeviceInfoActivity) getActivity();
        rgEnergy.setOnCheckedChangeListener(this);
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int electricityConstant = MokoSupport.getInstance().electricityConstant;
        int total;
        switch (checkedId) {
            case R.id.rb_daily:
                // 切换日
                total = MokoSupport.getInstance().eneryTotalToday;
                float totalToday = total * 1.0f / electricityConstant;
                String eneryTotalToday = MokoUtils.getDecimalFormat("0.##").format(totalToday);
                tvEnergyTotal.setText(eneryTotalToday);
                Calendar calendarDaily = Calendar.getInstance();
                String time = MokoUtils.calendar2strDate(calendarDaily, "HH");
                String date = MokoUtils.calendar2strDate(calendarDaily, "MM-dd");
                tvDuration.setText(String.format("00:00 to %s:00,%s", time, date));
                tvUnit.setText("Hour");
                List<EnergyInfo> energyHistoryToday = MokoSupport.getInstance().energyHistoryToday;
                if (energyHistoryToday != null) {
                    adapter.replaceData(energyHistoryToday);
                }
                break;
            case R.id.rb_monthly:
                // 切换月
                total = MokoSupport.getInstance().eneryTotalMonthly;
                float totalMonthly = total * 1.0f / electricityConstant;
                String eneryTotalMonthly = MokoUtils.getDecimalFormat("0.##").format(totalMonthly);
                tvEnergyTotal.setText(eneryTotalMonthly);
                Calendar calendarMonthly = Calendar.getInstance();
                String end = MokoUtils.calendar2strDate(calendarMonthly, "MM-dd");
                calendarMonthly.add(Calendar.DAY_OF_MONTH, -29);
                String start = MokoUtils.calendar2strDate(calendarMonthly, "MM-dd");
                tvDuration.setText(String.format("%s to %s", start, end));
                tvUnit.setText("Date");
                List<EnergyInfo> energyHistory = MokoSupport.getInstance().energyHistory;
                if (energyHistory != null) {
                    adapter.replaceData(energyHistory);
                }
                break;
        }
    }

    public void resetEnergyData() {
        MokoSupport.getInstance().eneryTotalToday = 0;
        MokoSupport.getInstance().eneryTotalMonthly = 0;
        MokoSupport.getInstance().energyHistoryToday.clear();
        MokoSupport.getInstance().energyHistory.clear();
        if (rbDaily.isChecked()) {
            int electricityConstant = MokoSupport.getInstance().electricityConstant;
            int total = MokoSupport.getInstance().eneryTotalToday;
            float totalToday = total * 1.0f / electricityConstant;
            String eneryTotalToday = MokoUtils.getDecimalFormat("0.##").format(totalToday);
            tvEnergyTotal.setText(eneryTotalToday);
            List<EnergyInfo> energyHistoryToday = MokoSupport.getInstance().energyHistoryToday;
            adapter.replaceData(energyHistoryToday);
        } else {
            int electricityConstant = MokoSupport.getInstance().electricityConstant;
            int total = MokoSupport.getInstance().eneryTotalMonthly;
            float totalMonthly = total * 1.0f / electricityConstant;
            String eneryTotalMonthly = MokoUtils.getDecimalFormat("0.##").format(totalMonthly);
            tvEnergyTotal.setText(eneryTotalMonthly);
            List<EnergyInfo> energyHistory = MokoSupport.getInstance().energyHistory;
            adapter.replaceData(energyHistory);
        }
    }
}
