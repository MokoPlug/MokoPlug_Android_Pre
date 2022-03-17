package com.moko.mokoplugpre.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.entity.PlugInfo;

public class PlugListAdapter extends BaseQuickAdapter<PlugInfo, BaseViewHolder> {
    public PlugListAdapter() {
        super(R.layout.item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, PlugInfo item) {
        helper.setText(R.id.tv_device_name, item.name);
        if (item.overloadState == 0) {
            if (item.onoff == 0) {
                helper.setText(R.id.tv_device_status, "OFF");
            } else {
                helper.setText(R.id.tv_device_status, String.format("ON,%sW,%sV,%sA", item.electricityP, item.electricityV, item.electricityC));
            }
        } else {
            helper.setText(R.id.tv_device_status, "OVERLOAD");
        }
        helper.setText(R.id.tv_device_rssi, String.format("%ddBm", item.rssi));
    }
}
