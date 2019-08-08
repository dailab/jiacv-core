package de.dailab.jiactng.agentcore.group;

import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;

public interface IAgentGroup {

	/**
	 * Returns the name of the group.
	 * 
	 * @return the name of the group
	 */
	String getName();

	/**
	 * Sets the name of the group.
	 * 
	 * @param name the name of the group to set
	 */
	void setName(String name);

	void addMember(IAgent member);

	/**
	 * TODO remove from staffing and reduce cardinality
	 * @param member
	 * @return
	 */
	boolean removeMember(IAgent member);

	List<IAgent> getMembers();

	void setMembers(List<IAgent> members);

	List<AgentRoleCardinality> getStructure();

	void setStructure(List<AgentRoleCardinality> structure);

	List<Staffing> getStaff();

	void setStaff(List<Staffing> staff);

}