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
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import de.dailab.jiactng.agentcore.AAgentBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * A monitor component that opens a window which shows the memory contens in a
 * tree. Can be used for watching an debugging the agent. Note that the updates
 * are currently happening via an own thread - every 1000ms.
 * 
 * @author Thomas Konnerth
 */
/**
 * @author moekon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class MemoryMonitor extends AAgentBean implements ActionListener,
    Runnable {

  /**
   * The tree-representation of the memories contents.
   */
  private KnowledgeTree       tree      = null;

  /**
   * The frame in which the monitor shows up.
   */
  JFrame                      frame     = null;

  /**
   * The thread that handels the monitor-updates.
   */
  private Thread              myThread  = null;

  /**
   * Synchronization object for the thread
   */
  private Boolean             syncObj   = Boolean.TRUE;

  /**
   * Activity flag for the thread.
   */
  private boolean             active    = false;

  /**
   * Action constant for the refresh-button.
   */
  private final static String GETMEMORY = "GETMEMORY";

  /**
   * Action constant for the clear-button.
   */
  private final static String CLEAR     = "CLEAR";

  /**
   * Action constant for the stop-button.
   */
  private final static String STOP      = "STOP";

  /**
   * Initializes the GUI. Called by agent-lifecycle.
   * 
   * @see de.dailab.jiactng.agentcore.AAgentBean#doInit()
   */
  public void doInit() {
    initGui();
  }

  /**
   * Initialization of the GUI. Creates two buttons (refresh and clear), and a
   * frame for the knowledgetree.
   */
  private void initGui() {
    frame = new JFrame();
    frame.setLayout(new BorderLayout());

    JPanel buttonpanel = new JPanel();

    JButton button = new JButton("Refresh");
    button.setActionCommand(GETMEMORY);
    button.addActionListener(this);
    buttonpanel.add(button);

    button = new JButton("Clear");
    button.setActionCommand(CLEAR);
    button.addActionListener(this);
    buttonpanel.add(button);

    button = new JButton("Stop");
    button.setActionCommand(STOP);
    button.addActionListener(this);
    buttonpanel.add(button);

    tree = new KnowledgeTree(thisAgent.getAgentName());
    tree.setPreferredSize(new Dimension(800, 500));

    frame.add(buttonpanel, BorderLayout.NORTH);
    frame.add(tree, BorderLayout.CENTER);

    frame.pack();
    frame.setVisible(true);

  }

  /**
   * Starting of the monitor. Creates the thread and starts it.
   * 
   * @see de.dailab.jiactng.agentcore.AAgentBean#doStart()
   */
  public void doStart() {
    myThread = new Thread(this);
    myThread.start();
    active = true;
  }

  /**
   * Stopping of the monitor. Stops the thread and destroys frame.
   * 
   * @see de.dailab.jiactng.agentcore.AAgentBean#doStop()
   */
  public void doStop() {
    active = false;
    myThread = null;
    frame.dispose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.AAgentBean#execute()
   */
  public void execute() {
    // does nothing, as the monitor has it's own thread.
  }

  /**
   * Run-method for the thread. Waits for 1000ms and updates the tree as long as
   * the active-flag is true.
   * 
   * @see java.lang.Runnable#run()
   * @see #active
   */
  public void run() {
    while (active) {
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

  /**
   * Action-execution for the buttons. Possible events are: CLEAR clears the
   * tree, GETMEMORY refreshes the tree and STOP which stops the agent.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(CLEAR)) {
      tree.clear();
    } else if (e.getActionCommand().equals(GETMEMORY)) {
      updateMemoryTree();
    } else if (e.getActionCommand().equals(STOP)) {
      try {
        thisAgent.stop();
        thisAgent.cleanup();
      } catch (LifecycleException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }

  }

  /**
   * Update for the knowledgeTree. Reads all tuples from the memory and
   * organizes them in a hierarchical fashin. The first argument is used to
   * create the path in the tree (with "." as delimiter) while the second
   * argument is added as a leaf.
   */
  private void updateMemoryTree() {
    // clear old tree
    tree.clear();

    // get current memory state and iterate
    Set<Tuple> mem = memory.readAll(new Tuple(null, null));
    for (Tuple next : mem) {
      String path = next.getArg1();
      String value = next.getArg2();
      DefaultMutableTreeNode root = tree.getRootNode();
      DefaultMutableTreeNode parent = root;
      StringTokenizer tk = new StringTokenizer(path, ".");

      // for alle tokens in first argument of Tuple:
      while (tk.hasMoreTokens()) {
        String token = tk.nextToken();
        DefaultMutableTreeNode newParent = null;

        // search if current token is already in tree
        Enumeration en = parent.children();
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
              .nextElement();
          if (token.equals(node.getUserObject())) {
            // if token is found in tree, use the node as new parent node and
            // continue with next token
            newParent = node;
            break;
          }
        }
        // if no token could be found, create new child of current parentnode,
        // and use child as new parent for the next token.
        if (newParent == null) {
          newParent = tree.addObject(parent, token, true);
        }
        parent = newParent;
      }

      // add second argument of tuple as leaf
      tree.addObject(parent, new DefaultMutableTreeNode(value), true);
    }
  }

}
