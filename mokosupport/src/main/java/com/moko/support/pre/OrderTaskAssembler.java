package com.moko.support.pre;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pre.entity.ParamsReadKeyEnum;
import com.moko.support.pre.task.ParamsReadTask;
import com.moko.support.pre.task.ParamsWriteTask;

public class OrderTaskAssembler {
    public static OrderTask readAdvInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ADV_INTERVAL);
        return task;
    }

    public static OrderTask readAdvName() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ADV_NAME);
        return task;
    }

    public static OrderTask readCountdown() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_COUNTDOWN);
        return task;
    }

    public static OrderTask readElectricity() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ELECTRICITY_VALUE);
        return task;
    }

    public static OrderTask readEnergyHistory() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ENERGY_HISTORY);
        return task;
    }

    public static OrderTask readEnergyHistoryToday() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ENERGY_HISTORY_TODAY);
        return task;
    }

    public static OrderTask readEnergySavedParams() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ENERGY_SAVED_PARAMS);
        return task;
    }

    public static OrderTask readEnergyTotal() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ENERGY_TOTAL);
        return task;
    }

    public static OrderTask readFirmwareVersion() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_FIRMWARE_VERISON);
        return task;
    }

    public static OrderTask readLoadState() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_LOAD_STATE);
        return task;
    }

    public static OrderTask readMac() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_MAC);
        return task;
    }

    public static OrderTask readOverloadTopValue() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_OVERLOAD_TOP_VALUE);
        return task;
    }

    public static OrderTask readOverloadValue() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_OVERLOAD_VALUE);
        return task;
    }

    public static OrderTask readPowerState() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_POWER_STATE);
        return task;
    }

    public static OrderTask readSwitchState() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_SWITCH_STATE);
        return task;
    }

    public static OrderTask readElectricityConstant() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsReadKeyEnum.GET_ELECTRICITY_CONSTANT);
        return task;
    }

    public static OrderTask writeAdvInterval(int advInterval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvInterval(advInterval);
        return task;
    }

    public static OrderTask writeAdvName(String advName) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvName(advName);
        return task;
    }

    public static OrderTask writeCountdown(int countdown) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setCountdown(countdown);
        return task;
    }

    public static OrderTask writeEnergySavedParams(int savedInterval, int changed) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSavedParams(savedInterval, changed);
        return task;
    }

    public static OrderTask writeOverloadTopValue(int topValue) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setOverloadTopValue(topValue);
        return task;
    }

    public static OrderTask writePowerState(int powerState) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setPowerState(powerState);
        return task;
    }

    public static OrderTask writeResetEnergyTotal() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setResetEnergyTotal();
        return task;
    }

    public static OrderTask writeReset() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setReset();
        return task;
    }

    public static OrderTask writeSwitchState(int switchState) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSwitchState(switchState);
        return task;
    }

    public static OrderTask writeSystemTime() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSystemTime();
        return task;
    }
}
