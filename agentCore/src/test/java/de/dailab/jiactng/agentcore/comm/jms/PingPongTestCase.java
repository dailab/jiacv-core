/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PingPongTestCase extends TestCase {
    public static final String DUMMY_ADDRESS= "dummyAddress";
    public static final String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.ICommunicationBean#send";

    private IAgentNode _node;
    private PingerBean _pingerBean;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/jms/agentTests.xml");
        _node = (IAgentNode) newContext.getBean("PingPongNode");
        
        IGroupAddress address= CommunicationAddressFactory.createGroupAddress(DUMMY_ADDRESS);
        for(IAgent agent : _node.findAgents()) {
            for(IAgentBean bean : agent.getAgentBeans()) {
                if(bean instanceof PingerBean) {
                    _pingerBean= (PingerBean) bean;
                } 
            }
            if(agent.getCommunication()!=null) {
              agent.getCommunication().joinGroup(address);    
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
        super.tearDown();
    }
}
