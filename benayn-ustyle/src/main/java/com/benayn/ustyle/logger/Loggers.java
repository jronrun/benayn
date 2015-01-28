package com.benayn.ustyle.logger;

import static com.benayn.ustyle.Decisions.isClazzExists;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.benayn.ustyle.Decision;
import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Optional;


public final class Loggers {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Loggers.class);
	
	/**
	 * Returns a new wrapped logger instance
	 * 
	 * @see Loggers#of(Object)
	 * @param target
	 * @return
	 */
	public static <I> I from(Object target) {
		return of(target).instance();
	}
	
	/**
	 * Returns a new wrapped logger instance
	 * <p>Custom log delegate: use "cbu.log.delegate" as key, and the full class name as value, 
	 * the class which is a custom log delegate class that extends of {@link Journalize}. eg:
	 * <pre>
	 * package com.custom.journalize.usage
	 * 
	 * public class CustomJournalize extend Journalize<CustomJournalize> {
	 *  	...
	 * }
	 * </pre>
	 * Then at the system initializing, set the custom log delegate like below:
	 * <pre>
	 * System.setProperty("cbu.log.delegate", "com.custom.journalize.usage.CustomJournalize");
	 * </pre>
	 * 
	 * @see System#setProperty(String, String)
	 * @param target
	 * @return
	 */
	public static Loggers of(Object target) {
		return new Loggers(target);
	}
	
	/**
	 * Initializes a newly {@link Loggers} instance
	 * 
	 * @param target
	 */
	private Loggers(Object target) {
		intl(Journalize.class.isInstance(target) ? ((Journalize<?>) target) : asJournalizer(target));
	}

	/**
	 * Returns a named logger instance
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked") public <I> I instance() {
		return (I) parasitifer.THIS();
	}
	
	/**
	 * Log a message
	 * 
	 * @param message
	 */
	public Loggers log(Object message) {
		this.parasitifer.journalize(this.lev, message);
		return this;
	}
	
	/**
	 * Log a collection with custom decision
	 * 
	 * @param collection
	 * @param decision
	 * @return
	 */
	public <T> Loggers log(Collection<T> collection, Decision<T> decision) {
		this.parasitifer.journalize(this.lev, collection, decision);
		return this;
	}
	
	/**
	 * Set TRACE log level
	 * 
	 * @return
	 */
	public Loggers trace() {
		return chgL('T');
	}
	
	/**
	 * Set DEBUG log level
	 * 
	 * @return
	 */
	public Loggers debug() {
		return chgL('D');
	}
	
	/**
	 * Set INFO log level
	 * 
	 * @return
	 */
	public Loggers info() {
		return chgL('I');
	}
	
	/**
	 * Set WARN log level
	 * 
	 * @return
	 */
	public Loggers warn() {
		return chgL('W');
	}
	
	/**
	 * Set ERROR log level
	 * 
	 * @return
	 */
	public Loggers error() {
		return chgL('E');
	}
	
	/**
	 * Sets the log level with given level, default is INFO
	 * 'T': trace, 'D': debug, 'I': info, 'W': warn, 'E': error, 'F': fatal
	 * 
	 * @param level
	 * @return
	 */
	public Loggers chgL(char level) {
		this.lev = level;
		return this;
	}
	
	/**
	 * Log a preinstall message
	 * 
	 * @return
	 */
	public Loggers journal() {
		if (this.preinstallation.isPresent()) {
			this.parasitifer.journalize(this.lev, this.preinstallation.get());
		}
		
		return this;
	}
	
	/**
	 * Log a message target and returns a new wrapped logger instance
	 * 
	 * @param target
	 * @return
	 */
	public static Loggers journal(Object target) {
		return of(log).log(target);
	}
	
	/**
	 * Preinstall a log message
	 * 
	 * @param message
	 * @return
	 */
	public Loggers install(Object message) {
		this.preinstallation = Optional.fromNullable(message);
		return this;
	}
	
	/**
	 * 
	 */
	public static class ZDefaultJournalizer extends Journalize<ZDefaultJournalizer> {

		@Override public boolean isDebugEnabled() {
			return isLogEnabled('D');
		}

		@Override public boolean isErrorEnabled() {
			return isLogEnabled('E');
		}

		@Override public boolean isFatalEnabled() {
			return isLogEnabled('F');
		}

		@Override public boolean isInfoEnabled() {
			return isLogEnabled('I');
		}

		@Override public boolean isTraceEnabled() {
			return isLogEnabled('T');
		}

		@Override public boolean isWarnEnabled() {
			return isLogEnabled('W');
		}

		@Override protected void log(char l, String m, Throwable t) {
			Level ll = getL(l);
			if (null != t) { delegate().log(ll, m, t); } else { delegate().log(ll, m); }
		}

		@Override protected boolean isLogEnabled(char l) {
			return delegate().isLoggable(getL(l));
		}

		protected Logger delegate = null;
		
		@Override protected ZDefaultJournalizer update(Object target) {
			if (Logger.class.isInstance(target)) { this.delegate = (Logger) target; }
			else if (Strs.is(target)) { this.delegate = Logger.getLogger((String) target); }
			else { this.delegate = Logger.getLogger(getClazz(target).getName()); }
			return this;
		}

		@Override protected ZDefaultJournalizer THIS() {
			return this;
		}

		/* (non-Javadoc)
		 * @see com.google.common.collect.ForwardingObject#delegate()
		 */
		@Override protected Logger delegate() {
			return this.delegate;
		}
		
		private Level getL(Character l) {
			switch (l) {
			case 'T': return Level.FINEST;
			case 'D': return Level.FINE;
			case 'I': return Level.INFO;
			case 'W': return Level.WARNING;
			case 'E': 
			case 'F': return Level.SEVERE;
			default: return null;
			}
		}
	}
	
	/**
	 * Forwarding Journalizer
	 */
	private Journalize<?> parasitifer;
	private Optional<Object> preinstallation;
	private Character lev = 'I';

	private static Journalize<?> asJournalizer(Object target) {
		String prop = null;
		if ((prop = System.getProperty("cbu.log.delegate")) != null) {
			return u(Suppliers2.<Journalize<?>>toInstance(prop).get(), target);
		} 
		
		prop = "com.benayn.ustyle.ForwardingSlf4j";
		if (isClazzExists("org.slf4j.Logger") && isClazzExists(prop)) {
			return u(Suppliers2.<Journalize<?>>toInstance(prop).get(), target);
		} 

		prop = "com.benayn.ustyle.ForwardingApacheLogging";
		if (isClazzExists("org.apache.commons.logging.Log") && isClazzExists(prop)) {
			return u(Suppliers2.<Journalize<?>>toInstance(prop).get(), target);
		} 

		return u(new ZDefaultJournalizer(), target);
	}
	
	private static Journalize<?> u(Journalize<?> journalize, Object target) {
		journalize.update(target);
		return journalize;
	}
	
	private void intl(Journalize<?> parasitifer) {
		this.parasitifer = parasitifer;
	}
	
}