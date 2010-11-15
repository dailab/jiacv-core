package de.dailab.jiactng.agentcore.group;

import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;

public interface IAgentGroup {

	/**
	 * Returns the name of the group.
	 * 
	 * @return the name of the group
	 */
	public abstract String getName();

	/**
	 * Sets the name of the group.
	 * 
	 * @param name the name of the group to set
	 */
	public abstract void setName(String name);

	/**
	 * {@inheritDoc}
	 */
	public abstract void addMember(IAgent member);

	/**
	 * TODO remove from staffing and reduce cardinality
	 * @param member
	 * @return
	 */
	public abstract boolean removeMember(IAgent member);

	/**
	 * {@inheritDoc}
	 */
	public abstract List<IAgent> getMembers();

	/**
	 * {@inheritDoc}
	 */
	public abstract void setMembers(List<IAgent> members);

	public abstract List<AgentRoleCardinality> getStructure();

	public abstract void setStructure(List<AgentRoleCardinality> structure);

	public abstract List<Staffing> getStaff();

	public abstract void setStaff(List<Staffing> staff);

}