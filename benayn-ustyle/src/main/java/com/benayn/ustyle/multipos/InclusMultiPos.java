/**
 * 
 */
package com.benayn.ustyle.multipos;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 
 * @param <E>
 * @param <L>
 * @param <R>
 */
public abstract class InclusMultiPos<E, L, R> extends NegateMultiPos<E, L, R>  {
	
	protected boolean[] inclusive = new boolean[] { false, false };

	protected InclusMultiPos(L left, R right) {
		super(left, right);
	}
	
	/**
	 * Set whether the results contain the left and right borders, Default are both exclusive.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public InclusMultiPos<E, L, R> inclus(Boolean left, Boolean right) {
		this.inclusive[0] = checkNotNull(left);
		this.inclusive[1] = checkNotNull(right);
		return this;
	}
	
	/**
	 * Set the results contain the left borders
	 * 
	 * @return
	 */
	public E inclusL() {
		return inclus(true, false).result();
	}
	
	/**
	 * Set the results contain the right borders
	 * 
	 * @return
	 */
	public E inclusR() {
		return inclus(false, true).result();
	}
	
	/**
	 * Set the results contain the left and right borders
	 * 
	 * @return
	 */
	public E inclus() {
		return inclus(true, true).result();
	}
	
}
