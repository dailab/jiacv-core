package de.dailab.jiactng.agentcore.comm;

/**
 * Kapselt Werte, die einen ActiveMQ-Broker steuern. Mit diesen Werten kann dann
 * ein embedded Broker erzeugt/gestartet werden.
 * 
 * @author janko
 */
public class BrokerValues {

	String _name;
	String _url;
	String _discoveryMethod;
	String _discoveryAddress;
	boolean _persistent;
	boolean _jmx;

	public BrokerValues() {		
	}
	
	public BrokerValues(String name, String url, String discoveryMethod, String discoveryAddress, boolean jmx) {
		setName(name);
		setDiscoveryAddress(discoveryAddress);
		setDiscoveryMethod(discoveryMethod);
		setUrl(url);
		setJmx(jmx);
	}

	public String getDiscoveryAddress() {
		return _discoveryAddress;
	}

	public void setDiscoveryAddress(String discoveryAddress) {
		_discoveryAddress = discoveryAddress;
	}

	public String getDiscoveryMethod() {
		return _discoveryMethod;
	}

	public void setDiscoveryMethod(String discoveryMethod) {
		_discoveryMethod = discoveryMethod;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		_url = url;
	}

	public boolean isJmx() {
		return _jmx;
	}

	public void setJmx(boolean jmx) {
		_jmx = jmx;
	}

	/**
	 * Erzeugt eine Instanz mit defaultwerten..
	 * um einfach einen Broker erzeugen zu können.
	 * @return
	 */
	public static BrokerValues getDefaultInstance() {
		BrokerValues values = new BrokerValues("TngBroker", "tcp://localhost:61616", "multicast", "239.255.2.45:5555", true);
		return values;
	}

	public boolean isPersistent() {
		return _persistent;
	}

	public void setPersistent(boolean persistent) {
		_persistent = persistent;
	}
	

}
