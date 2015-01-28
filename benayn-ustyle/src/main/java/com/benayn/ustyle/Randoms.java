package com.benayn.ustyle;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.benayn.ustyle.behavior.StructBehavior;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

public final class Randoms {
	
	/**
	 * 
	 */
	protected static final Log log = Loggers.from(Randoms.class);
	
	/**
	 * Returns the given object's instance that fill properties with the random value.
	 * 
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Object target) {
		return (T) new RandomStructBehavior(target).doDetect();
	}
	
	/**
	 * 
	 */
	private static class RandomStructBehavior extends StructBehavior<Object> {
		private Field field = null;
		private SecureRandom r = new SecureRandom();
		public RandomStructBehavior(Object delegate) { super(delegate); }

		@Override protected Object booleanIf() { return r.nextBoolean(); }
		@Override protected Object byteIf() { return ((Integer) r.nextInt(Byte.MAX_VALUE - 1)).byteValue(); }
		@Override protected Object characterIf() { 
			char[] chs = UUID.randomUUID().toString().toCharArray();
			return chs[r.nextInt(chs.length - 1)]; 
		}
		@Override protected Object doubleIf() { return r.nextDouble(); } 
		@Override protected Object floatIf() { return r.nextFloat(); } 
		@Override protected Object integerIf() { return r.nextInt(); } 
		@Override protected Object longIf() { return r.nextLong(); } 
		@Override protected Object shortIf() { return ((Integer) r.nextInt(Short.MAX_VALUE - 1)).shortValue(); }
		@Override protected Object nullIf() { return null; }
		@Override protected Object afterNullIf() {
			if (this.delegate instanceof Field) {
				this.field = ((Field) this.delegate);
				this.delegate = this.field.getType();
			}
			return toBeContinued();
		}

		@Override protected Object noneMatched() {
			return new RandomValueBehavior(this.clazz, this.field).doDetect();
		}
	}
	
	/**
	 * 
	 */
	private static class RandomValueBehavior extends ValueBehavior<Object> {
		private Field field = null;
		private SecureRandom r = new SecureRandom();
		public RandomValueBehavior(Object delegate, Field field) { super(delegate); this.field = field; }
		@Override protected <T> Object classIf(Class<T> resolvedP) { return toBeContinued(); }

		@Override protected Object primitiveIf() { return null; }
		@Override protected Object eightWrapIf() { return null; }
		@Override protected Object stringIf(String resolvedP) { return UUID.randomUUID().toString().replace('-', 'c'); }

		@Override protected Object dateIf(Date resolvedP) {
			return Dater.now().set()
					.years(Dater.now().asCalendar().get(Calendar.YEAR))
					.months(Math.max(2, r.nextInt(11)))
					.days(Math.max(2, r.nextInt(27)))
					.hours(Math.max(2, r.nextInt(23)))
					.minutes(Math.max(2, r.nextInt(59)))
					.second(Math.max(2, r.nextInt(59)))
					.get(); 
		}
		
		@Override protected Object enumif(Enum<?> resolvedP) {
			Enum<?>[] enums = (Enum<?>[]) this.clazz.getEnumConstants();
			return enums[r.nextInt(enums.length - 1)];
		}

		@Override protected <T> Object arrayIf(T[] resolvedP, boolean isPrimitive) {
			int l = Math.max(2, r.nextInt(5));
			if (isPrimitive) {
				this.clazz = Primitives.wrap(this.clazz);
			}
			Object[] rArr = ObjectArrays.newArray(this.clazz, l);
			for (int i = 0; i < rArr.length; i++) {
				rArr[i] = get(this.clazz);
			}
			if (isPrimitive) {
				return Arrays2.unwraps(rArr);
			}
			
			return rArr;
		}

		@Override protected Object bigDecimalIf(BigDecimal resolvedP) { return BigDecimal.valueOf(r.nextDouble()); }
		@Override protected Object bigIntegerIf(BigInteger resolvedP) { return BigInteger.valueOf(r.nextLong()); }

