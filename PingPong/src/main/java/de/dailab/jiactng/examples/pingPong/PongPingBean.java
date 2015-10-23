/*
 * Created on 16.02.2007
 */
package de.dailab.jiactng.examples.pingPong;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ontology.Ping;
import ontology.Pong;
import de.dailab.jiactng.agentcore.AbstractAgentBean;

/**
 * One of the Beans for the local PingPong example. This bean creates a GUI
 * which allows the user to send 'ping's. The execute-method of the bean tries
 * to read a 'pong', and shows it in the GUI if successful.
 * 
 * @author Thomas Konnerth
 */
public class PongPingBean extends AbstractAgentBean implements ActionListener {

  /**
   * Frame for the pongBean
   */
  private JFrame     pongFrame     = null;

  /**
   * Counter for sent pings, used to distinguish the pings.
   */
  private int        count         = 0;

  /**
   * Textfield to show result of read pongs.
   */
  private JTextField pingTextfield = null;

  /**
   * Initialisation of GUI. Consists of a Button (to send pings) and a Textfield
   * (to show Pongs)
   */
  private void initGui() {
    pongFrame = new JFrame();
    JPanel pongPanel = new JPanel(new BorderLayout());
    pongFrame.setPreferredSize(new Dimension(200, 100));
    pongFrame.add(pongPanel);

    JButton pongButton = new JButton("Pong");
    pongPanel.add(pongButton, BorderLayout.NORTH);
    pongButton.addActionListener(this);

    pingTextfield = new JTextField();
    pingTextfield.setEditable(false);
    pongPanel.add(pingTextfield, BorderLayout.SOUTH);

    pongFrame.pack();
    pongFrame.setVisible(true);
  }

  /**
   * Initialisation of PongBean. Calls initGui.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doInit()
   */
  public void doInit() {
    initGui();
  }

  /**
   * Execution of the PongBean. Tries to read a ping and shows it in the GUI if
   * successful.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
   */
  public void execute() {
    // try to read ping with a template-tuple.
    Ping template = new Ping(null);
    Ping read = this.memory.remove(template);

    if (read != null) {
      // ping was found, write to textfield
      pingTextfield.setText("read: " + read.getPing());
    }
  }

  /**
   * Stopping of PongBean. Disposes frame.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doInit()
   */
  public void doStop() {
    pongFrame.dispose();
  }

  /**
   * ActionListener for the Ping-Button.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    // write 'pong' into queue
    this.memory.write(new Pong("pong_" + (count++)));
  }

}
