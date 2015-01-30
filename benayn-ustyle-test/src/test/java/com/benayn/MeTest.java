/**
 * 
 */
package com.benayn;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.Comparer;
import com.benayn.ustyle.DateStyle;
import com.benayn.ustyle.Dater;
import com.benayn.ustyle.Decision;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Funcs;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Scale62;
import com.benayn.ustyle.SimplePurview;
import com.benayn.ustyle.SixtytwoScale;
import com.benayn.ustyle.Sources;
import com.benayn.ustyle.base.Domain;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Betner;
import com.benayn.ustyle.string.Finder;
import com.benayn.ustyle.string.Indexer;
import com.benayn.ustyle.string.Replacer;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

/**
 *
 */
public class MeTest extends Assert {
	
//	protected Logger log = Loggers.from(getClass());
//	protected org.apache.commons.logging.Log log = Loggers.from(getClass());
	protected Log log = Loggers.from(getClass());
	
	
	@Test
	public void testTmp() {
		log.info(Integer.class);
		log.info(Objects2.TO_STRING.apply(new Float[]{Float.valueOf("33.3"), Float.valueOf("44.4")}));
	}
	
	@Test
	public void testObjects2Hashcode() {
		assertEquals(Objects2.hashCodes(BigDecimal.valueOf(37.73)), Objects2.hashCodes(new BigDecimal("37.73")));
		assertEquals(Objects2.hashCodes(BigInteger.valueOf(123456789)), Objects2.hashCodes(new BigInteger("123456789")));
		
		assertEquals(Objects2.hashCodes(getTestMap()), Objects2.hashCodes(getTestMap()));
		assertEquals(Objects2.hashCodes(getTestMap2()), Objects2.hashCodes(getTestMap2()));
		
		assertEquals(Objects2.hashCodes(getTestPerson()), Objects2.hashCodes(getTestPerson()));
		assertEquals(Objects2.hashCodes(getTestPersons()), Objects2.hashCodes(getTestPersons()));
		
		assertEquals(Objects2.hashCodes(new Float[]{(float) 33.320000 }), Objects2.hashCodes(Arrays2.asArray(Float.valueOf("33.320000"))));
		assertEquals(Objects2.hashCodes(null), Objects2.hashCodes(null));
		
		Pair<Person, Map<String, Object>> p1 = Pair.of(getTestPerson(), getTestMap());
		Pair<Person, Map<String, Object>> p2 = Pair.of(getTestPerson(), getTestMap());
		assertEquals(p1.hashCode(), p2.hashCode());
		assertTrue(p1.equals(p2));
		
		Pair<List<Person>, Map<Object, Object>> p11 = Pair.of(getTestPersons(), getTestMap2());
		Pair<List<Person>, Map<Object, Object>> p22 = Pair.of(getTestPersons(), getTestMap2());
		assertEquals(p11.hashCode(), p22.hashCode());
		assertTrue(p11.equals(p22));
		
		assertEquals(Objects2.EQUAL_HASHCODE_WRAP.apply(p11).hashCode(), p11.hashCode());
		assertTrue(Objects2.EQUAL_HASHCODE_WRAP.apply(p11).equals(p22));
		
		assertEquals(p11.toString(), p22.toString());
		assertEquals(p11.toString(), Objects2.toString(p11));
		
		assertTrue(Objects2.isEqual(Decisions.isEmpty(), Decisions.isEmpty()));
		assertTrue(Objects2.isEqual(Decisions.isBaseClass(), Decisions.isBaseClass()));
		assertTrue(Objects2.isEqual(Decisions.isBaseStructure(), Decisions.isBaseStructure()));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testObjects2IsEqual() {
		assertTrue(Objects2.isEqual(getTestMap(), getTestMap()));
		assertTrue(Objects2.isEqual(getTestMap2(), getTestMap2()));
		assertTrue(Objects2.isEqual(getTestPerson(), getTestPerson()));
		assertTrue(Objects2.isEqual(getTestPersons(), getTestPersons()));
		
		Person p = getTestPerson();
		p.setName("name");
		assertFalse(Objects2.isEqual(p, getTestPerson()));
		
		Map<String, Object> m = getTestMap();
		m.put("a", 3);
		assertFalse(Objects2.isEqual(m, getTestMap()));
		
		assertTrue(Objects2.isEqual(BigInteger.valueOf(123456789), new BigInteger("123456789")));
		assertTrue(Objects2.isEqual(BigDecimal.valueOf(123.30021000), new BigDecimal("123.30021")));
		
		String dateS = "2012-11-12 23:03:28";
		assertFalse(Objects2.isEqual(Dater.of(dateS).get(), new Date()));
		assertTrue(Objects2.isEqual(Dater.of(dateS).get(), Dater.from(dateS).get()));
		
		assertTrue(Objects2.isEqual(
			ImmutableSet.of(3, "3", Float.valueOf("22.0001000")),
			ImmutableSet.of(Float.valueOf("22.0001000"), "3", 3)
		));
		
		assertFalse(Objects2.isEqual(
			ImmutableSet.of(3, "3", Float.valueOf("22.0001000")),
			ImmutableSet.of(Float.valueOf("22.0001000"), "3", 3, (byte) 11)
		));
		
		assertTrue(Objects2.isEqual(Decisions.isBaseClass(), Decisions.ClazzDecision.IS_BASE_CLASS));
		assertTrue(Objects2.isEqual(Decisions.isBaseClass(), Decisions.isBaseClass()));
		
		assertFalse(Objects2.isEqual(Integer.class, int.class));
		assertTrue(Objects2.isEqual(Integer.class, Integer.class));
		
		Map<String, Object> m1 = getTestMap();
		Map<String, Object> m2 = getTestMap();

//		Map<Object, Object> m = ImmutableMap.<Object, Object>of(
//				33, new Person("hello", 33), 
//				44, 44, 
//				55, ImmutableMap.of(
//						333, new Person("3 ceng person", 333),
//						444, 444,
//						555, ImmutableMap.of(
//								3333, new Person("4 ceng person", 3333),
//								4444, 4444,
//								5555, ImmutableMap.of(
//										33333, new Person("5 ceng person", 33333),
//										44444, 44444,
//										55555, mapTest()
//									)
//							)
//					)
//			);
		Map<Object, Object> m2l1 = (Map<Object, Object>) m2.get("maptest");
		Map<Integer, Object> m2l2 = (Map<Integer, Object>) m2l1.get(55);
		Map<Integer, Object> m2l3 = (Map<Integer, Object>) m2l2.get(555);
		Map<Integer, Object> m2l4 = (Map<Integer, Object>) m2l3.get(5555);
		Map<Integer, Object> m2l5 = (Map<Integer, Object>) m2l4.get(55555);
//		Map<Object, Object> m = ImmutableMap.<Object, Object>of(
//				66, new Person("hello", 66), 
//				77, 77, 
//				88, ImmutableMap.of(
//						666, new Person("3 ceng person", 666),
//						777, 777,
//						888, ImmutableMap.of(
//								6666, new Person("4 ceng person", 6666),
//								7777, 7777,
//								8888, ImmutableMap.of(
//										66666, new Person("5 ceng person", 66666),
//										77777, 77777,
//										88888, 88888
//										99999, innM
//										)
//								)
//						)
//				);
		
		Map<Integer, Object> m2l6 = (Map<Integer, Object>) m2l5.get(88);
		Map<Integer, Object> m2l7 = (Map<Integer, Object>) m2l6.get(888);
		Map<Integer, Object> m2l8 = (Map<Integer, Object>) m2l7.get(8888);
		Map<Integer, Object> m2l9 = (Map<Integer, Object>) m2l8.get(99999);
		assertTrue(Objects2.isEqual(m1, m2));
		assertTrue(Objects2.isEqual(888888, m2l9.get(888888)));
		
		m2l9.put(8888888, "diff");
		assertFalse(Objects2.isEqual(m1, m2));
		
		m2l9.remove(8888888);
		assertTrue(Objects2.isEqual(m1, m2));
		
		m2l9.put(8888888, getTestPerson());
		assertFalse(Objects2.isEqual(m1, m2));
		
		Map<Object, Object> m1l1 = (Map<Object, Object>) m1.get("maptest");
		Map<Integer, Object> m1l2 = (Map<Integer, Object>) m1l1.get(55);
		Map<Integer, Object> m1l3 = (Map<Integer, Object>) m1l2.get(555);
		Map<Integer, Object> m1l4 = (Map<Integer, Object>) m1l3.get(5555);
		Map<Integer, Object> m1l5 = (Map<Integer, Object>) m1l4.get(55555);
		
		Map<Integer, Object> m1l6 = (Map<Integer, Object>) m1l5.get(88);
		Map<Integer, Object> m1l7 = (Map<Integer, Object>) m1l6.get(888);
		Map<Integer, Object> m1l8 = (Map<Integer, Object>) m1l7.get(8888);
		Map<Integer, Object> m1l9 = (Map<Integer, Object>) m1l8.get(99999);
		
		assertTrue(Objects2.isEqual(888888, m1l9.get(888888)));
		
		m1l9.put(8888888, getTestPerson());
		assertTrue(Objects2.isEqual(m1, m2));
		
		Person pM1l9 = (Person) m1l9.get(8888888);
		pM1l9.setAge(28);
		assertFalse(Objects2.isEqual(m1, m2));
		
		pM1l9.setAge(999);
		assertTrue(Objects2.isEqual(m1, m2));
		
		log.info(pM1l9);
		pM1l9.getAddr().getInnerAddr().getInnerMap().put("different", 1);
		log.info(pM1l9);
		assertFalse(Objects2.isEqual(m1, m2));
		
		pM1l9.getAddr().getInnerAddr().getInnerMap().remove("different");
		assertTrue(Objects2.isEqual(m1, m2));
		
		m1 = Maps.newHashMap(m2);
		assertTrue(Objects2.isEqual(m1, m2));

		Person pM2l9 = (Person) m2l9.get(8888888);
		pM2l9.setAge(18);
		// pM2l9 same instance
		assertTrue(Objects2.isEqual(m1, m2));
		
	}
	
	@Test
	public void testArray() {
		isArrEquals(new Byte[]{33}, Arrays2.asArray((byte) 33));
		isArrEquals(new Byte[]{33,44}, Arrays2.asArray((byte) 33, (byte) 44));
		isArrEquals(Arrays2.asList(new Byte[]{33,44}), Arrays2.asList((byte) 33, (byte) 44));
		
		isArrEquals(new Short[]{33}, Arrays2.asArray((short) 33));
		isArrEquals(new Short[]{33, 44}, Arrays2.asArray((short) 33, (short) 44));
		isArrEquals(Arrays2.asList(new Short[]{33, 44}), Arrays2.asList((short) 33, (short) 44));
		
		isArrEquals(new Integer[]{33}, Arrays2.asArray(33));
		isArrEquals(new Integer[]{33,44}, Arrays2.asArray(33,44));
		isArrEquals(Arrays2.asList(new Integer[]{33,44}), Arrays2.asList(33,44));
		
		isArrEquals(new Long[]{Long.valueOf(33), Long.valueOf(44)}, Arrays2.asArray((long) 33, (long) 44));
		isArrEquals(Arrays2.asList(new Long[]{Long.valueOf(33), Long.valueOf(44)}), Arrays2.asList((long) 33, (long) 44));
		
		isArrEquals(new Float[]{(float) 33.320000 }, Arrays2.asArray(Float.valueOf("33.320000")));
		isArrEquals(new Float[]{(float) 33.320000, (float) 44.23 }, Arrays2.asArray(Float.valueOf("33.320000"), Float.valueOf((float) 44.23)));
		isArrEquals(Arrays2.asList(new Float[]{(float) 33.320000, (float) 44.23 }), Arrays2.asList(Float.valueOf("33.320000"), Float.valueOf((float) 44.23)));
		
		isArrEquals(new Double[]{Double.valueOf("22.2222200000")}, Arrays2.asArray((double) 22.2222200000));
		isArrEquals(new Double[]{Double.valueOf("22.2222200000"), (double) 33.33}, Arrays2.asArray((double) 22.2222200000, Double.valueOf("33.33")));
		isArrEquals(Arrays2.asList(new Double[]{Double.valueOf("22.2222200000"), (double) 33.33}), Arrays2.asList((double) 22.2222200000, Double.valueOf("33.33")));
		
		isArrEquals(new Character[]{'Z'}, Arrays2.asArray(Character.valueOf('Z')));
		isArrEquals(new Character[]{'Z','\n'}, Arrays2.asArray(Character.valueOf('Z'), '\n'));
		isArrEquals(Arrays2.asList(new Character[]{'Z','\n'}), Arrays2.asList(Character.valueOf('Z'), '\n'));
		
		isArrEquals(new Boolean[]{ Boolean.valueOf("true")}, Arrays2.asArray(true));
		isArrEquals(new Boolean[]{ Boolean.valueOf("true"), false}, Arrays2.asArray(true, new Boolean("false")));
		isArrEquals(Arrays2.asList(new Boolean[]{ Boolean.valueOf("true"), false}), Arrays2.asList(true, new Boolean("false")));
		
		float[] floatP = new float[] { (float) 11.12, 12, (float) 13.00 };
		Float[] floatW = new Float[] { Float.valueOf((float) 11.12), Float.valueOf(12), Float.valueOf((float) 13.00) };
		isArrEquals(floatP, Arrays2.unwrap(floatW));
		isArrEquals(floatW, Arrays2.wraps(floatP));
		isArrEquals(new Float[]{Float.valueOf(1)}, Arrays2.wraps(1F));
		isArrEquals(new float[]{1}, Arrays2.unwrap(Float.valueOf(1)));

		long[] longP = new long[] { 22, 33, 44 };
		Long[] longW = new Long[] { Long.valueOf(22), Long.valueOf(33), Long.valueOf(44) };
		isArrEquals(longP, Arrays2.unwrap(longW));
		isArrEquals(longW, Arrays2.wraps(longP));
		isArrEquals(new Long[]{Long.valueOf(1)}, Arrays2.wraps((long) 1));
		isArrEquals(new long[]{3}, Arrays2.unwrap(Long.valueOf(3)));
		
		short[] shortP = new short[] { 11, 12, 13};
		Short[] shortW = new Short[] { 11, 12, 13};
		isArrEquals(shortP, Arrays2.unwrap(shortW));
		isArrEquals(shortW, Arrays2.wraps(shortP));
		isArrEquals(new Short[]{1}, Arrays2.wraps((short) 1));
		isArrEquals(new short[]{3}, Arrays2.unwrap(Short.valueOf((short) 3)));
		
		double[] douP = new double[] { 11.12, 12, 13.00 };
		Double[] douW = new Double[] { Double.valueOf(11.12), Double.valueOf(12), Double.valueOf(13.00) };
		isArrEquals(douP, Arrays2.unwrap(douW));
		isArrEquals(douW, Arrays2.wraps(douP));
		isArrEquals(new Double[]{Double.valueOf(1)}, Arrays2.wraps(1D));
		isArrEquals(new double[]{1}, Arrays2.unwrap(Double.valueOf(1)));
		
		char[] charP = new char[] { 'a', 'B', '\n'};
		Character[] charW = new Character[] { 'a', 'B', '\n'};
		isArrEquals(charP, Arrays2.unwrap(charW));
		isArrEquals(charW, Arrays2.wraps(charP));
		isArrEquals(new char[]{'\t'}, Arrays2.unwrap(Character.valueOf('\t')));
		isArrEquals(new Character[]{'\r'}, Arrays2.wraps('\r'));
				
		int[] intP = new int[] { 11, 12, 13 };
		Integer[] intW = new Integer[] { 11, 12, 13 };
		isArrEquals(intP, Arrays2.unwrap(intW));
		isArrEquals(intW, Arrays2.wraps(intP));
		isArrEquals(new Integer[]{1}, Arrays2.wraps(1));
		isArrEquals(new int[]{1}, Arrays2.unwrap(1));
		
		boolean[] boolP = new boolean[] { false, true, false };
		Boolean[] boolW = new Boolean[] { false, true, false };
		isArrEquals(boolP, Arrays2.unwrap(boolW));
		isArrEquals(boolW, Arrays2.wraps(boolP));
		isArrEquals(new Boolean[] {false}, Arrays2.wraps(false));
		isArrEquals(new Boolean[] {true}, Arrays2.wraps(true));
		isArrEquals(new boolean[] {false}, Arrays2.unwrap(new Boolean(false)));
		isArrEquals(new boolean[] {true}, Arrays2.unwrap(new Boolean(true)));
		
		byte[] byteP = new byte[] { 11, 12, 13 };
		Byte[] byteW = new Byte[] { 11, 12, 13 };
		isArrEquals(byteP, Arrays2.unwrap(byteW));
		isArrEquals(byteW, Arrays2.wraps(byteP));
		isArrEquals(new Byte[]{1}, Arrays2.wraps((byte) 1));
		isArrEquals(new byte[]{1}, Arrays2.unwrap(Byte.valueOf((byte) 1)));
		
	}

	private void isArrEquals(Object o1, Object o2) {
//		assertTrue(ArrayUtils.isEquals(o1, o2));
		assertTrue(Objects2.isEqual(o1, o2));
		assertEquals(Objects2.hashCodes(o1), Objects2.hashCodes(o2));
		log.info(o1);
		log.info(o2);
	}
	
	@Test
	public void testSlf4j() {
		Logger l = Loggers.from("slf4j logger");
		l.debug("person is: {}", getTestPerson());
	}
	
	@Test
	public void testReflecter() {
		Domain d = Reflecter.from(Domain.class).setTrace(true).populate(Domain.getProps()).get();
		log.info(d);
		
		log.info("---------------------");
		Domain d1 = Reflecter.from(d).copyTo(Domain.class);
		log.info(d1);
		
		assertEquals(d, d1);
		
		log.info("---------------------");
		Reflecter.from(getTestPerson()).setTrace(true).asMap();
		
		log.info("---------------------");
		Reflecter.from(getTestPerson()).asMap();
	}
	
	@Test
	public void testLoggersLevel() {
		Person p = getTestPerson();
		log.trace(p);
		log.debug(p);
		log.info(p);
		log.warn(p);
		log.error(p);
		log.fatal(p);
	}
	
	@Test
	public void testSixtytwo() {
		for (int i = 0; i < 1; i++) {
			sixty2();
		}
	}
	
	private void sixty2() {
		int begin = 0;
		int len = begin + 1000;
		int rLen = 8;
		
		String r = null;
		for (int i = begin; i <= len; i++) {
			assertEquals(r = SixtytwoScale.SixtyTwoScale(i, rLen), Scale62.get(i, rLen));
			assertEquals(Long.valueOf(i), (Long) Scale62.get(r));
		}
	}

	@Test
	public void testPurview() {
		for (int i = 0; i < 63; i++) {
			purT();
		}
	}
	
	private void purT() {
		int[] ps = new int[50];
		int rSeed = 63;
		List<Integer> l = Lists.newArrayList(rSeed);
		Random r = new Random();
		for (int i = 0; i < ps.length; i++) {
			int ra = r.nextInt(rSeed);
			while (l.contains(ra)) {
				ra = r.nextInt(rSeed);
			}
			ps[i] = ra;
			l.add(ra);
		}
		log.info(ps);
		
		SimplePurview p = SimplePurview.of(ps);
		log.info("purview: " + p.purview());
		for (int i = 0; i < ps.length; i++) {
			assertTrue(p.has(ps[i]));
			int[] aa = Arrays.copyOf(ps, i + 1);
			log.info(aa);
			assertTrue(p.has(aa));
		}
		
		for (int i = 0; i < rSeed; i++) {
			if (l.contains(i))  {
				assertTrue(p.has(i));
				continue;
			}
			assertFalse(p.has(i));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMapper() {
		Map<Object, Object> emptyM = Maps.newHashMap();
		Map<Object, Object> e = Maps.newHashMap();
		e.put("tier", 888);
		
		Map<Object, Object> d = Maps.newHashMap();
		d.put("e", e);
		
		Map<Object, Object> c = Maps.newHashMap();
		c.put("d", d);
		
		Map<Object, Object> b = Maps.newHashMap();
		b.put("c", c);
		
		Map<Object, Object> a = Maps.newHashMap();
		a.put("b", b);
		
		Map<Object, Object> tier = Maps.newHashMap();
		tier.put(111, getTestMap());
		
		Map<Object, Object> tm = Mapper.tiers(tier);
		
		//put
		tm.put("a.b.c.d.e.tier", 888);
		assertTrue(tm.containsKey("a.b.c.d.e.tier"));
		assertTrue(tm.containsKey("a.b.c.d.e"));
		assertTrue(tm.containsKey("a.b.c.d"));
		assertTrue(tm.containsKey("a.b.c"));
		assertTrue(tm.containsKey("a.b"));
		assertTrue(tm.containsKey("a"));
		
		tm.put(33.12, "desc");
		assertTrue(tm.containsKey(33.12));
		assertEquals("desc", tm.get(33.12));
		assertEquals("desc", tm.remove(33.12));
		assertFalse(tm.containsKey(33.12));
		
		//get
		assertEquals(888, ((Map<Object, Object>) tm.get("a")).get("b.c.d.e.tier"));
		assertEquals(888, ((Map<Object, Object>) tm.get("a.b")).get("c.d.e.tier"));
		assertEquals(888, ((Map<Object, Object>) tm.get("a.b.c")).get("d.e.tier"));
		assertEquals(888, ((Map<Object, Object>) tm.get("a.b.c.d")).get("e.tier"));
		assertEquals(888, ((Map<Object, Object>) tm.get("a.b.c.d.e")).get("tier"));
		assertEquals(a, tm.get("a"));
		assertEquals(b, tm.get("a.b"));
		assertEquals(c, tm.get("a.b.c"));
		assertEquals(d, tm.get("a.b.c.d"));
		assertEquals(e, tm.get("a.b.c.d.e"));
		assertEquals(888, tm.get("a.b.c.d.e.tier"));
		
		//containKey
		assertTrue(tm.containsKey("a"));
		assertTrue(tm.containsKey("a.b"));
		assertTrue(tm.containsKey("a.b.c"));
		assertTrue(tm.containsKey("a.b.c.d"));
		assertTrue(tm.containsKey("a.b.c.d.e"));
		assertTrue(tm.containsKey("a.b.c.d.e.tier"));
		assertFalse(tm.containsKey("a1"));
		assertFalse(tm.containsKey("a.b1"));
		assertFalse(tm.containsKey("a.b.c1"));
		assertFalse(tm.containsKey("a.b.c.d1"));
		assertFalse(tm.containsKey("a.b.c.d.e1"));
		assertFalse(tm.containsKey("a.b.c.d.e.tier1"));
		
		
		//remove1
		assertEquals(a, tm.remove("a"));
		assertFalse(tm.containsKey("a"));
		
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(b, tm.remove("a.b"));
		assertFalse(tm.containsKey("a.b"));
		
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(c, tm.remove("a.b.c"));
		assertFalse(tm.containsKey("a.b.c"));
		
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(d, tm.remove("a.b.c.d"));
		assertFalse(tm.containsKey("a.b.c.d"));
		
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(e, tm.remove("a.b.c.d.e"));
		assertFalse(tm.containsKey("a.b.c.d.e"));
		
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(888, tm.remove("a.b.c.d.e.tier"));
		assertFalse(tm.containsKey("a.b.c.d.e.tier"));
		
		//remove2
		tm.put("a.b.c.d.e.tier", 888);
		assertEquals(888, tm.remove("a.b.c.d.e.tier"));
		assertFalse(tm.containsKey("a.b.c.d.e.tier"));
		assertEquals(emptyM, tm.remove("a.b.c.d.e"));
		assertFalse(tm.containsKey("a.b.c.d.e"));
		assertEquals(emptyM, tm.remove("a.b.c.d"));
		assertFalse(tm.containsKey("a.b.c.d"));
		assertEquals(emptyM, tm.remove("a.b.c"));
		assertFalse(tm.containsKey("a.b.c"));
		assertEquals(emptyM, tm.remove("a.b"));
		assertFalse(tm.containsKey("a.b"));
		assertEquals(emptyM, tm.remove("a"));
		assertFalse(tm.containsKey("a"));
		
		tm.put("a.b.c.d.e.tier", 888);
		tm.put("a.b.c.d.e.tier1", 111);
		tm.put("a.b.c.d.e.tier2", 222);
		assertEquals(888, tm.get("a.b.c.d.e.tier"));
		assertEquals(111, tm.get("a.b.c.d.e.tier1"));
		assertEquals(222, tm.get("a.b.c.d.e.tier2"));
		tm.put("a.b.c.d.e1", "e1");
		assertEquals("e1", tm.get("a.b.c.d.e1"));
		tm.put("a.b.c.d1", "d1");
		assertEquals("d1", tm.get("a.b.c.d1"));
		tm.put("a.b.c1", "c1");
		assertEquals("c1", tm.get("a.b.c1"));
		tm.put("a.b1", "b1");
		assertEquals("b1", tm.get("a.b1"));
		tm.put("a1", "a1");
		assertEquals("a1", tm.get("a1"));
		log.info(tm);
		
		Map<Object, Object> m = getTestMap2();
		assertNull(m.get(88888));
		assertFalse(m.containsKey(88888));
		assertFalse(m.containsValue(88888));
		
		Map<Object, Object> m2 = Mapper.from(m).uniqueKey().deepLook().map();
		
		assertEquals(88888, m2.get(88888));
		assertTrue(m2.containsKey(88888));
		assertTrue(m2.containsValue(88888));
		
		try {
			m2.put(55555, 2);
		} catch (Exception ex) {
			assertEquals("The key: 55555 is already exist.", ex.getMessage());
		}
		
		Map<Object, Object> mmm = Maps.newHashMap();
		Map<Object, Object> m3 = Mapper.from(mmm).keyNotNull().map();
		try {
			m3.put(null, null);
		} catch (Exception ex) {
			assertEquals("The key must be not null.", ex.getMessage());
			assertEquals(NullPointerException.class, ex.getClass());
		}
		
		m3 = Mapper.from(mmm).valueNotNull().map();
		try {
			m3.put(null, null);
		} catch (Exception ex) {
			assertEquals("The value must be not null.", ex.getMessage());
			assertEquals(NullPointerException.class, ex.getClass());
		}
		
		m3 = Mapper.from(mmm).keyAndValueNotNull().map();
		try {
			m3.put(null, null);
		} catch (Exception ex) {
			assertEquals(NullPointerException.class, ex.getClass());
		}
	}
	
	@Test
	public void testLoggers() {
//		Logger l = LoggerFactory.getLogger("abcd");
//		Log ll = Loggers.from(l);
		Log ll = Loggers.from(this);
		
		String str = "<li><div>com.taxi.util.Person@hello@abcde@world@</div><div>hello world</div></li><li>what</li>";
		String start = "@";
		String end = start;
		
		ll.info(Strs.betnLast(str, end).asMap());
		ll = Loggers.from("hello world");
		ll.debug(getTestPersons());
		ll = Loggers.from(Person.class);
		ll.warn(getTestPerson());
		Gather.from(getTestPersons()).info();
		
		Map<String, Object> m = getTestMap();
		Map<String, Object> res = Mapper.from(m).filter(new Decision<Entry<String, Object>>() {
			
			@Override public boolean apply(Entry<String, Object> input) {
				return "person".equals(input.getKey());
			}
		}).uniqueKey().deepLook().map();
		
		assertNull(res.get("cmd"));
		assertNull(res.get("upt"));
		assertNull(res.get("name"));
		
		try {
			res.put("test", 12);
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
		
		assertEquals("paulo", Mapper.from(m).info().get("name"));
		Mapper.from(res).logset().chgL('E').journal();
		
		m = Mapper.from(m).uniqueKey().map();
		assertNull(m.get("name"));
		
		m = Mapper.from(m).uniqueKey().deepLook().map();
		assertEquals("paulo", m.get("name"));
		
		try {
			m.put("name", "frank");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
			assertEquals("The key: name is already exist.", e.getMessage());
		}
		
		log.info("end");
	}

	@Test
	public void testDaterParser() {
		String[][] dates = new String[][] {
			new String[] { "2010-01-19 23:59:59", DateStyle.yyyy_MM_dd_HH_mm_ss },
			new String[] { "2010-01-19T23:59:59.123456789", DateStyle.ISO_FORMAT },
			new String[] { "2010-01-19", DateStyle.yyyy_MM_dd },
			new String[] { "2010/01/19 23:59:59", "yyyy/MM/dd HH:mm:ss" },
			new String[] { "2010/01/19T23:59:59.123456789", "yyyy/MM/dd'T'HH:mm:ss.SSS" },
			new String[] { "2010/01/19", "yyyy/MM/dd" },
			new String[] { "23:59:59", "HH:mm:ss" },
			new String[] { "20100119", "yyyyMMdd" },
			new String[] { "235959", "HHmmss" },
			new String[] { "20100119235959", DateStyle.TIGHT.style() },
		};
		Loggers.journal(dates);
		Dater dater = null;
		for (String[] date : dates) {
			dater = Dater.from(date[0]);
			log.info(Dater.of(date[0]).asText() + " -- " + dater.asText());
			assertEquals(date[1], dater.using().style());
		}
	}
	
	@Test
	public void testVerySimpleJsonParse() throws InterruptedException {
		//{"cmd":8010,"code":0,"result":{"oid":"e7a20BZJ","pid":"f3150hnb","did":"471a120F","msg":"good","upt":1359099728088,"pushOffline":1}}
		String target = "{\"cmd\":8010,\"code\":0,\"result\":" +
				"{\"oid\":\"e7a20BZJ\",\"pid\":\"f3150hnb\",\"did\":\"471a120F\"," +
				"\"msg\":\"good\",\"upt\":1359099728088,\"pushOffline\":1}}";
		Replacer r = Strs.replace(target);
		target = r.lookups("\"", "{", "}", "result:").byNones();
		
		Map<String, String> m = Splitter.on(",").withKeyValueSeparator(":").split(target);
		assertEquals("8010", m.get("cmd"));
		assertEquals("e7a20BZJ", m.get("oid"));
		assertEquals("f3150hnb", m.get("pid"));
	}
	
	@Test
	public void testLargerString() throws IOException {
//		InputSupplier<InputStreamReader> supplier = 
//				CharStreams.newReaderSupplier(new InputSupplier<InputStream>() {
//
//			@Override
//			public InputStream getInput() throws IOException {
//				return MeTest.class.getResourceAsStream("/test.html");
//			}
//		}, Charsets.UTF_8);
//		
//		String large =  CharStreams.toString(supplier);
		
		String large = Sources.asString(MeTest.class, "/test.html");
		
		Finder l = Finder.of(large);
		assertEquals("Content-Type", l.betns("http-equiv=\"", "\" content").get());
		log.info(l.betn("<script", "</script>").inclus().get());
		log.info(l.betn("<head>", "</head>").inclus().gets());
		log.info(l.betn("<script", "</script>").inclus().gets());
		
		assertEquals(l.separ("*").betn("<script", "</script>").inclus().gets(),
				Joiner.on("*").join(l.separ("*").betn("<script", "</script>").inclus().asList()));
		l.separ("*").betn("<script", "</script>").inclus().asGather().info();
	}
	
	@Test
	public void testMutilPosUsage() {
		String target = "http://a.nmu.kuaidadi.com/rockmongo/index.php?action=admin.index";
		Finder f = Finder.of(target);
		
		assertEquals("a.nmu.kuaidadi.com", target = f.betn("//", "/").asymms(1, 3).get());
		
		f.update(target);
		assertEquals("com", f.after(".").pos(-1).get());
		assertEquals("kuaidadi.com", f.after(".").pos(-2).get());
		assertEquals("nmu.kuaidadi.com", f.after(".").pos(-3).get());
	}
	
	@Test
	public void testReplacerLookerUsage() {
		String result = "7d6cee3160600f9c87bc6df777258ff021f4949d50578955e15bc7951c3b3a92";
		String target = "<7d6cee31 60600f9c 87bc6df7 77258ff0 21f4949d 50578955 e15bc795 1c3b3a92>";
		Replacer r = Replacer.of(target);
		
		assertEquals(result, r.betn("<", ">").negates().lookups(" ").byNones());
		assertEquals(result, r.betn(1, -1).negates().lookups(" ").byNones());
		assertEquals(result, r.before("<").inclusR().after(">").inclusL().lookups(" ").byNones());
		
		r.contextM();
		assertEquals(result, r.setBetn("<", ">").first().lookups(" ").byNones());
		assertEquals(result, r.setBetn(1, -1).lookups(" ").byNones());
		
		Finder l = Finder.of(target);
		assertEquals(result, l.betns("<", ">").lookup(" ").negates().gets());
		assertEquals(result, l.lookup(" ").negates().betns("<", ">").gets());
		assertEquals(result, l.betns(1, -1).lookup(" ").negates().gets());
		assertEquals(result, l.before("<").inclus(false, true).negates()
							  .after(">").inclus(true, false).negates()
							  .lookup(" ").negates().gets());
	}
	
	@Test
	public void testResultReplacer() {
		String target = "<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>";
		// target.substring(5, 7) == "li"
		// (left, right]
		
		// |< |u |l |> |< |l |i |> |f |i  |r  |s  |t  |   |l  |i  |<  |/  |l  |i  |>  |<  |l  |i  
		// |1 |2 |3 |4 |5 |6 |7 |8 |9 |10 |11 |12 |13 |14 |15 |16 |17 |18 |19 |20 |21 |22 |23 |24 
		
		// |>  |2  |0  |1  |3  |-  |0  |3  |-  |2  |8  |   |1  |9  |:  |1  |3  |:  |5  |8  |<  |/  
		// |25 |26 |27 |28 |29 |30 |31 |32 |33 |34 |35 |36 |37 |38 |39 |40 |41 |42 |43 |44 |45 |46 
		
		// |l  |i  |>  |<  |/  |u  |l  |>  |<  |u  |l  |>  |s  |e  |c  |o  |n  |d  |   |u  |l  |<  |/  |u  |l  |>  |
		// |47 |48 |49 |50 |51 |52 |53 |54 |55 |56 |57 |58 |59 |60 |61 |62 |63 |64 |65 |66 |67 |68 |69 |70 |71 |72 |
		
		Replacer r = Replacer.of(target);
		
		betnIdxRepResult(r);
		beforeIdxRepResult(r);
		afterIdxRepResult(r);
		
		betnStrRepResult(r);
		betnStrAsymmRepResult(r);
		beforeStrRepResult(r);
		afterStrRepResult(r);
		betnNextStrRepResult(r);
		betnLastStrRepResult(r);
		
		lookupRepResult(r);
		complexRepResult(r);
		
		setRepResult(r);
	}
	
	private void setRepResult(Replacer r) {
		log.debug("setRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		String target = "funcity@gmail.com";
		assertEquals("fun    @gmail.com",
				r.set(target).lookups("city").with(' '));
		assertEquals("fun              ",
				r.set(target).afters(3).befores("@").with(' '));
		assertEquals("*2013-03-28 19:13:58*",
				r.setAfter(3).befores("2013").afters("58").with("*"));
		
		assertEquals("<ul><li>",
				r.setAfter("<li>").negate().first().betns("<li>", "</li>").with("*"));
		assertEquals("first li</li><li>*</li></ul><ul>second ul</ul>",
				r.set("first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>").betns("<li>", "</li>").with("*"));
		
		assertEquals("2013-03-28 19:13:58", 
				r.setBefore(44).after("<li>").negate().last().byNone().all());
		assertEquals("<ul><li>first li</li><li>2013*28 19:13:58", 
				r.setBefore("</li>").last().betns("2013", "28").with("*"));
		assertEquals("2013sep03sep28 19sep13sep58", 
				r.setBetn(25, 44).lookups("-").lookups(":").with("sep"));
		assertEquals("2013-03-28 19:13:58", 
				r.setBetn("<li>", "</li>").last().with("*"));
		assertEquals("", 
				r.setBetn("</li>", "<li>").last().with("*"));
		assertEquals("2013-03-28 19:13:58",
				r.setBetnLast(">").last().afters(-4).byNones());
		assertEquals("ul",
				r.setBetnNext("<").first().afters(-1).byNones());
	}

	private void complexRepResult(Replacer r) {
		log.debug("complexRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>**", 
				r.before("</ul>").negate().last().betns(4, -1).with("*"));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("                         2013 03-28 19    58                            ",
				r.betn(25, 44).negates().lookup("-").first().afters("58").befores("2013").betns("19", "58").with(' '));
	}

	private void lookupRepResult(Replacer r) {
		log.debug("lookupRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19*13*58</li></ul><ul>second ul</ul>",
				r.lookups(":").with("*"));
		assertEquals("<ul><li>first li*<li>2013-03-28 19:13:58*</ul><ul>second ul</ul>",
				r.lookups("</li>").with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58     </ul><ul>second ul</ul>",
				r.lookup("</li>").pos(2).with(' '));
		assertEquals(r.lookup("</li>").pos(2).with(' '), r.lookups("</li>").last(' '));
	}

	private void betnLastStrRepResult(Replacer r) {
		log.debug("betnLastStrRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul*ul>",
				r.betnLasts("ul").with("*"));
		assertEquals("*><ul>second *",
				r.betnLast("ul").negates().with("*"));
		assertEquals("*><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</*",
				r.betnLast("ul").negates().first("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul                                                                  ul>",
				r.betnLasts("ul").with(' '));
		assertEquals("                                                     ><ul>second        ",
				r.betnLast("ul").negates().with(' '));
		assertEquals("                                                   ul><ul>second ul     ",
				r.betnLast("ul").inclus(true, true).negates().with(' '));
		assertEquals("   ><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul               ",
				r.betnLast("ul").asymm(1, 3).inclus(false, true).negates().with(' '));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<                                                                  </ul>",
				r.betnLasts("<").with(' '));
		assertEquals("                      li>2013-03-28 19:13:58                            ",
				r.betnLast("<").negates().with(' '));
		assertEquals(" ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul     ",
				r.betnLast("<").negate().first().with(' '));
		assertEquals("                      li>2013-03-28 19:13:58                            ",
				r.betnLast("<").negate().last().with(' '));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("     li>first li</li><li>2013-03-28 19:13:58</li></ul>                  ",
				r.betnLast("<").negate().pos(2).with(' '));
	}

	private void betnNextStrRepResult(Replacer r) {
		log.debug("betnNextStrRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul*ul><ul*ul</ul>",
				r.betnNexts("ul").with("*"));
		assertEquals("*><li>first li</li><li>2013-03-28 19:13:58</li></>second *",
				r.betnNext("ul").negates().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul                                                ul><ul        ul</ul>",
				r.betnNexts("ul").with(' '));
		assertEquals("   ><li>first li</li><li>2013-03-28 19:13:58</li></>second        ",
				r.betnNext("ul").negates().with(' '));
		
		assertEquals("<ul><li*li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betnNext("li").first().with("*"));
		assertEquals("<ul><li>first li</li*li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betnNext("li").last().with("*"));
		assertEquals("<ul><li*li></ul><ul>second ul</ul>",
				r.betnNext("li").asymms(1, 5).with("*"));
		assertEquals("*>first li</li><li>2013-03-28 19:13:58</*",
				r.betnNext("li").asymm(1, 5).negates().with("*"));
		assertEquals("*li>first li</li><li>2013-03-28 19:13:58</*",
				r.betnNext("li").asymm(1, 5).inclus(true, false).negates().with("*"));
		assertEquals("*li>first li</li><li>2013-03-28 19:13:58</li*",
				r.betnNext("li").asymm(1, 5).inclus(true, true).negates().with("*"));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<   <li>first li<    <li>2013-03-28 19:13:58<    </ul><            </ul>",
				r.betnNexts("<").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><            </ul>",
				r.betnNexts("<").last(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><            </ul>",
				r.betnNext("<").pos(4).with(' '));
	}

	private void afterStrRepResult(Replacer r) {
		log.debug("afterStrRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013*",
				r.afters("2013").with("*"));
		assertEquals("*-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.after("2013").negates().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013                                           ",
				r.afters("2013").with(' '));
		assertEquals("                             -03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.after("2013").negates().with(' '));
		
		assertEquals("<ul><li>*", 
				r.afters("<li>").with("*"));
		assertEquals("*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.after("<li>").negates().with("*"));
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.after("<li>").inclus(true, false).negates().with("*"));
		
		assertEquals("*2013*",
				r.after("<li>").negate().last().afters("2013").with("*"));
		assertEquals("*-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.after("<li>").negate().last().after("2013").negates().with("*"));
	}

	private void beforeStrRepResult(Replacer r) {
		log.debug("beforeStrRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores("2013").with("*"));
		assertEquals("<ul><li>first li</li><li>*",
				r.before("2013").negates().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("                         2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores("2013").with(' '));
		assertEquals("<ul><li>first li</li><li>                                               ",
				r.before("2013").negates().with(' '));
		
		assertEquals("*<li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores("<li>").with("*"));
		assertEquals("<ul><li>first li</li>*", 
				r.before("<li>").negates().with("*"));
		assertEquals("<ul><li>first li</li><li>*", 
				r.before("<li>").inclus(false, true).negates().with("*"));
		
		assertEquals("*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.before("</li>").first().befores("2013").with("*"));
		assertEquals("<ul><li>first li*", 
				r.before("</li>").negate().first().befores("2013").with("*"));
	}

	private void betnStrAsymmRepResult(Replacer r) {
		log.debug("betnStrAsymmRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>*</li></ul><ul>second ul</ul>",
				r.betn("<li>", "</li>").asymms(1, 2).with("*"));
		assertEquals("*first li</li><li>2013-03-28 19:13:58*",
				r.betn("<li>", "</li>").asymm(1, 2).negates().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>                                    </li></ul><ul>second ul</ul>",
				r.betn("<li>", "</li>").asymms(1, 2).with(' '));
		assertEquals("        first li</li><li>2013-03-28 19:13:58                            ",
				r.betn("<li>", "</li>").asymm(1, 2).negates().with(' '));
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li>                       ",
				r.betn("<li>", "</li>").asymm(1, 2).inclus(true, true).negates().with(' '));
		assertEquals("        first li</li><li>2013-03-28 19:13:58</li>                       ",
				r.betn("<li>", "</li>").asymm(1, 2).inclus(false, true).negates().with(' '));
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58                            ",
				r.betn("<li>", "</li>").asymm(1, 2).inclus(true, false).negates().with(' '));
	}

	private void betnStrRepResult(Replacer r) {
		log.debug("betnStrRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>*</li><li>*</li></ul><ul>second ul</ul>",
				r.betns("<li>", "</li>").with("*"));
		assertEquals("<ul>*</ul><ul>*</ul>",
				r.betns("<ul>", "</ul>").with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>                                             </ul><ul>second ul</ul>",
				r.betns("<ul>", "</ul>").first(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>         </ul>",
				r.betns("<ul>", "</ul>").last(' '));
		
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li>second ul*", 
				r.betn("<ul>", "</ul>").negates().with("*"));
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li>*", 
				r.betn("<ul>", "</ul>").negate().first().with("*"));
		assertEquals("*second ul*", 
				r.betn("<ul>", "</ul>").negate().last().with("*"));
		assertEquals("*second ul*", 
				r.betn("<ul>", "</ul>").negate().pos(2).with("*"));
		assertEquals("*<ul>second ul</ul>", 
				r.betn("<ul>", "</ul>").inclus(true, true).negate().pos(2).with("*"));
		assertEquals("*<li>2013-03-28 19:13:58</li>*", 
				r.betn("<li>", "</li>").inclus(true, true).negate().pos(2).with("*"));
		assertEquals("*2013-03-28 19:13:58</li>*", 
				r.betn("<li>", "</li>").inclus(false, true).negate().pos(2).with("*"));
		assertEquals("*<li>2013-03-28 19:13:58*", 
				r.betn("<li>", "</li>").inclus(true, false).negate().pos(2).with("*"));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("        first li</li><li>2013-03-28 19:13:58                            ",
				r.betn("<li>", "</li>").asymm(1, 2).negates().with(' '));
		assertEquals("        first li</li><li>2013-03-28 19:13:58</li>                       ",
				r.betn("<li>", "</li>").asymm(1, 2).inclus(false, true).negates().with(' '));
		assertEquals("<ul><li>                                    </li></ul><ul>second ul</ul>",
				r.betn("<li>", "</li>").asymms(1, 2).with(' '));
		
		assertEquals("<ul><li>*</li><li>*</li></ul><ul>second ul</ul>",
				r.betns("<li>", "</li>").betns("2013", "58").with("*"));
		assertEquals("*2013*58*",
				r.betn("<li>", "</li>").negates().betns("2013", "58").last("*"));
	}

	private void afterIdxRepResult(Replacer r) {
		log.debug("afterIdxRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>*", r.afters(4).with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul     ",
				r.afters(-5).last(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>     <ul>second ul     ",
				r.afters(-5).with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>*<ul>second ul*",
				r.afters(-5).with("*"));
		
		assertEquals("*</li></ul><ul>second ul</ul>", 
				r.after(44).negates().with("*"));
		
		assertEquals("*</ul></ul>", r.after(-5).negates().with("*"));
		//      	  <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("                                                      <ul>second ul</ul>",
				r.after(-5).negate().first().with(' '));
		assertEquals("*",
				r.after(-5).negate().last().with("*"));
		assertEquals("*<ul>secon*", 
				r.after(-5).negate().first().afters(-9).with("*"));
	}

	private void beforeIdxRepResult(Replacer r) {
		log.debug("beforeIdxRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("*</li></ul><ul>second ul</ul>", r.befores(44).with("*"));
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.befores(4).with("*"));
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores(4).first("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.befores(4).last("*"));
		assertEquals("*</ul>", r.befores(-5).with("*"));
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58*", 
				r.before(44).negates().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58                            ", 
				r.before(44).negates().with(' '));
		
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58*",
				r.before(44).negates().befores(4).with("*"));
		assertEquals("<ul><ul>*", r.before(4).negates().with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>                  ", 
				r.before(4).negate().last().with(' '));
		assertEquals("*", 
				r.before(4).negate().first().with("*"));
	}

	private void betnIdxRepResult(Replacer r) {
		log.debug("betnIdxRepResult");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<*>", r.betns(1, -1).with("*"));
		assertEquals("<                                                                      >", 
				r.betns(1, -1).with(' '));
		assertEquals("<*><li>first li</li><li>2013-03-28 19:13:58</li></*><*>second *</*>", 
				r.betns(-3, -1).with("*"));
		assertEquals("<  ><li>first li</li><li>2013-03-28 19:13:58</li></  ><  >second   </  >", 
				r.betns(-3, -1).with(' '));
		assertEquals("<  ><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.betns(-3, -1).first(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</  >", 
				r.betns(-3, -1).last(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second   </ul>", 
				r.betns(-3, -1).by(' ').pos(4));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second *</ul>", 
				r.betns(-3, -1).by("*").pos(4));
		
		assertEquals("*ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul*", 
				r.betn(1, -1).negates().with("*"));
		assertEquals("*ul*", 
				r.betn(1, 3).negate().last().with("*"));
		assertEquals(r.betn(1, 3).negate().first().with("*"), r.betn(1, 3).negate().last().with("*"));
		assertEquals(r.betn(1, 3).negate().first().with("*"), r.betn(1, 3).negate().pos(3).with("*"));
		
		assertEquals("<*><l*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betns(1, 3).betns(5, 7).first("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>        </li><li>                   </li></ul><ul>         </ul>", 
				r.betns(8, 16).betns(25, 44).betns(58, 67).with(' '));
	}
	
	@Test
	public void testContextReplacer() {
		String target = "<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>";
		// target.substring(5, 7) == "li"
		// (left, right]
		
		// |< |u |l |> |< |l |i |> |f |i  |r  |s  |t  |   |l  |i  |<  |/  |l  |i  |>  |<  |l  |i  
		// |1 |2 |3 |4 |5 |6 |7 |8 |9 |10 |11 |12 |13 |14 |15 |16 |17 |18 |19 |20 |21 |22 |23 |24 
		
		// |>  |2  |0  |1  |3  |-  |0  |3  |-  |2  |8  |   |1  |9  |:  |1  |3  |:  |5  |8  |<  |/  
		// |25 |26 |27 |28 |29 |30 |31 |32 |33 |34 |35 |36 |37 |38 |39 |40 |41 |42 |43 |44 |45 |46 
		
		// |l  |i  |>  |<  |/  |u  |l  |>  |<  |u  |l  |>  |s  |e  |c  |o  |n  |d  |   |u  |l  |<  |/  |u  |l  |>  |
		// |47 |48 |49 |50 |51 |52 |53 |54 |55 |56 |57 |58 |59 |60 |61 |62 |63 |64 |65 |66 |67 |68 |69 |70 |71 |72 |
		
		Replacer r = Replacer.ctx(target);
		
		betnIdxRepContext(r);
		beforeIdxRepContext(r);
		afterIdxRepContext(r);
		
		betnStrRepContext(r);
		betnStrAsymmRepContext(r);
		beforeStrRepContext(r);
		afterStrRepContext(r);
		betnNextStrRepContext(r);
		betnLastStrRepContext(r);
		
		lookupRepContext(r);
		complexRepContext(r);
		
		setRepContext(r);
	}
	
	private void setRepContext(Replacer r) {
		log.debug("setRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		//            2013-03-28 19:13:58
		assertEquals("***********19:13:58", r.setBetn("<li>", "</li>").pos(2).after(" ").negates().with('*'));
		assertEquals("2013-03-28 19:13:58", r.setBetn("<li>", "</li>").pos(2).after(" ").fnegates('a').with('*'));
		
		assertEquals("*<ul*second ", r.setBetnLast("ul").pos(2).befores("<").with("*"));
		assertEquals("><ul>****** ", r.setBetnLast("ul").pos(2).lookups("second").with('*'));
		
		assertEquals(">****** ", r.setBetnNext("ul").last().betns(1, -1).with('*'));
		assertEquals(">* ", r.setBetnNext("ul").last().betns(1, -1).with("*"));
		
		assertEquals("*</li></ul><ul>second ul</ul>", r.setAfter(25).befores("</li>").with("*"));
		assertEquals("<ul>*first li</li>*", r.setBefore(25).afters("</li>").with("*"));
		assertEquals("<ul><li>first li</li>*", r.setBefore(25).afters("</li>").last("*"));
		
		assertEquals("***********19:13:58", r.setBetn(25, 44).after(" ").negates().with('*'));
	}

	private void complexRepContext(Replacer r) {
		log.debug("complexRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		String target = "funcity@gmail.com";
		assertEquals("fun    @gmail.com", r.set(target).afters(3).befores("@").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores("@").with("*"));
		
		assertEquals("<ul><li>first li</li><li>2013-03-   19:13:58</li></ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").pos(2).befores(" ").after("-").pos(2).with(' '));
	}

	private void lookupRepContext(Replacer r) {
		log.debug("lookupRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><*>first *</*><*>2013-03-28 19:13:58</*></ul><ul>second ul</ul>", 
				r.lookups("li").with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><  >first   </  ><  >2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				r.lookups("li").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				r.lookups("li").last(' '));
		assertEquals(r.lookups("li").by(' ').pos(5), r.lookups("li").last(' '));
		assertEquals("<ul><*>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.lookups("li").first("*"));
		
		assertEquals(r.lookups("li").with("*"), r.lookup("li").all().with("*"));
	}

	private void betnLastStrRepContext(Replacer r) {
		log.debug("betnLastStrRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li*li></ul><ul>second ul</ul>", 
				r.betnLasts("li").with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li                                       li></ul><ul>second ul</ul>", 
				r.betnLasts("li").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul            ul</ul>", 
				r.betnLasts("ul").last(' '));
		
		//</li>< 2 places
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58      /ul><ul>second ul</ul>", 
				r.betnLasts("li").last(' '));
		assertEquals("<ul><li>first li</li><li                      li></ul><ul>second ul</ul>", 
				r.betnLast("li").asymms(4, 5).with(' '));
		
	}

	private void betnNextStrRepContext(Replacer r) {
		log.debug("betnNextStrRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li*li</li*li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.betnNexts("li").with("*"));
		assertEquals("<ul><*</*>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.betnNext("li").inclus().with("*"));
		
		assertEquals("<*<li>first li<*<li>2013-03-28 19:13:58<*</ul><*</ul>", 
				r.betnNexts("<").with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<   <li>first li<    <li>2013-03-28 19:13:58<    </ul><            </ul>", 
				r.betnNexts("<").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><            </ul>", 
				r.betnNexts("<").by(' ').pos(4));
		assertEquals(r.betnNexts("<").by(' ').pos(4), r.betnNexts("<").last(' '));
	}

	private void afterStrRepContext(Replacer r) {
		log.debug("afterStrRep"); 
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013*", r.afters("2013").with("*"));
		assertEquals("<ul><li>first li</li><li>*", r.after("2013").inclusL().with("*"));
		assertEquals("<ul><li>first li</li><li>                                               ", 
				r.after("2013").inclusL().with(' '));
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>*<ul>second ul*", 
				r.afters(" ul").with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul*", 
				r.afters(" ul").last("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul*", 
				r.after(" ul").first().by("*").pos(2));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second*", 
				r.after(" ul").inclus(true, false).first().by("*").pos(2));
	}

	private void beforeStrRepContext(Replacer r) {
		log.debug("beforeStrRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.befores("2013").with("*"));
		assertEquals("*-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.before("2013").inclusR().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("                             -03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.before("2013").inclusR().with(' '));
		
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.before("<li>").first().with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.before("<li>").first().last("*"));
		assertEquals("*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.before("<li>").inclus(true, true).first().with("*"));
	}

	private void betnStrAsymmRepContext(Replacer r) {
		log.debug("betnStrAsymmRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>*</li></ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").asymms(1, 2).with("*"));
		assertEquals("<ul>*</ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").asymm(1, 2).inclus().with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>                                             </ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").asymm(1, 2).inclus().with(' '));
		
		assertEquals("<ul><*>first *</*><*>2013-03-28 19:13:58</*></ul><ul>second ul</ul>", 
				r.betn("</", ">").asymms(2, 5).with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><  >first   </  ><  >2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				r.betn("</", ">").asymms(2, 5).with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				r.betn("</", ">").asymms(2, 5).last(' '));
	}

	private void betnStrRepContext(Replacer r) {
		log.debug("betnStrRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>                                             </ul><ul>second ul</ul>", 
				r.betns("<ul>", "</ul>").first(' '));
		assertEquals("<ul>*</ul><ul>second ul</ul>", r.betns("<ul>", "</ul>").first("*"));
		assertEquals("*<ul>second ul</ul>", r.betn("<ul>", "</ul>").inclus().first("*"));
		assertEquals("<ul>*</ul><ul>*</ul>", r.betns("<ul>", "</ul>").with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>                                             </ul><ul>         </ul>", 
				r.betns("<ul>", "</ul>").with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>         </ul>", 
				r.betns("<ul>", "</ul>").last(' '));
		assertEquals("<ul><li>*</li><li>*</li></ul><ul>second ul</ul>", 
				r.betns("<li>", "</li>").with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>*</ul>", 
				r.betns("<ul>", "</ul>").last("*"));
		
		assertEquals("<ul><li>first li</li><li>2013*58</li></ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").last().betns("2013", "58").with("*"));
		assertEquals("<ul><li>first li</li><li>2013             58</li></ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").last().betns("2013", "58").with(' '));
		//			  <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals(" ul> li>first li /li> li>2013-03-28 19:13:58 /li> /ul> ul>second ul /ul>", 
				r.betn("<ul>", "li>first").first().with(' '));
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.betn("<li>", "</li>").negates().with("*"));
	}

	private void afterIdxRepContext(Replacer r) {
		log.debug("afterIdxRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second *******", 
				r.afters(65).with('*'));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second 8", 
				r.afters(65).with("8"));
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>*<ul>second ul*", 
				r.afters(54).afters(-5).with("*"));
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>     <ul>second ul     ", 
				r.afters(54).afters(-5).with(' '));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul*", 
				r.afters(54).afters(-5).last("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul88888", 
				r.afters(54).afters(-5).last('8'));
		
		assertEquals("<ul><li>first li*", r.afters(16).with("*"));
		assertEquals("*</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.after(16).negates().with("*"));
		
		//			  <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.after(-5).negates().with("*"));
		assertEquals("*</ul>", r.after(-5).negates().last("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.after(-5).negates().first("*"));
	}

	private void beforeIdxRepContext(Replacer r) {
		log.debug("beforeIdxRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		
		assertEquals("*<li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", r.befores(21).with("*"));
		assertEquals(r.befores(21).with("*"), r.befores(21).first("*"));
		assertEquals("*</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", r.befores(21).befores(-5).with("*"));
		
		assertEquals("****<li>first li</li><li>2013-03-28 19:13:58</li></ul>****second ul</ul>", 
				r.befores(4).with('*'));
		assertEquals("*<li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.befores(4).with("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>****second ul</ul>", 
				r.befores(4).last('*'));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>*second ul</ul>", 
				r.befores(4).by("*").pos(2));

		assertEquals("<ul><li>first li</li>*", r.before(21).negates().with("*"));
		assertEquals("<ul>*", r.before(4).negate().first().with("*"));
		assertEquals("<ul><li>*", r.before(8).negate().first().with("*"));
	}

	private void betnIdxRepContext(Replacer r) {
		log.debug("betnIdxRep");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("furong****@gmail.com", r.set("furonglang@gmail.com").afters(6).befores("@").with('*'));
		assertEquals("furong****@gmail.com", r.set("furonglang@gmail.com").afters(6).befores("@").first('*'));
		assertEquals("<ul><li>first li</li><li>                   </li></ul><ul>second ul</ul>", 
				r.betns(25, 44).with(' '));
		assertEquals("<ul><li>first li</li><li>           19:13:58</li></ul><ul>second ul</ul>", 
				r.betns(25, 44).betns(0, 11).with(' '));
		
		assertEquals("<ul>****first li</li>****2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betns(4, 8).with('*'));
		assertEquals("<ul>*first li</li>*2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betns(4, 8).with("*"));
		assertEquals("<ul><li>first li</li>*2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betns(4, 8).last("*"));
		assertEquals("<ul>*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>",
				r.betns(4, 8).first("*"));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				r.betn(4, 8).negates().first("*"));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>*", 
				r.betn(4, 8).negates().afters("</ul>").first("*"));
	}

	@Test
	public void testLooker() {
		String target = "<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>";
		// target.substring(5, 7) == "li"
		// (left, right]
		
		// |< |u |l |> |< |l |i |> |f |i  |r  |s  |t  |   |l  |i  |<  |/  |l  |i  |>  |<  |l  |i  
		// |1 |2 |3 |4 |5 |6 |7 |8 |9 |10 |11 |12 |13 |14 |15 |16 |17 |18 |19 |20 |21 |22 |23 |24 
		
		// |>  |2  |0  |1  |3  |-  |0  |3  |-  |2  |8  |   |1  |9  |:  |1  |3  |:  |5  |8  |<  |/  
		// |25 |26 |27 |28 |29 |30 |31 |32 |33 |34 |35 |36 |37 |38 |39 |40 |41 |42 |43 |44 |45 |46 
		
		// |l  |i  |>  |<  |/  |u  |l  |>  |<  |u  |l  |>  |s  |e  |c  |o  |n  |d  |   |u  |l  |<  |/  |u  |l  |>  |
		// |47 |48 |49 |50 |51 |52 |53 |54 |55 |56 |57 |58 |59 |60 |61 |62 |63 |64 |65 |66 |67 |68 |69 |70 |71 |72 |
		
		Finder l = Finder.of(target);
		
		//looker
		betnIdxLook(l);
		beforeIdxLook(l);
		afterIdxLook(l);
		
		betnStrLook(l);
		betnStrAsymmLook(l);
		beforeStrLook(l);
		afterStrLook(l);
		betnNextStrLook(l);
		betnLastStrLook(l);
		
		lookupLook(l);
		complexLook(l);
		
		//set
		setLook(l);
	}
	
	private void complexLook(Finder l) {
		log.debug("complexLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		String target = "funcity@gmail.com";
		
		assertEquals("city", l.set(target).afters(3).befores("@").get());
		assertEquals("", l.befores("@").get());
		assertEquals("funcity", l.set(target).befores("@").get());
	}

	private void setLook(Finder l) {
		log.debug("setLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>second ", l.set("><ul>second ").afters(">").get());
		assertEquals("<ul>second ", l.setBetnLast("ul").pos(2).afters(">").get());
		assertEquals("second ", l.setBetnNext("ul").pos(2).afters(1).get());
		assertEquals("ond", l.setBetn("<ul>", "</ul>").last().befores(" ").afters(3).get());
		
		assertEquals("ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul", l.setBetn(1, -1).get());
		assertEquals("2013-03-28 19:13:58", l.setBetn(1, -1).befores("</ul").afters("</li>").betns(4, -5).get());
		assertEquals("<ul><li>first li", l.setBefore(16).get());
		assertEquals("</ul>", l.setAfter(67).get());
	}

	private void lookupLook(Finder l) {
		log.debug("lookupLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("li", l.lookups("li").get());
		assertEquals("li,li,li,li,li", l.lookups("li").find().all());
		assertEquals("li", l.lookup("li").last().get());
	}

	private void betnLastStrLook(Finder l) {
		log.debug("betnLastStrLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals(">first li</li><li>2013-03-28 19:13:58</", l.betnLasts("li").get());
		assertEquals("</li><", l.betnLasts("li").get(2));
		assertEquals("li</li><li", l.betnLast("li").inclus().get(2));
		assertEquals(">first li</", l.betnLast("li").asymms(1, 3).get());
		
		assertEquals(">first li</li><li>2013-03-28 19:13:58</,</li><", l.betnLast("li").all().get());
		assertEquals(">first li</li><li>2013-03-28 19:13:58</,</li><", l.betnLasts("li").find().all());
		
		//negate
		assertEquals("<ul><lili></ul><ul>second ul</ul>", l.betnLast("li").negates().get());
		assertEquals("<ul><li>first lili>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.betnLast("li").negates().getLast());
		assertEquals("<ul><li>first >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.betnLast("li").inclus(true, true).negates().getLast());
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first           >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.betnLast("li").inclus(true, true).fnegates(' ').getLast());
		assertEquals("<ul><                                           ></ul><ul>second ul</ul>", l.betnLast("li").inclus(true, true).fnegates(' ').get());
		
		assertEquals("<ul><                                           ></ul><ul>second ul</ul>,<ul><li>first           >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().betnLast("li").inclus(true, true).fnegates(' ').find().all());
		
		assertEquals("<ul><                                           ></ul><ul>second ul</ul>", 
				l.betnLast("li").inclus(true, true).fnegates(' ').find().all());
		assertEquals("<ul><li                                       li></ul><ul>second ul</ul>", 
				l.betnLast("li").fnegates(' ').find().all());
		
		assertEquals("<ul><li#li></ul><ul>second ul</ul>", l.betnLast("li").fnegates("#").find().all());
		
	}

	private void betnNextStrLook(Finder l) {
		log.debug("betnNextStrLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals(">first ", l.betnNexts("li").get());
		assertEquals("><", l.betnNexts("li").get(2));
		assertEquals("li><li", l.betnNext("li").inclus().get(2));
		assertEquals("", l.betnNexts("li").get(3));
		
		assertEquals(">first ,><", l.betnNexts("li").find().all());
		assertEquals(">first ,><", l.betnNext("li").all().get());
		
		assertEquals(">first li</li><li>2013-03-28 19:13:58</", l.betnNext("li").asymms(1, 5).get());
		assertEquals(">first li</li><", l.betnNext("li").asymms(1, 4).get());
		assertEquals("li>first li", l.betnNext("li").inclus().get());
		
		//negate
		assertEquals("<ul><lili</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.betnNext("li").negates().get());
		assertEquals("<ul><li       li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li  li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().betnNext("li").fnegates(' ').find().all());
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><           </li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</      >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().betnNext("li").inclus(true, true).fnegates(' ').find().all());
		assertEquals("<ul><           </li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>abc<ul><li>first li</      >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().separ("abc").betnNext("li").inclus(true, true).fnegates(' ').find().all());
		
		assertEquals("<ul><li       li</li  li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.betnNext("li").fnegates(' ').find().all());
		assertEquals("<ul><           </      >2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.betnNext("li").inclus(true, true).fnegates(' ').find().all());
		
		assertEquals("<ul><*</*>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.betnNext("li").inclus(true, true).fnegates("*").find().all());

	}

	private void afterStrLook(Finder l) {
		log.debug("afterStrLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.afters("<ul>").get());
		assertEquals("second ul</ul>", l.afters("<ul>").get(2));
		assertEquals("second ul</ul>", l.afters("<ul>").getLast());
		
		assertEquals("<ul>second ul</ul>", l.after("<ul>").inclusL().getLast());
		assertEquals("<ul>second ul</ul>", l.after("<ul>").inclus(true, false).last().get());
		assertEquals("second ul</ul>", l.after("<ul>").inclusR().getLast());
		
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,second ul</ul>", l.afters("<ul>").find().all());
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,second ul</ul>", l.after("<ul>").all().get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul>second ul</ul>", l.after("<ul>").inclus(true, true).all().get());
		assertEquals("", l.after("</ul>").last().get());
		
		//negate
		assertEquals("<ul>", l.after("<ul>").negates().get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>", l.after("<ul>").negates().get(2));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>", l.after("<ul>").negates().getLast());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>", l.after("<ul>").negate().last().get());
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>                  ,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().after("</ul>").fnegate(' ').all().get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>                       ,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul     ", 
				l.splitAll().after("</ul>").inclus(true, false).fnegate(' ').all().get());
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>                  ",
				l.after("</ul>").fnegates(' ').find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>#",
				l.after("</ul>").fnegates("#").find().all());
	}

	private void beforeStrLook(Finder l) {
		log.debug("beforeStrLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li", l.befores("</li>").get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58", l.befores("</li>").getLast());
		assertEquals("<ul><li>first li,<ul><li>first li</li><li>2013-03-28 19:13:58", l.before("</li>").all().get());
		assertEquals("<ul><li>first li,<ul><li>first li</li><li>2013-03-28 19:13:58", l.befores("</li>").find().all());
		
		assertEquals("<ul><li>first li,<ul><li>first li</li><li>2013-03-28 19:13:58", l.before("</li>").inclus(true, false).all().get());
		assertEquals("<ul><li>first li</li>,<ul><li>first li</li><li>2013-03-28 19:13:58</li>", l.before("</li>").inclus(false, true).all().get());
		assertEquals("<ul><li>first li</li>,<ul><li>first li</li><li>2013-03-28 19:13:58</li>", l.before("</li>").inclus(true, true).all().get());
		
		//negate
		assertEquals("</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.before("</li>").negates().get());
		assertEquals("</li></ul><ul>second ul</ul>", l.before("</li>").negates().get(2));
		
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.before("<li>").negates().find().first());
		
		//        <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().before("<li>").negates().find().all());
		assertEquals(",<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,,<li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().before("<li>").fnegates(",").find().all());
		assertEquals(",first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,,2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().before("<li>").inclus(false, true).fnegates(",").find().all());
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,                     <li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().before("<li>").fnegates(' ').find().all());
		assertEquals("        first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,                         2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().before("<li>").inclus(false, true).fnegates(' ').find().all());
		assertEquals("<li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.before("<li>").negates().get(2));
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("                     <li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.before("<li>").fnegates(' ').find().all());
		assertEquals(" <li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.before("<li>").fnegates(" ").find().all());
	}
	
	private void betnStrAsymmLook(Finder l) {
		log.debug("betnStrAsymm");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("first li</li><li>2013-03-28 19:13:58", l.betn("<li>", "</li>").asymm(1, 2).first().get());
		assertEquals("first li</li><li>2013-03-28 19:13:58", l.betn("<li>", "</li>").asymms(1, 2).get());
		
		assertEquals("2013-03-28 19:13:58", l.betn("<li>", "</li>").asymms(2, 2).find().all());
		assertEquals("<li>2013-03-28 19:13:58", l.betn("<li>", "</li>").asymm(2, 2).inclusL().find().all());
		assertEquals("2013-03-28 19:13:58</li>", l.betn("<li>", "</li>").asymm(2, 2).inclusR().find().all());
		assertEquals("<li>2013-03-28 19:13:58</li>", l.betn("<li>", "</li>").asymm(2, 2).inclus().find().all());
		
		assertEquals(">first li</li><li>2013-03-28 19:13:58</", l.betn("li", "li").asymms(1, 5).get());
		
		//negate
		assertEquals("<ul><lili></ul><ul>second ul</ul>", l.betn("li", "li").asymm(1, 5).negates().get());
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li                                       li></ul><ul>second ul</ul>", 
				l.betn("li", "li").asymm(1, 5).fnegates(' ').get());
		assertEquals("<ul><li***************************************li></ul><ul>second ul</ul>", 
				l.betn("li", "li").asymm(1, 5).fnegates('*').get());
		assertEquals("<ul><li*li></ul><ul>second ul</ul>", l.betn("li", "li").asymm(1, 5).fnegates("*").get());
		
		assertEquals("<ul>*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li>*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().betn("<ul>", "first").asymm(1, 1).fnegates("*").find().all());
		assertEquals("<ul>*first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>@123<ul><li>first li</li>*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.splitAll().separ("@123").betn("<ul>", "first").asymm(1, 1).fnegates("*").find().all());
		assertEquals("<ul>*first li</li>*2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.separ("@123").betn("<ul>", "first").asymm(1, 1).fnegates("*").find().all());
	}

	private void betnStrLook(Finder l) {
		log.debug("betnStrLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("first li", l.betns("<li>", "</li>").get());
		assertEquals("first li", l.betn("<li>", "</li>").first().get());
		assertEquals("2013-03-28 19:13:58", l.betns("<li>", "</li>").getLast());
		assertEquals("2013-03-28 19:13:58", l.betn("<li>", "</li>").last().get());
		
		assertEquals("2013-03-28 19:13:58", l.betn("<li>", "</li>").pos(2).get());
		assertEquals("2013-03-28 19:13:58", l.betns("<li>", "</li>").find().pos(2));
		assertEquals("<li>2013-03-28 19:13:58", l.betn("<li>", "</li>").inclusL().find().pos(2));
		assertEquals("2013-03-28 19:13:58</li>", l.betn("<li>", "</li>").inclusR().find().pos(2));
		assertEquals("<li>2013-03-28 19:13:58</li>", l.betn("<li>", "</li>").inclus().find().pos(2));
		
		assertEquals("first li,2013-03-28 19:13:58", l.betn("<li>", "</li>").all().get());
		assertEquals("first li,2013-03-28 19:13:58", l.betns("<li>", "</li>").find().all());
		
		//negate
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li></li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.betn("<li>", "</li>").negates().get());
		assertEquals("<ul><li>first li</li><li></li></ul><ul>second ul</ul>", l.betn("<li>", "</li>").negates().get(2));
		assertEquals("<ul><li>first li</li><li></li></ul><ul>second ul</ul>", l.betn("<li>", "</li>").negate().pos(2).get());
		assertEquals("<ul><li>first li</li><li></li></ul><ul>second ul</ul>", l.betn("<li>", "</li>").negates().getLast());
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>                   </li></ul><ul>second ul</ul>", l.betn("<li>", "</li>").fnegates(' ').getLast());
		assertEquals("<ul><li>        </li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li><li>                   </li></ul><ul>second ul</ul>", 
				l.splitAll().betn("<li>", "</li>").fnegates(' ').find().all());
		assertEquals("<ul>                 <li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li>                            </ul><ul>second ul</ul>", 
				l.splitAll().betn("<li>", "</li>").inclus(true, true).fnegates(' ').find().all());
		
		assertEquals("<ul>                                             </ul><ul>second ul</ul>", l.betn("<ul>", "</ul>").fnegates(' ').get());
		assertEquals("<ul>                                             </ul><ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>         </ul>", 
				l.splitAll().betn("<ul>", "</ul>").fnegates(' ').find().all());
		
		assertEquals("<li>                                    </li>,<li><li>          </li><li>19:13:58</li></li>,<li><li>2013-03-28</li><li>        </li></li>", 
				l.splitAll().set("<li><li>2013-03-28</li><li>19:13:58</li></li>").betn("<li>", "</li>").fnegates(' ').find().all());
		assertEquals("<li>                                    </li>", 
				l.set("<li><li>2013-03-28</li><li>19:13:58</li></li>").betn("<li>", "</li>").fnegates(' ').find().all());
		
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>        </li><li>                   </li></ul><ul>second ul</ul>", 
				l.betn("<li>", "</li>").fnegates(' ').find().all());
		assertEquals("<ul><li></li><li></li></ul><ul>second ul</ul>", 
				l.betn("<li>", "</li>").negates().find().all());
		assertEquals("<ul>                                             </ul><ul>second ul</ul>", 
				l.betn("<li>", "</li>").inclus(true, true).fnegates(' ').find().all());
	}
	
	private void afterIdxLook(Finder l) {
		log.debug("afterIdxLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("</ul>", l.afters(67).get());
		assertEquals("</ul>,</ul>", l.after(67).all().get());
		assertEquals("</ul>,</ul>", l.afters(67).find().all());
		
		//negate
		assertEquals("<ul>", l.after(4).negates().get());
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>     <ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul     ", 
				l.splitAll().after(67).fnegates(' ').find().all());
		//            <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>*<ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul*", 
				l.splitAll().after(67).fnegates("*").find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>#####<ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul#####", 
				l.splitAll().after(67).fnegates('#').find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li><ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul", 
				l.splitAll().after(67).negates().find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li><ul>second ul</ul>###<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul", 
				l.splitAll().separ("###").after(67).negates().find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li><ul>second ul</ul>#<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul", 
				l.splitAll().separ('#').after(67).negates().find().all());
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li><ul>second ul", 
				l.after(67).negates().find().all());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li>     <ul>second ul     ", 
				l.after(67).fnegates(' ').find().all());
		
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li><ul>second ul</ul>", l.after(67).negates().get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul", l.after(67).negates().get(2));
	}
	
	private void beforeIdxLook(Finder l) {
		log.debug("beforeIdxLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58", l.befores(44).get());
		assertEquals("<ul>", l.befores(4).get());
		assertEquals("<ul>,<ul>", l.before(4).all().get());
		assertEquals("<ul>,<ul>", l.befores(4).find().all());
		
		//negate
		assertEquals("</li></ul><ul>second ul</ul>", l.before(44).negates().get());
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>    second ul</ul>", 
				l.splitAll().before(4).fnegate(' ').all().get());
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>@<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>    second ul</ul>", 
				l.splitAll().separ('@').before(4).fnegate(' ').all().get());
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li></ul>    second ul</ul>", 
				l.before(4).fnegate(' ').all().get());
		assertEquals(" <li>first li</li><li>2013-03-28 19:13:58</li></ul> second ul</ul>", 
				l.before(4).fnegate(" ").all().get());
		//			  <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("    <li>first li</li><li>2013-03-28 19:13:58</li></ul>    second ul</ul>", 
				l.before(4).fnegate(' ').all().get());
		assertEquals("hello<li>first li</li><li>2013-03-28 19:13:58</li></ul>hellosecond ul</ul>", 
				l.before(4).fnegate("hello").all().get());
		
		//        <ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", l.before(4).negates().get());
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>second ul</ul>", l.before(4).negates().get(2));
		assertEquals("<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul>    second ul</ul>", l.before(4).fnegates(' ').get(2));
	}

	private void betnIdxLook(Finder l) {
		log.debug("betnIdxLook");
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("li", l.betns(5, 7).get());
		assertEquals("li", l.betns(5, 7).getLast());
		assertEquals("li", l.betn(5, 7).first().get());
		assertEquals("li,li,li,li,li", l.betns(5, 7).find().all());
		assertEquals("li,li,li,li,li", l.betn(5, 7).all().get());
		
		assertEquals("first li", l.betns(8, 16).get());
		assertEquals("first li", l.betns(8, 16).find().all());
		
		assertEquals("ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul", l.betns(1, -1).get());
		
		//negate
		assertEquals("<ul><li>second ul</ul>", l.betn(8, 58).negates().get());
		//<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>
		assertEquals("<ul>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>", 
				l.betn(4, 8).negates().get());
		assertEquals("<ul><  >first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first   </li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</  ><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li><  >2013-03-28 19:13:58</li></ul><ul>second ul</ul>,<ul><li>first li</li><li>2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				l.splitAll().betn(5, 7).fnegates(' ').find().all());
		assertEquals("<ul><  >first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>#<ul><li>first   </li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>#<ul><li>first li</  ><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>#<ul><li>first li</li><  >2013-03-28 19:13:58</li></ul><ul>second ul</ul>#<ul><li>first li</li><li>2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", 
				l.splitAll().separ('#').betn(5, 7).fnegates(' ').find().all());
		assertEquals("<ul><  >first   </  ><  >2013-03-28 19:13:58</  ></ul><ul>second ul</ul>", l.betn(5, 7).fnegates(' ').find().all());
		assertEquals("<ul><##>first ##</##><##>2013-03-28 19:13:58</##></ul><ul>second ul</ul>", l.betn(5, 7).fnegates("##").find().all());
	}

	@Test
	public void testBetween() {
		String str = "{cmd:8012,code:0,result:{idx:3,oid:1a87410i result }}";
		String separator = "result";
		Betner betn = Betner.of(str);
		
		assertEquals(2, betn.before(separator).countMatches());
		assertEquals("{cmd:8012,code:0,", betn.before(separator).first());
		assertEquals("{cmd:8012,code:0,result", betn.before(separator).inclusive(true, true).first());
		//before the left is beginning
		assertEquals("{cmd:8012,code:0,result", betn.before(separator).inclusive(false, true).first());
		
		betn.inclusReset();
		assertEquals("{cmd:8012,code:0,result:{idx:3,oid:1a87410i ", betn.before(separator).last());
		
		assertEquals(StringUtils.substringBefore(str, separator), Strs.betn(str).before(separator).first());
		assertEquals(StringUtils.substringBeforeLast(str, separator), Strs.betn(str).before(separator).last());
		
		assertEquals(StringUtils.substringAfter(str, separator), Strs.betn(str).after(separator).first());
		assertEquals(StringUtils.substringAfterLast(str, separator), Strs.betn(str).after(separator).last());
		
		assertEquals(":{idx:3,oid:1a87410i result }}", betn.after(separator).first());
		assertEquals("result:{idx:3,oid:1a87410i result }}", betn.after(separator).inclusive(true, true).first());
		//after the right is ending
		assertEquals("result:{idx:3,oid:1a87410i result }}", betn.after(separator).inclusive(true, false).first());
		
		betn.inclusReset();
		assertEquals(" }}", betn.after(separator).last());
		
		assertEquals("idx:3,oid:1a87410i result ", betn.between("{", "}").result(2));
		assertEquals("{idx:3,oid:1a87410i result ", betn.between("{", "}").inclusL().result(2));
		betn.inclusReset();
		assertEquals("idx:3,oid:1a87410i result }", betn.between("{", "}").inclusR().result(2));
		betn.inclusReset();
		assertEquals("{idx:3,oid:1a87410i result }", betn.between("{", "}").inclusBoth().result(2));
		betn.inclusReset();
		
		//"{cmd:8012,code:0,result:{idx:3,oid:1a87410i result }}"
		assertEquals("0,result:{idx", betn.betweenLast(":").result(2));
		betn.inclusReset();
		assertEquals(":0,result:{idx", betn.betweenLast(":").inclusL().result(2));
		betn.inclusReset();
		assertEquals("0,result:{idx:", betn.betweenLast(":").inclusR().result(2));
		betn.inclusReset();
		assertEquals(":0,result:{idx:", betn.betweenLast(":").inclusBoth().result(2));
		betn.inclusReset();
		
		assertEquals("{idx", betn.betweenNext(":").result(2));
		betn.inclusReset();
		assertEquals(":{idx", betn.betweenNext(":").inclusL().result(2));
		betn.inclusReset();
		assertEquals("{idx:", betn.betweenNext(":").inclusR().result(2));
		betn.inclusReset();
		assertEquals(":{idx:", betn.betweenNext(":").inclusBoth().result(2));
		betn.inclusReset();
	}
	
	@Test
	public void testComparer() {
		Object target = null;
		assertFalse(Comparer.expect(target, 'L'));
		assertTrue(Comparer.expect(target, null, 'L'));
//		assertTrue(Comparer.expect(target, null));
		target = 'L';
		assertTrue(Comparer.expect(target, null, 'L'));
		assertFalse(Comparer.expect(null, 'M', 'N'));
		target = 'N';
		assertTrue(Comparer.expect(target, 'M', 'N'));
		
		Integer delegate = 30;
		Comparer<Integer> c = Comparer.of(delegate);
		
		assertTrue(c.gte(30));
		assertTrue(c.gt(29));
		assertTrue(c.is(30));
		assertTrue(c.lt(31));
		assertTrue(c.lte(30));
		
		assertFalse(c.gt(30));
		assertFalse(c.lt(30));
		
		assertFalse(c.all(30, 31, null));
		assertFalse(c.any(null, 35, 29));
		assertTrue(c.all(30));
		assertTrue(c.any(null, 35, 29, 30));
		
		assertTrue(Object[].class.isInstance(new Object[]{}));
		assertTrue(Object[].class.isInstance(new String[]{}));
		assertTrue(Object[].class.isInstance(new BigDecimal[]{}));
		assertTrue(Object[].class.isInstance(new Integer[]{}));
		assertTrue(Object[].class.isInstance(new Dater[]{}));
	}
	
	@Test
	public void testDater() throws ParseException {
		String sDate = "2012-11-11 23:11:11";
		Dater dater = Dater.of(sDate);
		
		String testP = "yyyyMMdd HH:mm:ss";
		String testD = "20121111 00:00:00";
		Dater testDater = Dater.of(testD, testP);
		
		assertEquals(sDate, dater.asText());
		assertTrue(dater.isSameDay(testDater.get()));
		assertFalse(dater.isSameInstant(testDater.get()));
		assertFalse(dater.asText().equals(testDater.asText()));
		
		assertEquals(dater.add().year(1).asText(), "2013-11-11 23:11:11");
		assertEquals(dater.add().minute(1).asText(), "2013-11-11 23:12:11");
		assertEquals(dater.add().month(-1).asText(), "2013-10-11 23:12:11");
		assertEquals(dater.add().month(1).asText(), "2013-11-11 23:12:11");
		assertEquals(dater.add().minute(-1).asText(), "2013-11-11 23:11:11");
	
		assertEquals(dater.set().year(1).asText(), "0001-11-11 23:11:11");
		assertEquals(dater.set().year(2012).asText(), "2012-11-11 23:11:11");
		
		assertEquals(dater.asDayText(), "2012-11-11");
		assertEquals(dater.asClockText(), "23:11:11");
		assertEquals(dater.asText("yyyyMMddHHmmss"), "20121111231111");
		assertEquals(dater.asText(), "2012-11-11 23:11:11");
		
		assertEquals(dater.add().years(10).months(10).minute(20)
				.set().hours(1).second(80).asText() , "2023-09-11 01:32:20");
		
		assertEquals(dater.asDayRangeText()[0], "2023-09-11 00:00:00");
		assertEquals(dater.asDayRangeText()[1], "2023-09-11 23:59:59");
		
		assertEquals(dater.asRangeText("16:00:00", "18:00:00")[0], "2023-09-11 16:00:00");
		assertEquals(dater.asRangeText("16:00:00", "18:00:00")[1], "2023-09-11 18:00:00");
		
		assertTrue(dater.inThisDay(Dater.of("2023-09-11 23:59:59").get()));
		assertTrue(dater.inThisDay(Dater.of("2023-09-11 00:00:00").get()));
		assertTrue(dater.inGivenDay(Dater.of("2023-09-11 11:11:11").get()));
		
		//2012-11-11 23:11:11
		Dater testDater2 = Dater.of(sDate);
		String tDate = "2012-11-11 22:11:11";
		
		assertTrue(testDater2.since(tDate).is(Dater.HOUR));
		assertTrue(testDater2.since(tDate).lt(61 * Dater.MINUTE));
		assertTrue(testDater2.since(tDate).lte(61 * Dater.MINUTE));
		assertTrue(testDater2.since(tDate).gt(59 * Dater.MINUTE));
		assertTrue(testDater2.since(tDate).gte(59 * Dater.MINUTE));
		
		tDate = "2012-12-11 23:11:11";
		assertTrue(testDater2.until(tDate).lt(Dater.DAY * 39));
		assertTrue(testDater2.until(tDate).lte(Dater.DAY * 39));
		assertTrue(testDater2.until(tDate).gt(Dater.DAY * 29));
		assertTrue(testDater2.until(tDate).gte(Dater.DAY * 29));
		assertTrue(testDater2.until(tDate).is(Dater.DAY * 30));
		
		Date now = new Date();
		long increment = Dater.of(now).add().hour(1).sinceMillis(now);
		assertEquals((DateUtils.addHours(now, 1).getTime() - now.getTime()), increment);
		
		assertEquals("2013-01-05 09:02:48", Dater.iso("2013-01-05T01:02:48.28Z").asText());
	}
	
	@Test
	public void testParseable() {
		assertTrue(Objects2.isParseable("123456"));
		assertTrue(Objects2.isParseable(123456));
		
		assertFalse(Objects2.isParseable("he1"));
		assertFalse(Objects2.isParseable("he"));
		assertFalse(Objects2.isParseable(null));
		
		assertTrue(Funcs.TO_BOOLEAN.apply("tRue"));
		assertFalse(Funcs.TO_BOOLEAN.apply("FalSe"));
		assertNull(Funcs.TO_BOOLEAN.apply("a"));
		assertNull(Funcs.TO_BOOLEAN.apply("hell"));
		assertNull(Funcs.TO_BOOLEAN.apply(null));
	}
	
	@Test
	public void testMD5() {
		String s = "{\"cmd\":30008,\"request\":{\"oid\":\"4eae0A2k\",\"did\":\"6fdba0FF\",\"pid\":\"94b308Wv\",\"type\":1,\"txt\":\"澧冪晫\"}}";
		byte[] bytes = s.getBytes(Charsets.UTF_8);
		String res = Hashing.md5().hashBytes(bytes).toString();
		String res2 = Hashing.md5().newHasher().putBytes(bytes).hash().toString();
		
		assertEquals(res, res2);
	}
	
	@Test
	public void testIndex() {
		String s = "test hello world test";
		
		Indexer sub = Indexer.of(s);
		assertEquals("s", sub.quietly().between(2, 3));
		assertEquals(Strs.EMPTY, sub.between(2, -2));
		
		sub = Indexer.of(s);
		assertEquals("st hello world te", sub.between(2, -2));
		assertEquals(" hello world ", sub.between(2, -2));
		assertEquals("ello world ", sub.after(2));
		assertEquals("lo world ", sub.after(2));
		assertEquals("lo worl", sub.before(-2));
		assertEquals("lo wo", sub.before(-2));
		
		sub = Indexer.of(s);
		assertEquals("te", sub.between(-4, -2));
		
		sub = Indexer.of(s);
		assertEquals("test", sub.after(-4));
		
		sub = Indexer.of(s).resetMode();
		assertEquals("st hello world te", sub.between(2, -2));
		assertEquals("st hello world te", sub.between(2, -2));
		assertEquals("st hello world te", sub.between(2, -2));
	}
	
	@Test
	public void testIsEmpty() {
		assertTrue(Strs.isEmpty(null));
		assertTrue(Strs.isEmpty(""));
		assertFalse(Strs.isEmpty(" "));
		assertFalse(Strs.isEmpty("   "));
		
		assertTrue(Strs.isBlank("   "));
		assertFalse(Strs.isBlank("null"));
		assertTrue(Strs.isBlank("null", "null"));
	}
	
	@Test
	public void testContainsIndex() {
		String[] any = new String[]{ "pid", "did"};
		assertTrue(Strs.contains("hedidabc", any));
		assertEquals(2, Strs.indexAny("hedidabc", any));
		assertEquals(Strs.INDEX_NONE_EXISTS, Strs.indexAny("", any));
	}
	
	@Test
	public void testEndAny() {
		String[] any = new String[]{ "pid", "did"};
		assertTrue(Strs.endAny("piddid", any));
		assertTrue(Strs.endAny("piddidpid", any));
		assertFalse(Strs.endAny("hell", any));
	}
	
	@Test
	public void testStartAny() {
		String[] any = new String[]{ "pid", "did"};
		assertTrue(Strs.startAny("pid=a326cd3", any));
		assertTrue(Strs.startAny("pid=a326cd3", 0, any));
		assertFalse(Strs.startAny("pid=a326cd3", 1, any));
		
		assertFalse(Strs.startAny("adid", any));
		assertTrue(Strs.startAny("didpidab", 3, any));
	}
	
	@Test
	public void testLookerReplacerSetXXX() {
		String target = "<ul><li>first li</li><li>2013-03-28 19:13:58</li></ul><ul>second ul</ul>";
		Finder l = Finder.of(target);
		
		assertEquals(l.betns(5, 7).get(), l.setBetns(5, 7).first().get());
		assertEquals(l.betn(5, 7).all().get(), l.setBetns(5, 7).all().get());
		assertEquals(l.separ("separ").betn(5, 7).all().get(), l.separ("separ").setBetns(5, 7).all().get());
		assertEquals(l.separ("split").splitAll().betn(5, 7).all().get(), l.separ("split").splitAll().setBetns(5, 7).all().get());
		assertEquals(l.betn(5, 7).last().get(), l.setBetns(5, 7).last().get());
		assertEquals(l.betn(5, 7).pos(10).get(), l.setBetns(5, 7).pos(10).get());
		assertEquals(l.betn(5, 7).fnegate("betn").first().get(), l.setBetns(5, 7).fnegate("betn").first().get());
		
		assertEquals(l.before(-5).first().get(), l.setBefores(-5).first().get());
		assertEquals(l.before(-5).all().get(), l.setBefores(-5).all().get());
		assertEquals(l.before(-5).last().get(), l.setBefores(-5).last().get());
		assertEquals(l.before(-5).pos(2).get(), l.setBefores(-5).pos(2).get());
		assertEquals(l.before(-5).fnegate("before").pos(2).get(), l.setBefores(-5).fnegate("before").pos(2).get());
		
		assertEquals(l.after(-5).first().get(), l.setAfters(-5).first().get());
		assertEquals(l.after(-5).all().get(), l.setAfters(-5).all().get());
		assertEquals(l.after(-5).last().get(), l.setAfters(-5).last().get());
		assertEquals(l.after(-5).pos(2).get(), l.setAfters(-5).pos(2).get());
		assertEquals(l.after(-5).fnegate("after").pos(2).get(), l.setAfters(-5).fnegate("after").pos(2).get());
		
		assertEquals(l.betn("<li>", "</li>").first().get(), l.setBetn("<li>", "</li>").first().get());
		assertEquals(l.betn("<li>", "</li>").last().get(), l.setBetn("<li>", "</li>").last().get());
		assertEquals(l.betn("<li>", "</li>").all().get(), l.setBetn("<li>", "</li>").all().get());
		assertEquals(l.betn("<li>", "</li>").pos(2).get(), l.setBetn("<li>", "</li>").pos(2).get());
		assertEquals(l.betn("<li>", "</li>").asymm(1, 2).pos(2).get(), l.setBetn("<li>", "</li>").asymm(1, 2).pos(2).get());
		assertEquals(l.betn("<li>", "</li>").asymm(1, 2).inclus(true, true).pos(1).get(), 
				l.setBetn("<li>", "</li>").asymm(1, 2).inclus(true, true).pos(1).get());
	}
	
	@Test
	public void testReplacerLookerUsage2() {
		String target = "";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "abc \r";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "abc\n";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "abc\r\n";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "abc";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "abc\nabc";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "a";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "\r";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "\n";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
		target = "\r\n";
		assertEquals(StringUtils.chop(target), Strs.chop(target));
	}
	
	// ------------------------------------------------------------- //
	
	List<Person> getTestPersons() {
		List<Person> persons = Lists.newArrayList();
		for (int i = 0; i < 20; i++) {
			Map<String, Object> m = Maps.newHashMap();
			m.put("ik1" + (i + 100), (i + 100));
			m.put("ik2", i);
			
			InnerAddr innerAddr = new InnerAddr();
			innerAddr.setInnerLong(1000000L + i);
			innerAddr.setInnerMap(m);
			
			Address addr = new Address();
			addr.setCity(310000 + i);
			addr.setStreets(ImmutableList.of("street" + i, "road" + i));
			addr.setInnerAddr(innerAddr);
			
			Person p = new Person("name" + i, 10 + i);
			p.setAddr(addr);
			
			persons.add(p);
		}
		return persons;
	}
	
	Person getTestPerson() {
		Map<String, Object> m = Maps.newHashMap();
		m.put("ikey1", 18);
		m.put("ikey2", "ikey2 value");
		m.put("ikey3", getTestPersons().get(5));
		
		InnerAddr innerAddr = new InnerAddr();
		innerAddr.setInnerLong(1000000L);
		innerAddr.setInnerMap(m);
		
		Address addr = new Address();
		addr.setCity(310000);
		addr.setStreets(ImmutableList.of("street 1", "gu dun street", "nan jiu road 23"));
		addr.setInnerAddr(innerAddr);
		
		Person person = new Person("hello world", 999);
		person.setAddr(addr);
		return person;
	}
	
	private Map<String, Object> getTestMap() {
		String str = "{\"cmd\":8006,\"code\":0,\"result\":{\"oid\":\"be38a0d8\",\"pid\":\"a9b530To\",\"did\":\"65649e0t\"," +
				"\"type\":2,\"snd\":\"422720ty\",\"lat\":0.0,\"lng\":0.0,\"upt\":1352184076535,\"name\":\"paulo\",\"length\":59}}";
		Map<String, Object> m = readMap(str);
		m.put("person", new Person("jack", 23));
		m.put("maptest", getTestMap2());
		return m;
	}
	
	private Map<Object, Object> getTestMap2() {
		Map<Object, Object> m = ImmutableMap.<Object, Object>of(
				33, new Person("hello", 33), 
				44, 44, 
				55, ImmutableMap.of(
						333, new Person("3 ceng person", 333),
						444, 444,
						555, ImmutableMap.of(
								3333, new Person("4 ceng person", 3333),
								4444, 4444,
								5555, ImmutableMap.of(
										33333, new Person("5 ceng person", 33333),
										44444, 44444,
										55555, mapTest()
									)
							)
					)
			);
		return m;
	}
	
	private Map<Object, Object> mapTest() {
		Map<Integer, Object> innM = Maps.newHashMap();
		innM.put(888888, 888888);
		
		Map<Object, Object> m = ImmutableMap.<Object, Object>of(
				66, new Person("hello", 66), 
				77, 77, 
				88, ImmutableMap.of(
						666, new Person("3 ceng person", 666),
						777, 777,
						888, ImmutableMap.of(
								6666, new Person("4 ceng person", 6666),
								7777, 7777,
								8888, ImmutableMap.of(
										66666, new Person("5 ceng person", 66666),
										77777, 77777,
										88888, 88888,
										99999, innM
										)
								)
						)
				);
		return m;
	}
	
	public String toJson(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		try {
			mapper.writeValue(writer, object);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			log.error("Unable to serialize to json: " + object, e);
			return null;
		}
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readMap(String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, HashMap.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}


class Person {
	private String name;
	private Integer age;
	private Address addr;
	public Person() {}
	public Person(String name, Integer age) {
		this.name = name;
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public Address getAddr() {
		return addr;
	}
	public void setAddr(Address addr) {
		this.addr = addr;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (!age.equals(other.age))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}

class Address {
	private Integer city;
	private List<String> streets;
	private InnerAddr innerAddr;
	public Integer getCity() {
		return city;
	}
	public void setCity(Integer city) {
		this.city = city;
	}
	public List<String> getStreets() {
		return streets;
	}
	public void setStreets(List<String> streets) {
		this.streets = streets;
	}
	public InnerAddr getInnerAddr() {
		return innerAddr;
	}
	public void setInnerAddr(InnerAddr innerAddr) {
		this.innerAddr = innerAddr;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result
				+ ((innerAddr == null) ? 0 : innerAddr.hashCode());
		result = prime * result + ((streets == null) ? 0 : streets.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (innerAddr == null) {
			if (other.innerAddr != null)
				return false;
		} else if (!innerAddr.equals(other.innerAddr))
			return false;
		if (streets == null) {
			if (other.streets != null)
				return false;
		} else if (!streets.equals(other.streets))
			return false;
		return true;
	}
	
}

class InnerAddr {
	private Map<String, Object> innerMap;
	private Long innerLong;
	public Map<String, Object> getInnerMap() {
		return innerMap;
	}
	public void setInnerMap(Map<String, Object> innerMap) {
		this.innerMap = innerMap;
	}
	public Long getInnerLong() {
		return innerLong;
	}
	public void setInnerLong(Long innerLong) {
		this.innerLong = innerLong;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((innerLong == null) ? 0 : innerLong.hashCode());
		result = prime * result
				+ ((innerMap == null) ? 0 : innerMap.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InnerAddr other = (InnerAddr) obj;
		if (innerLong == null) {
			if (other.innerLong != null)
				return false;
		} else if (!innerLong.equals(other.innerLong))
			return false;
		if (innerMap == null) {
			if (other.innerMap != null)
				return false;
		} else if (!innerMap.equals(other.innerMap))
			return false;
		return true;
	}
	
}


class L1 {
	private Integer one;

	public Integer getOne() {
		return one;
	}

	public void setOne(Integer one) {
		this.one = one;
	}
}

class L2 extends L1 {
	private String two;

	public String getTwo() {
		return two;
	}

	public void setTwo(String two) {
		this.two = two;
	}
}

class LDest {
	private Integer one;
	private String two;
	public Integer getOne() {
		return one;
	}
	public void setOne(Integer one) {
		this.one = one;
	}
	public String getTwo() {
		return two;
	}
	public void setTwo(String two) {
		this.two = two;
	}
}
