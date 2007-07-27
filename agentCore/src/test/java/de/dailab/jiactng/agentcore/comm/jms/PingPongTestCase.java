/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PingPongTestCase extends TestCase {
    public static String DUMMY_ADDRESS= "dummyAddress";
    public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";

    private BrokerService _broker;
    private IAgentNode _node;
    private PingerBean _pingerBean;
    
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
        
        ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/jms/agentTests.xml");
        _node = (IAgentNode) newContext.getBean("PingPongPlatform");
        
        IGroupAddress address= CommunicationAddressFactory.createGroupAddress(DUMMY_ADDRESS);
        for(IAgent agent : _node.findAgents()) {
            for(IAgentBean bean : agent.getAgentBeans()) {
                if(bean instanceof PingerBean) {
                    _pingerBean= (PingerBean) bean;
                } else if(bean instanceof CommunicationBean) {
                    ((CommunicationBean) bean).joinGroup(address);
                }
            }
        }
        
        if(_pingerBean == null) {
            fail("pingerBean could not be found");
        }
    }
    
    public void testPingPong() throws Exception {
        _pingerBean.startPingProcess();
        if(!_pingerBean.waitForPong()) {
            fail("no pong received");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        ((SimpleAgentNode)_node).shutdown();
        _broker.stop();
        super.tearDown();
    }
}
