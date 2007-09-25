/*
 * $Id$ 
 */
package de.dailab.jiactng.examples.chat;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class Tester {
    public static void main(String[] args) {
        AbstractApplicationContext context= new ClassPathXmlApplicationContext(new String[]{"de/dailab/jiactng/examples/chat/chatAgentTemplate.xml"}, false);
        PropertyPlaceholderConfigurer configurer= new PropertyPlaceholderConfigurer();
        Properties properties= new Properties();
        properties.setProperty("communicatorName", "Blubber");
        configurer.setProperties(properties);
        context.addBeanFactoryPostProcessor(configurer);
        context.refresh();
        
        GenericApplicationContext blubb= new GenericApplicationContext();
        
        for(String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }
}
