package com.ljremote.json.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.LJFunction;
import com.ljremote.server.driver.LJDriver;

public class LJFunctionServiceImpl extends AbstractLJService implements LJFunctionService {

	private static final int GROUP_LEVEL_ONE_ID = 0x30000;
	private static final int GROUP_LEVEL_TWO_ID = 0x10000;
	private static Log log = LogFactory.getLog(LJFunctionServiceImpl.class);
	
	public LJFunctionServiceImpl(LJDriver driver) {
		super(driver);
	}

	@Override
	public Log getLog() {
		return log;
	}

	public List<LJFunction> getLJFunctions() {
		List<LJFunction> list = new ArrayList<LJFunction>();
		List<LJFunction> functionsList = driver.ljFunctions().getLJFunctionsList();
		LJFunction groupLevelOne = new LJFunction();
		LJFunction groupLevelTwo = new LJFunction();
		int currentLevel = 1;
		for( LJFunction function : functionsList ) {
			switch (function.getId()) {
			case GROUP_LEVEL_ONE_ID:
				groupLevelOne = new LJFunction(GROUP_LEVEL_ONE_ID, function.getName());
				currentLevel = 1;
				list.add(groupLevelOne);
				break;
			case GROUP_LEVEL_TWO_ID:
				groupLevelTwo = new LJFunction(GROUP_LEVEL_TWO_ID, function.getName());
				currentLevel = 2;
				groupLevelOne.addFunction(groupLevelTwo);
				break;
			default:
				switch (currentLevel) {
				case 2:
					groupLevelTwo.addFunction(function);
					break;
				case 1:
				default:
					groupLevelOne.addFunction(function);
					break;
				}
				break;
			}
		}
		return list;
	}

	public boolean executeFunction(int id) {
		return driver.ljFunctions().executeFunction(id);
	}

	public boolean executeFunction(LJFunction function) {
		return driver.ljFunctions().executeFunction(function.getId());
	}

}
