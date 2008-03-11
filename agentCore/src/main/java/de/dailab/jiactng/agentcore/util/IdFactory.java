package de.dailab.jiactng.agentcore.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Collection of ID creation methods. TODO concept for id and naming.
 * 
 * @author axle
 */
public final class IdFactory {
    public static enum IdPrefix {
        Agent("a-"), Platform("p-"), Node("n-"), Session("s-");

        private final String _value;

        private IdPrefix(String value) {
            _value = value;
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * The default length of a session id. Use generate(int) if you want another length.
     */
    public final static int DEFAULT_LENGTH = 12;

    private final static int NANO_SLEEP = 5;

    private static Set<String> allocatedSessions = new HashSet<String>();

    /**
     * Constructor hidden to avoid instantiation.
     */
    private IdFactory() {
    }

    /**
     * Creates a id for an agent platform. Remember, platform in TNG is a virtual concept which acquaints agents with
     * each other regardless of the node they are running on.
     * 
     * @param hashcode
     *            the hashcode of the agent platform object
     * @return the id of the agent platform
     */
    public static String createPlatformId(int hashcode) {
        return createId(IdPrefix.Platform, hashcode);
    }

    /**
     * Creates a id for an agent node. Remember, agent node coresponds to the Java VM the agent is living in.
     * 
     * @param hashcode
     *            the hashcode of the agent node object
     * @return the id of the agent node
     */
    public static String createAgentNodeId(int hashcode) {
        return createId(IdPrefix.Node, hashcode);
    }

    /**
     * Creates a id for an agent.
     * 
     * @param hashcode
     *            the hashcode of the agent object
     * @return the id of the agent
     */
    public static String createAgentId(int hashcode) {
        return createId(IdPrefix.Agent, hashcode);
    }

    /**
     * Creates a id for a session.
     * 
     * @param hashcode
     *            the hashcode of the action or service object.
     * @return the session id
     */
    public static String createSessionId(int hashcode) {
        return createId(IdPrefix.Session, hashcode);
    }

    public static String createId(IdPrefix prefix, int hashcode) {
        return new StringBuilder(prefix.toString()).append(Long.toHexString(System.currentTimeMillis() + hashcode))
                .toString();
    }

    /**
     * Generates a session id with the specific length. The session consists of digits and chars a to f. A session will
     * not be generated twice. If all possible session id with the specific length are allocated this methode will not
     * return and hangs in a infinite loop.
     * 
     * @param length
     *            the length of the generated id
     * @return a string representing a random and unique session id.
     */
    public static String generate(int length) {
        StringBuilder buffer = new StringBuilder("");
        String output = null;

        Random random = new Random();
        int oneRandomInt = 0;

        for (int i = 0; i < length; i++) {
            oneRandomInt = random.nextInt(16);
            if (oneRandomInt == 10) {
                buffer.append("a");
            } else if (oneRandomInt == 11) {
                buffer.append("b");
            } else if (oneRandomInt == 12) {
                buffer.append("c");
            } else if (oneRandomInt == 13) {
                buffer.append("d");
            } else if (oneRandomInt == 14) {
                buffer.append("e");
            } else if (oneRandomInt == 15) {
                buffer.append("f");
            } else {
                buffer.append(oneRandomInt);
            }

            try {
                // for very small NANOs trustless
                Thread.sleep(0, NANO_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        output = buffer.toString();

        // solange output schon im Set existiert, soll
        // eine neue Session generiert werden
        // und zu output zugewiesen werden.
        while (allocatedSessions.contains(output)) {
            // koennte auch ne Katastrophe auf dem Stack geben, wenn alle IDs schon vergeben sind
            output = generate(length);
        }
        allocatedSessions.add(output);

        return output;
    }

    /**
     * Generates a session id with the default session id length. This methode calls generate(int).
     * 
     * @return a string representing a random and unique session id with the default length.
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * If a session id not be needed any more, the session can be devaluated with this methode and other processes can
     * allocate this session id. If this not will be done, in long life (long running) systems the amount of available
     * unique session ids could be exhausted. This depends on the session id length and the amount of processes with a
     * unique session id.
     * 
     * @param session
     *            removes a sessionID from the list of sessions, so it can be reused.
     */
    public static void devaluate(String session) {
        allocatedSessions.remove(session);
    }

}
