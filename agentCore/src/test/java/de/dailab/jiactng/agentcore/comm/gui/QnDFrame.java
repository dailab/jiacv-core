package de.dailab.jiactng.agentcore.comm.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Quick and Dirty Frame -- um mal schnell ne Componente zu testen
 * @author janko
 *
 */
public class QnDFrame extends JFrame {

	public QnDFrame(JComponent comp) {
		super(comp.getClass().getName());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1024, 768);
		this.getRootPane().setLayout(new BorderLayout());
		this.getRootPane().add(comp, BorderLayout.CENTER);
		this.pack();
	}
	
}
