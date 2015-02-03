package com.benayn;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.benayn.ustyle.Dater;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.base.Domain;
import com.benayn.ustyle.metest.generics.GenericsUtils;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

public class Me2Test extends MeTest {
	
	
	//---------------------
	
	private abstract class CustomMap<T> extends AbstractMap<String, Integer> {
    }
    private abstract class OtherCustomMap<T> implements Map<String, Integer> {
    }
    
    @SuppressWarnings("rawtypes") private interface Foo {

		Map<String, Integer> a();
		Map<?, ?> b();
		Map<?, ? extends Set> b2();
		Map<?, ? super Set> b3();
		Map c();
		CustomMap<Date> d();
		CustomMap<?> d2();
		CustomMap d3();

		OtherCustomMap<Date> e();
		OtherCustomMap<?> e2();
		OtherCustomMap e3();
	}
	
	protected Type getType(Method method) {
		return GenericsUtils.getMapValueReturnType(method);
	}

	protected Class<?> targetClass = Foo.class;
    protected String methods[] = new String[]{"a", "b", "b2", "b3", "c", "d", "d2", "d3", "e", "e2", "e3"};
    protected Type expectedResults[] = new Class[]{
            Integer.class, null, Set.class, Set.class, null, Integer.class,
            Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
    
    @Test
    public void testGenericsUtils() throws SecurityException, NoSuchMethodException {
        for (int i = 0; i < this.methods.length; i++) {
        	Method method = this.targetClass.getMethod(this.methods[i]);
            Type type = getType(method);
            assertEquals(this.expectedResults[i], type);
        }
    }
    
    //-----------------------
    
    @Test
    public void testTypeTo() {
    	TypeToken<List<String>> stringListType = Util.<String>listType();
    	log.info(stringListType);
    }
    
	static class Util {
		static <T> TypeToken<List<T>> listType() {
			return new TypeToken<List<T>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 7745332644799895653L;
			};
		}
	}
    
	@Test
	public void testDaterTmp() {
		Date now = new Date();
		assertEquals(now.getTime() - Dater.DAY * 7, Dater.of(now).add().day(-7).get().getTime());
		assertEquals(24*60*60*1000, Dater.DAY);
		System.out.println(new Date().getTime());
		Date dateStar = new Date();
		int days = 3;
		for (int i = 0; i < days; i++) {
			System.out.println(Dater.of(dateStar).add().day(i).asText("yyyyMMdd"));
		}
	}
	
	@Test
	public void testObjFill4Test() {
		Domain d = Reflecter.from(Domain.class).populate4Test().get();
		log.info(Objects2.toString(d));
		log.info(d);
		log.info(Randoms.get(Domain.class));
		
		int[] ia = new int[]{1,2,3,4,5,6,7,8,9,0};
		assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9, 0]", Objects2.toString(ia));
		
		Domain d1 = Randoms.get(Domain.class);
		Domain d2 = Reflecter.from(d1).clones();
		Reflecter.from(d2).mapper().info();
		assertTrue(Objects2.isEqual(d1, d2));
	}
	
	@Test
	public void testTypeToken() {
		assertEquals("java.lang.String", new IKnowMyType<String>() {}.getMyType().toString());
		assertEquals(String.class, new IKnowMyType<String>() {}.getMyType().getRawType());
		
		IKnowMyType<LinkedList<HashMap<String, Set<Integer>>>> type = new IKnowMyType<LinkedList<HashMap<String, Set<Integer>>>>() { };
		log.info(type.getMyType().getRawType());
		
		//List<Map<String, Set<Integer>>> ll = new LinkedList<Map<String,Set<Integer>>>();
	}
	
	private static Class<?> getClass(Type type, int i) {
        if (type instanceof ParameterizedType) { // 处理泛型类型
            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            return (Class<?>) getClass(((TypeVariable<?>) type).getBounds()[0], 0); // 处理泛型擦拭对象<R>
        } else {// class本身也是type，强制转型
            return (Class<?>) type;
        }
    }

    private static Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) { // 处理多级泛型
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
        } else if (genericClass instanceof TypeVariable) { // 处理泛型擦拭对象<R>
            return (Class<?>) getClass(((TypeVariable<?>) genericClass).getBounds()[0], 0);
        } else {
            return (Class<?>) genericClass;
        }
    }
	
	static <K, V> TypeToken<Map<K, V>> mapOf( Class<K> keyType, Class<V> valueType) {
	    return new TypeToken<Map<K, V>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5781653436802867344L;}
	    
	        .where(new TypeParameter<K>() {}, keyType)
	        .where(new TypeParameter<V>() {}, valueType);
	}

	static <K, V> TypeToken<Map<K, V>> mapOf(TypeToken<K> keyType, TypeToken<V> valueType) {
		return new TypeToken<Map<K, V>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5781653436802867344L; }
		
			.where(new TypeParameter<K>() { }, keyType)
			.where(new TypeParameter<V>() { }, valueType);
	}

}
		   
abstract class IKnowMyType<T> {
	TypeToken<T> getMyType() {
		return new TypeToken<T>(getClass()) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6438454656575803067L;
		};
	}
}
