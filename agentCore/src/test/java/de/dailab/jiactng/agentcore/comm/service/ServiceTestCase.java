/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ServiceTestCase extends TestCase {
    public static String DUMMY_ADDRESS= "dummyAddress";
    public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";

    private BrokerService _broker;
    private IAgentNode _node;
    private ClientBean _clientBean;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _broker= new BrokerService();
        String destination= "localhost:61616";
        System.out.println("setup Broker on " + destination);
        _broker.setPersistent(false);
        _broker.setUseJmx(true);
        _broker.addConnector("tcp://" + destination);
        _broker.start();
        System.out.println("broker started");
        
        ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/service/agentTests.xml");
        _node = (IAgentNode) newContext.getBean("ServiceTestPlatform");
        
        for(IAgent agent : _node.findAgents()) {
            for(IAgentBean bean : agent.getAgentBeans()) {
                if(bean instanceof ClientBean) {
                    _clientBean= (ClientBean) bean;
                }
            }
        }
        
        if(_clientBean == null) {
            fail("clientBean could not be found");
        }
    }
    
    public void testServices() throws Exception {
//        System.out.println("try to print something...");
//        assertEquals("could not log", null, _clientBean.printHelloWorld());
//        
//        System.out.println("and now measure performance...");
//        assertEquals("could not log", null, _clientBean.printTimes());
    }

    @Override
    protected void tearDown() throws Exception {
        ((SimpleAgentNode)_node).shutdown();
        _broker.stop();
        super.tearDown();
    }
}
