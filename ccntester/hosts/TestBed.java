package ccntester.hosts;

import ccntester.logging.*;
import java.util.*;

public class TestBed {
    ArrayList <Host> hosts_;  // List of all hosts
    
    public TestBed() {
        hosts_= new ArrayList<Host>();
    }
    
    /** Return host with most free space*/
    public Host getFreeHost()
    {
        Host freeHost= null;
        double freePercent= -1.0;
        for (Host h: hosts_) {
            double fp= h.getFreePercent();
            if (fp > freePercent) {
                freePercent= fp;
                freeHost= h;
            }
        }
        return freeHost;
    }
    
    /** Add a new host to the list */
    public void addHost(Host h)
    {
        hosts_.add(h);
    }
    
    /** Accessor function for host list */
    public ArrayList <Host> getHosts()
    {
        return hosts_;
    }
}
