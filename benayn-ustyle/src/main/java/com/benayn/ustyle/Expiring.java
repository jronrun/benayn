/**
 * 
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Ticker.systemTicker;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * 
 */
public final class Expiring<T> {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Expiring.class);
	
	/**
	 * Returns a new Expiring instance with empty iterator
	 * 
	 * @see Collections#emptyIterator()
	 * @return
	 */
	public static <T> Expiring<T> empty() {
		return of(Collections.<T>emptyIterator());
	}
	
	/**
	 * Returns a new Expiring instance
	 * 
	 * @param targets
	 * @return
	 */
	public static <T> Expiring<T> of(@SuppressWarnings("unchecked") T... elements) {
		return of(Arrays.asList(checkNotNull(elements)));
	}
	
	/**
	 * Returns a new Expiring instance
	 * 
	 * @param iterator
	 * @return
	 */
	public static <T> Expiring<T> of(Iterable<T> iterable) {
		return of(checkNotNull(iterable).iterator());
	}
	
	/**
	 * Returns a new Expiring instance
	 * 
	 * @param iterator
	 * @return
	 */
	public static <T> Expiring<T> of(Iterator<T> iterator) {
		return of(Lists.newArrayList(checkNotNull(iterator)));
	}

	/**
	 * Returns a new Expiring instance
	 * 
	 * @param collection
	 * @return
	 */
	public static <T> Expiring<T> of(Collection<T> collection) {
		return new Expiring<T>(collection);
	}
	
	/**
	 * Returns a new Expiring instance with given Enumeration
	 * 
	 * @param enumeration
	 * @return
	 */
	public static <T> Expiring<T> of(Enumeration<T> enumeration) {
		return of(Iterators.forEnumeration(enumeration));
	}
	
	/**
	 * Returns a new {@link ExpiringSet} instance
	 */
	public ExpiringSet<T> buildSet() {
		Set<T> target = (Set<T>) (handleExistEls ? Sets.<T>newHashSet() : 
				(elements instanceof Set ? elements : Sets.newHashSet(elements)));
		
		ExpiringSet<T> expiringSet = new ExpirationSet<>(target, ticker.or(systemTicker()),
				defaultExpireTime.orNull(), defaultExpireUnit.orNull());
		
		if (handleExistEls) {
			expiringSet.addAll(elements);
		}
		
		return expiringSet;
	}
	
	/**
	 * Sets the default expire time and unit
	 */
	public Expiring<T> withDefault(long expireTime, TimeUnit expireUnit) {
		checkArgument(expireTime > 0);
		this.defaultExpireTime = Optional.of(expireTime);
		this.defaultExpireUnit = Optional.of(expireUnit);
		return this;
	}
	
	/**
	 * Sets the time source for expire, default is {@link Ticker#systemTicker()}
	 */
	public Expiring<T> withTicker(Ticker ticker) {
		this.ticker = Optional.of(ticker);
		return this;
	}
	
	/**
	 * Will append the expire info to the delegate elements that already add with default expire time and unit
	 * , default is not append. Need call {@link Expiring#withDefault(long, TimeUnit)} first
	 * @see #unhandleFormer()
	 * @return
	 */
	public Expiring<T> handleFormer() {
		if (null == this.defaultExpireTime || null == this.defaultExpireUnit) {
			throw new UnsupportedOperationException(
					"There is no default expire info, call Expiring#withDefault(long, TimeUnit) first");
		}
		
		this.handleExistEls = true;
		return this;
	}
	
	/**
	 * Will not append the expire info to the delegate elements that already add
	 * @see #handleFormer()
	 * @return
	 */
	public Expiring<T> unhandleFormer() {
		this.handleExistEls = false;
		return this;
	}
	
	/**
	 * 
	 */
	public interface ExpiringSet<E> extends Set<E> {
		
		/**
		 * Adds the specified element to this set if it is not already present.
		 * Expire after the given duration time
		 */
		boolean add(E item, long expireTime, TimeUnit expireUnit);
		
		/**
		 * Check if the given item has expire info
		 */
		boolean hasExpiration(E item);
		
	}
	
	/**
	 * 
	 */
	static class ExpirationSet<T> extends AbstractSet<T> implements ExpiringSet<T> {

		protected Set<T> delegate() {
			return this.delegate;
		}

		@Override public boolean contains(Object object) {
			int hash = h(object);
			if (expireTarget.containsKey(hash)) {
				ExpiringEntry entry = expireTarget.get(hash);
				if (!entry.isExpired()) {
					return true;
				}
				
				remove(object);
				return false;
			}
			
			return delegate().contains(object);
		}

		@Override public boolean remove(Object object) {
			expireTarget.remove(h(object));
			return delegate().remove(object);
		}

		@Override public void clear() {
			expireTarget.clear();
			delegate().clear();
		}

		@Override public Iterator<T> iterator() {
			return new ExpiringIterator<>(delegate().iterator(), expireTarget);
		}

		@Override public int size() {
			return Iterators.size(iterator());
		}

		@Override public boolean add(final T item) {
			if (null != defaultTime && null != defaultUnit) {
				return add(item, defaultTime, defaultUnit);
			}
			
			return delegate().add(item);
		}

		@Override public boolean add(T item, long expireTime, TimeUnit expireUnit) {
			expireTarget.put(h(checkNotNull(item)), ExpiringEntry.of(ticker, expireTime, expireUnit));
			return delegate().add(item);
		}
		
		@Override public boolean hasExpiration(T item) {
			return expireTarget.containsKey(h(item));
		}

		private Set<T> delegate = null;
		private Ticker ticker;
		private Long defaultTime;
		private TimeUnit defaultUnit;
		private final Map<Integer, ExpiringEntry> expireTarget = Maps.newHashMap();
		
		protected ExpirationSet(Set<T> delegate, Ticker ticker, Long defaultTime, TimeUnit defaultUnit) {
			this.ticker = ticker;
			this.delegate = delegate;
			this.defaultTime = defaultTime;
			this.defaultUnit = defaultUnit;
		}

	}
	
	/**
	 * 
	 */
	static class ExpiringIterator<T> implements Iterator<T> {

	    @Override public T next() {
	        if (!hasNext()) {
	            throw new NoSuchElementException();
	        }
	        state = State.NOT_READY;
	        return next;
	    }

	    @Override public void remove() {
	        delegateIterator.remove();
	    }
	    
	    @Override public boolean hasNext() {
	        checkState(state != State.FAILED);
	        switch (state) {
	            case DONE: return false;
	            case READY: return true;
	            default:
	                state = State.FAILED;
	                next = nextUnexpired();
	                if (next == null) {
	                    state = State.DONE;
	                    return false;
	                } else {
	                    state = State.READY;
	                    return true;
	                }
	        }
	    }

	    /**
	     * Returns the next unexpired entry from our underlying iterator - removing any expired values along the way.
	     * Returns null when the underlying iterator has no more entries
	     */
	    private T nextUnexpired() {
	    	int hash; while (delegateIterator.hasNext()) {
	            final T nextEntry = delegateIterator.next();
	            
	            if (expireTarget.containsKey(hash = h(nextEntry))) {
	            	if (!expireTarget.get(hash).isExpired()) {
	            		return nextEntry;
	            	}
	            	
	            	remove();
	            	expireTarget.remove(hash);
	            	continue;
	            }
	            
	            return nextEntry;
	        }
	        
	        return null;
	    }

	    /** Tracks the state of our iterator */
	    private enum State { READY, NOT_READY, DONE, FAILED }
	    private State state = State.NOT_READY;

	    private final Iterator<T> delegateIterator;
	    private final Map<Integer, ExpiringEntry> expireTarget;
	    private T next;

	    protected ExpiringIterator(final Iterator<T> entryIterator, Map<Integer, ExpiringEntry> expireTarget) {
	        this.delegateIterator = entryIterator;
	        this.expireTarget = expireTarget;
	    }
	    
	}
	
	/**
	 * 
	 */
	protected static class ExpiringEntry {
		
        private final Stopwatch stopwatch;
        private final long expirationTime;
        private final TimeUnit expirationUnit;
        
        protected static ExpiringEntry of(Ticker ticker, long expirationTime, TimeUnit expirationUnit) {
    		return new ExpiringEntry(ticker, expirationTime, expirationUnit);
    	}

        protected ExpiringEntry(Ticker ticker, long expirationTime, TimeUnit expirationUnit) {
            checkArgument(expirationTime > 0);
            this.expirationUnit = checkNotNull(expirationUnit);
            this.expirationTime = expirationTime;
            this.stopwatch = Stopwatch.createStarted(checkNotNull(ticker));
        }

		protected void touch() {
			stopwatch.reset().start();
		}

		protected boolean isExpired() {
			return (stopwatch.elapsed(expirationUnit) >= expirationTime);
		}

		@Override public String toString() {
			ToStringHelper helper = MoreObjects.toStringHelper(getClass());
			helper.add("expirationTime", expirationTime);
			helper.add("expirationUnit", expirationUnit);
			helper.add("stopwatch", stopwatch);
			return helper.toString();
		}
		
    }
	
	private Expiring() { }
	
	private Expiring(Collection<T> collection) {
		elements = checkNotNull(collection);
	}
	
	private Collection<T> elements;
	private boolean handleExistEls = false;
	private Optional<Ticker> ticker = Optional.absent();
    private Optional<Long> defaultExpireTime = Optional.absent();
    private Optional<TimeUnit> defaultExpireUnit = Optional.absent();
	
	private static int h(Object... targets) {
		return Objects.hashCode(targets);
	}
	
}
