package de.dailab.jiactng.agentcore.comm;

import java.lang.reflect.Array;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

/**
 * Eine MessageListener, der den Consumern übergeben wird. Er soll nur auf Nachrichten aus Queues reagieren
 * 
 * @author janko
 */
public class QueueMessageListener implements MessageListener {
	IProtocolHandler _protocol;
	CommBean _commBean;
	Log log = LogFactory.getLog(getClass());

	public QueueMessageListener(IProtocolHandler protocol, CommBean commBean) {
		_protocol = protocol;
		_commBean = commBean;
	}

	/**
	 * Empfängt die JMS Nachrichten, leitet an die CommBean und an das Protocol weiter. momentan wird jede ObjektNachricht
	 * bestätigt.
	 */
	public void onMessage(Message msg) {
		if (log.isDebugEnabled()) {
			log.debug("JiacMessageListener msg received");
		}
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
		String payLoadString = convertPayLoadToString(jMsg.getPayload());
		System.out.println("Output:" + payLoadString);
		// System.out.println("Von:" + jMsg.getStartPoint() + " An:" + jMsg.getEndPoint() + " Content:"
		// + jMsg.getPayload().toString());
		System.out.println("AddressProperty: " + addressProperty);
	}

	private String convertPayLoadToString(Object payload) {
		StringBuffer sb = new StringBuffer();
		System.out.println(payload.getClass().getName());
		System.out.println(payload.getClass());
		if (payload.getClass().getName().equals("[C")) {
			char[] content = (char[]) payload;
			sb.append(content);
		}
		if (payload.getClass().equals(Character.TYPE)) {
			System.out.println("isn char");
		}
		if (payload.getClass().isArray()) {
			System.out.println("isn Array");
			System.out.println(Array.getLength(payload) + " einträge");
			System.out.println("Type ist:" + Array.get(payload, 0).getClass().getName());
			System.out.println("Type ist:" + payload.getClass().getComponentType());
		}
		return sb.toString();
	}
}
