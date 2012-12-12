package com.ljremote.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.ljremote.server.ClientManager.ClientConfig;
import com.ljremote.server.LJServer.ClientConnectionListener.ConnectionEvent;
import com.ljremote.server.LJServer.ClientConnectionListener.EventType;
import com.ljremote.server.LJServer.OnServerStatusChangeListener.STATUS;

/**
 * A multi-threaded streaming server that uses JSON-RPC over sockets.
 * 
 */
public class LJServer {

	private static final Log log = LogFactory.getLog(LJServer.class);

	private static final int SERVER_SOCKET_SO_TIMEOUT = 5000;

	public static final long DEFAULT_CLIENT_TIMEOUT = 15;

	public static final TimeUnit DEFAULT_CLIENT_TIMEOUT_UNIT = TimeUnit.SECONDS;

	private ServerSocket serverSocket;
	private JsonRpcServer jsonRpcServer;
	private int maxClientErrors = 5;

	private AtomicBoolean isStarted = new AtomicBoolean(false);
	private AtomicBoolean keepRunning = new AtomicBoolean(false);

	private ExecutorService serverExecutor;

	private int maxClients;

	private OnServerStatusChangeListener onServerStatusChangeListener;

	private List<ClientConnectionListener> clientConnectionListeners;

	private ClientManager clientManager;

	/**
	 * Creates a {@code StreamServer} with the given max number of threads using
	 * the given {@link ServerSocket} to listen for client connections.
	 * 
	 * @param jsonRpcServer
	 *            the {@link JsonRpcServer} that will handle requests
	 * @param maxThreads
	 *            the mac number of threads the server will spawn
	 * @param serverSocket
	 *            the {@link ServerSocket} used for accepting client connections
	 */
	public LJServer(ClientManager manager, JsonRpcServer jsonRpcServer, int maxThreads,
			ServerSocket serverSocket) {

		// initialize values
		this.clientManager= manager;
		this.jsonRpcServer = jsonRpcServer;
		this.serverSocket = serverSocket;

		this.maxClients = maxThreads;
		clientConnectionListeners = new LinkedList<LJServer.ClientConnectionListener>();

		// create the executor server
		serverExecutor = Executors.newSingleThreadExecutor();
		// we can't allow the server to re-throw exceptions
		jsonRpcServer.setRethrowExceptions(false);
	}

	/**
	 * Creates a {@code LJServer} with the given max number of threads. A
	 * {@link ServerSocket} is created using the default
	 * {@link ServerSocketFactory} that listes on the given {@code port} and
	 * {@link InetAddress}.
	 * 
	 * @param jsonRpcServer
	 *            the {@link JsonRpcServer} that will handle requests
	 * @param maxThreads
	 *            the mac number of threads the server will spawn
	 * @param port
	 *            the port to listen on
	 * @param backlog
	 *            the {@link ServerSocket} backlog
	 * @param bindAddress
	 *            the address to listen on
	 * @throws IOException
	 *             on error
	 */
	public LJServer(ClientManager manager,JsonRpcServer jsonRpcServer, int maxThreads, int port,
			int backlog, InetAddress bindAddress) throws IOException {
		this(manager,jsonRpcServer, maxThreads, ServerSocketFactory.getDefault()
				.createServerSocket(port, backlog, bindAddress));
	}
	
	/**
	 * Creates a {@code StreamServer} with the given max number of threads using
	 * the given {@link ServerSocket} to listen for client connections.
	 * 
	 * @param jsonRpcServer
	 *            the {@link JsonRpcServer} that will handle requests
	 * @param maxThreads
	 *            the mac number of threads the server will spawn
	 * @param serverSocket
	 *            the {@link ServerSocket} used for accepting client connections
	 */
	public LJServer(JsonRpcServer jsonRpcServer, int maxThreads,
			ServerSocket serverSocket) {
		this(null, jsonRpcServer, maxThreads, serverSocket);
	}
	
	/**
	 * Creates a {@code LJServer} with the given max number of threads. A
	 * {@link ServerSocket} is created using the default
	 * {@link ServerSocketFactory} that listes on the given {@code port} and
	 * {@link InetAddress}.
	 * 
	 * @param jsonRpcServer
	 *            the {@link JsonRpcServer} that will handle requests
	 * @param maxThreads
	 *            the mac number of threads the server will spawn
	 * @param port
	 *            the port to listen on
	 * @param backlog
	 *            the {@link ServerSocket} backlog
	 * @param bindAddress
	 *            the address to listen on
	 * @throws IOException
	 *             on error
	 */
	public LJServer(JsonRpcServer jsonRpcServer, int maxThreads, int port,
			int backlog, InetAddress bindAddress) throws IOException {
		this(jsonRpcServer, maxThreads, ServerSocketFactory.getDefault()
				.createServerSocket(port, backlog, bindAddress));
	}

