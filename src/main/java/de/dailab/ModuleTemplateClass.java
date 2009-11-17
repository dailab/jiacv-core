package de.dailab;

/**
 * Just to have a class.
 * 
 * @author axle
 *
 */
public class ModuleTemplateClass {
	private String var;
	
	/**
	 * The default constructor sets the var.
	 */
	public ModuleTemplateClass() {
		var="DefaultConstructor";
	}
	
	/**
	 * The parameterized constructor sets the var according to the var parameter.
	 * This is an instance of Dependency Injection through constructor :)
	 * 
	 * @param var the value to set
	 */
	public ModuleTemplateClass(String var) {
		this.var=var;
	}

	/**
	 * Get the var.
	 * 
	 * @return the value of the var
	 */
	public String getVar() {
		return var;
	}

	/**
	 * Set the var.
	 * 
	 * @param var the value to set
	 */
	public void setVar(String var) {
		this.var = var;
	}
}
