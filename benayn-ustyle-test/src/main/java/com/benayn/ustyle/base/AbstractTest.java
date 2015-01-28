package com.benayn.ustyle.base;

import org.junit.Assert;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;

public class AbstractTest extends Assert {
	
	/**
	 * 
	 */
	protected final Log log = Loggers.from(getClass());
	
}
