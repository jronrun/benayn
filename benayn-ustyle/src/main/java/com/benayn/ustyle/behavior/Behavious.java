package com.benayn.ustyle.behavior;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class Behavious<O> {
	
	protected Object delegate;

	public Behavious(Object delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * Checks if the delegate is matched.
	 * 
	 * @return
	 */
	public abstract boolean isMatched();
	
	protected abstract O nullIf();
	
	/**
	 * Call this method after the {@link ValueBehavior#nullIf()}, Sub class can override this method to initialize something.
	 * 
	 * @return
	 */
	protected O afterNullIf() {
		return toBeContinued();
	}
	
	/**
	 * Returns the default behavior if not matched
	 * 
	 * @return
	 */
	protected abstract O noneMatched();
	
	/**
	 * Default is returns the xxxIf() method result immediately, 
	 * call {@link ValueBehavior#toBeContinued()} will changes the default behavior.
	 */
	protected O toBeContinued() {
		this.isContinue = true;
		return null;
	}

	protected boolean isContinue = false;
	protected void reset(O o) {
		//reset isContinue value to default behavior
		this.isContinue = false;
	}
	
}
