# JIAC V - Core

Copyright 2007 - 2024, DAI-Labor, TU Berlin

* Main contributors: Axel Heßler, Thomas Konnerth, Jan Keiser
* Additional contributors (also see `pom.xml` file and Git history): Michael Burkhardt, Janko Dimitrow, Jaschar Domann, Joachim Fuchs, Silvan Kaiser, Tobias Küster, Martin Löffelholz, Marco Lützenberger, Nils Masuch, Marcel Patzlaff, Christian Rakow, Alexander Thiele, Jakob Tonn, Hilmi Yildirim

Please refer to the [License](LICENSE.txt) and [Contributing](CONTRIBUTING.md) files for how to use and contribute to this project. For a changelog, please refer to [changes.xml](src/changes/changes.xml).

## About

JIAC V (Java-based Intelligent Agent Componentware, version 5) is a Java-based agent architecture and framework that eases the development and the operation of large-scale, distributed applications and services. The framework supports the design, implementation, and deployment of software agent systems. The entire software development process, from conception to deployment of full software systems, is supported by JIAC. It also allows for the possibility of reusing applications and services, and even modifying them during runtime.The focal points of JIAC are distribution, scalability, adaptability and autonomy. JIAC V applications can be developed using extensive functionality that is provided in a library. This library consists of already-prepared services, components, and agents which can be integrated into an application in order to perform standard tasks. The individual agents are based on a component architecture, which already provides the basic functionality for communication and process management. Application-specific functionality can be provided by the developer and be interactively integrated.

In addition to the common JIAC V-Framework, domain-specific extensions have been implemented that facilitate the quick and simple development of domain applications such as Next Generation Services.

### Our Philosophy

* Easy creation of complex, scalable distributed systems
* Quick to learn for developers familiar with Java
* Integration of agents and service-oriented architecture
* Abstraction from underlying network architecture
* Transparent communication across the network
* Many extensions for: migration, persistence, encryption, reactive behavior, service matching, load balancing, …
* Built on and around proven standards: Core configuration: Java, Spring, Maven; Communication: ActiveMQ, JMS; Semantics: OWL, OWL-S, SWRL; Interfaces: JMX, REST, Webservices


## Modules

This repository contains the "core" components of JIAC V. Additional components for advanced functionalities as well as different development and runtime tools can be found in other repositories.

This repository contains the following modules:

* **gateway**: For communication between nodes.
* **agentCoreAPI**: Different interfaces and basic model classes without using any external dependencies.
* **agentCore**: The actual implementation of the JIAC V agent framework, including message based communications, agent lifecyle, actions, etc.
* **AgentUnit**: Unit testing for JIAC V agents.
* **examples**: Various examples demonstrating the use of the basic JIAC V features


## Quick Start Guide

We recommend using the Apache Maven system. JIAC V can be downloaded and installed from the DAI-Open repository:

* https://repositories.dai-labor.de/extern/content/repositories/dai-open/

To use JIAC in a Maven project, simply add the DAI-Open repository and the AgentCore dependency to the project's `pom.xml` file as shown in this code snippet:

```xml
<dependencies>
   <dependency>
	<groupId>de.dailab.jiactng</groupId>
	<artifactId>agentCore</artifactId>
	<version>5.2.4</version>
   </dependency>
</dependencies>
<repositories>
   <repository>
	<id>dai-open</id>
	<name>DAI Open</name>
	<url>https://repositories.dai-labor.de/extern/content/repositories/dai-open</url>
	<releases>
		<enabled>true</enabled>
	</releases>
	<snapshots>
		<enabled>false</enabled>
	</snapshots>
   </repository>
</repositories>
```

Alternatively, if you do not use Maven, you can download the JIAC V all-in-one Jar (see column to the right) and use that as a dependency in your projects. For details on installing and using JIAC V, please consult the manual.

For detailed documentation, guides, tutorials, etc. please see the list of references below.


## References