		@SuppressWarnings("unchecked") @Override protected <K, V> Object mapIf(Map<K, V> resolvedP) {
			if (null == this.field) {
				return null;
			}
			
			//java.util.Map<java.lang.String, java.lang.Integer>
			ParameterizedType parameterizedType = (ParameterizedType) this.field.getGenericType();
			//interface java.util.Map
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            //[class java.lang.String, class java.lang.Integer]
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            //[K, V]
            //TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
            
            //avoid dead loop
            if (field.getDeclaringClass() == actualTypeArguments[0]
            		|| field.getDeclaringClass() == actualTypeArguments[1]) {
            	return null;
            }
            if (isTypeArgInterf(actualTypeArguments[0])
            		|| isTypeArgInterf(actualTypeArguments[1])) {
            	return null;
            }
            
            Map<K, V> rMap = null;
            if (rawType == Map.class) {
            	rMap = Maps.newHashMap();
            } else {
            	rMap = (Map<K, V>) Suppliers2.toInstance(rawType).get();
            }
            
            if (null == rMap) {
            	return null;
            }
            
            try {
            	for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
    				rMap.put((K) get(getRawType(actualTypeArguments[0])), (V) get(getRawType(actualTypeArguments[1])));
    			}
			} catch (Exception e) {
				return null;
			}
            
			return rMap;
		}
		
		@SuppressWarnings("unchecked") @Override protected <T> Object setIf(Set<T> resolvedP) {
			if (null == this.field) {
				return null;
			}
			
			ParameterizedType parameterizedType = (ParameterizedType) this.field.getGenericType();
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            
            //avoid dead loop
            if (field.getDeclaringClass() == actualTypeArguments[0]) {
            	return null;
            }
            if (isTypeArgInterf(actualTypeArguments[0])) {
            	return null;
            }
            
            Set<T> rSet = null;
            if (rawType == Set.class) {
            	rSet = Sets.newHashSet();
            } else {
            	rSet = (Set<T>) Suppliers2.toInstance(rawType).get();
            }
            
            if (null == rSet) {
            	return null;
            }
            
            try {
            	for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
    				rSet.add((T) get(getRawType(actualTypeArguments[0])));
    			}
			} catch (Exception e) {
				return null;
			}
            
            return rSet;
		}
		
		@SuppressWarnings("unchecked") @Override protected <T> Object listIf(List<T> resolvedP) {
			if (null == this.field) {
				return null;
			}
			
			ParameterizedType parameterizedType = (ParameterizedType) this.field.getGenericType();
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            
            //avoid dead loop
            if (field.getDeclaringClass() == actualTypeArguments[0]) {
            	return null;
            }
            
            if (isTypeArgInterf(actualTypeArguments[0])) {
            	return null;
            }
            
            List<T> rList = null;
            if (rawType == List.class) {
            	rList = Lists.newArrayList();
            } else {
            	rList = (List<T>) Suppliers2.toInstance(rawType).get();
            }
            
            if (null == rList) {
            	return null;
            }
            
            try {
            	for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
    				rList.add((T) get(getRawType(actualTypeArguments[0])));
    			}
			} catch (Exception e) {
				return null;
			}
            
            return rList;
		}
		
		private Object getRawType(Object o) {
			try {
				return ((ParameterizedType) o).getRawType();
			} catch (Exception e) {
				return o;
			}
		}
		
		private boolean isTypeArgInterf(Type actualTypeArgument) {
			try {
				return ((Class<?>) ((ParameterizedType) actualTypeArgument).getRawType()).isInterface();
			} catch (Exception e) {
				return false;
			}
		}

		@Override protected Object beanIf() { return Reflecter.from(this.clazz).populate4Test().get(); }
		@Override protected Object nullIf() { return null; }
		
	}
	
	private Randoms() {}
}
