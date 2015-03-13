/**
 * 
 */
package com.benayn.ustyle;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class Decisional<T> implements Decision<T> {

	/* (non-Javadoc)
	 * @see com.google.common.base.Predicate#apply(java.lang.Object)
	 */
	@Override
	public boolean apply(T input) {
		decision(input);
		return true;
	}
	
	protected abstract void decision(T input);

}
