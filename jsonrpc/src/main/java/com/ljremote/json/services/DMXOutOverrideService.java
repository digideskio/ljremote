package com.ljremote.json.services;

import java.util.List;

import com.ljremote.json.model.DMXChannel;

public interface DMXOutOverrideService {

	public boolean overRideChannel(DMXChannel channel);
	
	public boolean overRideChannels(List<DMXChannel> channels);
}
