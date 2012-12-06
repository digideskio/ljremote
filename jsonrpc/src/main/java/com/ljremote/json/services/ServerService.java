package com.ljremote.json.services;

public interface ServerService {

	String helloWord();
	
	/**
	 * Connect to server and open session
	 * @return client timeout, -1 if connection refused, 0 for no timeout
	 */
	int connect();
	
	/**
	 * Send hello to keep the session alive
	 * @return true if session still alive
	 */
	boolean hello();
	
	/**
	 * Close session
	 */
	void closeSession();
	
	void iWantMyException(Exception e) throws Exception;
}
