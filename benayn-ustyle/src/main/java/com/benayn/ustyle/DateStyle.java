/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * https://github.com/jronrun/benayn
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
     * EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final DateStyle CST = new DateStyle() {
        @Override public String style() {
            return CST_FORMAT;
        }
    };
    
    /**
     * EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final DateStyle GMT = new DateStyle() {
        @Override public String style() {
            return GMT_FORMAT;
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
	 * Returns the locale whose date format symbols should be used
	 * 
	 * @return
	 */
	protected Locale locale() {
	    return null;
	}
	
	/**
	 * Returns the date format symbols to be used for formatting
	 * 
	 * @return
	 */
	protected DateFormatSymbols formatSymbols() {
	    return null;
	}
	
	/**
	 * Formats a Date into a date/time string with given pattern
	 * 
	 * @return
	 */
	public Function<Date, String> to() {
		return TO_STRING_FUNC;
	}
	
	private final Function<Date, String> TO_STRING_FUNC = new Function<Date, String>() {
        @Override public String apply(Date input) { 
            return getDateFormat().format(input);
        }
    };
	
	/**
	 * Parses text from the beginning of the given string to produce a date whit given pattern
	 * 
	 * @return
	 */
	public Function<String, Date> from() {
		return TO_DATE_FUNC;
	}
	
	private final Function<String, Date> TO_DATE_FUNC = new Function<String, Date>() {
        @Override public Date apply(String input) {
            try {
                return getDateFormat().parse(input);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            return null;
        }
    };
	
	/**
	 * Returns the {@link SimpleDateFormat} instance
	 * 
	 * @return
	 */
	protected SimpleDateFormat getDateFormat() {
	    if (dateFormatInst.isPresent()) {
	        return dateFormatInst.get();
	    }
	    
	    Locale locale = null;
	    DateFormatSymbols formatSymbols = null;
	    String pattern = checkNotNull(style());
	    
	    //pattern, locale
	    if (null != (locale = locale())) {
	        return (dateFormatInst = Optional.of(new SimpleDateFormat(pattern, locale))).get();
	    } 
	    
	    //pattern, formatSymbols
	    else if (null != (formatSymbols = formatSymbols())) {
	        return (dateFormatInst = Optional.of(new SimpleDateFormat(pattern, formatSymbols))).get();
	    } 
	    
	    //pattern
	    return (dateFormatInst = Optional.of(new SimpleDateFormat(pattern))).get();
	}
	
	private Optional<SimpleDateFormat> dateFormatInst = Optional.absent();
	
	public static final String HH_mm_ss = "HH:mm:ss";
	public static final String yyyy_MM_dd = "yyyy-MM-dd";
	public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String CST_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
	public static final String GMT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
	
}