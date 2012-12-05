package com.ljremote.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

/**
 * A multi-threaded streaming server that uses JSON-RPC
 * over sockets.
 *
 */
public class LJServer {


		private static final Log LOGGER = LogFactory.getLog(LJServer.class);

		private static final int SERVER_SOCKET_SO_TIMEOUT	= 5000;

		public static final long DEFAULT_CLIENT_TIMEOUT = 8000;

		private ThreadPoolExecutor executor;
		private ServerSocket serverSocket;
		private JsonRpcServer jsonRpcServer;
		private int maxClientErrors = 5;

		private AtomicBoolean isStarted 	= new AtomicBoolean(false);
		private AtomicBoolean keepRunning 	= new AtomicBoolean(false);

		private ExecutorService serverExecutor;

		private int maxClients;
		
		/**
		 * Creates a {@code StreamServer} with the given max number
		 * of threads using the given {@link ServerSocket} to listen
		 * for client connections.
		 * 
		 * @param jsonRpcServer the {@link JsonRpcServer} that will handle requests
		 * @param maxThreads the mac number of threads the server will spawn
		 * @param serverSocket the {@link ServerSocket} used for accepting client connections
		 */
		public LJServer(
			JsonRpcServer jsonRpcServer, int maxThreads, ServerSocket serverSocket) {

			// initialize values
			this.jsonRpcServer		= jsonRpcServer;
			this.serverSocket		= serverSocket;

			this.maxClients = maxThreads;
			// create the executor server
//			executor = new ThreadPoolExecutor(
//				maxThreads+1, maxThreads+1, 0L, TimeUnit.MILLISECONDS,
//	            new LinkedBlockingQueue<Runnable>());
//			executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

			serverExecutor = Executors.newSingleThreadExecutor();
			// we can't allow the server to re-throw exceptions
			jsonRpcServer.setRethrowExceptions(false);
		}

		/**
		 * Creates a {@code LJServer} with the given max number
		 * of threads.  A {@link ServerSocket} is created using the
		 * default {@link ServerSocketFactory} that listes on the
		 * given {@code port} and {@link InetAddress}.
		 * 
		 * @param jsonRpcServer the {@link JsonRpcServer} that will handle requests
		 * @param maxThreads the mac number of threads the server will spawn
		 * @param port the port to listen on
		 * @param backlog the {@link ServerSocket} backlog
		 * @param bindAddress the address to listen on
		 * @throws IOException on error
		 */
		public LJServer(
			JsonRpcServer jsonRpcServer, int maxThreads,
			int port, int backlog, InetAddress bindAddress)
			throws IOException {
			this(jsonRpcServer, maxThreads,
				ServerSocketFactory.getDefault().createServerSocket(port, backlog, bindAddress));
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
			LOGGER.info(
				"StreamServer starting "
				+serverSocket.getInetAddress()
				+":"+serverSocket.getLocalPort());

			// start the server
			keepRunning.set(true);
			serverExecutor.submit(new StreamServer(maxClients));
//			executor.submit(new Server());
		}

		/**
		 * Stops the server thread.
		 * @throws InterruptedException if a graceful shutdown didn't happen
		 */
		public void stop()
			throws InterruptedException {

			// make sure we're started
			if (!isStarted.get()) {
				throw new IllegalStateException(
					"The StreamServer is not started");
			}

			// stop the server
			keepRunning.set(false);

			// wait for the clients to stop
//			executor.shutdownNow();
			serverExecutor.shutdownNow();

			try {
				serverSocket.close();
			} catch (IOException e) { /* no-op */ }

			try {

				// wait for it to finish
//				if (!executor.isTerminated()) {
//					executor.awaitTermination(
//						2000 + SERVER_SOCKET_SO_TIMEOUT, TimeUnit.MILLISECONDS);
//				}
				if(serverExecutor.isTerminated()){
					serverExecutor.awaitTermination(2000 + SERVER_SOCKET_SO_TIMEOUT, TimeUnit.MILLISECONDS);
				}

				// set the flags
				isStarted.set(false);
				keepRunning.set(false);
				
			} catch (InterruptedException e) {
				LOGGER.error( "InterruptedException while waiting for termination", e);
				throw e;
			}
		}

		private class StreamServer implements Runnable {
			private ThreadPoolExecutor clientHandleExecutors;
			
			public StreamServer(int maxClients) {
				clientHandleExecutors= new ThreadPoolExecutor(maxClients, maxClients, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
				clientHandleExecutors.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
			}

			@Override
			public void run() {
				ServerSocket serverSocket= LJServer.this.serverSocket;
				
				while (LJServer.this.keepRunning.get()) {
					try {
						serverSocket.setSoTimeout(SERVER_SOCKET_SO_TIMEOUT);
						
						clientHandleExecutors.submit(new ClientHandle(serverSocket.accept()));
					} catch (SocketTimeoutException e) {
						// this is expected because of so_timeout

					} catch(SSLException ssle) {
						LOGGER.error( "SSLException while listening for clients, terminating", ssle);
						break;
						
					} catch(IOException ioe) {
						// this could be because the ServerSocket was closed
						if (SocketException.class.isInstance(ioe) && !keepRunning.get()) {
							break;
						}
						LOGGER.error( "Exception while listening for clients", ioe);
					}
					
				}
			}
			
		}
		
