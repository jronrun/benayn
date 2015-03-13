/**
 * 
 */
package com.benayn.ustyle.multipos;

import static com.google.common.base.Preconditions.checkNotNull;

import com.benayn.ustyle.string.Strs;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class AsymmMultiPos<E, L, R> extends InclusMultiPos<E, L, R> {
	
	protected String asymmLR = null;

	protected AsymmMultiPos(L left, R right) {
		super(left, right);
	}
	
	/**
	 * The result with the given left position and right position 
	 * which left and right tag is asymmetric will be involved in the operation
	 * 
	 * @param leftPos
	 * @param rightPos
	 * @return
	 */
	public E asymms(Integer leftPos, Integer rightPos) {
		return asymm(leftPos, rightPos).result();
	}
	
	/**
	 * The result with the given left position and right position 
	 * which left and right tag is asymmetric will be involved in the operation
	 * 
	 * @param leftPos
	 * @param rightPos
	 * @return
	 */
	public AsymmMultiPos<E, L, R> asymm(Integer leftPos, Integer rightPos) {
		this.asymmLR = new StringBuilder().
			append(checkNotNull(leftPos)).append(Strs.WHITE_SPACE).
			append(checkNotNull(rightPos)).toString();
		return this;
	}
	
}
