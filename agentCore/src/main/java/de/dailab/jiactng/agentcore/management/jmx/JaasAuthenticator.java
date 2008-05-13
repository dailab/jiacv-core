package de.dailab.jiactng.agentcore.management.jmx;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Implementation of a configurable JAAS-based JMX authenticator.
 * @author Jan Keiser
 */
public class JaasAuthenticator implements JMXAuthenticator {

	protected Configuration configuration;
	
	@Override
	public Subject authenticate(Object credentials) {
        // verify that credentials is of type String[]
        if (!(credentials instanceof String[])) {
            // Special case for null so we get a more informative message
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }

        // verify that the array contains two elements (username/password)
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length != 2) {
            throw new SecurityException("Credentials should have 2 elements");
        }
        
        // perform authentication
		try {
			LoginContext lc = new LoginContext("LoginJaas", null, 
					new JaasCallbackHandler(aCredentials[0], aCredentials[1].toCharArray()), 
					configuration);
			lc.login();
			Set<Principal> principals = lc.getSubject().getPrincipals();

			// transform to JMX principals
			HashSet<JMXPrincipal> jmxPrincipals = new HashSet<JMXPrincipal>();
			for (Principal principal : principals) {
				jmxPrincipals.add(new JMXPrincipal(principal.getName()));
			}

			lc.logout();
            return new Subject(true, jmxPrincipals, Collections.EMPTY_SET, Collections.EMPTY_SET);
		} catch (LoginException le) {
		    le.printStackTrace();
		    throw new SecurityException("Authentication failed");
		} catch (SecurityException se) {
		    se.printStackTrace();
		    throw se;
		}
	}

	/**
	 * Setter for configuration of the authenticator.
	 * @param entries The list of configured JAAS login modules.
	 */
	public void setConfiguration(List<JaasConfigurationEntry> entries) {
		configuration = new JaasConfiguration(entries);
	}

	/**
	 * Setter for system properties which are needed for a login module.
	 * @param properties The system properties as a set of key-value pairs.
	 */
	public void setSystemProperties(Map<String,String> properties) {
		for (Entry<String,String> property : properties.entrySet()) {
			System.setProperty(property.getKey(), property.getValue());
		}
	}

	/**
	 * This class represents a JAAS configuration.
	 * @author Jan Keiser
	 */
	private class JaasConfiguration extends Configuration {

		private AppConfigurationEntry[] entries;

		/**
		 * Creates a JAAS configuration with a list of configured login modules.
		 * @param entries The list of configured JAAS login modules.
		 */
		public JaasConfiguration(List<JaasConfigurationEntry> entries) {
			int size = entries.size();
			this.entries = new AppConfigurationEntry[size];
			for (int i=0; i<size; i++) {
				JaasConfigurationEntry entry = entries.get(i);
				String controlFlag = entry.getControlFlag();
				AppConfigurationEntry.LoginModuleControlFlag flag = null;
				if (controlFlag.equals("optional")) {
					flag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
				}
				else if (controlFlag.equals("required")) {
					flag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
				}
				else if (controlFlag.equals("requisite")) {
					flag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
				}
				else if (controlFlag.equals("sufficient")) {
					flag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
				}
				this.entries[i] = new AppConfigurationEntry(
						entry.getLoginModuleName(),
						flag,
						entry.getOptions());
			}
		}

		@Override
		public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
			return entries;
		}
	}

	/**
	 * Implementation of a callback handler which uses name and password from the
	 * JMX authenticator.
	 * @author Jan Keiser
	 */
	private class JaasCallbackHandler implements CallbackHandler {

		private String username;
		private char[] password;

		/**
		 * Creates a JAAS callback handler which provides username and password.
		 * @param username The login name of the user.
		 * @param password The password of the user.
		 */
		public JaasCallbackHandler(String username, char[] password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
	        for (int i = 0; i < callbacks.length; i++) {
	            if (callbacks[i] instanceof TextOutputCallback) {
	  
	                // display the message according to the specified type
	                TextOutputCallback toc = (TextOutputCallback)callbacks[i];
	                switch (toc.getMessageType()) {
	                case TextOutputCallback.INFORMATION:
	                    System.out.println(toc.getMessage());
	                    break;
	                case TextOutputCallback.ERROR:
	                    System.out.println("ERROR: " + toc.getMessage());
	                    break;
	                case TextOutputCallback.WARNING:
	                    System.out.println("WARNING: " + toc.getMessage());
	                    break;
	                default:
	                    throw new IOException("Unsupported message type: " + toc.getMessageType());
	                }

	            } else if (callbacks[i] instanceof NameCallback) {
	  
	                // provide username
	                NameCallback nc = (NameCallback)callbacks[i];
	                nc.setName(username);

	            } else if (callbacks[i] instanceof PasswordCallback) {
	  
	                // provide sensitive information
	                PasswordCallback pc = (PasswordCallback)callbacks[i];
	                pc.setPassword(password);
	  
	            } else {
	                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
	            }
	        }
		}
	}
}
