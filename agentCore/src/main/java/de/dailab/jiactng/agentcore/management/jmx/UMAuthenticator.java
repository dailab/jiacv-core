package de.dailab.jiactng.agentcore.management.jmx;

import java.util.Collections;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import de.dailab.usermanagement.webservice.UserManagerService;
import de.dailab.usermanagement.wsclient.UMWSFactory;

/**
 * A JMX-compliant authenticator for protection of the remote management interface.
 * It uses a webservice of the user management to check the validity of the
 * user credentials. 
 * 
 * @author Jan Keiser
 */
public class UMAuthenticator implements JMXAuthenticator {

	private UserManagerService servicePort = null;

	/**
	 * Authenticates a JMX user.
	 * @param credentials an array containing username and password
	 * @return a subject of the successfully authenticated JMX principal
	 * @throws SecurityException if the authentication failed
	 * @see de.dailab.usermanagement.client.stubs.UserAdministration_PortType#authenticateUser(String, String, String)
	 */
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

        // Perform authentication
        String username = (String) aCredentials[0];
        String password = (String) aCredentials[1];
		boolean result = false;
		boolean exception = true;
		try {
			result = servicePort.validateUser(username, password);
			exception = false;
		} catch (NullPointerException e) {
			System.err.println("No URL specified for UMAuthenticator");
		} catch (Exception e) {
			System.err.println(e.toString());
		}

        if (result) {
            return new Subject(true,
                               Collections.singleton(new JMXPrincipal(username)),
                               Collections.EMPTY_SET,
                               Collections.EMPTY_SET);
        } else if (exception) {
        	throw new SecurityException("Defective authentication");
        } else {
            throw new SecurityException("Invalid credentials");
        }
	}

	public void setUrl(String url) {
		servicePort = UMWSFactory.getUserManagerService(url);
	}
}
