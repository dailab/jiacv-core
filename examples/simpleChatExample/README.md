This is a JIAC V example application based on AgentCore features only. 

This application shows in one AgentBean and one IFact the power of JIAC V 
to write distributed applications - this is a chat application für local 
networks. 

Starting
========

 * as application: run 'mvn package' and find a start script in {project}/target/appassembler/bin named 'SimpleChat' (Linux/Mac) and 'SimpleChat.bat' (Windows). 

 * as developer: use the starter Java main class 'de.dailab.jiactng.examples.SimpleChatExample'. It is just for debugging use in for IDE. 

Contents
========

This example uses the JIAC V group addresses between agents to share messages. 
The agent's register to a common communication group address and listen at this 
address to get new messages, that should be displayed. 

For this use, the contained agent bean observices the agent's memory for changes. 
The JIAC connunication bean will be used to send new messages to all registered 
agent's. 

Java classes
------------
 * ChatMessage - a IFact class to hold all needed information about 'chat messages'
 * SimpleChatAgentBean - implemented funcionality for sending and receiving chat messages
 * SimpleChatUI - a simple AWT/Swing graphical user interface to send and display chat messages. 
 * SimpleChatExample - a degugging starter for your IDE

Configurations
--------------
 * pom.xml - contains JIAC V dependencies and a configuration to create appassember application with starter scripts. 
 * simplechatnode.xml - the configuration of JIAC V agent containing one agent as users avatar in the JIAC V world. 

