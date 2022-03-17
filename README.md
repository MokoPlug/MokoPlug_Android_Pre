# BluetoothPlug Android SDK Guide（English）

## Intro

Please read the part of this document which you need.

* We will explain the important classes in the SDK.

* will help developers to get started.

* will explain notes in your developing progress.


## Design instructions

We divide the communications between SDK and devices into three stages: Scanning stage, Connection stage, Communication stage. For ease of understanding, let's take a look at the related classes and the relationships between them.

### 1.Scanning stage

**`com.moko.support.MokoBleScanner`**

Scanning processing class, support to open scan, close scan and get the raw data of the scanned device.

**`com.moko.support.callback.MokoScanDeviceCallback`**

Scanning callback interface,this interface can be used to obtain the scan status and device data.

**`com.moko.support.service.DeviceInfoParseable`**

Parsed data interface,this interface can parsed the device broadcast frame, get the specific data. the implementation can refer to `PlugInfoParseableImpl` in the project,the `DeviceInfo` will be parsed to `PlugInfo`.

### 2.Connection stage

**`com.moko.support.MokoSupport`**

BLE operation core class, extends from `Mokoblelib`.It can connect the device, disconnect the device, send the device connection status, turn on Bluetooth, turn off Bluetooth, judge whether Bluetooth is on or not, receive data from the device and send data to the device, notify the page data update, turn on and off characteristic notification.

### 3.Communication stage

**`com.moko.support.OrderTaskAssembler`**

We assemble read data and write data to `OrderTask`, send the task to the device through `MokoSupport`, and receive the resopnse.

**`com.moko.ble.lib.event.ConnectStatusEvent`**

The connection status is notified by `EventBus`, the device connection status and disconnection status are obtained from this event.

**`com.moko.ble.lib.event.OrderTaskResponseEvent`**

The response is notified by `EventBus`, we can get result when we send task to device from this event,distinguish between function via `OrderTaskResponse`.

## Get Started

### Prepare

**Development environment:**

* Android Studio 3.6.+

* minSdkVersion 18

**Import to Project**

Copy the module mokosupport into the project root directory and add dependencies in build.gradle. As shown below:

```
dependencies {
    ...
    implementation project(path: ':mokosupport')
}
```

add mokosupport in settings.gradle.As shown below:

```
include ':app', ':mokosupport'
```

### Start Developing

**Initialize**

First of all, you should initialize the MokoSupport.We recommend putting it in Application.

```
MokoSupport.getInstance().init(getApplicationContext());
```

**Scan devices**

Before operating the Bluetooth scanning device, we need to apply for permission, which we have added in mokosupport `AndroidManifest.xml`

```
...
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
...
```

Start scanning task to find devices around you, then you can get their advertisement content, connect to device and change parameters.

```
MokoBleScanner mokoBleScanner = new MokoBleScanner(this);
mokoBleScanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override
    public void onStartScan() {
    }

    @Override
    public void onScanDevice(DeviceInfo device) {
    }

    @Override
    public void onStopScan() {
    }
});
```

at the sometime, you can stop the scanning task in this way:

```
mokoBleScanner.stopScanDevice();
```

You can use DeviceInfoParseable to parsed advertisement data to the shown data, such as electricity voltage,power,overloadState and etc...

```
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
```

**Connect to devices**

Connect to the device in order to do more operations(change parameter, OTA),the only parameter required is the MAC address.

```
MokoSupport.getInstance().connDevice(plugInfo.mac);
```

You can get the connection status through `ConnectStatusEvent`,remember to register `EventBus`

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onConnectStatusEvent(ConnectStatusEvent event) {
    String action = event.getAction();
    if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
    // connect failed
    ...
    }
    if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
    // connect success
    ...
    }
}
```

You can write params to device and read it after the device connected.

```
ArrayList<OrderTask> orderTasks = new ArrayList<>();
orderTasks.add(OrderTaskAssembler.writeSystemTime());
orderTasks.add(OrderTaskAssembler.readAdvInterval());
orderTasks.add(OrderTaskAssembler.readAdvName());
orderTasks.add(OrderTaskAssembler.readCountdown());
orderTasks.add(OrderTaskAssembler.readElectricity());
orderTasks.add(OrderTaskAssembler.readElectricityConstant());
orderTasks.add(OrderTaskAssembler.readEnergyHistory());
orderTasks.add(OrderTaskAssembler.readEnergyHistoryToday());
orderTasks.add(OrderTaskAssembler.readEnergySavedParams());
orderTasks.add(OrderTaskAssembler.readEnergyTotal());
orderTasks.add(OrderTaskAssembler.readFirmwareVersion());
orderTasks.add(OrderTaskAssembler.readLoadState());
orderTasks.add(OrderTaskAssembler.readMac());
orderTasks.add(OrderTaskAssembler.readOverloadTopValue());
orderTasks.add(OrderTaskAssembler.readOverloadValue());
orderTasks.add(OrderTaskAssembler.readPowerState());
orderTasks.add(OrderTaskAssembler.readSwitchState());
MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

