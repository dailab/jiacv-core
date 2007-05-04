package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.servicediscovery.IService;
import de.dailab.jiactng.agentcore.servicediscovery.IServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.ServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.ServiceParameter;

/**
 * AgentBean, die das Service-Interface und damit auch das Effector-Interface implementiert, d.h. Actions hostet. Der
 * Service soll Actions Anbieten, die dann als Service (also z.b. auch als WebService) exposable sind.
 * 
 * @author janko
 */
public class ExampleService extends AbstractAgentBean implements IService {

	public static final String SERVICE_GETTIME = "getTime";
	public static final String SERVICE_GETSYSINFO = "getSysInfo";
	public static final String SERVICE_TIME_DIFFERENCE = "getTimeDifference";

	public static final char SEPARATOR = ':';

	// erstmal nur lokal aufrufen und ergbins nach system.out schreiben
	public void doAction(DoAction actionName) {
		if (SERVICE_GETTIME.equals(actionName.getThisAction().getName())) {
			System.out.println(getTime());
		} else if (SERVICE_GETSYSINFO.equals(actionName.getThisAction().getName())) {
			System.out.println(getSysInfo());
		} else if (SERVICE_TIME_DIFFERENCE.equals(actionName.getThisAction().getName())) {
			System.out.println(getTimeDifference(new Date()).toString());
		}
	}

	/**
	 * Wird bei der AgentenInitalisierung aufgerufen, das Ergebnis gespeichert
	 */
	public ArrayList<Action> getActions() {
		ArrayList<Action> actionList = new ArrayList<Action>();
		actionList.add(new Action(SERVICE_GETTIME, this, new Class[0], new Class[0]));
		actionList.add(new Action(SERVICE_GETSYSINFO, this, new Class[0], new Class[0]));
		Class inputClasses[] = { Date.class };
		Class outputClasses[] = { Date.class };
		actionList.add(new Action(SERVICE_TIME_DIFFERENCE, this, inputClasses, outputClasses));
		return actionList;
	}

	// SERVICE_GETTIME
	// die WOrkermethode für diesen Service
	private Date getTime() {
		return (new GregorianCalendar()).getTime();
	}

	// SERVICE_GETSYSINFO
	// die WOrkermethode für diesen Service
	private String getSysInfo() {
		String s = System.getProperties().getProperty("os.name") + ':' + System.getProperties().getProperty("os.arch")
																						+ ':' + System.getProperties().getProperty("os.version");
		return s;
	}

	// n Service der eingabe und ausgabeparameter hat
	// die WOrkermethode für diesen Service
	private Date getTimeDifference(Date srcTime) {
		// mock
		return new Date();
	}

	/**
	 * Entsprechend der getActions-Methode wird eine Liste mit ServiceBeschreibungen erzeugt.
	 * 
	 * @return
	 */
	public List<IServiceDescription> getServiceDescriptions() {
		List<IServiceDescription> list = new ArrayList<IServiceDescription>();
		// aktuelles Datum/Zeit
		GregorianCalendar gc = new GregorianCalendar();
		// 3 minuten weiterstellen
		gc.roll(GregorianCalendar.MINUTE, 2);		
		
		String keywords[] = { "Time" };
		ServiceParameter spIn[] = {};
		ServiceParameter spOut[] = {};
		ServiceDescription sd = new ServiceDescription(gc.getTime(), createServiceId(SERVICE_GETTIME), SERVICE_GETTIME,
																						keywords, spIn, spOut, "", "", createServiceId(SERVICE_GETTIME), "", null);
		list.add(sd);
		
		// noch eine minute weiterstellen
		gc.roll(GregorianCalendar.MINUTE, 1);	
		keywords = new String[2];
		keywords[0] = "System";
		keywords[1] = "Info";
		spIn = new ServiceParameter[0];
		spOut = new ServiceParameter[0];
		sd = new ServiceDescription(gc.getTime(), createServiceId(SERVICE_GETSYSINFO), SERVICE_GETSYSINFO, keywords, spIn,
																						spOut, "", "", createServiceId(SERVICE_GETSYSINFO), "", null);
		list.add(sd);
		
		// noch eine Stunde weiterstellen
		gc.roll(GregorianCalendar.HOUR, 1);	
		keywords = new String[1];
		keywords[0] = "Time";
		spIn = new ServiceParameter[1];
		spIn[0] = new ServiceParameter("java.util.Date", "srcDate");
		spOut = new ServiceParameter[1];
		spOut[0] = new ServiceParameter("java.util.Date", "timeDiff");
		sd = new ServiceDescription(gc.getTime(), createServiceId(SERVICE_TIME_DIFFERENCE), SERVICE_TIME_DIFFERENCE, keywords, spIn,
																						spOut, "", "", createServiceId(SERVICE_TIME_DIFFERENCE), "", null);
		list.add(sd);
		return list;
	}

	/**
	 * Erzeugt n eindeutigen String für die Operation des Services
	 * 
	 * @param operationName der Operationsname
	 * @return eindeutiger String für die ServiceOperation
	 */
	private String createServiceId(String operationName) {
		return operationName + SEPARATOR + this.getClass().getSimpleName() + beanName + SEPARATOR
																						+ thisAgent.getAgentName() + SEPARATOR + thisAgent.getAgentNode().getName();
	}

	/**
	 * Service-Methode, ruft eine Service-Operation auf.
	 */
	public ServiceParameter[] invoke(String serviceName) {
		if (SERVICE_GETTIME.equals(serviceName)) {
			System.out.println(getTime());
		} else if (SERVICE_GETSYSINFO.equals(serviceName)) {
			System.out.println(getSysInfo());
		} else if (SERVICE_TIME_DIFFERENCE.equals(serviceName)) {
			System.out.println(getTimeDifference(new Date()).toString());
		}
		return null;
	}
}
