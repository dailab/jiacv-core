package de.dailab.jiactng;

import junit.framework.TestCase;

public abstract class JIACTestForJUnit3 extends TestCase {

	static {
		System.setProperty("log4j.configuration", "jiactng_log4j.properties");
	}

}
