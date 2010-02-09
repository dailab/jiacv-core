package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

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
	private transient String state;

	/**
	 * Creates the description of an agent bean.
	 * @param name the name of the agent bean
	 * @param state the life-cycle state of the agent bean
	 */
	public AgentBeanDescription(String name, String state) {
		this.name = name;
		this.state = state;
	}

	/**
	 * Get the name of the agent bean.
	 * @return the name of the agent bean
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the agent bean.
	 * @param newName the name to set
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Get the life-cycle state of the agent bean.
	 * @return the current life-cycle state of the agent bean 
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public String getState() {
		return state;
	}

	/**
	 * Set the life-cycle state of the agent bean
	 * @param newState the life-cycle state to set
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public void setState(String newState) {
		state = newState;
	}

	/**
	 * Checks the equality of two agent bean descriptions. The descriptions are equal
	 * if their names are equal.
	 * @param obj the other agent bean description
	 * @return the result of the equality check
	 * @see EqualityChecker#equals(Object, Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj == null || !(obj instanceof AgentBeanDescription)) {
            return false;
        }

        AgentBeanDescription other= (AgentBeanDescription) obj;
        return EqualityChecker.equals(name, other.name);
    }

    /**
	 * Returns the hash code by calculation from this class and the agent bean name, 
	 * thus it is the same hash code for all agent bean descriptions with the same name.
	 * @return the calculated hash code
     */
    @Override
    public int hashCode() {
        return AgentBeanDescription.class.hashCode() ^ (name != null ? name.hashCode() : 0);
    }

    /**
	 * Returns a multiline text which contains the name and life-cycle state
	 * of the agent bean description.
	 * @return a string representation of the agent bean description
     */
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
