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
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.activity.DeviceInfoActivity;
import com.moko.mokoplugpre.dialog.TimerDialog;
import com.moko.mokoplugpre.view.CircularProgress;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerFragment extends Fragment {

    private static final String TAG = TimerFragment.class.getSimpleName();
    @BindView(R.id.circular_progress)
    CircularProgress circularProgress;
    @BindView(R.id.tv_countdown_tips)
    TextView tvCountdownTips;
    @BindView(R.id.tv_timer)
    TextView tvTimer;

    private DeviceInfoActivity activity;

    public TimerFragment() {
    }

    public static TimerFragment newInstance() {
        TimerFragment fragment = new TimerFragment();
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
                    case MokoSupport.NOTIFY_FUNCTION_COUNTDOWN:
                        int countdown = MokoSupport.getInstance().countDown;
                        if (countdown > 0) {
                            int onoff = MokoSupport.getInstance().switchState;
                            tvCountdownTips.setVisibility(View.VISIBLE);
                            tvCountdownTips.setText(getString(R.string.countdown_tips, onoff == 1 ? "OFF" : "ON"));
                            int hour = countdown / 3600;
                            int minute = (countdown % 3600) / 60;
                            int second = (countdown % 3600) % 60;
                            tvTimer.setText(String.format("%02d:%02d:%02d", hour, minute, second));
                            int countDownInit = MokoSupport.getInstance().countDownInit;
                            int progress = Math.round(maxProgress - maxProgress / countDownInit * countdown);
                            circularProgress.setProgress(progress);
                        } else {
                            circularProgress.setProgress(36);
                            tvCountdownTips.setVisibility(View.GONE);
                            tvTimer.setText("00:00:00");
                        }
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
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        ButterKnife.bind(this, view);
        int countdown = MokoSupport.getInstance().countDown;
        if (countdown > 0) {
            int onoff = MokoSupport.getInstance().switchState;
            tvCountdownTips.setVisibility(View.VISIBLE);
            tvCountdownTips.setText(getString(R.string.countdown_tips, onoff == 1 ? "OFF" : "ON"));
            int hour = countdown / 3600;
            int minute = (countdown % 3600) / 60;
            int second = (countdown % 3600) % 60;
            tvTimer.setText(String.format("%02d:%02d:%02d", hour, minute, second));
            int countDownInit = MokoSupport.getInstance().countDownInit;
            int progress = Math.round(maxProgress - maxProgress / countDownInit * countdown);
            circularProgress.setProgress(progress);
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

    //    int countdown = 0;
//    int count = 0;
    final float maxProgress = 36.0f;

    @OnClick({R.id.circular_progress, R.id.cv_timer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.circular_progress:
                // TODO: 2020/4/24 测试
//                countdown += 1;
//                circularProgress.setProgress(countdown);
                break;
            case R.id.cv_timer:
                // 设置倒计时
//                countdown = 300;
//                count = countdown;
//                countdown();
                int onoff = MokoSupport.getInstance().switchState;
                TimerDialog timerDialog = new TimerDialog();
                timerDialog.setOnoff(onoff == 1);
                timerDialog.setListener(new TimerDialog.TimerListener() {
                    @Override
                    public void onConfirmClick(TimerDialog dialog) {
                        int hour = dialog.getWvHour();
                        int minute = dialog.getWvMinute();
                        int countdown = hour * 3600 + minute * 60;
                        activity.setTimer(countdown);
                        dialog.dismiss();
                    }
                });
                timerDialog.show(activity.getSupportFragmentManager());
                break;
        }
    }

//    private void countdown() {
//        tvTimer.setText(String.valueOf(countdown));
//        int progress = Math.round(maxProgress - maxProgress / count * countdown);
//        circularProgress.setProgress(progress);
//        if (countdown <= 0)
//            return;
//        tvTimer.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                countdown--;
//                countdown();
//            }
//        }, 1000);
//    }
}
