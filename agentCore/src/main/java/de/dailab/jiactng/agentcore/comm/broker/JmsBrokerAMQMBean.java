package de.dailab.jiactng.agentcore.comm.broker;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

public interface JmsBrokerAMQMBean extends AbstractLifecycleMBean {

	public void addConnector(String bindAddress);
	
	public void addNetworkConnector(String discoveryAddress);

	public void addProxyConnector(String bindAddress);

	public String getMasterConnectorURI();

	public void setMasterConnectorURI(String masterConnectorURI);

	public boolean getSlave();

	public boolean getStarted();

	public String getBrokerName();

	public void setBrokerName(String brokerName);

	public String getDataDirectory();

	public void setDataDirectory(String dataDirectory);

	public String getTmpDataDirectory();

	public void setTmpDataDirectory(String tmpDataDirectory);

	public boolean getPersistent();

	public void setPersistent(boolean persistent);

	public boolean getPopulateJMSXUserID();

	public void setPopulateJMSXUserID(boolean populateJMSXUserID);

	public boolean getUseJmx();

	public void setUseJmx(boolean useJmx);

	public String getBrokerObjectName();

	public void setBrokerObjectName(String brokerObjectName);

	public String[] getNetworkConnectorURIs();

	public void setNetworkConnectorURIs(String[] networkConnectorURIs);

	public String[] getTransportConnectorURIs();

	public void setTransportConnectorURIs(String[] transportConnectorURIs);

	public boolean getUseLoggingForShutdownErrors();

	public void setUseLoggingForShutdownErrors(boolean useLoggingForShutdownErrors);

	public boolean getUseShutdownHook();

	public void setUseShutdownHook(boolean useShutdownHook);

	public boolean getAdvisorySupport();

	public void setAdvisorySupport(boolean advisorySupport);

	public void deleteAllMessages();

	public boolean getDeleteAllMessagesOnStartup();

	public void setDeleteAllMessagesOnStartup(boolean deleteAllMessagesOnStartup);

	public String getVmConnectorURI();

	public void setVmConnectorURI(String vmConnectorURI);

	public boolean getShutdownOnMasterFailure();

	public void setShutdownOnMasterFailure(boolean shutdownOnMasterFailure);

	public boolean getKeepDurableSubsActive();

	public void setKeepDurableSubsActive(boolean keepDurableSubsActive);

	public boolean getUseVirtualTopics();

	public void setUseVirtualTopics(boolean useVirtualTopics);

	public int getPersistenceThreadPriority();

	public void setPersistenceThreadPriority(int persistenceThreadPriority);
}
