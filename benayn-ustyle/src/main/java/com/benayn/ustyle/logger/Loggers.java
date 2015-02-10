package com.benayn.ustyle.logger;

import static com.benayn.ustyle.Decisions.isClazzExists;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.string.Strs;


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
		return of(target).getInstance();
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
	@SuppressWarnings("unchecked") public <I> I getInstance() {
		return (I) parasitifer.THIS();
	}
	
	/**
	 * Log a message target with INFO level and returns a new wrapped logger instance
	 * 
	 * @param target
	 * @return
	 */
	public static Loggers journal(Object target) {
		return instance().info(target);
	}
	
	/**
	 * Returns a {@link Loggers} instance
	 * 
	 * @return
	 */
	public static Loggers instance() {
	    return of(log);
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
	
	public Loggers trace(Object message) {
        return journalize(message, 'T');
    }
    
    public Loggers debug(Object message) {
        return journalize(message, 'D');
    }
    
    public Loggers info(Object message) {
        return journalize(message, 'I');
    }
    
    public Loggers warn(Object message) {
        return journalize(message, 'W');
    }
    
    public Loggers error(Object message) {
        return journalize(message, 'E');
    }
    
    public Loggers fatal(Object message) {
        return journalize(message, 'F');
    }

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
	
	/**
	 * Log target as JSON
	 * 
	 * @return
	 */
	public Loggers jsonStyle() {
        this.parasitifer.jsonStyle();
        return this;
    }
	
	/**
	 * Log target as formatted JSON
	 * 
	 * @return
	 */
	public Loggers humanStyle() {
        this.parasitifer.humanStyle();
        return this;
    }
	
	/**
	 * Log target as more info string
	 * 
	 * @return
	 */
	public Loggers infoStyle() {
        this.parasitifer.infoStyle();
        return this;
    }
	
	//'T': trace, 'D': debug, 'I': info, 'W': warn, 'E': error, 'F': fatal
	private Loggers journalize(Object message, Character level) {
        this.parasitifer.journalize(level, message);
        return this;
    }
	
	private static Journalize<?> u(Journalize<?> journalize, Object target) {
		journalize.update(target);
		return journalize;
	}
	
	private void intl(Journalize<?> parasitifer) {
		(this.parasitifer = parasitifer).jsonStyle();
	}
	
}