package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.LJFunction;

public interface LJFunctionService {

	public List<LJFunction> getLJFunctions();
	
	public boolean executeFunction(int id);
	
	public boolean executeFunction(LJFunction function);
}
