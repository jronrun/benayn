# Benayn 
[![Build Status](https://api.travis-ci.org/jronrun/benayn.svg?branch=master)](https://travis-ci.org/jronrun/benayn)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.benayn/benayn/badge.svg)](http://search.maven.org/#browse%7C-1501833162)

Guava, Berkeley DB JE, Usage, Utilities

## Maven Dependencies
<a href="https://oss.sonatype.org/content/groups/public/com/benayn/">sonatype repository</a>
```xml
<dependency>
	<groupId>com.benayn</groupId>
	<artifactId>benayn-ustyle</artifactId>
	<version>${benayn.version}</version>
</dependency>
```

## Usage

```java

  public static class User {
      private String name;
      private int age;
      private Date birth;
      private Address address;
      private Map<Byte, List<Float>> testMap; 
      //get set ...
  }
  
  public static class Address {
      private Integer code;
      private String detail;
      private Lonlat lonlat;
      //get set ...
  }
  
  public static class Lonlat {
      private double lon;
      private double lat;
      //get set ...
  }
    
  @Test public void testUsage() {

        //set random value to user properties for test
        User user = Randoms.get(User.class);

        Map<Byte, List<Float>> testMapValue = user.getTestMap();
        //set testMap property null
        user.setTestMap(null);

        FacadeObject<User> userWrap = FacadeObject.wrap(user);

        //log as formatted JSON string, see below
        /*
         {
            "birth" : "2015-05-18 02:07:07",
            "testMap" : null,
            "address" : {
               "detail" : "fb232c0cca432c4b11c82f4cf4069405c6f4",
               "lonlat" : {
                  "lon" : 0.46031046103583306,
                  "lat" : 0.23163925851477474
               },
               "code" : -886743908
            },
            "age" : -397182609,
            "name" : "7f9c2734c2965c49fac9788c8dda8a2ace31"
         } 
         */
        userWrap.info();
        assertEquals(JsonW.of(user).asJson(), userWrap.getJson());

        //{"birth":1425988977384,"address":{"detail":"moon",
        //"lonlat":{"lon":0.12,"lat":0.10},"code":30},"age":18,"name":"jack"}
        String json = "{\"birth\":1425988977384,\"address\":{\"detail\":\"moon\",\"lonlat\":"
                + "{\"lon\":0.12,\"lat\":0.10},\"code\":30},\"age\":18,\"name\":\"jack\"}";

        Map<String, Object> jsonMap = JsonR.of(json).deepTierMap();
        //same as jsonMap.get("lon")
        assertEquals(0.12, jsonMap.get("address.lonlat.lon"));  

        //same as jsonMap.get("address.lonlat.lat")
        assertEquals(0.10, jsonMap.get("lat"));                 

        //populate with map
        User user2 = Reflecter.from(User.class).populate(jsonMap).get();
        assertFalse(Objects2.isEqual(user, user2));             //deeply compare

        //populate with JSON
        userWrap.populate(json);
        assertTrue(Objects2.isEqual(user, user2));
        assertTrue(user.getAddress().getLonlat().getLon() 
               == user2.getAddress().getLonlat().getLon());

        //modify user.address.lonlat.lat
        user.getAddress().getLonlat().setLat(0.2);
        assertFalse(Objects2.isEqual(user, user2));

        //type
        TypeDescrib testMapType = userWrap.getType("testMap");
        assertTrue(testMapType.isPair());
        assertEquals(Byte.class, testMapType.next().rawClazz());
        //nextPairType() same as next(1)
        assertEquals(List.class, testMapType.nextPairType().rawClazz());    
        assertEquals(Float.class, testMapType.next(1).next().rawClazz());
        
        assertEquals(double.class, userWrap.getType("address.lonlat.lon").rawClazz());
        assertEquals(Integer.class, userWrap.getType("address.code").rawClazz());
        
        assertEquals(user.getAddress().getLonlat().getLat(), 
                     userWrap.getValue("address.lonlat.lat"));
        assertEquals(user.getAddress().getDetail(), userWrap.getValue("address.detail"));
        assertEquals(user.getBirth().getTime(), userWrap.<Date>getValue("birth").getTime());

        //resolve type
        Object resolveObj = Resolves.get(userWrap.getField("testMap"), testMapValue);
        assertTrue(resolveObj instanceof Map);
        Map<?, ?> resolveMap = (Map<?, ?>) resolveObj;
        assertTrue(resolveMap.size() > 0);

        for (Object key : resolveMap.keySet()) {
            assertTrue(key instanceof Byte);
            assertTrue(resolveMap.get(key) instanceof List);

            List<?> list = (List<?>) resolveMap.get(key);
            for (Object listVal : list) {
                assertTrue(listVal instanceof Float);
            }
        }

        //some string test
        String str = "helloworld@test.com";

        assertEquals("hello*****@test.com", 
                Replacer.ctx(str).afters(5).befores("@").with('*'));
        assertEquals("*****world*********", 
                Replacer.of(str).after(5).negates().before("@").negates().with('*'));

        assertEquals("test", Finder.of(str).afters("@").befores(".").get());        
        assertEquals("world@test", Indexer.of(str).between(5, -4));
        assertEquals("test", Betner.of(str).between("@", ".").first());
    }
```

## More

- <a href="https://github.com/jronrun/benayn/blob/master/benayn-ustyle-test/src/test/java/com/benayn/Me3Test.java">benayn-ustyle usage</a>
- <a href="https://github.com/jronrun/benayn/blob/master/benayn-ustyle-test/src/test/java/com/benayn/berkeley/BerkeleyUsage.java">the benayn berkeley class usage</a>

