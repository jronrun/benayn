package com.benayn;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.benayn.ustyle.Arrays2;
import com.benayn.ustyle.DateStyle;
import com.benayn.ustyle.Dater;
import com.benayn.ustyle.Dater.DateUnit;
import com.benayn.ustyle.JSONer;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Reflecter.MethodOptions;
import com.benayn.ustyle.Resolves;
import com.benayn.ustyle.Sources;
import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.string.Indexer;
import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;


public class Me5Test extends Me4Test {
    
    @Test
    public void testTmp() {
        
    }
    
    @Test
	public void testDaterInterval() {
		Dater dater = Dater.now();
		DateUnit add = dater.add();
		
		Dater now = Dater.now();
		assertEquals("just now", now.interval(add.get()));
		
		assertEquals("10 months ago", now.interval(add.month(-10).get()));
		add.month(10);
		
		assertEquals("1 months ago", now.interval(add.days(-31).get()));
		add.days(31);
		
		assertEquals("7 days ago", now.interval(add.days(-10).get()));
		add.days(10);
		
		assertEquals("6 days ago", now.interval(add.days(-6).get()));
		add.days(6);
		
		assertEquals("1 days ago", now.interval(add.hours(-24).get()));
		add.hour(24);
		
		assertEquals("22 hours ago", now.interval(add.hour(-22).get()));
		add.hour(22);
		
		assertEquals("1 hours ago", now.interval(add.minute(-60).get()));
		add.minute(60);
		
		assertEquals("55 minutes ago", now.interval(add.minute(-55).get()));
		add.minute(55);
	}
    
    @Test
    public void testDaterPK() {
		DateStyle[] styles = new DateStyle[] {DateStyle.TIGHT, DateStyle.DAY, DateStyle.DEFAULT};
		
        for (DateStyle style : styles) {
        	Dater dater = Dater.of(new Date(), style);
            
            Date now = new Date();
            String nowStr = Dater.now().asText();
            assertFalse(dater.before(now));
            assertFalse(dater.after(now));
            assertTrue(dater.meanwhile(now));
            assertTrue(dater.beforeOrMeanwhile(now));
            
            assertFalse(dater.before(nowStr));
            assertFalse(dater.after(nowStr));
            assertTrue(dater.meanwhile(nowStr));
            assertTrue(dater.beforeOrMeanwhile(nowStr));
            
            dater.add().day(-1);
            assertTrue(dater.before(now));
            assertFalse(dater.after(now));
            assertFalse(dater.meanwhile(now));
            assertTrue(dater.beforeOrMeanwhile(now));
            
            assertTrue(dater.before(nowStr));
            assertFalse(dater.after(nowStr));
            assertFalse(dater.meanwhile(nowStr));
            assertTrue(dater.beforeOrMeanwhile(nowStr));
            
            dater.add().day(2);
            assertFalse(dater.before(now));
            assertTrue(dater.after(now));
            assertFalse(dater.meanwhile(now));
            assertFalse(dater.beforeOrMeanwhile(now));
            
            assertFalse(dater.before(nowStr));
            assertTrue(dater.after(nowStr));
            assertFalse(dater.meanwhile(nowStr));
            assertFalse(dater.beforeOrMeanwhile(nowStr));
            
		}
    }
    
