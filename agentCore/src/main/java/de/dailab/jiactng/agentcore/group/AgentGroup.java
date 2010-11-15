package de.dailab.jiactng.agentcore.group;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.IAgent;

public class AgentGroup implements IAgentGroup, BeanNameAware {
	private static final long serialVersionUID = 4180693496889986338L;

	private String name;

	List<IAgent> members = new ArrayList<IAgent>();
	List<AgentRoleCardinality> structure = new ArrayList<AgentRoleCardinality>();
	List<Staffing> staff = new ArrayList<Staffing>();
	
	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#addMember(de.dailab.jiactng.agentcore.IAgent)
	 */
	public void addMember(IAgent member) {
		if (!members.contains(member)) {
			members.add(member);
		}
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#removeMember(de.dailab.jiactng.agentcore.IAgent)
	 */
	public boolean removeMember(IAgent member) {
		return members.remove(member);
	}
	
	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#getMembers()
	 */
	public List<IAgent> getMembers() {
		return members;
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#setMembers(java.util.List)
	 */
	public void setMembers(List<IAgent> members) {
		this.members.clear();
		this.members.addAll(members);
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#getStructure()
	 */
	public List<AgentRoleCardinality> getStructure() {
		return structure;
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#setStructure(java.util.List)
	 */
	public void setStructure(List<AgentRoleCardinality> structure) {
		this.structure.clear();
		this.structure.addAll(structure);
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#getStaff()
	 */
	public List<Staffing> getStaff() {
		return staff;
	}

	/* (non-Javadoc)
	 * @see de.dailab.jiactng.agentcore.group.IAgentGroup#setStaff(java.util.List)
	 */
	public void setStaff(List<Staffing> staff) {
		this.staff.clear();
		this.staff.addAll(staff);
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}
}
