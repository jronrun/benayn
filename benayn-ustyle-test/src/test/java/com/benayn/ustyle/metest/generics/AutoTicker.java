package com.benayn.ustyle.metest.generics;

import java.util.concurrent.TimeUnit;

/**
 * An automatic ticker that increments by the given time every time it is read.  It starts
 */
public class AutoTicker extends ManualTicker {
    /** How long to auto-tick */
    private final long time;
    /** The units to auto-tick */
    private final TimeUnit unit;

    @Override public long read() {
        tick(time, unit);
        return super.read();
    }

    public AutoTicker(final long time, final TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }
}
