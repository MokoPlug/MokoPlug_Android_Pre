package com.moko.support.pre.event;

public class DataChangedEvent {
    private int function;
    private byte[] value;

    public int getFunction() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
