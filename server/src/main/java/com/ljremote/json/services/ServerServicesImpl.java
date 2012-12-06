package com.ljremote.json.services;

import com.ljremote.server.LJServer;

public class ServerServicesImpl implements ServerService{

	public String helloWord() {
		return "Hello world !";
	}

	@Override
	public int connect() {
		return (int) LJServer.DEFAULT_CLIENT_TIMEOUT;
	}

	@Override
	public boolean hello() {
		return true;
	}

	@Override
	public void closeSession() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void iWantMyException(Exception e) throws Exception {
		throw e;
	}

}
