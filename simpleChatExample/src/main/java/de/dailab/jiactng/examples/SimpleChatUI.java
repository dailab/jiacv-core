package de.dailab.jiactng.examples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Simple chat UI for writing messages and displaying past messages.
 * 
 * @author kuester
 */
public class SimpleChatUI extends JFrame {

	private static final long serialVersionUID = 7968115785141545541L;

	private static final String NL = System.getProperty("line.separator");
	
	private final MessageHandler handler;
	
	private final JTextField output;
	
	public SimpleChatUI(MessageHandler handler) {
		super("Simple JIAC Chat Exmaple UI");
		
		this.handler = handler;
	
		final JTextField input = new JTextField();
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleChatUI.this.handler.sendMessage(input.getText());
				input.setText("");
			}
		});

		output = new JTextField();
		output.setEditable(false);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(output, BorderLayout.CENTER);
		this.getContentPane().add(input, BorderLayout.SOUTH);
		this.pack();
	}

	public void addMessage(ChatMessage message) {
		this.output.setText(this.output.getText() + formatMessage(message) + NL);
	}
	
	private String formatMessage(ChatMessage message) {
		return String.format("[%1$tH:%1$tM:%1$tS] %2$s: %3$s", message.sendtimestamp, message.sendername, message.message);
	}
	
	public static SimpleChatUI createInstance(MessageHandler handler) {
		final SimpleChatUI frame = new SimpleChatUI(handler);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
		return frame;
	}

	/**
	 * to be implemented by SimpleChatAgentBean
	 *
	 * @author kuester
	 */
	public interface MessageHandler {
		
		void sendMessage(String message);
		
	}
	
}
