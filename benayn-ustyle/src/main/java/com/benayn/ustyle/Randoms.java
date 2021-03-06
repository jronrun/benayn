package com.benayn.ustyle;

import static com.benayn.ustyle.TypeRefer.of;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.behavior.StructBehavior;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

/**
 * https://github.com/jronrun/benayn
 */
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
		    Dater dater = Dater.now();
			return dater.set()
					.years(dater.getYear())
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

		@SuppressWarnings("unchecked")
        @Override protected <K, V> Object mapIf(Map<K, V> resolvedP) {
			if (null == this.field) {
                Map<K, V> rMap = (Map<K, V>) (this.clazz.isInterface() 
			            ? Maps.newHashMap() : (Map<K, V>) Suppliers2.toInstance(this.clazz).get());
			    for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
			        rMap.put((K) get(String.class), (V) get(Integer.class));
			    }
			    return rMap;
			}
			
			TypeDescrib typeDesc = of(this.field).asTypeDesc();
			
			//avoid dead loop
			if (field.getDeclaringClass() == typeDesc.next().rawClazz()
			        || field.getDeclaringClass() == typeDesc.nextPairType().rawClazz()) {
			    return null;
			}
			
			return getInst(typeDesc);
			
//			//java.util.Map<java.lang.String, java.lang.Integer>
//			ParameterizedType parameterizedType = (ParameterizedType) this.field.getGenericType();
//			//interface java.util.Map
//            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
//            //[class java.lang.String, class java.lang.Integer]
//            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
//            //[K, V]
//            //TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
//            
//            //avoid dead loop
//            if (field.getDeclaringClass() == actualTypeArguments[0]
//            		|| field.getDeclaringClass() == actualTypeArguments[1]) {
//            	return null;
//            }
//            if (isTypeArgInterf(actualTypeArguments[0])
//            		|| isTypeArgInterf(actualTypeArguments[1])) {
//            	return null;
//            }
//            
//            Map<K, V> rMap = null;
//            if (rawType == Map.class) {
//            	rMap = Maps.newHashMap();
//            } else {
//            	rMap = (Map<K, V>) Suppliers2.toInstance(rawType).get();
//            }
//            
//            if (null == rMap) {
//            	return null;
//            }
//            
//            try {
//            	for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
//    				rMap.put((K) get(getRawType(actualTypeArguments[0])), (V) get(getRawType(actualTypeArguments[1])));
//    			}
//			} catch (Exception e) {
//				return null;
//			}
//            
//			return rMap;
		}
		
		@SuppressWarnings("unchecked")
        @Override protected <T> Object setIf(Set<T> resolvedP) {
		    if (null == this.field) {
		        Set<T> rSet = (Set<T>) (this.clazz.isInterface() 
                        ? Sets.newHashSet() : (Set<T>) Suppliers2.toInstance(this.clazz).get());
                for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
                    rSet.add((T) get(String.class));
                }
                return rSet;
            }
			
			TypeDescrib typeDesc = of(this.field).asTypeDesc();
            
            //avoid dead loop
            if (field.getDeclaringClass() == typeDesc.next().rawClazz()) {
                return null;
            }
            
            return getInst(typeDesc);
		}
		
		@SuppressWarnings("unchecked")
        @Override protected <T> Object listIf(List<T> resolvedP) {
		    if (null == this.field) {
		        List<T> rList = (List<T>) (this.clazz.isInterface() 
                        ? Lists.newArrayList() : (List<T>) Suppliers2.toInstance(this.clazz).get());
                for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
                    rList.add((T) get(String.class));
                }
                return rList;
            }
			
			TypeDescrib typeDesc = of(this.field).asTypeDesc();
            
            //avoid dead loop
            if (field.getDeclaringClass() == typeDesc.next().rawClazz()) {
                return null;
            }
            
            return getInst(typeDesc);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" }) private Object getInst(TypeDescrib type) {
		    Class<?> clazz = type.rawClazz();
		    
		    if (type.hasChild()) {
		        //Set<Map<String, List<Long>>>
		        
		        boolean isInterf = clazz.isInterface();
		        
		        //Map
		        if (Map.class.isAssignableFrom(clazz)) {
		            Map map = null;
		            if (isInterf) {
		                map = Maps.newHashMap();
		            } else {
		                map = (Map) Suppliers2.toInstance(clazz).get();
		            }
		            
		            for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
	                    map.put(getInst(type.next()), getInst(type.nextPairType()));
	                }
		            
		            return map;
		        }
		        //Set
		        else if (Set.class.isAssignableFrom(clazz)) {
		            Set set = null;
		            if (isInterf) {
		                set = Sets.newHashSet();
		            } else {
		                set = (Set) Suppliers2.toInstance(clazz).get();
		            }
		            
		            for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
	                    set.add(getInst(type.next()));
	                }
		            
		            return set;
		        }
		        //List
		        else if (List.class.isAssignableFrom(clazz)) {
		            List list = null;
		            if (isInterf) { 
		                list = Lists.newArrayList();
		            } else {
		                list = (List) Suppliers2.toInstance(clazz).get();
		            }
		            
		            for (int i = 0; i < Math.max(2, r.nextInt(5)); i++) {
	                    list.add(getInst(type.next()));
	                }
		            return list;
		        }
		        
		    }

		    return get(clazz);
		}
		
//		private Object getRawType(Object o) {
//			try {
//				return ((ParameterizedType) o).getRawType();
//			} catch (Exception e) {
//				return o;
//			}
//		}
//		
//		private boolean isTypeArgInterf(Type actualTypeArgument) {
//			try {
//				return ((Class<?>) ((ParameterizedType) actualTypeArgument).getRawType()).isInterface();
//			} catch (Exception e) {
//				return false;
//			}
//		}

		@Override protected Object beanIf() { return Reflecter.from(this.clazz).populate4Test().get(); }
		@Override protected Object nullIf() { return null; }
		
	}
	
	private Randoms() {}
}
