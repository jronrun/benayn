/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.benayn.ustyle.string.Strs.WHITE_SPACE;

/**
 * https://github.com/jronrun/benayn
 */
public class StrMatcher extends AbstrStrMatcher<StrMatcher> {
	
	/**
	 * Returns a new {@link StrMatcher} instance
	 * 
	 * @param target
	 * @return
	 */
	public static StrMatcher from(String target) {
		return new StrMatcher().update(target);
	}
	
	/**
	 * Returns a new {@link StrMatcher} instance with empty string ""
	 * 
	 * @return
	 */
	public static StrMatcher empty() {
		return from(EMPTY);
	}
	
	/**
	 * Returns a new {@link StrMatcher} instance with whitespace String " "
	 * 
	 * @return
	 */
	public static StrMatcher space() {
		return from(WHITE_SPACE);
	}

	/* (non-Javadoc)
	 * @see com.funcity.me.string.AbstrStrMatcher#THIS()
	 */
	@Override protected StrMatcher THIS() {
		return this;
	}

}
