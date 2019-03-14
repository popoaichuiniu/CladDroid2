package com.popoaichuiniu.jacy;

import soot.SootMethod;
import soot.Unit;

import java.util.Objects;

public class InstrumentInfoByte {

    String methodString;
    String byteTag;
    String name;
    String type;
    boolean isLocal;
    String id;//action extra, category
    boolean isIf;

    public InstrumentInfoByte(String methodString, String byteTag, String name, String type, boolean isLocal, String id, boolean isIf) {
        this.methodString = methodString;
        this.byteTag = byteTag;
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
        InstrumentInfoByte that = (InstrumentInfoByte) o;
        return isLocal == that.isLocal &&
                isIf == that.isIf &&
                Objects.equals(methodString, that.methodString) &&
                Objects.equals(byteTag, that.byteTag) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodString, byteTag, name, type, isLocal, id, isIf);
    }
}
