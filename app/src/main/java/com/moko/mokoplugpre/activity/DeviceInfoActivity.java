package com.moko.mokoplugpre.activity;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mokoplugpre.AppConstants;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;
import com.moko.mokoplugpre.dialog.AlertMessageDialog;
import com.moko.mokoplugpre.dialog.LoadingMessageDialog;
import com.moko.mokoplugpre.fragment.EnergyFragment;
import com.moko.mokoplugpre.fragment.PowerFragment;
import com.moko.mokoplugpre.fragment.SettingFragment;
import com.moko.mokoplugpre.fragment.TimerFragment;
import com.moko.mokoplugpre.service.DfuService;
import com.moko.mokoplugpre.utils.FileUtils;
import com.moko.mokoplugpre.utils.ToastUtils;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.OrderTaskAssembler;
import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.entity.ParamsWriteKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R2.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R2.id.tv_title)
    TextView tvTitle;
    @BindView(R2.id.radioBtn_power)
    RadioButton radioBtnPower;
    @BindView(R2.id.radioBtn_energy)
    RadioButton radioBtnEnergy;
    @BindView(R2.id.radioBtn_timer)
    RadioButton radioBtnTimer;
    @BindView(R2.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R2.id.rg_options)
    RadioGroup rgOptions;
    private FragmentManager fragmentManager;
    private PowerFragment powerFragment;
    private EnergyFragment energyFragment;
    private TimerFragment timerFragment;
    private SettingFragment settingFragment;
    public String mDeviceMac;
    public String mDeviceName;
    private int validCount;
    private boolean isUpdate;
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
                    if (isUpdate) {
                        return;
                    }
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

    public void onBack(View view) {
        if (isWindowLocked())
            return;
        back();
    }

    public void onMore(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, MoreActivity.class));
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
        if (checkedId == R.id.radioBtn_power) {
            fragmentManager.beginTransaction()
                    .show(powerFragment)
                    .hide(energyFragment)
                    .hide(timerFragment)
                    .hide(settingFragment)
                    .commit();
        } else if (checkedId == R.id.radioBtn_energy) {
            fragmentManager.beginTransaction()
                    .hide(powerFragment)
                    .show(energyFragment)
                    .hide(timerFragment)
                    .hide(settingFragment)
                    .commit();
        } else if (checkedId == R.id.radioBtn_timer) {
            fragmentManager.beginTransaction()
                    .hide(powerFragment)
                    .hide(energyFragment)
                    .show(timerFragment)
                    .hide(settingFragment)
                    .commit();
        } else if (checkedId == R.id.radioBtn_setting) {
            fragmentManager.beginTransaction()
                    .hide(powerFragment)
                    .hide(energyFragment)
                    .hide(timerFragment)
                    .show(settingFragment)
                    .commit();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Power
    ///////////////////////////////////////////////////////////////////////////

    public void onOnOff(View view) {
        if (isWindowLocked())
            return;
        powerFragment.changeSwitchState();
    }

    public void changeSwitchState(boolean switchState) {
        showSyncingProgressDialog();
        OrderTask orderTask = OrderTaskAssembler.writeSwitchState(switchState ? 1 : 0);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Timer
    ///////////////////////////////////////////////////////////////////////////
    public void onTimer(View view) {
        if (isWindowLocked())
            return;
        timerFragment.setTimer();
    }

    public void setTimer(int countdown) {
        showSyncingProgressDialog();
        OrderTask orderTask = OrderTaskAssembler.writeCountdown(countdown);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setting
    ///////////////////////////////////////////////////////////////////////////
    public void changeName() {
        tvTitle.setText(MokoSupport.getInstance().advName);
    }


    public void onModifyName(View view) {
        if (isWindowLocked())
            return;
        startActivityForResult(new Intent(this, ModifyNameActivity.class), AppConstants.REQUEST_CODE_MODIFY_NAME);
    }

    public void onModifyPowerStatus(View view) {
        if (isWindowLocked())
            return;
        // 修改上电状态
        startActivityForResult(new Intent(this, ModifyPowerStatusActivity.class), AppConstants.REQUEST_CODE_MODIFY_POWER_STATUS);

    }

    public void onCheckUpdate(View view) {
        if (isWindowLocked())
            return;
        // 升级
        chooseFirmwareFile();
    }

    public void onModifyAdvInterval(View view) {
        if (isWindowLocked())
            return;
        // 修改广播间隔
        startActivityForResult(new Intent(this, AdvIntervalActivity.class), AppConstants.REQUEST_CODE_ADV_INTERVAL);

    }

    public void onModifyOverloadValue(View view) {
        if (isWindowLocked())
            return;
        // 修改过载保护值
        startActivityForResult(new Intent(this, OverloadValueActivity.class), AppConstants.REQUEST_CODE_OVERLOAD_VALUE);

    }

    public void onModifyPowerReportInterval(View view) {
        if (isWindowLocked())
            return;
        // 修改电能上报间隔
        startActivityForResult(new Intent(this, EnergySavedIntervalActivity.class), AppConstants.REQUEST_CODE_ENERGY_SAVED_INTERVAL);
    }

    public void onModifyPowerChangeNotification(View view) {
        if (isWindowLocked())
            return;
        // 修改电能变化百分比
        startActivityForResult(new Intent(this, EnergySavedPercentActivity.class), AppConstants.REQUEST_CODE_ENERGY_SAVED_PERCENT);
    }

    public void onModifyEnergyConsumption(View view) {
        if (isWindowLocked())
            return;
        // 重置累计电能
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

    public void onReset(View view) {
        if (isWindowLocked())
            return;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_MODIFY_NAME) {
            if (resultCode == RESULT_OK) {
                final String deviceName = MokoSupport.getInstance().advName;
                settingFragment.setDeviceName(deviceName);
                changeName();
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ADV_INTERVAL) {
            if (resultCode == RESULT_OK) {
                final int advInterval = MokoSupport.getInstance().advInterval;
                settingFragment.setAdvInterval(advInterval);
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_OVERLOAD_VALUE) {
            if (resultCode == RESULT_OK) {
                final int overloadTopValue = MokoSupport.getInstance().overloadTopValue;
                settingFragment.setOverloadTopValue(overloadTopValue);
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ENERGY_SAVED_INTERVAL) {
            if (resultCode == RESULT_OK) {
                final int energySavedInterval = MokoSupport.getInstance().energySavedInterval;
                settingFragment.setEnergySavedInterval(energySavedInterval);
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ENERGY_SAVED_PERCENT) {
            if (resultCode == RESULT_OK) {
                final int energySavedPercent = MokoSupport.getInstance().energySavedPercent;
                settingFragment.setEnergySavedPercent(energySavedPercent);
            }
        }
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath))
                    return;
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuService.class);
                    showDFUProgressDialog("Waiting...");
                    isUpdate = true;
                } else {
                    ToastUtils.showToast(this, "file is not exists!");
                }
            }
        }
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
        builder.setTitle("Dismiss");
        builder.setCancelable(false);
        builder.setMessage("The device disconnected!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.show();
    }

    private int mDeviceConnectCount;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                Toast.makeText(DeviceInfoActivity.this, "Error:DFU Failed", Toast.LENGTH_SHORT).show();
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            ToastUtils.showToast(DeviceInfoActivity.this, "DFU Successfully!");
            isUpdate = !isUpdate;
            dismissDFUProgressDialog();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            mDFUDialog.setMessage("Progress:" + percent + "%");
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            ToastUtils.showToast(DeviceInfoActivity.this, "Opps!DFU Failed. Please try again!");
            XLog.i("Error:" + message);
            isUpdate = !isUpdate;
            dismissDFUProgressDialog();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }
}
