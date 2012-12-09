package com.ljremote.json.services;

import com.ljremote.json.model.SessionConfig;

public class ServerServicesImpl implements ServerService{

	public String helloWord() {
		return "Hello world !";
	}


	@Override
	public void hello() {
	}


	@Override
	public void iWantMyException(Exception e) throws Exception {
		throw e;
	}


	@Override
	public SessionConfig connect() {
		return new SessionConfig(0, 0);
	}


	@Override
	public void closeSession(int id) {
	}

}
