package com.moko.support.pre;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleLib;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.pre.entity.EnergyInfo;
import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.handler.MokoCharacteristicHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MokoSupport extends MokoBleLib {
    // 通知接收头
    public static final int HEADER_NOTIFY = 0xB4;
    // 开关状态通知
    public static final int NOTIFY_FUNCTION_SWITCH = 0x01;
    // 负载检测通知
    public static final int NOTIFY_FUNCTION_LOAD = 0x02;
    // 过载保护通知
    public static final int NOTIFY_FUNCTION_OVERLOAD = 0x03;
    // 倒计时通知
    public static final int NOTIFY_FUNCTION_COUNTDOWN = 0x04;
    // 当前电压、电流、功率通知
    public static final int NOTIFY_FUNCTION_ELECTRICITY = 0x05;
    // 当前电能数据通知
    public static final int NOTIFY_FUNCTION_ENERGY = 0x06;

    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoSupport() {
        //no instance
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        super.init(context);
    }

    @Override
    public MokoBleManager getMokoBleManager() {
        MokoBleConfig mokoSupportBleManager = new MokoBleConfig(mContext, this);
        return mokoSupportBleManager;
    }

    ///////////////////////////////////////////////////////////////////////////
    // connect
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        mCharacteristicMap = new MokoCharacteristicHandler().getCharacteristics(gatt);
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(Enum orderCHAR) {
        return mCharacteristicMap.get(orderCHAR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCHARNull() {
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            disConnectBle();
            return true;
        }
        return false;
    }

    @Override
    public void orderFinish() {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_FINISH);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderTimeout(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderResult(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public boolean orderResponseValid(BluetoothGattCharacteristic characteristic, OrderTask orderTask) {
        final UUID responseUUID = characteristic.getUuid();
        final OrderCHAR orderCHAR = (OrderCHAR) orderTask.orderCHAR;
        return responseUUID.equals(orderCHAR.getUuid());
    }

    @Override
    public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
        final UUID responseUUID = characteristic.getUuid();
        OrderCHAR orderCHAR = null;
        if (responseUUID.equals(OrderCHAR.CHAR_PARAMS_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_PARAMS_NOTIFY;
        }
        if (orderCHAR == null)
            return false;
        XLog.i(orderCHAR.name());
        if (HEADER_NOTIFY != (value[0] & 0xFF))
            return true;
        final int function = value[1] & 0xFF;
        final int length = value[2] & 0xFF;
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        OrderTaskResponse response = new OrderTaskResponse();
        response.orderCHAR = orderCHAR;
        response.responseValue = value;
        response.responseType = function;
        switch (function) {
            case NOTIFY_FUNCTION_SWITCH:
                if (length != 1)
                    return true;
                final int switchState = value[3] & 0xFF;
                this.switchState = switchState;
                break;
            case NOTIFY_FUNCTION_LOAD:
                if (length != 1)
                    return true;
                if (1 != (value[3] & 0xFF))
                    return true;
                break;
            case NOTIFY_FUNCTION_OVERLOAD:
                if (length != 2)
                    return true;
                this.overloadState = 1;
                break;
            case NOTIFY_FUNCTION_COUNTDOWN:
                if (length != 9)
                    return true;
                byte[] countDownInitBytes = Arrays.copyOfRange(value, 4, 8);
                final int countDownInit = MokoUtils.toInt(countDownInitBytes);
                this.countDownInit = countDownInit;
                byte[] countDownBytes = Arrays.copyOfRange(value, 8, 12);
                final int countDown = MokoUtils.toInt(countDownBytes);
                this.countDown = countDown;
                break;
            case NOTIFY_FUNCTION_ELECTRICITY:
                if (length == 7) {
                    byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                    final int v = MokoUtils.toInt(vBytes);
                    electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                    byte[] cBytes = Arrays.copyOfRange(value, 5, 8);
                    final int c = MokoUtils.toInt(cBytes);
                    electricityC = String.valueOf(c);

                    byte[] pBytes = Arrays.copyOfRange(value, 8, 10);
                    final int p = MokoUtils.toInt(pBytes);
                    electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                } else if (length == 10) {
                    byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                    final int v = MokoUtils.toInt(vBytes);
                    electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                    byte[] cBytes = Arrays.copyOfRange(value, 5, 9);
                    final int c = MokoUtils.toIntSigned(cBytes);
                    electricityC = String.valueOf(c);

                    byte[] pBytes = Arrays.copyOfRange(value, 9, 13);
                    final int p = MokoUtils.toIntSigned(pBytes);
                    electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                } else {
                    return true;
                }
                break;
            case NOTIFY_FUNCTION_ENERGY:
                if (length != 17)
                    return true;
                EnergyInfo energyInfo = new EnergyInfo();
                int year = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                int month = value[5] & 0xFF;
                int day = value[6] & 0xFF;
                int hour = value[7] & 0xFF;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                byte[] totalBytes = Arrays.copyOfRange(value, 8, 12);
                long total = MokoUtils.longFrom8Bytes(totalBytes);
                this.eneryTotal = total;

                byte[] totalMonthlyBytes = Arrays.copyOfRange(value, 12, 15);
                final int totalMonthly = MokoUtils.toInt(totalMonthlyBytes);
                this.eneryTotalMonthly = totalMonthly;

                byte[] totalTodayBytes = Arrays.copyOfRange(value, 15, 18);
                final int totalToday = MokoUtils.toInt(totalTodayBytes);
                this.eneryTotalToday = totalToday;

                byte[] currentBytes = Arrays.copyOfRange(value, 18, 20);
                final int current = MokoUtils.toInt(currentBytes);
                String energyCurrent = String.valueOf(current);

                energyInfo.recordDate = MokoUtils.calendar2strDate(calendar, "yyyy-MM-dd HH");
                energyInfo.date = energyInfo.recordDate.substring(5, 10);
                energyInfo.hour = energyInfo.recordDate.substring(11);
                energyInfo.value = energyCurrent;
                if (energyHistory != null) {
                    EnergyInfo first = energyHistory.get(0);
                    if (energyInfo.date.equals(first.date)) {
                        first.value = String.valueOf(eneryTotalToday);
                    } else {
                        energyInfo.type = 1;
                        energyInfo.value = String.valueOf(eneryTotalToday);
                        energyHistory.add(0, energyInfo);
                    }
                } else {
                    energyHistory = new ArrayList<>();
                    energyInfo.type = 1;
                    energyHistory.add(energyInfo);
                }
                if (energyHistoryToday != null) {
                    EnergyInfo first = energyHistoryToday.get(0);
                    if (energyInfo.recordDate.equals(first.recordDate)) {
                        first.value = energyCurrent;
                    } else {
                        energyInfo.type = 0;
                        energyHistoryToday.add(0, energyInfo);
                    }
                } else {
                    energyHistoryToday = new ArrayList<>();
                    energyInfo.type = 0;
                    energyHistoryToday.add(energyInfo);
                }
                break;
        }
        event.setAction(MokoConstants.ACTION_CURRENT_DATA);
        event.setResponse(response);
        EventBus.getDefault().post(event);
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    public String advName;

    public int advInterval;
    public int switchState;
    public int powerState;
    public int overloadState;
    public int overloadTopValue;
    public String electricityV;
    public String electricityC;
    public String electricityP;
    public long eneryTotal;
    public int eneryTotalToday;
    public int eneryTotalMonthly;
    public int countDown;
    public int countDownInit;
    public String firmwareVersion;
    public String mac;
    public int energySavedInterval;
    public int energySavedPercent;
    public List<EnergyInfo> energyHistory;
    public List<EnergyInfo> energyHistoryToday;
    public int overloadValue;
    public int electricityConstant;
}
