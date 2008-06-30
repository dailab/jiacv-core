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
	 * for each agent holding an possible obsolete action a message will be sent to him, requesting
	 * him to refresh his Actions within the Directory. 
	 * When the next interval begins all actions that havn't got a refreshment are erased.
	 * 
	 * Default: 2000 milliseconds
	 *
	 * @param interval time in milliseconds
	 */
	public void setRefreshingInterval(long intervall);

	/**
	 * Gets the interval in milliseconds after which action entries will be refreshed
	 * and their presence on the providing agents will be checked.
	 * 
	 * @return interval in milliseconds
	 */
	public long getRefreshingInterval();

	/**
	 * Sets the first time (in milliseconds) a refreshment of actions stored 
	 * within the directory will be commenced. Can be different from 
	 * refreshing interval given with the other setter.
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @param firstRefresh the first time of refreshment
	 */
	public void setFirstRefresh(long firstRefresh);

	/**
	 * Gets the first time (in milliseconds) a refreshment of actions stored 
	 * within the directory will be commenced. Can be different from 
	 * refreshing interval given with the other getter.
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @return firstRefresh the first time of refreshment
	 */
	public long getFirstRefresh();

	/**
	 * Sets the interval after which the directory will check all local agents stored within it
	 * if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 * 
	 * @param agentPingIntervall time in milliseconds
	 */
	public void setAgentPingInterval(long agentPingIntervall);

	/**
	 * Gets the interval after which the directory will check all local agents stored within it
	 * if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 * 
	 * @return time in milliseconds
	 */
	public long getAgentPingInterval();

	/**
	 * Sets the interval after which changes are propagated to the other nodes. (if instantPropagation == false)
	 * This interval is used for "alive detection" of other agent nodes too. If there will be no message from another
	 * agent node within two times this interval the agent node will be removed from this directory with all entries
	 * of agents or actions from it.
	 *  
	 * Default: 3000 milliseconds
	 *  
	 * @param cpInterval interval in milliseconds
	 */
	public void setChangePropagateInterval(long cpInterval);

	/**
	 * Gets the interval after which changes are propagated to the other nodes. (if instantPropagation == false)
	 * This interval is used for "alive detection" of other agent nodes too. If there will be no message from another
	 * agent node within two times this interval the agent node will be removed from this directory with all entries
	 * of agents or actions from it.
	 * 
 	 * Default: 3000 milliseconds
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
	 * sets the communicationAddress on which all <code>AgentNode</code>s group together and exchange searchRequests and
	 * necessary overhead
	 * 
	 * @param nodes <code>GroupAddress</code> on which all <code>AgentNode</code>s register
	 */
	public void setOtherNodes(String groupName);
	
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
	
	/**
	 * sets if changes should be propagated instantly or a collection of changes should be send every <code>changePropagationInterval</code> ms
	 * 
	 * @param instantPropagation is false by default -> changes will be collected and send as a bundle
	 * 
	 * Note: can be changed during runtime without causing problems
	 */
	public void setInstantPropagation(boolean instantPropagation);
	
	/**
	 * 
	 * @return <code>true</code>, if changes should be propagated instantly to the other <code>AgentNode</code>s.
	 * 
	 * Default: false -> local changes will be buffered and send every <code>changePropagationInterval</code> ms
	 */
	public boolean getInstantPropagation();
	
}
