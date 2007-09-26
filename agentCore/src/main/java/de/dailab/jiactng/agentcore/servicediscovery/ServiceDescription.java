package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Eine Implementation für die Servicebeschreibung Wenn wsdl-Beschreibung null
 * ist, wird davon ausgegangen, dass es kein WebService ist.
 * @TODO equals, hashCode nachziehen
 * @author janko
 */
public class ServiceDescription implements IServiceDescription {

	private Date _expireDate;
	private String _id;  // soll eigentlich ordinal sein, aber momentan bug in hibernate.. lieber string nehmen
	private String _name;
	private Set<String> _keywords;
	private List<ServiceParameter> _inputParams;
	/** wenn null, gibts keine returnwerte	 */
	private List<ServiceParameter> _outputParams;
	private String _preCondition;
	private String _postCondition;
	private String _providerAddress;
	private String _qoSRating;
	private String _wsdl;
	private ServiceParameter _inputWrapper;

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
	 * @param wsdl wsdl beschreibung
	 */
	public ServiceDescription(Date expireDate, String id, String name, Set<String> keywords,
			List<ServiceParameter> inputParams, List<ServiceParameter> outputParams, String preCondition,
			String postCondition, String providerAddress, String qoSRating, String wsdl) {
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
		_wsdl = wsdl;
	}

	// default constructor.. extra für hibernate
	public ServiceDescription() {
		_keywords = new HashSet<String>();
	}
	
	
	public Date getExpireDate() {
		return _expireDate;
	}

	public String getId() {
		return _id;
	}

	public List<ServiceParameter> getInputParameter() {
		return _inputParams;
	}

	public Set<String> getKeywords() {
		return _keywords;
	}

	public String getName() {
		return _name;
	}

	public List<ServiceParameter> getOutputParameter() {
		return _outputParams;
	}

	public String getPostCondition() {
		return _postCondition;
	}

	public String getPreCondition() {
		return _preCondition;
	}

	public String getProviderAddress() {
		return _providerAddress;
	}

	public String getQoSRating() {
		return _qoSRating;
	}

	public String getWsdlDescription() {
		return _wsdl;
	}

	public boolean isWebService() {
		return _wsdl != null && _wsdl.length() > 0;
	}

