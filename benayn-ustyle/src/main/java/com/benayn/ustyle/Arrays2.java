package com.benayn.ustyle;

import static com.google.common.collect.ObjectArrays.newArray;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.benayn.ustyle.Objects2.Exchanging;
import com.benayn.ustyle.behavior.StructBehavior;
import com.google.common.base.Defaults;
import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Primitives;

/**
 * https://github.com/jronrun/benayn
 */
public final class Arrays2 {
	
	/**
	 * 
	 */
	public static final Object[] EMPTY_ARRAY = new Object[0];
	private static BiMap<Class<?>, Class<?>> primToWrapArrayClasses = HashBiMap.create();
	
	static {
        primToWrapArrayClasses.put(boolean[].class,     Boolean[].class);
        primToWrapArrayClasses.put(byte[].class,        Byte[].class);
        primToWrapArrayClasses.put(char[].class,        Character[].class);
        primToWrapArrayClasses.put(double[].class,      Double[].class);
        primToWrapArrayClasses.put(float[].class,       Float[].class);
        primToWrapArrayClasses.put(int[].class,         Integer[].class);
        primToWrapArrayClasses.put(long[].class,        Long[].class);
        primToWrapArrayClasses.put(short[].class,       Short[].class);
	}
	
	/**
	 * Returns the wrapper array type or {@code type} itself if not matched.
	 * 
	 * @param type
	 * @return
	 */
	public static Class<?> wrapArrayType(Class<?> type) {
        Class<?> clazz = primToWrapArrayClasses.get(type);
	    return null != clazz ? clazz : type;
	}
	
	/**
	 * Returns the primitive array type or {@code type} itself if not matched.
     *
	 * @param type
	 * @return
	 */
	public static Class<?> unwrapArrayType(Class<?> type) {
        Class<?> clazz = (Class<?>) primToWrapArrayClasses.inverse().get(type);
        return null != clazz ? clazz : type;
    }

	/**
	 * Returns an list containing all of the given elements
	 * 
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, I> List<T> asList(I... target) {
		return (List<T>) Arrays.asList(asArray(target));
	}
	
	/**
	 * Returns an array containing all of the given elements
	 * 
	 * @param target
	 * @return
	 */
	public static <T> T[] asArray(T... target) {
		return target;
	}
	
	/**
	 * Converts the given array of primitive ints to object Integers.
	 * 
	 * @param target
	 * @return
	 */
	public static Integer[] wrap(int... target) {
		return doWrap(Objects2.Integers, target);
	}
	
	/**
	 * Converts the given array of object Integers to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static int[] unwrap(Integer... target) {
		return (int[]) doUnwrap(Objects2.Integers, target);
	}
	
	/**
	 * Converts the given array of primitive booleans to object Booleans.
	 * 
	 * @param target
	 * @return
	 */
	public static Boolean[] wrap(boolean... target) {
		return doWrap(Objects2.Booleans, target);
	}
	
	/**
	 * Converts the given array of object Booleans to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static boolean[] unwrap(Boolean... target) {
		return (boolean[]) doUnwrap(Objects2.Booleans, target);
	}
	
	/**
	 * Converts the given array of primitive bytes to object Bytes.
	 * 
	 * @param target
	 * @return
	 */
	public static Byte[] wrap(byte... target) {
		return doWrap(Objects2.Bytes, target);
	}
	
	/**
	 * Converts the given array of object Bytes to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static byte[] unwrap(Byte... target) {
		return (byte[]) doUnwrap(Objects2.Bytes, target);
	}
	
	/**
	 * Converts the given array of primitive chars to object Characters.
	 * 
	 * @param target
	 * @return
	 */
	public static Character[] wrap(char... target) {
		return doWrap(Objects2.Characters, target);
	}
	
	/**
	 * Converts the given array of object Characters to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static char[] unwrap(Character... target) {
		return (char[]) doUnwrap(Objects2.Characters, target);
	}
	
	/**
	 * Converts the given array of primitive doubles to object Doubles.
	 * 
	 * @param target
	 * @return
	 */
	public static Double[] wrap(double... target) {
		return doWrap(Objects2.Doubles, target);
	}
	
	/**
	 * Converts the given array of object Doubles to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static double[] unwrap(Double... target) {
		return (double[]) doUnwrap(Objects2.Doubles, target);
	}
	
	/**
	 * Converts the given array of primitive floats to object Floats.
	 * 
	 * @param target
	 * @return
	 */
	public static Float[] wrap(float... target) {
		return doWrap(Objects2.Floats, target);
	}
	
