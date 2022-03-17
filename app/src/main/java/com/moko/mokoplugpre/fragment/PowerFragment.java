package com.moko.mokoplugpre.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.activity.DeviceInfoActivity;
import com.moko.mokoplugpre.utils.ToastUtils;
import com.moko.mokoplugpre.view.ArcProgress;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PowerFragment extends Fragment {

    private static final String TAG = PowerFragment.class.getSimpleName();
    @BindView(R.id.arc_progress)
    ArcProgress arcProgress;
    @BindView(R.id.tv_power)
    TextView tvPower;
    @BindView(R.id.tv_onoff)
    TextView tvOnoff;
    @BindView(R.id.cv_onoff)
    CardView cvOnoff;
    @BindView(R.id.tv_overload)
    TextView tvOverload;
    private boolean switchState = false;
    private DeviceInfoActivity activity;

    public PowerFragment() {
    }

    public static PowerFragment newInstance() {
        PowerFragment fragment = new PowerFragment();
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
                    case MokoSupport.NOTIFY_FUNCTION_SWITCH:
                        int onoff = MokoSupport.getInstance().switchState;
                        setOnOff(onoff);
                        break;
                    case MokoSupport.NOTIFY_FUNCTION_OVERLOAD:
                        setOverLoad();
                        break;
                    case MokoSupport.NOTIFY_FUNCTION_LOAD:
                        ToastUtils.showToast(getActivity(), "load insertion");
                        break;
                    case MokoSupport.NOTIFY_FUNCTION_ELECTRICITY:
                        String electricityP = MokoSupport.getInstance().electricityP;
                        float progress = Math.abs(Float.parseFloat(electricityP)) * 0.1f;
                        arcProgress.setProgress(progress);
                        tvPower.setText(electricityP);
                        break;
                }
            }
        });
    }

    private void setOnOff(int onoff) {
        if (onoff == 0) {
            switchState = false;
            cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
            tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue_2681ff));
            tvOnoff.setText("OFF");
        } else {
            switchState = true;
            cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blue_2681ff));
            tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
            tvOnoff.setText("ON");
        }
    }

    private void setOverLoad() {
        cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey_d9d9d9));
        tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
        tvOnoff.setText("OFF");
        cvOnoff.setEnabled(false);
        tvOverload.setVisibility(View.VISIBLE);
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
        View view = inflater.inflate(R.layout.fragment_power, container, false);
        ButterKnife.bind(this, view);
        int onoff = MokoSupport.getInstance().switchState;
        int overloadState = MokoSupport.getInstance().overloadState;
        if (overloadState == 0) {
            setOnOff(onoff);
        } else {
            setOverLoad();
        }
        String electricityP = MokoSupport.getInstance().electricityP;
        if (!TextUtils.isEmpty(electricityP)) {
            float progress = Math.abs(Float.parseFloat(electricityP)) * 0.1f;
            arcProgress.setProgress(progress);
            tvPower.setText(electricityP);
        }
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

//    int electricityP = 0;

    @OnClick({R.id.arc_progress, R.id.cv_onoff})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.arc_progress:
                // TODO: 2020/4/24 测试
//                electricityP += 8;
//                float progress = electricityP * 0.1f;
//                arcProgress.setProgress(progress);
//                tvPower.setText(String.valueOf(electricityP));
                break;
            case R.id.cv_onoff:
                switchState = !switchState;
                activity.changeSwitchState(switchState);
                break;
        }
    }

//    public void changePowerState() {
//        MokoSupport.getInstance().switchState = switchState ? 1 : 0;
//        int onoff = switchState ? 1 : 0;
//        setOnOff(onoff);
//    }
}
