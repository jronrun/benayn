package com.benayn;

import org.junit.Test;

import com.benayn.ustyle.Objects2.FacadeObject;

public class Me5Test extends Me4Test {

    @Test
    public void testTmp() {
        FacadeObject<User> fo = FacadeObject.wrap(User.class);
        fo.populate4Test();
        fo.info();
        log.info(fo.toString());
    }
    
}
