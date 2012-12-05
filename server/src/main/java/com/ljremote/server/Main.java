package com.ljremote.server;

import java.io.IOException;
import java.net.ServerSocket;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.DriverServiceImpl;
import com.ljremote.json.services.ServerService;
import com.ljremote.json.services.ServerServicesImpl;
import com.ljremote.server.driver.LJDriver;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LJDriver driver = new LJDriver();
		driver.findLJ();
//		JSonServiceManager serviceManager = new JSonServiceManager();

		 ServerService serverService = new ServerServicesImpl();
		 DriverService driverService = new DriverServiceImpl(driver);

//		serviceManager.registerService(new ServerServicesImpl(),
//				ServerService.class);
//		serviceManager.registerService(new DriverServiceImpl(driver),
//				DriverService.class);
		// JsonRpcServer jsonRpcServer = new JsonRpcServer(
		// serviceManager.getCompositeService(Main.class.getClassLoader()));

//		JsonRpcServer jsonRpcServer = new JsonRpcServer(
//				ProxyUtil.createCompositeServiceProxy(
//						Main.class.getClassLoader(), serviceManager.getServiceImpl(),
//						serviceManager.getServiceClass(), true));
		JsonRpcServer jsonRpcServer = new JsonRpcServer(
				ProxyUtil.createCompositeServiceProxy(
						Main.class.getClassLoader(), new Object[] {
							serverService, driverService, },
							new Class<?>[] { ServerService.class,
							DriverService.class, }, true));

		int maxThreads = 5;
		int port = 2508;
		try {
			LJServer server = new LJServer(jsonRpcServer,
					maxThreads, new ServerSocket(port));
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
