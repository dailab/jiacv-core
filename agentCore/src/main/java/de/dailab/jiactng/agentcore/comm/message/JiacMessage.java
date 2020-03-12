package de.dailab.jiactng.agentcore.comm.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * The implementation of {@link IJiacMessage}.
 * 
 * @author Marcel Patzlaff
 */
@SuppressWarnings("serial")
public final class JiacMessage implements IJiacMessage {
    /*
     * TODO: changed JiacMessage to final to avoid unexpected subclassing
     *       We should consider the necessity for typed contents to force the
     *       existence of specific content types (i.e. subclass of IFact as argument in
     *       the constructor...)
     */
    
    
    
   private IFact payload;

   private Map<String, String> headers;

   private transient boolean recursionDetected = false;

   /**
    * Constructor for a template message with no content.
    */
   public JiacMessage() {
      this(null, null);
   }

   /**
    * Creates a message with a given payload.
    * @param payload the payload of the message
    */
   public JiacMessage(IFact payload) {
      this(payload, null);
   }

   /**
    * Creates a message with a given payload and reply-to-address.
    * @param payload the payload of the message
    * @param replyToAddress the address where reply messages will be send
    */
   public JiacMessage(IFact payload, ICommunicationAddress replyToAddress) {
      this.payload = payload;
      headers = new Hashtable<String, String>();
      if (replyToAddress != null) {
         setHeader(Header.REPLY_TO, replyToAddress.toString());
      }
   }

   /**
    * Creates a message from JMX composite data, but with ignoring the payload.
    * @param descr the message description based on JMX open types.
    */
   public JiacMessage(CompositeData descr) {
	   headers = new Hashtable<String, String>();
	   CompositeData messageHeaders = (CompositeData) descr.get(IJiacMessage.ITEMNAME_HEADERS);
	   for (String key : messageHeaders.getCompositeType().keySet()) {
		   headers.put(key, (String) messageHeaders.get(key));
	   }
   }

   /**
    * {@inheritDoc}
    */
   public IFact getPayload() {
      return payload;
   }

   /**
    * Set the payload (content) of this message.
    * @param newPayload the payload to set
    */
   public void setPayload(IFact newPayload) {
      payload = newPayload;
   }

   /**
    * {@inheritDoc}
    */
   public String getHeader(String key) {
      return headers.get(key);
   }

