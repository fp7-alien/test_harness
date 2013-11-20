package ccntester.network;

import ccntester.logging.*;
import java.io.*;
import java.util.*;
import ccntester.hosts.*;

public class CCNNode {
    String name_;                                       // Node name
    ArrayList <CCNNode> downstream_;      // Downstream nodes
    CCNNode upstream_;                  // Upstream node
    double requestRate_;                // Rate at which requests are made
    int bufferLen_;                             // buffer length (packets)

    Host host_= null; // Host and port on which the node is implemented
    int port_ = 0;

    /** Set up a new node */
    public CCNNode (String name, CCNNode upstream, double requestRate,
                    int bufferLen) {
        name_= name;
        upstream_= upstream;
        downstream_= new ArrayList <CCNNode>();
        requestRate_= requestRate;
        bufferLen_= bufferLen;
    }

    /** Add node to list of upstream nodes */
    public void addDownstream(CCNNode n)
    {
        downstream_.add(n);
    }

    /** Get the rate at which requests are made */
    public double getRequestRate()
    {
        return requestRate_;
    }

    /** Return the name of the node */
    public String getName() {
        return name_;
    }
    
    /** Set the host and port for this node*/
    public void setHostAndPort(Host h, int p)
    {
        host_= h;
        port_= p;
    }
    
    /** Get host */
    public Host getHost()
    {
        return host_;
    }
    
    /** Get host name*/
    public String getHostName()
    {   
        return host_.getName();
    }
    
    /** Get port*/
    public int getPort()
    {
        return port_;
    }
    
    /** Get length of buffer */
    public int getBufferLen()
    {
        return bufferLen_;
    }
    
    /** Get a list of downstream nodes*/
    public ArrayList <CCNNode> getDownstream() {
        return downstream_;
    }
    

}
