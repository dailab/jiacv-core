package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Klasse zum Beschreiben eines Agenten. Sie enthuelt also META-Infos ueber den Agenten.
 * Es ist nicht der Agent selbst.
 * @author janko
 * @author axle
 */
public class AgentDescription implements IFact {
	private static final long serialVersionUID = -6931826359293880734L;

	/** Agent IDentifier.*/
	private String aid;
	
	/** Agent name. */
	private String name;
	
	/** Agent's state. */
	private String state;
	
	/** Kommunikation Identifier. */
	private IEndPoint endpoint;
	
	public AgentDescription(String aid, String name, String state, IEndPoint endpoint) {
		this.aid=aid;
		this.name=name;
		this.state=state;
		this.endpoint=endpoint;
	}

	/**
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * @param aid the aid to set
	 */
	public void setAid(String aid) {
		this.aid = aid;
	}

	/**
	 * @return the name
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
	 * @return the state
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

	/**
	 * @return the endpoint
     * 
     * TODO: vielleicht geht das hier noch schicker
	 */
	public IEndPoint getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint the endpoint to set
	 */
	public void setEndpoint(IEndPoint endpoint) {
		this.endpoint = endpoint;
	}

    @Override
    public String toString() {
        StringBuilder builder= new StringBuilder();

        // name
        builder.append("Agent:\n name=");
        if (name != null) {
        	builder.append("'").append(name).append("'");
        } else {
        	builder.append("null");
        }

        // aid
        builder.append("\n aid=");
        if (aid != null) {
        	builder.append("'").append(aid).append("'");
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

        // endpoint
        if (endpoint != null) {
        	// local
        	builder.append("\n endpoint(local)=");
        	if (endpoint.getLocalId() != null) {
        		builder.append("'").append(endpoint.getLocalId()).append("'");
        	} else {
            	builder.append("null");        		
        	}
        	// universal
        	builder.append("\n endpoint(global)=");
        	if (endpoint.getUniversalId() != null) {
        		builder.append("'").append(endpoint.getUniversalId()).append("'");
        	} else {
            	builder.append("null");
        	}
        } else {
        	builder.append("\n endpoint=null");
        }

        builder.append('\n');
        return builder.toString();
    }
}
