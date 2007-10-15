package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Klasse zum Beschreiben einer AgentBean. Sie enthaelt die 
 * Meta-Infos zur AgentBean. Es ist nicht die Beans selbst.
 * @author axle
 */
public class AgentBeanDescription implements IFact {
	private static final long serialVersionUID = -6851071975088826943L;

	/** The name of the agentbean in this context. */
	private String name;
	
	/** The current state of the agentbean. */
	private String state;

	public AgentBeanDescription(String name, String state) {
		this.name = name;
		this.state = state;
	}

	/**
	 * @return the name of the agentbean
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the current state of the agentbean 
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	
    @Override
    public String toString() {
        StringBuilder builder= new StringBuilder();

        // name
        builder.append("AgentBean:\n name=");
        if (name != null) {
        	builder.append("'").append(name).append("'");
        } else {
        	builder.append("null");
        }

        // state
        builder.append("\n state=");
        if (state != null) {
        	builder.append("'").append(state).append("'");
        } else {
        	builder.append("null");
        }

        builder.append('\n');
        return builder.toString();
    }	
}
