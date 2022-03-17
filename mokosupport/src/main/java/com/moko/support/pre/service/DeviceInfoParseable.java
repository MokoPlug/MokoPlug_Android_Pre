package com.moko.support.pre.service;

import com.moko.support.pre.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
