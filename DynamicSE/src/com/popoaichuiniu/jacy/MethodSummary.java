package com.popoaichuiniu.jacy;

import soot.SootMethod;

import java.util.Objects;

public class MethodSummary {
    public SootMethod sootMethod;
    public int paraIndex;

    boolean isParaFlowToReturn;
    IntentDataTransfer returnIntentDataTransfer = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodSummary that = (MethodSummary) o;
        return paraIndex == that.paraIndex &&
                Objects.equals(sootMethod, that.sootMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, paraIndex);
    }

    public MethodSummary(SootMethod sootMethod, int paraIndex) {
        this.sootMethod = sootMethod;
        this.paraIndex = paraIndex;
    }
}
