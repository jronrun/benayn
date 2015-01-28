/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Function;

/**
 *
 */
public abstract class DateStyle {
	
	/**
	 * 
	 */
	protected final Log log = Loggers.from(getClass());
	
	/**
	 * yyyy-MM-dd'T'HH:mm:ss.SSS zzz
	 */
	public static final DateStyle ISO = new DateStyle() {
		@Override public String style() {
			return ISO_FORMAT;
		}
	};
	
	/**
	 * yyyyMMddHHmmss
	 */
	public static final DateStyle TIGHT = new DateStyle() {
		@Override public String style() {
			return yyyyMMddHHmmss;
		}
	};
	
	/**
	 * yyyy-MM-dd
	 */
	public static final DateStyle DAY = new DateStyle() {
		@Override public String style() {
			return yyyy_MM_dd;
		}
	};
	
	/**
	 * HH:mm:ss
	 */
	public static final DateStyle CLOCK = new DateStyle() {
		@Override public String style() {
			return HH_mm_ss;
		}
	};
	
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static final DateStyle DEFAULT = new DateStyle() {
		@Override public String style() {
			return yyyy_MM_dd_HH_mm_ss;
		}
	};
	
	/**
	 * Returns a new DateStyle with given date pattern
	 * 
	 * @param datePattern
	 * @return
	 */
	public static DateStyle from(final String datePattern) {
		return new DateStyle() {
			
			@Override public String style() {
				return datePattern;
			}
		};
	}
	
	/**
	 * Returns a date/time pattern
	 * 
	 * @return
	 */
	public abstract String style();
	
	/**
	 * Formats a Date into a date/time string with given pattern
	 * 
	 * @return
	 */
	public Function<Date, String> to() {
		return new Function<Date, String>() {
			@Override public String apply(Date input) { 
				return new SimpleDateFormat(checkNotNull(style())).format(input);
			}
		};
	}
	
	/**
	 * Parses text from the beginning of the given string to produce a date whit given pattern
	 * 
	 * @return
	 */
	public Function<String, Date> from() {
		return new Function<String, Date>() {
			@Override public Date apply(String input) {
				try {
					return new SimpleDateFormat(checkNotNull(style())).parse(input);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
		};
	}
	
	public static final String HH_mm_ss = "HH:mm:ss";
	public static final String yyyy_MM_dd = "yyyy-MM-dd";
	public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
}