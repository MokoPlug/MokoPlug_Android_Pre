package com.moko.mokoplugpre.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.databinding.DialogTimerBinding;

import java.util.ArrayList;

public class TimerDialog extends MokoBaseDialog<DialogTimerBinding> {
    public static final String TAG = TimerDialog.class.getSimpleName();
    private boolean on_off;

    @Override
    protected DialogTimerBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogTimerBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        mBind.tvSwitchState.setText(on_off ? R.string.countdown_timer_off : R.string.countdown_timer_on);
        initWheelView();
        mBind.tvBack.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvConfirm.setOnClickListener(v -> {
            listener.onConfirmClick(this);
        });
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
        mBind.wvHour.setData(hour);
        mBind.wvHour.setDefault(0);
        ArrayList<String> minute = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i > 1) {
                minute.add(i + " mins");
            } else {
                minute.add(i + " min");

            }
        }
        mBind.wvMinute.setData(minute);
        mBind.wvMinute.setDefault(0);
    }

    public int getWvHour() {
        return mBind.wvHour.getSelected();
    }

    public int getWvMinute() {
        return mBind.wvMinute.getSelected();
    }


    private TimerListener listener;

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public interface TimerListener {
        void onConfirmClick(TimerDialog dialog);
    }

    public void setOnOff(boolean on_off) {
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