	/**
	 * Starts the server.
	 */
	public void start() {

		// make sure we're not already started
		if (!isStarted.compareAndSet(false, true)) {
			throw new IllegalStateException(
					"The StreamServer is already started");
		}

		// we're starting
		log.info("StreamServer starting " + serverSocket.getInetAddress() + ":"
				+ serverSocket.getLocalPort());
		log.info(String.format("Default client time out: %d %s (%d ms)",
				DEFAULT_CLIENT_TIMEOUT, DEFAULT_CLIENT_TIMEOUT_UNIT,
				getDefaultClientTimeout()));
		// start the server
		keepRunning.set(true);
		serverExecutor.submit(new MultiThreadedServer(maxClients));
		fireOnServerStatusChange(STATUS.RUNNING);
	}

	/**
	 * Stops the server thread.
	 * 
	 * @throws InterruptedException
	 *             if a graceful shutdown didn't happen
	 */
	public void stop() throws InterruptedException {

		// make sure we're started
		if (!isStarted.get()) {
			throw new IllegalStateException("The StreamServer is not started");
		}

		// stop the server
		keepRunning.set(false);

		// wait for the clients to stop
		serverExecutor.shutdownNow();

		try {
			serverSocket.close();
		} catch (IOException e) { /* no-op */
		}

		try {

			// wait for it to finish
			if (!serverExecutor.isTerminated()) {
				serverExecutor.awaitTermination(
						2000 + SERVER_SOCKET_SO_TIMEOUT, TimeUnit.MILLISECONDS);
			}

			// set the flags
			isStarted.set(false);
			keepRunning.set(false);
			fireOnServerStatusChange(STATUS.STOPPED);
		} catch (InterruptedException e) {
			log.error("InterruptedException while waiting for termination", e);
			throw e;
		}
	}

	/**
	 * @return the maxClientErrors
	 */
	public int getMaxClientErrors() {
		return maxClientErrors;
	}

	/**
	 * @param maxClientErrors
	 *            the maxClientErrors to set
	 */
	public void setMaxClientErrors(int maxClientErrors) {
		this.maxClientErrors = maxClientErrors;
	}

	/**
	 * @return the isStarted
	 */
	public boolean isStarted() {
		return isStarted.get();
	}

	public interface OnServerStatusChangeListener {
		public enum STATUS {
			RUNNING, STOPPED;
		}

		public void onServerStatusChange(STATUS status);
	}

	public void setOnServerStatusChange(OnServerStatusChangeListener listener) {
		onServerStatusChangeListener = listener;
	}

	private void fireOnServerStatusChange(STATUS status) {
		if (onServerStatusChangeListener != null) {
			onServerStatusChangeListener.onServerStatusChange(status);
		}
	}

	public interface ClientConnectionListener {
		public enum EventType {
			CONNECT, DISCONNECT
		}

		public class ConnectionEvent {
			private EventType type;
			private String clientType;
			private String addr;
			private int port;
			private int timeOut;
			private Object data;

			public ConnectionEvent(EventType type, String clientType,
					String addr, int port, int timeOut) {
				super();
				this.setType(type);
				this.clientType = clientType;
				this.addr = addr;
				this.port = port;
				this.timeOut = timeOut;
			}

			public EventType getType() {
				return type;
			}

			public void setType(EventType type) {
				this.type = type;
			}

			public String getClientType() {
				return clientType;
			}

			public void setClientType(String clientType) {
				this.clientType = clientType;
			}

			public String getAddr() {
				return addr;
			}

			public void setAddr(String addr) {
				this.addr = addr;
			}

			public int getPort() {
				return port;
			}

			public void setPort(int port) {
				this.port = port;
			}

			public int getTimeOut() {
				return timeOut;
			}

			public void setTimeOut(int timeOut) {
				this.timeOut = timeOut;
			}

			public Object getData() {
				return data;
			}

			public void setData(Object data) {
				this.data = data;
			}

		}

		public void onClientConnectionEvent(ConnectionEvent event);
	}

