package com.benayn;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Reflecter.MethodOptions;
import com.benayn.ustyle.Suppliers2;


public class Me5Test extends Me4Test {

    @Test
    public void testTmp() {
        FacadeObject<User> fo = Objects2.wrapObj(User.class);
        fo.populate4Test().info();
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