* [DAI-Labor website](https://dai-labor.de/)
* [JIAC V development group (internal)](https://gitlab.dai-labor.de/jiacv)
* [JIAC V on JIAC.de](https://www.jiac.de/agent-frameworks/jiac-v/)
  * [Manual (PDF)](https://jiac.de/Downloads/jiac/jiac_manual.pdf)
  * [Introduction Slides (PDF)](https://jiac.de/Downloads/jiac/JIAC-Intro.pdf)
* [DAI-Open Maven Repository](https://repositories.dai-labor.de/extern/content/repositories/dai-open/)
  * [JIAC V Developer Documentation](https://repositories.dai-labor.de/jiactng/5.2.4/)
  * [JIAC V Javadocs](https://repositories.dai-labor.de/jiactng/5.2.4/apidocs/index.html)


## Selected Publications

Marco Lützenberger, Thomas Konnerth, Tobias Küster: Programming of Multiagent Applications with JIAC In: Leitão, P. and Karnouskos, S. (Eds.) Industrial Agents — Emerging Applications of Software Agents in Industry, Elsevier, 2015, 381-400

Marco Lützenberger, Tobias Küster, Nils Masuch, Jakob Tonn, Sahin Albayrak:
Engineering JIAC Multi-Agent Systems (Demonstration) In: Alessio Lomuscio, Paul Scerri, Ana Bazzan, and Michael Huhns (eds.) Proceedings of the 13th International Conference on Autonomous Agents and Multiagent Systems (AAMAS 2014), Paris, France.

Marco Lützenberger, Tobias Küster, Thomas Konnerth, Alexander Thiele, Nils Masuch, Axel Heßler, Jan Keiser, Michael Burkhardt, Silvan Kaiser, Jakob Tonn, Sahin Albayrak: Engineering Industrial Multi-Agent Systems: The JIAC V Approach In: Engineering Multi-Agent Systems (EMAS), AAMAS 2013
Workshop, Saint Paul, MN, USA.

Marco Lützenberger, Tobias Küster, Thomas Konnerth, Alexander Thiele, Nils Masuch, Axel Heßler, Jan Keiser, Michael Burkhardt, Silvan Kaiser, Jakob Tonn, Sahin Albayrak: Industrial Process Optimisation with JIAC (Demonstration) In: Proceedings of the 12th International Conferences on Autonomous Agents and Multiagent Systems (AAMAS 2013), Saint Paul, MN, United States of America.

Marco Lützenberger, Tobias Küster, Thomas Konnerth, Alexander Thiele, Nils Masuch, Axel Heßler, Jan Keiser, Michael Burkhardt, Silvan Kaiser, Jakob Tonn, Sahin Albayrak: JIAC V – A MAS Framework for Industrial Applications In: Proceedings of the 12th International Conferences on Autonomous Agents and Multiagent Systems (AAMAS 2013), Saint Paul, MN, United States of America.

Silvan Kaiser, Michael Burkhardt und Jakob Tonn: Drag-and-Drop Migration: An Example of Mapping User Actions to Agent Infrastructures. 1st International Workshop on Infrastructures and Tools for Multiagent Systems (ITMAS 2010), 2010, Toronto, Canada.

Benjamin Hirsch, Thomas Konnerth und Axel Heßler: Merging Agents and Services – the JIAC Agent Platform. In: Bordini, Dastani, Dix, El Fallah Seghrouchni: Multi-Agent Programming: Languages, Tools and Applications, pp. 159-185. Springer, 2009.

Alexander Thiele, Thomas Konnerth, Silvan Kaiser, Jan Keiser und Benjamin Hirsch:Applying JIAC V to real world problems: The MAMS case. In: Multiagent System Technologies, volume 7 of Lecture Notes in Artificial Intelligence, pages 268-277, Hamburg, Germany, September 2009.

Thomas Konnerth, Silvan Kaiser, Alexander Thiele, and Jan Keiser, MAMS service framework. In: AAMAS ’09: Proceedings of The 8th International Conference on Autonomous Agents and Multiagent Systems, pages 1351-1352, Richland, SC, May, 10- 15 2009. International Foundation for Autonomous Agents and Multiagent Systems.

Thomas Konnerth, S. Schenk, Silvan Kaiser und Joachim Fuchs. AESOA – An Agent Enabled Service Oriented Architecture. In: IEEE SOCA 2007
