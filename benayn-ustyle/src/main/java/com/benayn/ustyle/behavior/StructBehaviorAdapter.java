package com.benayn.ustyle.behavior;

public abstract class StructBehaviorAdapter<O> extends StructBehavior<O> {

	public StructBehaviorAdapter(Object delegate) {
		super(delegate);
	}

	@Override protected O booleanIf() {
		return defaultBehavior();
	}

	@Override protected O byteIf() {
		return defaultBehavior();
	}

	@Override protected O characterIf() {
		return defaultBehavior();
	}

	@Override protected O doubleIf() {
		return defaultBehavior();
	}

	@Override protected O floatIf() {
		return defaultBehavior();
	}

	@Override protected O integerIf() {
		return defaultBehavior();
	}

	@Override protected O longIf() {
		return defaultBehavior();
	}

	@Override protected O shortIf() {
		return defaultBehavior();
	}

	@Override protected O nullIf() {
		return defaultBehavior();
	}

	@Override protected O noneMatched() {
		return defaultBehavior();
	}

	protected abstract O defaultBehavior();
	
}
