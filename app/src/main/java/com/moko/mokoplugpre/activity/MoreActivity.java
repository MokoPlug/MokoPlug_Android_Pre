package com.moko.mokoplugpre.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;
import com.moko.support.pre.MokoSupport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @Date 2020/4/30
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.mokoplugpre.activity.MoreActivity
 */
public class MoreActivity extends BaseActivity {


    @BindView(R2.id.tv_title)
    TextView tvTitle;
    @BindView(R2.id.tv_product_name)
    TextView tvProductName;
    @BindView(R2.id.tv_firmware_version)
    TextView tvFirmwareVersion;
    @BindView(R2.id.tv_device_mac)
    TextView tvDeviceMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        tvTitle.setText(MokoSupport.getInstance().advName);
        tvProductName.setText(MokoSupport.getInstance().advName);
        tvFirmwareVersion.setText(MokoSupport.getInstance().firmwareVersion);
        tvDeviceMac.setText(MokoSupport.getInstance().mac);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void back(View view) {
        finish();
    }
}
