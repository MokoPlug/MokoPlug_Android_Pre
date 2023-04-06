package com.moko.mokoplugpre.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.databinding.DialogScanFilterBinding;

public class ScanFilterDialog extends MokoBaseDialog<DialogScanFilterBinding> {
    public static final String TAG = ScanFilterDialog.class.getSimpleName();


    private int filterRssi;
    private String filterName;

    @Override
    protected DialogScanFilterBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogScanFilterBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        mBind.tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        mBind.sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = progress - 100;
                mBind.tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBind.sbRssi.setProgress(filterRssi + 100);
        if (!TextUtils.isEmpty(filterName)) {
            mBind.etFilterName.setText(filterName);
            mBind.etFilterName.setSelection(filterName.length());
        }
        mBind.ivFilterDelete.setOnClickListener(v -> {
            mBind.etFilterName.setText("");
        });
        mBind.tvDone.setOnClickListener(v -> {
            listener.onDone(mBind.etFilterName.getText().toString(), filterRssi);
            dismiss();
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onDismiss();
        }
    }

    @Override
    public int getDialogStyle() {
        return R.style.TopDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.TOP;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public interface OnScanFilterListener {
        void onDone(String filterName, int filterRssi);

        void onDismiss();
    }
}
