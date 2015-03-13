package com.benayn.ustyle.behavior;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.Arrays2;
import com.google.common.primitives.Primitives;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class ValueBehavior<O> extends Behavious<O> {
	
	protected Class<?> clazz;
	
	public ValueBehavior(Object delegate) {
		super(delegate);
	}

	public O doDetect() {
		O o = null;
		
		//null
		if (null == this.delegate) {
			o = nullIf();
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		o = afterNullIf();
		if (this.isContinue) { reset(o); } else { return o; }
		
		boolean isClazz = false;
		//class
		if (this.delegate instanceof Class) {
			o = classIf(this.clazz = (Class<?>) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
			isClazz = true;
		} else {
			this.clazz = delegate.getClass();
		}
		
		//primitive
		if (Primitives.allPrimitiveTypes().contains(clazz)) {
			o = primitiveIf();
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//eight wrap type
		if (Primitives.allWrapperTypes().contains(clazz)) {
			o = eightWrapIf();
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//date
		if (Date.class.isAssignableFrom(this.clazz)) {
			o = dateIf(isClazz ? null : (Date) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//string
		if (String.class.isAssignableFrom(this.clazz)) {
			o = stringIf(isClazz ? null : (String) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//enum
		if (Enum.class.isAssignableFrom(this.clazz)) {
			o = enumif(isClazz ? null : (Enum<?>) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//array
		if (this.clazz.isArray()) {
			this.clazz = this.clazz.getComponentType();
			o = arrayIf(isClazz ? null : Arrays2.wraps(this.delegate), Primitives.allPrimitiveTypes().contains(this.clazz));
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//BigDecimal
		if (BigDecimal.class.isAssignableFrom(this.clazz)) {
			o = bigDecimalIf(isClazz ? null : (BigDecimal) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//BigInteger
		if (BigInteger.class.isAssignableFrom(this.clazz)) {
			o = bigIntegerIf(isClazz ? null : (BigInteger) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//map
		if (Map.class.isAssignableFrom(this.clazz)) {
			o = mapIf(isClazz ? null : (Map<?, ?>) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//set
		if (Set.class.isAssignableFrom(this.clazz)) {
			o = setIf(isClazz ? null : (Set<?>) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//list
		if (List.class.isAssignableFrom(this.clazz)) {
			o = listIf(isClazz ? null : (List<?>) this.delegate);
			if (this.isContinue) { reset(o); } else { return o; }
		}
		
		//java bean
		return beanIf();
	}
	
	/**
	 * Call this method before the last method {@link ValueBehaviorAdapter#beanIf()}, Sub class can override this method to patch something.
	 * 
	 * @return
	 */
	protected O beforeBeanIf() {
		return toBeContinued();
	}
	
	protected abstract <T> O classIf(Class<T> resolvedP);
	protected abstract O primitiveIf();
	protected abstract O eightWrapIf();
	protected abstract O dateIf(Date resolvedP);
	protected abstract O stringIf(String resolvedP);
	protected abstract O enumif(Enum<?> resolvedP);
	protected abstract <T> O arrayIf(T[] resolvedP, boolean isPrimitive);
	protected abstract O bigDecimalIf(BigDecimal resolvedP);
	protected abstract O bigIntegerIf(BigInteger resolvedP);
	protected abstract <K, V> O mapIf(Map<K, V> resolvedP);
	protected abstract <T> O setIf(Set<T> resolvedP);
	protected abstract <T> O listIf(List<T> resolvedP);
	protected abstract O beanIf();
	
	@Override protected O noneMatched() {
		return null;
	}

	@Override public boolean isMatched() {
		return true;
	}
	
}