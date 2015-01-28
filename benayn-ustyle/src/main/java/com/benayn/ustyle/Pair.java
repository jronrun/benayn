/**
 * 
 */
package com.benayn.ustyle;



/**
 *
 */
public class Pair<L, R> {

	private L l; private R r;
	
	public Pair(L left, R right) {
		this.l = left;
		this.r = right;
	}

	/**
	 * 
	 */
	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<L, R>(left, right);
	}
	
	public L getL() {
		return this.l;
	}
	
	public R getR() {
		return this.r;
	}
	
	@Override public int hashCode() {
		return Objects2.hashCodes(this);
	}

	@Override public boolean equals(Object obj) {
		return Objects2.isEqual(this, obj);
	}

	@Override public String toString() {
		return Objects2.toString(this);
	}
	
}
