package de.dailab.jiactng.agentcore.management.jmx;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

/**
 * Implementation of a JMX authenticator based on a password file.
 * @author Jan Keiser
 */
public class PasswordFileAuthenticator implements JMXAuthenticator {

	private Properties passwords;

	/**
	 * Authenticates a management client with the given client credentials.
	 * @param credentials a string array containing user name and password.
	 * @return the authenticated subject containing its associated principals.
	 */
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
        String password = passwords.getProperty(aCredentials[0]);
        if (!aCredentials[1].equals(password)) {
        	throw new SecurityException("Authentication failed");
        }
        
		// transform to JMX principals
		final HashSet<JMXPrincipal> jmxPrincipals = new HashSet<JMXPrincipal>();
		jmxPrincipals.add(new JMXPrincipal(aCredentials[0]));

        return new Subject(true, jmxPrincipals, Collections.EMPTY_SET, Collections.EMPTY_SET);
	}

	/**
	 * Setter for name of password file.
	 * @param filename The name of the password file.
	 * @throws IOException if the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
	 * @throws IllegalArgumentException if a malformed Unicode escape appears in the file.
	 */
	public void setPasswordFile(String filename) throws IOException {
		passwords = new Properties();
		passwords.load(new FileReader(filename));
	}

}
