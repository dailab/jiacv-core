package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

public class CommunicationBeanTest extends TestCase {

	public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";
	private static IAgentNode _communicationPlatform;
	private static IAgent _communicator;
    private static int testCount= -1;
    
	private static List<IAgentBean> _beans;
	private static CommunicationBean _cBean;
	private static List<ICommunicationAddress> _addressList = new ArrayList<ICommunicationAddress>();
	private static List<Listener> _listeners = new ArrayList<Listener>();
	private static List<Selector> _selectors = new ArrayList<Selector>();

	private static Map<ICommunicationAddress, List<ListenerContext>> _addressToListenerMap;
	private static Log _log = null;

	@Override
	protected void setUp() throws Exception {
		if (testCount < 0){
			testCount= 0;
			_log = LogFactory.getLog("CommunicationBeanTestLog");
			super.setUp();
			
			ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/communicationTestContext.xml");
			_communicationPlatform = (IAgentNode) xmlContext.getBean("CommunicationPlatform");
			
			List<IAgent> beanlist = _communicationPlatform.findAgents(); 
			
			_communicator =  beanlist.get(0);
			
			_beans = _communicator.getAgentBeans(); 

			Iterator<IAgentBean> it = _beans.iterator();
			while (it.hasNext()){
				ILifecycle lc = it.next();
				if (lc instanceof CommunicationBean){
					_cBean = (CommunicationBean) lc;
				}
			}


			_log.info("Setting up Test Environment");

			int n = 10;

			for (int i = 0; i <= n; i++){
				if (i % 2 == 0){
					_addressList.add(i, CommunicationAddressFactory.createMessageBoxAddress(String.valueOf(i)));
				} else {
					_addressList.add(i, CommunicationAddressFactory.createGroupAddress(String.valueOf(i)));
				}
				_listeners.add(i, new Listener(String.valueOf(i)));
                _selectors.add(i, new Selector("key", String.valueOf(i)));
			}
		}
	}

	public void testRegister() throws Exception {
		_log.info("Testing registering Listeners");
        testCount++;
		System.out.println("Testing registering Listeners");

		// then creating an address through registering a normal listener
		_cBean.register(_listeners.get(1), _addressList.get(0), _selectors.get(0));
		_cBean.register(_listeners.get(2), _addressList.get(1), null);
        _cBean.register(_listeners.get(3), _addressList.get(0), null);
		// and two other normal listeners...
		_cBean.register(_listeners.get(4), _addressList.get(1), _selectors.get(0));
		_cBean.register(_listeners.get(5), _addressList.get(0), _selectors.get(1));

        // double registration
        _cBean.register(_listeners.get(1), _addressList.get(0), _selectors.get(0));
        _cBean.register(_listeners.get(4), _addressList.get(1), _selectors.get(0));
        _cBean.register(_listeners.get(3), _addressList.get(0), null);
        
        _addressToListenerMap = _cBean.addressToListenerMap;
        
		// WildcardListener setup is checked....
		_log.info("Begin of SetupCheck of registered Listeners");
		
		// AddressListener setup is checked...
		
		List<ListenerContext> a0List = _addressToListenerMap.get(_addressList.get(0));
		List<ListenerContext> a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertTrue("Check: Listeners for Address 0 exists", a0List != null);
		assertTrue("Check: Listeners for Address 1 exists", a1List != null);
        
        System.out.println("registered for '" + _addressList.get(0) + "': " + a0List);
        assertEquals("Check:  there are missing registrations for '" + _addressList.get(0) + "'", 3, a0List.size());
        
        System.out.println("registered for '" + _addressList.get(1) + "': " + a1List);
        assertEquals("Check:  there are missing registrations for '" + _addressList.get(1) + "'", 2, a1List.size());
	}

	public void testUnregister() throws Exception {
		_log.info("Checking unregistering of Listeners");
        testCount++;

		List<ListenerContext> a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertNotNull("Check: Still Listeners on address 1", a1List);
		
        _cBean.unregister(_listeners.get(4), _addressList.get(1), _selectors.get(0));
		_cBean.unregister(_listeners.get(2), _addressList.get(1), null);
		
		_addressToListenerMap = _cBean.addressToListenerMap;
		a1List = _addressToListenerMap.get(_addressList.get(1));
		
		assertNull("Check: Last normal Listener on address 1 gone, others should be gone too", a1List);
		
	}

	
	@Override
	protected void tearDown() throws Exception {
		if(testCount >= 2){
			_log.info("tearing down testing environment");
			super.tearDown();
			((SimpleAgentNode)_communicationPlatform).shutdown();
			_log.info("CommunicationBeanTest closed. All Tests done. Good Luck!");
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




