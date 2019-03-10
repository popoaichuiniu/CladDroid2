package com.popoaichuiniu.jacy;

import soot.SootMethod;

import java.util.Objects;

public class IntentDataTransfer{

    public SootMethod targetSootMethod;
    public int targetParameter;
    public String type;

    public String value;

    public static String TYPE_ACTION="ACTION";
    public static String TYPE_CATEGORY="CATEGORY";
    public static String TYPE_EXTRA="TYPE_EXTRA";
    public static String TYPE_INTENT="TYPE_INTENT";
    public static  int TARGET_Return=-1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentDataTransfer that = (IntentDataTransfer) o;
        return targetParameter == that.targetParameter &&
                Objects.equals(targetSootMethod, that.targetSootMethod) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetSootMethod, targetParameter, type);
    }
}
