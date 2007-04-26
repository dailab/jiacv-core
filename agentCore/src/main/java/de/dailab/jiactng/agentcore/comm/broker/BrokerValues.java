package de.dailab.jiactng.agentcore.comm.broker;

import de.dailab.jiactng.agentcore.comm.Util;

/**
 * Kapselt Werte, die einen ActiveMQ-Broker steuern. Mit diesen Werten kann dann ein embedded Broker erzeugt/gestartet
 * werden. <br>
 * Idee: Als Brokernamen den AgentNodeNamen nehmen ?? <br>
 * Zum starten eine Brokers muss u.a. die Url gesetzt sein, diese kann entweder gesetzt werden, oder aus den Properties
 * Port und Protocol erzeugt werden - in diesem Fall wird die lokale Ip ermittelt und zur erzeugung der Url verendet.
 * 
 * @author janko
 */
public class BrokerValues {

	public static final String PROTOCOL_IP_SEPARATOR = "://";
	public static final char IP_PORT_SEPARATOR = ':';

	String _name;
	String _url;
	String _discoveryMethod;
	String _discoveryAddress;
	boolean _persistent;
	boolean _jmx;
	String _port;
	String _protocol;

	public BrokerValues() {}

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
	 * Erzeugt eine Instanz mit defaultwerten.. um einfach einen Broker erzeugen zu können.
	 * 
	 * @return
	 */
	public static BrokerValues getDefaultInstance() {
		BrokerValues values = new BrokerValues("TngBroker", "tcp://localhost:61616", "multicast", "239.255.2.45:5555", true);
		values.setProtocol("tcp");
		values.setPort("61616");
		values.setUrlFromPortAndProtocol();
		return values;
	}

	public boolean isPersistent() {
		return _persistent;
	}

	public void setPersistent(boolean persistent) {
		_persistent = persistent;
	}

	public String getPort() {
		return _port;
	}

	public void setPort(String port) {
		_port = port;
	}

	public String getProtocol() {
		return _protocol;
	}

	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	/**
	 * Erzeugt die Url aus Port, Protocol und ermittelter eigener IpAdresse, und setzt die Url neu. Nur wenn Port und
	 * Protocol gesetzt wurde, sonst passiert nichts.
	 */
	public void setUrlFromPortAndProtocol() {
		if (getPort() != null && getProtocol() != null) {
			setUrl(createUrl());
		}
	}

	/**
	 * Erzeugt einen Url String der Form <Protocol>://<IP>:<Port>, z.B. tcp://localhost:60606 oder
	 * tcp://192.168.2.1:10000
	 * 
	 * @return die Url als String
	 */
	public String createUrl() {
		String ipStr = Util.getLocalIp();
		return _protocol + PROTOCOL_IP_SEPARATOR + ipStr + IP_PORT_SEPARATOR + _port;
	}
}
