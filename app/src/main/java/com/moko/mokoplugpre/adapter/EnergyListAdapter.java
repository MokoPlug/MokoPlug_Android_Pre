package com.moko.mokoplugpre.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mokoplugpre.R;
import com.moko.support.pre.entity.EnergyInfo;

public class EnergyListAdapter extends BaseQuickAdapter<EnergyInfo, BaseViewHolder> {
    public EnergyListAdapter() {
        super(R.layout.item_energy);
    }

    @Override
    protected void convert(BaseViewHolder helper, EnergyInfo item) {
        if (item.type == 0) {
            helper.setText(R.id.tv_date, item.hour);
        } else {
            helper.setText(R.id.tv_date, item.date);
        }
        helper.setText(R.id.tv_value, item.value);
    }
}
