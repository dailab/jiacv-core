package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

public class CommunicationBeanTest extends TestCase {

	public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";

	private static BrokerService _broker;
	private static IAgentNode _communicationPlatform;
	private static IAgent _communicator;
	private static boolean setupDone = false;
	private static boolean lastTestDone = false;
	private static List<IAgentBean> _beans;
	private static CommunicationBean _cBean;
	private static List<ICommunicationAddress> _addressList = new ArrayList<ICommunicationAddress>();
	private static List<Listener> _listeners = new ArrayList<Listener>();
	private static List<Selector> _selectors = new ArrayList<Selector>();

	private static Map<Selector, WildcardListenerContext> _selectorToListenerMap;
	private static Map<ICommunicationAddress, List<ListenerContext>> _addressToListenerMap;

	@Override
	protected void setUp() throws Exception {

		if (!setupDone){
			setupDone = true;
			super.setUp();
			_broker= new BrokerService();
			String destination= "localhost:61616";
			System.out.println("setup Broker on " + destination);
			_broker.setPersistent(false);
			_broker.setUseJmx(true);
			_broker.addConnector("tcp://" + destination);
			_broker.start();
			System.out.println("broker started");

			ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/communicationTestContext.xml");
			_communicationPlatform = (IAgentNode) xmlContext.getBean("CommunicationPlatform");
			
			List<IAgent> beanlist = _communicationPlatform.findAgents(); 
			
			_communicator =  beanlist.get(0);
			
//			_communicationPlatform.init();
//			_communicationPlatform.start();
			_beans = _communicator.getAgentBeans(); 

			Iterator<IAgentBean> it = _beans.iterator();
			while (it.hasNext()){
				ILifecycle lc = it.next();
				if (lc instanceof CommunicationBean){
					_cBean = (CommunicationBean) lc;
				}
			}


			int n = 10;

			for (int i = 0; i <= n; i++){
				if (i % 2 == 0){
					MessageBoxAddress address = new MessageBoxAddress("" + i );
					_addressList.add(i, address);
				} else {
					GroupAddress address = new GroupAddress("" + i);
					_addressList.add(i, address);
				}
				_listeners.add(i, new Listener("" + i)) ;
				_selectors.add(i, new Selector("key", "" +i));
			}
		}


	}

