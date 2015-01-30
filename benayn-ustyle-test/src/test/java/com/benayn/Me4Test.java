package com.benayn;

import java.util.Set;

import org.junit.Test;

import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Resolves;
import com.benayn.ustyle.TypeRefer;


public class Me4Test extends Me3Test {
    
    public static class ResolveTest {
        boolean booleanP;
        Boolean booleanW;
        
        byte byte1;
        short[] shortParr;
        Short[] shortWarr;
        String[] stringArr;
        
        Set<Byte> byteSet;
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
        
//        assertTrue(Resolves.get(ref.field("byteSet"), 1) instanceof Set[]);
    }
    
    public static class JsonTest {
//      Map<Short, Long> simpleMap;
      Set<Byte> simpleSet;
  }
    
    @Test
    public void testTmp() {
        String json = null;
        
        com.benayn.berkeley.Person p = Randoms.get(com.benayn.berkeley.Person.class);
        json = JsonW.toJson(p);
        log.info(JsonW.fmtJson(json));
        com.benayn.berkeley.Person p2 = JsonR.of(json).asObject(com.benayn.berkeley.Person.class);
        assertTrue(Objects2.isEqual(p, p2));
        com.benayn.berkeley.Person p3 = Reflecter.from(p).copyTo(com.benayn.berkeley.Person.class);
        assertTrue(Objects2.isEqual(p, p3));
        
        JsonTest jt = Randoms.get(JsonTest.class);
        assertNotNull(jt);
        JsonTest jt3 = Reflecter.from(jt).copyTo(JsonTest.class);
        assertTrue(Objects2.isEqual(jt, jt3));
        json = JsonW.toJson(jt);
        log.info(JsonW.fmtJson(json));
        JsonTest jt2 = JsonR.of(json).asObject(JsonTest.class);
        assertNotNull(jt2);
        
//        assertTrue(jt2.simpleSet instanceof Set);
//        for (Byte b : jt2.simpleSet) {
//            assertTrue(b instanceof Byte);
//        }
//        
//        assertTrue(Objects2.isEqual(jt, jt2));
    }
    
}
