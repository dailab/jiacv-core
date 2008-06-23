package de.dailab.jiactng.agentcore.comm.wp;

import java.util.Set;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBeanMBean;

/**
 * JMX-based management interface of the directory bean.
 * @author Jan Keiser
 */
public interface DirectoryAgentNodeBeanMBean extends AbstractAgentNodeBeanMBean {

	/**
	 * Sets the refreshing interval. After this interval the space will be checked for old actions,
	 * for each of this actions a message will be send to the agent providing it,
	 * which will sent back a message with the actions provided to refresh them.
	 * When the next interval begins all actions that weren't refreshed will be removed.
	 * 
	 * @param intervall time in milliseconds
	 */
	public void setRefreshingIntervall(long intervall);

	/**
	 * Gets the interval in milliseconds after which action entries will be refreshed
	 * and their presence on the providing agents will be checked.
	 * 
	 * @return interval in milliseconds
	 */
	public long getRefreshingIntervall();

	/**
	 * Sets the first time (in milliseconds) a refreshment of actions stored 
	 * within the directory will be commenced. Can be different from 
	 * refreshing interval given with the other setter.
	 * 
	 * @param firstRefresh the first time of refreshment
	 */
	public void setFirstRefresh(long firstRefresh);

	/**
	 * Gets the first time (in milliseconds) a refreshment of actions stored 
	 * within the directory will be commenced. Can be different from 
	 * refreshing interval given with the other getter.
	 * 
	 * @return firstRefresh the first time of refreshment
	 */
	public long getFirstRefresh();

	/**
	 * Sets the interval after which the directory will ping all agents stored within it
	 * to check if they are still alive.
	 * 
	 * @param agentPingIntervall time in milliseconds
	 */
	public void setAgentPingIntervall(long agentPingIntervall);

	/**
	 * Gets the interval after which the directory will ping all agents stored within it
	 * to check if they are still alive.
	 * 
	 * @return time in milliseconds
	 */
	public long getAgentPingIntervall();

	/**
	 * Sets the interval after which changes are propagated to the other nodes.
	 * This interval is used for "alive detection" of other agent nodes too. If there will be no message from another
	 * agent node within two times this interval the agent node will be removed from this directory with all entries
	 * of agents or actions from it.
	 *  
	 * @param cpInterval interval in milliseconds
	 */
	public void setChangePropagateInterval(long cpInterval);

	/**
	 * Gets the interval after which changes are propagated to the other nodes.
	 * This interval is used for "alive detection" of other agent nodes too. If there will be no message from another
	 * agent node within two times this interval the agent node will be removed from this directory with all entries
	 * of agents or actions from it.
	 * 
	 * @return interval in milliseconds
	 */
	public long getChangePropagateInterval();

	/**
	 * Sets if incoming entries from other agent nodes will be stored or ignored.
	 * <b>IMPORTANT</b>: if set to false during <b>runtime</b> all nonlocal entries will be removed from the directory!
	 * 
	 * @param isActive if <code>true</code>, incoming entries will be cached within local directory
	 */
	public void setCacheIsActive(boolean isActive);

	/**
	 * Checks if incoming entries from other agent nodes are stored or ignored.
	 * 
	 * @return <code>true</code>, if incoming entries are cached within local directory
	 */
	public boolean getCacheIsActive();

	/**
	 * Information about the facts stored in the directory memory.
	 * @return information about facts stored in directory memory
	 */
	public CompositeData getSpace();

	/**
	 * Checks if the message transport is active.
	 * @return <code>true</code>, if the message transport is active
	 */
	public boolean isMessageTransportActive();

	/**
	 * Gets the identifier of the used message transport.
	 * @return identifier of the message transport
	 */
	public String getMessageTransportIdentifier();

	/**
	 * Gets the UUIDs of the other agent nodes which directory content is cached in the local directory.
	 * @return the set of UUIDs
	 */
	public Set<String> getOtherNodes();
}
