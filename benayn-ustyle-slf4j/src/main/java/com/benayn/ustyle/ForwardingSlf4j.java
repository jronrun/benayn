/**
 * 
 */
package com.benayn.ustyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.benayn.ustyle.logger.Journalize;
import com.benayn.ustyle.string.Strs;

/**
 *
 */
public class ForwardingSlf4j extends Journalize<ForwardingSlf4j> implements Logger {

	/**
	 * 
	 */
	protected Logger delegate = null;

	@Override public String getName() {
		return delegate().getName();
	}

	@Override public boolean isTraceEnabled() {
		return delegate().isTraceEnabled();
	}

	@Override public void trace(String msg) {
		delegate().trace(msg);
	}

	@Override public void trace(String format, Object arg) {
		delegate().trace(format, aStr(arg));
	}

	@Override public void trace(String format, Object arg1, Object arg2) {
		delegate().trace(format, aStr(arg1), aStr(arg2));
	}

	@Override public void trace(String format, Object... arguments) {
		delegate().trace(format, aStrs(arguments));
	}

	@Override public void trace(String msg, Throwable t) {
		delegate().trace(msg, t);
	}

	@Override public boolean isTraceEnabled(Marker marker) {
		return delegate().isTraceEnabled(marker);
	}

	@Override public void trace(Marker marker, String msg) {
		delegate().trace(marker, msg);
	}

	@Override public void trace(Marker marker, String format, Object arg) {
		delegate().trace(marker, format, aStr(arg));
	}

	@Override public void trace(Marker marker, String format, Object arg1, Object arg2) {
		delegate().trace(marker, format, aStr(arg1), aStr(arg2));
	}

	@Override public void trace(Marker marker, String format, Object... argArray) {
		delegate().trace(marker, format, aStrs(argArray));
	}

	@Override public void trace(Marker marker, String msg, Throwable t) {
		delegate().trace(marker, msg, t);
	}

	@Override public boolean isDebugEnabled() {
		return delegate().isDebugEnabled();
	}

	@Override public void debug(String msg) {
		delegate().debug(msg);
	}

	@Override public void debug(String format, Object arg) {
		delegate().debug(format, aStr(arg));
	}

	@Override public void debug(String format, Object arg1, Object arg2) {
		delegate().debug(format, aStr(arg1), aStr(arg2));
	}

	@Override public void debug(String format, Object... arguments) {
		delegate().debug(format, aStrs(arguments));
	}

	@Override public void debug(String msg, Throwable t) {
		delegate().debug(msg, t);
	}

	@Override public boolean isDebugEnabled(Marker marker) {
		return delegate().isDebugEnabled(marker);
	}

	@Override public void debug(Marker marker, String msg) {
		delegate().debug(marker, msg);
	}

	@Override public void debug(Marker marker, String format, Object arg) {
		delegate().debug(marker, format, aStr(arg));
	}

	@Override public void debug(Marker marker, String format, Object arg1, Object arg2) {
		delegate().debug(marker, format, aStr(arg1), aStr(arg2));
	}

	@Override public void debug(Marker marker, String format, Object... argArray) {
		delegate().debug(marker, format, aStrs(argArray));
	}

	@Override public void debug(Marker marker, String msg, Throwable t) {
		delegate().debug(marker, msg, t);
	}

	@Override public boolean isInfoEnabled() {
		return delegate().isInfoEnabled();
	}

	@Override public void info(String msg) {
		delegate().info(msg);
	}

	@Override public void info(String format, Object arg) {
		delegate().info(format, aStr(arg));
	}

	@Override public void info(String format, Object arg1, Object arg2) {
		delegate().info(format, aStr(arg1), aStr(arg2));
	}

	@Override public void info(String format, Object... arguments) {
		delegate().info(format, aStrs(arguments));
	}

	@Override public void info(String msg, Throwable t) {
		delegate().info(msg, t);
	}

	@Override public boolean isInfoEnabled(Marker marker) {
		return delegate().isInfoEnabled(marker);
	}

	@Override public void info(Marker marker, String msg) {
		delegate().info(marker, msg);
	}

	@Override public void info(Marker marker, String format, Object arg) {
		delegate().info(marker, format, aStr(arg));
	}

	@Override public void info(Marker marker, String format, Object arg1, Object arg2) {
		delegate().info(marker, format, aStr(arg1), aStr(arg2));
	}

	@Override public void info(Marker marker, String format, Object... argArray) {
		delegate().info(marker, format, aStrs(argArray));
	}