	public void registerClientConnectionListenrer(
			ClientConnectionListener listener) {
		clientConnectionListeners.add(listener);
	}

	public void fireOnClientConnectionEvent(ConnectionEvent event) {
		for (ClientConnectionListener listener : clientConnectionListeners) {
			listener.onClientConnectionEvent(event);
		}
	}

	private class MultiThreadedServer implements Runnable {
		private ThreadPoolExecutor clientHandleExecutors;

		public MultiThreadedServer(int maxClients) {
			clientHandleExecutors = new ThreadPoolExecutor(maxClients,
					maxClients, 0, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());
			clientHandleExecutors
					.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		}

		@Override
		public void run() {
			ServerSocket serverSocket = LJServer.this.serverSocket;

			while (LJServer.this.keepRunning.get()) {
				try {
					serverSocket.setSoTimeout(SERVER_SOCKET_SO_TIMEOUT);

					clientHandleExecutors.submit(new ClientHandle(serverSocket
							.accept()));
				} catch (SocketTimeoutException e) {
					// this is expected because of so_timeout

				} catch (SSLException ssle) {
					log.error(
							"SSLException while listening for clients, terminating",
							ssle);
					break;

				} catch (IOException ioe) {
					// this could be because the ServerSocket was closed
					if (SocketException.class.isInstance(ioe)
							&& !keepRunning.get()) {
						break;
					}
					log.error("Exception while listening for clients", ioe);
				}

			}
		}

	}

	private class ClientHandle implements Runnable {

		private Socket socket;
		private InputStreamChecker inputChecker;
		private ExecutorService timerExecutor;
		private ConnectionEvent event;
		private ClientConfig conf;

		public ClientHandle(Socket socket) {
			// log the connection
			log.info("Connection from " + socket.getInetAddress() + ":"
					+ socket.getPort());
			this.socket = socket;
			timerExecutor = Executors.newSingleThreadExecutor();
			event = new ConnectionEvent(EventType.CONNECT, null, socket
					.getInetAddress().toString(), socket.getPort(),
					(int) getDefaultClientTimeout());
			conf= clientManager == null ? null : clientManager.getConfig(socket.getRemoteSocketAddress(), getDefaultClientTimeout());
			log.info("chaud cacao");
			try {
				inputChecker = new InputStreamChecker(socket.getInputStream());
			} catch (IOException e) {
				log.error("Client socket failed", e);
			}
		}

		@Override
		public void run() {
			try {
				fireOnClientConnectionEvent(event);
				// keep handling requests
				int errors = 0;
				while ((conf == null || !conf.closeTrigged.get()) && LJServer.this.keepRunning.get() && socket.isConnected()) {
					Future<Integer> f = timerExecutor.submit(inputChecker);
					if (f.get(DEFAULT_CLIENT_TIMEOUT,
							DEFAULT_CLIENT_TIMEOUT_UNIT) > 0) {
						try {
							log.info("coucou");
							jsonRpcServer.handle(socket.getInputStream(),
									socket.getOutputStream());
						} catch (Throwable t) {
							errors++;
							if (errors < maxClientErrors) {
								log.error("Exception while handling request", t);
							} else {
								log.error(
										"Closing client connection due to repeated errors",
										t);
								break;
							}
						}
					}
					;
				}
			} catch (InterruptedException e) {
				log.error(e);
			} catch (ExecutionException e) {
				log.error(e);
			} catch (TimeoutException e) {
				log.warn(String.format("Timeout expired on %s", socket));
			} finally {
				// clean up
				try {
					socket.close();
					log.info(String.format("Client %s:%d disconnected",
							socket.getInetAddress(), socket.getPort()));
					event.setType(EventType.DISCONNECT);
					fireOnClientConnectionEvent(event);
				} catch (IOException e) { /* no-op */
				}
			}
		}
	}

	private class InputStreamChecker implements Callable<Integer> {

		InputStream in;

		public InputStreamChecker(InputStream in) {
			this.in = in;
		}

		@Override
		public Integer call() throws Exception {
			log.trace(String.format("Checking input %s", in.hashCode()));
			int res;
			while ((res = in.available()) <= 0) {
				if (in.available() > 0) {
				}
			}
			return res;
		}
	}

	public static long getDefaultClientTimeout() {
		return DEFAULT_CLIENT_TIMEOUT_UNIT.toMillis(DEFAULT_CLIENT_TIMEOUT);
	}
}
