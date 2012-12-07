package com.ljremote.android.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.ServerService;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class LJClientService extends Service {

	private static final String TAG = "LJClientService";
	private final IBinder binder = new LocalBinder();
	private Socket socket;
	private InetSocketAddress sockAddr = null;
	private MODE state;
	private JsonRpcClient jrpc;
	private ServerService serverService;
	private DriverService driverService;
	private int timeOut = 0;
	private OnModeChangeListener onModeChangeListener = null;

	interface OnModeChangeListener {
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

	public enum MODE {
		UNBOUND, BOUND, DRIVE
	}

	public class LocalBinder extends Binder {
		LJClientService getService() {
			return LJClientService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		state = MODE.UNBOUND;
		jrpc = new JsonRpcClient();
		jrpc.setExceptionResolver(new LJNotFoundException().getExceptionResolver());
		socket = new Socket();
		Log.i(TAG, "Service bound");
		return binder;
	}

	public MODE getCurrentMode() {
		return state;
	}

	public void setHost(String host, int port) throws UnknownHostException {
		this.sockAddr = new InetSocketAddress(host, port);
		Log.d(TAG, "Host set to " + sockAddr);
	}

	private AsyncTask<Void, Void, Integer> ConnectTask = new AsyncTask<Void, Void, Integer>() {

		protected Integer doInBackground(Void... params) {
			if (sockAddr != null && state == MODE.UNBOUND) {
				try {
					Log.v(TAG, "coucou");
					socket.connect(sockAddr);
					serverService = ProxyUtil
							.createClientProxy(getClassLoader(),
									ServerService.class, jrpc, socket);
					return serverService.connect();
				} catch (IOException e) {
					Log.e(TAG, "Could not connect to server", e);
				}
			}
			return -1;
		}

		protected void onPostExecute(Integer result) {
			Log.v(TAG, result.toString());
			if (result >= 0 && state == MODE.UNBOUND) {
				timeOut = result;
				Log.i(TAG, "Connected to server " + sockAddr);
				changeMode(MODE.BOUND);
			}
		}

	};

	private AsyncTask<Void, Void, Void> DisconnectTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			switch (state) {
			case BOUND:
			case DRIVE:
				if (socket != null && socket.isConnected()) {
					serverService.closeSession();
				}
				break;
			default:
				break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			switch (state) {
			case BOUND:
			case DRIVE:
				changeMode(MODE.UNBOUND);
			default:
				break;
			}
		}

	};

	private AsyncTask<Void, Void, Boolean> DriveTask = new AsyncTask<Void, Void, Boolean>() {

		@Override
		protected Boolean doInBackground(Void... params) {
			if (state == MODE.BOUND) {
				try {
					driverService = ProxyUtil
							.createClientProxy(getClassLoader(),
									DriverService.class, jrpc, socket);
					driverService.findLJ();
					return true;
				} catch (LJNotFoundException e) {
					Log.e(TAG, "Could not find LJ", e);
				} catch (IOException e) {
					Log.e(TAG, "Could not find lj", e);
				}

			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (state == MODE.BOUND && result) {
				changeMode(MODE.DRIVE);
			}
		}
	};
	
	public void connect() {
		ConnectTask.execute();
		// try {
		// socket = new Socket(addr, port);
		// serverService = ProxyUtil.createClientProxy(getClassLoader(),
		// ServerService.class, jrpc, socket);
		// timeOut = serverService.connect();
		//
		// Log.i(TAG, "Connected to server " + addr + ":" + port);
		// changeMode(MODE.BOUND);
		// } catch (IOException e) {
		// Log.e(TAG, "Could not connect to server", e);
		// }
	}

	public void disconnect() {
		// switch (state) {
		// case BOUND:
		// case DRIVE:
		// if (socket != null && socket.isConnected()) {
		// new Handler(new Handler.Callback() {
		//
		// @Override
		// public boolean handleMessage(Message msg) {
		// serverService.closeSession();
		// return true;
		// }
		// });
		// }
		// changeMode(MODE.UNBOUND);
		// break;
		// default:
		// break;
		// }
		DisconnectTask.execute();
	}

	public void drive() {
		// if (state == MODE.BOUND) {
		// try {
		// driverService = ProxyUtil.createClientProxy(getClassLoader(),
		// DriverService.class, jrpc, socket);
		// driverService.findLJ();
		// changeMode(MODE.DRIVE);
		// } catch (LJNotFoundException e) {
		// Log.e(TAG, "Could not find LJ", e);
		// } catch (IOException e) {
		// Log.e(TAG, "Could not find lj", e);
		// }
		//
		// }
		DriveTask.execute();
	}

	public void stopDrive() {
		if (state == MODE.DRIVE) {
			changeMode(MODE.BOUND);
		}
	}

	private void changeMode(MODE newMode) {
		state = newMode;
		Log.i(TAG, "Mode changed to " + state);
		fireOnModeChange();
	}

	private void fireOnModeChange() {
		if (onModeChangeListener != null) {
			onModeChangeListener.onModeChange(state);
		}
	}

	public void setOnModeChangeListener(OnModeChangeListener listener) {
		onModeChangeListener = listener;
	}
}
