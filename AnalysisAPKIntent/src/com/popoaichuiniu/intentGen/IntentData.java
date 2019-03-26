package com.popoaichuiniu.intentGen;

import java.util.Objects;

public class IntentData
{
    String dataString;
    String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentData data1 = (IntentData) o;
        return Objects.equals(dataString, data1.dataString) &&
                Objects.equals(type, data1.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataString, type);
    }

    @Override
    public String toString() {
        return dataString+";"+type;
    }

    public IntentData(String dataString, String type) {
        this.dataString = dataString;
        this.type = type;
    }
}