package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.Date;
import java.util.List;
import java.util.Set;

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

	public Set<String> getKeywords();

	public String getProviderAddress();

	public List<ServiceParameter> getInputParameter();

	public List<ServiceParameter> getOutputParameter();

	public String getPreCondition();

	public String getPostCondition();

	// needed for generic webservice interface
	public String getWsdlDescription();
	
	public boolean isWebService();

	// Inputparameter werden in _einem_ Objekt gekapselt
	public ServiceParameter getInputWrapper();
	
	public String getWsdlUrl();
}
