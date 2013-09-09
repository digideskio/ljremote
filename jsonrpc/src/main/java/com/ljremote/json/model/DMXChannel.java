package com.ljremote.json.model;

public class DMXChannel {

	private int channel;
	private int value;
	private boolean force;
	
	public DMXChannel() {
		setChannel(-1);
		setValue(0);
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

}
