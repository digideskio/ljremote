package com.ljremote.json.services;

import com.ljremote.json.model.SessionConfig;

public class ServerServicesImpl implements ServerService{

	public String helloWord() {
		return "Hello world !";
	}


	public String hello() {
		return "hello";
	}


	public Void iWantMyException(Exception e) throws Exception {
		throw e;
	}


	public SessionConfig connect() {
		return new SessionConfig(0, 0);
	}


	public Boolean closeSession(int id) {
		return true;
	}

}
