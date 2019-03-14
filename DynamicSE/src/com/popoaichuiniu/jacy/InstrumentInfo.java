package com.popoaichuiniu.jacy;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

public class InstrumentInfo {
    SootMethod sootMethod;
    Unit point;
    String name;
    String type;
    boolean isLocal;
    String id;//action extra, category
    boolean isIf;


    public InstrumentInfo(SootMethod sootMethod, Unit point, String name, String type, boolean isLocal, String id, boolean isIf) {
        this.sootMethod = sootMethod;
        this.point = point;
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
        this.id = id;
        this.isIf = isIf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentInfo that = (InstrumentInfo) o;
        return isLocal == that.isLocal &&
                isIf == that.isIf &&
                Objects.equals(sootMethod, that.sootMethod) &&
                Objects.equals(point, that.point) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, point, name, type, isLocal, id, isIf);
    }
}