   /**
    * {@inheritDoc}
    */
   public void setHeader(String key, String value) {
      if (value == null) {
         headers.remove(key);
      }
      else {
         headers.put(key, value);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getHeaderKeys() {
      final Set<String> result = new HashSet<String>();
      result.addAll(headers.keySet());
      return result;
   }

   /**
	* Checks the equality of two messages. The messages are equal
	* if a recursion was detected within this message, or if their 
	* headers and payload are equal.
	* @param obj the other message
	* @return the result of the equality check
    */
   @Override
   public synchronized boolean equals(Object obj) {
      if (recursionDetected) { return true; }

      try {
         recursionDetected = true;
         if (obj == this) { return true; }

         if (! (obj instanceof JiacMessage)) { return false; }

         final JiacMessage other = (JiacMessage) obj;
         final Set<String> otherKeys = other.getHeaderKeys();

         if (otherKeys.size() != headers.size()) { return false; }

         for (String key : otherKeys) {
            final String myValue = headers.get(key);
            if (myValue == null || !other.getHeader(key).equals(myValue)) { return false; }
         }

         final IFact otherPayload = other.getPayload();
         return otherPayload != null && payload != null ? otherPayload.equals(payload) : otherPayload == payload;
      }
      finally {
         recursionDetected = false;
      }
   }

   /**
	* Returns the hash code by calculation from this class, the headers, and payload.
	* Thus it is the same hash code for all messages with the same headers and payload.
	* It returns 0 if a recursion was detected within this message.
	* @return the calculated hash code
    */
   @Override
   public synchronized int hashCode() {
      if (recursionDetected) { return 0; }

      try {
         recursionDetected = true;
         int hashCode = JiacMessage.class.hashCode();

         for (String key : getHeaderKeys()) {
            hashCode ^= key.hashCode() << 7;
            hashCode ^= getHeader(key).hashCode();
         }

         if (payload != null) {
            hashCode ^= payload.hashCode();
         }

         return hashCode;
      }
      finally {
         recursionDetected = false;
      }
   }

   /**
	* Returns a single-line text which contains the headers, payload,
	* and sender of the message. It returns only "&lt;recursion&gt;" 
	* if a recursion was detected within this message.
	* @return a string representation of the message
    */
   public synchronized String toString() {
      if (recursionDetected) { return "<recursion>"; }

      try {
         recursionDetected = true;
         return String.format("[Headers: %s, Payload: %s, Sender: %s",
        		 headers,getPayload(), getSender());
      }
      finally {
         recursionDetected = false;
      }
   }

   /**
    * {@inheritDoc}
    */
   public ICommunicationAddress getSender() {
      final String uri = getHeader(Header.SENDER);

      if (uri != null) { return CommunicationAddressFactory.createFromURI(uri); }

      return null;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getGroup(){
	   return getHeader(Header.GROUP);
   }

   /**
    * {@inheritDoc}
    */
   public ICommunicationAddress getReplyToAddress() {
      final String uri = getHeader(Header.REPLY_TO);

      if (uri != null) { return CommunicationAddressFactory.createFromURI(uri); }

      return null;
   }

   /**
    * Set the protocol of this message.
    * @param protocol the protocol to set
    */
   public void setProtocol(String protocol) {
      setHeader(Header.PROTOCOL, protocol);
   }

   /**
    * {@inheritDoc}
    */
   public String getProtocol() {
      return getHeader(Header.PROTOCOL);
   }

   /**
    * Sets that the payload is converted to BinaryContent.
    * @param binaryContentFlag <code>true</code> if the payload is converted
    */
   public void setBinaryContent(boolean binaryContentFlag) {
	   if (binaryContentFlag) {
		   setHeader(Header.BINARY_CONTENT, "true");
	   }
	   else {
		   headers.remove(Header.BINARY_CONTENT);
	   }
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasBinaryContent() {
	   return headers.keySet().contains(Header.BINARY_CONTENT) && 
			   headers.get(Header.BINARY_CONTENT).equals("true");
   }

   /**
    * Set the sender of this message.
    * @param sender the communication address of the sender
    */
   public void setSender(ICommunicationAddress sender) {
      setHeader(Header.SENDER, sender != null ? sender.toString() : null);
   }
   
   /**
    * Set the group of this message.
    */
   public void setGroup(String group) {
      setHeader(Header.GROUP, group);
   }

   /**
    * Gets the type of JIAC message descriptions based on JMX open types.
    * 
    * @return A composite type containing headers and payload.
    * @throws OpenDataException
    *             if an error occurs during the creation of the type.
    * @see javax.management.openmbean.CompositeType
    */
   public OpenType<?> getDescriptionType() throws OpenDataException {
      final ArrayList<String> itemNames = new ArrayList<String>();
      final ArrayList<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();

      // create open type of message payload
      if (payload != null) {
         itemNames.add(ITEMNAME_PAYLOAD);
         if (payload instanceof JmxDescriptionSupport) {
            itemTypes.add(((JmxDescriptionSupport) payload).getDescriptionType());
         }
         else {
            itemTypes.add(SimpleType.STRING);
         }
      }

      // create open type of message headers
      if (headers != null) {
         itemNames.add(ITEMNAME_HEADERS);
         final String[] headerNames = getHeaderKeys().toArray(new String[getHeaderKeys().size()]);
         final int headerSize = headerNames.length;
         final OpenType<?>[] headerTypes = new OpenType<?>[headerSize];
         for (int i = 0; i < headerSize; i++) {
            // each header value is of type String
            headerTypes[i] = SimpleType.STRING;
         }
         itemTypes.add(new CompositeType(headers.getClass().getName(), "headers of a JIAC-TNG message", headerNames, headerNames, headerTypes));
      }

      // use names of message items as their description
      final ArrayList<String> itemDescriptions = itemNames;

      // transform list to array
      final int compositeSize = itemTypes.size();
      final OpenType<?>[] itemTypesArray = new OpenType<?>[compositeSize];
      for (int i = 0; i < compositeSize; i++) {
         itemTypesArray[i] = itemTypes.get(i);
      }

      // create and return open type of a JIAC message
      return new CompositeType(
    		  this.getClass().getName(), 
    		  "standard JIAC-TNG message", 
    		  itemNames.toArray(new String[itemNames.size()]), 
    		  itemDescriptions.toArray(new String[itemDescriptions.size()]), 
    		  itemTypesArray);
   }

   /**
    * Gets the description of this JIAC message based on JMX open types.
    * 
    * @return Composite data containing headers and payload.
    * @throws OpenDataException
    *             if an error occurs during the creation of the data.
    * @see javax.management.openmbean.CompositeData
    */
   public Object getDescription() throws OpenDataException {
      final Map<String, Object> items = new HashMap<String, Object>();
      final CompositeType type = (CompositeType) getDescriptionType();

      // create open data of message payload
      if (payload != null) {
         if (payload instanceof JmxDescriptionSupport) {
            items.put(ITEMNAME_PAYLOAD, ((JmxDescriptionSupport) payload).getDescription());
         }
         else {
            items.put(ITEMNAME_PAYLOAD, payload.toString());
         }
      }

      // create open data of message headers
      if (headers != null) {
         final String[] headerNames = getHeaderKeys().toArray(new String[getHeaderKeys().size()]);
         final int headerSize = headerNames.length;
         final Object[] headerValues = new Object[headerSize];
         for (int i = 0; i < headerSize; i++) {
            headerValues[i] = getHeader(headerNames[i]);
         }
         items.put(ITEMNAME_HEADERS, new CompositeDataSupport((CompositeType) type.getType(ITEMNAME_HEADERS), headerNames, headerValues));
      }

      return new CompositeDataSupport(type, items);
   }
}
