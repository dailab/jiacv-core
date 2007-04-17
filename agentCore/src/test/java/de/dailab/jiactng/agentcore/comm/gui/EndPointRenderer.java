package de.dailab.jiactng.agentcore.comm.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.dailab.jiactng.agentcore.comm.EndPoint;

/**
 * Der Renderer für die AgentCombos ValueObjekte sind vom Typ AgentStub
 * 
 * @author janko
 */
public class EndPointRenderer extends JLabel implements ListCellRenderer {
	public EndPointRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	/*
	 * This method finds the image and text corresponding to the selected value and returns the label, set up to display
	 * the text and image.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
																					boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		EndPoint endpoint = (EndPoint) value;
		if (endpoint != null) {
			setText(endpoint.toString());
			setFont(list.getFont());
		}
		return this;
	}
}