package de.dailab;

import junit.framework.TestCase;

public class ModuleTemplateClassTest extends TestCase {
	public void testDefaultConstructor() {
		ModuleTemplateClass mtc=new ModuleTemplateClass();
		assertEquals("DefaultConstructor", mtc.getVar());
	}
	
	public void testParamConstructor() {
		ModuleTemplateClass mtc=new ModuleTemplateClass("ParamConstructor");
		assertEquals("ParamConstructor", mtc.getVar());
	}
	
	public void testGetSet() {
		ModuleTemplateClass mtc=new ModuleTemplateClass();
		mtc.setVar("GetSet");
		assertEquals("GetSet", mtc.getVar());
	}
}
