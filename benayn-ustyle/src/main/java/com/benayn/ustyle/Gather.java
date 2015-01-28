package com.benayn.ustyle;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public final class Gather<T> {

	/**
	 * 
	 */
	protected final static Loggers logger = Loggers.of(Gather.class);
	
	private Optional<Collection<T>> elements;
	private Optional<List<Decision<T>>> predicates;
	
	{
		List<Decision<T>> l = Lists.newLinkedList();
		predicates = Optional.of(l);
	}
	
	
	private Gather() { }
	
	private Gather(Collection<T> collection) {
		elements = Optional.fromNullable(collection);
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
		return new Gather<T>(Collections.list(enumeration));
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
	public Gather<T> filter(Decision<T> decision) {
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
	public Gather<T> filterAsGather(Decision<T> decision) {
		return from(Collections2.filter(elements.get(), decision));
	}
	
	/**
	 * Returns the given elements of {@code unfiltered} that satisfy a predicate.
	 * 
	 * @param decision
	 * @return
	 */
	public Collection<T> filterAsCollection(Decision<T> decision) {
		return Collections2.filter(elements.get(), decision);
	}
	
	/**
	 * Returns the first element that satisfies the given decision
	 * 
	 * @param decision
	 * @return
	 */
	public T find(Decision<T> decision) {
		return Iterables.find(result(), decision);
	}
	
	/**
	 * Returns an Optional containing the first element that satisfies the given decision
	 * 
	 * @param decision
	 * @return
	 */
	public Optional<T> tryFind(Decision<T> decision) {
		return Iterables.tryFind(result(), decision);
	}
	
	/**
	 * Returns the first element that satisfies the given decision, or defaultValue if none found.
	 * 
	 * @param decision
	 * @param defaultValue
	 * @return
	 */
	public T find(Decision<T> decision, T defaultValue) {
		return Iterables.find(result(), decision, defaultValue);
	}
	
	/**
	 * Applies the special decision to each element
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<T> loop(Decision<T> decision) {
		if (Decisions.isEmpty().apply(result())) {
			return this;
		}
		
		Iterables.all(result(), decision);
		return this;
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
	 * Preinstall the delegate elements as log target
	 * 
	 * @return
	 */
	public Loggers logset() {
		return logger.install(result());
	}
	
	/**
	 * Log all element with INFO level
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<T> info(Decision<T> decision) {
		logger.log(result(), decision);
		return this;
	}

	/**
	 * Log all element with INFO level
	 * 
	 * @return
	 */
	public Gather<T> info() {
		logger.log(result());
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
		
		for (Decision<T> predicate : predicates.get()) {
			elements = Optional.fromNullable(Collections2.filter(elements.get(), predicate));
		}
		isFiltered = true;
	}
	
}
