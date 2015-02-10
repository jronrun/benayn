/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.benayn.ustyle.string.Strs.INDEX_NONE_EXISTS;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;

import com.benayn.ustyle.Decision;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 *
 */
public abstract class AbstrStrMatcher<M> {

	/**
	 * 
	 */
	protected final Log log = Loggers.from(getClass());
	
	/**
	 * Returns the delegate string
	 * 
	 * @return
	 */
	public String get() {
		return this.delegate.get();
	}
	
	/**
	 * Returns the sub instance of the {@link AbstrStrMatcher}
	 * 
	 * @return
	 */
	protected abstract M THIS();
	
	/**
	 * Append the specified string to the delegate string
	 * 
	 * @param join
	 * @return
	 */
	public M joins(Object... join) {
		this.delegate = Optional.of(join(join));
		return THIS();
	}
	
	/**
	 * Append the specified string to the delegate string
	 * 
	 * @param separator
	 * @param join
	 * @return
	 */
	public M joinsWith(String separator, Object... join) {
		this.delegate = Optional.of(joinWith(separator, join));
		return THIS();
	}
	
	/**
	 * Returns a new string that append the specified string to the delegate string
	 * 
	 * @param join
	 * @return
	 */
	public String join(Object... join) {
		return joinWith(EMPTY, join);
	}
	
	/**
	 * Returns a new string that append the specified string 
	 * to the delegate string with specified separator
	 * 
	 * @param join
	 * @return
	 */
	public String joinWith(String separator, Object... join) {
		return Joiner.on(separator).join(delegate.get(), EMPTY, join);
	}
	
	/**
	 * Update the given string as the new delegate string
	 * 
	 * @param asNewDelegate
	 * @return
	 */
	public M update(String asNewDelegate) {
		this.delegate = Optional.of(asNewDelegate);
		updateHandle();
		return THIS();
	}
	
	/**
	 * Compares target string {@code String} to another {@code String}, ignoring case considerations.
	 * 
	 * @return
	 */
	public M ignoreCase() {
		this.ignoreCase = true;
		return THIS();
	}
	
	/**
     * Checks if delegate string contains all string in the given string array
     * 
     * @param containWith
     * @return
     */
	public boolean containAll(String... containWith) {
	    for (String contain : containWith) {
            if (!contain(contain)) {
                return false;
            }
        }
	    
	    return true;
	}
	
	/**
     * Checks if delegate string contains all string in the given string list
     * 
     * @param containWith
     * @return
     */
	public boolean containsAll(List<String> containWith) {
        return containAll(checkNotNull(containWith).toArray(new String[containWith.size()]));
    }
	
	/**
	 * Checks if delegate string contains any string in the given string array
	 * 
	 * @param containWith
	 * @return
	 */
	public boolean contain(String... containWith) {
	    return indexs(0, containWith) >= 0;
	}
	
	/**
	 * Checks if delegate string contains any string in the given string list
	 * 
	 * @param containWith
	 * @return
	 */
	public boolean contains(List<String> containWith) {
		return contain(checkNotNull(containWith).toArray(new String[containWith.size()]));
	}
	
	/**
	 * Check if delegate string ends with any string in the given string array
	 * 
	 * @param endWith
	 * @return
	 */
	public boolean end(String... endWith) {
		return ends(Arrays.asList(checkNotNull(endWith)));
	}
	
	/**
	 * Check if delegate string ends with any string in the given string list
	 * 
	 * @param endWith
	 * @return
	 */
	public boolean ends(List<String> endWith) {
		return startOrEndAny(0, endWith, 'e');
	}
	
	/**
	 * Check if delegate string starts with any of a array of 
	 * specified strings beginning at the specified index.
	 * 
	 * @param startWith
	 * @return
	 */
	public boolean start(String... startWith) {
		return startOrEndAny(0, Arrays.asList(checkNotNull(startWith)), 's');
	}
	
	/**
	 * Check if delegate string starts with any of a list of 
	 * specified strings beginning at the specified index.
	 * 
	 * @param toffset
	 * @param startWith
	 * @return
	 */
	public boolean starts(final Integer toffset, List<String> startWith) {
		return startOrEndAny(toffset, startWith, 's');
	}
	
	/**
	 * Search delegate string to find the first index of any string in 
	 * the given string array, starting at the specified index
	 * 
	 * @param indexWith
	 * @return
	 */
	public int index(String... indexWith) {
		return indexs(0, checkNotNull(indexWith));
	}
	
	/**
	 * Search delegate string to find the first index of any string in 
	 * the given string list, starting at the specified index
	 * 
	 * @param fromIndex
	 * @param indexWith
	 * @return
	 */
	public int indexs(final Integer fromIndex, String... indexWith) {
		int index = INDEX_NONE_EXISTS;
		final String target = ignoreCase ? delegate.get().toLowerCase() : delegate.get();
		
		for (String input : indexWith) {
			String target2 = ignoreCase ? input.toLowerCase() : input;
			if ((index = target.indexOf(target2, fromIndex)) >= 0) {
				return index;
			}
		}
		
		return index;
	}
	
	/**
	 * Checks if a delegate string is empty ("") or null or special value.
	 * 
	 * @param specialValueAsEmpty
	 * @return
	 */
	public boolean isEmpty(String... specialValueAsEmpty) {
		if (Strings.isNullOrEmpty(delegate.get())) {
			return true;
		}
		
		if (null == specialValueAsEmpty || specialValueAsEmpty.length < 1) {
			return false;
		}
		
		if (Gather.from(Arrays.asList(checkNotNull(specialValueAsEmpty))).filter(new Decision<String>(){
			@Override public boolean apply(String input) {
				return isEqual(input, delegate.get());
			}
		}).noneNullList().size() > 0) {
			return true;
		}
		
		return Decisions.isEmpty().apply(delegate.get());
	}
	
	/**
	 * 
	 */
	protected void updateHandle() {
		
	}
	
	protected Optional<String> delegate = null;
	private boolean ignoreCase = Boolean.FALSE;

	private boolean startOrEndAny(final Integer toffset, List<String> startWith, final char startOrEnd) {
		final String target = ignoreCase ? delegate.get().toLowerCase() : delegate.get();
		
		return Gather.from(checkNotNull(startWith)).filter(new Decision<String>(){
			@Override public boolean apply(String input) {
				String target2 = ignoreCase ? input.toLowerCase() : input;
				
				switch (startOrEnd) {
				case 's':
					return target.startsWith(target2, toffset);
				case 'e':
					return target.endsWith(target2);
				default:
					return false;
				}
			}
		}).noneNullList().size() > 0;
	}
	
	/**
	 * 
	 */
	protected boolean isEqual(String targetLeft, String targetRight) {
		return ignoreCase ? targetLeft.equalsIgnoreCase(targetRight) : targetLeft.equals(targetRight);
	}
	
}
