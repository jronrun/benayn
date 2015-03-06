package com.benayn.ustyle;

import static com.benayn.ustyle.string.Strs.INDEX_NONE_EXISTS;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.primitives.Primitives.allPrimitiveTypes;
import static com.google.common.primitives.Primitives.isWrapperType;
import static com.google.common.primitives.Primitives.wrap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.JSONer.ReadJSON;
import com.benayn.ustyle.inner.Options;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

public final class Reflecter<T> {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Reflecter.class);
	
	/**
     * <p>The inner class separator character: <code>'$' == {@value}</code>.</p>
     */
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
	
	/**
	 * 
	 */
	private Optional<T> delegate;
	
	private Optional<Gather<Field>> fieldHolder = Optional.absent();
	private Optional<Mapper<String, Object>> nameValMap = Optional.absent();
	
	private Reflecter() { }
	private Reflecter(T target) {
		this.delegate = Optional.fromNullable(target);
		intlFields();
	}

	/**
	 * Returns a new Reflecter instance
	 * 
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked") public static <T> Reflecter<T> from(T target) {
		return Decisions.isClass().apply(target) 
		        ? (((Class<T>) target).isArray() 
		                ? (Reflecter<T>) from(ObjectArrays.newArray(((Class<T>) target).getComponentType(), 0)) 
		                        : from((Class<T>) target)) : new Reflecter<T>(target);
	}
	
	/**
     * Returns a new {@link Reflecter} instance with given {@link Class}
     * 
     * @param target
     * @return
     */
	@SuppressWarnings("unchecked") public static <T> Reflecter<T> from(Class<T> target) {
        return new Reflecter<T>((T) Suppliers2.toInstance((Class<?>) target).get());
    }
	
	/**
	 * Returns the property value to which the specified property name
	 * 
	 * @param propName
	 * @return
	 */
	public <F> F val(String propName) {
		Triple<String, Field, Reflecter<Object>> triple = getNestRefInfo(propName);
		return triple.getR().getPropVal(triple.getC(), triple.getL());
	}
	
	/**
	 * Set the specified property value to the specified property name in this object instance
	 * 
	 * @param propName
	 * @param propVal
	 * @return
	 */
	public <V> Reflecter<T> val(String propName, V propVal) {
		Triple<String, Field, Reflecter<Object>> triple = getNestRefInfo(propName);
		triple.getR().setPropVal(triple.getC(), triple.getL(), propVal);
		this.isChanged = true;
		return this;
	}
	
	/**
	 * Returns the nested {@link Reflecter} instance with given tier property name
	 * 
	 * @param propName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <N> Reflecter<N> getReflecter(String propName) {
		return (Reflecter<N>) getNestRefInfo(propName).getR();
	}
	
	/**
	 * Returns the belong instance with given property name
	 * 
	 * @param propName
	 * @return
	 */
	public <I> I getObject(String propName) {
		return getReflecter(propName).get();
	}
	
	/**
	 * Clone a bean based on the delegate target available property getters and setters
     * 
	 * @return
	 */
	public <Dest> Dest clones() {
		return copyTo(delegateClass());
	}
	
	/**
	 * Copy all the same property to the given object
	 * 
	 * @param dest
	 * @return
	 */
	public <Dest> Dest copyTo(Object dest) {
		return copyTo(dest, new String[]{});
	}
	
	/**
	 * Copy all the same property to the given object, except the property name in the given exclude array
	 * 
	 * @param dest
	 * @param excludes
	 * @return
	 */
	public <Dest> Dest copyTo(Object dest, String... excludes) {
		return from(dest)
				.setExchanges(exchangeProps)
				.setExchangeFuncs(exchangeFuncs)
				.setAutoExchange(autoExchange)
				.setExcludePackagePath(excludePackagePath)
				.setTrace(trace)
				.populate(asMap(), excludes).get();
	}
	
	/**
	 * Returns delegate object instance as a map, key is property name, value is property value
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked") public <V> Map<String, V> asMap() {
		return (Map<String, V>) mapper().map();
	}
	
	/**
	 * Populate the JavaBeans properties of this delegate object, based on the specified name/value pairs
	 * 
	 * @param properties
	 * @return
	 */
	public <V> Reflecter<T> populate(Map<String, V> properties) {
		return populate(properties, new String[]{});
	}
	
	/**
	 * Populate the JavaBeans properties of this delegate object, based on the specified name/value pairs
	 * 
	 * @param properties
	 * @return
	 */
	public <V> Reflecter<T> populate(Map<String, V> properties, String... excludes) {
		return populate(properties, Arrays.asList(excludes));
	}
	
	/**
     * Populate the JavaBeans properties of this delegate object, based on the JSON string
     * 
     * @see ReadJSON#asObject(Object)
     */
    public <V> Reflecter<T> populate(String json) {
        return populate(json, new String[]{});
    }
	
	/**
	 * Populate the JavaBeans properties of this delegate object, based on the JSON string
     * 
     * @see ReadJSON#asObject(Object)
	 */
	public <V> Reflecter<T> populate(String json, String... excludes) {
	    return JSONer.addJsonExchangeFunc(this).populate(JSONer.readNoneNullMap(json), excludes);
	}
	
	/**
	 * Populate the JavaBeans properties of this delegate object, based on the specified name/value pairs
	 * 
	 * @param properties
	 * @return
	 */
	public <V> Reflecter<T> populate(Map<String, V> properties, List<String> excludes) {
		if (Decisions.isEmpty().apply(properties)) {
			return this;
		}
		
		if (this.delegate.get().getClass().isArray()) {
		    Object els = null;
		    
		    if (null != (els = properties.get(JSONer.ReadJSON.itemsF))) {
		        this.delegate = Optional.fromNullable(Resolves.<T>get(this.delegate.get().getClass(), els));
		    }
		    
		    return this;
		}
		
		if (this.autoExchange) {
			autoExchange();
		}
		
		fieldLoop(new TransformMap2ObjVal<V>(properties, excludes));
		this.isChanged = true;
		return this;
	}

	/**
	 * Populate the JavaBeans properties of this delegate object with the random values
	 * 
	 * @return
	 */
	public Reflecter<T> populate4Test() {
		fieldLoop(new RandomVal2ObjVal());
		this.isChanged = true;
		return this;
	}
	
	/**
	 * Exchange the properties that matched the given decision with given exchange function which with {@link Field}
	 * 
	 * @param exchangeFunc
	 * @param decision
	 * @return
	 */
	public <I, O> Reflecter<T> exchWithField(final Function<Pair<Field, I>, O> exchangeFunc, Decision<Field> decision) {
		this.fieldHolder.get().filterAsGather(decision).loop(new Decisional<Field>() {

			@Override protected void decision(Field input) {
				exchWithField(input.getName(), input.getName(), exchangeFunc);
			}
		});
		return this;
	}
	
	/**
	 * Exchange from properties map key to delegate target field name with exchange function which with {@link Field}
	 * 
	 * @param targetFieldName
	 * @param keyFromPropMap
	 * @param exchangeFunc
	 * @return
	 */
	public <I, O> Reflecter<T> exchWithField(String targetFieldName, String keyFromPropMap, Function<Pair<Field, I>, O> exchangeFunc) {
		exchange(targetFieldName, keyFromPropMap);
		exchangeFieldFuncs.put(keyFromPropMap, exchangeFunc);
		return this;
	}
	
	/**
	 * Exchange the properties that matched the given decision with given exchange function
	 * 
	 * @param exchangeFunc
	 * @param decision
	 * @return
	 */
	public <I, O> Reflecter<T> exchange(final Function<I, O> exchangeFunc, Decision<Field> decision) {
		this.fieldHolder.get().filterAsGather(decision).loop(new Decisional<Field>() {

			@Override protected void decision(Field input) {
				exchange(input.getName(), input.getName(), exchangeFunc);
			}
		});
		return this;
	}
	
	/**
	 * Exchange properties with given exchange function
	 * 
	 * @param exchangeFunc
	 * @param inOutWithSameNameProps
	 * @return
	 */
	public <I, O> Reflecter<T> exchange(Function<I, O> exchangeFunc, String... inOutWithSameNameProps) {
		for (String propName : inOutWithSameNameProps) {
			exchange(propName, propName, exchangeFunc);
		}
		return this;
	}
	
	/**
	 * Do not auto exchange when the Field class type is primitive or wrapped primitive or Date
	 * 
	 * @return
	 */
	public Reflecter<T> noneAutoExchange() {
		this.autoExchange = Boolean.FALSE;
		return this;
	}
	
	/**
     * Auto exchange when the Field class type is primitive or wrapped primitive or Date
     * 
     * @return
     */
	public Reflecter<T> autoExchange() {
	    if (!this.autoExchangeAdd) {
	        exchange(Funcs.TO_BOOLEAN, booleanD);
	        exchange(Funcs.TO_BYTE, byteD);
	        exchange(Funcs.TO_DOUBLE, doubleD);
	        exchange(Funcs.TO_FLOAT, floatD);
	        exchange(Funcs.TO_INTEGER, integerD);
	        exchange(Funcs.TO_LONG, longD);
	        exchange(Funcs.TO_SHORT, shortD);
	        exchange(Funcs.TO_DATE, dateD);
	        exchange(Funcs.TO_CHARACTER, characterD);
	        exchange(Funcs.TO_STRING, stringD);
	        exchange(Funcs.TO_BIGDECIMAL, bigDecimalD);
	        exchange(Funcs.TO_BIGINTEGER, bigIntegerD);
	        this.autoExchangeAdd = true;
	    }
	    
	    this.autoExchange = Boolean.TRUE;
	    return this;
	}

	/**
	 * Exchange from properties map key to delegate target field name
	 * 
	 * @param targetFieldName
	 * @param keyFromPropMap
	 * @return
	 */
	public Reflecter<T> exchange(String targetFieldName, String keyFromPropMap) {
		exchangeProps.put(targetFieldName, keyFromPropMap);
		return this;
	}
	
	/**
	 * Exchange from properties map key to delegate target field name with exchange function
	 * 
	 * @param targetFieldName
	 * @param keyFromPropMap
	 * @param exchangeFunc
	 * @return
	 */
	public <I, O> Reflecter<T> exchange(String targetFieldName, String keyFromPropMap, Function<I, O> exchangeFunc) {
		exchange(targetFieldName, keyFromPropMap);
		exchangeFuncs.put(keyFromPropMap, exchangeFunc);
		return this;
	}
	
	/**
	 * Returns delegate object instance as a {@link Mapper}
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked") public <V> Mapper<String, V> mapper() {
		if (!isChanged && nameValMap.isPresent()) {
			return (Mapper<String, V>) nameValMap.get();
		}
		
		Map<String, Object> fm = Maps.newHashMap();
		if (fieldHolder.isPresent()) {
			fieldLoop(new TransformFields2Map<Object>(fm, this.trace));
		}
		nameValMap = Optional.of(Mapper.from(fm));
		
		this.isChanged = false;
		return (Mapper<String, V>) nameValMap.get();
	}
	
	/**
	 * Returns the delegate object
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <O> O get() {
		return (O) delegate.orNull();
	}
	
	/**
	 * Returns the Field with given property name
	 * 
	 * @param propName
	 * @return
	 */
	public Field field(String propName) {
		return getNestRefInfo(propName).getC();
	}
	
	/**
	 * Returns the fields {@link Gather}
	 * 
	 * @return
	 */
	public Gather<Field> fieldGather() {
		return this.fieldHolder.orNull();
	}
	
	/**
	 * Returns the field {@link Gather#loop(Decision)}
	 * 
	 * @param decision
	 * @return
	 */
	public Gather<Field> fieldLoop(Decision<Field> decision) {
		return fieldGather().loop(decision);
	}
	
	/**
	 * Loops the object's properties
	 * 
	 * @param decision
	 * @return
	 */
	public <V> Reflecter<T> propLoop(final Decision<Pair<Field, V>> decision) {
		fieldLoop(new Decisional<Field>() {

			@SuppressWarnings("unchecked") @Override protected void decision(Field input) {
				decision.apply((Pair<Field, V>) Pair.of(input, getPropVal(input, input.getName())));
			}
		});
		return this;
	}
	
	/**
	 * Ignore the fields in the given modifiers for the field represented
     * The <code>Modifier</code> class should be used to decode the modifiers.
     * 
	 * @param mod
	 * @return
	 */
	public Reflecter<T> ignore(final Integer mod) {
		return filter(new Decision<Field>(){
			
			@Override public boolean apply(Field input) {
				return !((mod.intValue() & input.getModifiers()) != 0);
			}
		});
	}
	
	/**
	 * Ignored the given package path to transform field to map
	 * 
	 * @param packagePath
	 * @return
	 */
	public Reflecter<T> packageIgnore(String packagePath) {
		this.excludePackagePath.add(checkNotNull(packagePath));
		return this;
	}
	
	/**
	 * Only the primitive property in delegate target join the future operate
	 * 
	 * @return
	 */
	public Reflecter<T> onlyPrimitives() {
		return filter(primitivesD);
	}
	
	/**
	 * Filter the delegate target fields with special decision
	 * 
	 * @param decision
	 */
	public Reflecter<T> filter(Decision<Field> decision) {
		this.fieldHolder.get().filter(decision);
		return this;
	}
	
	/**
	 *
	 * @param <V>
	 */
	private class TransformMap2ObjVal<V> implements Decision<Field> {
		
		V v;
		String name;
		Object propObj;
		Map<String, V> props;
		List<String> exclude;
		boolean isExchange = false;
		
		private TransformMap2ObjVal(Map<String, V> props, List<String> excludeProps) {
			this.props = props;
			this.exclude = excludeProps;
		}

		@SuppressWarnings("unchecked") @Override public boolean apply(Field input) {
			if (Modifier.isStatic(input.getModifiers())
					|| Modifier.isFinal(input.getModifiers())) {
				return true;
			}
			
			name = input.getName();
			isExchange = exchangeProps.containsKey(name) || exchangeFieldFuncs.containsKey(name);
			name = isExchange ? exchangeProps.get(name) : name;
			
			if (!props.containsKey(name)) {
				return true;
			}
			
			if (!Decisions.isEmpty().apply(exclude) && exclude.contains(name)) {
				return true;
			}
			
			v = props.get(name);
			if (null == v) {
				return true;
			}
			
			if (Map.class.isInstance(v)
					&& !Decisions.isBaseClass().apply(input.getType())) {
				propObj = Suppliers2.toInstance(input.getType()).get();
				setPropVal(input, name, from(propObj).populate((Map<String, V>) v, exclude).get());
				return true;
			}
			
			setPropVal(input, name, isExchange ? exchangeVal(input, name, v) : v);
			return true;
		}
	}
	
	/**
	 * Exchange from properties map key to delegate target field name with the given exchange map
	 * 
	 * @param exchangeMap
	 * @return
	 */
	public Reflecter<T> setExchanges(Map<String, String> exchangeMap) {
		exchangeProps.putAll(checkNotNull(exchangeMap));
		return this;
	}
	
	/**
	 * Exchange from properties map key to delegate target field name with the given exchange function map
	 * 
	 * @return
	 */
	public Reflecter<T> setExchangeFuncs(Map<String, Function<?, ?>> exchangeFuncMap) {
		exchangeFuncs.putAll(checkNotNull(exchangeFuncMap));
		return this;
	}
	
	/**
	 * Ignored the given package path to transform field to map
	 * 
	 * @param excludePackages
	 * @return
	 */
	public Reflecter<T> setExcludePackagePath(Set<String> excludePackages) {
		for (String pkg : checkNotNull(excludePackages)) {
			if (!this.excludePackagePath.contains(pkg)) {
				this.excludePackagePath.add(pkg);
			}
		}
		return this;
	}
	
	public Reflecter<T> setTrace(boolean isTrace) {
		trace = isTrace;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	private <N> Triple<String, Field, Reflecter<N>> getNestRefInfo(String propName) {
	    Field field = null;
		int idx = checkNotNull(propName).indexOf(TIER_SEP);
		
		if (idx > Strs.INDEX_NONE_EXISTS) {
		    String prop = propName.substring(0, idx);
		    field = matchField(prop);
			N val = getPropVal(field, prop);
        	return from(null == val ? field.getType() : val).getNestRefInfo(propName.substring(idx + 1));
	    }
		
		return null == (field = matchField(propName)) ? null : Triple.of(propName, field, (Reflecter<N>) this);
	}
	
	/**
	 * 
	 * @param <V>
	 */
	private class TransformFields2Map<V> implements Decision<Field> {
		
		V v;
		String k;
		boolean traceAble;
		Map<String, V> nameValueMap;
		
		private TransformFields2Map(Map<String, V> nameValueMap, boolean traceAble) {
			this.nameValueMap = nameValueMap;
			this.traceAble = traceAble;
		}
		
		@SuppressWarnings("unchecked") @Override public boolean apply(Field input) {
			// Reject field from inner class.
			if (input.getName().indexOf(INNER_CLASS_SEPARATOR_CHAR) != INDEX_NONE_EXISTS) {
	            return true;
	        }
			
			k = input.getName();
			v = getPropVal(input, k);
			
			//IS_STRING, IS_MAP, IS_COLLECTION, IS_PRIMITIVE, IS_DATE
			if (Predicates.isNull().apply(v) 
					|| isWrapperType(v.getClass())
					|| allPrimitiveTypes().contains(v)
					|| Map.class.isInstance(v)
					|| Enum.class.isInstance(v)
					|| String.class.isInstance(v)
					|| Collection.class.isInstance(v)
					|| Date.class.isInstance(v)
					|| BigInteger.class.isInstance(v)
					|| BigDecimal.class.isInstance(v)
					|| v.getClass().isArray()) {
				
				nameValueMap.put(k, v);
				return true;
			}
			
			// Reject field from inner class.
			String clzN = v.getClass().getName();
			if (!Modifier.isStatic(v.getClass().getModifiers()) 
			        && clzN.indexOf(INNER_CLASS_SEPARATOR_CHAR) != INDEX_NONE_EXISTS) {
				return true;
			}
			for (String packagePath : excludePackagePath) {
				if (c(clzN, packagePath)) {
					return true;
				}
			}
			
			if (traceAble) {
				log.info(String.format("transform %s to map for property %s.%s", clzN, delegateClass().getName(), k));
			}
			
			nameValueMap.put(k, (V) from(v).asMap());
			return true;
		}

		private boolean c(String clzN, String string) {
			return clzN.indexOf(string) != INDEX_NONE_EXISTS;
		}
	}
	
	private class RandomVal2ObjVal implements Decision<Field> {
		
		@Override public boolean apply(Field input) {
			// Reject field from inner class.
			if (input.getName().indexOf(INNER_CLASS_SEPARATOR_CHAR) != INDEX_NONE_EXISTS) {
	            return true;
	        }
			
			setPropVal(input, input.getName(), Randoms.get(input));
			return true;
		}
		
	}
	
	/**
	 * 
	 */
	private void intlFields() {
		if (!delegate.isPresent()) {
			return;
		}
		
		List<Field> fields = Lists.newLinkedList();
		Class<?> clazz = delegateClass();
		
		while (clazz != null) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = isInnerClass() ? null : clazz.getSuperclass();
		}
		
		fieldHolder = Optional.of(Gather.from(fields));
	}
	
	/**
	 * <p>Is the delegate object class an inner class or static nested class.</p>
	 * 
	 * @return
	 */
    public boolean isInnerClass() {
        return delegate.isPresent() && delegateClass().getEnclosingClass() != null;
    }
	
	/**
	 * 
	 * @param field
	 * @param propName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <V> V getPropVal(Field field, String propName) {
		try {
			field.setAccessible(true);
			return (V) field.get(delegate.get());
		} catch (IllegalArgumentException e) {
			log.error(String.format("get %s's value error.", propName), e);
		} catch (IllegalAccessException e) {
			log.error(String.format("get %s's value error.", propName), e);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param field
	 * @param propName
	 * @param propVal
	 */
	private <V> void setPropVal(Field field, String propName, V propVal) {
		try {
			if (this.trace) {
				log.info(String.format("set %s.%s = %s (%s)", delegateClass().getName(), 
						propName, propVal, (null == propVal ? "null" : propVal.getClass().getName())));
			}
			
			if ("serialVersionUID".equals(field.getName())) {
			    return;
			}
			
			field.setAccessible(true);
			field.set(delegate.get(), propVal);
		} catch (IllegalArgumentException e) {
			log.error(String.format("set the value %s %s to the property %s %s error.", 
					null != propVal ? propVal.getClass().getName() : Strs.EMPTY, propVal, field.getType().getName(), propName));
		} catch (IllegalAccessException e) {
			log.error(String.format("set the value %s %s to the property %s %s error.", 
			        null != propVal ? propVal.getClass().getName() : Strs.EMPTY, propVal, field.getType().getName(), propName));
		}
	}
	
	@SuppressWarnings("unchecked")
	private <V, I, O> V exchangeVal(Field field, String propName, V propVal) {
		if (exchangeFuncs.containsKey(propName)) {
			Function<I, O> func = (Function<I, O>) exchangeFuncs.get(propName);
			return (V) func.apply((I) propVal);
		}
		
		if (exchangeFieldFuncs.containsKey(propName)) {
			Function<Pair<Field, I>, O> func = (Function<Pair<Field, I>, O>) exchangeFieldFuncs.get(propName);
			return (V) func.apply(Pair.of(field, (I) propVal));
		}
		
		return propVal;
	}
	
	/**
	 * 
	 * @param propName
	 * @return
	 */
	private Field matchField(final String propName) {
		checkNotNull(propName);
		Field field = fieldHolder.get().find(new Decision<Field>(){
			@Override public boolean apply(Field input) {
				return input.getName().equals(propName);
			}
		}, null);
		
		return checkNotNull(field, "The property %s is not exists.", propName);
	}
	
	private Reflecter<T> setAutoExchange(boolean isAutoExchange) {
		this.autoExchange = isAutoExchange;
		return this;
	}
	
	private static final Decision<Field> booleanD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Boolean.class; }
	};
	private static final Decision<Field> byteD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Byte.class; }
	};
	private static final Decision<Field> doubleD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Double.class; }
	};
	private static final Decision<Field> floatD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Float.class; }
	};
	private static final Decision<Field> integerD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Integer.class; }
	};
	private static final Decision<Field> longD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Long.class; }
	};
	private static final Decision<Field> shortD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Short.class; }
	};
	private static final Decision<Field> characterD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return wrap(input.getType()) == Character.class; }
	};
	
	private static final Decision<Field> bigDecimalD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return input.getType() == BigDecimal.class; }
	};
	private static final Decision<Field> bigIntegerD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return input.getType() == BigInteger.class; }
	};
	private static final Decision<Field> dateD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return input.getType() == Date.class; }
	};
	private static final Decision<Field> stringD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return input.getType() == String.class; }
	};
	private static final Decision<Field> primitivesD = new Decision<Field>(){
		@Override public boolean apply(Field input) { return allPrimitiveTypes().contains(input.getType()); }
	};
	
	/**
	 * Determines whether the delegate has a default constructor
	 */
	public boolean hasDefaultConstructor() {
	    if (!delegate.isPresent()) {
	        return false;
	    }
	    
	    final Constructor<?>[] constructors = delegateClass().getConstructors();
        for (final Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        
        return false;
	}
	
	/**
	 * 
	 */
	public static class ConstructorOptions<T> extends Options<Reflecter<T>, ConstructorOptions<T>> {

	    Class<?>[] parameterTypes;
	    
        public ConstructorOptions(Reflecter<T> reflecter, Class<?>[] parameterTypes) {
            this.reference(reflecter, this);
            this.parameterTypes = parameterTypes;
        }
        
        /**
         * @see Reflecter#getConstructor(Class...)
         */
        public Constructor<T> get() {
            return this.outerRef.getConstructor(parameterTypes);
        }
	    
        /**
         * @see Suppliers2#newInstance(Constructor, Object...)
         */
        public T newInstance(Object... parameters) {
            return Suppliers2.newInstance(get(), parameters).get();
        }
        
        /**
         * @see Modifier#isPrivate(int)
         */
        public boolean isPrivate() {
            return Modifier.isPrivate(get().getModifiers());
        }
        
        /**
         * @see Modifier#isPublic(int)
         */
        public boolean isPublic() {
            return Modifier.isPublic(get().getModifiers());
        }
        
        /**
         * @see Modifier#isPublic(int)
         */
        public boolean isProtected() {
            return Modifier.isProtected(get().getModifiers());
        }
	}
	
	/**
	 * Returns a new ConstructorOptions instance
	 */
    public ConstructorOptions<T> constructor(Class<?>... parameterTypes) {
	    return new ConstructorOptions<T>(this, parameterTypes);
	}
	
	/**
	 * @see Class#getDeclaredConstructors()
	 * @see Class#getDeclaredConstructor(Class...)
	 */
    public Constructor<T> getConstructor(Class<?>... parameterTypes) {
	    return Suppliers2.constructor(delegateClass(), parameterTypes).get();
	}
	
    /**
     * Only supports all the constructor arguments type has no primitive type or default constructor
     * 
     * @see #getConstructor(Class...)
     * @see Constructor#newInstance(Object...)
     */
	public T newInstance(Object... parameters) {
	    return Suppliers2.newInstance(delegateClass(), parameters).get();
	}
	
	/**
     * @see Decisions#instantiatable()
     */
    public boolean instantiatable() {
        return Decisions.instantiatable().apply(delegateClass());
    }
    
    /**
     * @see Decisions#notInstantiatable()
     */
    public boolean notInstantiatable() {
        return Decisions.notInstantiatable().apply(delegateClass());
    }
    
    /**
     * 
     */
    public static class MethodOptions<T> extends Options<Reflecter<T>, MethodOptions<T>> {
        
        Method method;
        
        private MethodOptions(Reflecter<T> reflecter, Method method) {
            this.reference(reflecter, this);
            this.method = method;
        }
        
        /**
         * Returns the {@link Method} instance
         */
        public Method get() {
        	return this.method;
        }
        
        /**
         * @see Suppliers2#call(Object, Method, Object...)
         */
        @SuppressWarnings("unchecked") public <R> R call(Object... parameters) {
            return (R) Suppliers2.call(this.outerRef.delegate.get(), get(), parameters).get();
        }
        
        /**
         * @see Modifier#isPublic(int)
         */
        public boolean isPublic() {
        	return Modifier.isPublic(get().getModifiers());
        }
        
        /**
         * @see Modifier#isProtected(int)
         */
        public boolean isProtected() {
        	return Modifier.isProtected(get().getModifiers());
        }
        
        /**
         * @see Modifier#isPrivate(int)
         */
        public boolean isPrivate() {
        	return Modifier.isPrivate(get().getModifiers());
        }
        
        /**
         * @see Modifier#isAbstract(int)
         */
        public boolean isAbstract() {
        	return Modifier.isAbstract(get().getModifiers());
        }
        
        /**
         * @see Modifier#isStatic(int)
         */
        public boolean isStatic() {
        	return Modifier.isStatic(get().getModifiers());
        }
        
        /**
         * @see Modifier#isFinal(int)
         */
        public boolean isFinal() {
        	return Modifier.isFinal(get().getModifiers());
        }
        
        /**
         * @see Modifier#isSynchronized(int)
         */
        public boolean isSynchronized() {
        	return Modifier.isSynchronized(get().getModifiers());
        }
        
        /**
         * @see Modifier#isNative(int)
         */
        public boolean isNative() {
        	return Modifier.isNative(get().getModifiers());
        }
        
        /**
         * @see Modifier#isStrict(int)
         */
        public boolean isStrict() {
        	return Modifier.isStrict(get().getModifiers());
        }
    }
    
    /**
     * Returns a new MethodOptions instance
     */
    public MethodOptions<T> method(String methodName) {
        return new MethodOptions<T>(this, getMethod(methodName));
    }
    
    /**
     * Returns the methods {@link Gather}
     * 
     * @return
     */
    public Gather<Method> methodGather() {
        return Gather.from(methods().values());
    }
    
    /**
     * Returns the methods {@link Mapper}
     * 
     * @return
     */
    public Mapper<String, Method> methodMapper() {
    	return Mapper.from(methods());
    }
    
    /**
     * Returns the method {@link Gather#loop(Decision)}
     * 
     * @param decision
     * @return
     */
    public Gather<Method> methodLoop(Decision<Method> decision) {
        return methodGather().loop(decision);
    }
    
    /**
     * Returns the {@link Method} instance with given name
     */
    public Method getMethod(String methodName) {
        return checkNotNull(methods().get(checkNotNull(methodName)), 
        		"The method %s is not exists of object %s", methodName, delegateClass().getName());
    }
    
    /**
     * Returns the {@link Method} map of the delegate
     */
    public Map<String, Method> methods() {
        if (this.methodHolder.isPresent()) {
            return this.methodHolder.get();
        }
        
        Map<String, Method> methodMap = Maps.newHashMap();
        Class<?> currentClass = delegateClass();
        
        while (currentClass != null) {
            for (Method m : currentClass.getDeclaredMethods()) {
                m.setAccessible(true);
                methodMap.put(m.getName(), m);
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return (this.methodHolder = Optional.of(methodMap)).get();
    }
	
	/**
	 * Returns the delegate class
	 */
	@SuppressWarnings("unchecked")
    public Class<T> delegateClass() {
	    return delegate.isPresent() ? (Class<T>) delegate.get().getClass() : null;
	}

	//<delegate target prop, populate map key>
	private Map<String, String> exchangeProps = Maps.newHashMap();
	//<populate map key, exchange function>
	private Map<String, Function<?, ?>> exchangeFuncs = Maps.newHashMap();
	private Map<String, Function<?, ?>> exchangeFieldFuncs = Maps.newHashMap();
	private boolean autoExchange = Boolean.TRUE;
	private boolean autoExchangeAdd = Boolean.FALSE;
	private boolean isChanged = false;
	private Set<String> excludePackagePath = Sets.newHashSet("com.google.common", "ch.qos.logback", "com.benayn.ustyle");
	private boolean trace = Boolean.FALSE;
	protected static final String TIER_SEP = ".";
	private Optional<Map<String, Method>> methodHolder = Optional.absent();
}
