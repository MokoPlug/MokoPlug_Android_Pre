package com.moko.mokoplugpre.dialog;

import android.view.View;
import android.widget.TextView;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;
import com.moko.mokoplugpre.view.WheelView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerDialog extends BaseDialog {
    public static final String TAG = TimerDialog.class.getSimpleName();

    @BindView(R2.id.tv_switch_state)
    TextView tvSwitchState;
    @BindView(R2.id.wv_hour)
    WheelView wvHour;
    @BindView(R2.id.wv_minute)
    WheelView wvMinute;
    private boolean on_off;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_timer;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        tvSwitchState.setText(on_off ? R.string.countdown_timer_off : R.string.countdown_timer_on);
        initWheelView();
    }
    private void initWheelView() {
        ArrayList<String> hour = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i > 1) {
                hour.add(i + " hours");
            } else {
                hour.add(i + " hour");
            }
        }
        wvHour.setData(hour);
        wvHour.setDefault(0);
        ArrayList<String> minute = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i > 1) {
                minute.add(i + " mins");
            } else {
                minute.add(i + " min");

            }
        }
        wvMinute.setData(minute);
        wvMinute.setDefault(0);
    }

    public int getWvHour() {
        return wvHour.getSelected();
    }

    public int getWvMinute() {
        return wvMinute.getSelected();
    }


    @OnClick(R2.id.tv_back)
    public void onBack(View view) {
        dismiss();
    }

    @OnClick(R2.id.tv_confirm)
    public void onConfirm(View view) {
        listener.onConfirmClick(this);
    }

    private TimerListener listener;

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public interface TimerListener {
        void onConfirmClick(TimerDialog dialog);
    }

    public void setOnoff(boolean on_off) {
        this.on_off = on_off;
    }



    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

}
