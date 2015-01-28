package com.benayn.ustyle.inner;

public abstract class Options<H, C> {
	
	/**
	 * The outer class instance reference
	 */
	protected H outerRef = null;
	
	/**
	 * The sub class instance of {@link Options} reference
	 * 
	 * @return
	 */
	protected C THIS = null;
	
	/**
	 * Sets the outer instance and sub instance reference
	 * 
	 * @param outerRef
	 * @param childRef
	 */
	protected void reference(H outerRef, C childRef) {
		this.outerRef = outerRef;
		this.THIS = childRef;
	}
}