    @Test
    public void testTierKeyColl() {
        String target = Sources.asString("test.json");
        Map<String, Object> map = JSONer.readDeepTierMap(target);
        assertTrue(88056 == Resolves.<Integer>get(int.class, map.get("result.items[1].shopId")));
        assertTrue(116.668537d == (Double) map.get("result.items[0].loc[0]"));
        assertTrue(39.127551d == (Double) map.get("result.items[1].loc[1]"));
        assertTrue(3l == (Long) map.get("result.days[2]"));
        
        Map<String, Object> root = Mapper.asDeepTierMap(Maps.<String, Object>newHashMap());
        Mapper<String, Object> rootM = Mapper.from(root);
        
        //primitive array
        root.put("test4.priarr", new byte[1]);
        assertNotNull(root.get("test4.priarr"));
        root.put("test4.priarr[0]", (byte) 15);
        assertTrue(root.containsKey("test4.priarr[0]"));
        assertDeepEqual((byte) 15, root.get("test4.priarr[0]"));
        assertTrue(((byte[]) root.get("test4.priarr")).length == 1);
        
        root.put("test4.priarr[1]", (byte) 88);
        assertTrue(root.containsKey("test4.priarr[1]"));
        assertDeepEqual((byte) 88, root.get("test4.priarr[1]"));
        assertTrue(((byte[]) root.get("test4.priarr")).length == 2);
        
        assertTrue((byte) 15 == (Byte) root.remove("test4.priarr[0]"));
        assertTrue(((byte[]) root.get("test4.priarr")).length == 1);
        assertTrue(root.containsKey("test4.priarr[0]"));
        assertDeepEqual((byte) 88, root.get("test4.priarr[0]"));
        
        //Object array
        root.put("test3.objarr", new Float[1]);
        assertNotNull(root.get("test3.objarr"));
        root.put("test3.objarr[0]", 3.0f);
        assertTrue(root.containsKey("test3.objarr[0]"));
        assertDeepEqual(3.0f, root.get("test3.objarr[0]"));
        assertTrue(((Object[]) root.get("test3.objarr")).length == 1);
        
        root.put("test3.objarr[1]", 22.3f);
        assertTrue(root.containsKey("test3.objarr[1]"));
        assertDeepEqual(22.3f, root.get("test3.objarr[1]"));
        assertTrue(((Object[]) root.get("test3.objarr")).length == 2);
        
        assertTrue(3.0f == (Float) root.remove("test3.objarr[0]"));
        assertTrue(((Object[]) root.get("test3.objarr")).length == 1);
        assertTrue(root.containsKey("test3.objarr[0]"));
        assertDeepEqual(22.3f, root.get("test3.objarr[0]"));
        
        //set
        root.put("test2.set", Sets.newHashSet());
        assertNotNull(root.get("test2.set"));
        root.put("test2.set[0]", (byte) 1);
        assertTrue(root.containsKey("test2.set[0]"));
        assertDeepEqual((byte) 1, root.get("test2.set[0]"));
        assertTrue(((Set<?>) root.get("test2.set")).size() == 1);
        
        root.put("test2.set[1]", (byte) 21);
        assertTrue(root.containsKey("test2.set[1]"));
        assertDeepEqual((byte) 21, root.get("test2.set[1]"));
        assertTrue(((Set<?>) root.get("test2.set")).size() == 2);
        
        assertTrue((byte) 1 == (Byte) root.remove("test2.set[0]")); 
        assertTrue(((Set<?>) root.get("test2.set")).size() == 1);
        assertTrue(root.containsKey("test2.set[0]"));
        assertDeepEqual((byte) 21, root.get("test2.set[0]"));
        
        //list
        root.put("test.list[0]", 11);
        rootM.info();
        
        assertTrue(root.get("test") instanceof Map);
        assertTrue(root.get("test.list") instanceof List);
        assertTrue(((List<?>) root.get("test.list")).size() == 1);
        assertDeepEqual(11, root.get("test.list[0]"));
        assertTrue(root.containsKey("test.list[0]"));
        
        root.put("test.list[1]", 22);
        assertTrue(((List<?>) root.get("test.list")).size() == 2);
        assertDeepEqual(22, root.get("test.list[1]"));
        assertTrue(root.containsKey("test.list[1]"));
        
        assertDeepEqual(11, root.remove("test.list[0]"));
        rootM.info();
        assertTrue(((List<?>) root.get("test.list")).size() == 1);
        assertDeepEqual(22, root.get("test.list[0]"));
        assertTrue(root.containsKey("test.list[0]"));
        assertFalse(root.containsKey("test.list[1]"));
        
    }
    
    public static class Item {
        Float floata;
        float floatp;
        Set<Float> sFloats;
        List<Float> floatList;
        Map<Float, Float> floatMap;
        Float[] aFloatWarr;
        float[] aFloatParr;
    }
    
