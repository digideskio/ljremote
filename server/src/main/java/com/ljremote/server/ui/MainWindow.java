package com.ljremote.server.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.services.BGCueService;
import com.ljremote.json.services.BGCueServiceImpl;
import com.ljremote.json.services.CueListService;
import com.ljremote.json.services.CueListServiceImpl;
import com.ljremote.json.services.CueService;
import com.ljremote.json.services.CueServiceImpl;
import com.ljremote.json.services.DriverService;
import com.ljremote.json.services.DriverServiceImpl;
import com.ljremote.json.services.SequenceService;
import com.ljremote.json.services.SequenceServiceImpl;
import com.ljremote.json.services.ServerService;
import com.ljremote.json.services.StaticService;
import com.ljremote.json.services.StaticServiceImpl;
import com.ljremote.server.ClientManager;
import com.ljremote.server.LJServer;
import com.ljremote.server.LJServer.ClientConnectionListener;
import com.ljremote.server.LJServer.OnServerStatusChangeListener;
import com.ljremote.server.Main;
import com.ljremote.server.driver.LJDriver;
import com.ljremote.server.driver.util.WindowUtil;

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
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(lblClients)
					.addGap(18)
					.addComponent(clientNumber)
					.addGap(317))
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
					.addContainerGap())
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
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
		
		JButton btnPrintAllWindows = new JButton("Print All Windows");
		btnPrintAllWindows.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				WindowUtil.enumAllWindows();
			}
		});
		panel_2.add(btnPrintAllWindows);
		panel_1.setLayout(gl_panel_1);
		
		Component verticalGlue = Box.createVerticalGlue();
		generalPanel.add(verticalGlue);

		JPanel panel = new JPanel();
		tabbedPane.addTab("Log", null, panel, null);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);

		JToolBar toolBar = new JToolBar();
		sl_panel.putConstraint(SpringLayout.NORTH, toolBar, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, toolBar, 22, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, panel);
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		panel.add(toolBar);

		JLabel lblLevel = new JLabel("Level:");
		toolBar.add(lblLevel);

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"ALL", "TRACE", "INFO", "DEBUG"}));
		toolBar.add(comboBox);

		JButton btnClear = new JButton("Clear");
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				logPane.setText("");
			}
		});
		toolBar.add(btnClear);
		
		
		JScrollPane scrollPane_1 = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.NORTH, scrollPane_1, 3, SpringLayout.SOUTH, toolBar);
		sl_panel.putConstraint(SpringLayout.WEST, scrollPane_1, 0, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, scrollPane_1, 0, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, scrollPane_1, 0, SpringLayout.EAST, panel);
		logPane = JTextPaneAppender.createJTextPaneAppender();
		scrollPane_1.setViewportView(logPane);
		panel.add(scrollPane_1);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Clients", null, panel_3, null);
		SpringLayout sl_panel_3 = new SpringLayout();
		panel_3.setLayout(sl_panel_3);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_panel_3.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, panel_3);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, panel_3);
		panel_3.add(scrollPane);
		
		clientTabble = new JTable();
		clientTabble.setFillsViewportHeight(true);
		scrollPane.setViewportView(clientTabble);
		clientTabble.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Id", "Type", "IP Address", "Port", "Time Out"
			}
		) {
			Class[] columnTypes = new Class[] {
				Object.class, String.class, String.class, Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, true, true, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		clientTabble.getColumnModel().getColumn(1).setResizable(false);
		clientTabble.getColumnModel().getColumn(2).setResizable(false);
		clientTabble.getColumnModel().getColumn(3).setResizable(false);
		clientTabble.getColumnModel().getColumn(3).setPreferredWidth(35);
		clientTabble.getColumnModel().getColumn(4).setResizable(false);
		clientTabble.getColumnModel().getColumn(4).setPreferredWidth(55);
		
		setDefaultConfig();
	}

	protected void createAndStartServer() {
		LJDriver driver = new LJDriver();
		
		if ( driver == null ) {
			log.error("Impossible d'initialiser le driver");
			return;
		}
	
		driver.findLJ();
		ClientManager clientManager = new ClientManager();
		DriverService driverService = new DriverServiceImpl(driver);
		JsonRpcServer jsonRpcServer = new JsonRpcServer(
				ProxyUtil.createCompositeServiceProxy(
						Main.class.getClassLoader(),
							new Object[] {
								clientManager,
								driverService,
								new StaticServiceImpl(driver),
								new CueServiceImpl(driver),
								new SequenceServiceImpl(driver),
								new BGCueServiceImpl(driver),
								new CueListServiceImpl(driver),
							},
							new Class<?>[] {
								ServerService.class,
								DriverService.class,
								StaticService.class,
								CueService.class,
								SequenceService.class,
								BGCueService.class,
								CueListService.class,
							}
						, true));
	
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

	public void onClientConnectionEvent(ConnectionEvent event) {
		switch (event.getType()) {
		case CONNECT:
			((DefaultTableModel) clientTabble.getModel()).addRow(new Object[]{event.getId(),"",event.getAddr(),event.getPort(),event.getTimeOut()});
			clientNumber.setText(String.valueOf(++clientNumberInt));
			break;
		case DISCONNECT:
			DefaultTableModel model= (DefaultTableModel) clientTabble.getModel();
			boolean founded= false;
			for(int row=0;!founded && row < model.getRowCount();row++){
				if((Integer) model.getValueAt(row, 3) == event.getPort() && model.getValueAt(row, 2).equals(event.getAddr())){
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
