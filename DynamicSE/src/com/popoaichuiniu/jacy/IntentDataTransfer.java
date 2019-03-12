package com.popoaichuiniu.jacy;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

public class IntentDataTransfer{

    public SootMethod targetSootMethod;
    public int targetParameter=-2;

    public String type="";

    public String value="";

    public Unit  whereGen;

    public static String TYPE_ACTION="ACTION";
    public static String TYPE_CATEGORY="CATEGORY";
    public static String TYPE_EXTRA="EXTRA";
    public static String TYPE_INTENT="INTENT";
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

    public IntentDataTransfer()
    {

    }
    public IntentDataTransfer(IntentDataTransfer intentDataTransfer)
    {
        this.targetSootMethod=intentDataTransfer.targetSootMethod;
        this.targetParameter=intentDataTransfer.targetParameter;
        this.type=intentDataTransfer.type;
        this.value=intentDataTransfer.value;
        this.whereGen=intentDataTransfer.whereGen;
    }

    @Override
    public String toString() {
        return "IntentDataTransfer{" +
                "targetSootMethod=" + targetSootMethod +
                ", targetParameter=" + targetParameter +
                ", type='" + type + '\'' +
                '}';
    }
}
