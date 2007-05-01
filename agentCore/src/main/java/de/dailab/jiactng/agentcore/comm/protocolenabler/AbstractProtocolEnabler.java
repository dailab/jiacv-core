package de.dailab.jiactng.agentcore.comm.protocolenabler;

import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.CommMessageListener;
import javax.jms.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for protocol enablers
 *
 * @author Joachim Fuchs
 */
public class AbstractProtocolEnabler implements CommMessageListener {
    
    protected Log log = LogFactory.getLog(getClass());
    /**
     * The communication bean associated with this enabler
     */
    private CommBean commBean = null;

    
    /**
     * messages via JMS are requests from our platform's agents
     */
    public void messageReceivedFromQueue(Message message) {
    }

    /**
     * messages via JMS are requests from our platform's agents
     */
    public void messageReceivedFromTopic(Message message) {
    }

    public CommBean getCommBean() {
        return commBean;
    }

    public void setCommBean(CommBean commBean) {
        this.commBean = commBean;
    }

}
