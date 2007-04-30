package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.Date;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Ein Interface zur Beschreibung von Services
 * 
 * @author janko
 */
public interface IServiceDescription extends IFact {
	
	public String getName();

	public String getId();

	/* Quality of Service Rating */
	public String getQoSRating();

	public Date getExpireDate();

	public String[] getKeywords();

	public String getProviderAddress();

	public ServiceParameter[] getInputParameter();

	public ServiceParameter[] getOutputParameter();

	public String getPrecondition();

	public String getPostCondition();

	// needed for generic webservice interface
	public String getWsdlDescription();
	
	public boolean isWebService();

}
