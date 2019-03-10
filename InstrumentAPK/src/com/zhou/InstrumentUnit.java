package com.zhou;

import soot.Body;
import soot.Unit;

public class InstrumentUnit {
    public Body body = null;
    public Unit point = null;
    public String message = null;

    public InstrumentUnit(Body body, Unit unit, String appPath) {
        super();
        this.body = body;
        this.point = unit;
        this.message = appPath;
    }


}