package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;

/**
 * Implementation of a Simple JVM message transport. Uses a static HashMap for
 * storing address registrations and forwards messages to the subscribers by
 * direct reference.
 * 
 * @author moekon
 * 
 */
public class SimpleMessageTransport extends MessageTransport {

  /**
   * Static HashMap for storing adresses and listeners. The HashMap exsists only
   * once per JVM and stores all listeners for all addresses. Contents are as
   * follows: for each key address (messageBox or groupAddress) a HashMap value
   * is stored. The stored HashMap contains the receivers as keys, and a set of
   * patterns that they registered for.
   */
  private static HashMap<ICommunicationAddress, HashMap<SimpleMessageTransport, Set<IJiacMessage>>> addressToListenerMap = new HashMap<ICommunicationAddress, HashMap<SimpleMessageTransport, Set<IJiacMessage>>>();

  public SimpleMessageTransport() {
    this("simpleMessaging");
  }

  protected SimpleMessageTransport(String transportIdentifier) {
    super(transportIdentifier);
  }

  @Override
  public void doInit() throws Exception {
    super.doInit();

  }

  @Override
  public void doCleanup() throws Exception {
    super.doCleanup();
  }

  @Override
  public void send(IJiacMessage message, ICommunicationAddress address, long timeToLive) throws CommunicationException {

    synchronized (addressToListenerMap) {
      boolean messageDelegated = false;

      // get all listeners for the address
      HashMap<SimpleMessageTransport, Set<IJiacMessage>> listenerToSelectorMap = addressToListenerMap.get(address);

      if (listenerToSelectorMap == null) {
        // no lister found, so nothing to do
        log.warn("No receivers found for message: " + message + " with address: " + address);
        return;
      }

      // check the templates for each listener
      for (Entry<SimpleMessageTransport, Set<IJiacMessage>> entry : listenerToSelectorMap.entrySet()) {
        Set<IJiacMessage> selectorSet = entry.getValue();

        if (selectorSet == null) {
          // no selectors found - should not really happen, as empty selectors
          // are set to JIACMessage template by the listen-method
          log.warn("Empty selectorSet! No receivers found for message: " + message + " with address: " + address);
          continue;
        }

        for (IJiacMessage selector : selectorSet) {
          if (matchingMessageHeaders(selector, message)) {
            try {
              // create a copy of the message in order to decouple local
              // references
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream(baos);
              oos.writeObject(message);
              oos.flush();
              baos.flush();

              ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
              ObjectInputStream ois = new ObjectInputStream(bais);
              Object obj = ois.readObject();
              IJiacMessage newMessage = (JiacMessage) obj;
              oos.close();
              baos.close();

              ois.close();
              bais.close();

              // call receiver with message
              entry.getKey().delegateMessage(newMessage, address);
              messageDelegated = true;

            } catch (Exception ex) {
              ex.printStackTrace();
              log.error(ex);
            }

            break;
          }
        }

      } // end destination loop

      if (!messageDelegated) {
        log.warn("No receivers with matching selector found for message: " + message + " with address: " + address);
      }

    }

  }

  @Override
  public void listen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException {

    // for empty selectors, a JIACMessage template is created to avoid
    // null-values
    if (selector == null) {
      selector = new JiacMessage();
    }

    synchronized (addressToListenerMap) {
      // fetch existing listener list for address from hashMap
      HashMap<SimpleMessageTransport, Set<IJiacMessage>> listenerToSelectorMap = addressToListenerMap.get(address);

      if (listenerToSelectorMap == null) {
        // no listeners found for this address, create new list
        listenerToSelectorMap = new HashMap<SimpleMessageTransport, Set<IJiacMessage>>();
      }

      // fetch existing selector set for this listener
      Set<IJiacMessage> selectorSet = listenerToSelectorMap.get(this);

      if (selectorSet == null) {
        // no selectors found, so create new set
        selectorSet = new HashSet<IJiacMessage>();
      }

      // add new selector and new listener and store everything in the maps
      selectorSet.add(selector);
      listenerToSelectorMap.put(this, selectorSet);
      addressToListenerMap.put(address, listenerToSelectorMap);
    }

  }

  @Override
  public void stopListen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException {

    // for empty selectors, a JIACMessage template is created to avoid
    // null-values
    if (selector == null) {
      selector = new JiacMessage();
    }

    synchronized (addressToListenerMap) {

      // fetch existing listener list for address from hashMap
      HashMap<SimpleMessageTransport, Set<IJiacMessage>> listenerToSelectorMap = addressToListenerMap.get(address);
      if (listenerToSelectorMap == null) {
        log.warn("Found no listeners for address: " + address + " Skipping deregistration.");
        return;
      }

      // fetch existing selector set for this listener
      Set<IJiacMessage> selectorSet = listenerToSelectorMap.get(this);
      if (selectorSet == null) {
        log.warn("Found no selectors for address: " + address + " Skipping deregistration.");
        return;
      }

      selectorSet.remove(selector);

      // check if selectors are empty
      if (selectorSet.size() <= 0) {
        listenerToSelectorMap.remove(this);
      } else {
        listenerToSelectorMap.put(this, selectorSet);
      }

      // check if listeners are empty
      if (listenerToSelectorMap.size() <= 0) {
        addressToListenerMap.remove(address);
      } else {
        addressToListenerMap.put(address, listenerToSelectorMap);
      }

    }
  }

  /**
   * Check if the header of the received message matches the selector. This is
   * so, if all non-null values of the selector header are present and equal in
   * the message-header. null-values in the selector header, as well as
   * additional keys in the message are ignored.
   * 
   * @param selector
   *          the selector template
   * @param message
   *          the received message
   * @return true, if all key-value pairs in the selector are also set in the
   *         received message
   */
  private boolean matchingMessageHeaders(IJiacMessage selector, IJiacMessage message) {
    for (String selectorKey : selector.getHeaderKeys()) {
      String selectorValue = selector.getHeader(selectorKey);
      String messageValue = message.getHeader(selectorKey);

      // ignore null-values
      if (selectorValue == null) {
        continue;
      }

      // mismatch for one message value (either message-value is null, or not
      // equals) -> message does not match
      if ((messageValue == null) || !selectorValue.equals(messageValue)) {
        return false;
      }
    }

    return true;
  }

}
