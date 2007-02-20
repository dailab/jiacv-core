/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiangtng.agentcore.knowledge;

import java.util.Set;

import org.sercho.masp.space.SimpleObjectSpace;

/**
 * @author Thomas Konnerth
 * @see de.dailab.jiangtng.agentcore.knowledge.Tuple
 */
public class Memory implements IMemory {

  private SimpleObjectSpace<Tuple> space   = new SimpleObjectSpace<Tuple>(
                                               "MySpace");

  private int                      timeOut = 10000;

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#out(de.dailab.jiangtng.agentcore.knowledge.Tuple)
   */
  public void out(Tuple tp) {
    space.write(tp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#in(de.dailab.jiangtng.agentcore.knowledge.Tuple)
   */
  public Tuple in(Tuple tp) {
    return space.remove(tp, timeOut);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#read(de.dailab.jiangtng.agentcore.knowledge.Tuple)
   */
  public Tuple read(Tuple tp) {
    return space.read(tp, timeOut);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#test(de.dailab.jiangtng.agentcore.knowledge.Tuple)
   */
  public Tuple test(Tuple tp) {
    return space.read(tp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#readAll(de.dailab.jiangtng.agentcore.knowledge.Tuple)
   */
  public Set<Tuple> readAll(Tuple tp) {
    return space.readAll(tp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#getTimeOut()
   */
  public int getTimeOut() {
    return timeOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiangtng.agentcore.knowledge.IMemory#setTimeOut(int)
   */
  public void setTimeOut(int timeOut) {
    if (timeOut < 0) {
      this.timeOut = Integer.MAX_VALUE;
    } else {
      this.timeOut = timeOut;
    }
  }
}
