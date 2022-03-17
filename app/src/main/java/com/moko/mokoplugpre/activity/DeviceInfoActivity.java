package com.moko.mokoplugpre.activity;


import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.dialog.AlertMessageDialog;
import com.moko.mokoplugpre.dialog.LoadingMessageDialog;
import com.moko.mokoplugpre.fragment.EnergyFragment;
import com.moko.mokoplugpre.fragment.PowerFragment;
import com.moko.mokoplugpre.fragment.SettingFragment;
import com.moko.mokoplugpre.fragment.TimerFragment;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.OrderTaskAssembler;
import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.entity.ParamsWriteKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.IdRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.radioBtn_power)
    RadioButton radioBtnPower;
    @BindView(R.id.radioBtn_energy)
    RadioButton radioBtnEnergy;
    @BindView(R.id.radioBtn_timer)
    RadioButton radioBtnTimer;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    private FragmentManager fragmentManager;
    private PowerFragment powerFragment;
    private EnergyFragment energyFragment;
    private TimerFragment timerFragment;
    private SettingFragment settingFragment;
    public String mDeviceMac;
    public String mDeviceName;
    private int validCount;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        initFragment();
        rgOptions.setOnCheckedChangeListener(this);
        radioBtnPower.setChecked(true);
        tvTitle.setText(MokoSupport.getInstance().advName);
        mDeviceName = MokoSupport.getInstance().advName;
        mDeviceMac = MokoSupport.getInstance().mac;
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
    }

    private void initFragment() {
        fragmentManager = getFragmentManager();
        powerFragment = PowerFragment.newInstance();
        energyFragment = EnergyFragment.newInstance();
        timerFragment = TimerFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, powerFragment)
                .add(R.id.frame_container, energyFragment)
                .add(R.id.frame_container, timerFragment)
                .add(R.id.frame_container, settingFragment)
                .show(powerFragment)
                .hide(energyFragment)
                .hide(timerFragment)
                .hide(settingFragment)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    MokoSupport.getInstance().countDown = 0;
                    MokoSupport.getInstance().countDownInit = 0;
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS_WRITE:
                        final int cmd = value[1] & 0xFF;
                        ParamsWriteKeyEnum configKeyEnum = ParamsWriteKeyEnum.fromParamKey(cmd);
                        switch (configKeyEnum) {
                            case SET_RESET_ENERGY_TOTAL:
                                settingFragment.resetEnergyTotal();
                                energyFragment.resetEnergyData();
                                break;
                            case SET_RESET:
                                break;
                        }
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @OnClick({R.id.tv_back, R.id.iv_more})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.iv_more:
                startActivity(new Intent(this, MoreActivity.class));
                break;
        }
    }

    private void back() {
        if (MokoSupport.getInstance().isBluetoothOpen()) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Disconnect Device");
            dialog.setMessage("Please confirm again whether to disconnect the device.");
            dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                @Override
                public void onClick() {
                    MokoSupport.getInstance().disConnectBle();
                }
            });
            dialog.show(getSupportFragmentManager());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_power:
                fragmentManager.beginTransaction()
                        .show(powerFragment)
                        .hide(energyFragment)
                        .hide(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_energy:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .show(energyFragment)
                        .hide(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_timer:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .hide(energyFragment)
                        .show(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_setting:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .hide(energyFragment)
                        .hide(timerFragment)
                        .show(settingFragment)
                        .commit();
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Power
    ///////////////////////////////////////////////////////////////////////////

    public void changeSwitchState(boolean switchState) {
        showSyncingProgressDialog();
        OrderTask orderTask = OrderTaskAssembler.writeSwitchState(switchState ? 1 : 0);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Timer
    ///////////////////////////////////////////////////////////////////////////

    public void setTimer(int countdown) {
        showSyncingProgressDialog();
        OrderTask orderTask = OrderTaskAssembler.writeCountdown(countdown);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setting
    ///////////////////////////////////////////////////////////////////////////

    public void resetEnergyConsumption() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset Energy Consumption");
        dialog.setMessage("Please confirm again whether to reset the accumulated electricity? Value will be recounted after clearing.");
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                showSyncingProgressDialog();
                OrderTask orderTask = OrderTaskAssembler.writeResetEnergyTotal();
                MokoSupport.getInstance().sendOrder(orderTask);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void reset() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset Device");
        dialog.setMessage("After reset,the relevant data will be totally cleared");
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                showSyncingProgressDialog();
                OrderTask orderTask = OrderTaskAssembler.writeReset();
                MokoSupport.getInstance().sendOrder(orderTask);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void changeName() {
        tvTitle.setText(MokoSupport.getInstance().advName);
    }
}
