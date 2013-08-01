package com.ljremote.client.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import com.ljremote.client.LJClient;
import com.ljremote.client.LJClient.MODE;
import com.ljremote.client.LJClient.OnModeChangeListener;

public class MainWindow implements OnModeChangeListener {

	private JFrame frame;
	private LJClient client;
	private JButton btnConnect;
	private JButton btnDisconnect;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
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
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel serverPanel = new JPanel();
		tabbedPane.addTab("Server", null, serverPanel, null);
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
		
		JPanel panel_1 = new JPanel();
		serverPanel.add(panel_1);
		panel_1.setLayout(new GridLayout(2, 0, 0, 0));
		
		btnConnect = new JButton("Connect");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				new CreateAndConnectClient().execute();
			}
		});
		panel_1.add(btnConnect);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(client != null && client.getState() != MODE.NONE);
		btnDisconnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				new DisconnectClient().execute();
			}
		});
		panel_1.add(btnDisconnect);
		
		JButton btnSendHello = new JButton("Send hello");
		btnSendHello.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				new SendHello().execute();
			}
		});
		panel_1.add(btnSendHello);
		
		JButton btnNewButton = new JButton("Auto send Hello");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				new StartStopAutoSendHello().execute();
			}
		});
		panel_1.add(btnNewButton);
		
		JPanel driverPanel = new JPanel();
		tabbedPane.addTab("Driver", null, driverPanel, null);
		driverPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JButton btnFindLj = new JButton("Find LJ");
		driverPanel.add(btnFindLj);
		
		JButton btnLjVersion = new JButton("LJ Version ?");
		driverPanel.add(btnLjVersion);
		
		JButton btnLjReady = new JButton("LJ Ready ?");
		driverPanel.add(btnLjReady);
		
		JPanel logPanel = new JPanel();
		frame.getContentPane().add(logPanel, BorderLayout.SOUTH);
		SpringLayout sl_logPanel = new SpringLayout();
		logPanel.setLayout(sl_logPanel);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_logPanel.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.NORTH, logPanel);
		sl_logPanel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, logPanel);
		sl_logPanel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, logPanel);
		sl_logPanel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, logPanel);
		logPanel.add(scrollPane);
		
		JTextPane logPane = new JTextPane();
		scrollPane.setViewportView(logPane);
	}

	private class StartStopAutoSendHello extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			if ( client != null ){
				client.startStopKeepAlive();
			}
			return null;
		}
	};

	private class SendHello extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			if ( client != null ){
				System.out.println("hello");
				client.getServerService().hello();
			}
			return null;
		}
	};

	private class DisconnectClient extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			if ( client != null ){
				client.disconnect();
			}
			return null;
		}
	};

	private class CreateAndConnectClient extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			if ( client == null ){
				try {
					client = new LJClient("192.168.0.10",2508);
					client.registerOnModeChangeListener(MainWindow.this);
				} catch (UnknownHostException e) {
				}
			}
			if ( client != null ) {
				client.connect();
			}
			return null;
		}
	};

	public void onModeChange(MODE newMode) {
		switch (newMode) {
		case DRIVE:
			break;
		case CONNECT:
			btnConnect.setEnabled(false);
			btnDisconnect.setEnabled(true);
			break;
		case NONE:
			btnConnect.setEnabled(true);
			btnDisconnect.setEnabled(false);
			break;
		default:
			break;
		}
	}
}
