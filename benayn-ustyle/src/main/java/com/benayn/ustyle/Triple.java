/**
 * 
 */
package com.benayn.ustyle;


/**
 * @ClassName: Triple
 * @Description: 
 * @author: paulo.ye
 * @date Feb 8, 2015 2:23:38 AM
 *
 */
public class Triple<L, C, R> {
	
	private L l; private C c; private R r;
	
	public Triple(L left, C center, R right) {
		this.l = left;
		this.c = center;
		this.r = right;
	}

	/**
	 * 
	 */
	public static <L, C, R> Triple<L, C, R> of(L left, C center, R right) {
		return new Triple<L, C, R>(left, center, right);
	}
	
	public L getL() {
		return this.l;
	}
	
	public C getC() {
		return this.c;
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
