/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.ExceptionListener;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
interface IJMSMessageDelegate extends ExceptionListener {
    void onMessage(IJiacMessage message, JMSCommunicationAddress from, String selector);
    void onError(JMSCommunicationAddress from, String selector, Exception exception);
}
