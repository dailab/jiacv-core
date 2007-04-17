package de.dailab.jiactng.agentcore.comm.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.dailab.jiactng.agentcore.comm.CommBean;

/**
 * Fasst EIN PlatfromMonitorPanel und mehrere AgentPanels in einer TabbedPaneKomponente zusammen
 */
public class CommMonitorPanel extends JPanel {

	CommBean _platformCommBean;
	List<CommBean> _agentCommBeanList;
	JTabbedPane _tabbedPane;

	public CommMonitorPanel() {
		super();
		setLayout(new BorderLayout());
		_agentCommBeanList = new ArrayList<CommBean>();
		_tabbedPane = new JTabbedPane();
	}

	/**
	 * muss aufgerufen werden, nachdem die CommBeans gesetzt wurden.
	 */
	public void doInit() {
		this.add(_tabbedPane, BorderLayout.CENTER);
		initTabbedPane();
	}

	private void initTabbedPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new PlatformMonitorPanel(_platformCommBean), BorderLayout.CENTER);
		_tabbedPane.addTab("Platform", null, panel, "Kommunikation der Platformen");
		for (Iterator iter = _agentCommBeanList.iterator(); iter.hasNext();) {
			CommBean agentCommBean = (CommBean) iter.next();
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(new AgentPanel(agentCommBean), BorderLayout.CENTER);
			_tabbedPane.addTab(agentCommBean.getBeanName(), null, panel, "Kommunikation des Agenten "
																							+ agentCommBean.getBeanName());
		}
	}

	public CommBean getPlatformCommBean() {
		return _platformCommBean;
	}

	public void setPlatformCommBean(CommBean platformCommBean) {
		_platformCommBean = platformCommBean;
	}

	public boolean add(CommBean agentCommBean) {
		return _agentCommBeanList.add(agentCommBean);
	}

	public boolean remove(CommBean agentCommBean) {
		return _agentCommBeanList.remove(agentCommBean);
	}

	public static void main(String[] args) {
		CommMonitorPanel cmp = new CommMonitorPanel();
		cmp.setPlatformCommBean(new CommBean());
		cmp.add(new CommBean());
		cmp.add(new CommBean());
		cmp.doInit();
		QnDFrame frame = new QnDFrame(cmp);
		frame.setVisible(true);
	}

	public List<CommBean> getAgentCommBeanList() {
		return _agentCommBeanList;
	}

	public void setAgentCommBeanList(List<CommBean> agentCommBeanList) {
		_agentCommBeanList = agentCommBeanList;
	}
}
