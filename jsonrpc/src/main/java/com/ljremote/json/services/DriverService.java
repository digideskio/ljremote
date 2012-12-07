package com.ljremote.json.services;

import com.ljremote.json.exceptions.LJNotFoundException;

public interface DriverService {

	boolean isLJready();
	String getLJversion();
	void findLJ() throws LJNotFoundException;
}