	@Override public void info(Marker marker, String msg, Throwable t) {
		delegate().info(marker, msg, t);
	}

	@Override public boolean isWarnEnabled() {
		return delegate().isWarnEnabled();
	}

	@Override public void warn(String msg) {
		delegate().warn(msg);
	}

	@Override public void warn(String format, Object arg) {
		delegate().warn(format, aStr(arg));
	}

	@Override public void warn(String format, Object arg1, Object arg2) {
		delegate().warn(format, aStr(arg1), aStr(arg2));
	}

	@Override public void warn(String format, Object... arguments) {
		delegate().warn(format, aStrs(arguments));
	}

	@Override public void warn(String msg, Throwable t) {
		delegate().warn(msg, t);
	}

	@Override public boolean isWarnEnabled(Marker marker) {
		return delegate().isWarnEnabled(marker);
	}

	@Override public void warn(Marker marker, String msg) {
		delegate().warn(marker, msg);
	}

	@Override public void warn(Marker marker, String format, Object arg) {
		delegate().warn(marker, format, aStr(arg));
	}

	@Override public void warn(Marker marker, String format, Object arg1, Object arg2) {
		delegate().warn(marker, format, aStr(arg1), aStr(arg2));
	}

	@Override public void warn(Marker marker, String format, Object... argArray) {
		delegate().warn(marker, format, aStrs(argArray));
	}

	@Override public void warn(Marker marker, String msg, Throwable t) {
		delegate().warn(marker, msg, t);
	}

	@Override public boolean isErrorEnabled() {
		return delegate().isErrorEnabled();
	}

	@Override public void error(String msg) {
		delegate().error(msg);
	}

	@Override public void error(String format, Object arg) {
		delegate().error(format, aStr(arg));
	}

	@Override public void error(String format, Object arg1, Object arg2) {
		delegate().error(format, aStr(arg1), aStr(arg2));
	}

	@Override public void error(String format, Object... arguments) {
		delegate().error(format, aStrs(arguments));
	}

	@Override public void error(String msg, Throwable t) {
		delegate().error(msg, t);
	}

	@Override public boolean isErrorEnabled(Marker marker) {
		return delegate().isErrorEnabled(marker);
	}

	@Override public void error(Marker marker, String msg) {
		delegate().error(marker, msg);
	}

	@Override public void error(Marker marker, String format, Object arg) {
		delegate().error(marker, format, aStr(arg));
	}

	@Override public void error(Marker marker, String format, Object arg1, Object arg2) {
		delegate().error(marker, format, aStr(arg1), aStr(arg2));
	}

	@Override public void error(Marker marker, String format, Object... argArray) {
		delegate().error(marker, format, aStrs(argArray));
	}

	@Override public void error(Marker marker, String msg, Throwable t) {
		delegate().error(marker, msg, t);
	}

	@Override protected Logger delegate() {
		return this.delegate;
	}

	@Override protected void log(char l, String m, Throwable t) {
		switch (l) {
		case 'T': if (null != t) { delegate().trace(m, t); } else { delegate().trace(m); } break;
		case 'D': if (null != t) { delegate().debug(m, t); } else { delegate().debug(m); } break;
		case 'I': if (null != t) { delegate().info(m, t); } else { delegate().info(m); } break;
		case 'W': if (null != t) { delegate().warn(m, t); } else { delegate().warn(m); } break;
		case 'E':
		case 'F': if (null != t) { delegate().error(m, t); } else { delegate().error(m); } break;
		}
	}

	@Override protected boolean isLogEnabled(char l) {
		switch (l) {
		case 'T': return delegate().isTraceEnabled();
		case 'D': return delegate().isDebugEnabled();
		case 'I': return delegate().isInfoEnabled();
		case 'W': return delegate().isWarnEnabled();
		case 'E': 
		case 'F': return delegate().isErrorEnabled();
		default: return false;
		}
	}

	@Override protected ForwardingSlf4j THIS() {
		return this;
	}

	@Override protected ForwardingSlf4j update(Object target) {
		if (Logger.class.isInstance(target)) { this.delegate = (Logger) target; }
		else if (Strs.is(target)) { this.delegate = LoggerFactory.getLogger((String) target); }
		else { this.delegate = LoggerFactory.getLogger(getClazz(target)); }
		return this;
	}

	@Override public boolean isFatalEnabled() {
		return delegate().isErrorEnabled();
	}
	
}