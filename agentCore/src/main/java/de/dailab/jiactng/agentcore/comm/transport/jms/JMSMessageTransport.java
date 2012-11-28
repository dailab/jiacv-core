package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.BinaryContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The JMSMessageTransports holds a JMSReceiver and a JMSSender. All Messages received from any of the listeners will be
 * delegated to the defaultDelegate set to this transport.
 * 
 * 
 * Notes: A defaultDelegate should be set with setDefaultDelegate before using it.
 * 
 * @author Janko, Loeffelholz
 */
public class JMSMessageTransport extends MessageTransport {

   private ConnectionFactory _connectionFactory;
   private Connection _connection;
   private JMSSender sender;
   private JMSReceiver receiver;

   /**
    * Creates a JMS message transport with the transport identifier "jms".
    * 
    * @see #JMSMessageTransport(String)
    */
   public JMSMessageTransport() {
      this("jms");
   }

   /**
    * Creates a JMS message transport with a given transport identifier.
    * 
    * @param transportIdentifier
    *           the transport identifier
    * @see MessageTransport#MessageTransport(String)
    */
   public JMSMessageTransport(final String transportIdentifier) {
      super(transportIdentifier);
   }

   /**
    * Initializes the JMSMessageTransport. Notes: ConnectionFactory needed!
    * 
    * @throws Exception
    *            if no logger or connection factory is set, or if the creation of the JMS sender or receiver failed.
    * @see JMSSender#JMSSender(Connection, org.apache.commons.logging.Log)
    * @see JMSReceiver#JMSReceiver(Connection, JMSMessageTransport, org.apache.commons.logging.Log)
    */
   @Override
   public void doInit() throws Exception {

      if (this.log == null) {
         throw new Exception("logging was not set!");
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransport initializing...");
      }

      if (this._connectionFactory == null) {
         throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
      }

      this._connection = this._connectionFactory.createConnection();

      this.sender = new JMSSender(this._connection, this.createChildLog("sender"));
      this.receiver = new JMSReceiver(this._connection, this, this.createChildLog("receiver"));
      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransport initialized");
      }

      this._connection.start();
   }

   /**
    * cleans up the JMSMessageTransports and the classes it holds
    */
   @Override
   public void doCleanup() {
      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransport commences Cleanup");
      }

      try {
         this._connection.stop();
      }
      catch (final JMSException e) {
         this.log.warn("could not stop JMS connection ", e);
      }

      try {
         this._connection.close();
      }
      catch (final Exception e) {
         this.log.warn("could not close connection ", e);
      }

      this._connection = null;
      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransport cleaned up");
      }
   }

   /**
    * Retrieves JiacMessages from JMSMessages
    * 
    * @param message
    *           a JMSMessage received
    * @return the JiacMessage included within the JMSMessage
    * @throws JMSException
    */
   static IJiacMessage unpack(final Message message) throws JMSException {
      IFact payload;
      if (message instanceof BytesMessage) {
         final int length = (int) ((BytesMessage) message).getBodyLength();
         final byte[] data = new byte[length];
         ((BytesMessage) message).readBytes(data);
         payload = new BinaryContent(data);
      }
      else {
         payload = (IFact) ((ObjectMessage) message).getObject();
      }

      final IJiacMessage result = new JiacMessage(payload);
      for (final Enumeration<?> keys = message.getPropertyNames(); keys.hasMoreElements();) {
         final Object keyObj = keys.nextElement();

         if (keyObj instanceof String) {
            final String key = (String) keyObj;
            final Object valueObj = message.getObjectProperty(key);

            if (valueObj instanceof String) {
               result.setHeader(key, (String) valueObj);
            }
         }
      }

      return result;
   }

   /**
    * Puts a JiacMessage into a JMSMessage which could then be send using JMS
    * 
    * @param message
    *           the JiacMessage to sent
    * @param session
    *           a (JMS)session needed to create the message
    * @return Message a JMS message to send over a JMS broker
    * @throws JMSException
    */
   static Message pack(final IJiacMessage message, final Session session) throws JMSException {
      final IFact payload = message.getPayload();
         // IFact payload= message.getPayload();
         Message result;

         if (payload != null && payload instanceof BinaryContent) {
            result = session.createBytesMessage();
            ((BytesMessage) result).writeBytes(((BinaryContent) payload).getData());
         }
         else {
            result = session.createObjectMessage();
            ((ObjectMessage) result).setObject(payload);
         }

         for (final String key : message.getHeaderKeys()) {
            result.setStringProperty(key, message.getHeader(key));
         }

         return result;
     }

   /*
    * U S E I N G     T H E      S E N D E R
    */

   /**
    * Sends the given JiacMessage If the timeout is reached, the message will be expired. Please consider that the
    * clocks of different hosts may run asynchronous!
    * 
    * @param message
    *           a JiacMessage
    * @param commAdd
    *           a CommunicationAddress, which might be a GroupAddress or a MessageBoxAddress
    * @param ttl
    *           the time-to-live of the message in milliseconds or 0 for using timeout specified by this message
    *           transport
    * @throws CommunicationException
    *            if an error occurs while sending the message
    */
   @Override
   public void send(final IJiacMessage message, final ICommunicationAddress commAdd, final long ttl) throws CommunicationException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransport sends Message to address '" + commAdd.toUnboundAddress() + "'");
      }

      try {
         this.sender.send(message, commAdd, (ttl == 0) ? this.timeToLive : ttl);
      }
      catch (final Exception ex) {
         if (this.log.isErrorEnabled()) {
           this.log.error("Sending of Message to address '" + commAdd.toUnboundAddress() + "' through JMS failed!",ex);
         } 
         throw new CommunicationException("error while sending message", ex);
      }
   }

   /*
    * U S E I N G       T H E      R E C E I V E R
    */

   /**
    * Initializes a new listener for the given address and selector
    * 
    * @param address
    *           the address to listen to
    * @param selector
    *           if you want to get only special messages use this to select them
    * @throws CommunicationException
    *            if an error occurs while creating the message listener
    */
   @Override
   public void listen(final ICommunicationAddress address, final IJiacMessage selector) throws CommunicationException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("JMSMessageTransports starts to listen at '" + address.toUnboundAddress() + "' with selector'" + selector + "'");
      }
      try {
         this.receiver.listen(address, selector);
      }
      catch (final JMSException jms) {
         if (this.log.isErrorEnabled()) {
            this.log.error("Listening to address '" + address.toUnboundAddress() + "' through JMS failed!");
            this.log.error("Errorcause reads '" + jms.getCause() + "'");
         }
         throw new CommunicationException("error while registrating", jms);
      }
   }

   /**
    * Stops receiving messages from a given address by removing the listener aligned to it from the listener list
    * (especially useful for temporaryDestinations).
    * 
    * @param address
    *           the address you had listen to
    * @param selector
    *           the selector given with the address when you started to listen to it
    */
   @Override
   public void stopListen(final ICommunicationAddress address, final IJiacMessage selector) {
      this.receiver.stopListen(address, selector);
   }

   //
   // /**
   // * Get the connection factory used for creating sender and receiver.
   // * @return the connection factory
   // */
   // public ConnectionFactory getConnectionFactory() {
   // return connectionFactory;
   // }

   /**
    * Set the connection factory to be used for creating sender and receiver.
    * 
    * @param newConnectionFactory
    *           the connection factory
    */
   public void setConnectionFactory(final ConnectionFactory newConnectionFactory) {
      this._connectionFactory = newConnectionFactory;
   }
}
