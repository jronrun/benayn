/**
 * 
 */
package com.benayn.ustyle.string;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public final class Indexer extends AbstrStrMatcher<Indexer> {
	
	/**
	 * Returns a new {@link Indexer} instance with given string
	 * 
	 * @param target
	 * @return
	 */
	public static Indexer of(String target) {
		return new Indexer().update(target);
	}
	
	/**
	 * Returns a new string that is a substring of delegate string
	 * 
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	public String between(int leftIndex, int rightIndex) {
		String result = this.target4Sub.substring(reviseL(leftIndex), reviseR(rightIndex));
		return isResetMode ? result : (this.target4Sub = result);
	}
	
	/**
	 * Returns a new string that is a substring of delegate string with specified end index.
	 * 
	 * @param rightIndex
	 * @return
	 */
	public String before(int rightIndex) {
		return between(0, rightIndex);
	}
	
	/**
	 * Returns a new string that is a substring of delegate string with specified begin index.
	 *  
	 * @param leftIndex
	 * @return
	 */
	public String after(int leftIndex) {
		return between(leftIndex, this.target4Sub.length());
	}
	
	/**
	 * Reset delegate string as the substring target
	 * 
	 * @return
	 */
	public Indexer resetMode() {
		this.isResetMode = true;
		return this;
	}
	
	/**
	 * Reset the result as the substring target
	 * 
	 * @return
	 */
	public Indexer reduceMode() {
		this.isResetMode = false;
		return this;
	}
	
	/**
	 * Reset the specified string as the substring target
	 * 
	 * @return
	 */
	public Indexer reset(String specified) {
		this.target4Sub = checkNotNull(specified);
		return this;
	}
	
	/**
	 * StringIndexOutOfBoundsException exceptions will be ignored
	 * 
	 * @return
	 */
	public Indexer quietly() {
		this.isQuietly = true;
		return this;
	}
	
	/**
	 * Returns the substring target
	 * 
	 * @return
	 */
	public String get() {
		return this.target4Sub;
	}
	
	/**
	 * 
	 */
	private boolean isQuietly = false;
	private boolean isResetMode = false;
	private String target4Sub = null;
	
	private int reviseR(int rightIndex) {
		int len = this.target4Sub.length();
		rightIndex = rightIndex < 0 ? (len + rightIndex) : rightIndex;
		
		if (!isQuietly) {
			return rightIndex;
		}
		
		rightIndex = rightIndex < 0 ? 0 : rightIndex;
		rightIndex = rightIndex > len ? len : rightIndex;
		
		return rightIndex;
	}

	private int reviseL(int leftIndex) {
		int len = this.target4Sub.length();
		leftIndex = leftIndex < 0 ? (len + leftIndex) : leftIndex;
		
		if (!isQuietly) {
			return leftIndex;
		}
		
		leftIndex = leftIndex < 0 ? 0 : leftIndex;
		leftIndex = leftIndex > len ? (((len - 1) < 0) ? 0 : (len - 1)) : leftIndex;
		
		return leftIndex;
	}
	
	@Override protected void updateHandle() {
		this.target4Sub = delegate.get();
	}

	/* (non-Javadoc)
	 * @see com.funcity.me.string.AbstrStrMatcher#THIS()
	 */
	@Override protected Indexer THIS() {
		return this;
	}

}
