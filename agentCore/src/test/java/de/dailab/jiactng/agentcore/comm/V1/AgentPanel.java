package de.dailab.jiactng.agentcore.comm.V1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.CommMessageListener;
import de.dailab.jiactng.agentcore.comm.EndPoint;
import de.dailab.jiactng.agentcore.comm.IEndPoint;
import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.JiacMessageFactory;
import de.dailab.jiactng.agentcore.comm.Util;

/**
 * Die Gui hat Zugriff auf eine Communikationsbean.. Über diese Bean läuft der Jms zugriff.
 * 
 * @author janko
 */
public class AgentPanel extends JPanel implements ActionListener, CommMessageListener {
	static String LAYOUT_COLUMNS = "3dlu, left:max(20dlu;pref), 3dlu, left:max(200dlu;pref), 3dlu, left:max(20dlu;pref),";
	static String LAYOUT_ROWS = "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 6dlu";
	FormLayout layout = new FormLayout(LAYOUT_COLUMNS, LAYOUT_ROWS);

	JTextField _msgTf;
	JTextField _destAddressTf;
	JComboBox _destAddressCombo;
	JTextArea _incomingQueueMessageArea;
	JTextArea _incomingTopicMessageArea;	
	IEndPoint _endpoint;

	JButton _sendButton;
	JButton _refreshButton;
	JButton _clearButton;

	CommBean _commBean;

	public AgentPanel(CommBean _commBean) {
		super();
		setCommBean(_commBean);
		_commBean.addCommMessageListener(this);
		init(_commBean.getAddress());
	}

	public void init(IEndPoint endpoint) {
		_endpoint = endpoint;
		_sendButton = new JButton("send");
		_sendButton.addActionListener(this);
		_refreshButton = new JButton("refresh");
		_refreshButton.addActionListener(this);
		_clearButton = new JButton("clear");
		_clearButton.addActionListener(this);
		_destAddressTf = new JTextField(20);
		_destAddressCombo = new JComboBox();
		_destAddressCombo.setRenderer(new EndPointRenderer());
		_destAddressCombo.addActionListener(this);
		_msgTf = new JTextField(20);
		_incomingQueueMessageArea = new JTextArea(14, 60);
		_incomingQueueMessageArea.setLineWrap(true);
		_incomingQueueMessageArea.setWrapStyleWord(true);
		_incomingTopicMessageArea = new JTextArea(14, 60);
		_incomingTopicMessageArea.setLineWrap(true);
		_incomingTopicMessageArea.setWrapStyleWord(true);

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Own Address", cc.xy(2, 2));
		builder.addLabel(_endpoint.toString(), cc.xy(4, 2));

		builder.addLabel("Dest Address", cc.xy(2, 4));
		builder.add(_destAddressCombo, cc.xy(4, 4));
		builder.add(_refreshButton, cc.xyw(6, 4, 1));

		builder.addLabel("Message", cc.xy(2, 6));
		builder.add(_msgTf, cc.xyw(4, 6, 1));
		builder.add(_sendButton, cc.xyw(6, 6, 1));

		builder.addLabel("IncomingQueueMessages", cc.xy(2, 8));
		builder.add(new JScrollPane(_incomingQueueMessageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xyw(4, 8, 1));

		builder.addLabel("IncomingTopicMessages", cc.xy(2, 10));
		builder.add(new JScrollPane(_incomingTopicMessageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xyw(4, 10, 1));
		
		builder.add(_clearButton, cc.xyw(6, 8, 1));

		this.add(builder.getPanel(), BorderLayout.CENTER);
		this.setPreferredSize(builder.getPanel().getPreferredSize());
	}

	public void updateGui() {
		List agents = _commBean.getLocalAgents();
		List<IEndPoint> endPointList = new ArrayList<IEndPoint>();
		for (Iterator iter = agents.iterator(); iter.hasNext();) {
			IAgent agent = (IAgent) iter.next();
			for (Iterator agentIter = agent.getAdaptors().iterator(); agentIter.hasNext();) {
				Object agentBean = agentIter.next();
				if (agentBean instanceof CommBean) {
					CommBean cb = (CommBean) agentBean;
					endPointList.add(cb.getAddress());
				}
			}
		}
		updateEndpointCombo(endPointList);
	}

	/**
	 * Setzt das ComboModel mit neuen Werten.
	 * 
	 * @param agentList eine Liste von agentenstubs
	 */
	public void updateEndpointCombo(List<IEndPoint> agentList) {
		int selectedIndex = (_destAddressCombo.getSelectedIndex() >= 0) ? _destAddressCombo.getSelectedIndex() : 0;
		_destAddressCombo.setModel(new DefaultComboBoxModel(agentList.toArray(new IEndPoint[0])));
		_destAddressCombo.setSelectedIndex(selectedIndex);
	}

	public IEndPoint getDestinationEndPoint() {
		if (_destAddressCombo.getSelectedItem() != null) {
			return (IEndPoint) _destAddressCombo.getSelectedItem();
		}
		return (IEndPoint) _destAddressCombo.getItemAt(0);
	}

	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if (src == _sendButton) {
			IEndPoint recipient = getDestinationEndPoint();
			String msgContent = _msgTf.getText() + "[" + _endpoint.toString() + "]";
			IJiacMessage jiacMsg = JiacMessageFactory.createJiacMessage("QueuePing", msgContent, _endpoint, recipient, null);
			_commBean.send(jiacMsg, recipient.toString());
			// _commBean.publish("TopicOP", msgContent+".. im Topic", recipient);
		} else if (src == _refreshButton) {
			updateGui();
		} else if (src == _clearButton) {
			_incomingQueueMessageArea.setText("");
		} else if (src == _destAddressCombo) {}
	}

	public void messageReceivedFromQueue(Message message) {
		System.out.println("Message im AgentPanel gekriegt.." + message.toString());
		IJiacMessage jiacMsg = Util.extractJiacMessage(message);
		_incomingQueueMessageArea.append(jiacMsg.toString()+"\n");
	}

	public void messageReceivedFromTopic(Message message) {
		System.out.println("Message im AgentPanel gekriegt.." + message.toString());
		IJiacMessage jiacMsg = Util.extractJiacMessage(message);
		_incomingTopicMessageArea.append(jiacMsg.toString()+"\n");
	}
	
	public CommBean getCommBean() {
		return _commBean;
	}

	public void setCommBean(CommBean commBean) {
		_commBean = commBean;
	}

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

}