		/**
		 * Server thread.
		 */
		private class Server
			implements Runnable {

			/**
			 * {@inheritDoc}
			 */
			public void run() {
				// get the server socket
				ServerSocket serverSocket = LJServer.this.serverSocket;

				// start the listening loop
				Socket clientSocket = null;
				while (LJServer.this.keepRunning.get()) {
					try {
						// wait for a connection
						serverSocket.setSoTimeout((int)SERVER_SOCKET_SO_TIMEOUT);
						clientSocket = serverSocket.accept();

						// log the connection
						LOGGER.info(
							"Connection from "+clientSocket.getInetAddress()+":"+clientSocket.getPort());

						// spawn a new Server for the next connection
						// and break out of the server loop
						executor.submit(new Server());
						break;

					} catch (SocketTimeoutException e) {
						// this is expected because of so_timeout

					} catch(SSLException ssle) {
						LOGGER.error( "SSLException while listening for clients, terminating", ssle);
						break;
						
					} catch(IOException ioe) {
						// this could be because the ServerSocket was closed
						if (SocketException.class.isInstance(ioe) && !keepRunning.get()) {
							break;
						}
						LOGGER.error( "Exception while listening for clients", ioe);
					}
				}

				// handle the request
				// get the streams
				InputStream input;
				OutputStream output;
				try {
					input = clientSocket.getInputStream();
					output = clientSocket.getOutputStream();
				} catch (IOException e) {
					LOGGER.error( "Client socket failed", e);
					return;
				}
				ExecutorService executor= Executors.newSingleThreadExecutor();
				InputStreamChecker inputChecker = new InputStreamChecker(input);

				try {
					// keep handling requests
					int errors = 0;
					int loop= -1;
					while (LJServer.this.keepRunning.get() && clientSocket.isConnected()) {
						LOGGER.debug(String.format("Loop %d: TO=%d", ++loop,clientSocket.getSoTimeout()));
						
						Future<Integer> f= executor.submit(inputChecker);
						if(f.get(8000, TimeUnit.MILLISECONDS) > 0){
							// handle it if keepalive
							try {
//							if (input.available()==0) {
//								Thread.yield();
////								if(input.read() < 0){
////									break;								
////								}
////							if(!clientSocket.getKeepAlive()){
////								break;
////							}
//								continue;
//							}
								
								jsonRpcServer.handle(input, output);
							} catch (Throwable t) {
								errors++;
								if (errors<maxClientErrors) {
									LOGGER.error( "Exception while handling request", t);
								} else {
									LOGGER.error( "Closing client connection due to repeated errors", t);
									break;
								}
							}
						};
					}
				} catch (SocketException e1) {
					LOGGER.info("Timeout !!!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TimeoutException e) {
					LOGGER.error("Timeout expired");
				} finally {
					// clean up
					try {
						clientSocket.close();
						input.close();
						output.close();
						LOGGER.info("Client disconnected");
						LOGGER.debug(String.format("Estamted running tasks after: %d", LJServer.this.executor.getActiveCount()-1));
					} catch (IOException e) { /* no-op */ }
				}

			}
		}

		/**
		 * @return the maxClientErrors
		 */
		public int getMaxClientErrors() {
			return maxClientErrors;
		}

		/**
		 * @param maxClientErrors the maxClientErrors to set
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


		private class ClientHandle implements Runnable{

			private Socket socket;
			private InputStreamChecker inputChecker;
			private ExecutorService timerExecutor;
			
			public ClientHandle(Socket socket) {
				// log the connection
				LOGGER.info(
					"Connection from "+socket.getInetAddress()+":"+socket.getPort());
				this.socket= socket;
				timerExecutor = Executors.newSingleThreadExecutor();
				try {
					inputChecker= new InputStreamChecker(socket.getInputStream());
				} catch (IOException e) {
					LOGGER.error("Client socket failed", e);
				}
			}
			
			@Override
			public void run() {
				try {
					// keep handling requests
					int errors = 0;
					while (LJServer.this.keepRunning.get() && socket.isConnected()) {
						Future<Integer> f= timerExecutor.submit(inputChecker);
						if(f.get(DEFAULT_CLIENT_TIMEOUT, TimeUnit.MILLISECONDS) > 0){
							try {
								LOGGER.info("coucou");
								jsonRpcServer.handle(socket.getInputStream(), socket.getOutputStream());
							} catch (Throwable t) {
								errors++;
								if (errors<maxClientErrors) {
									LOGGER.error( "Exception while handling request", t);
								} else {
									LOGGER.error( "Closing client connection due to repeated errors", t);
									break;
								}
							}
						};
					}
				} catch (InterruptedException e) {
					LOGGER.error(e);
				} catch (ExecutionException e) {
					LOGGER.error(e);
				} catch (TimeoutException e) {
					LOGGER.warn(String.format("Timeout expired on %s",socket));
				} finally {
					// clean up
					try {
						socket.close();
						LOGGER.info(String.format("Client %s:%d disconnected",socket.getInetAddress(),socket.getPort()));
					} catch (IOException e) { /* no-op */ }
				}
			}
		}
		
		private class InputStreamChecker implements Callable<Integer>{

			InputStream in;
			public InputStreamChecker(InputStream in) {
				this.in= in;
			}
			@Override
			public Integer call() throws Exception {
				LOGGER.trace(String.format("Checking input %s", in.hashCode()));
				 int res;
				 while((res= in.available())<=0){ 
					if(in.available() > 0){
					}
				}
				return res;
			}
			
		}
}
