package com.popoaichuiniu.jacy;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

public class IntentDataTransfer{//

    public SootMethod targetSootMethod;
    public int targetParameter=-2;
    public String type="";//TYPE_ACTION

    public String id ="";//ID
    public Unit whereGenUnit;
    public SootMethod whereGenSootMethod;


    public static String TYPE_ACTION="ACTION";
    public static String TYPE_CATEGORY="CATEGORY";
    public static String TYPE_EXTRA="EXTRA";

    public static String TYPE_RETURN_BOOLEAN_EXTRA="BOOLEAN_EXTRA";
    public static String TYPE_RETURN_BYTE_EXTRA="BYTE_EXTRA";
    //public static String TYPE_RETURN_CHAR_EXTRA="CHAR_EXTRA";
    public static String TYPE_RETURN_SHORT_EXTRA="SHORT_EXTRA";
    public static String TYPE_RETURN_INT_EXTRA="INT_EXTRA";
    public static String TYPE_RETURN_FLOAT_EXTRA="FLOAT_EXTRA";
    public static String TYPE_RETURN_LONG_EXTRA="LONG_EXTRA";
    public static String TYPE_RETURN_DOUBLE_EXTRA="DOUBLE_EXTRA";
    public static String TYPE_RETURN_STRING_EXTRA="STRING_EXTRA";

    public static String TYPE_INTENT="INTENT";


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
        this.id =intentDataTransfer.id;
        this.whereGenUnit =intentDataTransfer.whereGenUnit;
        this.whereGenSootMethod=intentDataTransfer.whereGenSootMethod;
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
