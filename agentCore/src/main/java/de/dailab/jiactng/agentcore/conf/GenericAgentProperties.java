package de.dailab.jiactng.agentcore.conf;


/**
 * This class is needed to add multiple agents to a node just from one configuration using Spring.
 * It contains all parameters needed to create the agents. To use it declare it as a bean in one of 
 * your tng configuration files. See the JUnit test GenericAgentPropertiesInjection for an example.
 * 
 * Please note that the agent bean definition must be put in a separate file (given with setAgentConfig) 
 * and that the file containing the agentNode definiton must not contain any (spring) reference to this file. 
 * otherwise you will get an infinite loop.
 * 
 * @author thiele
 *
 */
public class GenericAgentProperties {
	
	private int count;
	private String agentNamePattern;
	private String agentConfig;
	private String agentBeanName;
	
	
	/**
	 * @return count, returns the number of agents that will be created from this definition
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count, sets the number of agents that will be created from this definition
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return agentNamePattern, returns the pattern used to create the name of an Agent
	 * @see #createAgentName(int) for details
	 */
	public String getAgentNamePattern() {
		return agentNamePattern;
	}
	/**
	 * @param agentNamePattern, sets the pattern used to create the name of an Agent
	 *  
	 */
	public void setAgentNamePattern(String agentNamePattern) {
		this.agentNamePattern = agentNamePattern;
	}
	/**
	 * @return agentConfig, returns the spring configuration file. 
	 */
	public String getAgentConfig() {
		return agentConfig;
	}
	
	/**
	 * @param agentConfig, sets the spring configuration file
	 * This file is supposed to contain the bean definition of the agent including all referenced beans. 
	 * The name of the bean definition is given via the AgentBeanName property.
	 */
	public void setAgentConfig(String agentConfig) {
		this.agentConfig = agentConfig;
	}

	/**
	 * @return agentBeanName, returns the name of the agent bean.
	 */
	public String getAgentBeanName() {
		return agentBeanName;
	}
	
	/**
	 * @param agentBeanName, sets the bean name which is supposed to contain the agent definition.
	 * this definition will be used to create the instances of the agents, thus is can be seen as a template for all agents.
	 */
	public void setAgentBeanName(String agentBeanName) {
		this.agentBeanName = agentBeanName;
	}
	
	/**
	 * @param number, the number of the current iteration cycle or any other number if wanted
	 * @return agent name, returns a name that is created by replacing any occurrence of "#{count}" with the number.
	 * Currently the patter simple replaces the any occurrences of "#{count}" with the current iteration number, starting with 1.
	 * Thus, given the pattern "name#{count}" the names of the agents would be name1, name2, ... name n.  
	 */
	public String createAgentName(int number) {
		return(agentNamePattern.replace("#{count}", String.valueOf(number)));
	}
	
}
