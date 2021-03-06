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

import org.junit.Test;

import com.benayn.berkeley.Person;
import com.benayn.pre.ShowGenerics;
import com.benayn.pre.ref.GenericsUtils;
import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.JSONer;
import com.benayn.ustyle.JSONer.ReadJSON;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Objects2.FacadeObject;
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
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

public class Me3Test extends Me2Test {
    
    public static class WrapObjectTest {
        String name;
        Integer age;
    }
    
    @Test
    public void testWrapObject2() {
        FacadeObject<WrapObjectTest> face = FacadeObject.wrap(new WrapObjectTest());
        assertTrue(face.isPublic());
        assertTrue(face.isStatic());
        assertTrue(face.isInnerClass());
        assertFalse(face.isPrivate());
        assertFalse(face.isAbstract());
        assertFalse(face.isFinal());
        assertFalse(face.isInterface());
        assertFalse(face.isProtected());

        Person wo1 = new Person();
        FacadeObject<Person> wrapObj1 = FacadeObject.wrap(wo1);
        assertTrue(wrapObj1.isPublic());
        assertFalse(wrapObj1.isStatic());
        assertFalse(wrapObj1.isInnerClass());
        assertFalse(wrapObj1.isPrivate());
        assertFalse(wrapObj1.isAbstract());
        assertFalse(wrapObj1.isFinal());
        assertFalse(wrapObj1.isInterface());
        assertFalse(wrapObj1.isProtected());
        
        wrapObj1.info();
        Map<String, Object> obj1Map = wrapObj1.asMap(); 
        for (String prop : obj1Map.keySet()) {
            if ("serialVersionUID".equals(prop)) {
                continue;
            }
            assertNull(obj1Map.get(prop));
        }
        assertTrue(Strs.contains(wrapObj1.getJson(), "null"));
        log.info(wrapObj1.getJson());
        assertTrue(Objects2.isEqual(wo1.getFirstName(), wrapObj1.getValue("firstName")));
        
        //
        
        wrapObj1.populate4Test();
        wrapObj1.info();
        obj1Map = wrapObj1.asMap();
        for (String prop : obj1Map.keySet()) {
            if ("serialVersionUID".equals(prop)) {
                continue;
            }
            assertNotNull(obj1Map.get(prop));
        }
        assertTrue(Objects2.isEqual(wo1.getFirstName(), wrapObj1.getValue("firstName")));
        assertFalse(Strs.contains(wrapObj1.getJson(), "null"));
        log.info(wrapObj1.getJson());
        
        Person p2 = Randoms.get(Person.class);
        assertFalse(Objects2.isEqual(wo1, p2));
        wrapObj1.populate(JSONer.toJson(p2));
        assertTrue(Objects2.isEqual(wo1, p2));
        wrapObj1.info();
        
        wrapObj1.populate(ImmutableMap.of("id", p2.getId() + 11L));
        assertFalse(Objects2.isEqual(wo1, p2));
        assertEquals(wo1.getId(), Long.valueOf((p2.getId() + 11L)));
        assertTrue(Strs.contains(wrapObj1.getJson(), p2.getId() + 11L + ""));
        
        p2.setFirstName(wo1.getFirstName() + "abc");
        assertFalse(Objects2.isEqual(p2.getFirstName(), wo1.getFirstName()));
        assertTrue(Objects2.isEqual(wo1.getFirstName(), wrapObj1.getValue("firstName")));
        wrapObj1.setValue("firstName", p2.getFirstName());
        assertDeepEqual(p2.getFirstName(), wrapObj1.getValue("firstName"));
        assertDeepEqual(p2.getFirstName(), wo1.getFirstName());
        
        Person p3 = new Person();
        assertDeepNotEqual(wo1, p3);
        wrapObj1.copyTo(p3, "firstName");
        assertDeepNotEqual(wo1, p3);
        wrapObj1.copyTo(p3);
        assertDeepEqual(wo1, p3);
        assertDeepEqual(wo1, wrapObj1.clone());
    }
    
