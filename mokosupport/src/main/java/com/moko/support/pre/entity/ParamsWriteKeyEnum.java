package com.moko.support.pre.entity;

import java.io.Serializable;


public enum ParamsWriteKeyEnum implements Serializable {

    SET_ADV_NAME(0x01),
    SET_ADV_INTERVAL(0x02),
    SET_SWITCH_STATE(0x03),
    SET_POWER_STATE(0x04),
    SET_OVERLOAD_TOP_VALUE(0x05),
    SET_RESET_ENERGY_TOTAL(0x06),
    SET_COUNTDOWN(0x07),
    SET_RESET(0x08),
    SET_ENERGY_SAVED_PARAMS(0x09),
    SET_SYSTEM_TIME(0x0A),
    SET_REPORT_INTERVAL(0x0B),
    ;


    private int paramsKey;

    ParamsWriteKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsWriteKeyEnum fromParamKey(int paramsKey) {
        for (ParamsWriteKeyEnum paramsKeyEnum : ParamsWriteKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
