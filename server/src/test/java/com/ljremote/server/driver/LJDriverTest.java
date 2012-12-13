package com.ljremote.server.driver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class LJDriverTest {
	

	private Process launchLJDummy() throws IOException{
		return Runtime.getRuntime().exec("LJ_Dummy.exe");
	}
	
	@Test
	public void testFindLJ() {
		try {
			Process proc = launchLJDummy();
			LJDriver driver = new LJDriver();
			assertTrue(driver.findLJ());
			proc.destroy();
		} catch (IOException e) {
			fail("Failed to launch LJDumy: " + e.getMessage());
		}
	}

	@Test
	public void testGetLJ() {
//		try {
//			Process proc = launchLJDummy();
//			LJDriver driver = new LJDriver();
//			assertTrue(driver.findLJ() && driver.getLJ() != null);
//			proc.destroy();
//		} catch (IOException e) {
//			fail("Failed to launch LJDumy: " + e.getMessage());
//		}
	}

	@Test
	public void testIsLJReady() {
//		LJDriver driver = new LJDriver();
//		driver.findLJ();
//		try {
//			assertTrue(driver.isLJReady());
//		} catch (LJNotFoundException e) {
//			fail(e.getMessage());
//		}
	}
	
	@Test
	public void testGetLJVersion(){
//		LJDriver driver = new LJDriver();
//		driver.findLJ();
//		String ver;
//		try {
//			ver = driver.getLJVersion();
//			assertNotNull(ver);
//		} catch (LJNotFoundException e) {
//			fail(e.getMessage());
//		}
	}

}
