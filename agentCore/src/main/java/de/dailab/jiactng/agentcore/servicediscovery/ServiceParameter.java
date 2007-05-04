package de.dailab.jiactng.agentcore.servicediscovery;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Eine Klasse, die einen Parameter darstellt
 * 
 * @author janko
 */
public class ServiceParameter implements IFact {

	String _type;
	String _name;

	public ServiceParameter(String type, String name) {
		setName(name);
		setType(type);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	public String toString() {
		return _type + ": " + _name;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof ServiceParameter) {
			ServiceParameter sp = (ServiceParameter) o;
			if (_name != null && sp._name != null && _type != null && sp._type != null && _name.equals(sp._name)
																							&& _type.equals(sp._type)) {
				return true;
			}
		}
		return false;
	}

	// durchschnitt der beiden hashcodes bilden
	public int hashCode() {
		return (_name.hashCode() + _type.hashCode()) / 2;
	}

}