    @Test
    public void testTypeConverter() {
    	final JSONer jsoner = JSONer.build();
        
        FacadeObject<Item> fo = FacadeObject.wrap(Item.class);
        fo.populate4Test();
        
        jsoner.register(new JSONer.GenericConverter<String, Float>() {

            @Override protected Float forward(String input) {
                String oo = (String) JSONer.readMap(input).get("xx");
                return Float.valueOf(oo.replace("xx", ""));
            }

            @Override protected String backward(Float input) {
                return String.format("{\"%s\" : \"xx%s\"}", "xx", input);
            }
        }, float.class, Float.class);
        
        String json = jsoner.update(fo.get()).asJson();
        Item item = jsoner.update(json).asObject(Item.class);
        
        assertDeepEqual(fo.get(), item);
    }
    
    @Test
    public void testJSONerConvert() {
        //test property and type converter
        FacadeObject<JsonTest> fo = FacadeObject.wrap(JsonTest.class);
        fo.populate4Test();
        
        final JSONer jsoner2 = JSONer.build();
        
        jsoner2.register(new JSONer.GenericConverter<String, Float>() {

        	@Override protected Float forward(String input) {
                String oo = (String) JSONer.readMap(input).get("xx");
                return Float.valueOf(oo.replace("xx", ""));
            }

            @Override protected String backward(Float input) {
                return String.format("{\"%s\" : \"xx%s\"}", "xx", input);
            }
        }, Float.class, float.class);
        
        jsoner2.register(new JSONer.GenericConverter<String, Short[]>() {

			@Override protected Short[] forward(String input) {
				return JSONer.read(input).asObject(Short[].class);
			}

			@Override protected String backward(Short[] input) {
				return JSONer.toJson(input);
			}
		}, Short[].class);
        
        jsoner2.register(new JSONer.GenericConverter<String, short[]>() {

            @Override protected short[] forward(String input) {
                Short[] s = JSONer.read(input).asObject(Short[].class);
                return Arrays2.unwrap(s);
            }

            @Override protected String backward(short[] input) {
                return JSONer.toJson(input);
         }}, short[].class);
        
        jsoner2.register(new JSONer.GenericConverter<String, Character[]>() {

            @Override
            protected Character[] forward(String input) {
                return JSONer.read(input).asObject(Character[].class);
            }

            @Override
            protected String backward(Character[] input) {
                return JSONer.toJson(input);
            }
        }, Character[].class);
        
        jsoner2.register(new JSONer.GenericConverter<String, Byte[]>() {

            @Override protected Byte[] forward(String input) {
                return JSONer.read(input).asObject(Byte[].class);
            }

            @Override protected String backward(Byte[] input) {
                return JSONer.toJson(input);
            }
        }, "bytePArr", "byteaArr");
        
        jsoner2.register(new JSONer.GenericConverter<String, Date>() {
            @Override protected Date forward(String input) {
                return Dater.from(Indexer.of(input).between(1, -1)).get();
            }
            @Override protected String backward(Date input) {
                return "x" + Dater.of(input).asText(DateStyle.ISO) + "y";
            }
        }, "date");
        
        jsoner2.register(new JSONer.GenericConverter<Number, EnumTest>() {
            @Override protected EnumTest forward(Number input) {
                if (5.1 == input.doubleValue()) {
                    return EnumTest.TEST1;
                }
                if (8.7 == input.doubleValue()) {
                    return EnumTest.TEST2;
                }
                return null;
            }
            @Override protected Number backward(EnumTest input) {
                switch (input) {
                    case TEST1: return 5.1;
                    case TEST2: return 8.7;
                }
                return null;
            }
        }, "enumTest");
        
        String jstr = jsoner2.update(fo.get()).asJson();
        log.info("after converter: " + jstr);
        JsonTest jt2 = jsoner2.update(jstr).asObject(JsonTest.class);
        assertDeepEqual(fo.get(), jt2);
        
        String jsonString = JSONer.toJson(fo.get());
        log.info("original: " + jsonString);
        JsonTest jsonTest = JSONer.read(jsonString).asObject(JsonTest.class);
        assertDeepEqual(fo.get(), jsonTest);
    }
    
