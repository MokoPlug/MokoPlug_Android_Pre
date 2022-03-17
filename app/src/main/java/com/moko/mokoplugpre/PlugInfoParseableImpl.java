package com.moko.mokoplugpre;

import android.text.TextUtils;
import android.util.SparseArray;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mokoplugpre.entity.PlugInfo;
import com.moko.support.pre.entity.DeviceInfo;
import com.moko.support.pre.service.DeviceInfoParseable;

import java.util.Arrays;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class PlugInfoParseableImpl implements DeviceInfoParseable<PlugInfo> {
    @Override
    public PlugInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult scanResult = deviceInfo.scanResult;
        SparseArray<byte[]> manufacturer = scanResult.getScanRecord().getManufacturerSpecificData();
        if (manufacturer == null || manufacturer.size() == 0) {
            return null;
        }
        int manufacturerId = manufacturer.keyAt(0);
        // 20ff
        if (!"20ff".equalsIgnoreCase(String.format("%04X", manufacturerId)))
            return null;
        byte[] manufacturerData = manufacturer.get(manufacturerId);
        String electricityV = "";
        String electricityC = "";
        String electricityP = "";
        String binary = "";
        if (manufacturerData.length == 13) {
            byte[] electricityVBytes = Arrays.copyOfRange(manufacturerData, 2, 4);
            byte[] electricityCBytes = Arrays.copyOfRange(manufacturerData, 4, 7);
            byte[] electricityPBytes = Arrays.copyOfRange(manufacturerData, 7, 9);
            binary = MokoUtils.hexString2binaryString(MokoUtils.byte2HexString(manufacturerData[12]));
            electricityV = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toInt(electricityVBytes) * 0.1f);
            electricityC = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(electricityCBytes) * 0.001f);
            electricityP = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toInt(electricityPBytes) * 0.1f);
        }
        if (manufacturerData.length == 16) {
            byte[] electricityVBytes = Arrays.copyOfRange(manufacturerData, 2, 4);
            byte[] electricityCBytes = Arrays.copyOfRange(manufacturerData, 4, 8);
            byte[] electricityPBytes = Arrays.copyOfRange(manufacturerData, 8, 12);
            binary = MokoUtils.hexString2binaryString(MokoUtils.byte2HexString(manufacturerData[15]));
            electricityV = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toInt(electricityVBytes) * 0.1f);
            electricityC = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toIntSigned(electricityCBytes) * 0.001f);
            electricityP = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toIntSigned(electricityPBytes) * 0.1f);
        }
        if (TextUtils.isEmpty(electricityV)
                || TextUtils.isEmpty(electricityC)
                || TextUtils.isEmpty(electricityP)
                || TextUtils.isEmpty(binary))
            return null;
        PlugInfo plugInfo = new PlugInfo();
        plugInfo.name = deviceInfo.name;
        plugInfo.mac = deviceInfo.mac;
        plugInfo.rssi = deviceInfo.rssi;
        plugInfo.electricityV = electricityV;
        plugInfo.electricityC = electricityC;
        plugInfo.electricityP = electricityP;
        if (TextUtils.isEmpty(binary)) {
            plugInfo.overloadState = 0;
            plugInfo.onoff = 0;
            return plugInfo;
        }
        plugInfo.overloadState = Integer.parseInt(binary.substring(1, 2));
        plugInfo.onoff = Integer.parseInt(binary.substring(2, 3));
        return plugInfo;
    }
}