    @Test
    public void testWrapObject() {
        WrapObjectTest wo1 = new WrapObjectTest();
        wo1.name = "test1";
        wo1.age = 15;
        log.info(wo1.toString());
        
        WrapObjectTest wo2 = new WrapObjectTest();
        wo2.name = "test1";
        wo2.age = 15;
        log.info(wo2.toString());
        
        assertFalse(Strs.contains(wo1.toString(), "name", "age"));
        assertFalse(Strs.contains(wo2.toString(), "name", "age"));
        assertFalse(wo1.equals(wo2));
        assertFalse(wo2.equals(wo1));
        assertNotEquals(wo1.hashCode(), wo2.hashCode());
        
        FacadeObject<WrapObjectTest> wrap1 = Objects2.wrapObj(wo1);
        FacadeObject<WrapObjectTest> wrap2 = Objects2.wrapObj(wo2);
        assertTrue(Strs.contains(wrap1.toString(), "name", "age"));
        assertTrue(Strs.contains(wrap2.toString(), "name", "age"));
        assertTrue(wrap1.equals(wrap2));
        assertTrue(wrap2.equals(wrap1));
        assertTrue(wrap1.equals(wo2));
        assertTrue(wrap2.equals(wo1));
        assertEquals(wrap1.hashCode(), wrap2.hashCode());
        
        assertNotNull(Objects2.wrapObj(null));
        assertEquals(null, Objects2.wrapObj(null).get());
        
        assertTrue(Objects2.isEqual(wo1, wrap1.clone()));
        assertTrue(Objects2.isEqual(wo2, wrap2.clone()));
    }
    
    @Test
    public void testFacadeObject() {
    	User user = new User();
    	FacadeObject<User> fo = FacadeObject.wrap(user);
    	fo.populate4Test();
    	
    	assertTrue(fo.getObject("address") instanceof User);
    	assertTrue(fo.getObject("address.lonlat") instanceof Address);
    	assertTrue(fo.getObject("address.lonlat.lat") instanceof Lonlat);
    	
    	assertTrue(fo.getValue("address") instanceof Address);
    	assertTrue(fo.getValue("address.lonlat") instanceof Lonlat);
    	assertTrue(fo.getValue("address.lonlat.lat") instanceof Double);
        
    	assertEquals(user.getAddress().getLonlat().getLat(), 
                fo.getValue("address.lonlat.lat"));
    	
    	double lat = 0.12;
    	assertNotEquals(lat, user.getAddress().getLonlat().getLat());
    	fo.setValue("address.lonlat.lat", lat);
    	assertEquals(fo.getValue("address.lonlat.lat"), user.getAddress().getLonlat().getLat());
    	assertEquals(fo.getValue("address.lonlat.lat"), lat);
    	
    }
    
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
        json = JSONer.toJson(jt);
        log.info(JSONer.fmtJson(json));
        JsonTest jt2 = JSONer.read(json).asObject(JsonTest.class);
        assertNotNull(jt2);
        log.info(jt);
        log.info(jt2);
        assertTrue(Objects2.isEqual(jt, jt2));
        
        Domain d = Randoms.get(Domain.class);
        json = JSONer.toJson(d);
        log.info(JSONer.fmtJson(json));
        Domain d2 = JSONer.read(json).asObject(Domain.class);
        assertTrue(Objects2.isEqual(d, d2));
        Domain d3 = Reflecter.from(d2).copyTo(Domain.class);
        assertTrue(Objects2.isEqual(d, d3));
        
        Person p = Randoms.get(Person.class);
        json = JSONer.toJson(p);
        log.info(JSONer.fmtJson(json));
        Person p2 = JSONer.read(json).asObject(Person.class);
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
        assertTrue(new byte[]{(byte)1}.getClass().isArray());
        assertTrue(new User[]{new User()}.getClass().isArray());
        assertTrue(new Object[]{new Object(), 1, 2.1, "cc", 'c'}.getClass().isArray());
        assertTrue(Byte[].class == Arrays2.wraps(new Object[]{115, 67}, Byte.class).getClass());
        assertTrue(Byte[].class == Arrays2.wraps(9, Byte[].class).getClass());
        assertTrue(Byte[].class == Resolves.get(Byte[].class, new Object[]{115, 67}).getClass());
        assertTrue(short[].class == Arrays2.unwrapArrayType(Short[].class));
        assertTrue(Long[].class == Arrays2.wrapArrayType(long[].class));
        
