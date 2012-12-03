package com.ljremote.json.services;

import java.util.List;
import java.util.Map;

import com.ljremote.json.model.Static;

public interface StaticService {

	/**
	 * Toggle static state
	 * @param id static's id
	 * @return true if successful
	 */
	public boolean toggleStatic(int id);
	
	/**
	 * Set static state to enable
	 * @param id static's id
	 * @param enable enable or not static
	 * @return true if successful
	 */
	public boolean enableStatic(int id, boolean enable);
	
	/**
	 * Set static state for several statics
	 * @param enableMap map of couple id and state
	 * @return true if successful
	 */
	public boolean enableStatic(Map<Integer, Boolean> enableMap);
	
	/**
	 * @param id
	 * @param level
	 * @return true if successful
	 */
	public boolean setStaticIntensity(int id, int level);
	
	/**
	 * @param intensityMap
	 * @return true if successful
	 */
	public boolean setStaticIntensity(Map<Integer, Integer> intensityMap);
	
	/**
	 * @param s
	 * @return true if successful
	 */
	public boolean updateStatic(Static s);
	
	/**
	 * @param statics
	 * @return true if successful
	 */
	public boolean updateStatics(List<Static> statics);
	
	/**
	 * Get LJ static list
	 * @return static list
	 */
	public List<Static> getstatics();
}
