package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ServiceDescriptionTest extends TestCase {
	ServiceDescription desc = new ServiceDescription();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSetOutputParameter() {
		desc.setOutputParameter(null);
		List<ServiceParameter> resultList = desc.getOutputParameter();
		assertNull(resultList);
		List<ServiceParameter> list = new ArrayList<ServiceParameter>();
		desc.setOutputParameter(list);
		resultList = desc.getOutputParameter();
		assertNull(resultList);
		list = new ArrayList<ServiceParameter>();
		list.add(null);
		resultList = desc.getOutputParameter();
		assertNull(resultList);
	}

}
