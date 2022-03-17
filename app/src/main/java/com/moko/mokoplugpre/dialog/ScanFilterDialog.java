package com.moko.mokoplugpre.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanFilterDialog extends BaseDialog {
    public static final String TAG = ScanFilterDialog.class.getSimpleName();

    @BindView(R2.id.et_filter_name)
    EditText etFilterName;
    @BindView(R2.id.tv_rssi)
    TextView tvRssi;
    @BindView(R2.id.sb_rssi)
    SeekBar sbRssi;

    private int filterRssi;
    private String filterName;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_scan_filter;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = progress - 100;
                tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRssi.setProgress(filterRssi + 100);
        if (!TextUtils.isEmpty(filterName)) {
            etFilterName.setText(filterName);
            etFilterName.setSelection(filterName.length());
        }
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

    @OnClick(R2.id.iv_filter_delete)
    public void onFilterDelete(View view) {
        etFilterName.setText("");
    }

    @OnClick(R2.id.tv_done)
    public void onDone(View view) {
        listener.onDone(etFilterName.getText().toString(), filterRssi);
        dismiss();
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