    @Test
    public void testJsonerTypeConvert() {
      //test type converter
        Class<?>[] types = new Class[] {
            Byte.class, Character.class, Double.class, Integer.class, Long.class, Short.class, Float.class,
            Date.class, String.class, BigDecimal.class, BigInteger.class,
            Object.class, Object[].class, Enum.class, 
            Map.class, Set.class, List.class, 
            Class.class
        };
        
        Object[] values = new Object[] {
            (byte) 1, 'c', 2d, 3, 4L, (short) 5, 6F,
            new Date(), "string", new BigDecimal("11.11"), new BigInteger("12"),
            Randoms.get(User.class), new Object[]{1, 'a', "bb", Randoms.get(Address.class)}, EnumTest.TEST1, 
            ImmutableMap.of(Randoms.get(Lonlat.class), Randoms.get(User.class))
            , ImmutableSet.of(Randoms.get(Lonlat.class), Randoms.get(Address.class))
            , ImmutableList.of(Randoms.get(Lonlat.class), Randoms.get(User.class)), 
            User.class
        };
        
        JSONer jsoner = JSONer.build();
        
        for (int i = 0; i < types.length; i++) {
            if (types[i] == Class.class) {
                jsoner.register(new Converter<String, Class<?>>() {

                    @Override protected Class<?> doForward(String a) {
                        return User.class;
                    }

                    @Override protected String doBackward(Class<?> b) {
                        return String.format("{\"%s\" : \"%s\"}", b.toString(), b.getName());
                    }
                }, types[i]);
                
                continue;
            }
            
            final Object obj = values[i];
            
            jsoner.register(new Converter<String, Object>() {
                
                @Override protected Object doForward(String a) {
                    String objStr = (String) JSONer.of(a).asMap().get(obj.toString());
                    return JSONer.of(objStr).asObject(obj);
                }
                
                @Override protected String doBackward(Object b) {
                    return String.format("{\"%s\" : %s}", b.toString(), JSONer.toJson(b));
                }
            }, types[i]);
        }
        
        for (int i = 0; i < values.length; i++) {
            log.info(">>>> Test JSONer no." + (i + 1) + " " + types[i].getName());
            
            Object target = values[i];
            log.info("Before: " + target);
            
            String json = jsoner.update(target).asJson();
            assertDeepEqual(jsoner.getTypeConverter(types[i]).reverse().convert(target), json);
            log.info("JSON: " + json);
            
            if (types[i] == Class.class) {
                assertDeepEqual(target, jsoner.getTypeConverter(types[i]).convert(json));
                continue;
            }
            
            Object result = jsoner.update(json).asObject(target);
            log.info("result: " + result);
            
            assertDeepEqual(target, result);
        }
    }
    
    @Test
    public void testConverter() {
        Converter<Long, Date> convert1 = new Converter<Long, Date>() {
            
            @Override protected Date doForward(Long a) {
                return new Date(a);
            }
            
            @Override protected Long doBackward(Date b) {
                return b.getTime();
            }
        };
        
        Dater now = Dater.now();
        assertDeepEqual(convert1.convert(now.getTime()), now.get());
        assertDeepEqual(convert1.reverse().convert(now.get()), now.getTime());
        
        Function<Long, Date> func = convert1;
        assertDeepEqual(func.apply(now.getTime()), now.get());
        
        Converter<String, Long> convert2 = new Converter<String, Long>() {
            
            @Override protected Long doForward(String a) {
                return Longs.tryParse(a);
            }
            
            @Override protected String doBackward(Long b) {
                return String.valueOf(b);
            }
        };
        
        List<Converter<?, ?>> list = Lists.newArrayList();
        list.add(convert1);
        list.add(convert2);
        
        assertEquals(2, list.size());
        list.remove(convert1);
        assertEquals(1, list.size());
        list.remove(convert1);
        assertEquals(1, list.size());
        assertTrue(convert2.equals(list.get(0)));
        
        list.remove(convert2);
        assertEquals(0, list.size());
        
        Map<Converter<?, ?>, Integer> map = Maps.newHashMap();
        map.put(convert1, 1);
        map.put(convert2, 2);
        
        assertEquals(2, map.size());
        map.remove(convert1);
        assertEquals(1, map.size());
        map.remove(convert1);
        assertEquals(1, map.size());
        
        Converter<?, ?> tmp = null;
        for (Converter<?, ?> converter : map.keySet()) {
            tmp = converter;
        }
        assertTrue(convert2.equals(tmp));
        
        map.remove(convert2);
        assertEquals(0, map.size());
    }
    
