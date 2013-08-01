package com.ljremote.json.services;

import com.ljremote.json.model.SessionConfig;

public interface ServerService {

	String helloWord();
	
	/**
	 * Connect to server and open session
	 * @return client timeout, -1 if connection refused, 0 for no timeout
	 */
	SessionConfig connect();
	
	/**
	 * Send hello to keep the session alive
	 * @return true if session still alive
	 */
	String hello();
	
	/**
	 * Close session
	 */
	Boolean closeSession(int id);
	
	Void iWantMyException(Exception e) throws Exception;
}