        ResolveTest rt = Randoms.get(ResolveTest.class);
        Reflecter<ResolveTest> ref = Reflecter.from(rt);
        
        assertTrue(Resolves.get(ref.field("shortWarr"), rt.shortParr) instanceof Short[]);
        assertTrue(Resolves.get(ref.field("shortParr"), rt.shortWarr) instanceof short[]);
        
        log.info(TypeRefer.of(String.class).asTypeDesc());
        log.info(TypeRefer.of(ref.field("shortParr")).asTypeDesc().rawClazz().getComponentType());
        log.info(TypeRefer.of(ref.field("shortWarr")).asTypeDesc().rawClazz().getComponentType());
        
        assertTrue(rt.shortParr.getClass().isArray());
        assertTrue(rt.shortWarr.getClass().isArray());
        
        assertTrue(Resolves.get(ref.field("booleanW"), "false") instanceof Boolean);
        assertTrue(Resolves.get(ref.field("booleanP"), Boolean.TRUE) instanceof Boolean);
        assertTrue(Resolves.get(ref.field("byte1"), 1) instanceof Byte);
        
        assertTrue(Resolves.get(ref.field("stringArr"), 1) instanceof String[]);
        
        //Set<List<Map<Byte, Long>>>
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
        
      //Set<List<Map<Byte, Long>>>
        Set<?> sr2 = Resolves.get(ref.field("byteSet"), rt.byteSet);
        assertTrue(sr2 instanceof Set);
        for (Object l1 : sr) {
            assertTrue(l1 instanceof List);
            for (Object m1 : (List<?>) l1) {
                assertTrue(m1 instanceof Map);
                for (Object b1 : ((Map<?, ?>) m1).keySet()) {
                    assertTrue(b1 instanceof Byte);
                    assertTrue(((Map<?, ?>) m1).get(b1) instanceof Long);
                }
            }
        }
        
        //List<Map<Short, Set<Map<Integer, Double>>>>
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
        
        //List<Map<Short, Set<Map<Integer, Double>>>>
        List<?> list2 = Resolves.get(ref.field("list"), rt.list);
        assertTrue(list2 instanceof List);
        for (Object m1 : list2) {
            assertTrue(m1 instanceof Map);
            for (Object s1 : ((Map<?, ?>) m1).keySet()) {
                assertTrue(s1 instanceof Short);
                Set<?> set1 = (Set<?>) ((Map<?, ?>) m1).get(s1);
                assertTrue(set1 instanceof Set);
                for (Object m2 : set1) {
                    assertTrue(m2 instanceof Map);
                    for (Object i1 : ((Map<?, ?>) m2).keySet()) {
                        assertTrue(i1 instanceof Integer);
                        assertTrue(((Map<?, ?>) m2).get(i1) instanceof Double);
                    }
                }
            }
        }
        
        //Map<Person, List<Set<Map<Float, Person>>>>
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
        
