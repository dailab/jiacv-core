package de.dailab.jiactng.agentcore.comm.V2;

import javax.swing.JFrame;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.gui.AgentPanel;

public class TestPlatformCommunication extends AbstractAgentBean {
	JFrame _frame;
	CommBean _commBean;
	AgentPanel _agentPanel;
	
	int _counter = 0;
	int _timer = 10;
	
	public TestPlatformCommunication() {
		_frame = createFrame();
	}

	public JFrame createFrame() {
		JFrame frame = new JFrame(this.getClass().getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 768);
		return frame;
	}

	public void execute() {
//		_counter++;
//		if (_timer == _counter) {
//			_counter = 0;
//			_agentPanel.updateGui();
//		}
	}

	public CommBean getCommBean() {
		return _commBean;
	}

	public void setCommBean(CommBean commBean) {		
		_commBean = commBean;
		_agentPanel = new AgentPanel(commBean);
		// System.out.println("AgentName:"+this.thisAgent.getAgentName());
		System.out.println("BeanName:" + this.getBeanName());
		
		_frame.getContentPane().removeAll();
		_frame.getContentPane().add(_agentPanel);
		_frame.pack();
		_frame.setVisible(true);
	}
}
