package com.benayn.ustyle;

import org.junit.Test;

import com.benayn.ustyle.base.AbstractTest;

public class Scale62Test extends AbstractTest {
	
	 @Test
     public void testSixtytwo() {
             for (int i = 0; i < 10; i++) {
                     sixty2();
             }
             Scale62.clearCache();
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

}