	public int hashCode() {
		long hashCode = 0;
		int cnt = 1; // einfach mal mit 1 initialisieren, um div0 zu verhindern
		if (_expireDate != null) {
			hashCode += _expireDate.hashCode();
			cnt++;
		}
		if (_id != null) {
			hashCode += _id.hashCode();
			cnt++;
		}
		if (_name != null) {
			hashCode += _name.hashCode();
			cnt++;
		}
		if (_preCondition != null) {
			hashCode += _preCondition.hashCode();
			cnt++;
		}
		if (_postCondition != null) {
			hashCode += _postCondition.hashCode();
			cnt++;
		}
		if (_providerAddress != null) {
			hashCode += _providerAddress.hashCode();
			cnt++;
		}
		if (_qoSRating != null) {
			hashCode += _qoSRating.hashCode();
			cnt++;
		}
		if (_keywords != null) {
			hashCode += _keywords.hashCode();
			cnt++;
		}
		if (_inputParams != null) {
			hashCode += _inputParams.hashCode();
			cnt++;
		}
		if (_outputParams != null) {
			hashCode += _outputParams.hashCode();
			cnt++;
		}
		if (_wsdl != null) {
			hashCode += _wsdl.hashCode();
			cnt++;
		}
		return (int) (hashCode / cnt);
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
			boolean _wsdlEquals = false;

			if (_expireDate != null && desc._expireDate != null && _expireDate.equals(desc._expireDate)) {
				_expireDateEquals = true;
			} else if (_expireDate == null && desc._expireDate == null) {
				_expireDateEquals = true;
			}
			if (_id != null && desc._id != null && _id.equals(desc._id)) {
				_idEquals = true;
			} else if (_id == null && desc._id == null) {
				_idEquals = true;
			}
			if (_name != null && desc._name != null && _name.equals(desc._name)) {
				_nameEquals = true;
			} else if (_name == null && desc._name == null) {
				_nameEquals = true;
			}
			if (_preCondition != null && desc._preCondition != null && _preCondition.equals(desc._preCondition)) {
				_preEquals = true;
			} else if (_preCondition == null && desc._preCondition == null) {
				_preEquals = true;
			}
			if (_postCondition != null && desc._postCondition != null && _postCondition.equals(desc._postCondition)) {
				_postEquals = true;
			} else if (_postCondition == null && desc._postCondition == null) {
				_postEquals = true;
			}
			if (_providerAddress != null && desc._providerAddress != null && _providerAddress.equals(desc._providerAddress)) {
				_providerEquals = true;
			} else if (_providerAddress == null && desc._providerAddress == null) {
				_providerEquals = true;
			}
			if (_qoSRating != null && desc._qoSRating != null && _qoSRating.equals(desc._qoSRating)) {
				_qosEquals = true;
			} else if (_qoSRating == null && desc._qoSRating == null) {
				_qosEquals = true;
			}
			if (_wsdl != null && desc._wsdl != null && _wsdl.equals(desc._wsdl)) {
				_wsdlEquals = true;
			} else if (_wsdl == null && desc._wsdl == null) {
				_wsdlEquals = true;
			}

			if (_expireDateEquals && _idEquals && _nameEquals && _preEquals && _postEquals && _providerEquals && _qosEquals
					&& _wsdlEquals && equalsKeywords(desc._keywords) && equalsServiceParameter(_inputParams, desc._inputParams)
					&& equalsServiceParameter(_outputParams, desc._outputParams)) {
				return true;
			}
		}
		return false;
	}

	// private boolean equalsKeywords(String[] keywords) {
	// if (keywords != null && _keywords != null && keywords.length ==
	// _keywords.length) {
	// // achtung es wird auf die reihenfolge geachtet.. das ist evtl. nicht
	// gewünscht
	// for (int i = 0; i < keywords.length; i++) {
	// if (!keywords[i].equals(_keywords[i])) {
	// // sobald ein element ungleich ist.. wird false geliefert
	// return false;
	// }
	// // alle elemente waren gleich, true liefern
	// return true;
	// }
	// }
	// if (keywords == null && _keywords == null) {
	// return true;
	// }
	// return false;
	// }

	private boolean equalsKeywords(Set<String> keywords) {
		if (keywords != null && _keywords != null && keywords.size() == _keywords.size()) {
			for (String keyword : keywords) {
				if (!_keywords.contains(keyword)) {
					return false;
				}
			}
			// alle elemente waren gleich, true liefern
			return true;
		}
		if (keywords == null && _keywords == null) {
			return true;
		}
		return false;
	}
	
	private boolean equalsServiceParameter(List<ServiceParameter> thisParams, List<ServiceParameter> params) {
		if (thisParams != null && params != null && thisParams.size() == params.size()) {			
			for (ServiceParameter param : thisParams) {
				if (!thisParams.contains(param)) {
					// sobald ein element ungleich ist.. wird false geliefert
					return false;
				}
			}
			// alle elemente waren gleich, true liefern
			return true;
		}
		if (thisParams == null && params == null) {
			return true;
		}
		return false;
	}

	public void setExpireDate(Date expireDate) {
		_expireDate = expireDate;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setInputParameter(List<ServiceParameter> inputParams) {
		_inputParams = inputParams;
	}

	public void setKeywords(Set<String> keywords) {
		_keywords = keywords;
	}

	public void setName(String name) {
		_name = name;
	}

	/**
	 * leere Liste, oder Liste nur mit nulls wird ignoriert; -> die outputParams werden auf null gesetzt
	 * @param outputParams
	 */
	public void setOutputParameter(List<ServiceParameter> outputParams) {
		if (containsNullOnly(outputParams)) {
			_outputParams = null;
		} else {
			_outputParams = outputParams;
		}
	}

	public void setPostCondition(String postCondition) {
		_postCondition = postCondition;
	}

	public void setPreCondition(String preCondition) {
		_preCondition = preCondition;
	}

	public void setProviderAddress(String providerAddress) {
		_providerAddress = providerAddress;
	}

	public void setQoSRating(String qoSRating) {
		_qoSRating = qoSRating;
	}

	public void setWsdlDescription(String wsdl) {
		_wsdl = wsdl;
	}

	public ServiceParameter getInputWrapper() {
		return _inputWrapper;
	}

	public void setInputWrapper(ServiceParameter wrapper) {
		_inputWrapper = wrapper;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[').append(_name).append(',')
		.append(_expireDate).append(',')
		.append("[keywords:").append(Arrays.toString(_keywords.toArray(new String[0]))).append(']').append(',')
				.append("[InputParams:").append(Arrays.toString(_inputParams.toArray(new ServiceParameter[0]))).append(']').append(',')
    .append("[OutputParams:").append(Arrays.toString(_outputParams.toArray(new ServiceParameter[0]))).append(']').append(',')
    .append(_providerAddress).append(',')
    .append(_qoSRating).append(',')
		.append("Webservice?:").append(isWebService()).append(']');
		return sb.toString();
	}
	
	private static boolean containsNullOnly(List<?> list) {
		for(Object o : list) {
			if (o!= null) {
				return false;
			}
		}
		return true;
	}
}
