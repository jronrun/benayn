package com.benayn.ustyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

public final class Gather<T> {

	/**
	 * 
	 */
	protected final static Loggers logger = Loggers.of(Gather.class);
	
	/**
	 * Returns a new gather instance with empty iterator
	 * 
	 * @see Collections#emptyIterator()
	 * @return
	 */
	public static <T> Gather<T> empty() {
		return from(Collections.<T>emptyIterator());
	}
	
	/**
	 * Returns a new gather instance
	 * 
	 * @param targets
	 * @return
	 */
	public static <T> Gather<T> from(@SuppressWarnings("unchecked") T... elements) {
		return null == elements 
				? Gather.<T>empty() : from(Arrays.asList(elements));
	}
	
	/**
	 * Returns a new gather instance
	 * 
	 * @param iterator
	 * @return
	 */
	public static <T> Gather<T> from(Iterable<T> iterable) {
		return null == iterable 
				? Gather.<T>empty() : from(iterable.iterator());
	}
	
	/**
	 * Returns a new gather instance
	 * 
	 * @param iterator
	 * @return
	 */
	public static <T> Gather<T> from(Iterator<T> iterator) {
		return null == iterator 
				? Gather.<T>empty() : from(Lists.newArrayList(iterator));
	}

	/**
	 * Returns a new gather instance
	 * 
	 * @param collection
	 * @return
	 */
	public static <T> Gather<T> from(Collection<T> collection) {
		return new Gather<T>(collection);
	}
	
	/**
	 * Returns a new gather instance with given Enumeration
	 * 
	 * @param enumeration
	 * @return
	 */
	public static <T> Gather<T> from(Enumeration<T> enumeration) {
		return null == enumeration 
				? Gather.<T>empty() : from(Iterators.forEnumeration(enumeration));
	}
	
	/**
	 * Returns elements as a list 
	 * 
	 * @return
	 */
	public List<T> list() {
		return asList(true);
	}
	
	/**
	 * Returns elements as a none null list
	 * 
	 * @return
	 */
	public List<T> noneNullList() {
		return asList(false);
	}
	
	/**
	 * Returns a copy of the given elements sorted by special ordering
	 * 
	 * @param ordering
	 * @return
	 */
	public List<T> orderList(Ordering<T> ordering) {
		return ordering.sortedCopy(noneNullList());
	}
	
	/**
	 * Sort elements by special ordering
	 * 
	 * @param ordering
	 * @return
	 */
	public Gather<T> order(Ordering<T> ordering) {
		Collection<T> sorted = ordering.sortedCopy(list());
		elements = Optional.fromNullable(sorted);
		return this;
	}
	
	/**
	 * Filter the given elements with special decision
	 * 
	 * @param decision
	 */
	public Gather<T> filter(Predicate<T> decision) {
		predicates.get().add(decision);
		isFiltered = false;
		return this;
	}

	/**
	 * Returns a new Gather with the given elements of {@code unfiltered} that satisfy a predicate.
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<T> filterAsGather(Predicate<T> decision) {
		return from(Collections2.filter(elements.get(), decision));
	}
	
	/**
	 * Returns the given elements of {@code unfiltered} that satisfy a predicate.
	 * 
	 * @param decision
	 * @return
	 */
	public Collection<T> filterAsCollection(Predicate<T> decision) {
		return Collections2.filter(elements.get(), decision);
	}
	
	/**
     * Uses {@link Iterables#find}; returns the first element in the delegate that
     * satisfies a predicate, as an {@link Optional<T>}, which contains either the
     * found element or <code>null</code> if not found.
     */
    public Optional<T> finds(Predicate<? super T> decision) {
        return Optional.fromNullable(Iterables.find(each(), decision, null));
    }
	
	/**
	 * Returns the first element that satisfies the given decision
	 * 
	 * @param decision
	 * @return
	 */
	public T find(Predicate<T> decision) {
		return Iterables.find(result(), decision);
	}
	
	/**
	 * Returns an Optional containing the first element that satisfies the given decision
	 * 
	 * @param decision
	 * @return
	 */
	public Optional<T> tryFind(Predicate<T> decision) {
		return Iterables.tryFind(result(), decision);
	}
	
	/**
	 * Returns the first element that satisfies the given decision, or defaultValue if none found.
	 * 
	 * @param decision
	 * @param defaultValue
	 * @return
	 */
	public T find(Predicate<T> decision, T defaultValue) {
		return Iterables.find(result(), decision, defaultValue);
	}
	
	/**
	 * Applies the special decision to each element
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<T> loop(Predicate<T> decision) {
		if (Decisions.isEmpty().apply(result())) {
			return this;
		}
		
		Iterables.all(result(), decision);
		return this;
	}
	
	/**
     * Make the delegate usable as the <code>{@link Iterable} in a for loop
     */
	public Iterable<T> each() {
		return noneNullList();
	}
	
	/**
     * Returns true if every element in the delegate satisfies the given predicate.
     */
	public boolean all(Predicate<T> decision) {
		return Iterables.all(each(), decision);
	}
	
	/**
     * Returns true if any element in the delegate satisfies the given predicate.
     */
	public boolean any(Predicate<T> decision) {
		return Iterables.any(each(), decision);
	}
	
