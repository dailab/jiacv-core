/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit3;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNodeMBean;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MethodExposingBeanTest extends JIACTestForJUnit3 {
    private IAgentNode _node;
    private IAgent _agent;
    private ExampleExposingBean _testBean;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/action/agentTests.xml");
        _node = (IAgentNode) newContext.getBean("myNode");
        _agent = _node.findAgents().get(0);
        _testBean = (ExampleExposingBean)_agent.getAgentBeans().get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ((SimpleAgentNodeMBean)_node).shutdown();
    }

    public void testActions() {
        Set<String> expected= new HashSet<String>();
        
        String prefix= ExampleExposingBean.class.getName() + AbstractMethodExposingBean.METHOD_SEPARATING_CHAR;
        expected.add(prefix + "getFlag");
        expected.add(prefix + "saveMessage");
        expected.add("OderDochAnders");
        
        for(Action action : _testBean.getAllActionsFromMemory()) {
            if(!expected.remove(action.getName())) {
                fail("got '" + action.getName() + "' but expected one of " + expected);
            }
        }
        
        assertEquals("missing some actions " + expected, 0, expected.size());
    }

    public void testOneAction() {
    	Action action = _testBean.getActionFromMemory("OderDochAnders");
    	assertNotNull("missing action 'OderDochAnders'", action);
    }
}
