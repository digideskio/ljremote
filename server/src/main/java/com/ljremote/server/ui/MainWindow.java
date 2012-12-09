package com.ljremote.server.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.DriverServiceImpl;
import com.ljremote.json.services.ServerService;
import com.ljremote.server.ClientManager;
import com.ljremote.server.LJServer;
import com.ljremote.server.LJServer.ClientConnectionListener;
import com.ljremote.server.LJServer.OnServerStatusChangeListener;
import com.ljremote.server.Main;
import com.ljremote.server.driver.LJDriver;

public class MainWindow implements OnServerStatusChangeListener, ClientConnectionListener {

	private JFrame frmLjServer;
	private JTextPane logPane;
	private JLabel status;
	private LJServer server= null;
	private static final Log log = LogFactory.getLog(MainWindow.class);
	private static final String SERVER_RUNNING = "Running";
	private static final String SERVER_STOPPED = "Stopped";
	private JButton btnStop;
	private JButton btnStart;
	private JTable clientTabble;
	private JLabel clientNumber;
	private int clientNumberInt;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmLjServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frmLjServer = new JFrame();
		frmLjServer.setTitle("LJ Server");
		frmLjServer.setBounds(100, 100, 450, 300);
		frmLjServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmLjServer.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JPanel generalPanel = new JPanel();
		tabbedPane.addTab("General", null, generalPanel, null);
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Status",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		generalPanel.add(panel_1);

		JLabel lblStatus = new JLabel("Status:");

		status = new JLabel("");

		JPanel panel_2 = new JPanel();
		
		JLabel lblClients = new JLabel("Clients:");
		
		clientNumber = new JLabel("");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(lblStatus, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(status)
					.addContainerGap(321, Short.MAX_VALUE))
				.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(lblClients)
					.addGap(18)
					.addComponent(clientNumber)
					.addGap(317))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblStatus)
						.addComponent(status))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblClients)
						.addComponent(clientNumber))
					.addPreferredGap(ComponentPlacement.RELATED, 140, Short.MAX_VALUE)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);

		btnStart = new JButton("Start");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				createAndStartServer();
			}
		});
		panel_2.add(btnStart);

		btnStop = new JButton("Stop");
		btnStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				stopServer();
			}
		});
		panel_2.add(btnStop);
		panel_1.setLayout(gl_panel_1);

		JPanel panel = new JPanel();
		tabbedPane.addTab("Log", null, panel, null);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JToolBar toolBar = new JToolBar();
		panel.add(toolBar);

		JLabel lblLevel = new JLabel("Level:");
		toolBar.add(lblLevel);

		JComboBox comboBox = new JComboBox();
		toolBar.add(comboBox);

		JButton btnClear = new JButton("Clear");
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				logPane.setText("");
			}
		});
		toolBar.add(btnClear);
		
		logPane = JTextPaneAppender.createJTextPaneAppender();
		panel.add(logPane);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Clients", null, panel_3, null);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_3.add(scrollPane);
		
		clientTabble = new JTable();
		clientTabble.setFillsViewportHeight(true);
		scrollPane.setViewportView(clientTabble);
		clientTabble.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Type", "IP Address", "Port", "Time Out"
			}
		) {
			Class<?>[] columnTypes = new Class[] {
				String.class, String.class, Integer.class, Integer.class
			};
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		clientTabble.getColumnModel().getColumn(0).setResizable(false);
		clientTabble.getColumnModel().getColumn(1).setResizable(false);
		clientTabble.getColumnModel().getColumn(2).setResizable(false);
		clientTabble.getColumnModel().getColumn(2).setPreferredWidth(35);
		clientTabble.getColumnModel().getColumn(3).setResizable(false);
		clientTabble.getColumnModel().getColumn(3).setPreferredWidth(55);
		
		setDefaultConfig();
	}

	protected void createAndStartServer() {
		LJDriver driver = new LJDriver();
		driver.findLJ();
	
		ClientManager clientManager = new ClientManager();
		DriverService driverService = new DriverServiceImpl(driver);
		JsonRpcServer jsonRpcServer = new JsonRpcServer(
				ProxyUtil.createCompositeServiceProxy(
						Main.class.getClassLoader(), new Object[] {
							clientManager, driverService, },
							new Class<?>[] { ServerService.class,
							DriverService.class, }, true));
	
		jsonRpcServer.setErrorResolver(new LJNotFoundException().getErrorResolver());
		int maxThreads = 5;
		int port = 2508;
		clientNumberInt= 0;
		try {
			server = new LJServer(clientManager,jsonRpcServer,
					maxThreads, new ServerSocket(port));
			server.setOnServerStatusChange(this);
			server.registerClientConnectionListenrer(this);
			server.start();
		} catch (IOException e1) {
			log.error(e1);
		}
	}

	protected void stopServer() {
		if(server !=null){
			try {
				server.stop();
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
	}

	private void toggleButtons(boolean started) {
		btnStart.setEnabled(!started);
		btnStop.setEnabled(started);
	}
	
	private void setDefaultConfig(){
		status.setText(SERVER_STOPPED);
		clientNumber.setText(String.valueOf(clientNumberInt));
		toggleButtons(false);
	}

	@Override
	public void onServerStatusChange(STATUS status) {
		switch (status) {
		case RUNNING:
			this.status.setText(SERVER_RUNNING);
			toggleButtons(true);
			break;
		case STOPPED:
			this.status.setText(SERVER_STOPPED);
			toggleButtons(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClientConnectionEvent(ConnectionEvent event) {
		switch (event.getType()) {
		case CONNECT:
			((DefaultTableModel) clientTabble.getModel()).addRow(new Object[]{"",event.getAddr(),event.getPort(),event.getTimeOut()});
			clientNumber.setText(String.valueOf(++clientNumberInt));
			break;
		case DISCONNECT:
			DefaultTableModel model= (DefaultTableModel) clientTabble.getModel();
			boolean founded= false;
			for(int row=0;!founded && row < model.getRowCount();row++){
				if((Integer) model.getValueAt(row, 2) == event.getPort() && model.getValueAt(row, 1).equals(event.getAddr())){
					model.removeRow(row);
					clientNumber.setText(String.valueOf(--clientNumberInt));
					founded= true;
				}
			}
			break;
		default:
			break;
		}
	}
}
