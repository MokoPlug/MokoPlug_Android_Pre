package com.moko.support.pre.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.moko.support.pre.entity.OrderCHAR;
import com.moko.support.pre.entity.OrderServices;

import java.util.HashMap;


public class MokoCharacteristicHandler {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    public MokoCharacteristicHandler() {
        //no instance
        mCharacteristicMap = new HashMap<>();
    }

    public HashMap<OrderCHAR, BluetoothGattCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mCharacteristicMap != null && !mCharacteristicMap.isEmpty()) {
            mCharacteristicMap.clear();
        }

        if (gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS_READ.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS_READ.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS_READ, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS_WRITE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS_WRITE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS_WRITE, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS_NOTIFY, characteristic);
            }
        }
        return mCharacteristicMap;
    }
}
