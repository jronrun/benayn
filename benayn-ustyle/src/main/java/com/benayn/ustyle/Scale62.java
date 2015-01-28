/**
 * 
 */
package com.benayn.ustyle;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.math.LongMath;

/**
 *
 */
public final class Scale62 {
	
	/**
	 * 
	 */
	private static final LoadingCache<Long, String> scale62PoolS = CacheBuilder.newBuilder()
			.initialCapacity(1000).maximumSize(2000)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<Long, String>() {

				@Override
				public String load(Long key) throws Exception {
					return toS(key, DEFAULT_LEN);
				}

			});
	
	/**
	 * 
	 */
	private static final Pair<Long, String> DEFAULT_SCALE62_VALS = Pair.of(0L, null);
	
	/**
	 * Returns the given 62 scale string's {@link Long} value
	 * 
	 * @param target
	 * @return
	 */
	public static long get(String target) {
		if (DEFAULT_SCALE62_VALS.getR() == target) {
			return DEFAULT_SCALE62_VALS.getL();
		}

		return toL(target);
	}

	/**
	 * Returns the given {@link Long} value's 62 scale string, default minimum string length is 8
	 * 
	 * @param target
	 * @return
	 */
	public static String get(long target) {
		return get(target, DEFAULT_LEN);
	}
	
	/**
	 * Returns the given {@link Long} value's 62 scale string, with given minimum string length
	 * 
	 * @param target
	 * @param minLength
	 * @return
	 */
	public static String get(long target, int minLength) {
		if (DEFAULT_SCALE62_VALS.getL() == target) {
			return DEFAULT_SCALE62_VALS.getR();
		}
		
		if (DEFAULT_LEN == minLength) {
			return scale62PoolS.getUnchecked(target);
		}
		
		return toS(target, minLength);
	}
	
	public static void clearCache() {
		scale62PoolS.invalidateAll();
	}
	
	/**
	 * 
	 */
	public static final String KEYS = "oTu8VgY4PAhjL7Xz9QWmUxBc3S5qFaCsIiOGtZ6yHwDr1JKvdf2EepRbnNklM";
	
	private static Long toL(String value) {
		if (null == value) {
			return 0L;
		}
		
		int idxSep = value.indexOf(SEPARATOR_CHAR);
		if (idxSep > -1) {
			value = value.substring(idxSep + 1);
		}
		
		long r = 0;
		int length = value.length();
		for (int i = 0; i < length; i++) {
			long val = LongMath.pow(RADIX, (length - i - 1));
			char c = value.charAt(i);
			int tmp = KEYS.indexOf(c);
			r += (tmp * val);
		}

		return r;
	}
	
	private static String toS(long value, int totalLen) {
		if (value == 0) {
			return null;
		}
		
		StringBuilder r = new StringBuilder();
		while (value > 0) {
			long val = value % RADIX;
			r.insert(0, KEYS.charAt((int) val));
			value = value / RADIX;
		}
		
		int resultLen = r.length();
		if (resultLen == (totalLen - 1)) {
			r.insert(0, SEPARATOR_CHAR);
		} else {
			String rr = r.toString();
			r.insert(0, SEPARATOR_CHAR).insert(0, fillChar(rr, totalLen - 1 - resultLen));
		}
		
	    return r.toString();
	}

	private static String fillChar(String str, int len) {
		String uuid = Hashing.md5().hashString(str, Charsets.UTF_8).toString().replace('0', 'a');
		return uuid.substring(uuid.length() - len);
	}
	
	private static final char SEPARATOR_CHAR = '0';
	private static final int RADIX = 61;
	private static final int DEFAULT_LEN = 8;
}
