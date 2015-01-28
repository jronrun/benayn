package com.benayn.ustyle;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.benayn.ustyle.base.AbstractTest;
import com.google.common.collect.Lists;

public class SimplePurviewTest extends AbstractTest {

	@Test
	public void testSimplePurview() {
		for (int i = 0; i < 100; i++) {
            purT();
		}
    }

	private void purT() {
		int[] ps = new int[62];
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
			if (l.contains(i)) {
				assertTrue(p.has(i));
				continue;
			}
			assertFalse(p.has(i));
		}
	}
}
