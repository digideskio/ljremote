package com.ljremote.server.ui;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class JTextPaneAppender extends AppenderSkeleton {
	
	private JTextPane textPane;
	private StyledDocument doc;

	public JTextPaneAppender(){
		this.textPane= new JTextPane();
		doc= textPane.getStyledDocument();
	}
	
	@Override
	protected void append(LoggingEvent arg0) {
		final String message = layout.format(arg0);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					doc.insertString(doc.getLength(), message, null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void close() {
		super.closed= true;
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	public JTextPane getView(){
		return textPane;
	}
	
	public static JTextPane createJTextPaneAppender(){
		return new JTextPaneAppender().getView();
	}
}
