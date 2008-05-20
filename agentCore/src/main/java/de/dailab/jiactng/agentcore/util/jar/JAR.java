package de.dailab.jiactng.agentcore.util.jar;

import java.io.InputStream;

/**
 * Interface for accessing jars.
 * @author Ralf Sesseler
 */
public interface JAR {

	  /**
	   * The name of the jar.
	   * @return The filename of the jar (absolute or class path relative).
	   **/
	  public String getJarName();

	  /**
	   * Gets input stream for entry in jar.
	   * @param name The file name of the jar entry.
	   * @return Input stream to read the jar entry.
	   **/
	  public InputStream getInputStream(String name);
	  
	  /**
	   * Looks in the internal index whether the given resource exists in this JAR or not.
	   * @param resource The filename of the jar entry.
	   * @return <code>true</code> if the entry exists, <code>false</code> otherwise.
	   */
	  public boolean constainsResource(String resource);
}
