package com.benayn.ustyle.metest.generics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Ticker;

/**
 * A ticker that can be "ticked" manually.  This ticker always starts with a reference of "0"
 */
public class ManualTicker extends Ticker {
    private final AtomicLong tick = new AtomicLong(0);
    
    /**
     * Call this to "tick" the ticker.  It adds the amount of time to how long it has gone so far
     *
     * @param time the amount of time to add
     * @param unit the TimeUnit to add
     * @return the ticker
     */
    public ManualTicker tick(final long time, final TimeUnit unit) {
        tick.addAndGet(TimeUnit.NANOSECONDS.convert(time, unit));
        return this;
    }
    
    /**
     * Call this to set the ticker to a fixed value.
     *
     * @param time the amount of time to set
     * @param unit the TimeUnit to set
     * @return the ticker
     */
    public ManualTicker set(final long time, final TimeUnit unit) {
        tick.set(TimeUnit.NANOSECONDS.convert(time, unit));
        return this;
    }
    
    @Override public long read() { return tick.get(); }
}
