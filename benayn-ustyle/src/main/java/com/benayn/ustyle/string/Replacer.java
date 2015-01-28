/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.google.common.base.Preconditions.checkNotNull;

import com.benayn.ustyle.multipos.MultiPos;

/**
 *
 */
public final class Replacer extends FindingReplacing<Replacer> {
	
	/**
	 * Returns a new result mode {@link Replacer} instance
	 * Previous replaced result as next operate string mode
	 * 
	 * @param target
	 * @return
	 */
	public static Replacer of(String target) {
		return new Replacer().update(target).resultM();
	}
	
	/**
	 * Returns the context mode Replacer instance,
	 * Previous look result as next operands string, all operate as one search in original string mode,
	 * look result same as {@link Finder}'s behavior 
	 * but only {@link MultiPos#all()} method that will have the different behavior.
	 * 
	 * @param target
	 * @return
	 */
	public static Replacer ctx(String target) {
		return of(target).contextM();
	}

	/**
	 * Returns a result string with all occurrences with empty ("")
	 * 
	 * @return
	 */
	public String byNones() {
		return byNone().all();
	}
	
	/**
	 * Returns a MultiPos instance with all occurrences with empty ("")
	 * 
	 * @return
	 */
	public MultiPos<String, String, String> byNone() {
		return by(EMPTY);
	}
	
	/**
	 * Replaces first occurrences with given replacement
	 * 
	 * @param replacement
	 * @return
	 */
	public String first(Object replacement) {
		return by(replacement).first();
	}
	
	/**
	 * Replaces last occurrences with given replacement
	 * 
	 * @param replacement
	 * @return
	 */
	public String last(Object replacement) {
		return by(replacement).last();
	}
	
	/**
	 * Replaces all occurrences with given replacement
	 * <B>May multiple substring replaced.</B>
	 * 
	 * @param replacement
	 * @return
	 */
	public String with(Object replacement) {
		return by(replacement).all();
	}
	
	/**
	 * Returns a MultiPos instance with all occurrences with given replacement
	 * 
	 * @param replacement
	 * @return
	 */
	public MultiPos<String, String, String> by(final Object replacement) {
		return new MultiPos<String, String, String>(checkNotNull(replacement).toString(), null) {

			@Override protected String result() {
				return findingReplacing(left, Character.class.isInstance(replacement) ? 'C' : 'S', pos, position);
			}
		};
	}
	
	/**
	 * Previous look result as next operands string, All operate as one search string mode
	 * 
	 * @return
	 */
	public Replacer contextM() {
		this.searchMode = 'C';
		return this;
	}
	
	/**
	 * Previous replaced result as next operate string mode
	 * 
	 * @return
	 */
	public Replacer resultM() {
		this.searchMode = 'S';
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.funcity.me.string.AbstrStrMatcher#THIS()
	 */
	@Override protected Replacer THIS() {
		return this;
	}

}
