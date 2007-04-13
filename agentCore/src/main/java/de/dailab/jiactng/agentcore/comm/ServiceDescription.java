package de.dailab.jiactng.agentcore.comm;

import java.io.Serializable;
import java.util.Date;

/**
 * Eine Implementation für die Servicebeschreibung
 * 
 * @author janko
 */
public class ServiceDescription implements IServiceDescription, Serializable {

	Date _expireDate;
	String _id;
	String _name;
	String[] _keywords;
	ServiceParameter[] _inputParams;
	ServiceParameter[] _outputParams;
	String _preCondition;
	String _postCondition;
	String _providerAddress;
	String _qoSRating;

	/**
	 * Full Constructor...
	 * 
	 * @param expireDate
	 * @param id
	 * @param name
	 * @param keywords
	 * @param inputParams
	 * @param outputParams
	 * @param preCondition
	 * @param postCondition
	 * @param providerAddress
	 * @param qoSRating
	 */
	public ServiceDescription(Date expireDate, String id, String name, String[] keywords, ServiceParameter[] inputParams,
			ServiceParameter[] outputParams, String preCondition, String postCondition, String providerAddress,
			String qoSRating) {
		super();
		_expireDate = expireDate;
		_id = id;
		_name = name;
		_keywords = keywords;
		_inputParams = inputParams;
		_outputParams = outputParams;
		_preCondition = preCondition;
		_postCondition = postCondition;
		_providerAddress = providerAddress;
		_qoSRating = qoSRating;
	}

	public Date getExpireDate() {
		return _expireDate;
	}

	public String getId() {
		return _id;
	}

	public ServiceParameter[] getInputParameter() {
		return _inputParams;
	}

	public String[] getKeywords() {
		return _keywords;
	}

	public String getName() {
		return _name;
	}

	public ServiceParameter[] getOutputParameter() {
		return _outputParams;
	}

	public String getPostCondition() {
		return _postCondition;
	}

	public String getPrecondition() {
		return _preCondition;
	}

	public String getProviderAddress() {
		return _providerAddress;
	}

	public String getQoSRating() {
		return _qoSRating;
	}

	public int hashCode() {
		long hashCode = _expireDate.hashCode() + _id.hashCode() + _name.hashCode() + _preCondition.hashCode()
				+ _postCondition.hashCode() + _providerAddress.hashCode() + _qoSRating.hashCode() + _keywords.hashCode()
				+ _inputParams.hashCode() + _outputParams.hashCode();
		return (int) (hashCode / 10);
	}

	/**
	 * 
	 */
	public boolean equals(Object o) {
		if (o != null && o instanceof ServiceDescription) {
			ServiceDescription desc = (ServiceDescription) o;
			boolean _expireDateEquals = false;
			boolean _idEquals = false;
			boolean _nameEquals = false;
			boolean _preEquals = false;
			boolean _postEquals = false;
			boolean _providerEquals = false;
			boolean _qosEquals = false;

			if (_expireDate != null && desc._expireDate != null && _expireDate.equals(desc._expireDate)) {
				_expireDateEquals = true;
			}
			if (_id != null && desc._id != null && _id.equals(desc._id)) {
				_idEquals = true;
			}
			if (_name != null && desc._name != null && _name.equals(desc._name)) {
				_nameEquals = true;
			}
			if (_preCondition != null && desc._preCondition != null && _preCondition.equals(desc._preCondition)) {
				_preEquals = true;
			}
			if (_postCondition != null && desc._postCondition != null && _postCondition.equals(desc._postCondition)) {
				_postEquals = true;
			}
			if (_providerAddress != null && desc._providerAddress != null && _providerAddress.equals(desc._providerAddress)) {
				_providerEquals = true;
			}
			if (_qoSRating != null && desc._qoSRating != null && _qoSRating.equals(desc._qoSRating)) {
				_qosEquals = true;
			}

			if (_expireDateEquals && _idEquals && _nameEquals && _preEquals && _postEquals && _providerEquals && _qosEquals
					&& equalsKeywords(desc._keywords) && equalsServiceParameter(_inputParams, desc._inputParams)
					&& equalsServiceParameter(_outputParams, desc._outputParams)) {
				return true;
			}
		}
		return false;
	}

	private boolean equalsKeywords(String[] keywords) {
		if (keywords != null && _keywords != null && keywords.length == _keywords.length) {
			// achtung es wird auf die reihenfolge geachtet.. das ist evtl. nicht gewünscht			
			for (int i = 0; i < keywords.length; i++) {
				if (!keywords[i].equals(_keywords[i])) {
					// sobald ein element ungleich ist.. wird false geliefert
					return false;
				}
				// alle elemente waren gleich, true liefern
				return true;
			}
		}
		return false;
	}

	private boolean equalsServiceParameter(ServiceParameter[] localParams, ServiceParameter[] params) {
		if (localParams != null && params != null && localParams.length == params.length) {
			for (int i = 0; i < localParams.length; i++) {
				if (!localParams[i].equals(params[i])) {
					// sobald ein element ungleich ist.. wird false geliefert
					return false;
				}
				// alle elemente waren gleich, true liefern
				return true;
			}
		}
		return false;
	}

}
