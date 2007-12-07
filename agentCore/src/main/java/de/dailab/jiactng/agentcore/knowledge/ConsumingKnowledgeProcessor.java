/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

/**
 * This consuming knowledge processor should be used when new knowledge in the memory
 * should be consumed and processed exclusively.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ConsumingKnowledgeProcessor<P extends IFact> implements IKnowledgeHandler<P> {
    protected class ConsumingSpaceObserver implements SpaceObserver<IFact> {
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent<? extends IFact> writeEvent= (WriteCallEvent<? extends IFact>) event;
                
                P template= (P)writeEvent.getObject();
                
                if(!factClass.isAssignableFrom(template.getClass())) {
                    // ignore matches which does not fit to the specific fact type
                    return;
                }
                
                P knowledge;
                synchronized(associatedMemories) {
                    IMemory currentMemory= associatedMemories.get(writeEvent.getSource());
                    if(currentMemory != null) {
                        // consume the knowledge
                        knowledge= currentMemory.remove(template);
                    } else {
                        return;
                    }
                }
                
                if(knowledge != null) {
                    handler.handle(knowledge);
                }
            }
        }
    }
    
    /**
     * Field which contains all memories the observer is associated to.
     * It is accessed from within the observer implementation.
     */
    Map<String, IMemory> associatedMemories;
    
    /**
     * Field which contains the reference to the handler.
     * It is accessed from within the observer implementation.
     */
    final IKnowledgeHandler<P> handler;
    
    /**
     * Field which contains the guessed fact type of the handler.
     * It is accessed from within the observer implementation.
     */
    final Class<P> factClass;
    
    private final ConsumingSpaceObserver _observer;
    
    /**
     * Constructor for inherited classes that implement {@link #handle(IFact)}.
     */
    protected ConsumingKnowledgeProcessor() {
        this(null);
    }
    
    /**
     * Constructor for a new <code>ConsumingSpaceObserver</code>.
     * If <code>handler</code> the this observer is used as
     * handler.
     *  
     * @param handler       the handler to delegate new knowledge to
     * @see #handle(IFact)
     */
    public ConsumingKnowledgeProcessor(IKnowledgeHandler<P> handler) {
        this.handler= handler != null ? handler : this;
        this.factClass= guessFactType(this.handler.getClass());
        this.associatedMemories= new HashMap<String, IMemory>();
        _observer= new ConsumingSpaceObserver();
    }
    
    /**
     * Attaches this consuming knowledge processor to the specified memory
     * with the given template
     * 
     * @param memory        the memory to attach to
     * @param template      the template which triggers this processor
     */
    public final void attachTo(IMemory memory, P template) {
        synchronized(associatedMemories) {
            memory.attach(_observer, template);
            associatedMemories.put(memory.getID(), memory);
        }
    }
    
    /**
     * Detaches this consuming knowledge processor from the specified memory.
     * 
     * @param memory        the memory to detach from
     */
    public final void detachFrom(IMemory memory) {
        synchronized(associatedMemories) {
            IMemory foundMemory= associatedMemories.remove(memory.getID());
            if(foundMemory != null) {
                foundMemory.detach(_observer);
            }
        }
    }

    /**
     * This method does nothing.
     * If this processor is subclassed and the combination with
     * {@link IKnowledgeHandler} is prefered then this method should
     * be re-implemented.
     * 
     * @param knowledge     the new knowledge from the memory
     */
    public void handle(P knowledge) {}
    
    /**
     * Search for the handle method in the given handler class and returns the
     * parameter type of it.
     * 
     * @param handlerClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<P> guessFactType(Class<? extends IKnowledgeHandler> handlerClass) {
        for(Method method : handlerClass.getMethods()) {
            if(method.getName().equals("handle") && method.getParameterTypes().length == 1) {
                Class<?> paramType= method.getParameterTypes()[0];
                
                if(IFact.class.isAssignableFrom(paramType)) {
                    return (Class<P>)paramType;
                }
            }
        }
        
        throw new IllegalStateException("could not find 'handle' method in handler"); 
    }
}
