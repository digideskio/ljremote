package com.ljremote.json.services;

import com.ljremote.json.exceptions.LJNotFoundException;

public interface DriverService {

	boolean isLJready() throws LJNotFoundException;

	String getLJversion() throws LJNotFoundException;

	void findLJ() throws LJNotFoundException;
}
