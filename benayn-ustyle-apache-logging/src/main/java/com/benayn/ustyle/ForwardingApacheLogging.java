/**
 * 
 */
package com.benayn.ustyle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.benayn.ustyle.logger.Journalize;
import com.benayn.ustyle.string.Strs;

/**
 *
 */
public class ForwardingApacheLogging extends Journalize<ForwardingApacheLogging> implements Log {
	
	protected Log delegate = null;
	
	@Override public boolean isDebugEnabled() {
		return delegate().isDebugEnabled();
	}

	@Override public boolean isErrorEnabled() {
		return delegate().isErrorEnabled();
	}

	@Override public boolean isFatalEnabled() {
		return delegate().isFatalEnabled();
	}

	@Override public boolean isInfoEnabled() {
		return delegate().isInfoEnabled();
	}

	@Override public boolean isTraceEnabled() {
		return delegate().isTraceEnabled();
	}

	@Override public boolean isWarnEnabled() {
		return delegate().isWarnEnabled();
	}

	@Override public void trace(Object message) {
		journalize('T', message, null);
	}

	@Override public void trace(Object message, Throwable t) {
		journalize('T', message, t);
	}

	@Override public void debug(Object message) {
		journalize('D', message, null);
	}

	@Override public void debug(Object message, Throwable t) {
		journalize('D', message, t);
	}

	@Override public void info(Object message) {
		journalize('I', message, null);
	}

	@Override public void info(Object message, Throwable t) {
		journalize('I', message, t);
	}

	@Override public void warn(Object message) {
		journalize('W', message, null);
	}

	@Override public void warn(Object message, Throwable t) {
		journalize('W', message, t);
	}

	@Override public void error(Object message) {
		journalize('E', message, null);
	}

	@Override public void error(Object message, Throwable t) {
		journalize('E', message, t);
	}

	@Override public void fatal(Object message) {
		journalize('F', message, null);
	}

	@Override public void fatal(Object message, Throwable t) {
		journalize('F', message, t);
	}

	@Override protected void log(char l, String m, Throwable t) {
		switch (l) {
		case 'T': if (null != t) { delegate().trace(m, t); } else { delegate().trace(m); } break;
		case 'D': if (null != t) { delegate().debug(m, t); } else { delegate().debug(m); } break;
		case 'I': if (null != t) { delegate().info(m, t); } else { delegate().info(m); } break;
		case 'W': if (null != t) { delegate().warn(m, t); } else { delegate().warn(m); } break;
		case 'E': if (null != t) { delegate().error(m, t); } else { delegate().error(m); } break;
		case 'F': if (null != t) { delegate().fatal(m, t); } else { delegate().fatal(m); } break;
		}
	}

	@Override protected boolean isLogEnabled(char l) {
		switch (l) {
		case 'T': return delegate().isTraceEnabled();
		case 'D': return delegate().isDebugEnabled();
		case 'I': return delegate().isInfoEnabled();
		case 'W': return delegate().isWarnEnabled();
		case 'E': return delegate().isErrorEnabled();
		case 'F': return delegate().isFatalEnabled();
		default: return false;
		}
	}

	@Override protected ForwardingApacheLogging update(Object target) {
		if (Log.class.isInstance(target)) { this.delegate = (Log) target; }
		else if (Strs.is(target)) { this.delegate = LogFactory.getLog((String) target); }
		else { this.delegate = LogFactory.getLog(getClazz(target)); }
		return this;
	}

	@Override protected ForwardingApacheLogging THIS() {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.google.common.collect.ForwardingObject#delegate()
	 */
	@Override protected Log delegate() {
		return this.delegate;
	}

}