    @Test
    public void testReflectAppend() {
    	tReflectConstructor();
    	
    	ConstructorPublic pub = Randoms.get(ConstructorPublic.class);
    	FacadeObject<ConstructorPublic> fo = FacadeObject.wrap(pub);
    	
    	try {
			fo.getMethod("noneExistMethod");
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
			assertTrue(e.getMessage().contains("The method noneExistMethod is not exists of object"));
		}
    	
    	fo.populate4Test();
    	
    	assertEquals(pub.getName(), fo.method("getName").call());
    	fo.method("setName").call("new name");
    	assertEquals("new name", pub.getName());
    	
    	assertEquals(pub.getAge(), fo.method("getAge").call());
    	fo.method("setAge").call(11);
    	assertEquals(11, pub.getAge());
    	assertTrue(fo.method("setAge").isPublic());
    	
    	MethodOptions<ConstructorPublic> mo = fo.method("staticTest");
    	assertEquals(ConstructorPublic.staticTest(), mo.call());
    	assertTrue(mo.isPrivate());
    	assertTrue(mo.isStatic());
    	assertFalse(mo.isSynchronized());
    }
    
    public void tReflectConstructor() {
        //public
        FacadeObject<ConstructorPublic> pubFo = FacadeObject.wrap(ConstructorPublic.class);
        assertTrue(pubFo.instantiatable());
        assertFalse(pubFo.notInstantiatable());
        assertTrue(pubFo.hasDefaultConstructor());
        
        ConstructorPublic pub = pubFo.newInstance();
        assertNotNull(pub);
        
        pub = pubFo.newInstance("name");
        assertNotNull(pub);
        assertEquals("name", pub.name);
        assertEquals(0, pub.age);
        
        try {
            pub = pubFo.newInstance("name", 1);
        } catch (Exception e) {
            assertEquals(NoSuchMethodException.class, e.getCause().getClass());
        }
        
        pub = pubFo.constructor(String.class, int.class).newInstance("name", 1);
        assertNotNull(pub);
        assertEquals("name", pub.name);
        assertEquals(1, pub.age);
        
        Constructor<ConstructorPublic> conPub = pubFo.getConstructor(String.class, int.class);
        ConstructorPublic pub2 = Suppliers2.newInstance(conPub, "name", 1).get();
        assertDeepEqual(pub, pub2);
        
        //private
        Reflecter<ConstructorPrivate> privateRef = Reflecter.from(new ConstructorPrivate());
        assertTrue(privateRef.notInstantiatable());
        assertFalse(privateRef.instantiatable());
        assertFalse(privateRef.hasDefaultConstructor());
        assertTrue(privateRef.constructor(String.class).isPrivate());
        assertFalse(privateRef.constructor().isPublic());
        assertTrue(privateRef.constructor(String.class, int.class).isProtected());
        
        ConstructorPrivate priv = privateRef.constructor(String.class, int.class).newInstance("privname", 11);
        assertNotNull(priv);
        assertEquals("privname", priv.name);
        assertEquals(11, priv.age);
        
    }
    
    public static class ConstructorPrivate {
        private String name;
        private int age;
        
        private ConstructorPrivate() {}
        
        @SuppressWarnings("unused")
        private ConstructorPrivate(String name) {
            this.name = name;
        };

        protected ConstructorPrivate(String name, int age) {
            this.name = name;
            this.age = age;
        }

    }
    
    public static class ConstructorPublic {
        private String name;
        private int age;
        
        public ConstructorPublic() {}
        
        public ConstructorPublic(String name) {
            this.name = name;
        };

        public ConstructorPublic(String name, int age) {
            this.name = name;
            this.age = age;
        };
        
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
		};
        
		private static Long staticTest() {
			return 15L;
		}
    }
    
}
