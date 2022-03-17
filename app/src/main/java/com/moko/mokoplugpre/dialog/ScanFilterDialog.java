package com.moko.mokoplugpre.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.mokoplugpre.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2020/4/23
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.mokoplugpre.dialog.ScanFilterDialog
 */
public class ScanFilterDialog extends BaseDialog {
    public static final String TAG = ScanFilterDialog.class.getSimpleName();

    @BindView(R.id.et_filter_name)
    EditText etFilterName;
    @BindView(R.id.tv_rssi)
    TextView tvRssi;
    @BindView(R.id.sb_rssi)
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

    @OnClick({R.id.iv_filter_delete, R.id.tv_done})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_filter_delete:
                etFilterName.setText("");
                break;
            case R.id.tv_done:
                listener.onDone(etFilterName.getText().toString(), filterRssi);
                dismiss();
                break;
        }
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
