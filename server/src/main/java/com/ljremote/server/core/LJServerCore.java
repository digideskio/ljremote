package com.ljremote.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.services.BGCueService;
import com.ljremote.json.services.BGCueServiceImpl;
import com.ljremote.json.services.ControlService;
import com.ljremote.json.services.ControlServiceImpl;
import com.ljremote.json.services.CueListService;
import com.ljremote.json.services.CueListServiceImpl;
import com.ljremote.json.services.CueService;
import com.ljremote.json.services.CueServiceImpl;
import com.ljremote.json.services.DMXOutOverrideService;
import com.ljremote.json.services.DMXOutOverrideServiceImpl;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.DriverServiceImpl;
import com.ljremote.json.services.LJFunctionService;
import com.ljremote.json.services.LJFunctionServiceImpl;
import com.ljremote.json.services.MasterIntService;
import com.ljremote.json.services.MasterIntServiceImpl;
import com.ljremote.json.services.SequenceService;
import com.ljremote.json.services.SequenceServiceImpl;
import com.ljremote.json.services.ServerService;
import com.ljremote.json.services.StaticService;
import com.ljremote.json.services.StaticServiceImpl;
import com.ljremote.server.ClientManager;
import com.ljremote.server.LJServer;
import com.ljremote.server.Main;
import com.ljremote.server.driver.LJDriver;
import com.ljremote.server.ui.LJServerGUI;

public class LJServerCore {

	/*--- CONSTANTS -----------------*/
	/** Application name */
	public final static String APP_NAME = "LJRemoteServer";

	/*--- PREFERENCES ---------------*/
	/** Root node for the package */
	public final static String PREFS_NODE = "com/ljremote/server/ljRemoteServer";
	
	/** User Preferences for the application */
	private static Preferences prefs = Preferences.userRoot().node(
			PREFS_NODE);
	
	/*--- FILES -----------------*/
	/** Preferences file name */
	private final static String PREFS_FILE = "settings.xml";
	
	/*--- Entries --------------*/
	private static final String SERVER_PORT = "server/port";
	private static final int 	SERVER_PORT_DEFAULT = 2508;
	private static final String SERVER_MAX_CLIENTS = "server/max_clients";
	private static final int 	SERVER_MAX_CLIENTS_DEFAULT = 5;
	
	/*--- OPTIONS ---------------*/
	private Options options;
	private boolean debug = false;

	private LJServerGUI mainFrame;

	private LJDriver driver;

	private LJServer server;
	
	/** Logger */
	private static final Log log = LogFactory.getLog(LJServerCore.class);
	
	public LJServerCore(String[] args) {
		log.info("Initiation");
		
		setAguments(args);
		importPreferences();
		init();
	}

	
	private void init() {
		setMainFrame(new LJServerGUI(this));
	}

	public void createAndStartServer() {
		applyPreferences();
		driver = new LJDriver();

		if (driver == null) {
			log.error("Impossible d'initialiser le driver");
			return;
		}

		driver.findLJ();
		ClientManager clientManager = new ClientManager();
		DriverService driverService = new DriverServiceImpl(driver);
		JsonRpcServer jsonRpcServer = new JsonRpcServer(
			ProxyUtil.createCompositeServiceProxy(
					Main.class.getClassLoader(),
					new Object[] { clientManager, driverService,
							new StaticServiceImpl(driver),
							new CueServiceImpl(driver),
							new SequenceServiceImpl(driver),
							new BGCueServiceImpl(driver),
							new CueListServiceImpl(driver),
							new ControlServiceImpl(driver),
							new LJFunctionServiceImpl(driver),
							new DMXOutOverrideServiceImpl(driver),
							new MasterIntServiceImpl(driver),
							},
					new Class<?>[] { ServerService.class,
							DriverService.class, StaticService.class,
							CueService.class, SequenceService.class,
							BGCueService.class, CueListService.class,
							ControlService.class, LJFunctionService.class, DMXOutOverrideService.class,
							MasterIntService.class},
					true));

		jsonRpcServer.setErrorResolver(new LJNotFoundException()
			.getErrorResolver());
		int maxThreads = getPrefs().getInt(SERVER_MAX_CLIENTS, SERVER_MAX_CLIENTS_DEFAULT);
		int port = getPrefs().getInt(SERVER_PORT, SERVER_PORT_DEFAULT);
		try {
			server = new LJServer(clientManager, jsonRpcServer, maxThreads,
				new ServerSocket(port));
			server.setOnServerStatusChange(getMainFrame());
			server.registerClientConnectionListenrer(getMainFrame());
			server.start();
		} catch (IOException e1) {
			log.error(e1);
		}
	}
	
