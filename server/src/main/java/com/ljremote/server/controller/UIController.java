package com.ljremote.server.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.server.core.LJServerCore;

import static com.ljremote.server.controller.UIControllerAction.*;

public class UIController implements ActionListener, WindowListener {

	private LJServerCore main;

	/** Logger */
	private static final Log log = LogFactory.getLog(UIController.class);
	
	public UIController(LJServerCore main) {
		super();
		this.main = main;
	}

	public void actionPerformed(ActionEvent event) {
		String cmd = event.getActionCommand();
		processCmd(cmd);
	}
	
	public void processCmd(String cmd) {
		log.debug(String.format("Command : %s", cmd));
		if (QUIT.equals(cmd)) {
			main.quit(0);
		} else if (cmd.startsWith(UI)) {
			processUI(cmd);
		} else if (cmd.startsWith(SERVER)) {
			processServer(cmd);
		}
	}

	private void processServer(String cmd) {
		if( cmd.startsWith(SERVER_START) ) {
			main.createAndStartServer();
		} else if ( cmd.startsWith(SERVER_STOP) ) {
			main.stopServer();
		} else if ( cmd.startsWith(SERVER_RESTART) ) {
			boolean restartServer = false;
			int clientNumberInt = main.serverIsBusy();
			if ( clientNumberInt > 0 ) {
				switch (JOptionPane
						.showConfirmDialog(
								main.getMainFrame(),
								String.format("%d clients are still connected are you sure you want to restart server now ?",
										clientNumberInt),
								"Restart Server",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,null
								)
						) {
				case JOptionPane.NO_OPTION:
					restartServer = false;
					break;
				case JOptionPane.YES_OPTION:
				default:
					break;
				}
			}
			if (restartServer) {
//						stopServer();
//						createAndStartServer();
			}
			main.stopServer();
			main.createAndStartServer();
		}
	}
	
	private void processUI(String cmd) {
	}
	
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosing(WindowEvent e) {
		if (main.getMainFrame().isDisplayable()) {
			main.getMainFrame().dispose();
		}
		main.quit(0);
	}

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


}
