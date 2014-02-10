/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.broker;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * This connection factory proxy class is used to lazy-initialise the underlying factory.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class ConnectionFactoryProxy implements ConnectionFactory {
   protected ConnectionFactory connectionFactory = null;
   private boolean persistent = false;

   /**
    * Creates a connection.
    * 
    * @return the connection
    * @throws JMSException if an error occurs during creation of the connection
    * @throws IllegalStateException if no broker is available or the broker is not yet initialized
    * 
    * @see ConnectionFactory#createConnection()
    */
   @Override
   public Connection createConnection() throws JMSException {
      checkConnectionFactory();
      return connectionFactory.createConnection();
   }

   /**
    * Creates a connection by using given credentials.
    * 
    * @param userName the user name
    * @param password the password
    * @return the connection
    * @throws JMSException if an error occurs during creation of the connection
    * @throws IllegalStateException if no broker is available or the broker is not yet initialised
    * 
    * @see ConnectionFactory#createConnection(String, String)
    */
   @Override
   public Connection createConnection(String userName, String password) throws JMSException {
      checkConnectionFactory();
      return connectionFactory.createConnection(userName, password);
   }

    private void checkConnectionFactory() {
        if (connectionFactory == null) {
            synchronized (this) {
                if (connectionFactory == null) {
                    // TODO: this uses a static method of the ActiveMQBroker
                    // without checking whether this broker is actually used.
                    // Needs to be reworked.
                    ActiveMQBroker.initialiseProxy(this);
                }
            }
        }
    }

   public boolean isPersistent() {
      return persistent;
   }

   public void setPersistent(boolean persistence) {
      this.persistent = persistence;
   }

}
