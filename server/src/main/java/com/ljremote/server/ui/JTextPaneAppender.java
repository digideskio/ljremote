package com.ljremote.server.ui;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class JTextPaneAppender extends AppenderSkeleton {

	private static JTextPane textPane;
	private static StyledDocument doc;
	private static Style styleBase;
	private static Style styleInfo;
	private static Style styleWarn;
	private static Style styleFatal;
	private static Style styleTrace;
	private static Style styleDebug;
	private static Style styleError;

	public JTextPaneAppender() {
		if (doc == null) {
			textPane = new JTextPane();
			textPane.setEditable(false);
			doc = textPane.getStyledDocument();

			styleBase = doc.addStyle("base", null);
			StyleConstants.setFontFamily(styleBase, "monospaced");

			styleInfo = doc.addStyle("info", styleBase);
			StyleConstants.setForeground(styleInfo, Color.BLUE);

			styleDebug = doc.addStyle("debug", styleBase);
			StyleConstants.setForeground(styleDebug, Color.GREEN);

			styleWarn = doc.addStyle("warn", styleBase);
			StyleConstants.setForeground(styleWarn, Color.ORANGE);

			styleError = doc.addStyle("error", styleBase);
			StyleConstants.setForeground(styleError, Color.RED);

			styleFatal = doc.addStyle("fatal", styleError);
			StyleConstants.setBold(styleFatal, true);

			styleTrace = doc.addStyle("trace", styleBase);
			StyleConstants.setItalic(styleTrace, true);
		}
	}

	@Override
	public void activateOptions() {
		super.activateOptions();
	}

	@Override
	protected void append(LoggingEvent event) {
		final String message = layout.format(event);
		final Style style;
		if (event.getLevel().equals(Level.INFO)) {
			style = styleInfo;
		} else if (event.getLevel().equals(Level.DEBUG)) {
			style = styleDebug;
		} else if (event.getLevel().equals(Level.WARN)) {
			style = styleWarn;
		} else if (event.getLevel().equals(Level.ERROR)) {
			style = styleError;
		} else if (event.getLevel().equals(Level.FATAL)) {
			style = styleFatal;
		} else if (event.getLevel().equals(Level.TRACE)) {
			style = styleTrace;
		} else {
			style = styleBase;
		}
		final String[] throwableStrRep = event.getThrowableStrRep();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					doc.insertString(doc.getLength(), message, style);
					if ( throwableStrRep != null ) {
						for ( int i = 0; i < throwableStrRep.length; i++ ) {
							doc.insertString(doc.getLength(), throwableStrRep[i], style);
						}
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void close() {
		super.closed = true;
	}

	public boolean requiresLayout() {
		return true;
	}

	public JTextPane getView() {
		return textPane;
	}

	/**
	 * @wbp.factory
	 * @return
	 */
	public static JTextPane createJTextPaneAppender() {
		return new JTextPaneAppender().getView();
	}

}
