package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Joachim Fuchs
 */
//TODO Agent richtig starten
//TODO Commmunication richtig testen :)
//TODO Agent richtig stoppen
public class SpringCommunicationBeanTest extends TestCase {
    
    ClassPathXmlApplicationContext ctx = null;
    CommunicationBean instance = null;
    
    public SpringCommunicationBeanTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
//        
//        ctx = new ClassPathXmlApplicationContext(
//                new String [] { "de/dailab/jiactng/agentcore/communication/communicationbeanTest.xml" }
//        );
//        
//        instance = (CommunicationBean)ctx.getBean("communicationBean");
//        
    }
    
    protected void tearDown() throws Exception {
//        
//        if (ctx != null) {
//         
//            ctx.close();
//            
//        }
//        
    }
    
    public void testPlatform() throws Exception {
        
//        Thread.sleep(30000);
        
    }
    
}
