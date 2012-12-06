package com.ljremote.server.driver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ljremote.json.exceptions.LJNotFoundException;

public class LJDriverTest {
	

	@Test
	public void testFindLJ() {
		LJDriver driver = new LJDriver();
		assertTrue(driver.findLJ());
	}

	@Test
	public void testGetLJ() {
		LJDriver driver = new LJDriver();
		assertTrue(driver.findLJ() && driver.getLJ() != null);
	}

	@Test
	public void testIsLJReady() {
		LJDriver driver = new LJDriver();
		driver.findLJ();
		try {
			assertTrue(driver.isLJReady());
		} catch (LJNotFoundException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetLJVersion(){
		LJDriver driver = new LJDriver();
		driver.findLJ();
		String ver;
		try {
			ver = driver.getLJVersion();
			assertNotNull(ver);
		} catch (LJNotFoundException e) {
			fail(e.getMessage());
		}
	}

}
