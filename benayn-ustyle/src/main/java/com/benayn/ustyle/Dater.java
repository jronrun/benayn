/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import com.benayn.ustyle.inner.Options;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Replacer;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

/**
 * https://github.com/jronrun/benayn
 */
public final class Dater {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Dater.class);
	
	/**
     * Number of milliseconds in a standard second.
     */
    public static final long SECOND = 1000;
    
    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MINUTE = 60 * SECOND;
    
    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long HOUR = 60 * MINUTE;
    
    /**
     * Number of milliseconds in a standard day.
     */
    public static final long DAY = 24 * HOUR;
    
    /**
     * Formats a Date into a date/time string with the {@link DateStyle#DEFAULT}
     * 
     * @param target
     * @return
     */
    public static String asText(Date target) {
    	return of(target).asText();
    }
    
    /**
     * Returns a new {@link Date} instance with given date string
     * 
     * @see #from(String)
     * @param target
     * @return
     */
    public static Date asDate(String target) {
    	return from(target).get();
    }
	
	/**
	 * Returns a new Dater instance with the present time
	 * 
	 * @return
	 */
	public static Dater now() {
		return of(System.currentTimeMillis());
	}
	
	/**
	 * Returns a new Dater instance with the present time and given {@link DateStyle}
	 * 
	 * @param dateStyle
	 * @return
	 */
	public static Dater now(DateStyle dateStyle) {
		return now().with(dateStyle);
	}
	
	/**
	 * Returns a new Dater instance with the present time and given date pattern
	 * 
	 * @param datePattern
	 * @return
	 */
	public static Dater now(String datePattern) {
		return now().with(datePattern);
	}
	
	/**
	 * Returns a new Dater instance with the given date
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(Date date) {
		return new Dater(checkNotNull(date));
	}
	
	/**
	 * Returns a new Dater instance with the given date
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(Long date) {
		return of(new Date(date));
	}
	
	/**
	 * Returns a new Dater instance with the given date, using date pattern: yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(String date) {
		return from(date).with(DateStyle.DEFAULT);
	}
	
	/**
	 * Returns a new Dater instance with the given date
	 * 
	 * <PRE>
	 * 	"2010-01-19 23:59:59",              //date time, "-"
	 * 	"2010-01-19T23:59:59.123456789",    //ISO date time, "-".
	 * 	"2010-01-19",                       //date only, "-".
	 * 	"2010/01/19 23:59:59",              //date time, "/".
	 * 	"2010/01/19T23:59:59.123456789",    //ISO date time, "/".
	 * 	"2010/01/19",                       //date only, "/".
	 * 	"23:59:59",                         //time only.
	 * 	"20100119",                         //date only tight.
	 * 	"235959",                           //time only tight.
	 * 	"20100119235959"                    //date time tight.
	 * </PRE>
	 * 
	 * @param date
	 * @return
	 */
	public static Dater from(String date) {
		return new Dater(checkNotNull(date), analyst(date));
	}
	
	/**
	 * Returns a new Dater instance with the given ISO date, eg: "2013-01-05T01:02:48.28Z"
	 * 
	 * @param date
	 * @return
	 */
	public static Dater iso(String date) {
		//Converting ISO8601-compliant String to java.util.Date
		return of(DatatypeConverter.parseDateTime(
				Strs.WHITESPACE.removeFrom(checkNotNull(date))));
	}
	
	/**
	 * Returns a new Dater instance with the given calendar
	 * 
	 * @param calendar
	 * @return
	 */
	public static Dater of(Calendar calendar) {
		return new Dater(checkNotNull(calendar));
	}
	
	/**
	 * Returns a new Dater instance with the given date and pattern, using date pattern: yyyyMMdd
	 * 
	 * @param date
	 * @return
	 */
	public static Dater shortDay(Date date) {
		return of(date, "yyyyMMdd");
	}
	
	/**
	 * Returns a new Dater instance with the given date and date style
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(Date date, DateStyle dateStyle) {
		return of(date).with(dateStyle);
	}
	
	/**
	 * Returns a new Dater instance with the given date and date style
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(String date, DateStyle dateStyle) {
		return from(date).with(dateStyle);
	}

	/**
	 * Returns a new Dater instance with the given date and pattern
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(Date date, String pattern) {
		return of(date).with(pattern);
	}
	
	/**
	 * Returns a new Dater instance with the given date and pattern
	 * 
	 * @param date
	 * @return
	 */
	public static Dater of(String date, String pattern) {
		return new Dater(checkNotNull(date), checkNotNull(pattern));
	}
	
	/**
	 * Changes the date style with the given date pattern
	 * 
	 * @param datePattern
	 * @return
	 */
	public Dater with(final String datePattern) {
		return with(DateStyle.from(checkNotNull(datePattern)));
	}
	
	/**
	 * Changes the date style with the given date style
	 * 
	 * @param dateStyle
	 * @return
	 */
	public Dater with(DateStyle dateStyle) {
		this.style = checkNotNull(dateStyle);
		return this;
	}
	
	/**
	 * Returns a new Comparer that target date milliseconds minus the delegate date milliseconds as comparison object
	 * 
	 * @param targetDate
	 * @return
	 */
	public Comparer<Long> until(String targetDate) {
		return until(this.style.from().apply(targetDate));
	}
	
	/**
	 * Returns a new Comparer that target date milliseconds minus the delegate date milliseconds as comparison object
	 * 
	 * @param targetDate
	 * @return
	 */
	public Comparer<Long> until(Date targetDate) {
		return Comparer.of(untilMillis(targetDate));
	}
	
	/**
	 * Returns target date milliseconds minus the delegate date milliseconds
	 * 
	 * @param targetDate
	 * @return
	 */
	public Long untilMillis(String targetDate) {
		return untilMillis(this.style.from().apply(targetDate));
	}
	
	/**
	 * Returns target date milliseconds minus the delegate date milliseconds
	 * 
	 * @param targetDate
	 * @return
	 */
	public Long untilMillis(Date targetDate) {
		return targetDate.getTime() - this.target.getTime();
	}
	
	/**
	 * Returns a new Comparer that delegate date milliseconds minus the target date milliseconds as comparison object
	 * 
	 * @param targetDate
	 * @return
	 */
	public Comparer<Long> since(String targetDate) {
		return since(this.style.from().apply(targetDate));
	}
	
	/**
	 * Returns a new Comparer that delegate date milliseconds minus the target date milliseconds as comparison object
	 * 
	 * @param targetDate
	 * @return
	 */
	public Comparer<Long> since(Date targetDate) {
		return Comparer.of(sinceMillis(targetDate));
	}
	
	/**
	 * Returns delegate date milliseconds minus the target date milliseconds
	 * 
	 * @param targetDate
	 * @return
	 */
	public Long sinceMillis(String targetDate) {
		return sinceMillis(this.style.from().apply(targetDate));
	}
	
	/**
	 * Returns delegate date milliseconds minus the target date milliseconds
	 * 
	 * @param targetDate
	 * @return
	 */
	public Long sinceMillis(Date targetDate) {
		return this.target.getTime() - targetDate.getTime();
	}
	
	/**
	 * Returns the current using date style
	 * 
	 * @return
	 */
	public DateStyle using() {
		return this.style;
	}
	
	/**
	 * Formats a Date into a date/time string with the given style
	 * 
	 * @return
	 */
	public String asText() {
		return this.style.to().apply(this.target);
	}
	
	/**
	 * Formats a Date into a date/time string with the "yyyy-MM-dd HH:mm:ss" style
	 * 
	 * @return
	 */
	public String asDefaultText() {
	    return asText(DateStyle.DEFAULT);
	}
	
	/**
	 * Formats a Date into a date/time string with the "yyyyMMddHHmmss" style
	 * 
	 * @return
	 */
	public String asTightText() {
		return asText(DateStyle.TIGHT);
	}
	
	/**
	 * Formats a Date into a date/time string with the "yyyyMMdd" style
	 * 
	 * @return
	 */
	public String asDayTightText() {
		return asText(DateStyle.DAY_TIGHT);
	}
	
	/**
	 * Formats a Date into a date/time string with the "yyyy-MM-dd" style
	 * 
	 * @return
	 */
	public String asDayText() {
		return asText(DateStyle.DAY);
	}
	
	/**
	 * Formats a Date into a date/time string with the "HH:mm:ss" style
	 * 
	 * @return
	 */
	public String asClockText() {
		return asText(DateStyle.CLOCK);
	}
	
	/**
	 * Formats a Date into a date/time string with the given style without change the previous style
	 * 
	 * @param dateStyle
	 * @return
	 */
	public String asText(String dateStyle) {
		return asText(DateStyle.from(checkNotNull(dateStyle)));
	}
	
	/**
	 * Formats a Date into a date/time string with the given style without change the previous style
	 * 
	 * @param dateStyle
	 * @return
	 */
	public String asText(DateStyle dateStyle) {
		DateStyle prevStyle = this.style;
		
		checkNotNull(dateStyle).setLocale(prevStyle.locale());
		dateStyle.setFormatSymbols(prevStyle.formatSymbols());
		String dayText = with(dateStyle).asText();
		with(prevStyle);
		return dayText;
	}
	
	/**
	 * Convert the delegate date into a {@code Calendar}
	 * 
	 * @return
	 */
	public Calendar asCalendar() {
		return asCalendar(true);
	}
	
	/**
	 * Convert the delegate date into a {@code Calendar}
	 * 
	 * @param lenient
	 * @return
	 */
	public Calendar asCalendar(boolean lenient) {
		intlCalendar(lenient);
		return this.delegate.get();
	}
	
	/**
	 * Returns the delegate date that applies the previous operate
	 * 
	 * @return
	 */
	public Date get() {
		return this.target;
	}
	
	/**
	 * Returns the new {@link Date} that applies the {@link DateStyle}
	 * 
	 * @return
	 */
	public Date asDate() {
		return from(asText()).get();
	}
	
	/**
	 * Returns the delegate date is today
	 * 
	 * @see #isSameDay(Calendar)
	 * @return
	 */
	public boolean isToday() {
		return isSameDay(Calendar.getInstance());
	}
	
	/**
	 * Returns a AddsOrSets instance that add the specified field to a delegate date
	 * 
	 * @return
	 */
	public DateUnit add() {
		if (this.add.isPresent()) { return add.get(); }
		
		return (this.add = Optional.of((DateUnit) new DateUnit(this) {
			
			@Override protected DateUnit handle(int calendarField, int amount) {
				Calendar c = asCalendar();
				c.add(calendarField, amount);
				target = c.getTime();
		        return this;
			}

		})).get();
	}
	
	/**
	 * Sets the hour, minute, second to the delegate date
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public Dater setClock(int hour, int minute, int second) {
		return set().hours(hour).minutes(minute).second(second);
	}
	
	/**
	 * Sets the hour, minute, second to the delegate date, format is {@link DateStyle#CLOCK}
	 * 
	 * @see #setClock(int, int, int)
	 * @see DateStyle#CLOCK
	 * @param clock
	 * @return
	 */
	public Dater setClock(String clock) {
		String tip = "clock format must HH:mm:ss";
		checkArgument(checkNotNull(clock).length() == 8, tip);
		List<String> pieces = Splitter.on(":").splitToList(clock);
		checkArgument(pieces.size() == 3, tip);
		return setClock(Ints.tryParse(pieces.get(0)), Ints.tryParse(pieces.get(1)), Ints.tryParse(pieces.get(2)));
	}
	
	/**
	 * Returns a AddsOrSets instance that set the specified field to a delegate date
	 * 
	 * @return
	 */
	public DateUnit set() {
		if (this.set.isPresent()) { return set.get(); }
		
		return (this.set = Optional.of((DateUnit) new DateUnit(this) {
			
			@Override protected DateUnit handle(int calendarField, int amount) {
				Calendar c = asCalendar();
				c.set(calendarField, amount);
				target = c.getTime();
		        return this;
			}

		})).get();
	}
	
	protected Optional<DateUnit> add = Optional.absent();
	protected Optional<DateUnit> set = Optional.absent();
	
	/**
	 * Checks if the target date in this delegate date 
	 * 
	 * @param theTargetDate
	 * @return
	 */
	public boolean inThisDay(Date theTargetDate) {
		Date[] thisDay = asDayRange();
		return theTargetDate.compareTo(thisDay[0]) >= 0 && theTargetDate.compareTo(thisDay[1]) <= 0;
	}
	
	/**
	 * Tests if delegate date is before the given clock, format is {@link DateStyle#CLOCK}
	 * 
	 * @param clock
	 * @return
	 */
	public boolean beforeClock(String clock) {
		return pkClock(clock, 'b');
	}
	
	/**
	 * Tests if delegate date is before or meanwhile the given clock, format is {@link DateStyle#CLOCK}
	 * 
	 * @param clock
	 * @return
	 */
	public boolean beforeOrSameClock(String clock) {
		return pkClock(clock, 'c');
	}
	
	/**
	 * Tests if delegate date is after the given clock, format is {@link DateStyle#CLOCK}
	 * 
	 * @param clock
	 * @return
	 */
	public boolean afterClock(String clock) {
		return pkClock(clock, 'a');
	}
	
	/**
	 * Tests if delegate date is after or meanwhile the given clock, format is {@link DateStyle#CLOCK}
	 * 
	 * @param clock
	 * @return
	 */
	public boolean afterOrSameClock(String clock) {
		return pkClock(clock, 'z');
	}
	
	/**
	 * Tests if delegate date is meanwhile the given clock, format is {@link DateStyle#CLOCK}
	 * 
	 * @param clock
	 * @return
	 */
	public boolean sameClock(String clock) {
		return pkClock(clock, 's');
	}
	
	/**
	 * Checks if the delegate date clock in the given clock range
	 * 
	 * @param startClock
	 * @param endClock
	 * @return
	 */
	public boolean inClock(String startClock, String endClock) {
		return beforeOrSameClock(endClock) && afterOrSameClock(startClock);
	}
	
	/**
	 * Checks if this delegate date in the given date 
	 * 
	 * @param theGivenDate
	 * @return
	 */
	public boolean inGivenDay(Date theGivenDate) {
		Date[] givenDay = of(theGivenDate).asDayRange();
		return this.target.compareTo(givenDay[0]) >= 0 && this.target.compareTo(givenDay[1]) <= 0;
	}
	
	/**
	 * Returns the beginning clock and the ending clock date string array
	 * 
	 * @return
	 */
	public String[] asDayRangeText() {
		return asRangeText("00:00:00", "23:59:59");
	}
	
	/**
	 * Returns the given beginning clock and the given ending clock date string array
	 * 
	 * @param beginClock
	 * @param endClock
	 * @return
	 */
	public String[] asRangeText(String beginClock, String endClock) {
		String thisDay = null;
		return new String[]{
				of((thisDay = (asDayText() + " ")) + beginClock).with(style).asText(), 
				of(thisDay + endClock).with(style).asText()
			};
	}
	
	/**
	 *  Returns the beginning clock and the ending clock date array
	 *  
	 * @return
	 */
	public Date[] asDayRange() {
		return asRange("00:00:00", "23:59:59");
	}
	
	/**
	 * Returns the given beginning clock and the given ending clock date array
	 * 
	 * @param beginClock
	 * @param endClock
	 * @return
	 */
	public Date[] asRange(String beginClock, String endClock) {
		String thisDay = null;
		return new Date[]{of((thisDay = (asDayText() + " ")) + beginClock).get(), of(thisDay + endClock).get()};
	}
	
	/**
	 *  Returns the new {@link Date} that with the beginning clock
	 *  
	 * @return
	 */
	public Date asDayBegin() {
		return asDayRange()[0];
	}
	
	/**
	 *  Returns the new {@link Date} that with the ending clock
	 *  
	 * @return
	 */
	public Date asDayEnd() {
		return asDayRange()[1];
	}
	
	/**
	 * Sets the interval describe
	 * 
	 * @param intervalDesc
	 * @return
	 */
	public Dater setIntervalDesc(IntervalDesc intervalDesc) {
		theIntervalDesc = Optional.of(checkNotNull(intervalDesc));
		return this;
	}
	
	/**
	 * Sets the {@link Locale#ENGLISH} to this date style
	 * 
	 * @return
	 */
	public Dater english() {
		return setLocale(Locale.ENGLISH);
	}
	
	/**
	 * Sets the {@link Locale#CHINESE} to this date style
	 * 
	 * @return
	 */
	public Dater chinese() {
		return setLocale(Locale.CHINESE);
	}
	
	/**
	 * Sets the {@link Locale} to this date style
	 * 
	 * @see DateStyle#setLocale(Locale)
	 * @param locale
	 * @return
	 */
	public Dater setLocale(Locale locale) {
		using().setLocale(locale);
		return this;
	}
	
	/**
	 * Sets the {@link DateFormatSymbols} to this date style
	 * 
	 * @see DateStyle#setFormatSymbols(DateFormatSymbols)
	 * @param dateFormatSymbols
	 * @return
	 */
	public Dater setFormatSymbols(DateFormatSymbols dateFormatSymbols) {
		using().setFormatSymbols(dateFormatSymbols);
		return this;
	}
	
	/**
	 * Returns the interval describe since given date to the delegate date
	 * 
	 * @param target
	 * @param desc
	 * @return
	 */
	public String interval(Date target) {
		double unit = 1000.0D;
		double dayUnit = DAY / unit;
		double hourUnit = HOUR / unit;
		double minUnit = MINUTE / unit;
		double interval = sinceMillis(target) / unit;
		IntervalDesc desc = theIntervalDesc.get();
		
		if (interval >= 0.0D) {
			if (interval / (12 * 30 * dayUnit) > 1.0D) {
				return asText(target);
			}
			if (interval / (30 * dayUnit) > 1.0D) {
				return String.format("%s%s", (int) (interval / (30 * dayUnit)), desc.getMonthAgo());
			}
			if (interval / (7 * dayUnit) > 1.0D) {
				return String.format("7%s", desc.getDayAgo());
			}
			if ((interval / (7 * dayUnit) <= 1.0D) && (interval / dayUnit >= 1.0D)) {
				return String.format("%s%s", (int) (interval / dayUnit), desc.getDayAgo());
			}
			if ((interval / dayUnit < 1.0D) && (interval / hourUnit >= 1.0D)) {
				return String.format("%s%s", (int) (interval / hourUnit), desc.getHourAgo());
			}
			if ((interval < hourUnit) && (interval >= minUnit)) {
				return String.format("%s%s", (int) (interval / minUnit), desc.getMinuteAgo());
			}
			
			return desc.getJustNow();
		}
		
		return asText(target);
	}
	
	/**
	 * Tests if delegate date is before or same as the given date string with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean beforeOrMeanwhile(String target) {
		return pk(target, 'c');
	}
	
	/**
	 * Tests if delegate date is before or same as the given date with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean beforeOrMeanwhile(Date target) {
		return pk(target, 'c');
	}
	
	/**
	 * Tests if delegate date is before the given date string with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean before(String target) {
		return pk(target, 'b');
	}
	
	/**
	 * Tests if delegate date is before the given date with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean before(Date target) {
		return pk(target, 'b');
	}
	
	/**
	 * Tests if delegate date is same as the given date string with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean meanwhile(String target) {
		return pk(target, 's');
	}
	
	/**
	 * Tests if delegate date is same as the given date with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean meanwhile(Date target) {
		return pk(target, 's');
	}
	
	/**
	 * Tests if delegate date is after or same as the given date string with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean afterOrMeanwhile(String target) {
		return pk(target, 'z');
	}
	
	/**
	 * Tests if delegate date is after or same as the given date with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean afterOrMeanwhile(Date target) {
		return pk(target, 'z');
	}
	
	/**
	 * Tests if delegate date is after the given date string with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean after(String target) {
		return pk(target, 'a');
	}
	
	/**
	 * Tests if delegate date is after the given date with same date style
	 * 
	 * @param target
	 * @return
	 */
	public boolean after(Date target) {
		return pk(target, 'a');
	}
	
	/**
	 * Returns the new {@link Date} that applied the using {@link DateStyle}
	 * 
	 * @param target
	 * @return
	 */
	public Date applyStyle(Date target) {
		return of(target, using()).asDate();
	}
	
	/**
	 * Returns the new {@link Date} that applied the using {@link DateStyle}
	 * 
	 * @param target
	 * @return
	 */
	public Date applyStyle(String target) {
		return from(target).with(using()).asDate();
	}
	
	/**
	 * 
	 */
	public abstract class DateUnit extends Options<Dater, DateUnit> {
		
		private DateUnit(Dater dater) {
			this.reference(dater, this);
		}

		/**
		 * Plus or minus or set a number of years to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater year(int amount) {
			return years(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of months to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater month(int amount) {
			return months(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of weeks to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater week(int amount) {
			return weeks(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of days to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater day(int amount) {
			return days(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of hours to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater hour(int amount) {
			return hours(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of minutes to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater minute(int amount) {
			return minutes(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of seconds to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater second(int amount) {
			return seconds(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of milliseconds to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public Dater millisecond(int amount) {
			return milliseconds(amount).outerRef;
		}
		
		/**
		 * Plus or minus or set a number of years to a delegate date
		 * 
		 * @param amount
		 * @return
		 */
		public DateUnit years(int amount) {
	        return handle(Calendar.YEAR, amount);
	    }

	    /**
	     * Plus or minus or set a number of months to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit months(int amount) {
	        return handle(Calendar.MONTH, amount);
	    }

	    /**
	     * Plus or minus or set a number of weeks to a delegate date.
	     * Sunday is the first day of week.
	     * 
	     * @see Calendar#DAY_OF_WEEK
	     * @param amount
	     * @return
	     */
		public DateUnit weeks(int amount) {
	        return handle(Calendar.DAY_OF_WEEK, amount);
	    }

	    /**
	     * Plus or minus or set a number of days to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit days(int amount) {
	        return handle(Calendar.DAY_OF_MONTH, amount);
	    }

	    /**
	     * Plus or minus or set a number of hours to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit hours(int amount) {
	        return handle(Calendar.HOUR_OF_DAY, amount);
	    }

	    /**
	     * Plus or minus or set a number of minutes to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit minutes(int amount) {
	        return handle(Calendar.MINUTE, amount);
	    }

	    /**
	     * Plus or minus or set a number of seconds to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit seconds(int amount) {
	        return handle(Calendar.SECOND, amount);
	    }

	    /**
	     * Plus or minus or set a number of milliseconds to a delegate date
	     * 
	     * @param amount
	     * @return
	     */
		public DateUnit milliseconds(int amount) {
	        return handle(Calendar.MILLISECOND, amount);
	    }
		
		/**
		 * @see Dater#get()
		 */
		public Date get() {
			return this.outerRef.get();
		}
		
		protected abstract DateUnit handle(int calendarField, int amount);
	}
	
	/**
	 * @see Date#getTime()
	 * @return
	 */
	public long getTime() {
	    return get().getTime();
	}
	
	/**
	 * Returns the year of the delegate date
	 * 
	 * @see Calendar.YEAR
	 * @return
	 */
	public int getYear() {
	    return asCalendar().get(Calendar.YEAR);
	}
	
	/**
     * Returns the month of the delegate date.
     * The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0; 
     * the last depends on the number of months in a year.
     * 
     * @see Calendar.MONTH
     * @return
     */
	public int getMonth() {
	    return asCalendar().get(Calendar.MONTH);
	}
	
	/**
	 * Returns the month description of the delegate date.
	 * 
	 * @return
	 */
	public String getMonthText() {
		return asText(DateStyle.MONTH);
	}
	
	/**
	 * Returns the week of the delegate date.
	 * Sunday is the first day of the week
	 * 
	 * @see Calendar.DAY_OF_WEEK
	 * @return
	 */
	public int getWeek() {
	    return asCalendar().get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * Checks if the delegate is Sunday
	 * 
	 * @return
	 */
	public boolean isSunday() {
		return Calendar.SUNDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Monday
	 * 
	 * @return
	 */
	public boolean isMonday() {
		return Calendar.MONDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Tuesday
	 * 
	 * @return
	 */
	public boolean isTuesday() {
		return Calendar.TUESDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Wednesday
	 * 
	 * @return
	 */
	public boolean isWednesday() {
		return Calendar.WEDNESDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Thursday
	 * 
	 * @return
	 */
	public boolean isThursday() {
		return Calendar.THURSDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Friday
	 * 
	 * @return
	 */
	public boolean isFriday() {
		return Calendar.FRIDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Saturday
	 * 
	 * @return
	 */
	public boolean isSaturday() {
		return Calendar.SATURDAY == getWeek();
	}
	
	/**
	 * Checks if the delegate is Weekend
	 * 
	 * @return
	 */
	public boolean isWeekend() {
		return isSaturday() || isSunday();
	}
	
	/**
	 * Returns the week description of the delegate date.
	 * 
	 * @return
	 */
	public String getWeekText() {
		return asText(DateStyle.WEEK);
	}
	
	/**
     * Returns the day of the delegate date
     * 
     * @see Calendar.DAY_OF_MONTH
     * @return
     */
	public int getDay() {
	    return asCalendar().get(Calendar.DAY_OF_MONTH);
	}
	
	/**
     * Returns the hour of the delegate date
     * 
     * @see Calendar.HOUR_OF_DAY
     * @return
     */
	public int getHour() {
	    return asCalendar().get(Calendar.HOUR_OF_DAY);
	}
	
	/**
     * Returns the minute of the delegate date
     * 
     * @see Calendar.MINUTE
     * @return
     */
	public int getMinute() {
	    return asCalendar().get(Calendar.MINUTE);
	}
	
	/**
     * Returns the second of the delegate date
     * 
     * @see Calendar.SECOND
     * @return
     */
	public int getSecond() {
	    return asCalendar().get(Calendar.SECOND);
	}
	
	/**
     * Returns the millisecond of the delegate date
     * 
     * @see Calendar.MILLISECOND
     * @return
     */
	public int getMillisecond() {
	    return asCalendar().get(Calendar.MILLISECOND);
	}
	
	/**
	 * Checks if two date objects represent the same instant in time
	 * 
	 * @param date
	 * @return
	 */
	public boolean isSameInstant(Date date) {
		return get().getTime() == checkNotNull(date).getTime();
	}
	
	/**
	 * Checks if two calendar objects represent the same instant in time
	 * 
	 * @param calendar
	 * @return
	 */
	public boolean isSameInstant(Calendar calendar) {
		return get().getTime() == checkNotNull(calendar).getTime().getTime();
	}
	
	/**
	 * Checks if two date objects are on the same day ignoring time
	 * 
	 * @param date
	 * @return
	 */
	public boolean isSameDay(Date date) {
		return isSameDay(toCalendar(checkNotNull(date), true));
	}
	
	/**
	 * Checks if two calendar objects are on the same day ignoring time.
	 * 
	 * @param calendar
	 * @return
	 */
	public boolean isSameDay(Calendar calendar) {
		Calendar c1 = asCalendar();
		return (c1.get(Calendar.ERA) == calendar.get(Calendar.ERA) &&
				c1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
				c1.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR));
	}
	
	private boolean pk(Object target, char expression) {
		Date thiz = asDate();
		Date that = (target instanceof Date) ? applyStyle((Date) target) : applyStyle((String) target);
		switch (expression) {
		case 'b': return thiz.before(that);
		case 'c': return thiz.before(that) || thiz.equals(that);
		case 'a': return thiz.after(that);
		case 'z': return thiz.after(that) || thiz.equals(that);
		case 's': return thiz.equals(that);
		}
		
		return false;
	}
	
	private boolean pkClock(String clock, char expression) {
		return pk(of(get()).setClock(clock).get(), expression);
	}
	
	private void intlCalendar(boolean lenient) {
		if (this.delegate.isPresent() && isSameInstant(this.target)) {
			return;
		}
		
		this.delegate = Optional.of(toCalendar(this.target, lenient));
	}

	private Calendar toCalendar(Date date, boolean lenient) {
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(lenient);
		calendar.setTime(date);
		return calendar;
	}
	
	/**
	 * Analyst date time string
	 * 
	 * @param date
	 * @return
	 */
	private static String analyst(String date) {
		String style = null;
		boolean hasDiagonal = false;
		Replacer r = Replacer.of(checkNotNull(date));
		if (hasDiagonal = r.contain("/")) {
			r.update(r.lookups("/").with("-"));
		}
		
		//ISO
		if (r.containAll(".", "T")) {
			style = DateStyle.ISO_FORMAT;
		}
		//CST
		else if (r.contain("CST")) {
		    style = DateStyle.CST_FORMAT;
		}
		//GMT
		else if (r.contain("GMT")) {
		    style = DateStyle.GMT_FORMAT;
		}
		//analyst
		else {
		    switch (date.length()) {
	        case 6: style = "HHmmss"; break;
	        case 8: style = r.contain(":") ? DateStyle.HH_mm_ss : DateStyle.yyyyMMdd; break;
	        case 10: style = DateStyle.yyyy_MM_dd; break;
	        case 14: style = DateStyle.yyyyMMddHHmmss; break;
	        case 19: style = DateStyle.yyyy_MM_dd_HH_mm_ss; break;
	        }
		}
		
		return hasDiagonal ? r.set(style).lookups("-").with("/") : style;
	}
	
	/**
	 * 
	 */
	private Optional<IntervalDesc> theIntervalDesc = 
			Optional.of(new IntervalDesc(" months ago", " days ago", " hours ago", " minutes ago", "just now")); 
	
	protected Date target = null;
	protected DateStyle style = null;
	protected Optional<Calendar> delegate = Optional.absent();
	
	/**
	 * 
	 */
	private Dater(Date date) {
		this.target = date;
		this.style = DateStyle.DEFAULT;
	}
	
	private Dater(String date, String pattern) {
		this.style = Strs.isBlank(pattern) ? DateStyle.DEFAULT : DateStyle.from(pattern);
		this.target = style.from().apply(date);
	}

	private Dater(Calendar calendar) {
		this(calendar.getTime());
		this.delegate = Optional.of(calendar);
	}
	
	public static class IntervalDesc {
		private String monthAgo;
		private String dayAgo;
		private String hourAgo;
		private String minuteAgo;
		private String justNow;
		
		public IntervalDesc(String monthAgo, String dayAgo, String hourAgo, String minuteAgo, String justNow) {
			this.monthAgo = monthAgo;
			this.dayAgo = dayAgo;
			this.hourAgo = hourAgo;
			this.minuteAgo = minuteAgo;
			this.justNow = justNow;
		}
		
		public String getMonthAgo() {
			return monthAgo;
		}
		public void setMonthAgo(String monthAgo) {
			this.monthAgo = monthAgo;
		}
		public String getDayAgo() {
			return dayAgo;
		}
		public void setDayAgo(String dayAgo) {
			this.dayAgo = dayAgo;
		}
		public String getHourAgo() {
			return hourAgo;
		}
		public void setHourAgo(String hourAgo) {
			this.hourAgo = hourAgo;
		}
		public String getMinuteAgo() {
			return minuteAgo;
		}
		public void setMinuteAgo(String minuteAgo) {
			this.minuteAgo = minuteAgo;
		}
		public String getJustNow() {
			return justNow;
		}
		public void setJustNow(String justNow) {
			this.justNow = justNow;
		}
		
	}
	
}
