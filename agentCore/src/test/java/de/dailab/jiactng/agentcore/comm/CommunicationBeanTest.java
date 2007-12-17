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
import de.dailab.jiactng.agentcore.comm.helpclasses.DummyTransport;
import de.dailab.jiactng.agentcore.comm.helpclasses.DummyTransport.RegistrationOrder;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

public class CommunicationBeanTest extends TestCase {

	public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";
	private static IAgentNode _communicationPlatform;
	private static IAgent _communicator;
    private static int testCount= -1;
    
	private static List<IAgentBean> _beans;
	private static CommunicationBean _cBean;
	private static List<ICommunicationAddress> _addressList = new ArrayList<ICommunicationAddress>();
	private static List<IJiacMessage> _selectors = new ArrayList<IJiacMessage>();

	private static DummyTransport _registration = new DummyTransport(); 
	
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

			_registration.doInit();
			_cBean.addTransport(_registration);
			
			// now clear orderbuffer to clear it from all orders that aren't associated with this testsuite
			_registration._orders.clear();
			
			_log.info("Setting up Test Environment");

			int n = 10;

			for (int i = 0; i <= n; i++){
				if (i % 2 == 0){
					_addressList.add(i, CommunicationAddressFactory.createMessageBoxAddress(String.valueOf(i)));
				} else {
					_addressList.add(i, CommunicationAddressFactory.createGroupAddress(String.valueOf(i)));
				}
                JiacMessage selectorTemplate= new JiacMessage();
                selectorTemplate.setHeader("key", String.valueOf(i));
                _selectors.add(i, selectorTemplate);
			}
		}
	}

	public void testRegister() throws Exception {
		_log.info("Testing registering addresses");
        testCount++;
		
        assertTrue("Check: No Orders yet", _registration._orders.isEmpty());
       
		_cBean.register(_addressList.get(0), _selectors.get(0));
		_cBean.register(_addressList.get(1), null);
        _cBean.register(_addressList.get(2), null);

        // double registration
		_cBean.register(_addressList.get(0), null);
		_cBean.register(_addressList.get(0), _selectors.get(0));
		_cBean.register(_addressList.get(0), _selectors.get(0));
		
		_cBean.register(_addressList.get(2), _selectors.get(1));
		_cBean.register(_addressList.get(2), _selectors.get(1));
		
		
		// Now check if the registration went right
        _addressToListenerMap = _cBean.addressToListenerMap;
        
        int listenerCountForAddress0Selector0 = 0;
        int listenerCountForAddress0SelectorNull = 0;
        int listenerCountForAddress1SelectorNull = 0;
        int listenerCountForAddress2SelectorNull = 0;
        int listenerCountForAddress2Selector1 = 0;
        
        List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(0));
        assertEquals("Contextcount for Address 0", 2, listenerList.size());
        
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress0SelectorNull = context.listeners;
        	} else if (context.selector.equals(_selectors.get(0))){
        		listenerCountForAddress0Selector0 = context.listeners;
        	}
        }
        listenerList = _addressToListenerMap.get(_addressList.get(1));
        assertEquals("Contextcount for Address 1", 1, listenerList.size());
        
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress1SelectorNull = context.listeners;
        	}
        }
        
        listenerList = _addressToListenerMap.get(_addressList.get(2));
        assertEquals("Contextcount for Address 2", 2, listenerList.size());
        
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress2SelectorNull = context.listeners;
        	} else if (context.selector.equals(_selectors.get(1))){
        		listenerCountForAddress2Selector1 = context.listeners;
        	}
        }
        
        assertEquals("Listener count for Address 0 Selector 0", 3, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 1, listenerCountForAddress0SelectorNull);
        assertEquals("Listener count for Address 1 Selector Null", 1, listenerCountForAddress1SelectorNull);
        assertEquals("Listener count for Address 2 Selector 1", 2, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
        
        // now let's check the orders given by the Bean to the Transports
        assertEquals("Check quantity of Orders", 5, _registration._orders.size());
        
        int registrations = 0;
        int unregistrations = 0;
        
        for (int i = 0; i < 5; i++){
        	RegistrationOrder order = _registration._orders.get(i);
        	if (order.isRegister()){
        		registrations++;
        	} else {
        		unregistrations++;
        	}
        	
        	switch(i) {
        	case 0 : 
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 0", order._address, _addressList.get(0));
        		assertEquals("Selector 0", order._selector, _selectors.get(0));
        		break;
        	case 1:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 1", order._address, _addressList.get(1));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	case 2:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 2", order._address, _addressList.get(2));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	case 3:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 0", order._address, _addressList.get(0));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	case 4:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 2", order._address, _addressList.get(2));
        		assertEquals("Selector 1", order._selector, _selectors.get(1));
        		break;
        	}
        }
        
        assertEquals("Check quantity of registrationOrders", 5, registrations);
        assertEquals("Check quantity of unregistrationOrders", 0, unregistrations);
        
        // now cleanup for the next test
        _registration._orders.clear();
		
	}

	public void testUnregisterAddress0() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;

        // just clear the orderbuffer to be sure
        _registration._orders.clear();
        
        // setting up unregistertest and checking correct testenvironment
		List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(0));
		assertNotNull("Check: Still listening on address 0", listenerList);
		assertEquals("Contextcount for Address 0", 2, listenerList.size());
		assertTrue("Check if Orderbuffer is empty", _registration._orders.isEmpty());
        
		int listenerCountForAddress0SelectorNull = 0;
		int listenerCountForAddress0Selector0 = 0;
		ListenerContext selector0Context = null;
		ListenerContext selectorNullContext = null;
		
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress0SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(0))){
        		listenerCountForAddress0Selector0 = context.listeners;
        		selector0Context = context;
        	}
        }
		
        assertNotNull("Check: Still listening on Address 0 with Selector 0", selector0Context);
        assertNotNull("Check: Still listening on Address 0 with Selector null", selectorNullContext);
        assertEquals("Listener count for Address 0 Selector 0", 3, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 1, listenerCountForAddress0SelectorNull);
        
        
        // testing first unregisterorder
        _cBean.unregister(_addressList.get(0), null);
        
        // quick test the order given
        assertEquals("One Order given", 1, _registration._orders.size());
        RegistrationOrder order = _registration._orders.get(0);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address0", order._address, _addressList.get(0));
        assertNull("Order of unregistration for Address 0 has selector null", order._selector);
		
        // now check the unregistration
		listenerList = _addressToListenerMap.get(_addressList.get(0));
		listenerCountForAddress0SelectorNull = 0;
		listenerCountForAddress0Selector0 = 0;
		selector0Context = null;
		selectorNullContext = null;
		
		assertNotNull("Check: Still listening on address 0", listenerList);
		assertEquals("Contextcount for Address 0", 1, listenerList.size());
		
		for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress0SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(0))){
        		listenerCountForAddress0Selector0 = context.listeners;
        		selector0Context = context;
        	}
        }
		
		assertNotNull("Check: Still listening on Address 0 with Selector 0", selector0Context);
        assertNull("Check: Not listening on Address 0 with Selector null", selectorNullContext);
		assertEquals("Listener count for Address 0 Selector 0", 3, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 0, listenerCountForAddress0SelectorNull);
		
        
        
        
        // Next Unregister
        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        
        // quick check that no order was given
        assertEquals("No further order given", 1, _registration._orders.size());
        
        listenerList = _addressToListenerMap.get(_addressList.get(0));
		listenerCountForAddress0SelectorNull = 0;
		listenerCountForAddress0Selector0 = 0;
		selector0Context = null;
		selectorNullContext = null;
		
		assertNotNull("Check: Still listening on address 0", listenerList);
		assertEquals("Contextcount for Address 0", 1, listenerList.size());
		
		for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress0SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(0))){
        		listenerCountForAddress0Selector0 = context.listeners;
        		selector0Context = context;
        	}
        }
		
		assertNotNull("Check: Still listening on Address 0 with Selector 0", selector0Context);
		assertNull("Check: Not listening on Address 0 with Selector null", selectorNullContext);
        assertEquals("Listener count for Address 0 Selector 0", 2, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 0, listenerCountForAddress0SelectorNull);
		
        
        // Now completely unregister address0
        
        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        // quick check that no order was given
        assertEquals("No further order given", 1, _registration._orders.size());
        assertNotNull("QuickCheck, if address is still registered", _addressToListenerMap.get(_addressList.get(0)));

        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        
        // quick test the order given
        assertEquals("Another Order given", 2, _registration._orders.size());
        order = _registration._orders.get(1);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address0", order._address, _addressList.get(0));
        assertEquals("Order of unregistration for Address 0 has selector0", _selectors.get(0), order._selector);
        
        // now make the final check
        listenerList = _addressToListenerMap.get(_addressList.get(0));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        // now cleanup for next test
        _registration._orders.clear();
        
	}
	
	public void testUnregisterAddress1() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;
        
        // clear orderlist just to be sure
        _registration._orders.clear();

        // setting up unregistertest and checking correct testenvironment
		List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(1));
		assertNotNull("Check: Still listening on address 1", listenerList);
		assertEquals("Contextcount for Address 1", 1, listenerList.size());
        
		int listenerCountForAddress1SelectorNull = 0;
		int listenerCountForAddress1SelectorOther = 0;
		ListenerContext selectorNullContext = null;
		
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress1SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else {
        		listenerCountForAddress1SelectorOther++;
        	}
        }
		
        assertNotNull("Check: Context with selector null existing", selectorNullContext);
        assertEquals("Listener count for Address 1 Selector Other", 0, listenerCountForAddress1SelectorOther);
        assertEquals("Listener count for Address 1 Selector Null", 1, listenerCountForAddress1SelectorNull);
        
        System.err.println("Now it comes...");
        // Now let's unregister it completely
        _cBean.unregister(_addressList.get(1), null);
        System.err.println("Now it came...");
        
        // quick test the order given
        assertEquals("One Order given", 1, _registration._orders.size());
        RegistrationOrder order = _registration._orders.get(0);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address1", order._address, _addressList.get(1));
        assertNull("Order of unregistration for Address 0 has selector null", order._selector);
        
        // now make the final check
        listenerList = _addressToListenerMap.get(_addressList.get(1));
		assertNull("Check: Unregistered from address 1", listenerList);        
        
		//cleanup orderbuffer for next test
		_registration._orders.clear();
	}
	

	public void testUnregisterAddress2() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;
        
        // clear the orderbuffer just to be sure
        _registration._orders.clear();
        
        // setting up unregistertest and checking correct testenvironment
		List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(2));
		assertNotNull("Check: Still listening on address 2", listenerList);
		assertEquals("Contextcount for Address 2", 2, listenerList.size());
        
		int listenerCountForAddress2SelectorNull = 0;
		int listenerCountForAddress2Selector1 = 0;
		ListenerContext selector1Context = null;
		ListenerContext selectorNullContext = null;
		
        for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress2SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(1))){
        		listenerCountForAddress2Selector1 = context.listeners;
        		selector1Context = context;
        	}
        }
		
        assertNotNull("Check: Still listening on Address 2 with Selector 1", selector1Context);
        assertNotNull("Check: Still listening on Address 2 with Selector Null", selectorNullContext);
        assertEquals("Listener count for Address 2 Selector 1", 2, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
        
		
        
        
        
        // testing first unregisterorder
        _cBean.unregister(_addressList.get(2), _selectors.get(1));
        
        // quick test the order given
        assertEquals("No Order given", 0, _registration._orders.size());
        
        // now some other checks
		
		listenerList = _addressToListenerMap.get(_addressList.get(2));
		listenerCountForAddress2SelectorNull = 0;
		listenerCountForAddress2Selector1 = 0;
		selectorNullContext = null;
		selector1Context = null;
		
		assertNotNull("Check: Still listening on address 2", listenerList);
		assertEquals("Contextcount for Address 2", 2, listenerList.size());
		
		for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress2SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(1))){
        		listenerCountForAddress2Selector1 = context.listeners;
        		selector1Context = context;
        	}
        }
		
		assertNotNull("Check: Still listening on Address 2 with Selector 1", selector1Context);
        assertNotNull("Check: Still listening on Address 2 with Selector Null", selectorNullContext);
		assertEquals("Listener count for Address 2 Selector 1", 1, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
		
        
        
        
        // Next Unregister
        _cBean.unregister(_addressList.get(2), _selectors.get(1));

        // quick test the order given
        assertEquals("One Order given", 1, _registration._orders.size());
        RegistrationOrder order = _registration._orders.get(0);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address2", order._address, _addressList.get(2));
        assertEquals("Order of unregistration for Address 2 has selector1", _selectors.get(1), order._selector);

        // now the other checks
        listenerList = _addressToListenerMap.get(_addressList.get(2));
		listenerCountForAddress2SelectorNull = 0;
		listenerCountForAddress2Selector1 = 0;
		selectorNullContext = null;
		selector1Context = null;
		
		assertNotNull("Check: Still listening on address 2", listenerList);
		assertEquals("Contextcount for Address 2", 1, listenerList.size());
		
		for (ListenerContext context : listenerList){
        	if (context.selector == null){
        		listenerCountForAddress2SelectorNull = context.listeners;
        		selectorNullContext = context;
        	} else if (context.selector.equals(_selectors.get(1))){
        		listenerCountForAddress2Selector1 = context.listeners;
        		selector1Context = context;
        	}
        }
		
		assertNull("Check: Unregistered for listening on Address 2 with Selector 1", selector1Context);
        assertNotNull("Check: Still listening on Address 2 with Selector Null", selectorNullContext);
		assertEquals("Listener count for Address 2 Selector 1", 0, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
		
        // Now completely unregister address2
        _cBean.unregister(_addressList.get(2)); // should work as (_addressList.get(2), null)
        
        // 	quick test the order given
        assertEquals("Further Order given", 2, _registration._orders.size());
        order = _registration._orders.get(1);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address2", order._address, _addressList.get(2));
        assertNull("Order of unregistration for Address 2 has selector null", order._selector);
        
        listenerList = _addressToListenerMap.get(_addressList.get(2));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        
	}

	
	@Override
	protected void tearDown() throws Exception {
		if(testCount >= 4){
			_log.info("tearing down testing environment");
			super.tearDown();
			((SimpleAgentNode)_communicationPlatform).shutdown();
			_log.info("CommunicationBeanTest closed. All Tests done. Good Luck!");
		}
	}
	
}




