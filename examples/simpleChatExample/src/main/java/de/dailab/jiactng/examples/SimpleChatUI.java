package de.dailab.jiactng.examples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Simple chat UI for writing messages and displaying past messages.
 *
 * @author kuester
 */
public final class SimpleChatUI extends JFrame {

	private static final long serialVersionUID = 7968115785141545541L;

	private static final String NL = System.getProperty("line.separator");

	/** Typed messages go here ... */
	private final MessageHandler handler;

	/** ... and received messages go here */
	private final JTextArea output;

	/**
	 * Create new Instance of Simple Chat UI. Better use the helper method
	 * {@link #createInstance(MessageHandler)} instead, for proper swing thread.
	 *
	 * @param handler
	 *          the message handler
	 */
	private SimpleChatUI(final MessageHandler handler) {
		super("Simple JIAC Chat Exmaple UI");

		this.handler = handler;

		// create input text field
		final JTextField input = new JTextField();
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SimpleChatUI.this.handler.sendMessage(input.getText());
				input.setText("");
			}
		});

		// create output text area
		this.output = new JTextArea();
		this.output.setEditable(false);

		// assemble widgets
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(new JScrollPane(this.output), BorderLayout.CENTER);
		this.getContentPane().add(input, BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Returns a default window size, currently 400x300 pixel
	 *
	 * @return a default dimension size of 400x300 pixel
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(400, 300);
	}

	/**
	 * Add a new message to be displayed in the UI's output panel.
	 *
	 * @param message
	 */
	public void addMessage(final ChatMessage message) {
		this.output.setText(this.output.getText() + this.formatMessage(message) + NL);
	}

	/*
	 * Return the message in usual chat message format.
	 */
	private String formatMessage(final ChatMessage message) {
		return String.format("[%1$tH:%1$tM:%1$tS] %2$s: %3$s", message.sendtimestamp, message.sendername, message.message);
	}

	/**
	 * Create new instance of the UI given a message handler to relay messages to,
	 * start and display it using a swing thread, and return it.
	 *
	 * @param handler
	 *          where to relay typed messages to
	 * @return new instance
	 */
	public static SimpleChatUI createInstance(final MessageHandler handler) {
		final SimpleChatUI frame = new SimpleChatUI(handler);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				// XXX currently, this brings the node to a rather harsh stop...
			}
		});
		return frame;
	}

	/**
	 * Interface for handling new messages, to be implemented by
	 * SimpleChatAgentBean. This callback is called whenever the user hits 'enter'
	 * in the input field.
	 *
	 * @author kuester
	 */
	public interface MessageHandler {

		/**
		 * Implement this method to handle messages that should be send.
		 *
		 * @param message
		 *          a text message to send
		 */
		void sendMessage(String message);

	}

	/**
	 * Testing...
	 *
	 * @param args
	 *          ignored parameters
	 */
	public static void main(final String[] args) throws Exception {
		final SimpleChatUI[] ui = new SimpleChatUI[1];
		ui[0] = createInstance(new MessageHandler() {
			@Override
			public void sendMessage(final String message) {
				ui[0].addMessage(new ChatMessage("Name", "Message", System.currentTimeMillis()));
			}
		});
	}

}