        //Map<Person, List<Set<Map<Float, Person>>>>
        Map<?, ?> map2 = Resolves.get(ref.field("map"), rt.map);
        assertTrue(map2 instanceof Map);
        for (Object p1 : map2.keySet()) {
            assertTrue(p1 instanceof Person);
            List<?> l1 = (List<?>) ((Map<?, ?>) map2).get(p1);
            assertTrue(l1 instanceof List);
            for (Object s1 : l1) {
                assertTrue(s1 instanceof Set);
                for (Object m2 : ((Set<?>) s1)) {
                    assertTrue(m2 instanceof Map);
                    for (Object  f1 : ((Map<?, ?>) m2).keySet()) {
                        assertTrue(f1 instanceof Float);
                        assertTrue(((Map<?, ?>) m2).get(f1) instanceof Person);
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
	
//	@Test
//	public void testJsonWfmt() throws IOException {
//		String json = Sources.asString(Me3Test.class, "/unfmt.json");
//		log.info(json);
//		log.info("\n" + JsonW.fmtJson(json));
//		log.info("\n" + JsonW.of(json).readable().fill("-----").aligns().asJson());
//		
//		log.info(JsonW.of(Domain.getDomain()).readable().fill(".....").align().asJson());
//		log.info(JsonW.of(Domain.getDomain()).readable().fill(".....").asJson());
//	}
	
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
		
		String typeStr = ShowGenerics.typeToString(f.getGenericType());
		log.info(typeStr);
	}
	
//	@Test
//	public void testJsonW() {
//		Domain d = Domain.getDomain();
//		Stopwatch w1 = Stopwatch.createUnstarted(), w2 = Stopwatch.createUnstarted();
//		
//		w1.start();
//		log.info(JsonW.toJson(d));
//		log.info(w1.stop().elapsed(TimeUnit.NANOSECONDS));
//		
//		w2.start();
//		log.info(toJson(d));
//		log.info(w2.stop().elapsed(TimeUnit.NANOSECONDS));
//		
//		Reflecter.from(d).propLoop(new Decisional<Pair<Field, Object>>() {
//
//			@Override protected void decision(Pair<Field, Object> input) {
//				log.info("--------------- " + input.getL().getType().getName());
//				//log.info(JsonW.of(input.getR()).dateFmt(DateStyle.DEFAULT).asJson());
//				
////				StringWriter strW = new StringWriter();
////				try {
////					JsonW.of(input.getR()).write(strW);
////				} catch (IOException e) {
////					e.printStackTrace();
////				}
////				log.info(strW.toString());
//				
//				log.info(JsonW.toJson(input.getR()));
//				log.info(toJson(input.getR()));
//			}
//		});
//	}
	
	@Test
    public void testJsonJackson() {
        String json = null;
        ReadJSON jr = null;
        
        JsonJacksonPO jjt = Randoms.get(JsonJacksonPO.class);
        log.info(jjt);
        
        json = toJson(jjt);
        log.info(json);
        
        jr = JSONer.read(json);
        
        JsonJacksonPO jjt2 = jr.asObject(JsonJacksonPO.class);
        log.info(jjt2);
        
        assertTrue(Objects2.isEqual(jjt, jjt2));
        
    }
	
	@Test
	public void testJson() throws IOException {
		
		//Mapper.from(JsonReader.jsonToMaps(json)).info();
        String json = Sources.asString(Me2Test.class, "/test.json");
        log.info(json);
        
        JSONer.read(json).mapper().info();
        JSONer.read(json).gather().info();
        
        String json2 = Sources.asString(Me2Test.class, "/test2.json");
        log.info(JSONer.readList(json2));
		
		Object[] o = new Object[]{false, true, false};
		Boolean[] b = Arrays2.convert(o, Boolean.class);
		boolean[] b2 = Arrays2.unwrap(Arrays2.convert(o, Boolean.class));
		log.info(b);
		log.info(b2);
	}
	
    public static class User {
        private String name;
        private int age;
        private Date birth;
        private Address address;
        private Map<Byte, List<Float>> testMap; 

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        public Date getBirth() {
            return birth;
        }
        public void setBirth(Date birth) {
            this.birth = birth;
        }
        public Address getAddress() {
            return address;
        }
        public void setAddress(Address address) {
            this.address = address;
        }
        public Map<Byte, List<Float>> getTestMap() {
            return testMap;
        }
        public void setTestMap(Map<Byte, List<Float>> testMap) {
            this.testMap = testMap;
        }
        
    }

    public static class Address {
        private Integer code;
        private String detail;
        private Lonlat lonlat;
        public Integer getCode() {
            return code;
        }
        public void setCode(Integer code) {
            this.code = code;
        }
        public String getDetail() {
            return detail;
        }
        public void setDetail(String detail) {
            this.detail = detail;
        }
        public Lonlat getLonlat() {
            return lonlat;
        }
        public void setLonlat(Lonlat lonlat) {
            this.lonlat = lonlat;
        }
        
    }

    public static class Lonlat {
        private double lon;
        private double lat;
        public double getLon() {
            return lon;
        }
        public void setLon(double lon) {
            this.lon = lon;
        }
        public double getLat() {
            return lat;
        }
        public void setLat(double lat) {
            this.lat = lat;
        }

    }

}
