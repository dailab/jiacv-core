package de.dailab.jiactng.agentcore.comm;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

/**
 * Eine MessageListener, der den Consumern übergeben wird.
 * Er soll nur auf Nachrichten aus Queues reagieren
 * @author janko
 */
public class QueueMessageListener implements MessageListener {
	IProtocolHandler _protocol;
	CommBean _commBean; 
	
	public QueueMessageListener(IProtocolHandler protocol, CommBean commBean) {
		_protocol = protocol;
		_commBean = commBean;
	}

	/**
	 * Empfängt die JMS Nachrichten, leitet an die CommBean und an das Protocol weiter.
	 * momentan wird jede ObjektNachricht bestätigt.
	 */
	public void onMessage(Message msg) {
		System.out.println(" JiacMessageListener msg received");
		if (msg != null) {
			try {
				ObjectMessage oMsg = (ObjectMessage) msg;
				String msgRecipient = oMsg.getStringProperty(Constants.ADDRESS_PROPERTY);
				debugMsg(oMsg, msgRecipient);
				// System.out
				// .println("An '" + msgReceipient + "' gerichtete Nachricht erhalten:\n
				// " + oMsg.getObject().toString());
				_commBean.messageReceivedFromQueue(msg);
				_protocol.processMessage(msg);
				msg.acknowledge();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void debugMsg(ObjectMessage msg, String addressProperty) throws JMSException {
		JiacMessage jMsg = (JiacMessage) msg.getObject();
		System.out.println("Von:" + jMsg.getStartPoint() + " An:" + jMsg.getEndPoint() + " Content:"
				+ jMsg.getPayload().toString());
		System.out.println("AddressProperty: " + addressProperty);
	}
}
