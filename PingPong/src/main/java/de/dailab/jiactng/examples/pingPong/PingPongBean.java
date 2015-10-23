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
public class PingPongBean extends AbstractAgentBean implements ActionListener {

  /**
   * Frame for the pingBean
   */
  private JFrame     pingFrame     = null;

  /**
   * Counter for sent pings, used to distinguish the pings.
   */
  private int        count         = 0;

  /**
   * Textfield to show result of read pongs.
   */
  private JTextField pongTextfield = null;

  /**
   * Initialisation of GUI. Consists of a Button (to send pings) and a Textfield
   * (to show Pongs)
   */
  private void initGui() {
    pingFrame = new JFrame();
    JPanel pingPanel = new JPanel(new BorderLayout());
    pingFrame.setPreferredSize(new Dimension(200, 100));
    pingFrame.add(pingPanel);

    JButton pingButton = new JButton("Ping");
    pingPanel.add(pingButton, BorderLayout.NORTH);
    pingButton.addActionListener(this);

    pongTextfield = new JTextField();
    pongTextfield.setEditable(false);
    pingPanel.add(pongTextfield, BorderLayout.SOUTH);

    pingFrame.pack();
    pingFrame.setVisible(true);
  }

  /**
   * Initialisation of PingBean. Calls initGui.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doInit()
   */
  public void doInit() {
    initGui();
  }

  /**
   * Execution of the PingBean. Tries to read a pong and shows it in the GUI if
   * successful.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
   */
  public void execute() {
    // try to read pong with a template-tuple.
    Pong template = new Pong(null);
    Pong read = this.memory.remove(template);

    if (read != null) {
      // pong was found, write to textfield
      pongTextfield.setText("read: " + read.getPong());
    }
  }

  /**
   * Stopping of PingBean. Disposes frame.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doInit()
   */
  public void doStop() {
    pingFrame.dispose();
  }

  /**
   * ActionListener for the Ping-Button.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    // write 'ping' into queue
    this.memory.write(new Ping("ping_" + (count++)));
  }

}