	public void stopServer() {
		if (server != null && server.isStarted()) {
			try {
				driver.stopActions();
				server.stop();
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
	}
	
	protected void setAguments(String[] args) {
		initOptions();
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			
			debug = cmd.hasOption("debug");
		} catch (ParseException e) {
			log.fatal("Error while parsing arguments", e);
		}
	}

	private void initOptions() {
		options = new Options();
		options.addOption("debug", false, "Active debug mode");
	}
	
	/**
	 * Get the user application preferences
	 * 
	 * @return user application preferences
	 */
	public Preferences getPrefs() {
		return prefs;
	}
	
	/**
	 * Restore global preferences to default values
	 * 
	 */
	private void restoreDefaultPreferences() {
		log.info("Restoring default preferences");
		applyPreferences();
	}
	
	/**
	 * Update application variables from preferences
	 * 
	 */
	private void applyPreferences() {
	}
	
	/**
	 * Import preferences from defined files
	 * 
	 */
	private void importPreferences() {
		log.info("Checking Preferences Files");
		FileInputStream input = null;

		// Importing preferences
		try {
			input = new FileInputStream(new File(PREFS_FILE));
			Preferences.importPreferences(input);
			log.info("User preferences file  \"" + PREFS_FILE
					+ "\" found and imported");
		} catch (FileNotFoundException e) {
			log.warn("User preferences file  \"" + PREFS_FILE
					+ "\" not found");
		} catch (IOException e) {
			log.warn("Error while working on \"" + PREFS_FILE + "\" : "
					+ e.getMessage());
		} catch (InvalidPreferencesFormatException e) {
			log.warn("Error while importing user preferences : "
					+ e.getMessage());
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}

		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			log.warn("Error with BackingStore");
			log.debug(e.getStackTrace().toString());
		}
		applyPreferences();
	}

	/**
	 * Export preferences
	 */
	private void exportPreferences() {
		log.info("Exporting Preferences");
		exportPreferencesToFile(prefs, PREFS_FILE, "user preferences");
	}

	/**
	 * Export preferences into file
	 * 
	 * @param prefs
	 *            {@link Preferences} to export
	 * @param fileName
	 *            name of the export file
	 * @param desc
	 *            description for logging
	 */
	private void exportPreferencesToFile(Preferences prefs, String fileName,
			String desc) {
		File file = new File(fileName);
		FileOutputStream output = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				log.warn("Impossible to export " + desc + " to \""
						+ fileName + "\" : " + e.getMessage());
			}
		}
		if (file.exists()) {
			try {
				output = new FileOutputStream(file);
				prefs.exportSubtree(output);
				log.info(desc + " exported into \"" + fileName + "\"");
			} catch (FileNotFoundException e) {
				log.warn("Impossible to export " + desc + " into \""
						+ fileName + "\" : " + e.getMessage());
			} catch (IOException e) {
				log.warn("Impossible to export " + desc + " into \""
						+ fileName + "\" : " + e.getMessage());
			} catch (BackingStoreException e) {
				log.warn("Impossible to export " + desc + " into \""
						+ fileName + "\" : " + e.getMessage());
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	/**
	 * Do task before quitting. Do not call {@link System#exit(int)} but this
	 * 
	 * @param status
	 *            @see {@link System#exit(int)}
	 */
	public void quit(int status) {
		log.info("EXITING with status " + status);
		exportPreferences();
		System.exit(status);
	}

	@Override
	protected void finalize() throws Throwable {
		quit(0);
		super.finalize();
	}
	
	/**
	 * Set the GUI main frame
	 * 
	 * @param mainFrame
	 *            the mainFrame to set
	 * @uml.property name="mainFrame"
	 */
	public void setMainFrame(LJServerGUI mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/**
	 * Get the GUI main frame
	 * 
	 * @return the mainFrame
	 * @uml.property name="mainFrame"
	 */
	public LJServerGUI getMainFrame() {
		return mainFrame;
	}


	public boolean isDebug() {
		return debug;
	}


	public int serverIsBusy() {
		if ( server != null && server.isStarted() ) {
			return server.getClientCount();
		}
		return 0;
	}


	public int getServerPort() {
		return getPrefs().getInt(SERVER_PORT, SERVER_PORT_DEFAULT);
	}


	public void updateServerPort(int port) {
		getPrefs().putInt(SERVER_PORT, port);
	}
}
