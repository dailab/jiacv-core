/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import de.dailab.jiactng.agentcore.AAgentBean;

public class MemoryMonitor extends AAgentBean implements ActionListener,
    Runnable {

  private KnowledgeTree       tree      = null;

  JFrame                      frame     = null;

  private Thread              myThread  = null;

  private Boolean             syncObj   = Boolean.TRUE;

  private final static String GETMEMORY = "GETMEMORY";

  private final static String CLEAR     = "CLEAR";

  public void doInit() {
    initGui();
  }

  private void initGui() {
    frame = new JFrame();
    frame.setLayout(new BorderLayout());

    JPanel buttonpanel = new JPanel();

    JButton button = new JButton("Get Memory");
    button.setActionCommand(GETMEMORY);
    button.addActionListener(this);
    buttonpanel.add(button);

    button = new JButton("Clear");
    button.setActionCommand(CLEAR);
    button.addActionListener(this);
    buttonpanel.add(button);

    Tuple agent = memory.test(new Tuple("thisAgent.name", null));
    System.err.println("### got: "+agent);

    tree = new KnowledgeTree(agent.getArg2());
    tree.setPreferredSize(new Dimension(800, 500));

    frame.add(buttonpanel, BorderLayout.NORTH);
    frame.add(tree, BorderLayout.CENTER);

    frame.pack();
    frame.setVisible(true);

  }

  public void doStart() {
    myThread = new Thread(this);
    myThread.start();
  }

  public void execute() {

  }
  
  public void run() {
    while (true) {
      try {
        synchronized (syncObj) {
          syncObj.wait(1000);
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      updateMemoryTree();
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(CLEAR)) {
      tree.clear();
    } else if (e.getActionCommand().equals(GETMEMORY)) {
      updateMemoryTree();
    }

  }

  private void updateMemoryTree() {
    tree.clear();
    Set<Tuple> mem = memory.readAll(new Tuple(null, null));
    Iterator<Tuple> it = mem.iterator();
    while (it.hasNext()) {
      Tuple next = it.next();
      String path = next.getArg1();
      String value = next.getArg2();
      DefaultMutableTreeNode root = tree.getRootNode();
      DefaultMutableTreeNode parent = root;
      StringTokenizer tk = new StringTokenizer(path, ".");

      while (tk.hasMoreTokens()) {
        String token = tk.nextToken();
        DefaultMutableTreeNode newParent = null;

        Enumeration en = parent.children();
        while(en.hasMoreElements()) {
          DefaultMutableTreeNode node=(DefaultMutableTreeNode)en.nextElement();
          if(token.equals(node.getUserObject())) {
            newParent = node;
            break;
          }
        }
        if (newParent == null) {
          newParent = tree.addObject(parent, token,true);
        }
        parent = newParent;
      }
      // root.g

      tree.addObject(parent, new DefaultMutableTreeNode(value), true);
    }
  }

}
