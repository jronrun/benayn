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

import com.benayn.berkeley.Person;
import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.Decisional;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Resolves;
import com.benayn.ustyle.Sources;
import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.TypeRefer;
import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.base.Domain;
import com.benayn.ustyle.base.EnumType;
import com.benayn.ustyle.base.JsonJacksonPO;
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
    
    public static enum EnumTest {
        TEST1, TEST2
    }
    
    public static class JsonTest {
        Map<Short, Long>                                                           simpleMap;
        Set<Byte>                                                                  simpleSet;
        List<Short>                                                                simpleList;
        byte[]                                                                     bytePArr;
        Short[]                                                                    shortWArr;
        EnumTest                                                                   enumTest;
        double[]                                                                   doublePArr;

        Map<String, Integer>                                                       mStrInt;
        Map<Long, Boolean>                                                         mLongBool;

        Set<Float>                                                                 sFloats;

        List<String>                                                               lStrs;
        List<Map<String, Set<Integer>>>                                            complex;

        EnumType                                                                   enum4Test;
        BigDecimal                                                                 bigDecimal;
        BigInteger                                                                 bigInteger;

        Date                                                                       date;
        String                                                                     string;

        Long                                                                       longa;
        Long[]                                                                     longaArr;
        long                                                                       longp;
        long[]                                                                     longpArr;

        Integer                                                                    integera;
        Integer[]                                                                  integeraArr;
        int                                                                        integerp;
        int[]                                                                      integerpArr;

        Short                                                                      shorta;
        Short[]                                                                    shortaArr;
        short                                                                      shortp;
        short[]                                                                    shortpArr;

        Byte                                                                       bytea;
        Byte[]                                                                     byteaArr;
        byte                                                                       bytep;
        byte[]                                                                     bytepArr;

        Double                                                                     doublea;
        Double[]                                                                   doubleaArr;
        double                                                                     doublep;
        double[]                                                                   doublepArr;

        Float                                                                      floata;
        Float[]                                                                    floataArr;
        float                                                                      floatp;
        float[]                                                                    floatpArr;

        Boolean                                                                    booleana;
        Boolean[]                                                                  booleanaArr;
        boolean                                                                    booleanp;
        boolean[]                                                                  booleanpArr;

        Character                                                                  charactera;
        Character[]                                                                characteraArr;
        char                                                                       characterp;
        char                                                                       characterpArr;

        Map<List<Long>, Set<Double>>                                               t1;
        Map<Set<Byte>, List<Short>>                                                t2;

        Map<List<String>, Set<Double>>                                             t3;
        Map<Set<String>, List<Byte>>                                               t4;

        Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> prop;
        
        Map<Person, Set<Short>> propObj;
        Map<List<Person>, Set<Person>> propObj2;
    }
    
    
    @Test
    public void testJsonRW() {
        String json = null;
        
        JsonTest jt = Randoms.get(JsonTest.class);
        assertNotNull(jt);
        JsonTest jt3 = Reflecter.from(jt).copyTo(JsonTest.class);
        assertTrue(Objects2.isEqual(jt, jt3));
        json = JsonW.toJson(jt);
        log.info(JsonW.fmtJson(json));
        JsonTest jt2 = JsonR.of(json).asObject(JsonTest.class);
        assertNotNull(jt2);
        log.info(jt);
        log.info(jt2);
        assertTrue(Objects2.isEqual(jt, jt2));
        
        Domain d = Randoms.get(Domain.class);
        json = JsonW.toJson(d);
        log.info(JsonW.fmtJson(json));
        Domain d2 = JsonR.of(json).asObject(Domain.class);
        assertTrue(Objects2.isEqual(d, d2));
        Domain d3 = Reflecter.from(d2).copyTo(Domain.class);
        assertTrue(Objects2.isEqual(d, d3));
        
        Person p = Randoms.get(Person.class);
        json = JsonW.toJson(p);
        log.info(JsonW.fmtJson(json));
        Person p2 = JsonR.of(json).asObject(Person.class);
        assertTrue(Objects2.isEqual(p, p2));
        Person p3 = Reflecter.from(p).copyTo(Person.class);
        assertTrue(Objects2.isEqual(p, p3));
        
    }
    
    public static class ResolveTest {
        boolean booleanP;
        Boolean booleanW;
        
        byte byte1;
        short[] shortParr;
        Short[] shortWarr;
        String[] stringArr;
        
        Set<List<Map<Byte, Long>>> byteSet;
        List<Map<Short, Set<Map<Integer, Double>>>> list;
        Map<Person, List<Set<Map<Float, Person>>>> map;
    }
    
    @Test
    public void testResolves() {
        ResolveTest rt = Randoms.get(ResolveTest.class);
        Reflecter<ResolveTest> ref = Reflecter.from(rt);
        
        log.info(TypeRefer.of(String.class).asTypeDesc());
        log.info(TypeRefer.of(ref.field("shortParr")).asTypeDesc().rawClazz().getComponentType());
        log.info(TypeRefer.of(ref.field("shortWarr")).asTypeDesc().rawClazz().getComponentType());
        
        assertTrue(rt.shortParr.getClass().isArray());
        assertTrue(rt.shortWarr.getClass().isArray());
        
        assertTrue(Resolves.get(ref.field("booleanW"), "false") instanceof Boolean);
        assertTrue(Resolves.get(ref.field("booleanP"), Boolean.TRUE) instanceof Boolean);
        assertTrue(Resolves.get(ref.field("byte1"), 1) instanceof Byte);
        
        assertTrue(Resolves.get(ref.field("shortParr"), rt.shortWarr) instanceof short[]);
        assertTrue(Resolves.get(ref.field("shortWarr"), rt.shortParr) instanceof Short[]);
        
        assertTrue(Resolves.get(ref.field("stringArr"), 1) instanceof String[]);
        
        Set<List<Map<Byte, Long>>> sr = Resolves.get(ref.field("byteSet"), rt.byteSet);
        assertTrue(sr instanceof Set);
        for (List<Map<Byte, Long>> l1 : sr) {
            assertTrue(l1 instanceof List);
            for (Map<Byte, Long> m1 : l1) {
                assertTrue(m1 instanceof Map);
                for (Byte b1 : m1.keySet()) {
                    assertTrue(b1 instanceof Byte);
                    assertTrue(m1.get(b1) instanceof Long);
                }
            }
        }
        
        List<Map<Short, Set<Map<Integer, Double>>>> list = Resolves.get(ref.field("list"), rt.list);
        assertTrue(list instanceof List);
        for (Map<Short, Set<Map<Integer, Double>>> m1 : list) {
            assertTrue(m1 instanceof Map);
            for (Short s1 : m1.keySet()) {
                assertTrue(s1 instanceof Short);
                Set<Map<Integer, Double>> set1 = m1.get(s1);
                assertTrue(set1 instanceof Set);
                for (Map<Integer, Double> m2 : set1) {
                    assertTrue(m2 instanceof Map);
                    for (Integer i1 : m2.keySet()) {
                        assertTrue(i1 instanceof Integer);
                        assertTrue(m2.get(i1) instanceof Double);
                    }
                }
            }
        }
        
        Map<Person, List<Set<Map<Float, Person>>>> map = Resolves.get(ref.field("map"), rt.map);
        assertTrue(map instanceof Map);
        for (Person p1 : map.keySet()) {
            assertTrue(p1 instanceof Person);
            List<Set<Map<Float, Person>>> l1 = map.get(p1);
            assertTrue(l1 instanceof List);
            for (Set<Map<Float, Person>> s1 : l1) {
                assertTrue(s1 instanceof Set);
                for (Map<Float, Person> m2 : s1) {
                    assertTrue(m2 instanceof Map);
                    for (Float  f1 : m2.keySet()) {
                        assertTrue(f1 instanceof Float);
                        assertTrue(m2.get(f1) instanceof Person);
                    }
                }
            }
        }
    }
    
    public static class RefTest {
        Map<Byte, List<Short>> map;
        List<Set<Map<Short, Long>>> list;
        Set<Map<Integer, List<Byte>>> set;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReflecterAsMap() {
        RefTest rt = Randoms.get(RefTest.class);
        log.info(rt);
        Map<String, Object> m = Reflecter.from(rt).asMap();
        
        Set<Map<Integer, List<Byte>>> propSet = (Set<Map<Integer, List<Byte>>>) m.get("set");
        assertTrue(propSet instanceof Set);
        for (Map<Integer, List<Byte>> m2 : propSet) {
            assertTrue(m2 instanceof Map);
            for (Integer i : m2.keySet()) {
                assertTrue(i instanceof Integer);
                List<Byte> l1 = m2.get(i);
                assertTrue(l1 instanceof List);
                for (Byte b1 : l1) {
                    assertTrue(b1 instanceof Byte);
                }
            }
        }
        
        List<Set<Map<Short, Long>>> propList = (List<Set<Map<Short, Long>>>) m.get("list");
        assertTrue(propList instanceof List);
        for (Set<Map<Short, Long>> s : propList) {
            assertTrue(s instanceof Set);
            for (Map<Short, Long> m1 : s) {
                assertTrue(m1 instanceof Map);
                for (Short s1 : m1.keySet()) {
                    assertTrue(s1 instanceof Short);
                    assertTrue(m1.get(s1) instanceof Long);
                }
            }
        }
        
        Map<Byte, List<Short>> propMap = (Map<Byte, List<Short>>) m.get("map");
        assertTrue(propMap instanceof Map);
        for (Byte b : propMap.keySet()) {
            assertTrue(b instanceof Byte);
            List<Short> v = propMap.get(b);
            assertTrue(v instanceof List);
            for (Short s : v) {
                assertTrue(s instanceof Short);
            }
        }
        
    }
    
    public static class Ran {
        Map<List<Set<Map<String, Long>>>, Set<List<Person>>> theMap;
        Set<Map<BigInteger, List<Person>>> theSet;
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
        for (Map<BigInteger, List<Person>> m : r.theSet) {
            assertTrue(m instanceof Map);
            for (BigInteger b : m.keySet()) {
                assertTrue(b instanceof BigInteger);
                List<Person> l = m.get(b);
                assertTrue(l instanceof List);
                for (Person p : l) {
                    assertTrue(p instanceof Person);
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
            
            Set<List<Person>> v = r.theMap.get(k);
            assertTrue(v instanceof Set);
            for (List<Person> l2 : v) {
                assertTrue(l2 instanceof List);
                for (Person p : l2) {
                    assertTrue(p instanceof Person);
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
    public void testJsonJackson() {
        String json = null;
        JsonR jr = null;
        
        JsonJacksonPO jjt = Randoms.get(JsonJacksonPO.class);
        log.info(jjt);
        
        json = toJson(jjt);
        log.info(json);
        
        jr = JsonR.of(json);
        
        JsonJacksonPO jjt2 = jr.asObject(JsonJacksonPO.class);
        log.info(jjt2);
        
        assertTrue(Objects2.isEqual(jjt, jjt2));
        
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
