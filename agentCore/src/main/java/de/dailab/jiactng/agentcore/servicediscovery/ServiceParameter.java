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
	String _id; // für hibernate eingeführt.. wegen bugs in hibernate isses n string; "autincrement spalten werden im sqlstatement trotzdem aufgeführt.--> fehler"
	// achtung.. equals/hashCode-methode beachtet id nicht.

	// gibt an, ob es ein Array ist, 
	// 0: einfacher datentyp; z.b. Object
	// 1: es ist ein Array des Datentyps; z.b. Object[] 
	// 2: es ist ein 2Dimensionales Array des Datentyps, z.b. Object[][] 
	// usw.
	int _arrayDimension;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public ServiceParameter(String type, String name) {
		setName(name);
		setType(type);
	}

	// default constructor.. für hibernate
	public ServiceParameter() {
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

	public int getArrayDimension() {
		return _arrayDimension;
	}

	public void setArrayDimension(int arrayDimension) {
		_arrayDimension = arrayDimension;
	}

	public String toString() {
		return '[' + _type+'('+_arrayDimension+')' + ": " + _name + ']';
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + _arrayDimension;
		result = PRIME * result + ((_name == null) ? 0 : _name.hashCode());
		result = PRIME * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ServiceParameter other = (ServiceParameter) obj;
		if (_arrayDimension != other._arrayDimension)
			return false;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		return true;
	}

}