	/**
     * Combines this delegate and another iterator into a new {@link Gather} instance
     */
	public Gather<T> concat(Iterable<? extends T> iterable) {
		return from(Iterables.concat(each(), iterable));
	}
	
	/**
     * Invokes a function on each element of the delegate, ignoring the return value;
     * returns the number of elements that were present.
     */
    public <O> int foreach(Function<? super T, O> function) {
        int count = 0;
        for (T item : each()) {
        	function.apply(item);
            ++count;
		}
        
        return count;
    }
    
    /**
     * Treating each element in the delegate as a value, applies a function t determine the key and uses the
     * multimap to group all values with the same key.
     */
    public <K> Multimap<K,T> groupBy(Function<? super T, K> function) {
        return Multimaps.index(each(), function);
    }
    
    /**
     * Return a string that is the result of concatenating the string representation
     * of each element in the delegate, using the given separator.  Nulls will be
     * represented by <code>"null"</code>.
     */
    public String join(String separator) {
        return join(Joiner.on(separator));
    }
    
    /**
     * Return the result of applying a {@link Joiner} to the elements in the sequence.
     */
    public String join(Joiner joiner) {
        return joiner.join(this.each());
    }
    
    /**
     * Returns an array of the given type containing the elements of the delegate.
     */
    public T[] asArray(Class<T> type) {
        return Iterators.toArray(each().iterator(), type);
    }
    
    /**
     * Returns a new {@link Gather} that applies a function to each element of this delegate as they are retrieved.
     */
    public <R> Gather<R> map(final Function<? super T, ? extends R> function) {
        return from(Iterators.transform(each().iterator(), function));
    }
    
    /**
     * Returns a map whose values are the delegate items and 
     * whose keys are the result of applying <code>keyMaker</code> to each value.
     */
    public <K> Map<K, T> asMap(Function<T, K> keyMaker) {
        Map<K, T> map = Maps.newHashMap();
        foreach(Funcs.addTo(map, keyMaker));
        return map;
    }
    
    /**
     * Return a <code>List</code> of two <code>Lists</code> generated by applying
     * a predicate to each element; the first list contains those for which it returns true, the second, false.
     */
    public List<List<T>> split(Predicate<? super T> predicate) {
        final List<T> a = Lists.newArrayList(), b = Lists.newArrayList();
        for (T elt: each()) {
            (predicate.apply(elt) ? a : b).add(elt);
        }
        List<List<T>> result = Lists.newArrayList();
        result.add(a);
        result.add(b);
        return result;
    }
	
    /**
     * Returns a new {@link Gather} that limits the number of items that are available from this iterator.
     */
    public Gather<T> take(int count) {
        return from(Iterables.limit(each(), count));
    }
    
    /**
     * Like {@link #take(int)} but returns {@link Gather} that provides elements as long
     * as a predicate returns true.
     */
    public Gather<T> takeWhile(final Predicate<T> predicate) {
    	final Iterator<T> it = each().iterator();
        return from(new AbstractIterator<T>() {
            protected T computeNext() {
                if (!it.hasNext()) {
                    return endOfData();
                }
                T t = it.next();
                return predicate.apply(t) ? t : endOfData();
            }
        });
    }
    
    /**
     * Returns a new {@link Gather} that skips leading elements as long as a predicate returns true.
     */
    public Gather<T> dropWhile(final Predicate<T> predicate) {
    	final Iterator<T> it = each().iterator();
        return from(new AbstractIterator<T>() {
            private boolean done = false;
            protected T computeNext() {
                if (!done) {
                    while (it.hasNext()) {
                        T t = it.next();
                        if (!predicate.apply(t)) {
                            done = true;
                            return t;
                        }
                    }
                }
                if (it.hasNext()) {
                    return it.next();
                }
                else {
                    return endOfData();
                }
            }
        });
    }
    
	/**
	 * Returns the delegate collection that apply the previous operate
	 * 
	 * @return
	 */
	public Collection<T> result() {
		doCollectionFilter();
		return elements.orNull();
	}
	
	/**
	 * Log all element with INFO level
	 * 
	 * @return
	 */
	public Gather<T> info() {
		logger.humanStyle().info(result());
		return this;
	}

	/**
	 * 
	 * @param nullable
	 * @return
	 */
	private List<T> asList(boolean nullable) {
		if (elements.isPresent()) {
			return Lists.newLinkedList(result());
		}
		
		if (!nullable) {
			return Lists.newLinkedList();
		}
		
		return null;
	}
	
	private boolean isFiltered = true;
	
	/**
	 * 
	 */
	private void doCollectionFilter() {
		if (!elements.isPresent() || !predicates.isPresent() || isFiltered) {
			return;
		}
		
		for (Predicate<T> predicate : predicates.get()) {
			elements = Optional.fromNullable(Collections2.filter(elements.get(), predicate));
		}
		isFiltered = true;
	}
	
	private Optional<Collection<T>> elements;
	private Optional<List<Predicate<T>>> predicates;
	
	{
		List<Predicate<T>> l = Lists.newLinkedList();
		predicates = Optional.of(l);
	}
	
	
	private Gather() { }
	
	private Gather(Collection<T> collection) {
		elements = Optional.fromNullable(collection);
	}
	
}
