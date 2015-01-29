package com.benayn;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.Decisional;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Sources;
import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.TypeRefer;
import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.base.Domain;
import com.benayn.ustyle.behavior.ValueBehavior;
import com.benayn.ustyle.metest.generics.GenericsUtils;
import com.benayn.ustyle.metest.generics.TestGenerics;
import com.google.common.base.Defaults;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

public class Me3Test extends Me2Test {
    
    public static class Ran {
        Map<List<Set<Map<String, Long>>>, Set<List<com.benayn.berkeley.Person>>> theMap;
        Set<Map<BigInteger, List<com.benayn.berkeley.Person>>> theSet;
        List<Map<Double, Set<BigDecimal>>> theList;
    }
    
    @Test
    public void testRandoms() {
        Reflecter<Ran> reflecter = Reflecter.from(new Ran());
        TypeDescrib theMapType = TypeRefer.of(reflecter.field("theMap")).asTypeDesc();
        
        assertTrue(theMapType.isPair());
        assertEquals(Map.class, theMapType.rawClazz());
        assertTrue(theMapType.next().hasChild());
        assertTrue(theMapType.nextPairType().hasChild());
        
        Ran r = Randoms.get(new Ran());
        
        Gather.from(r.theList).info();
        assertTrue(r.theList instanceof List);
        for (Map<Double, Set<BigDecimal>> l : r.theList) {
            assertTrue(l instanceof Map);
            for (Double d : l.keySet()) {
                assertTrue(d instanceof Double);
                Set<BigDecimal> s = l.get(d);
                assertTrue(s instanceof Set);
                for (BigDecimal b : s) {
                    assertTrue(b instanceof BigDecimal);
                }
            }
        }
        
        Gather.from(r.theSet).info();
        assertTrue(r.theSet instanceof Set);
        for (Map<BigInteger, List<com.benayn.berkeley.Person>> m : r.theSet) {
            assertTrue(m instanceof Map);
            for (BigInteger b : m.keySet()) {
                assertTrue(b instanceof BigInteger);
                List<com.benayn.berkeley.Person> l = m.get(b);
                assertTrue(l instanceof List);
                for (com.benayn.berkeley.Person p : l) {
                    assertTrue(p instanceof com.benayn.berkeley.Person);
                }
            }
        }
        
        Mapper.from(r.theMap).info();
        assertTrue(r.theMap instanceof Map);
        for (List<Set<Map<String, Long>>> k : r.theMap.keySet()) {
            assertTrue(k instanceof List);
            for (Set<Map<String, Long>> k1 : k) {
                assertTrue(k1 instanceof Set);
                for (Map<String, Long> k2 : k1) {
                    assertTrue(k2 instanceof Map);
                    for (String k2k : k2.keySet()) {
                        assertTrue(k2k instanceof String);
                        assertTrue(k2.get(k2k) instanceof Long);
                    }
                }
            }
            
            Set<List<com.benayn.berkeley.Person>> v = r.theMap.get(k);
            assertTrue(v instanceof Set);
            for (List<com.benayn.berkeley.Person> l2 : v) {
                assertTrue(l2 instanceof List);
                for (com.benayn.berkeley.Person p : l2) {
                    assertTrue(p instanceof com.benayn.berkeley.Person);
                }
            }
        }
        
        assertTrue(Map.class.isAssignableFrom(Hashtable.class));
        assertTrue(Map.class.isAssignableFrom(Map.class));
        assertTrue(Decisions.isAssignableFrom(Hashtable.class, Map.class));
    }
    
    public static abstract class GenericTestClass<E, L, R, D, T> {
        
    }
    
    public static class TestMultiPos extends GenericTestClass<Long, String, Integer, Short, Byte> {
        
    }
    
    public static class TestMap extends HashMap<String, Domain> {

        /**
         * 
         */
        private static final long serialVersionUID = 4784235900575923800L;
        
    }
    
    public static class TestComplex extends Hashtable<GenericTestClass<Long, String, Integer, Short, Byte>, Map<String, Domain>> {

        /**
         * 
         */
        private static final long serialVersionUID = -8266183555389833001L;
        
    }
    
    private void testTestMultiPos(TypeDescrib desc) {
        assertFalse(desc.isPair());
        assertNull(desc.nextPairType());
        assertEquals(5, desc.size());
        assertEquals(Long.class, desc.next().rawClazz());
        assertEquals(String.class, desc.next(1).rawClazz());
        assertEquals(Integer.class, desc.next(2).rawClazz());
        assertEquals(Short.class, desc.next(3).rawClazz());
        assertEquals(Byte.class, desc.next(4).rawClazz());
        
        log.info(desc.toString());
    }
    
