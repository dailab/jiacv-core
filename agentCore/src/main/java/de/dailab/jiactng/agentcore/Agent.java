/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.Tuple;
import de.dailab.jiactng.agentcore.lifecycle.Lifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleListener;

public class Agent implements ApplicationContextAware, BeanNameAware,
    LifecycleListener {

  private String                agentName  = null;

  private IMemory               memory     = null;

  private ArrayList<AAgentBean> adaptors   = null;

  private ApplicationContext    appContext = null;

  public static void main(String[] args) {
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        args[0]);

    // Object memBean = appContext.getBean("memory");
  }

  public IMemory getMemory() {
    return memory;
  }

  public void setMemory(IMemory memory) {
    this.memory = memory;
  }

  public ArrayList getAdaptors() {
    return adaptors;
  }

  public void setAdaptors(ArrayList adaptors) {
    this.adaptors = adaptors;
    if (appContext != null) initBeans();
    for (AAgentBean a : this.adaptors) {
      if (a instanceof Lifecycle) a.addLifecycleListener(this);
    }
  }

  private void initBeans() {
    memory.out(new Tuple("thisAgent", agentName));
    Iterator<AAgentBean> it = adaptors.iterator();
    while (it.hasNext()) {
      AAgentBean bean = it.next();
      System.err.println("### bean: " + bean.getClass().toString());
      memory.out(new Tuple(this.agentName + "." + bean.getClass().getName(),
          bean.beanName));
      bean.execute();
      try {
        bean.init();
        bean.start();
      } catch (LifecycleException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  public void setApplicationContext(ApplicationContext arg0)
      throws BeansException {
    appContext = arg0;
    if (adaptors != null) initBeans();
  }

  public void setBeanName(String arg0) {
    this.agentName = arg0;
  }

  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

}
