/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.knowledge;

import junit.framework.TestCase;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ConsumingKnowledgeProcessorTest extends TestCase {
    protected class FactHandler extends ConsumingKnowledgeProcessor<Fact> {
        @Override
        public void handle(Fact fact) {
            assertTrue("was detached but got fact: " + fact, active);
        }
    }
    
    protected IMemory memory;
    protected boolean active;
    protected FactHandler factHandler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        memory= new Memory();
        memory.init();
        memory.start();
        
        factHandler= new FactHandler();
        factHandler.attachTo(memory, null);
        active= true;
    }
    
    public void testProcessor() throws Exception {
        memory.write(new Fact(1, "foo", Boolean.FALSE));
        memory.write(CommunicationAddressFactory.createGroupAddress("alle"));
        Thread.sleep(1000);
        System.out.println(memory.readAll());
        assertTrue("too many objects where removed", memory.readAll().size() == 1);
        
        factHandler.detachFrom(memory);
        active= false;
        
        memory.write(new Fact(3, "bar", Boolean.FALSE));
        Thread.sleep(1000);
        System.out.println(memory.readAll());
        assertTrue("too many objects where removed", memory.readAll().size() == 2);
    }

    @Override
    protected void tearDown() throws Exception {
        memory.stop();
        memory.cleanup();
        super.tearDown();
    }
}
