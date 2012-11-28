package com.ljremote.server.driver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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
		assertTrue(driver.isLJReady());
	}
	
	@Test
	public void testGetLJVersion(){
		LJDriver driver = new LJDriver();
		driver.findLJ();
		String ver = driver.getLJVersion();
		assertNotNull(ver);
	}

}
