A Simple Chat Example for JIAC-TNG.

What this Example will do:
--------------------------

It will create a new AgentNode - a platform for agents
A Console is opened giving the user the possibility to add and remove agents to the node and listing these allready on the node.

After adding an agent the user is prompted to give this agent a name. This name will by his address within the chat.
This Address may be changed later during runtime using the GUI.

An Agent may be removed in a mostly similar way. To remove an agent his name is needed. The Name of the Agent isn't changed when the address changes.

If the User types quit into the console the agents and the platform will be shut down.



 What the Files within the Examples do:
 --------------------------------------
 SimpleChatExample.java
 	this File manages the platform, and console.
 	It's the main file to start with.
 	
 ChatGuiBean.java
 	this File manages the GUI parsing userinputs and managing sending and receiving messages through the ActiveMQ-Broker.
 
 TestContent.java
 	this File is not actually used at the moment. It allows messageing using strings instead of byteArrays.
 	providing an example for alternative messagePayloads too.
 	
 chatNode.xml
 	Springcontext used for creating the AgentNode used for the SimpleChatExample
 	
 chatAgentTemplate.xml
 	Springcontext used for creating the Agents used on the Node.
 	
 myLog4j.properties
 	Log4j configurationfiles allowing the declare the current level of logging during the exampleruntime.
 
 What is needed too: the package of Agentcore from Jiac-TNG. All messages will be sent and received using the CommunicationBean.
 
 
  