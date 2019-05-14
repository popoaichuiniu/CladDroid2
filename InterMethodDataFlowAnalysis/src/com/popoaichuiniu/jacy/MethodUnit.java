package com.popoaichuiniu.jacy;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

public class MethodUnit {
    public SootMethod sootMethod;
    public Unit unit;

    public MethodUnit(SootMethod sootMethod, Unit unit) {
        this.sootMethod = sootMethod;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodUnit that = (MethodUnit) o;
        return Objects.equals(sootMethod, that.sootMethod) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, unit);
    }

    @Override
    public String toString() {
        return "MethodUnit{" +
                "sootMethod=" + sootMethod +
                ", unit=" + unit +
                '}';
    }
}
