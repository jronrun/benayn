package com.benayn;

import org.junit.Test;

import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.base.Domain;



public class Me4Test extends Me3Test {
    
    @Test
    public void testTmp() {
        FacadeObject<Domain> fo = FacadeObject.wrap(Domain.getDomain());
        fo.info();
    }
    
}
