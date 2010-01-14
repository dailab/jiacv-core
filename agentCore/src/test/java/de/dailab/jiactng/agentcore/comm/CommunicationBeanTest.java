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

	/**
	 * Welcome to our CommunicationBeanTestCase.
	 * Here we are setting up the "environment" for our tests to come.
	 * This is happening exactly one time before the first test.
	 * 
	 * Spring will set up an Active-MQ broker, and create an agentplatform with an agent,
	 * that includes a communicationBean.
	 */
	@Override
	protected void setUp() throws Exception {
		if (testCount < 0){
			// if this is before the first test...
			testCount= 0;

			// init log
			_log = LogFactory.getLog("CommunicationBeanTestLog");
			super.setUp();
			
			// agentplatformcreation
			ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/communicationTestContext.xml");
			_communicationPlatform = (IAgentNode) xmlContext.getBean("CommunicationNode");
			
			// get a list of all agents on the platform. There should be exactly one...
			List<IAgent> beanlist = _communicationPlatform.findAgents(); 
			
			// get this one and only agent from the platform
			_communicator =  beanlist.get(0);
			
			// get the beans of the agent
			_beans = _communicator.getAgentBeans(); 

			// find the communicationBean within the beans of that agent
			Iterator<IAgentBean> it = _beans.iterator();
			while (it.hasNext()){
				ILifecycle lc = it.next();
				if (lc instanceof CommunicationBean){
					_cBean = (CommunicationBean) lc;
				}
			}

			// init DummyTransport for testing purposes and add it to the Communicationbean
			_registration.doInit();
			_cBean.addTransport(_registration);
			
			// now clear orderbuffer of Dummytransport, to clear it from the
			// initialisation orders given by default from the communicationBean
			_registration.orders.clear();
			
			_log.info("Setting up Test Environment");

			// create n addresses and selectors for testregistrations etc.
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

	/**
	 * this will set up the address/selector combinations we are going to unregister
	 * in the testcases to come. 
	 * 
	 * In the End the following address/selector combinations will be set up:
	 * 
	 * Address 0:
	 * 	3x selector 0
	 * 	1x selector null
	 *  
	 * Address 1:
	 * 	1x selector null
	 * 
	 * Address 2:
	 * 	1x selector null
	 * 	2x selector 1
	 * 
	 * @throws Exception
	 */
	public void testRegister() throws Exception {
		_log.info("Testing registering addresses");
        testCount++;
		
        assertTrue("Check: No Orders yet", _registration.orders.isEmpty());
       
        // Register one address with selector, two without. 
		_cBean.register(_addressList.get(0), _selectors.get(0));
		_cBean.register(_addressList.get(1), null);
        _cBean.register(_addressList.get(2), null);

        // double registration
        // Let's put two other listeners on the addresses 0 and 2.
        // Address 1 will be the only one that has only one listener
		_cBean.register(_addressList.get(0), null);
		_cBean.register(_addressList.get(0), _selectors.get(0));
		_cBean.register(_addressList.get(0), _selectors.get(0));
		
		_cBean.register(_addressList.get(2), _selectors.get(1));
		_cBean.register(_addressList.get(2), _selectors.get(1));
		
		
		/*
		 * Now check if the registration went right
		 * 
		 * First we will get the addressToListenerMap from the communicationBean,
		 * that stores the informations regarding the registered listeners on each
		 * address.
		 * 
		 * Then we will count all registered Listeners for the given addresses and
		 * selectors, and will compare them with the expected number of listeners
		 * 
		 * On the way we will also check the structure of the Listenercontexts for
		 * the addresses, as there should be one context for each Address/Selector
		 * combination given to the CommunicationBean
		 * 
		 */
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
        
        /*
         * After we just checked if the CommunicationBean processed the registrations
         * correctly and stored the right informations within itself, now we will 
         * check if the CommunicationBean also gave the right orders of registration
         * to it's Transports by checking the orders given to our DummyTransport
         */
        assertEquals("Check quantity of Orders", 5, _registration.orders.size());
        
        int registrations = 0;
        int unregistrations = 0;
        
        // As there should be five Orders given we will check each order given
        // to be the order we expected it to be.
        for (int i = 0; i < 5; i++){
        	RegistrationOrder order = _registration.orders.get(i);
        	if (order.isRegister()){
        		registrations++;
        	} else {
        		unregistrations++;
        	}
        	
        	switch(i) {
        	// first Order
        	case 0 : 
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 0", order._address, _addressList.get(0));
        		assertEquals("Selector 0", order._selector, _selectors.get(0));
        		break;
        	// second Order
        	case 1:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 1", order._address, _addressList.get(1));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	// third Order
        	case 2:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 2", order._address, _addressList.get(2));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	// fourth Order
        	case 3:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 0", order._address, _addressList.get(0));
        		assertNull("Check: Selector is Null", order._selector);
        		break;
        	// fifth Order
        	case 4:
        		assertTrue("Registering", order.isRegister());
        		assertEquals("Address 2", order._address, _addressList.get(2));
        		assertEquals("Selector 1", order._selector, _selectors.get(1));
        		break;
        	}
        }
        
        // now check that all 5 orders are registrationorders and there was no
        // unregistration order given.
        assertEquals("Check quantity of registrationOrders", 5, registrations);
        assertEquals("Check quantity of unregistrationOrders", 0, unregistrations);
        
        // now cleanup for the next test
        _registration.orders.clear();
		
	}

	/**
	 * In this test we will unregister all listeners for Address 0
	 * 
	 * In the beginning the Listeners for Address 0 should be:
	 * 	3x selector 0
	 * 	1x selector null
	 * 
	 * First we will check if all is set up allright.
	 * 
	 * Then we will first unregister the single listener with the null selector and
	 * check if the listener and context for that combination will correctly be gone.
	 * We also will check if the correct order was given to the transport.
	 *
	 * After that we will unregister one of the three listeners that are listening
	 * on address 0 with selector 0, and check that correctly no order is given
	 * to the transport, because there are still two more listeners on that context.
	 * 
	 * Finally we will unregister the last two listeners and check if the address
	 * was cleaned up and unregistered correctly on the transport.
	 * 
	 * @throws Exception
	 */
	public void testUnregisterAddress0() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;

        // just clear the orderbuffer to be sure
        _registration.orders.clear();
        
        // setting up unregistertest and checking correct testenvironment
		List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(0));
		assertNotNull("Check: Still listening on address 0", listenerList);
		assertEquals("Contextcount for Address 0", 2, listenerList.size());
		assertTrue("Check if Orderbuffer is empty", _registration.orders.isEmpty());
        
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
        assertEquals("One Order given", 1, _registration.orders.size());
        RegistrationOrder order = _registration.orders.get(0);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address0", order._address, _addressList.get(0));
        assertNull("Order of unregistration for Address 0 has selector null", order._selector);
		
        /* 
         * now check the unregistration
         * the context for Address 0, Selector null should be gone.
         *  
         */
		listenerList = _addressToListenerMap.get(_addressList.get(0));
		listenerCountForAddress0SelectorNull = 0;
		listenerCountForAddress0Selector0 = 0;
		selector0Context = null;
		selectorNullContext = null;
		
		/*
		 Nevertheless still there should be someone listening on that address
         * with the selector 0, so there should be one context remaining
		 */
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
        
        // quick check that no order was given, as there are still some listeners
        // listening with selector 0
        assertEquals("No further order given", 1, _registration.orders.size());
        
        // now check if number of listeners was updated correctly
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
        assertEquals("No further order given", 1, _registration.orders.size());
        assertNotNull("QuickCheck, if address is still registered", _addressToListenerMap.get(_addressList.get(0)));

        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        
        // quick test the order given
        assertEquals("Another Order given", 2, _registration.orders.size());
        order = _registration.orders.get(1);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address0", order._address, _addressList.get(0));
        assertEquals("Order of unregistration for Address 0 has selector0", _selectors.get(0), order._selector);
        
        // now make the final check
        // there should be no one left listening on this address
        listenerList = _addressToListenerMap.get(_addressList.get(0));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        // now cleanup for next test
        _registration.orders.clear();
        
	}
	
	/**
	 * This testcase will test correct unregistration of an address with only
	 * one listener and so only one context for it. So it's quite plain and simple.
	 * 
	 * First the correct setup is checked. After that we simply unregister the
	 * only listener for this address and check if the right order is given and
	 * the address is cleaned up properly
	 * 
	 * @throws Exception
	 */
	public void testUnregisterAddress1() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;
        
        // clear orderlist just to be sure
        _registration.orders.clear();

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
        
        // Now let's unregister it completely
        _cBean.unregister(_addressList.get(1), null);
        
        // quick test the order given
        assertEquals("One Order given", 1, _registration.orders.size());
        RegistrationOrder order = _registration.orders.get(0);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address1", order._address, _addressList.get(1));
        assertNull("Order of unregistration for Address 0 has selector null", order._selector);
        
        // now make the final check
        listenerList = _addressToListenerMap.get(_addressList.get(1));
		assertNull("Check: Unregistered from address 1", listenerList);        
        
		//cleanup orderbuffer for next test
		_registration.orders.clear();
	}
	

	/**
	 * our last unregistertest will unregister all listeners for address 2
	 * 
	 * The setup of listeners for this address should look like this:
	 * 	1x selector null
	 * 	2x selector 1
	 * 
	 * So this test is allmost like the testcase for address 0 but instead of
	 * first unregistering an listener with no selector, we will unregister the
	 * others first and finaly the only listener without selector. So it may be
	 * checked if the CommunicationBean isn't making any difference between listeners
	 * with and without selectors.
	 * 
	 * @throws Exception
	 */
	public void testUnregisterAddress2() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;
        
        // clear the orderbuffer just to be sure
        _registration.orders.clear();
        
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
        assertEquals("No Order given", 0, _registration.orders.size());
        
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
        assertEquals("One Order given", 1, _registration.orders.size());
        RegistrationOrder order = _registration.orders.get(0);
        
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
        assertEquals("Further Order given", 2, _registration.orders.size());
        order = _registration.orders.get(1);
        
        assertFalse("Order is Unregister", order.isRegister());
        assertEquals("Order regards Address2", order._address, _addressList.get(2));
        assertNull("Order of unregistration for Address 2 has selector null", order._selector);
        
        listenerList = _addressToListenerMap.get(_addressList.get(2));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        
	}
	

	/**
	 * If all tests are been committed we now can tear down our testenvironment
	 * which means shuting down the agentplatform and so all agents on it with
	 * all beans included by them.
	 */
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
