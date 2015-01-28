/**
 * 
 */
package com.benayn.ustyle;

import com.google.common.base.Function;

/**
 *
 */
public abstract class Functional<F, T> implements Function<F, T> {

	@Override
	public T apply(F input) {
		return callback(input);
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	protected abstract T callback(F input);
	
}
