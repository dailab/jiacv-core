package de.dailab.jiactng.agentcore.management.jmx;

import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

/**
 * Implementation of a configurable JAAS login module.
 * @author Jan Keiser
 */
public class JaasConfigurationEntry {

	private String loginModuleName;
	private String controlFlag;
	private Map<String,String> options;

	/**
	 * Getter for the name of the login module.
	 * @return The name of the login module.
	 */
	public String getLoginModuleName() {
		return loginModuleName;
	}

	/**
	 * Setter for the name of the login module.
	 * @param loginModuleName The name of the login module.
	 */
	public void setLoginModuleName(String loginModuleName) {
		this.loginModuleName = loginModuleName;
	}

	/**
	 * Getter for the control flag of the login module.
	 * @return One of "optional", "required", "requisite" or "sufficient".
	 */
	public String getControlFlag() {
		return controlFlag;
	}

	/**
	 * Setter for the control flag of the login module.
	 * @param controlFlag One of "optional", "required", "requisite" or "sufficient".
	 */
	public void setControlFlag(String controlFlag) {
		this.controlFlag = controlFlag;
	}

	/**
	 * Getter for options of the login module.
	 * @return The options of the login module as set of key-value pairs.
	 */
	public Map<String,String> getOptions() {
		return options;
	}

	/**
	 * Setter for options of the login module.
	 * @param options The options of the login module as set of key-value pairs.
	 */
	public void setOptions(Map<String,String> options) {
		this.options = options;
	}

}
