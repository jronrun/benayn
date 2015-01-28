/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.benayn.ustyle.Gather;
import com.benayn.ustyle.multipos.MultiPos;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 *
 */
public final class Finder extends FindingReplacing<Finder> {
	
	/**
	 * Returns a new {@link Finder} instance with given string
	 * 
	 * @param target
	 * @return
	 */
	public static Finder of(String target) {
		return new Finder().update(target);
	}

	/**
	 * Returns the first lookup result
	 * 
	 * @return
	 */
	public String get() {
		return find().first();
	}
	
	/**
	 * Returns the all lookup results
	 * 
	 * @return
	 */
	public String gets() {
		return find().all();
	}
	
	/**
	 * Returns the all lookup results as a list
	 * 
	 * @return
	 */
	public List<String> asList() {
		return Lists.newLinkedList(Splitter.on(separator.toString()).split(gets()));
	}
	
	/**
	 * Returns the all lookup results as a Gather
	 * 
	 * @return
	 */
	public Gather<String> asGather() {
		return Gather.from(asList());
	}
	
	/**
	 * Returns the last lookup result
	 * 
	 * @return
	 */
	public String getLast() {
		return find().last();
	}
	
	/**
	 * Returns the specified position lookup result
	 * 
	 * @return
	 */
	public String get(int position) {
		return find().pos(position);
	}

	/**
	 * Returns a NegateMultiPos instance with all lookup results
	 * 
	 * @return
	 */
	public MultiPos<String, String, String> find() {
		return new MultiPos<String, String, String>(null, null) {
			
			@Override protected String result() {
				return findingReplacing(EMPTY, 'L', pos, position);
			}
		};
	}
	
	/**
	 * Sets the given separator to separate the all results, 
	 * when call {@link Finder#splitAll()} and then call {@link MultiPos#all()}, eg:
	 * <pre>
	 *   finder.splitAll().betnNext("li").find().all();
	 * </pre>
	 * @see Finder#splitAll()
	 * @param separator
	 * @return
	 */
	public Finder separ(Object separator) {
		this.separator = checkNotNull(separator);
		return THIS();
	}
	
	/**
	 * Separate all the results with specified separator 
	 * by {@link Finder#separ(Object)} when call {@link MultiPos#all()} method
	 * 
	 * @see MultiPos#all()
	 * @see Finder#separ(Object)
	 * @return
	 */
	public Finder splitAll() {
		this.allAsOne = false;
		return THIS();
	}

	/* (non-Javadoc)
	 * @see com.funcity.me.string.FindingAndReplacing#THIS()
	 */
	@Override protected Finder THIS() {
		return this;
	}

}
