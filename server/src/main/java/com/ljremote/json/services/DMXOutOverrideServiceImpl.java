package com.ljremote.json.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.DMXChannel;
import com.ljremote.server.driver.LJDriver;

public class DMXOutOverrideServiceImpl extends AbstractLJService implements
		DMXOutOverrideService {

	private static Log log = LogFactory.getLog(DMXOutOverrideServiceImpl.class);
	
	public DMXOutOverrideServiceImpl(LJDriver driver) {
		super(driver);
	}

	public boolean overRideChannel(DMXChannel channel) {
		return driver.dmxOutOverride().override(listChannelsToMap(Arrays.asList(new DMXChannel[]{channel}))) == 0;
	}

	public boolean overRideChannels(List<DMXChannel> channels) {
		return driver.dmxOutOverride().override(listChannelsToMap(channels)) == 0;
	}

	@Override
	public Log getLog() {
		return log;
	}

	public Map<Integer,Integer> listChannelsToMap(List<DMXChannel> channels) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(DMXChannel channel : channels) {
			if ( channel.isForce() ) {
				map.put(channel.getChannel(), channel.getValue());
			}
		}
		return map;
	}
}
