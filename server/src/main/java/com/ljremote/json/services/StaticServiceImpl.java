package com.ljremote.json.services;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.Static;
import com.ljremote.server.driver.LJDriver;

public class StaticServiceImpl extends AbstractLJService implements
		StaticService {
	
	private final static Log log= LogFactory.getLog(StaticServiceImpl.class);

	public StaticServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean toggleStatic(int id) {
		return false;
	}

	public boolean enableStatic(int id, boolean enable) {
		try {
			driver.statics().enableStatic(id, enable);
		} catch (LJNotFoundException e) {
			log.error(e);
		}
		return true;
	}

	public boolean enableStatic(Map<Integer, Boolean> enableMap) {
		boolean allgood= true;
		for(Integer id: enableMap.keySet()){
			try {
				driver.statics().enableStatic(id, enableMap.get(id));
			} catch (LJNotFoundException e) {
				log.error(e);
				allgood= false;
			}
		}
		return allgood;
	}

	public boolean setStaticIntensity(int id, int level) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setStaticIntensity(Map<Integer, Integer> intensityMap) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateStatic(Static s) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateStatics(List<Static> statics) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Static> getstatics() {
		// TODO Auto-generated method stub
		return null;
	}

}
