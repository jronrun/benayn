package com.benayn;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Suppliers2;
import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.Reflecter;

public class Me5Test extends Me4Test {

    @Test
    public void testTmp() {
        FacadeObject<User> fo = Objects2.wrapObj(User.class);
        fo.populate4Test().info();
       
        
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
        };
        
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
    }
    
    @Test
    public void testReflectConstructor() {
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
    
}