```

You can get the response result from device through `OrderTaskResponseEvent`,

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
    // the task timout
    }
    if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
    // finish all task
    }
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
    // get the task result
        OrderTaskResponse response = event.getResponse();
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        int responseType = response.responseType;
        byte[] value = response.responseValue;
        ...
    }
    if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    // notify data
    }
}
```

> `ACTION_ORDER_RESULT`
>
> After the task is sent to the device, the data returned by the device can be obtained by using the `OrderTaskResponse`, and you can determine which task is being returned as a resultis according to the `response.orderCHAR`. The `response.responseValue` is the returned data.

> `ACTION_ORDER_TIMEOUT`
>
> Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. You can determine which task is being returned as a resultis according to the `response.orderCHAR` function and then the next task continues.

> `ACTION_ORDER_FINISH`
>
> When the task in the queue is empty, `onOrderFinish` will be called back.

> `ACTION_CURRENT_DATA`
>
> The data from device notify.

**Communication with the device**

All the read data and write data is encapsulated into `OrderTask` in `OrderTaskAssembler`, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback `OrderTaskResponse` after sending tasks successfully.

For example, if you want to get the switch state, please refer to the code example below.

```
// read switch state
MokoSupport.getInstance().sendOrder(derTaskAssembler.readSwitchState());
...
// get result
int onoff = MokoSupport.getInstance().switchState;
```
How to parse the returned results, please refer to the code of the sample project and documentation.

The current data of electricity, countdown and overload are sent to APP by notification. you can get the value from `ACTION_CURRENT_DATA `

```
@Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    activity.runOnUiThread(() -> {
        if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            int responseType = response.responseType;
            byte[] value = response.responseValue;
            switch (responseType) {
                case MokoSupport.NOTIFY_FUNCTION_SWITCH:
                    int onoff = MokoSupport.getInstance().switchState;
                    break;
                case MokoSupport.NOTIFY_FUNCTION_OVERLOAD:
                    break;
                case MokoSupport.NOTIFY_FUNCTION_LOAD:
                    ToastUtils.showToast(getActivity(), "load insertion");
                    break;
                case MokoSupport.NOTIFY_FUNCTION_ELECTRICITY:
                    String electricityP = MokoSupport.getInstance().electricityP;
                    break;
            }
        }
    });
}
```

**OTA**

We used the Nordic DFU for the OTA,dependencies have been added to build.gradle.

```
dependencies {
    api 'no.nordicsemi.android:dfu:0.6.2'
}
```

The OTA requires three important parameters:the path of firmware file,the adv name of device and the mac address of device.You can use it like this:

```
DfuServiceInitiator starter = new DfuServiceInitiator(deviceMac)
    .setDeviceName(deviceName)
    .setKeepBond(false)
    .setDisableNotification(true);
starter.setZip(null, firmwareFilePath);
starter.start(this, DfuService.class);
```
you can get progress of OTA through `DfuProgressListener`,the examples can be referred to demo project.

At the end of this part, you can refer all code above to develop. If there is something new, we will update this document.

## Notes

1.In Android-6.0 or later, Bluetooth scanning requires dynamic application for location permissions, as follows:

```
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
!= PackageManager.PERMISSION_GRANTED) {
ActivityCompat.requestPermissions(this,
                                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
} 
```

2.`EventBus` is used in the SDK and can be modified in `MokoSupport` if you want to use other communication methods.

```
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
public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
    ...
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_CURRENT_DATA);
    event.setResponse(response);
    EventBus.getDefault().post(event);
    ...
}
```
3.In order to record log files, `XLog` is used in the SDK, and the permission `WRITE_EXTERNAL_STORAGE` is applied. If you do not want to use it, you can modify it in `BaseApplication`, and only keep `XLog.init(config)`.


## Change log

* 2021.03.25 mokosupport version:1.0
	* Change the SDK structure
	* Support Android API 29
	* Support androidx
	* Optimize document content