    private void testPair(TypeDescrib desc) {
        assertTrue(desc.isPair());
        assertNotNull(desc.nextPairType());
        assertEquals(String.class, desc.next().rawClazz());
        assertEquals(Domain.class, desc.nextPairType().rawClazz());
        assertEquals(2, desc.size());
        log.info(desc.toString());
    }
    
    @Test
    public void testTypeRefer3() {
        TypeRefer ref = TypeRefer.of(TestMultiPos.class.getGenericSuperclass());
        log.info(ref.asDesc());
        TypeDescrib desc = ref.asTypeDesc();
        testTestMultiPos(desc);
        
        ref = TypeRefer.of(TestMap.class.getGenericSuperclass());
        log.info(ref.asDesc());
        desc = ref.asTypeDesc();
        testPair(desc);
        
        ref = TypeRefer.of(TestComplex.class.getGenericSuperclass());
        log.info(ref.asDesc());
        
        desc = ref.asTypeDesc();
        assertTrue(desc.isPair());
        assertEquals(2, desc.size());
        assertEquals(Hashtable.class, desc.rawClazz());
        assertEquals(GenericTestClass.class, desc.next().rawClazz());
        testTestMultiPos(desc.next());
        testPair(desc.nextPairType());
    }
	
	@Test
	public void testTypeRefer2() {
		//List<Map<String, Set<Integer>>>
		Field f = Reflecter.from(Domain.class).field("complex");
		TypeDescrib typeD = TypeRefer.of(f).asTypeDesc();
		log.info(typeD.toString());
		
	}
	
	Object instance(TypeDescrib typeD) {
		//Has child
		if (typeD.hasChild()) {
			//Is pair
			if (typeD.isPair()) { 
				
			}
			//Is not pair
			else { 
				
			}
		}
		//Has no child
		else {
			return new InstanceBehavior(typeD.rawClazz()).doDetect();
		}
		
		return null;
	}
	
	class InstanceBehavior extends ValueBehavior<Object> {

		public InstanceBehavior(Object delegate) { super(delegate); }

		@Override protected <T> Object classIf(Class<T> resolvedP) {
			return toBeContinued();
		}

		@Override protected Object primitiveIf() {
			return Defaults.defaultValue(this.clazz);
		}

		@Override protected Object eightWrapIf() {
			return Defaults.defaultValue(Primitives.unwrap(this.clazz));
		}

		@Override protected Object dateIf(Date resolvedP) {
			return new Date();
		}

		@Override protected Object stringIf(String resolvedP) {
			return new String();
		}

		@Override protected Object enumif(Enum<?> resolvedP) {
			return null;
		}

		@Override protected <T> Object arrayIf(T[] resolvedP, boolean isPrimitive) {
			return ObjectArrays.newArray(this.clazz, 0);
		}

		@Override protected Object bigDecimalIf(BigDecimal resolvedP) {
			return new BigDecimal(0);
		}

		@Override protected Object bigIntegerIf(BigInteger resolvedP) {
			return new BigInteger("0");
		}

		@Override protected <K, V> Object mapIf(Map<K, V> resolvedP) {
			return Maps.newHashMap();
		}

		@Override protected <T> Object setIf(Set<T> resolvedP) {
			return Sets.newHashSet();
		}

		@Override protected <T> Object listIf(List<T> resolvedP) {
			return Lists.newArrayList();
		}

		@Override protected Object beanIf() {
			return Suppliers2.toInstance(this.clazz).get();
		}

		@Override protected Object nullIf() {
			return null;
		}
		
	}
	
	@Test
	public void testTypeRefer() {
		//Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>>
		String expectTypeStr = "java.util.Map<java.util.List<java.util.Map<java.lang.String, java.util.Set<java.lang.Integer>>>, " +
				"java.util.Set<java.util.List<java.util.Map<java.lang.String, java.util.Set<java.lang.Integer>>>>>";
		
		Field f = Reflecter.from(Domain.class).field("prop");
		String typeStr = TypeRefer.of(f).asDesc();
		
		assertEquals(expectTypeStr, typeStr);
		log.info(typeStr);
		
		TypeDescrib td = TypeRefer.of(new TypeToken<Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -7230615046487021463L;
		}).asTypeDesc();
		
		typeStr = td.desc();
		assertEquals(expectTypeStr, typeStr);
		log.info(typeStr);
		
