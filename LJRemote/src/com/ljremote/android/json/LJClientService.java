package com.ljremote.android.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.SessionConfig;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.ServerService;

public class LJClientService extends Service {

	private static final String TAG = "LJClientService";
	private static final int MIN_TIMEOUT = 300;
	private static final float DEFAULT_KEEP_ALIVE_FACTOR = 0.8f;
	private final IBinder binder = new LocalBinder();
	private Socket socket;
	private InetSocketAddress sockAddr = null;
	private AtomicReference<MODE> state;
	private JsonRpcClient jrpc;
	private ServerService serverService;
	private DriverService driverService;
	private List<OnModeChangeListener> onModeChangeListeners;
//	private AlarmManager am;
//	private PendingIntent keepAliveSender;
	private ScheduledExecutorService keepAliveExecutor;
	private int keepAliveTimeout;
	private float keepAliveFactor;
	private SessionConfig config= null;

	public interface OnModeChangeListener {
		public void onModeChange(MODE newMode);
	}

	static class ProxyUtil {
		/**
		 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses
		 * the given {@link JsonRpcClient}.
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
				Class<T> proxyInterface, final JsonRpcClient client,
				Socket socket) throws IOException {

			// create and return the proxy
			return createClientProxy(classLoader, proxyInterface, false,
					client, socket.getInputStream(), socket.getOutputStream());
		}

		/**
		 * Creates a {@link Proxy} of the given {@link proxyInterface} that uses
		 * the given {@link JsonRpcClient}.
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
							return client.invokeAndReadResponse(
									method.getName(), arguments,
									method.getGenericReturnType(), ops, ips);
						}
					});
		}
	}

	public static enum MODE {
		NONE, UNBOUND, BOUND, DRIVE
	}

	public class LocalBinder extends Binder {
		LJClientService getService() {
			return LJClientService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
//		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		jrpc = new JsonRpcClient();
		jrpc.setExceptionResolver(new LJNotFoundException()
				.getExceptionResolver());
		socket = new Socket();
		state = new AtomicReference<LJClientService.MODE>(MODE.NONE);
		onModeChangeListeners = new LinkedList<LJClientService.OnModeChangeListener>();
//		registerReceiver(new KeepAliveAlarm(), new IntentFilter());
//		Intent intent = new Intent(this, KeepAliveAlarm.class);
//		keepAliveSender = PendingIntent.getBroadcast(this, 0, intent,
//				PendingIntent.FLAG_CANCEL_CURRENT);
		keepAliveFactor = DEFAULT_KEEP_ALIVE_FACTOR;
		keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
		Log.d(TAG, "KeepAliveSender created");
	}

	@Override
	public IBinder onBind(Intent intent) {
		state.set(MODE.UNBOUND);
		Log.i(TAG, "Service bound");
		return binder;
	}

	public MODE getCurrentMode() {
		return state.get();
	}

	public void setHost(String host, int port) throws UnknownHostException {
		this.sockAddr = new InetSocketAddress(host, port);
		Log.d(TAG, "Host set to " + sockAddr);
	}

	private AsyncTask<Void, Void, SessionConfig> ConnectTask = new AsyncTask<Void, Void, SessionConfig>() {

		protected SessionConfig doInBackground(Void... params) {
			if (sockAddr != null && state.get() == MODE.UNBOUND) {
				try {
					// Log.v(TAG, "coucou");
					socket.connect(sockAddr);
					serverService = ProxyUtil
							.createClientProxy(getClassLoader(),
									ServerService.class, jrpc, socket);
					return serverService.connect();
				} catch (IOException e) {
					Log.e(TAG, "Could not connect to server", e);
				}
			}
			return null;
		}

		protected void onPostExecute(SessionConfig result) {
			if (result != null && state.get() == MODE.UNBOUND) {
				config = result;
				Log.i(TAG, "Connected to server " + sockAddr
						+ ", Server timeout: " + config.getTimeOut() + "ms");
				changeMode(MODE.BOUND);
			}
		}

	};

	private AsyncTask<Void, Void, Void> DisconnectTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			switch (state.get()) {
			case BOUND:
			case DRIVE:
				if (socket != null && socket.isConnected()) {
					serverService.closeSession(config.getId());
				}
				break;
			default:
				break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			switch (state.get()) {
			case BOUND:
			case DRIVE:
				changeMode(MODE.UNBOUND);
			default:
				break;
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private AsyncTask<Void, Void, Boolean> DriveTask = new AsyncTask<Void, Void, Boolean>() {

		@Override
		protected Boolean doInBackground(Void... params) {
			Socket s = LJClientService.this.socket;
			Log.d(TAG,
					String.format(
							"Socket bound=%b, closed=%b, connected=%b, inputShut=%b, outputShot=%b",
							s.isBound(), s.isClosed(), s.isConnected(),
							s.isInputShutdown(), s.isOutputShutdown()));

			if (state.get() == MODE.BOUND) {
				try {
					driverService = ProxyUtil
							.createClientProxy(getClassLoader(),
									DriverService.class, jrpc, socket);
					driverService.findLJ();
					return true;
				} catch (LJNotFoundException e) {
					Log.e(TAG, "Could not find LJ",e);
				} catch (IOException e) {
					Log.e(TAG, "Could not find lj",e);
				} catch (Exception e) {
					Log.e(TAG, "An exception occured:", e);
					closeInternal();
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG, "hihi " + result);
			if (state.get() == MODE.BOUND && result) {
				changeMode(MODE.DRIVE);
			}
		}
	};

	private boolean keepAliveStarted;

	public void connect() {
		ConnectTask.execute();
	}

	public void disconnect() {
		DisconnectTask.execute();
	}

	public void drive() {
		DriveTask.execute();
	}

	public void stopDrive() {
		if (state.get() == MODE.DRIVE) {
			changeMode(MODE.BOUND);
		}
	}

	private void closeInternal() {
		try {
			socket.close();
			socket = new Socket();
			changeMode(MODE.UNBOUND);
		} catch (IOException e) {
			Log.e(TAG, "An exception occured:", e);
		}
	}

	private void startKeepAlive(long realTime) {
		if (config.getTimeOut() > MIN_TIMEOUT) {
			keepAliveTimeout = (int) (config.getTimeOut() * keepAliveFactor);
			long firstTime= realTime
					+ keepAliveTimeout/2;
//			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, keepAliveTimeout, keepAliveSender);
			final Runnable alarm =new Runnable() {
				
				@Override
				public void run() {
					doKeepAlive();
				}
			};
			Log.v(TAG, "before");
			keepAliveExecutor.scheduleAtFixedRate(alarm, keepAliveTimeout-MIN_TIMEOUT, keepAliveTimeout, TimeUnit.MILLISECONDS);
			keepAliveStarted = true;
			Log.i(TAG,
					String.format(
							"Started Keep Alive Service, firstTime= %tT:%1$tL, periode= %d ms",
							firstTime + Calendar.getInstance().getTimeInMillis() - SystemClock.elapsedRealtime(), keepAliveTimeout));
		}
	}

	private void stopKeepAlive() {
		if (keepAliveStarted) {
			keepAliveExecutor.shutdownNow();
//			am.cancel(keepAliveSender);
			Log.i(TAG, "Stoped Keep Alive Service");
		}
	}

	private void changeMode(MODE newMode) {
		MODE oldMode = state.getAndSet(newMode);
		Log.i(TAG, "Mode changed from " + oldMode + " to " + state);
		switch (state.get()) {
		case BOUND:
		case DRIVE:
			if (!keepAliveStarted) {
				startKeepAlive(SystemClock.elapsedRealtime());
			}
			break;
		default:
			stopKeepAlive();
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

	public void doKeepAlive() {
		Log.d("KeepAliveAlarm", "coucou");
		switch (state.get()) {
		case BOUND:
			try {
				serverService.hello();
				Log.v(TAG, "HELLO");
			} catch (Exception e) {
				Log.e(TAG, "An exception occured:", e);
			}
			break;
		case DRIVE:
			try {
				driverService.findLJ();
			} catch (LJNotFoundException e) {
				stopDrive();
			} catch (Exception e) {
				Log.e(TAG, "An exception occured:", e);
				closeInternal();
			}
			Log.v(TAG, "FIND");
		default:
			break;
		}
	}

}
