package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.communication.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JWindow;

/**
 * @author Joachim Fuchs
 */
public class PingAgentBean extends AbstractAgentBean {
    static int counter = 0;
    
    PingObject ping = new PingObject("ping!" + (++counter));
    
    public PingAgentBean() {
     
        
        JWindow w = new JWindow();
        
        w.setSize(100, 20);
        w.setLocation(800, 800);
        JButton jb = new JButton("ping!");
        jb.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                ping = new PingObject("ping!" + (++counter));
            }
            
        });
        jb.setBackground(Color.RED);
        w.getContentPane().add(jb);
        w.setVisible(true);
        
    }
    
    public void execute() {
        
        if (ping != null) {
            
            memory.write(ping);
            ping = null;
            
        }        
        
    }
    
}
