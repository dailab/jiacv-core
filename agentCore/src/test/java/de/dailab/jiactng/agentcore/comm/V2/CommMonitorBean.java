package de.dailab.jiactng.agentcore.comm.V2;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.gui.CommMonitorPanel;
import de.dailab.jiactng.agentcore.comm.gui.QnDFrame;

public class CommMonitorBean extends AbstractAgentBean {
	CommMonitorPanel _commMonitorPanel;
	QnDFrame _frame;

	private List<CommBean> _commBeanList = null;
	CommBean _platformCommBean;
	
	public CommMonitorBean() {
		super();
		_commMonitorPanel = new CommMonitorPanel();
		_commBeanList = new ArrayList<CommBean>();
	}

	public void springInit() {
		// commbeans müssen schon gesetzt sein am monitorPanel..
		_commMonitorPanel.doInit();
		_frame = new QnDFrame(_commMonitorPanel);
		_frame.setVisible(true);
	}
	
	public void execute() {
	}

	public List<CommBean> getCommBeanList() {
		return _commMonitorPanel.getAgentCommBeanList();
	}

	public void setCommBeanList(List<CommBean> commBeanList) {
		_commMonitorPanel.setAgentCommBeanList(commBeanList);
	}

	public CommBean getPlatformCommBean() {
		return _commMonitorPanel.getPlatformCommBean();
	}

	public void setPlatformCommBean(CommBean platformCommBean) {
		_commMonitorPanel.setPlatformCommBean(platformCommBean);
	}
	

}
