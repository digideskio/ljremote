package com.ljremote.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.State;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.SessionConfig;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.ServerService;

public class LJClient {

	private static final Log log = LogFactory.getLog(LJClient.class);
	private static final int MIN_TIMEOUT = 300;
	private static final float DEFAULT_KEEP_ALIVE_FACTOR = 0.8f;
	private Socket socket;
	private InetSocketAddress sockAddr = null;
	private AtomicReference<MODE> state;
	private JsonRpcClient jrpc;
	private ServerService serverService;
	private DriverService driverService;
	private LinkedList<LJClient.OnModeChangeListener> onModeChangeListeners;
	private SessionConfig config = null;
	private ScheduledExecutorService keepAliveExecutor;
	private int keepAliveTimeout;
	private float keepAliveFactor;
	private boolean keepAliveStarted;
	
	public interface OnModeChangeListener {
		public void onModeChange(MODE newMode);
	}
	
	public static enum MODE {
		NONE, CONNECT ,DRIVE
	}
	
	private final Runnable alarm = new Runnable() {
		
		public void run() {
			doKeepAlive();
		}
	};
	
	/**
	 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses the
	 * given {@link JsonRpcClient}.
	 * 
	 * @param <T>
	 *            the proxy type
	 * @param classLoader
	 *            the {@link ClassLoader}
	 * @param proxyInterface
	 *            the interface to proxy
	 * @param client
	 *            the {@link JsonRpcClient}
	 * @param socket
	 *            the {@link Socket}
	 * @return the proxied interface
	 */
	public static <T> T createClientProxy(ClassLoader classLoader,
			Class<T> proxyInterface, final JsonRpcClient client, Socket socket)
			throws IOException {

		// create and return the proxy
		return createClientProxy(classLoader, proxyInterface, false, client,
				socket.getInputStream(), socket.getOutputStream());
	}

