/**
 * 
 */
package com.benayn.ustyle.string;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * https://github.com/jronrun/benayn
 */
public final class Strs extends CharMatcher {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Strs.class);
	
	/**
     * The empty String <code>""</code>
     */
    public static final String EMPTY = "";
    
    /**
     * The whitespace String <code>" "</code>
     */
    public static final String WHITE_SPACE = " ";
    
    /**
     * The whitespace Character <code>' '</code>
     */
    public static final char CHAR_WHITE_SPACE = ' ';
    
    /**
     * {@code \u0009} tab character ('\t').
     */
    public static final char TAB = '\t';
    
    /**
     * {@code \u000a} linefeed LF ('\n').
     */
    public static final char LF = '\n';

    /**
     * {@code \u000d} carriage return CR ('\r').
     */
    public static final char CR = '\r';
    
    /**
     * Represents a failed index search.
     */
    public static final int INDEX_NONE_EXISTS = -1;
    
	/**
	 * Checks if a String is null
	 * 
	 * @param target
	 * @return
	 */
	public static boolean isNull(String target) {
		return null == target;
	}
	
	/**
	 * Checks if the given target is a instance of String
	 * 
	 * @param target
	 * @return
	 */
	public static boolean is(Object target) {
//		return String.class.isInstance(target);
		return target instanceof String;
	}
	
	/**
	 * Checks if a String is empty ("") or null
	 * 
	 * @param target
	 * @return
	 */
	public static boolean isEmpty(String target) {
		return Strings.isNullOrEmpty(target);
	}
	
	/**
	 * Checks if a Character is white space
	 * 
	 * @param ch
	 * @return
	 */
	public static boolean isWhitespace(int ch) {
		return CHAR_WHITE_SPACE == ch || TAB == ch || LF == ch || CR == ch;
    }
	
	/**
	 * Checks if a String is empty ("") or null or special value.
	 * 
	 * @param target
	 * @param specialValueAsEmpty
	 * @return
	 */
	public static boolean isEmpty(String target, String... specialValueAsEmpty) {
		if (isEmpty(target)) {
			return true;
		}
		
		return matcher(target).isEmpty(specialValueAsEmpty);
	}
	
	/**
	 * Checks if a String is whitespace, multi whitespace, empty ("") or null
	 * 
	 * @param target
	 * @return
	 */
	public static boolean isBlank(String target) {
		return isBlank(target, WHITE_SPACE);
	}
			
	/**
	 * Checks if a String is whitespace, multi whitespace, empty ("") or null or special value.
	 * 
	 * @param target
	 * @param specialValueAsEmpty
	 * @return
	 */
	public static boolean isBlank(String target, String... specialValueAsEmpty) {
		if (Strings.isNullOrEmpty(target)) {
			return true;
		}
		
		return isEmpty(WHITESPACE.removeFrom(target), specialValueAsEmpty);
	}
	
	/**
	 * <p>Remove the last character from a String.</p>
     * <p>If the String ends in {@code \r\n}, then remove both of them.</p>
     * 
	 * @param target
	 * @return
	 */
	public static String chop(String target) {
		if (isEmpty(target)) { return EMPTY; }
		int len = target.length(), lastIdx = -1;
		if (len >= 2 && target.charAt(len - 1) == LF && target.charAt(len - 2) == CR) {
			if (len == 2) { return EMPTY; } lastIdx = -2; 
		}
		return replace(target).afters(lastIdx).byNone().last();
	}
	
    /**
     * Checks if target string contains any string in the given string array
     * 
     * @param target
     * @param containWith
     * @return
     */
	public static boolean contains(String target, String... containWith) {
		return contains(target, Arrays.asList(containWith));
	}
	
	/**
	 * Checks if target string contains any string in the given string list
	 * 
	 * @param target
	 * @param containWith
	 * @return
	 */
	public static boolean contains(String target, List<String> containWith) {
		if (isNull(target)) {
			return false;
		}
		
		return matcher(target).contains(containWith);
	}

	/**
	 * Search target string to find the first index of any string in the given string array
	 * 
	 * @param target
	 * @param indexWith
	 * @return
	 */
	public static int indexAny(String target, String... indexWith) {
		return indexAny(target, 0, Arrays.asList(indexWith));
	}
	
	/**
	 * Search target string to find the first index of any string in the given string array, starting at the specified index
	 * 
	 * @param target
	 * @param fromIndex
	 * @param indexWith
	 * @return
	 */
	public static int indexAny(String target, Integer fromIndex, String... indexWith) {
		return indexAny(target, fromIndex, Arrays.asList(indexWith));
	}
	
	/**
	 * Search target string to find the first index of any string in the given string list, starting at the specified index
	 * 
	 * @param target
	 * @param fromIndex
	 * @param indexWith
	 * @return
	 */
	public static int indexAny(String target, Integer fromIndex, List<String> indexWith) {
		if (isNull(target)) {
			return INDEX_NONE_EXISTS;
		}
		
		return matcher(target).indexs(fromIndex, checkNotNull(indexWith).toArray(new String[indexWith.size()]));
	}
	
	/**
	 * Check if target string ends with any of an array of specified strings.
	 * 
	 * @param target
	 * @param endWith
	 * @return
	 */
	public static boolean endAny(String target, String... endWith) {
		return endAny(target, Arrays.asList(endWith));
	}
	
	/**
	 * Check if target string ends with any of a list of specified strings.
	 * 
	 * @param target
	 * @param endWith
	 * @return
	 */
	public static boolean endAny(String target, List<String> endWith) {
		if (isNull(target)) {
			return false;
		}
		
		return matcher(target).ends(endWith);
	}
	
	/**
	 * Check if target string starts with any of an array of specified strings.
	 * 
	 * @param target
	 * @param startWith
	 * @return
	 */
	public static boolean startAny(String target, String... startWith) {
		return startAny(target, 0, Arrays.asList(startWith));
	}
	
	/**
	 * Check if target string starts with any of an array of specified strings beginning at the specified index.
	 * 
	 * @param target
	 * @param toffset
	 * @param startWith
	 * @return
	 */
	public static boolean startAny(String target, Integer toffset, String... startWith) {
		return startAny(target, toffset, Arrays.asList(startWith));
	}
	
	/**
	 * Check if target string starts with any of a list of specified strings beginning at the specified index.
	 * 
	 * @param target
	 * @param toffset
	 * @param startWith
	 * @return
	 */
	public static boolean startAny(String target, Integer toffset, List<String> startWith) {
		if (isNull(target)) {
			return false;
		}
		
		return matcher(target).starts(toffset, startWith);
	}
	
	/**
	 * Returns a new {@link Indexer} instance
	 * 
	 * @param target
	 * @return
	 */
	public static Indexer index(String target) {
		return Indexer.of(target);
	}
	
	/**
	 * Returns the result mode {@link Replacer} instance
	 * 
	 * @see Replacer#of(String)
	 * @param target
	 * @return
	 */
	public static Replacer replace(String target) {
		return Replacer.of(target);
	}
	
	/**
	 * Returns a new context mode {@link Replacer} instance
	 * 
	 * @see Replacer#ctx(String)
	 * @param target
	 * @return
	 */
	public static Replacer replace2(String target) {
		return Replacer.ctx(target);
	}
	
	/**
	 * Returns a new {@link Finder} instance
	 * 
	 * @param target
	 * @return
	 */
	public static Finder find(String target) {
		return Finder.of(target);
	}
	
	/**
	 * Returns a new {@link Betner} instance
	 * 
	 * @param target
	 * @return
	 */
	public static Betner betn(String target) {
		return Betner.of(target);
	}
	
	/**
	 * Returns a new StrMatcher instance
	 * 
	 * @param target
	 * @return
	 */
	public static StrMatcher matcher(String target) {
		return StrMatcher.from(checkNotNull(target));
	}
	
	/**
	 * Returns a {@code String} between matcher that matches any character in a given range
	 * 
	 * @param target
	 * @param left
	 * @param right
	 * @return
	 */
	public static Betner betn(String target, String left, String right) {
		return betn(target).between(left, right);
	}
	
	/**
	 * Returns a {@code String} between matcher that matches any character 
	 * in the two instances of the same String, Adjacent tag matches.
	 * 
	 * @param target
	 * @param leftSameWithRight
	 * @return
	 */
	public static Betner betnNext(String target, String leftSameWithRight) {
		return betn(target).betweenNext(leftSameWithRight);
	}
	
	/**
	 * Returns a {@code String} between matcher that matches any character 
	 * in the two instances of the same String, The first and last one matches.
	 * 
	 * @param target
	 * @param leftSameWithRight
	 * @return
	 */
	public static Betner betnLast(String target, String leftSameWithRight) {
		return betn(target).betweenLast(leftSameWithRight);
	}
	
    /**
     * Returns a {@code String} between matcher that matches any character in the given left and ending
	 * 
     * @param target
     * @param separator
     * @return
     */
    public static Betner after(String target, String separator) {
    	return betn(target).after(separator);
    }
	
	/**
	 * Returns a {@code String} between matcher that matches any character in the beginning and given right
	 * 
	 * @param target
	 * @param separator
	 * @return
	 */
	public static Betner before(String target, String separator) {
		return betn(target).before(separator);
	}

	/* (non-Javadoc)
	 * @see com.google.common.base.CharMatcher#matches(char)
	 */
	@Override public boolean matches(char c) {
		return false;
	}

}
