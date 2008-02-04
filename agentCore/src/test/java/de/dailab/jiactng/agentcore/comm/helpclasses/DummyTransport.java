package de.dailab.jiactng.agentcore.comm.helpclasses;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;


/**
 * A Dummy Class to make it possible for a Testcase to determine which orders are
 * actually given by the CommunicationBean to it's transports. So it is possible
 * to check if an address is registered or unregistered and depending on the
 * frequency of requests to this DummyTransport when this happens.
 *  
 * Messages and their Destinations will be stored within the _sentTo and _messages lists
 * and can be retrieved too.
 *  
 * @author Martin Loeffelholz
 *
 */
public class DummyTransport extends MessageTransport {

	public List<RegistrationOrder> orders = new ArrayList<RegistrationOrder>();
	public ArrayList<ICommunicationAddress> sentTo = new ArrayList<ICommunicationAddress>();
	public ArrayList<IJiacMessage> messages = new ArrayList<IJiacMessage>();
	
	public DummyTransport(){
		super("DummyTransport");
	}
	
	@Override
	public void doCleanup() throws Exception {
		orders.clear();
		sentTo.clear();
		messages.clear();
	}

	@Override
	public void doInit() throws Exception {
		orders.clear();
		sentTo.clear();
		messages.clear();
	}

	@Override
	public void listen(ICommunicationAddress address, IJiacMessage selector)
			throws CommunicationException {
		RegistrationOrder order = new RegistrationOrder(address, selector);
		orders.add(order);
	}

	@Override
	public void send(IJiacMessage message, ICommunicationAddress address)
			throws CommunicationException {
		sentTo.add(address);
		messages.add(message);
	}

	@Override
	public void stopListen(ICommunicationAddress address, IJiacMessage selector)
			throws CommunicationException {
		RegistrationOrder order = new RegistrationOrder(address, selector);
		order.setRegister(false);
		orders.add(order);

	}
	
	public void printOrders(){
		for (RegistrationOrder order : orders){
        	System.out.println(order);
        }
	}

	/**
	 * Class to store Orders which are given, 
	 * from the communicationBean to the Transports.
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	public class RegistrationOrder{
		public ICommunicationAddress _address = null;
		public IJiacMessage _selector = null;
		
		// was the order an order to register(true) or unregister(false)?
		Boolean _register = true;
		
		public RegistrationOrder(){
			
		}
		
		public RegistrationOrder(ICommunicationAddress address, IJiacMessage selector){
			_address = address;
			_selector = selector;
		}
		
		public void setRegister(boolean register){
			_register = register;
		}
		
		public boolean isRegister(){
			return _register;
		}
		
		public String toString(){
			String result = _register ? "Registering for " : "Unregistering from ";
			result = result + _address + " with selector " + _selector;
			return result;
		}
		
	}
	
}