	/**
	 * Converts the given array of object Floats to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static float[] unwrap(Float... target) {
		return (float[]) doUnwrap(Objects2.Floats, target);
	}
	
	/**
	 * Converts the given array of primitive longs to object Longs.
	 * 
	 * @param target
	 * @return
	 */
	public static Long[] wrap(long... target) {
		return doWrap(Objects2.Longs, target);
	}
	
	/**
	 * Converts the given array of object Longs to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static long[] unwrap(Long... target) {
		return (long[]) doUnwrap(Objects2.Longs, target);
	}
	
	/**
	 * Converts the given array of primitive shorts to object Shorts.
	 * 
	 * @param target
	 * @return
	 */
	public static Short[] wrap(short... target) {
		return doWrap(Objects2.Shorts, target);
	}
	
	/**
	 * Converts the given array of object Shorts to primitives.
	 * 
	 * @param target
	 * @return
	 */
	public static short[] unwrap(Short... target) {
		return (short[]) doUnwrap(Objects2.Shorts, target);
	}
	
	/**
	 *
	 */
	public static <T> T[] convert(final Object[] target, Class<T> clazz) {
		return new StructBehavior<T[]>(clazz) {

			@SuppressWarnings("unchecked") @Override protected T[] booleanIf() {
				Boolean[] b = new Boolean[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Boolean.valueOf(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] byteIf() {
				Byte[] b = new Byte[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Byte.parseByte(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] characterIf() {
				Character[] b = new Character[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Character.valueOf(target[i].toString().charAt(0));
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] doubleIf() {
				Double[] b = new Double[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Doubles.tryParse(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] floatIf() {
				Float[] b = new Float[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Floats.tryParse(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] integerIf() {
				Integer[] b = new Integer[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Ints.tryParse(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] longIf() {
				Long[] b = new Long[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Longs.tryParse(target[i].toString());
				}
				return (T[]) b;
			}

			@SuppressWarnings("unchecked") @Override protected T[] shortIf() {
				Short[] b = new Short[target.length];
				for (int i = 0; i < target.length; i++) {
					b[i] = Short.parseShort(target[i].toString());
				}
				return (T[]) b;
			}

			@Override protected T[] nullIf() { return null; }
			@Override protected T[] noneMatched() { return null; }
			
		}.doDetect();
	}
	
	/**
	 * Returns the primitive array {@link Object#toString()} result
	 * 
	 * @param input
	 * @return
	 */
	public static String primArrayToString(Object input) {
		return new StructBehavior<String>(input) {

			@Override protected String booleanIf() { return Arrays.toString((boolean[]) this.delegate); }
			@Override protected String byteIf() { return Arrays.toString((byte[]) this.delegate); }
			@Override protected String characterIf() { return Arrays.toString((char[]) this.delegate); }
			@Override protected String doubleIf() { return Arrays.toString((double[]) this.delegate); }
			@Override protected String floatIf() { return Arrays.toString((float[]) this.delegate); }
			@Override protected String integerIf() { return Arrays.toString((int[]) this.delegate); }
			@Override protected String longIf() { return Arrays.toString((long[]) this.delegate); }
			@Override protected String shortIf() { return Arrays.toString((short[]) this.delegate); }
			@Override protected String nullIf() { return null; }
			@Override protected String noneMatched() { return null; }
			
		}.doDetect();
	}
	
	/**
	 * Converts the given object array to primitive array object.
	 * 
	 * @param target
	 * @return
	 */
	public static <T> Object unwraps(T[] target) {
		return new StructBehavior<Object>(target) {

			@Override protected Object booleanIf() { return unwrap((Boolean[]) this.delegate); }
			@Override protected Object byteIf() { return unwrap((Byte[]) this.delegate); }
			@Override protected Object characterIf() { return unwrap((Character[]) this.delegate); }
			@Override protected Object doubleIf() { return unwrap((Double[]) this.delegate); }
			@Override protected Object floatIf() { return unwrap((Float[]) this.delegate); }
			@Override protected Object integerIf() { return unwrap((Integer[]) this.delegate); }
			@Override protected Object longIf() { return unwrap((Long[]) this.delegate); }
			@Override protected Object shortIf() { return unwrap((Short[]) this.delegate); }
			@Override protected Object nullIf() { return null; }
			@Override protected Object noneMatched() { return this.delegate; }
			
		}.doDetect();
	}
	
	/**
     * Converts the given target as an array of primitive to object.
     * 
     * @param target
     * @return
     */
    public static <T> T[] wraps(Object target) {
        return wraps(target, null);
    }
    
    /**
     * Adds the element at the specified position from the specified array
     * 
     * @param array
     * @param index
     * @param element
     * @param clss
     * @return
     */
    static Object add(Object array, int index, Object element, Class<?> clss) {
        if (array == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
            }
            Object joinedArray = Array.newInstance(clss, 1);
            Array.set(joinedArray, 0, element);
            return joinedArray;
        }
        int length = Array.getLength(array);
        if (index > length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        Object result = Array.newInstance(clss, length + 1);
        System.arraycopy(array, 0, result, 0, index);
        Array.set(result, index, element);
        if (index < length) {
            System.arraycopy(array, index, result, index + 1, length - index);
        }
        return result;
    }
    
    /**
     * Returns the length of the specified array object, as an int.
     * 
     * @param array
     * @return
     */
    public static int length(Object array) {
        return null == array ? 0 : Array.getLength(array);
    }
    
    /**
     * Removes the element at the specified position from the specified array
     * 
     * @param array
     * @param index
     * @return
     */
    public static Object remove(Object array, int index) {
        int length = length(array);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }

        Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }

        return result;
    }
	
	/**
	 * Converts the given target as an array of primitive to object.
	 * 
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] wraps(Object target, Class<?> expectClazz) {
		if (null == target) {
			return null;
		}
		
		Class<?> clazz = target.getClass();
		
		if (null != expectClazz) {
		    expectClazz = expectClazz.isArray() ? expectClazz.getComponentType() : expectClazz;
		    Function<Object, T> func = (Function<Object, T>) Funcs.getParseFunction(expectClazz);
		    
		    if (null != func) {
		        expectClazz = Primitives.wrap(expectClazz);
		        T[] expectArray = null;
		        
		        //target is array
		        if (clazz.isArray()) {
		            Object[] providArray = wraps(target);
		            expectArray = (T[]) ObjectArrays.newArray(expectClazz, providArray.length);
		            
                    for (int i = 0; i < providArray.length; i++) {
                        expectArray[i] = func.apply(providArray[i]);
                    }
                }
		        //target is not array
		        else {
		            expectArray = (T[]) ObjectArrays.newArray(expectClazz, 1);
		            expectArray[0] = func.apply(target);
		        }
		        
		        return expectArray;
		    }
		    
		}
		
		if (!clazz.isArray()) {
			T[] a = (T[]) ObjectArrays.newArray(clazz, 1);
			a[0] = (T) target;
			return a;
		}
		
		return new StructBehavior<T[]>(target) {

			@Override protected T[] primitiveIs(boolean primitive) { return primitive ? toBeContinued() : noneMatched(); }
			@Override protected T[] booleanIf() { return (T[]) wrap((boolean[]) this.delegate); }
			@Override protected T[] byteIf() { return (T[]) wrap((byte[]) this.delegate); }
			@Override protected T[] characterIf() { return (T[]) wrap((char[]) this.delegate); }
			@Override protected T[] doubleIf() { return (T[]) wrap((double[]) this.delegate); }
			@Override protected T[] floatIf() { return (T[]) wrap((float[]) this.delegate); }
			@Override protected T[] integerIf() { return (T[]) wrap((int[]) this.delegate); }
			@Override protected T[] longIf() { return (T[]) wrap((long[]) this.delegate); }
			@Override protected T[] shortIf() { return (T[]) wrap((short[]) this.delegate); }
			@Override protected T[] nullIf() { return null; }
			@Override protected T[] noneMatched() { return (T[]) this.delegate; }
			
		}.doDetect();
	}
	
	private static <T> T[] doWrap(Exchanging<T> exch, Object target) {
		if (null == target) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		T[] r = newArray((Class<T>) Primitives.wrap(target.getClass().getComponentType()), Array.getLength(target));
		if (0 == r.length) {
			return r;
		}
		
		for (int i = 0; i < r.length; i++) {
			r[i] = exch.wrap(Array.get(target, i));
		}
		
		return r;
	}
	
	private static <T> Object doUnwrap(Exchanging<T> exch, T... target) {
		if (null == target) {
			return null;
		}
		
		Class<?> clazz = Primitives.unwrap(target.getClass().getComponentType());
		Object r = Array.newInstance(clazz, target.length);
		if (0 == target.length) {
			return r;
		}
		
		T el = null;
		Object defaultVal = Defaults.defaultValue(clazz);
		for (int i = 0; i < target.length; i++) {
			Array.set(r, i, (null == (el = target[i])) ? defaultVal : exch.unwraps(el));
		}
		
		return r;
	}
	
}
