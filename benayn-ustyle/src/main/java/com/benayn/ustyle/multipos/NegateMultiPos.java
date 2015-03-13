package com.benayn.ustyle.multipos;

import static com.google.common.base.Preconditions.checkNotNull;

import com.benayn.ustyle.string.Strs;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class NegateMultiPos<E, L, R> extends MultiPos<E, L, R> {

	//L: normal, N: negative (And with fill string if Looking instance), M: negative (And with fill char if Looking instance)
	protected Character plusminus = 'L';
	protected String filltgt = Strs.EMPTY;
	protected static final Character NEGATE = 'N';
	
	protected NegateMultiPos(L left, R right) {
		super(left, right);
	}
	
	/**
	 * The negative result will be involved in the operation
	 * <p><b>and the result will filled by empty "" if is a {@link com.benayn.ustyle.string.Finder} or {@link com.benayn.ustyle.string.Replacer#ctx(String)} instance</b>
	 * 
	 * @return NegateMultiPos<E, L, R> instance
	 */
	public NegateMultiPos<E, L, R> negate() {
		return negmark(NEGATE);
	}
	
	/**
	 * The negative result will be involved in the operation, 
	 * <p><b>and the result will filled by empty "" if is a {@link com.benayn.ustyle.string.Finder} or {@link com.benayn.ustyle.string.Replacer#ctx(String)} instance</b>
	 * 
	 * @return E
	 */
	public E negates() {
		return negmark(NEGATE).result();
	}
	
	/**
	 * The negative result will be involved in the operation, 
	 * and the result will filled by given fill target
	 * <p><b>Only work for {@link com.benayn.ustyle.string.Finder} and {@link com.benayn.ustyle.string.Replacer#ctx(String)} instance,
	 * not work for {@link com.benayn.ustyle.string.Replacer#of(String)} instance</b>
	 * 
	 * @param filler
	 * @return NegateMultiPos<E, L, R> instance
	 */
	public NegateMultiPos<E, L, R> fnegate(Object filler) {
		this.filltgt = checkNotNull(filler).toString();
		negmark(Character.class.isInstance(filler) ? 'M' : NEGATE);
		return this;
	}
	
	/**
	 * The negative result will be involved in the operation, 
	 * and the result will filled by given fill target char that has result's length
	 * <p><b>Only work for {@link com.benayn.ustyle.string.Finder} and {@link com.benayn.ustyle.string.Replacer#ctx(String)} instance,
	 * not work for {@link com.benayn.ustyle.string.Replacer#of(String)} instance</b>
	 * 
	 * @param filler
	 * @return E
	 */
	public E fnegates(Object filler) {
		return fnegate(filler).result();
	}
	
	private NegateMultiPos<E, L, R> negmark(Character pm) {
		this.plusminus = pm;
		return this;
	}
}