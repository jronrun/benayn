package com.benayn.ustyle.behavior;

import com.google.common.primitives.Primitives;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class StructBehavior<O> extends Behavious<O> {

	protected Class<?> clazz;
	
	public StructBehavior(Object delegate) {
		super(delegate);
	}
	
	@Override public boolean isMatched() {
		return this.isMatched;
	}
	
	public O doDetect() {
		O o = null;
		
		if (null == this.delegate) {
			o = nullIf();
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		o = afterNullIf();
		if (this.isContinue) { reset(o); } else { return o; }
		
		if (this.delegate instanceof Class) {
			this.clazz = (Class<?>) this.delegate;
		} else {
			this.clazz = delegate.getClass();
			if (this.clazz.isArray()) {
				this.clazz = this.clazz.getComponentType();
			}
		}
		
		if (Primitives.allPrimitiveTypes().contains(this.clazz)) {
			this.clazz = Primitives.wrap(this.clazz);
			this.isPrimitive = true;
		}
		
		o = primitiveIs(this.isPrimitive);
		if (this.isContinue) { reset(o); } else { return o; }
		
		if (clazz == Boolean.class) {
			o = booleanIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 
		
		if (clazz == Byte.class) {
			o = byteIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 
		
		if (clazz == Character.class) {
			o = characterIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 

		if (clazz == Double.class) {
			o = doubleIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 

		if (clazz == Float.class) {
			o = floatIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 

		if (clazz == Integer.class) {
			o = integerIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 

		if (clazz == Long.class) {
			o = longIf();
			if (this.isContinue) { reset(o); } else { return o; }
		} 

		if (clazz == Short.class) {
			o = shortIf();
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		this.isMatched = false;
		return noneMatched();
	}
	
	/**
	 * Checks if the delegate is primitive or not that sub class can override this method to changes the different behavior
	 * 
	 * @param primitive
	 * @return
	 */
	protected O primitiveIs(boolean primitive) {
		return toBeContinued();
	}
	
	protected abstract O booleanIf();
	protected abstract O byteIf();
	protected abstract O characterIf();
	protected abstract O doubleIf();
	protected abstract O floatIf();
	protected abstract O integerIf();
	protected abstract O longIf();
	protected abstract O shortIf();
	
	private boolean isMatched = true;
	protected boolean isPrimitive;
}
