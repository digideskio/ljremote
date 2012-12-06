package com.ljremote.json.services;

import java.util.List;
import java.util.Map;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.Static;

public interface StaticService {

	/**
	 * Toggle static state
	 * 
	 * @param id
	 *            static's id
	 * @return true if successful
	 */
	public boolean toggleStatic(int id);

	/**
	 * Set static state to enable
	 * 
	 * @param id
	 *            static's id
	 * @param enable
	 *            enable or not static
	 * @return true if successful
	 */
	public boolean enableStatic(int id, boolean enable)
			throws LJNotFoundException;

	/**
	 * Set static state for several statics
	 * 
	 * @param enableMap
	 *            map of couple id and state
	 * @return true if successful
	 */
	public boolean enableStatic(Map<Integer, Boolean> enableMap)
			throws LJNotFoundException;

	/**
	 * @param id
	 * @param level
	 * @return true if successful
	 */
	public boolean setStaticIntensity(int id, int level)
			throws LJNotFoundException;

	/**
	 * @param intensityMap
	 * @return true if successful
	 */
	public boolean setStaticIntensity(Map<Integer, Integer> intensityMap)
			throws LJNotFoundException;

	/**
	 * @param s
	 * @return true if successful
	 */
	public boolean updateStatic(Static s) throws LJNotFoundException;

	/**
	 * @param statics
	 * @return true if successful
	 */
	public boolean updateStatics(List<Static> statics)
			throws LJNotFoundException;

	/**
	 * Get LJ static list
	 * 
	 * @return static list
	 */
	public List<Static> getstatics() throws LJNotFoundException;
}