		assertEquals("java.util.Map", td.rawClazz().getName());
		assertEquals("java.util.List", td.next().rawClazz().getName());
		assertEquals("java.util.Map", td.next().next().rawClazz().getName());
		assertEquals("java.lang.String", td.next().next().next().rawClazz().getName());
		assertEquals("java.util.Set", td.next().next().nextPairType().rawClazz().getName());
		assertEquals("java.lang.Integer", td.next().next().nextPairType().next().rawClazz().getName());
		
		assertEquals("java.util.Set", td.nextPairType().rawClazz().getName());
		assertEquals("java.util.List", td.nextPairType().next().rawClazz().getName());
		assertEquals("java.util.Map", td.nextPairType().next().next().rawClazz().getName());
		assertEquals("java.lang.String", td.nextPairType().next().next().next().rawClazz().getName());
		assertEquals("java.util.Set", td.nextPairType().next().next().nextPairType().rawClazz().getName());
		assertEquals("java.lang.Integer", td.nextPairType().next().next().nextPairType().next().rawClazz().getName());
		
		log.info(TypeRefer.of(Reflecter.from(Domain.class).field("sDomains")).asDesc());
	}
	
	@Test
	public void testJsonWfmt() throws IOException {
		String json = Sources.asString(Me3Test.class, "/unfmt.json");
		log.info(json);
		log.info("\n" + JsonW.fmtJson(json));
		log.info("\n" + JsonW.of(json).readable().fill("-----").aligns().asJson());
		
		log.info(JsonW.of(Domain.getDomain()).readable().fill(".....").align().asJson());
		log.info(JsonW.of(Domain.getDomain()).readable().fill(".....").asJson());
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	@Test
	public void testGeneric() {
		List<Map<String, Set<Integer>>> ll = new LinkedList<Map<String,Set<Integer>>>();
		
		//List<Map<String, Set<Integer>>> complex
		Field f = Reflecter.from(Domain.getDomain()).field("complex");
		Class<? extends Map<?, ?>> c1 = (Class<? extends Map<?, ?>>) GenericsUtils.getCollectionFieldType(f);
		Class<?> c2 = GenericsUtils.getMapKeyType(c1);
		Class<?> c3 = GenericsUtils.getMapValueType(c1);
		log.info(c1);
		log.info(c2);
		log.info(c3);
		log.info(GenericsUtils.getCollectionFieldType(f, 3));
		
		String typeStr = TestGenerics.typeToString(f.getGenericType());
		log.info(typeStr);
	}
	
	@Test
	public void testJsonW() {
		Domain d = Domain.getDomain();
		Stopwatch w1 = Stopwatch.createUnstarted(), w2 = Stopwatch.createUnstarted();
		
		w1.start();
		log.info(JsonW.toJson(d));
		log.info(w1.stop().elapsed(TimeUnit.NANOSECONDS));
		
		w2.start();
		log.info(toJson(d));
		log.info(w2.stop().elapsed(TimeUnit.NANOSECONDS));
		
		Reflecter.from(d).propLoop(new Decisional<Pair<Field, Object>>() {

			@Override protected void decision(Pair<Field, Object> input) {
				log.info("--------------- " + input.getL().getType().getName());
				//log.info(JsonW.of(input.getR()).dateFmt(DateStyle.DEFAULT).asJson());
				
//				StringWriter strW = new StringWriter();
//				try {
//					JsonW.of(input.getR()).write(strW);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				log.info(strW.toString());
				
				log.info(JsonW.toJson(input.getR()));
				log.info(toJson(input.getR()));
			}
		});
	}
	
	@Test
	public void testJson2() {
		Domain d = Domain.getDomain();
		
//		d.setDate(null);
//		d.setBytepArr(null);
//		d.setsDomains(null);
//		d.setComplex(null);
//		d.setsFloats(null);
		
		String json = toJson(d);
		log.info(json);
		
		JsonR sj = JsonR.of(json);
		sj.mapper().info();
		
		Domain d3 = sj.asObject(Domain.class);
		log.info(d);
		log.info(d3);
		
	}
	
	@Test
	public void testJson() throws IOException {
		
		//Mapper.from(JsonReader.jsonToMaps(json)).info();
        String json = Sources.asString(Me2Test.class, "/test.json");
        log.info(json);
        
        JsonR.of(json).mapper().info();
        JsonR.of(json).gather().info();
        
        String json2 = Sources.asString(Me2Test.class, "/test2.json");
        log.info(JsonR.of(json2).list());
		
		Object[] o = new Object[]{false, true, false};
		Boolean[] b = Arrays2.convert(o, Boolean.class);
		boolean[] b2 = Arrays2.unwrap(Arrays2.convert(o, Boolean.class));
		log.info(b);
		log.info(b2);
	}

}
