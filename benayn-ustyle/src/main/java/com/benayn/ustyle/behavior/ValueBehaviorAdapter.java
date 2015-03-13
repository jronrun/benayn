package com.benayn.ustyle.behavior;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class ValueBehaviorAdapter<O> extends ValueBehavior<O> {

	public ValueBehaviorAdapter(Object delegate) {
		super(delegate);
	}
	
	protected abstract O defaultBehavior();

	@Override protected O nullIf() {
		return defaultBehavior();
	}

	@Override protected <T> O classIf(Class<T> resolvedP) {
		return defaultBehavior();
	}

	@Override protected O primitiveIf() {
		return defaultBehavior();
	}

	@Override protected O eightWrapIf() {
		return defaultBehavior();
	}

	@Override protected O dateIf(Date resolvedP) {
		return defaultBehavior();
	}

	@Override protected O stringIf(String resolvedP) {
		return defaultBehavior();
	}

	@Override protected O enumif(Enum<?> resolvedP) {
		return defaultBehavior();
	}

	@Override protected <T> O arrayIf(T[] resolvedP, boolean isPrimitive) {
		return defaultBehavior();
	}

	@Override protected O bigDecimalIf(BigDecimal resolvedP) {
		return defaultBehavior();
	}

	@Override protected O bigIntegerIf(BigInteger resolvedP) {
		return defaultBehavior();
	}

	@Override protected <K, V> O mapIf(Map<K, V> resolvedP) {
		return defaultBehavior();
	}

	@Override protected <T> O setIf(Set<T> resolvedP) {
		return defaultBehavior();
	}

	@Override protected <T> O listIf(List<T> resolvedP) {
		return defaultBehavior();
	}

	@Override protected O beanIf() {
		return defaultBehavior();
	}
	
}
