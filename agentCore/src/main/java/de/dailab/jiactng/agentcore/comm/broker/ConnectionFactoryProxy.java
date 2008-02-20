/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.broker;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * This connection factory proxy class is used to lazy-initialise the underlying
 * factory.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class ConnectionFactoryProxy implements ConnectionFactory {
    protected ConnectionFactory connectionFactory= null;
    
    @Override
    public Connection createConnection() throws JMSException {
        checkConnectionFactory();
        return connectionFactory.createConnection();
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        checkConnectionFactory();
        return connectionFactory.createConnection(userName, password);
    }
    
    private void checkConnectionFactory() {
        if(connectionFactory == null) {
            synchronized(this) {
                ActiveMQBroker.initialiseProxy(this);
            }
        }
    }
}
