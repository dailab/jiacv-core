package de.dailab.jiactng.agentcore.comm.broker;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Kapselt Werte, die einen ActiveMQ-Broker steuern. Mit diesen Werten kann dann ein embedded Broker erzeugt/gestartet
 * werden. <br>
 * Idee: Als Brokernamen den AgentNodeNamen nehmen ?? <br>
 * Zum starten eine Brokers muss u.a. die Url gesetzt sein, diese kann entweder gesetzt werden, oder aus den Properties
 * Port und Protocol erzeugt werden - in diesem Fall wird die lokale Ip ermittelt und zur erzeugung der Url verendet.
 * 
 * @author janko
 * 
 * @deprecated replaced by {@link ActiveMQBroker}
 */
@Deprecated
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

	/**
	 * Erzeugt eine Instanz mit defaultwerten.. um einfach einen Broker erzeugen zu koennen.
	 * 
	 * @return
	 */
	public static BrokerValues getDefaultInstance() {
		BrokerValues values = new BrokerValues("TngBroker", "tcp://localhost:61616", "multicast", "239.255.2.45:5555", true);
		values.setProtocol("tcp");
		values.setPort("61616");
		return values;
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
//        try {
//        	Enumeration<NetworkInterface> interfaces= NetworkInterface.getNetworkInterfaces();
//            while(interfaces.hasMoreElements()){
//                Enumeration<InetAddress> addresses= interfaces.nextElement().getInetAddresses();
//                while(addresses.hasMoreElements()) {
//                    InetAddress current= addresses.nextElement();
//                    if(current.isLoopbackAddress()) {
//                        return _protocol + PROTOCOL_IP_SEPARATOR + current.toString() + IP_PORT_SEPARATOR + _port;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // fall through
//        }
//        
		InetAddress current = null;
		try {
			current = InetAddress.getLocalHost();
			return _protocol + PROTOCOL_IP_SEPARATOR + current.getHostAddress() + IP_PORT_SEPARATOR + _port;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return _protocol + PROTOCOL_IP_SEPARATOR + "<unknownhost>" + IP_PORT_SEPARATOR + _port;
		}
	}
}
