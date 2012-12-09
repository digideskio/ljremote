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
	void hello();
	
	/**
	 * Close session
	 */
	void closeSession(int id);
	
	void iWantMyException(Exception e) throws Exception;
}
