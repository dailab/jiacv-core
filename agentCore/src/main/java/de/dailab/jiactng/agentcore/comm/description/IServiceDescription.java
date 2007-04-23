package de.dailab.jiactng.agentcore.comm.description;

import java.io.Serializable;
import java.util.Date;

/**
 * Ein Interface zur Beschreibung von Services
 * 
 * @author janko
 */
public interface IServiceDescription extends Serializable {

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
}
