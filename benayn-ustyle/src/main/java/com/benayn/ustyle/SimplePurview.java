/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.math.LongMath;

/**
 *
 */
public final class SimplePurview {
	
	/**
	 * 
	 */
	private Long delegate;

	/**
	 * Returns a new {@link SimplePurview} instance
	 * 
	 * @param targets
	 * @return
	 */
	public static SimplePurview of(int... targets) {
		return new SimplePurview(targets);
	}
	
	private SimplePurview(int... targets) {
		intl(targets);
	}
	
	/**
	 * Returns <code>true</code> if the given purview in the delegate purview 
	 * 
	 * @param values
	 * @return
	 */
	public boolean has(int... values) {
		long p = pur(values[0]);
		for (int i = 1, l = values.length; i < l; i++) {
			p += pur(values[i]);
		}
		return p == (this.delegate & p);
	}
	
	/**
	 * Returns the delegate purview
	 * 
	 * @return
	 */
	public Long purview() {
		return this.delegate;
	}
	
	private static long pur(int p) {
		return LongMath.pow(2, p); //(long) Math.pow(2, p)   2^p
	}
	
	private void intl(int[] targets) {
		long v = pur(targets[0]);
		for (int i = 1, l = targets.length, t = 0; i < l; i++) {
			checkArgument((t = targets[i]) <= MAX, "The purview: %s must less equal than %s", t, MAX);
			v += pur(t);
		}
		this.delegate = v;
	}
	
	private static final int MAX = 63;
}