	public void testRegister() throws Exception {
		System.out.println("Testing registering Listeners");

		// first a wildcardlistener to see if it will be put to the new addresses
		_cBean.register(_listeners.get(0), _selectors.get(0));

		// then creating an address through registering a normal listener
		_cBean.register(_listeners.get(1), _addressList.get(0), _selectors.get(0));

		// then register other wildcardlisteners
		_cBean.register(_listeners.get(2), _selectors.get(0));
		_cBean.register(_listeners.get(3), _selectors.get(1));

		// and two other normal listeners...
		_cBean.register(_listeners.get(4), _addressList.get(1), _selectors.get(0));
		_cBean.register(_listeners.get(5), _addressList.get(0), _selectors.get(1));

		_addressToListenerMap = _cBean._addressToListenerMap;
		_selectorToListenerMap = _cBean._selectorToListenerMap;


		// WildcardListener setup is checked....
		System.out.println("Begin of SetupCheck of registered Listeners");
		
		WildcardListenerContext s0 = _selectorToListenerMap.get(_selectors.get(0));
		WildcardListenerContext s1 = _selectorToListenerMap.get(_selectors.get(1));
		
		assertTrue("Check: WildcardListeners for Selector 0 exists", s0 != null);
		assertTrue("Check: WildcardListeners for Selector 1 exists", s1 != null);
		
		if (s0 != null){
			assertTrue("Check: (WC)Listener 0 listen on selector 0", s0.listeners.contains(_listeners.get(0)));
			assertTrue("Check: (WC)Listener 2 listen on selector 0", s0.listeners.contains(_listeners.get(2)));
		} else {
			assertTrue("Error: Wildcardlistenerregistration on Selector 0 failed!", false);
		}
		
		if (s1 != null){
			assertTrue("Check: (WC)Listener 3 listen on selector 1", s1.listeners.contains(_listeners.get(3)));
		} else {
			assertTrue("Error: Wildcardlistenerregistration on Selector 1 failed!", false);
		}
		
		// AddressListener setup is checked...
		
		List<ListenerContext> a0List = _addressToListenerMap.get(_addressList.get(0));
		List<ListenerContext> a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertTrue("Check: Listeners for Address 0 exists", a0List != null);
		assertTrue("Check: Listeners for Address 1 exists", a1List != null);
		
		if (a0List != null){
			// check WildcardListenerpresence on address 0
			int index = a0List.indexOf(s0);
			ListenerContext searchContext = a0List.get(index);
			
			assertTrue("Check: Wildcardlisteners (Selector 0) registered on address a0", searchContext instanceof WildcardListenerContext);
			assertTrue("Check: WildcardListener was registered on new address", searchContext.listeners.contains(_listeners.get(0)));
			assertTrue("Check: WildcardListeners were registered on earlier address", searchContext.listeners.contains(_listeners.get(2)));
			
			index = a0List.indexOf(s1);
			searchContext = a0List.get(index);
			assertTrue("Check: WildcardListeners were registered on earlier address", searchContext.listeners.contains(_listeners.get(3)));
			
			// check AddressListeners on address 0
			
			boolean listenerFound = false;
			for (ListenerContext context : a0List){
				if (!(context instanceof WildcardListenerContext)){
					if (context.selector.equals(_selectors.get(0))){
						searchContext = context;
						listenerFound = true;
						break;
					}
				}
			}
			
			assertTrue("Check: NonWildcardListeners were found on address 0", listenerFound);
			assertTrue("Check: Listener 1 registered for address 0 with selector 0", searchContext.listeners.contains(_listeners.get(1)));
			
		} else {
			assertTrue("Error: Listenerregistration on Address 0 failed!", false);
		}
		
		if (a1List != null){
			// check WildcardListenerpresence on address 0
			int index = a1List.indexOf(s0);
			ListenerContext searchContext = a1List.get(index);
			
			assertTrue("Check: Wildcardlisteners (Selector 0) registered on address a1", searchContext instanceof WildcardListenerContext);
			assertTrue("Check: WildcardListener was registered on new address", searchContext.listeners.contains(_listeners.get(0)));
			assertTrue("Check: WildcardListeners were registered on earlier address", searchContext.listeners.contains(_listeners.get(2)));
			
			index = a1List.indexOf(s1);
			searchContext = a1List.get(index);
			assertTrue("Check: WildcardListeners were registered on earlier address", searchContext.listeners.contains(_listeners.get(3)));
			
			// check AddressListeners on address 0
			
			searchContext = null;
			for (ListenerContext context : a1List){
				if (!(context instanceof WildcardListenerContext)){
					if (context.selector.equals(_selectors.get(0))){
						searchContext = context;
						break;
					}
				}
			}
			
			
			if (searchContext != null){
				assertTrue("Check: Listener 4 registered for address 1 with selector 0", searchContext.listeners.contains(_listeners.get(4)));
			} else {
				assertTrue("Error: no NonWildcardListeners were found on address 1", searchContext != null);
			}
			
			
		} else {
			assertTrue("Error: Listenerregistration on Address 1 failed!", false);
		}
	}

	public void testUnregister() throws Exception {
		System.out.println("Checking unregistering of Listeners");
		
		_cBean.unregister(_listeners.get(3), _selectors.get(1));
		
		_selectorToListenerMap = _cBean._selectorToListenerMap;
		
		WildcardListenerContext s0 = _selectorToListenerMap.get(_selectors.get(0));
		WildcardListenerContext s1 = _selectorToListenerMap.get(_selectors.get(1));
		
		assertNull("Check: WildcardListener with Selector 1 is unregistered.", s1);
		assertNotNull("Check: Other WildcardListeners still listening", s0);
		
//		List<ListenerContext> a0List = _addressToListenerMap.get(_addressList.get(0));
		List<ListenerContext> a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertNotNull("Check: Still Listeners on address 1", a1List);
		
		_cBean.unregister(_listeners.get(4), _addressList.get(1), _selectors.get(0));
		
		_addressToListenerMap = _cBean._addressToListenerMap;
		a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertNull("Check: Last normal Listener on address 1 gone, others should be gone too", a1List);
		
	}
	
	public void testRegisterSelectorNull() throws Exception {
		System.out.println("Registering a listener with selector=null.");
		_cBean.register(_listeners.get(6), _addressList.get(2), null);
	}
	
	public void testUnregisterSelectorNull() throws Exception {
		System.out.println("Unregistering Listener with selector=null");
		_cBean.unregister(_listeners.get(6), _addressList.get(2), null);
		
	}
	
	public void testLasttest(){
		System.out.println("Letzter Test!");
		lastTestDone = true;
		
		assertTrue(true);
		
	}



	@Override
	protected void tearDown() throws Exception {
		if(lastTestDone){
			super.tearDown();
//			((SimpleAgentNode)_communicator.shutdown();
//			_communicationPlatform.stop();
//			_communicationPlatform.cleanup();
//			_broker.stop();
			
		}
	}
	
	private class Listener implements IJiacMessageListener{

		private String _name;

		public Listener(String name){
			_name = name;
		}

		public void receive(IJiacMessage message, ICommunicationAddress at){

		}
	}
}




