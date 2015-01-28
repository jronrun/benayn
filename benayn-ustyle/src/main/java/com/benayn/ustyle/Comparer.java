/**
 * 
 */
package com.benayn.ustyle;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.collect.ComparisonChain;

/**
 *
 */
public final class Comparer<T> {
	
	/**
	 * 
	 */
	protected final Log log = Loggers.from(getClass());
	
	private Comparable<T> delegate;
	
	/**
	 * Returns a new Comparer instance
	 * 
	 * @param delegate
	 * @return
	 */
	public static <T> Comparer<T> of(Comparable<T> delegate) {
		return new Comparer<T>(delegate);
	}
	
	/**
	 * Returns true if the given target is any one of the given expects
	 * 
	 * @param target
	 * @param expects
	 * @return
	 */
	public static <C> boolean expect(C target, @SuppressWarnings("unchecked") C... expects) {
		if (null == expects) { return null == target; }
		for (C expect : expects) { if (expect == target) { return true; } }
		return false;
	}
	
	/**
	 * 
	 */
	private Comparer(Comparable<T> delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * Checks if the delegate object being tested is less than the given target
	 * 
	 * @param target
	 * @return
	 */
	public boolean lt(Comparable<T> target) {
		return compareTo(target) < 0;
	}
	
	/**
	 * Checks if the delegate object being tested is less than or equals the given target
	 * 
	 * @param target
	 * @return
	 */
	public boolean lte(Comparable<T> target) {
		return compareTo(target) <= 0;
	}
	
	/**
	 * Checks if the delegate object being tested is equals the given target
	 * 
	 * @param target
	 * @return
	 */
	public boolean is(Comparable<T> target) {
		return compareTo(target) == 0;
	}
	
	/**
	 * Checks if the delegate object being tested is greater than the given target
	 * 
	 * @param target
	 * @return
	 */
	public boolean gt(Comparable<T> target) {
		return compareTo(target) > 0;
	}
	
	/**
	 * Checks if the delegate object being tested is greater than or equals the given target
	 * 
	 * @param target
	 * @return
	 */
	public boolean gte(Comparable<T> target) {
		return compareTo(target) >= 0;
	}
	
	/**
	 * Returns the result of (delegate == target1) || (delegate == target2) || ... || (delegate == targetN) 
	 * 
	 * @param targets
	 * @return
	 */
	public boolean any(Object... targets) {
		if (null == targets) { return null == this.delegate; }
		
		for (Object target : targets) {
			if (null == target) { if (null == this.delegate) { return true; } continue; }
			if (target == this.delegate) { return true; }
		}
		
		return false;
	}
	
	/**
	 * Returns the result of (delegate == target1) && (delegate == target2) && ... && (delegate == targetN) 
	 * 
	 * @param targets
	 * @return
	 */
	public boolean all(Object... targets) {
		if (null == targets) { return null == this.delegate; }
		
		for (Object target : targets) {
			if (null == target) { if (null != this.delegate) { return false; } continue; }
			if (target != this.delegate) { return false; }
		}
		
		return true;
	}
	
	/**
	 * Compares this delegate object with the given object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
	 * @param target
	 * @return
	 */
	public int compareTo(Comparable<T> target) {
		return compare(target).result();
	}
	
	/**
	 * Compares two comparable objects as specified by
	 * {@link Comparable#compareTo}, <i>if</i> the result of this comparison
	 * chain has not already been determined.
	 * 
	 * @param target
	 * @return
	 */
	public ComparisonChain compare(Comparable<T> target) {
		return chain().compare(delegate, target);
	}
	
	/**
	 * Begins a new chained comparison statement
	 * 
	 * @return
	 */
	public static ComparisonChain chain() {
		return ComparisonChain.start();
	}
	
	/**
	 * Returns the delegate compare object
	 * 
	 * @return
	 */
	public Comparable<T> get() {
		return this.delegate;
	}
	
}