/**
 * 
 */
package com.benayn.ustyle.logger;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.benayn.ustyle.string.Strs.WHITE_SPACE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.benayn.ustyle.Decision;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Strings;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Iterables;

/**
 *
 */
public abstract class Journalize<S> extends ForwardingObject implements Log {

	/**
	 * Log a message with special level
	 * 
	 * @param message
	 */
	protected void journalize(Character l, Object message) {
		journalize(l, message, null);
	}
	
	/**
	 * Log a collection with special level
	 * 
	 * @param collection
	 * @param decision
	 */
	protected <E> void journalize(Character l, Collection<E> collection, Decision<E> decision) {
		journalize(collection, decision, l, isLogEnabled(l), null, -1);
	}

	@Override public void trace(Object msg) {
		journalize('T', msg, null);
	}

	@Override public void trace(Object msg, Throwable t) {
		journalize('T', msg, t);
	}

	@Override public void debug(Object msg) {
		journalize('D', msg, null);
	}

	@Override public void debug(Object msg, Throwable t) {
		journalize('D', msg, t);
	}
	
	@Override public void info(Object msg) {
		journalize('I', msg, null);
	}

	@Override public void info(Object msg, Throwable t) {
		journalize('I', msg, t);
	}

	@Override public void warn(Object msg) {
		journalize('W', msg, null);
	}

	@Override public void warn(Object msg, Throwable t) {
		journalize('W', msg, t);
	}

	@Override public void error(Object msg) {
		journalize('E', msg, null);
	}
	
	@Override public void error(Object msg, Throwable t) {
		journalize('E', msg, t);
	}
	
	@Override public void fatal(Object msg) {
		journalize('F', msg, null);
	}
	
	@Override public void fatal(Object msg, Throwable t) {
		journalize('F', msg, t);
	}
	
	/**
	 * 
	 */
	private class GatherLogDecision<E> implements Decision<E> {
		Character l; boolean enabled; int fmt;
		public GatherLogDecision(Character l, boolean enabled, int fmt) {
			this.l = l; this.enabled = enabled; this.fmt = fmt;
		}

		@Override public boolean apply(E input) {
			if (Entry.class.isInstance(input)) {
				return entryLog(input);
			}
			continueLog(input); return true;
		}
		
		@SuppressWarnings("unchecked")
		private boolean entryLog(E input) {
			Entry<Object, Object> entry = (Entry<Object, Object>) input;
			Object key = wrapping(entry.getKey());
			
			if (Map.class.isInstance(entry.getValue())) {
				return mapLog(key, entry.getValue());
			}
			
			if (Collection.class.isInstance(entry.getValue())) {
				return collLog(key, entry.getValue());
			}
			
			continueLog(key + "=" + wrapping(entry.getValue()));
			return true;
		}

		private boolean collLog(Object key, Object val) {
			@SuppressWarnings("unchecked")
			int size = Iterables.size((Collection<Object>) val);
			continueLog(String.format(inner_start_format, key, "collection", size));
			continueLog(val);
			continueLog(String.format(inner_end_format, key, size));
			return true;
		}

		private boolean mapLog(Object key, Object val) {
			@SuppressWarnings("unchecked")
			int size = Iterables.size(((Map<Object, Object>) val).entrySet());
			continueLog(String.format(inner_start_format, key, "map", size));
			continueLog(val);
			continueLog(String.format(inner_end_format, key, size));
			return true;
		}

		private void continueLog(Object target) {
			journalize(l, enabled, target, null, fmt + 1);
		}
	}
	
	protected <E> void journalize(Character level, Object target, Throwable throwable) {
		journalize(level, isLogEnabled(level), target, throwable, -1);
	}
	
	@SuppressWarnings("unchecked")
	private <E> void journalize(Character l, boolean enabled, Object tgt, Throwable t, int fmt) {
		if (!enabled) { return; }
		if (Strs.is(tgt)) { log(l, gs(fmt) + (String) tgt, t); return;}
		if (null == tgt) { log(l, null, t); return; }
		
		String prev = null;
		boolean logAs = -1 == fmt;
		if (Object[].class.isInstance(tgt)) { 
			if (logAs) { prev = Objects2.defaultTostring(tgt, " array"); }
			tgt = Arrays.asList(((Object[]) tgt)); 
			if (logAs) { log(l, logAs(prev, tgt), t); }
		}
		
		if (Map.class.isInstance(tgt)) {
			if (logAs) { prev = Objects2.defaultTostring(tgt); }
			tgt = Gather.from(((Map<Object, Object>)tgt).entrySet()).list();
			if (logAs) { log(l, logAs(prev, tgt), t); }
		}
		
		if (!Collection.class.isInstance(tgt)) {
			log(l, gs(fmt) + wrapping(tgt), t);
			return;
		} 
		journalize((Collection<E>) tgt, new GatherLogDecision<E>(l, enabled, fmt), l, enabled, t, fmt + 1);
	}
	
	private static final String coll_start = "%s--- start %s detail, size: %s ---";
	private static final String coll_end = "%s--- ended %s detail, size: %s ---";
	private static final String inner_start_format = "%s= (The key mapped value is a %s, below is detail, size: %s) {";
	private static final String inner_end_format = "} (\"%s\" mapped value detail end, size: %s)";
	private final String line_format = Strings.repeat(WHITE_SPACE, 3);
	
	private String gs(int format) {
		//not 1st and 2nd
		if (format != -1 && format != 0) { return Strings.repeat(line_format, format * 3); }
		return EMPTY;
	}
	
	private String logAs(String prev, Object after) {
		return new StringBuilder("Log ").append(prev).append(" as ").append(Objects2.defaultTostring(after)).toString();
	}

	private Object wrapping(Object target) {
		if (null == target) { return target; }
		if (Strs.is(target)) { return target; }
		if (Objects2.is8Type(target.getClass())) { return target; }
		if (BigInteger.class.isInstance(target)) { return target; }
		if (BigDecimal.class.isInstance(target)) { return target; }
		return Objects2.wrapObj(target);
	}

	protected <E> void journalize(Collection<E> collection, 
			Decision<E> decision, Character level, boolean logEnabled, Throwable throwable, int format) {
		if (!logEnabled) { return; }
		if (!Decisions.isEmpty().apply(collection)) {
			int size = collection.size();
			String hexs = Objects2.defaultTostring(collection);
			if (format < 1) { log(level, String.format(coll_start, gs(format), hexs, size), null); }
			Iterables.all(collection, decision);
			if (format < 1) { log(level, String.format(coll_end, gs(format), hexs ,size), null); }
			return;
		}
		
		log(level, "Null or empty collection.", null);
	}
	
	protected String aStr(Object obj) {
		if (obj instanceof String) { return (String) obj; }
		return Objects2.TO_STRING.apply(obj);
	}
	
	protected Object[] aStrs(Object... objs) {
		if (null == objs) { return null; }
		String[] strs = new String[objs.length];
		for (int i = 0; i < objs.length; i++) {
			strs[i] = aStr(objs[i]);
		}
		return strs;
	}
	
	@SuppressWarnings("unchecked")
	public <D> D delegator() {
		return (D) delegate();
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends Object> getClazz(Object target) {
		return Class.class.isInstance(target) ? ((Class<? extends Object>) target) : target.getClass();
	}
	
	//trace, debug, info, warn, error, fatal
	protected abstract void log(char l, String m, Throwable t);
	protected abstract boolean isLogEnabled(char l);
	protected abstract S update(Object target);

	/**
	 * 
	 */
	protected abstract S THIS();
}