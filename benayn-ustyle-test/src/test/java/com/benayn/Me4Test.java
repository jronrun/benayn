package com.benayn;

import java.util.Set;

import org.junit.Test;

import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;


public class Me4Test extends Me3Test {
    
    public static class JsonTest {
//      Map<Short, Long> simpleMap;
      Set<Byte> simpleSet;
  }
    
    @Test
    public void testTmp() {
        String json = null, json2 = null;
        
//        com.benayn.berkeley.Person p = Randoms.get(com.benayn.berkeley.Person.class);
//        json = JsonW.toJson(p);
//        log.info(JsonW.fmtJson(json));
//        com.benayn.berkeley.Person p2 = JsonR.of(json).asObject(com.benayn.berkeley.Person.class);
//        assertTrue(Objects2.isEqual(p, p2));
//        com.benayn.berkeley.Person p3 = Reflecter.from(p).copyTo(com.benayn.berkeley.Person.class);
//        assertTrue(Objects2.isEqual(p, p3));
        
        JsonTest jt = Randoms.get(JsonTest.class);
        assertNotNull(jt);
        JsonTest jt3 = Reflecter.from(jt).copyTo(JsonTest.class);
        assertTrue(Objects2.isEqual(jt, jt3));
        json = JsonW.toJson(jt);
        log.info(JsonW.fmtJson(json));
        JsonTest jt2 = JsonR.of(json).asObject(JsonTest.class);
        assertNotNull(jt2);
        json2 = JsonW.toJson(jt2);
//        assertTrue(Objects2.isEqual(json, json2));
        
//        assertTrue(jt2.simpleSet instanceof Set);
//        for (Byte b : jt2.simpleSet) {
//            assertTrue(b instanceof Byte);
//        }
//        
//        assertTrue(Objects2.isEqual(jt, jt2));
    }
    
}
