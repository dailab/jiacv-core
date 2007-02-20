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
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import de.dailab.jiangtng.agentcore.AAgentBean;

public class MemoryMonitor extends AAgentBean implements ActionListener {

  private TreeModel           model       = null;

  private KnowledgeTree       tree        = null;

  JFrame                      frame       = null;

  private final static String GETSERVICES = "GETSERVICES";

  private final static String CLEAR       = "CLEAR";

  private void initGui() {
    frame = new JFrame();
    frame.setLayout(new BorderLayout());

    JPanel buttonpanel = new JPanel();

    JButton button = new JButton("Get Memory");
    button.setActionCommand(GETSERVICES);
    button.addActionListener(this);
    buttonpanel.add(button);

    button = new JButton("Clear");
    button.setActionCommand(CLEAR);
    button.addActionListener(this);
    buttonpanel.add(button);

    Tuple agent = memory.in(new Tuple("thisAgent", null));

    tree = new KnowledgeTree(agent.getArg2());
    tree.setPreferredSize(new Dimension(800, 500));
    DefaultMutableTreeNode root = tree.getRootNode();

    frame.add(buttonpanel, BorderLayout.NORTH);
    frame.add(tree, BorderLayout.CENTER);

    frame.pack();
    frame.setVisible(true);

  }

  @Override
  public void execute() {
    // TODO Auto-generated method stub
    initGui();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(CLEAR)) {
      tree.clear();
    } else if (e.getActionCommand().equals(GETSERVICES)) {
      tree.clear();
      Set<Tuple> mem = memory.readAll(new Tuple(null, null));
      Iterator<Tuple> it = mem.iterator();
      while (it.hasNext()) {
        Tuple next = it.next();
        String path = next.getArg1();
        String value = next.getArg2();
        DefaultMutableTreeNode root = tree.getRootNode();
        DefaultMutableTreeNode parent = root;
        DefaultMutableTreeNode leaf = null;
        StringTokenizer tk = new StringTokenizer(path, ".");

        while (tk.hasMoreTokens()) {
          String token = tk.nextToken();
          DefaultMutableTreeNode newParent = null;

          newParent = tree.hasChild(parent, token);
          if (newParent == null) {
            newParent = tree.addObject(parent, token, true);
          }
          parent = newParent;
        }
        // root.g

        tree
            .addObject(parent, new DefaultMutableTreeNode(next.getArg2()), true);
      }

    }

  }

}
