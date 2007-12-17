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
                JiacMessage selectorTemplate= new JiacMessage();
                selectorTemplate.setHeader("key", String.valueOf(i));
                _selectors.add(i, selectorTemplate);
			}
		}
	}

	public void testRegister() throws Exception {
		_log.info("Testing registering addresses");
        testCount++;
		
		
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
        
        
		
	}

	public void testUnregisterAddress0() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;

        // setting up unregistertest and checking correct testenvironment
		List<ListenerContext> listenerList = _addressToListenerMap.get(_addressList.get(0));
		assertNotNull("Check: Still listening on address 0", listenerList);
		assertEquals("Contextcount for Address 0", 2, listenerList.size());
        
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
		
        assertEquals("Listener count for Address 0 Selector 0", 3, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 1, listenerCountForAddress0SelectorNull);
        
		
        
        
        
        // testing first unregisterorder
        _cBean.unregister(_addressList.get(0), null);
		
		listenerList = _addressToListenerMap.get(_addressList.get(0));
		listenerCountForAddress0SelectorNull = 0;
		listenerCountForAddress0Selector0 = 0;
		
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
		
		assertEquals("Listener count for Address 0 Selector 0", 3, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 0, listenerCountForAddress0SelectorNull);
		
        
        
        
        // Next Unregister
        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        
        listenerList = _addressToListenerMap.get(_addressList.get(0));
		listenerCountForAddress0SelectorNull = 0;
		listenerCountForAddress0Selector0 = 0;
		
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
		
		assertEquals("Listener count for Address 0 Selector 0", 2, listenerCountForAddress0Selector0);
        assertEquals("Listener count for Address 0 Selector Null", 0, listenerCountForAddress0SelectorNull);
		
        // Now completely unregister address0
        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        _cBean.unregister(_addressList.get(0), _selectors.get(0));
        
        listenerList = _addressToListenerMap.get(_addressList.get(0));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        
	}
	
	public void testUnregisterAddress1() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;

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
		
        assertEquals("Listener count for Address 1 Selector Other", 0, listenerCountForAddress1SelectorOther);
        assertEquals("Listener count for Address 1 Selector Null", 1, listenerCountForAddress1SelectorNull);
        
        // Now let's unregister it completely
        _cBean.unregister(_addressList.get(1), null);
        
        listenerList = _addressToListenerMap.get(_addressList.get(0));
		assertNull("Check: Unregistered from address 1", listenerList);        
        
	}
	

	public void testUnregisterAddress2() throws Exception {
		_log.info("Checking unregistering of Addresses");
        testCount++;

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
		
        assertEquals("Listener count for Address 2 Selector 1", 2, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
        
		
        
        
        
        // testing first unregisterorder
        _cBean.unregister(_addressList.get(2), _selectors.get(1));
		
		listenerList = _addressToListenerMap.get(_addressList.get(2));
		listenerCountForAddress2SelectorNull = 0;
		listenerCountForAddress2Selector1 = 0;
		
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
		
		assertEquals("Listener count for Address 2 Selector 1", 1, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
		
        
        
        
        // Next Unregister
        _cBean.unregister(_addressList.get(2), _selectors.get(1));
        
        listenerList = _addressToListenerMap.get(_addressList.get(2));
		listenerCountForAddress2SelectorNull = 0;
		listenerCountForAddress2Selector1 = 0;
		
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
		
		assertEquals("Listener count for Address 2 Selector 1", 0, listenerCountForAddress2Selector1);
        assertEquals("Listener count for Address 2 Selector Null", 1, listenerCountForAddress2SelectorNull);
		
        // Now completely unregister address2
        _cBean.unregister(_addressList.get(2)); // should work as (_addressList.get(2), null)
        
        listenerList = _addressToListenerMap.get(_addressList.get(2));
        assertNull("Check: Address 0 unregistered completely", listenerList);
        
        
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




