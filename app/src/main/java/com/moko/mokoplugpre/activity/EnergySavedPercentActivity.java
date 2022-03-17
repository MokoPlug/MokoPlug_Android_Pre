package com.moko.mokoplugpre.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.dialog.LoadingMessageDialog;
import com.moko.mokoplugpre.utils.ToastUtils;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.OrderTaskAssembler;
import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.entity.ParamsWriteKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EnergySavedPercentActivity extends BaseActivity {


    @BindView(R.id.et_energy_saved_percent)
    EditText etEnergySavedPercent;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_saved_percent);
        ButterKnife.bind(this);

        int energySavedPercent = MokoSupport.getInstance().energySavedPercent;
        etEnergySavedPercent.setText(String.valueOf(energySavedPercent));
        etEnergySavedPercent.setSelection(String.valueOf(energySavedPercent).length());

        getFocuable(etEnergySavedPercent);

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    if (MokoSupport.getInstance().isBluetoothOpen()) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                ToastUtils.showToast(this, R.string.timeout);
            }
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
                            case SET_ENERGY_SAVED_PARAMS:
                                if (0 == (value[3] & 0xFF)) {
                                    MokoSupport.getInstance().energySavedPercent = Integer.parseInt(etEnergySavedPercent.getText().toString());
                                    ToastUtils.showToast(EnergySavedPercentActivity.this, R.string.success);
                                    EnergySavedPercentActivity.this.setResult(EnergySavedPercentActivity.this.RESULT_OK);
                                    finish();
                                } else {
                                    ToastUtils.showToast(EnergySavedPercentActivity.this, R.string.failed);
                                }
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

    @OnClick({R.id.tv_back, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_confirm:
                String energySavedPercent = etEnergySavedPercent.getText().toString();
                if (TextUtils.isEmpty(energySavedPercent)) {
                    ToastUtils.showToast(this, "can't be blank");
                    return;
                }
                int percent = Integer.parseInt(energySavedPercent);
                if (percent < 1 || percent > 100) {
                    ToastUtils.showToast(this, "the range is 1~100");
                    return;
                }
                showSyncingProgressDialog();
                int energySavedInterval = MokoSupport.getInstance().energySavedInterval;
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.writeEnergySavedParams(energySavedInterval, percent));
                break;
        }
    }

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
}