	/**
	 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses the
	 * given {@link JsonRpcClient}.
	 * 
	 * @param <T>
	 *            the proxy type
	 * @param classLoader
	 *            the {@link ClassLoader}
	 * @param proxyInterface
	 *            the interface to proxy
	 * @param client
	 *            the {@link JsonRpcClient}
	 * @param ips
	 *            the {@link InputStream}
	 * @param ops
	 *            the {@link OutputStream}
	 * @return the proxied interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createClientProxy(ClassLoader classLoader,
			Class<T> proxyInterface, final boolean useNamedParams,
			final JsonRpcClient client, final InputStream ips,
			final OutputStream ops) {

		// create and return the proxy
		return (T) Proxy.newProxyInstance(classLoader,
				new Class<?>[] { proxyInterface }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						Object arguments = ReflectionUtil.parseArguments(
								method, args, useNamedParams);
						return client.invokeAndReadResponse(method.getName(),
								arguments, method.getGenericReturnType(), ops,
								ips);
					}
				});
	}

	public LJClient() {
		this.socket = new Socket();
		jrpc = new JsonRpcClient();
		jrpc.setExceptionResolver(new LJNotFoundException().getExceptionResolver());
		state = new AtomicReference<LJClient.MODE>(MODE.NONE);
		onModeChangeListeners = new LinkedList<LJClient.OnModeChangeListener>();
		keepAliveFactor = DEFAULT_KEEP_ALIVE_FACTOR;
		keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
	}
	
	public LJClient(String host, int port) throws UnknownHostException {
		this();
		setHost(host, port);
	}
	
	public void setHost(String host, int port) throws UnknownHostException {
		this.sockAddr = new InetSocketAddress(host, port);
		log.debug("Host set to " + sockAddr);
	}

	public void connect() {
		if ( sockAddr  != null && state.get() == MODE.NONE ) {
			try {
				socket.connect(sockAddr);
				serverService = createClientProxy(LJClient.class.getClassLoader(),
								ServerService.class, jrpc, socket);
				config = serverService.connect();
				if ( config != null ) {
					changeMode(MODE.CONNECT);
				}
			} catch (IOException e) {
				log.error("Could not connect to server", e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void disconnect() {
		switch ( state.get() ) {
		case DRIVE:
		case CONNECT:
			if ( socket != null ) {
				try {
					serverService.closeSession(config.getId());
					changeMode(MODE.NONE);
				} finally {
					closeInternal();
				}
			}
		default:
			break;
		}
	}
	
	public void drive() {
		if ( state.get() == MODE.CONNECT ) {
			try {
				driverService = createClientProxy(LJClient.class.getClassLoader(),
						DriverService.class, jrpc, socket);
				driverService.findLJ();
				changeMode(MODE.DRIVE);
			} catch (LJNotFoundException e) {
				log.error("Could not find LJ",e);
			} catch (IOException e) {
				log.error("Could not find lj",e);
			} catch (Exception e) {
				closeInternal();
			}
		}
	}
	
	public void stopDrive() {
		if ( state.get() == MODE.DRIVE ) {
			changeMode(MODE.CONNECT);
		}
	}
	
	private void startKeepAlive(long realTime) {
//		System.out.println("startKeepAlive");
		if (state.get() != MODE.NONE && config.getTimeOut() > MIN_TIMEOUT) {
//			System.out.println("config timeout: " + config.getTimeOut());
			keepAliveTimeout = (int) (config.getTimeOut() * keepAliveFactor);
//			System.out.println("keepAlive timeout: " + keepAliveTimeout);
//			System.out.println("real timeout: " + realTime);
			long firstTime= realTime
					+ keepAliveTimeout/2;
//			System.out.println("first timeout: " + firstTime);
			keepAliveStarted = true;
			keepAliveExecutor.scheduleAtFixedRate(alarm, keepAliveTimeout/2-MIN_TIMEOUT, keepAliveTimeout, TimeUnit.MILLISECONDS);
			log.info(String.format(
					"Started Keep Alive Service, firstTime= %tT:%1$tL, periode= %d ms",
					firstTime + Calendar.getInstance().getTimeInMillis(), keepAliveTimeout));
		}
	}
	
	private void stopKeepAlive() {
		if (keepAliveStarted) {
			keepAliveExecutor.shutdownNow();
			keepAliveStarted = false;
			log.info("Stoped Keep Alive Service");
		}
	}
	
	public void startStopKeepAlive(){
		if (  keepAliveStarted ) {
			stopKeepAlive();
		} else {
			startKeepAlive(System.currentTimeMillis());
		}
	}
	
	public void doKeepAlive() {
		log.debug("coucou");
		switch (state.get()) {
		case CONNECT:
			try {
				serverService.hello();
				log.info("HELLO");
			} catch (Exception e) {
				log.info("An exception occured:", e);
				closeInternal();
			}
			break;
		case DRIVE:
			try {
				driverService.findLJ();
				log.info("FIND");
			} catch (LJNotFoundException e) {
				stopDrive();
			} catch (Exception e) {
				log.error("An exception occured:", e);
				closeInternal();
			}
		default:
			break;
		}
	}
	
	private void changeMode(MODE newMode) {
		MODE oldMode = state.getAndSet(newMode);
		log.info("Mode changed from " + oldMode + " to " + state);
		switch (state.get()) {
		case DRIVE:
			break;
		case NONE:
			stopKeepAlive();
			break;
		default:
			break;
		}
		fireOnModeChange();
	}

	private void fireOnModeChange() {
		for (OnModeChangeListener listener : onModeChangeListeners) {
			listener.onModeChange(state.get());
		}
	}
	
	public void registerOnModeChangeListener(OnModeChangeListener listener) {
		onModeChangeListeners.add(listener);
	}
	
	private void closeInternal() {
		try {
			socket.close();
		} catch (IOException e) {
			log.error("An exception occured:", e);
		}
		socket = new Socket();
		changeMode(MODE.NONE);
	}
	
	public MODE getState() {
		return state.get();
	}
	
	public ServerService getServerService() {
		return serverService;
	}
	
	public DriverService getDriverService() {
		return driverService;
	}
}
