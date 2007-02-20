/*
 * Created on 19.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiangtng.agentcore.knowledge;

import java.awt.GridLayout;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class KnowledgeTree extends JPanel {

  /**
   * Comment for <code>serialVersionUID</code>
   */
  private static final long        serialVersionUID = 1L;

  protected DefaultMutableTreeNode rootNode;

  protected DefaultTreeModel       treeModel;

  protected JTree                  tree;

  public KnowledgeTree(String rootName) {
    super(new GridLayout(1, 0));

    rootNode = new DefaultMutableTreeNode(rootName);
    treeModel = new DefaultTreeModel(rootNode);
    treeModel.addTreeModelListener(new KnowledgeTreeListener());

    tree = new JTree(treeModel);
    tree.getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(true);

    JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane);
  }

  /** Remove all nodes except the root node. */
  public void clear() {
    clearNode(rootNode);
  }

  public void clearNode(DefaultMutableTreeNode node) {
    node.removeAllChildren();
    treeModel.reload();
  }

  /** Remove the currently selected node. */
  public void removeCurrentNode() {
    TreePath currentSelection = tree.getSelectionPath();
    if (currentSelection != null) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
          .getLastPathComponent());
      MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
      if (parent != null) {
        treeModel.removeNodeFromParent(currentNode);
        return;
      }
    }
  }

  /**
   * Remove the node from its parent.
   * 
   * @param node
   *          the node to remove (and all of its subnodes)
   */
  public void removeNode(DefaultMutableTreeNode node) {
    treeModel.removeNodeFromParent(node);
  }

  /**
   * Add an object to a node.
   * 
   * @param parent
   *          the node to add the object
   * @param child
   *          the object to add
   * @param shouldBeVisible
   *          if the added object should be visible afterwards
   * @return the created and added from the object node
   */
  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
      Object child, boolean shouldBeVisible) {
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

    if (parent == null) {
      parent = rootNode;
    }

    treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

    // Make sure the user can see the lovely new node.
    if (shouldBeVisible) {
      tree.scrollPathToVisible(new TreePath(childNode.getPath()));
    }
    return childNode;
  }

  class KnowledgeTreeListener implements TreeModelListener {

    public void treeNodesChanged(TreeModelEvent e) {
      DefaultMutableTreeNode node;
      node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

      /*
       * If the event lists children, then the changed node is the child of the
       * node we've already gotten. Otherwise, the changed node and the
       * specified node are the same.
       */
      try {
        int index = e.getChildIndices()[0];
        node = (DefaultMutableTreeNode) (node.getChildAt(index));
      } catch (NullPointerException exc) {
        exc.printStackTrace();
      }

      System.out.println("The user has finished editing the node.");
      System.out.println("New value: " + node.getUserObject());
    }

    public void treeNodesInserted(TreeModelEvent e) {
      // TODO Auto-generated method stub

    }

    public void treeNodesRemoved(TreeModelEvent e) {
      // TODO Auto-generated method stub

    }

    public void treeStructureChanged(TreeModelEvent e) {
      // TODO Auto-generated method stub

    }

  }

  public DefaultMutableTreeNode getRootNode() {
    return rootNode;
  }

  public static DefaultMutableTreeNode hasChild(DefaultMutableTreeNode root,
      Object userObject) {
    DefaultMutableTreeNode child = null;

    Enumeration children = root.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) children
          .nextElement();
      if (userObject.equals(node.getUserObject())) {
        child = node;
        break;
      }
      if (node.children().hasMoreElements()) {
        child = hasChild(node, userObject);
        if (child != null) break;
      }
    }
    
    return child;
  }
}
