package com.ljremote.server;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.model.SessionConfig;
import com.ljremote.json.services.ServerService;

public class ClientManager implements ServerService {
	private static final Log log = LogFactory.getLog(ClientManager.class);
	private Map<Integer, ClientConfig> clientConfigs= new HashMap<Integer, ClientManager.ClientConfig>();;
	private ClientConfig lastAdded= null;
	private int lastId = -1;
	
	public class ClientConfig {
		int id;
		long timeOut;
		AtomicBoolean closeTrigged;

		public ClientConfig(int id, long timeOut) {
			this.id = id;
			this.timeOut = timeOut;
			closeTrigged = new AtomicBoolean(false);
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public long getTimeOut() {
			return timeOut;
		}

		public void setTimeOut(long timeOut) {
			this.timeOut = timeOut;
		}

		public AtomicBoolean getCloseTrigged() {
			return closeTrigged;
		}

		public void setCloseTrigged(AtomicBoolean closeTrigged) {
			this.closeTrigged = closeTrigged;
		}

	}

	public String helloWord() {
		log.trace("Hello world !");
		return "Hello world !";
	}

	public String hello() {
		log.trace("Hello");
		return "hello";
	}

	public Void iWantMyException(Exception e) throws Exception {
		throw e;
	}

	public SessionConfig connect() {
		log.trace("Client connected");
		return new SessionConfig(lastId, clientConfigs.get(lastId).timeOut);
	}

	public Boolean closeSession(int id) {
		log.debug("Client " + id + " asked for closing session");
		clientConfigs.get(id).closeTrigged.set(true);
		log.trace("Session closed : " + clientConfigs.get(id).closeTrigged.get());
		return clientConfigs.get(id).closeTrigged.get();
	}

	public synchronized ClientConfig getConfig(final SocketAddress socketAddr,
			long defaultTimeOut) {
		return addClient(geneId(socketAddr), defaultTimeOut);
	}

	private ClientConfig addClient(int id, long defaultTimeOut) {
		lastId= id;
		clientConfigs.put(id, new ClientConfig(id, defaultTimeOut));
		return clientConfigs.get(id);
	}

	public int geneId(final SocketAddress socketAddr) {
		return socketAddr.hashCode();
	}

	public void consumeClient(ClientConfig conf) {
		clientConfigs.remove(conf);
	}
}
