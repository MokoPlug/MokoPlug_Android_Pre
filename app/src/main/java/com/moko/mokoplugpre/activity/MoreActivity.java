package com.moko.mokoplugpre.activity;

import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.mokoplugpre.databinding.ActivityMoreBinding;
import com.moko.support.pre.MokoSupport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @Date 2020/4/30
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.mokoplugpre.activity.MoreActivity
 */
public class MoreActivity extends BaseActivity<ActivityMoreBinding> {

    @Override
    protected void onCreate() {
        EventBus.getDefault().register(this);
        mBind.tvTitle.setText(MokoSupport.getInstance().advName);
        mBind.tvProductName.setText(MokoSupport.getInstance().advName);
        mBind.tvFirmwareVersion.setText(MokoSupport.getInstance().firmwareVersion);
        mBind.tvDeviceMac.setText(MokoSupport.getInstance().mac);
    }

    @Override
    protected ActivityMoreBinding getViewBinding() {
        return ActivityMoreBinding.inflate(getLayoutInflater());
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
