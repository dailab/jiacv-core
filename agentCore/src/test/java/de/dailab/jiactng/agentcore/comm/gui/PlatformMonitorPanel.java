package de.dailab.jiactng.agentcore.comm.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.CommMessageListener;
import de.dailab.jiactng.agentcore.comm.IEndPoint;
import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.JiacMessageFactory;
import de.dailab.jiactng.agentcore.comm.Util;

/**
 * Ein panel, das eine CommBean Gui speziell für die PlatfromCommBean darstellt.
 * @author janko
 *
 */
public class PlatformMonitorPanel extends JPanel implements ActionListener, CommMessageListener {
	static String LAYOUT_COLUMNS = "3dlu, left:max(20dlu;pref), 3dlu, left:max(200dlu;pref), 3dlu, left:max(20dlu;pref),3dlu, right:max(20dlu;pref) ";
	static String LAYOUT_ROWS = "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 6dlu";
	FormLayout layout = new FormLayout(LAYOUT_COLUMNS, LAYOUT_ROWS);

	JTextField _msgTf;
	JTextField _destAddressTf;
	JComboBox _destAddressCombo;
	JTextArea _incomingQueueMessageArea;
	JTextArea _incomingTopicMessageArea;
	IEndPoint _endpoint;

	JButton _sendButton;
	JButton _fileButton;
	JButton _refreshButton;
	JButton _clearQueueButton;
	JButton _clearTopicButton;

	CommBean _commBean;

	public PlatformMonitorPanel(CommBean _commBean) {
		super();
		setCommBean(_commBean);
		_commBean.addCommMessageListener(this);
		init(_commBean.getAddress());
	}

	public void init(IEndPoint endpoint) {
		IEndPoint[] epArray = new IEndPoint[]{};
		_endpoint = endpoint;
		_sendButton = new JButton("send");
		_sendButton.addActionListener(this);
		_refreshButton = new JButton("refresh");
		_refreshButton.addActionListener(this);
		_clearQueueButton = new JButton("clear");
		_clearQueueButton.addActionListener(this);
		_clearTopicButton = new JButton("clear");
		_clearTopicButton.addActionListener(this);
		_destAddressTf = new JTextField(20);
		_destAddressCombo = new JComboBox(epArray);
		_destAddressCombo.setRenderer(new EndPointRenderer());
		_destAddressCombo.addActionListener(this);
		_msgTf = new JTextField(20);
		_incomingQueueMessageArea = new JTextArea(6, 60);
		_incomingQueueMessageArea.setLineWrap(true);
		_incomingQueueMessageArea.setWrapStyleWord(true);
		_incomingTopicMessageArea = new JTextArea(6, 60);
		_incomingTopicMessageArea.setLineWrap(true);
		_incomingTopicMessageArea.setWrapStyleWord(true);

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Own Address", cc.xy(2, 2));
		builder.addLabel(_endpoint.toString(), cc.xy(4, 2));

		builder.addLabel("Dest Address", cc.xy(2, 4));
		builder.add(_destAddressCombo, cc.xy(4, 4));		
		builder.add(_refreshButton, cc.xyw(8, 4, 1));

		builder.addLabel("Message", cc.xy(2, 6));
		builder.add(_msgTf, cc.xyw(4, 6, 1));
		builder.add(_sendButton, cc.xyw(8, 6, 1));

		builder.addSeparator("IncomingQueueMessages", cc.xyw(2, 8, 7));
		builder.add(new JScrollPane(_incomingQueueMessageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xyw(2, 10, 7));

		builder.addSeparator("IncomingTopicMessages", cc.xyw(2, 14, 7));
		builder.add(new JScrollPane(_incomingTopicMessageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xyw(2, 16, 7));

		builder.add(_clearQueueButton, cc.xyw(8, 12, 1));

		builder.add(_clearTopicButton, cc.xyw(8, 18, 1));

		this.add(builder.getPanel(), BorderLayout.CENTER);
		this.setPreferredSize(builder.getPanel().getPreferredSize());
		this.invalidate();
	}

	public void messageReceivedFromQueue(Message message) {
		System.out.println("Message im AgentPanel gekriegt.." + message.toString());
		IJiacMessage jiacMsg = Util.extractJiacMessage(message);
		_incomingQueueMessageArea.append(jiacMsg.toString() + "\n");
	}

	public void messageReceivedFromTopic(Message message) {
		System.out.println("Message im AgentPanel gekriegt.." + message.toString());
		IJiacMessage jiacMsg = Util.extractJiacMessage(message);
		_incomingTopicMessageArea.append(jiacMsg.toString() + "\n");
	}

	public CommBean getCommBean() {
		return _commBean;
	}

	public void setCommBean(CommBean commBean) {
		_commBean = commBean;
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
		IEndPoint[] epArray = agentList.toArray(new IEndPoint[0]);
		DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(epArray);
		_destAddressCombo.setModel(defaultModel);
		_destAddressCombo.repaint();
//		_destAddressCombo.setSelectedIndex(selectedIndex);
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
		} else if (src == _fileButton) {
			// show file dialog..
		} else if (src == _clearQueueButton) {
			_incomingQueueMessageArea.setText("");
		} else if (src == _clearTopicButton) {
			_incomingTopicMessageArea.setText("");
		} else if (src == _destAddressCombo) {}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("hihi");
		frame.setSize(1024, 768);
		frame.add(new PlatformMonitorPanel(new CommBean()));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
