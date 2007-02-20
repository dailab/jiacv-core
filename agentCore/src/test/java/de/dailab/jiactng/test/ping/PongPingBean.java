/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.test.ping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dailab.jiactng.agentcore.AAgentBean;
import de.dailab.jiactng.agentcore.knowledge.Tuple;

public class PongPingBean extends AAgentBean implements ActionListener {

  private int        count = 0;

  private JTextField txt   = null;

  public PongPingBean() {
    System.err.println("### new PongPingBean created ");
  }

  private void initGui() {
    JFrame jp = new JFrame();
    JPanel panel = new JPanel(new BorderLayout());
    jp.setPreferredSize(new Dimension(200,100));
    jp.add(panel);

    JButton b = new JButton("Pong");
    panel.add(b, BorderLayout.NORTH);
    b.addActionListener(this);

    txt = new JTextField();
    txt.setEditable(false);
    panel.add(txt, BorderLayout.SOUTH);

    jp.pack();
    jp.setVisible(true);
  }

  public void doInit() {
    initGui();
  }

  @Override
  public void execute() {
    Tuple temp = new Tuple("pingpong.ping", null);
    Tuple read = this.memory.test(temp);
    if (read != null) {
      this.memory.in(read);
      txt.setText("read: " + read.getArg2());
      System.err.println("PongBean read: "+ read.getArg2());
    }
  }

  public void actionPerformed(ActionEvent e) {
    this.memory.out(new Tuple("pingpong.pong", "pong" + (count++)));
  }
}
