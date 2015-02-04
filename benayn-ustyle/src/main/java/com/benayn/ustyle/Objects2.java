package com.benayn.ustyle;

import static com.google.common.primitives.Primitives.allPrimitiveTypes;
import static com.google.common.primitives.Primitives.allWrapperTypes;
import static com.google.common.primitives.Primitives.isWrapperType;
import static com.google.common.primitives.Primitives.wrap;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.behavior.StructBehavior;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.behavior.ValueBehaviorAdapter;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public class Objects2 {
	
	/**
	 * 
	 */
	public static final String LINE_BREAK = System.getProperty("line.separator");
	public static final String NULL_STR = "<null>";

	/**
	 * The string representation for the given object
	 */
	public static final Function<Object, String> TO_STRING = new Function<Object, String>() {

		@Override public String apply(Object input) {
			return TostringValueBehavior.asString(input);
		}
	};
	
	/**
	 * Check if the given input is able parse to number or boolean
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isParseable(Object input) {
		return (null != input) && (isWrapperType(input.getClass())
				|| allPrimitiveTypes().contains(input.getClass())
				|| (String.class.isInstance(input) && ( Strs.inRange('0', '9').matchesAllOf(
				        MINUS_STRING.matcher((String) input).replaceFirst(Strs.EMPTY)) ) ) );
	}
	
	private static final Pattern MINUS_STRING = Pattern.compile("^-");
	
	/**
	 * Checks if the given input class is instance of the eight java type, wrapped or primitive 
	 * 
	 * @param input
	 * @return
	 */
	public static boolean is8Type(Class<?> input) {
		return allWrapperTypes().contains(wrap(input));
	}
	
	/**
	 * Checks if the given input class is primitive
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isPrimitive(Class<?> input) {
		return allPrimitiveTypes().contains(input);
	}
	
	/**
	 * Returns the default {@link Object#toString()} result
	 * 
	 * @see Object#toString()
	 * @param input
	 * @return
	 */
	public static String defaultTostring(Object input, Object... appends) {
		if (null == input) { return NULL_STR; }
		StringBuilder bul = new StringBuilder(input.getClass().getName());
		bul.append("@").append(Integer.toHexString(input.hashCode()));
		for (Object a : appends) {
			bul.append(null != a ? a.toString() : NULL_STR);
		}
		return bul.toString();
	}
	
	/**
	 * Wraps the {@link Object#toString()}, {@link Object#hashCode()}, {@link Object#equals(Object)} 
	 * and more nicely method to the given target
	 * 
	 * @param target
	 * @return
	 */
	public static <T> FacadeObject<T> wrapObj(final T target) {
	    return FacadeObject.wrap(target);
	}
	
	/**
	 * 
	 */
	public static class FacadeObject<T> extends ForwardingObject {
	    
	    /**
	     * 
	     */
	    private static final Log log = Loggers.from(FacadeObject.class);
	    
	    /**
	     * Wrap the given target as {@link FacadeObject}
	     * 
	     * @param target
	     * @return
	     */
	    public static <T> FacadeObject<T> wrap(T target) {
	        return new FacadeObject<T>(target);
	    }
	    
	    /**
	     * Override the given object's toString() method
	     * 
	     * @see Objects2#toString(Object)
	     */
        @Override public String toString() {
            return Objects2.toString(this.delegate());
        }
        
        /**
         * Override the given object's hashCode() method
         * 
         * @see Objects2#hashCodes(Object)
         */
        @Override public int hashCode() {
            return hashCodes(this.delegate());
        }

        /**
         * Override the given object's equals() method
         * 
         * @see Objects2#isEqual(Object, Object)
         */
        @Override public boolean equals(Object obj) {
            if (null != obj && obj instanceof FacadeObject) {
                obj = ((FacadeObject<?>) obj).get();
            }
            
            return isEqual(this.delegate(), obj);
        }
        
        /**
         * Override the given object's clone() method
         * 
         * @see Reflecter#clones()
         */
        public T clone() {
            return Reflecter.from(this.delegate()).clones();
        }

        /**
         * Returns the delegate object self
         * 
         * @return
         */
        public T get() {
            return delegate();
        }
        
        private FacadeObject(T delegate) {
            this.delegate = delegate;
        }
        
        @Override protected T delegate() {
            return delegate;
        }
        
        /**
         * @see TypeRefer#asTypeDesc()
         */
        public TypeDescrib getType(String propName) {
            return TypeRefer.of(getField(propName)).asTypeDesc();
        }
        
        /**
         * @see Reflecter#field(String)
         */
        public Field getField(String propName) {
            return this.reflection().field(propName);
        }
        
        /**
         * @see Reflecter#val(String)
         */
        public <F> F getValue(String propName) {
            return this.reflection().val(propName);
        }
        
        /**
         * @see Resolves#get(TypeDescrib, Object)
         * @see Reflecter#val(String, Object)
         */
        public void setResolvedValue(String propName, Object propVal) {
            setValue(propName, Resolves.get(getType(propName), propVal));
        }
        
        /**
         * @see Reflecter#val(String, Object)
         */
        public <V> void setValue(String propName, V propVal) {
            this.reflection().val(propName, propVal);
        }
        
        /**
         * @see Reflecter#copyTo(Object)
         */
        public <Dest> Dest copyTo(Object dest) {
            return this.reflection().copyTo(dest);
        }
        
        /**
         * @see Reflecter#copyTo(Object, String...)
         */
        public <Dest> Dest copyTo(Object dest, String... excludes) {
            return this.reflection().copyTo(dest, excludes);
        }
        
        /**
         * @see Reflecter#asMap()
         */
        public <V> Map<String, V> asMap() {
            return this.reflection().asMap();
        }
        
        /**
         * @see Reflecter#mapper()
         */
        public <V> Mapper<String, V> mapper() {
            return this.reflection().mapper();
        }
        
        /**
         * @see Reflecter#populate(Map)
         */
        public <V> void populate(Map<String, V> properties) {
            this.reflection().populate(properties);
        }
        
        /**
         * @see Reflecter#populate(Map, String...)
         */
        public <V> void populate(Map<String, V> properties, String... excludes) {
            this.reflection().populate(properties, excludes);
        }
        
        /**
         * @see Reflecter#populate(Map, List)
         */
        public <V> void populate(Map<String, V> properties, List<String> excludes) {
            this.reflection().populate(properties, excludes);
        }
        
        /**
         * @see Reflecter#populate(String)
         */
        public void populate(String json, String... excludes) {
            this.reflection().populate(json, excludes);
        }
        
        /**
         * {@link Reflecter#populate4Test()}
         */
        public void populate4Test() {
            this.reflection().populate4Test();
        }
        
        /**
         * Log the delegate target as an easy-to-read JSON string
         */
        public void info() {
            if (log.isInfoEnabled()) {
                log.info(JsonW.of(this.delegate()).readable().asJson());
            }
        }
        
        /**
         * @see JsonW#asJson()
         */
        public String getJson() {
            return JsonW.toJson(this.delegate());
        }
        
        /**
         * Returns the {@link Reflecter} instance of the delegate object
         * 
         * @return
         */
        public Reflecter<T> reflection() {
            return (theRef.isPresent() 
                    ? theRef : (theRef = Optional.of(Reflecter.from(this.delegate())))).get();
        }
	    
        private T delegate;
        private Optional<Reflecter<T>> theRef = Optional.absent();
        
	}
	
	/**
	 * Convert as string with given object
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		return TO_STRING.apply(obj);
	}
	
	/**
	 * Generates a hash code for given object
	 * 
	 * @param obj
	 * @return
	 */
	public static int hashCodes(Object obj) {
		return HashValueBehavior.hashcodes(obj);
	}
	
	/**
	 * Returns true if the given objects is equals
	 * 
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean isEqual(Object obj1, Object obj2) {
		return EqualValueBehavior.is(obj1, obj2);
	}
	
	/**
	 * 
	 * @param <K>
	 * @param <V>
	 */
	private static class MapToStringDecision<K, V> extends Decisional<Entry<K, V>> {
		
		ToStringHelper helper;
		
		/**
		 * @param helper
		 */
		public MapToStringDecision(ToStringHelper helper) {
			this.helper = helper;
		}

		private Object wrap(Object obj) {
			//IS_STRING, IS_MAP, IS_COLLECTION, IS_PRIMITIVE, IS_DATE
			if (ObjToString.isBaseStructure(obj)) {
				
				return ObjToString.wrapper(obj);
			}
			
			return TO_STRING.apply(obj);
		}

		/* (non-Javadoc)
		 * @see com.taxi.util.Decisional#decision(java.lang.Object)
		 */
		@Override protected void decision(Entry<K, V> input) {
			helper.addValue(wrap(input.getKey()) + "=" + wrap(input.getValue()));
		}
	}

	/**
	 * 
	 * @param <T>
	 */
	private static class CollToStringDecision<T> implements Decision<T> {
		
		ToStringHelper helper;
		
		/**
		 * @param helper
		 */
		public CollToStringDecision(ToStringHelper helper) {
			this.helper = helper;
		}

		/* (non-Javadoc)
		 * @see com.taxi.util.Decisional#decision(java.lang.Object)
		 */
		@Override public boolean apply(T input) {
			if (ObjToString.isBaseStructure(input)) {
				helper.addValue(ObjToString.wrapper(input));
				return true;
			}
			
			helper.addValue(TO_STRING.apply(input));
			return true;
		}
	}
	
	/**
	 * 
	 */
	private static class ObjToString extends Decisional<Entry<String, Object>> {
		
		ToStringHelper helper;
		
		/**
		 * @param helper
		 */
		public ObjToString(ToStringHelper helper) {
			this.helper = helper;
		}

		/**
		 * 
		 * @param obj
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected static Object wrapper(Object obj) {
			boolean isMap = false;
			boolean isColl = false;
			
			if (null != obj && obj.getClass().isArray()) {
				if (allPrimitiveTypes().contains(obj.getClass().getComponentType())) {
					obj = Arrays2.primArrayToString(obj);
				} else {
					obj = Arrays.asList((Object[]) obj);
				}
			}
			
			if ((isMap = Map.class.isInstance(obj)) 
					|| (isColl = Collection.class.isInstance(obj))) {
				ToStringHelper tsHelper = MoreObjects.toStringHelper(Strs.EMPTY);
				
				if (isColl) {
					Gather.from((Collection<Object>) obj)
						.loop(new CollToStringDecision<Object>(tsHelper));
				}
				
				if (isMap) {
					Mapper.from((Map<Object, Object>) obj).entryGather()
						.loop(new MapToStringDecision<Object, Object>(tsHelper));
				}
				
				return tsHelper.toString();
			}
			
			return obj;
		}
		
		protected static boolean isBaseStructure(Object input) {
			return null == input
				|| isWrapperType(input.getClass())
				|| allPrimitiveTypes().contains(input)
				|| Map.class.isInstance(input)
				|| String.class.isInstance(input)
				|| Collection.class.isInstance(input)
				|| Date.class.isInstance(input)
				|| BigInteger.class.isInstance(input)
				|| BigDecimal.class.isInstance(input);
		}

		/* (non-Javadoc)
		 * @see com.taxi.util.Decisional#decision(java.lang.Object)
		 */
		@Override protected void decision(Entry<String, Object> entry) {
			helper.add(entry.getKey(), wrapper(entry.getValue()));
		}
	}
	
	/**
	 * 
	 * @param <W>
	 */
	protected static abstract class Exchanging<W> {
		
		Object unwraps(W el) {
			return new StructBehavior<Object>(el) {
				@Override protected Object booleanIf() { return unwrap((Boolean) this.delegate); }
				@Override protected Object byteIf() { return unwrap((Byte) this.delegate); }
				@Override protected Object characterIf() { return unwrap((Character) this.delegate); }
				@Override protected Object doubleIf() { return unwrap((Double) this.delegate); }
				@Override protected Object floatIf() { return unwrap((Float) this.delegate); }
				@Override protected Object integerIf() { return unwrap((Integer) this.delegate); }
				@Override protected Object longIf() { return unwrap((Long) this.delegate); }
				@Override protected Object shortIf() { return unwrap((Short) this.delegate); }
				@Override protected Object nullIf() { return null; }
				@Override protected Object noneMatched() { return null; }
			}.doDetect();
		}
		
		int unwrap(Integer el) { return ((Integer) el).intValue(); }
		boolean unwrap(Boolean el) { return ((Boolean) el).booleanValue(); }
		byte unwrap(Byte el) { return ((Byte) el).byteValue(); }
		char unwrap(Character el) { return ((Character) el).charValue(); }
		double unwrap(Double el) { return ((Double) el).doubleValue(); }
		float unwrap(Float el) { return ((Float) el).floatValue(); }
		long unwrap(Long el) { return ((Long) el).longValue(); }
		short unwrap(Short el) { return ((Short) el).shortValue(); }

		@SuppressWarnings("unchecked")
		W wrap(Object val) {
			return (W) val;
		}
	}
	
	private static class TostringValueBehavior extends ValueBehaviorAdapter<String> {
		
		static String asString(Object target) {
			return new TostringValueBehavior(target).doDetect();
		}

		private TostringValueBehavior(Object delegate) {
			super(delegate);
		}

		@Override protected String nullIf() {
			return NULL_STR;
		}

		@Override protected String primitiveIf() {
			return String.valueOf(this.delegate);
		}

		@Override protected <T> String arrayIf(T[] resolvedP, boolean isPrimitive) {
			if (isPrimitive) {
				return Arrays2.primArrayToString(this.delegate);
			}
			
			String arrS = Arrays2.primArrayToString(Arrays2.unwraps(resolvedP));
			if (null != arrS) {
				return arrS;
			}
			
			return listIf(Arrays.asList(resolvedP));
		}

		@Override protected <K, V> String mapIf(Map<K, V> resolvedP) {
			return ObjToString.wrapper(resolvedP).toString();
		}

		@Override protected <T> String setIf(Set<T> resolvedP) {
			return ObjToString.wrapper(resolvedP).toString();
		}

		@Override protected <T> String listIf(List<T> resolvedP) {
			return ObjToString.wrapper(resolvedP).toString();
		}

		@Override protected String beanIf() {
			ToStringHelper helper = MoreObjects.toStringHelper(defaultTostring(this.delegate));
			Reflecter.from(this.delegate).mapper().entryLoop(new ObjToString(helper));
			
			return helper.toString();
		}

		@Override protected String defaultBehavior() {
			return this.delegate.toString();
		}
		
	}
	
	private static class HashValueBehavior extends ValueBehaviorAdapter<Integer> {
		
		static int hashcodes(Object obj) {
			return new HashValueBehavior(obj).doDetect();
		}

		private HashValueBehavior(Object delegate) {
			super(delegate);
		}

		@Override protected Integer defaultBehavior() {
			return Objects.hashCode(this.delegate);
		}

		@Override protected <K, V> Integer mapIf(Map<K, V> resolvedP) {
			return Objects.hashCode(resolvedP.values().toArray());
		}

		@Override protected <T> Integer setIf(Set<T> resolvedP) {
			return Objects.hashCode(resolvedP.toArray());
		}

		@Override protected <T> Integer listIf(List<T> resolvedP) {
			return Objects.hashCode(resolvedP.toArray());
		}
		
		@Override protected <T> Integer arrayIf(T[] resolvedP, boolean isPrimitive) { 
			return Objects.hashCode(resolvedP); 
		}

		@Override protected Integer beanIf() {
			return Objects.hashCode(((Map<?, ?>) Reflecter.from(this.delegate).asMap()).values().toArray());
		}
	}
	
	private static class EqualValueBehavior extends ValueBehavior<Boolean> {
		Object obj2;
		Class<?> clazz1;
		static final String unEqualFmt = "%s: %s != %s";
		static final Log log = Loggers.from(EqualValueBehavior.class);
		
		static boolean is(Object obj1, Object obj2) {
			return new EqualValueBehavior(obj1, obj2).doDetect();
		}
		
		private EqualValueBehavior(Object delegate, Object obj2) {
			super(delegate);
			this.obj2 = obj2;
		}
		
		@Override protected Boolean nullIf() {
			if (null == obj2) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "null if", "null", obj2));
			}
			return false;
		}

		@Override protected Boolean afterNullIf() {
			this.clazz1 = this.delegate.getClass();
			if (null == obj2 || this.clazz1 != obj2.getClass()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(unEqualFmt, "after null if", this.clazz1.getName(), null == obj2 ? null : obj2.getClass().getName()));
				}
				return false;
			}
			
			return toBeContinued();
		}

		@Override protected <T> Boolean classIf(Class<T> resolvedP) {
			if (resolvedP == ((Class<?>) obj2)) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "class if", resolvedP.getName(), ((Class<?>) obj2).getName()));
			}
			return false;
		}

		@Override protected Boolean primitiveIf() {
			if (this.delegate == obj2) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "primitive if", this.delegate, obj2));
			}
			return false;
		}

		@Override protected Boolean eightWrapIf() {
			return new StructBehavior<Boolean>(this.delegate) {

				@Override protected Boolean booleanIf() {
					if (Booleans.unwrap((Boolean) this.delegate) == Booleans.unwrap((Boolean) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "boolean if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean byteIf() {
					if (Bytes.unwrap((Byte) this.delegate) == Bytes.unwrap((Byte) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "byte if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean characterIf() {
					if (Characters.unwrap((Character) this.delegate) == Characters.unwrap((Character) obj2)) {
						return true;
					}
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "character if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean doubleIf() {
					if (Doubles.unwrap((Double) this.delegate) == Doubles.unwrap((Double) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "double if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean floatIf() {
					if (Floats.unwrap((Float) this.delegate) == Floats.unwrap((Float) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "float if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean integerIf() {
					if (Integers.unwrap((Integer) this.delegate) == Integers.unwrap((Integer) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "integer if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean longIf() {
					if (Longs.unwrap((Long) this.delegate) == Longs.unwrap((Long) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "long if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean shortIf() {
					if (Shorts.unwrap((Short) this.delegate) == Shorts.unwrap((Short) obj2)) {
						return true;
					}
					
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "short if", this.delegate, obj2));
					}
					return false;
				}

				@Override protected Boolean nullIf() { 
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "eightWrapIf null if", this.delegate, obj2));
					}
					return false; 
				}
				
				@Override protected Boolean noneMatched() { 
					if (log.isDebugEnabled()) {
						log.debug(String.format(unEqualFmt, "eightWrapIf noneMatched", this.delegate, obj2));
					}
					return false; 
				}
				
			}.doDetect();
		}

		@Override protected Boolean dateIf(Date resolvedP) {
			if (resolvedP.getTime() == ((Date) obj2).getTime()) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "date if", Dater.of(resolvedP).asText(), Dater.of(((Date) obj2)).asText()));
			}
			return false;
		}

		@Override protected Boolean stringIf(String resolvedP) {
			if (resolvedP.equals(obj2)) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "string if", resolvedP, obj2));
			}
			return false;
		}

		@Override protected Boolean enumif(Enum<?> resolvedP) {
			if (resolvedP == ((Enum<?>) obj2)) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "enum if", resolvedP, obj2));
			}
			return false;
		}

		@Override protected <T> Boolean arrayIf(T[] resolvedP, boolean isPrimitive) {
			Object[] a1 = null, a2 = null;
			if (isPrimitive) {
				a1 = resolvedP;
				a2 = Arrays2.wraps(obj2);
			} else {
				a1 = resolvedP;
				a2 = (Object[]) obj2;
			}
			
			if (a1.length != a2.length) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(unEqualFmt, "array if length", a1.length, a2.length));
				}
				return false;
			}
			
			boolean arrayEqual = true;
			for (int i = 0; i < a1.length; i++) {
				if (!isEqual(a1[i], a2[i])) {
					arrayEqual = false;
					break;
				}
			}
			
			if (arrayEqual) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "array if", a1, a2));
			}
			return false;
		}

		@Override protected Boolean bigDecimalIf(BigDecimal resolvedP) {
			BigDecimal bd2 = (BigDecimal) obj2;
			if (resolvedP.equals(bd2)) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "bigDecima if", resolvedP, bd2));
			}
			return false;
		}

		@Override protected Boolean bigIntegerIf(BigInteger resolvedP) {
			BigInteger bi2 = (BigInteger) obj2;
			if (resolvedP.equals(bi2)) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "bigInteger if", resolvedP, bi2));
			}
			return false;
		}

		@Override protected <K, V> Boolean mapIf(Map<K, V> resolvedP) {
			Map<?, ?> m2 = ((Map<?, ?>) obj2);
			if (resolvedP.size() != m2.size()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(unEqualFmt, "map if size", resolvedP.size(), m2.size()));
				}
				return false;
			}
			
			boolean mapEqual = true;
			for (Object k : resolvedP.keySet()) {
				if (!isEqual(resolvedP.get(k), m2.get(k))) {
					mapEqual = false;
					if (log.isDebugEnabled()) {
					    log.debug("map if equal failed key: " + k);
					}
					break;
				}
			}
			
			if (mapEqual) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "map if", resolvedP, m2));
			}
			return false;
		}

		@Override protected <T> Boolean setIf(Set<T> resolvedP){
			Set<?> s2 = (Set<?>) obj2;
			if (resolvedP.size() != s2.size()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(unEqualFmt, "set if size", resolvedP.size(), s2.size()));
				}
				return false;
			}
			
			boolean setEqual = true;
			for (Object o : resolvedP) {
				if (!s2.contains(o)) {
					setEqual = false;
					break;
				}
			}
			
			if (setEqual) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "set if", resolvedP, s2));
			}
			return false;
		}

		@Override protected <T> Boolean listIf(List<T> resolvedP) {
			List<?> l2 = (List<?>) obj2;
			if (resolvedP.size() != l2.size()) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(unEqualFmt, "list if size", resolvedP.size(), l2.size()));
				}
				return false;
			}
			
			boolean listEqual = true;
			for (int i = 0; i < resolvedP.size(); i++) {
				if (!isEqual(resolvedP.get(i), l2.get(i))) {
					listEqual = false;
					break;
				}
			}
			
			if (listEqual) {
				return true;
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.format(unEqualFmt, "list if", resolvedP, l2));
			}
			return false;
		}

		@Override protected Boolean beanIf() {
			return isEqual(Reflecter.from(this.delegate).asMap(), Reflecter.from(obj2).asMap());
		}
	}
	
	protected static final Exchanging<Integer> Integers = new Exchanging<Integer>() { };
	protected static final Exchanging<Boolean> Booleans = new Exchanging<Boolean>() { };
	protected static final Exchanging<Byte> Bytes = new Exchanging<Byte>() { };
	protected static final Exchanging<Character> Characters = new Exchanging<Character>() { };
	protected static final Exchanging<Double> Doubles = new Exchanging<Double>() { };
	protected static final Exchanging<Float> Floats = new Exchanging<Float>() { };
	protected static final Exchanging<Long> Longs = new Exchanging<Long>() { };
	protected static final Exchanging<Short> Shorts = new Exchanging<Short>() { };
	protected static final Exchanging<Object> PRIMITIVE_EXCHANGING = new Exchanging<Object>() { };

	
}
