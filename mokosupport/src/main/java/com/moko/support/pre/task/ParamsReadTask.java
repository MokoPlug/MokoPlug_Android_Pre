package com.moko.support.pre.task;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.pre.MokoSupport;
import com.moko.support.pre.entity.EnergyInfo;
import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.entity.ParamsReadKeyEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class ParamsReadTask extends OrderTask {
    public static final int HEADER_READ_SEND = 0xB0;
    public static final int HEADER_READ_GET = 0xB1;
    public byte[] data;

    public ParamsReadTask() {
        super(OrderCHAR.CHAR_PARAMS_READ, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsReadKeyEnum key) {
        switch (key) {
            case GET_ADV_NAME:
            case GET_ADV_INTERVAL:
            case GET_SWITCH_STATE:
            case GET_POWER_STATE:
            case GET_LOAD_STATE:
            case GET_OVERLOAD_TOP_VALUE:
            case GET_ELECTRICITY_VALUE:
            case GET_ENERGY_TOTAL:
            case GET_COUNTDOWN:
            case GET_FIRMWARE_VERISON:
            case GET_MAC:
            case GET_ENERGY_SAVED_PARAMS:
            case GET_ENERGY_HISTORY:
            case GET_OVERLOAD_VALUE:
            case GET_ENERGY_HISTORY_TODAY:
            case GET_ELECTRICITY_CONSTANT:
                createGetConfigData(key.getParamsKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) HEADER_READ_SEND, (byte) configKey, (byte) 0x00};
    }

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        final int cmd = value[1] & 0xFF;
        final int length = value[2] & 0xFF;
        if (header != HEADER_READ_GET)
            return false;
        ParamsReadKeyEnum configKeyEnum = ParamsReadKeyEnum.fromParamKey(cmd);
        if (configKeyEnum == null)
            return false;
        switch (configKeyEnum) {
            case GET_ADV_NAME:
                byte[] advNameBytes = Arrays.copyOfRange(value, 3, value.length);
                if (length != advNameBytes.length)
                    return false;
                MokoSupport.getInstance().advName = new String(advNameBytes);
                break;
            case GET_ADV_INTERVAL:
                if (length != 1)
                    return false;
                int advInterval = value[3] & 0xFF;
                MokoSupport.getInstance().advInterval = advInterval;
                break;
            case GET_SWITCH_STATE:
                if (length != 1)
                    return false;
                int switchState = value[3] & 0xFF;
                MokoSupport.getInstance().switchState = switchState;
                break;
            case GET_POWER_STATE:
                if (length != 1)
                    return false;
                int powerState = value[3] & 0xFF;
                MokoSupport.getInstance().powerState = powerState;
                break;
            case GET_LOAD_STATE:
                if (length != 1)
                    return false;
                int overLoadState = value[3] & 0xFF;
//                MokoSupport.getInstance().overloadState = overLoadState;
                break;
            case GET_OVERLOAD_TOP_VALUE:
                if (length != 2)
                    return false;
                byte[] overloadTopBytes = Arrays.copyOfRange(value, 3, 5);
                int overloadTop = MokoUtils.toInt(overloadTopBytes);
                MokoSupport.getInstance().overloadTopValue = overloadTop;
                break;
            case GET_ELECTRICITY_VALUE:
                if (length == 7) {
                    byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                    final int v = MokoUtils.toInt(vBytes);
                    MokoSupport.getInstance().electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                    byte[] cBytes = Arrays.copyOfRange(value, 5, 8);
                    final int c = MokoUtils.toInt(cBytes);
                    MokoSupport.getInstance().electricityC = String.valueOf(c);

                    byte[] pBytes = Arrays.copyOfRange(value, 8, 10);
                    final int p = MokoUtils.toInt(pBytes);
                    MokoSupport.getInstance().electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                } else if (length == 10) {
                    byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                    final int v = MokoUtils.toInt(vBytes);
                    MokoSupport.getInstance().electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                    byte[] cBytes = Arrays.copyOfRange(value, 5, 9);
                    final int c = MokoUtils.toIntSigned(cBytes);
                    MokoSupport.getInstance().electricityC = String.valueOf(c);

                    byte[] pBytes = Arrays.copyOfRange(value, 9, 13);
                    final int p = MokoUtils.toIntSigned(pBytes);
                    MokoSupport.getInstance().electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                } else {
                    return false;
                }
                break;
            case GET_ENERGY_TOTAL:
                if (length != 4)
                    return false;
                byte[] energyTotalBytes = Arrays.copyOfRange(value, 3, 7);
                long energyTotal = MokoUtils.longFrom8Bytes(energyTotalBytes);
                MokoSupport.getInstance().eneryTotal = energyTotal;
                break;
            case GET_COUNTDOWN:
                if (length != 9)
                    return false;
                if (0x01 == (value[3] & 0xFF)) {
                    byte[] countDownInitBytes = Arrays.copyOfRange(value, 4, 8);
                    int countDownInit = MokoUtils.toInt(countDownInitBytes);
                    MokoSupport.getInstance().countDownInit = countDownInit;
                    byte[] countDownBytes = Arrays.copyOfRange(value, 8, 12);
                    int countDown = MokoUtils.toInt(countDownBytes);
                    MokoSupport.getInstance().countDown = countDown;
                }
                break;
            case GET_FIRMWARE_VERISON:
                byte[] firmwareVerisonBytes = Arrays.copyOfRange(value, 3, value.length);
                if (firmwareVerisonBytes.length != length)
                    return false;
                String version = new String(firmwareVerisonBytes);
                MokoSupport.getInstance().firmwareVersion = version;
                break;
            case GET_MAC:
                if (length != 6)
                    return false;
                String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", value[3], value[4], value[5], value[6], value[7], value[8]);
                MokoSupport.getInstance().mac = mac;
                break;
            case GET_ENERGY_SAVED_PARAMS:
                if (length != 2)
                    return false;
                int savedInterval = value[3] & 0xFF;
                MokoSupport.getInstance().energySavedInterval = savedInterval;
                int energyChangedPercent = value[4] & 0xFF;
                MokoSupport.getInstance().energySavedPercent = energyChangedPercent;
                break;
            case GET_ENERGY_HISTORY:
                if (length == 1) {
                    // 没有历史数据
                    XLog.i("no history data");
                } else if (length == 7) {
                    total = value[3] & 0xFF;
                    energyInfos = new ArrayList<>();
                    int year = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                    int month = value[6] & 0xFF;
                    int day = value[7] & 0xFF;
                    int hour = value[8] & 0xFF;
                    int minute = value[9] & 0xFF;
                    calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    return false;
                } else {
                    return false;
                }
                break;
            case GET_ENERGY_HISTORY_DATA:
                int count = (value[2] & 0xFF) / 4;
                for (int i = 0; i < count; i++) {
                    EnergyInfo energyInfo = new EnergyInfo();
                    total--;
                    int day = value[3 + 4 * i];
                    byte[] energyBytes = Arrays.copyOfRange(value, 4 + 4 * i, 7 + 4 * i);
                    int energy = MokoUtils.toInt(energyBytes);
                    Calendar c = (Calendar) calendar.clone();
                    c.add(Calendar.DAY_OF_MONTH, day);
                    energyInfo.recordDate = MokoUtils.calendar2strDate(c, "yyyy-MM-dd HH");
                    energyInfo.type = 1;
                    energyInfo.date = energyInfo.recordDate.substring(5, 10);
                    energyInfo.value = String.valueOf(energy);
                    energyInfo.energy = energy;
                    eneryTotalMonth += energy;
                    energyInfos.add(energyInfo);
                }
                if (total > 0)
                    return false;
                Collections.reverse(energyInfos);
                MokoSupport.getInstance().energyHistory = energyInfos;
                MokoSupport.getInstance().eneryTotalMonthly = eneryTotalMonth;
                break;
            case GET_OVERLOAD_VALUE:
                if (length != 3)
                    return false;
                if (0x01 == (value[3] & 0xFF)) {
                    MokoSupport.getInstance().overloadState = 1;
                    byte[] overloadBytes = Arrays.copyOfRange(value, 4, 6);
                    MokoSupport.getInstance().overloadValue = MokoUtils.toInt(overloadBytes);
                } else {
                    MokoSupport.getInstance().overloadState = 0;
                }
                break;
            case GET_ENERGY_HISTORY_TODAY:
                if (length == 1) {
                    // 没有历史数据
                    XLog.i("no history data");
                } else if (length == 4) {
                    total = value[3] & 0xFF;
                    byte[] totalTodayBytes = Arrays.copyOfRange(value, 4, 7);
                    final int totalToday = MokoUtils.toInt(totalTodayBytes);
                    MokoSupport.getInstance().eneryTotalToday = totalToday;
                    energyInfos = new ArrayList<>();
                    calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    return false;
                } else {
                    return false;
                }
                break;
            case GET_ENERGY_HISTORY_TODAY_DATA:
                int todayCount = (value[2] & 0xFF) / 3;
                for (int i = 0; i < todayCount; i++) {
                    EnergyInfo energyInfo = new EnergyInfo();
                    total--;
                    int hour = value[3 + 3 * i];
                    byte[] energyBytes = Arrays.copyOfRange(value, 4 + 3 * i, 6 + 3 * i);
                    int energy = MokoUtils.toInt(energyBytes);
                    Calendar c = (Calendar) calendar.clone();
                    c.add(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, 0);
                    energyInfo.recordDate = MokoUtils.calendar2strDate(c, "yyyy-MM-dd HH");
                    energyInfo.type = 0;
                    energyInfo.hour = energyInfo.recordDate.substring(11);
                    energyInfo.value = String.valueOf(energy);
                    energyInfo.energy = energy;
                    energyInfos.add(energyInfo);
                }
                if (total > 0)
                    return false;
                Collections.reverse(energyInfos);
                MokoSupport.getInstance().energyHistoryToday = energyInfos;
                break;
            case GET_ELECTRICITY_CONSTANT:
                if (length != 2)
                    return false;
                byte[] pulseConstantBytes = Arrays.copyOfRange(value, 3, 5);
                int pulseConstant = MokoUtils.toInt(pulseConstantBytes);
                MokoSupport.getInstance().electricityConstant = pulseConstant;
                break;
        }
        return true;
    }

    private int total;
    private List<EnergyInfo> energyInfos;
    private Calendar calendar;

    private int eneryTotalMonth = 0;
}
