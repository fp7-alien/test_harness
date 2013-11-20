package ccntester.event;

import ccntester.network.*;
import ccntester.testharness.*;
import ccntester.logging.*;

public class RequestEvent implements Event {
    long time_; // time of event
    int id_;   // Document id to be requested
    CCNNode node_;   // node which makes request
    TestHarness harness_;  // Test harness for schedule

    /** A request for a given document id is sent from a given node 
     * via the test harness at a given time */
    public RequestEvent (long time, int id, CCNNode node, TestHarness h) 
    {
        time_= time;
        id_= id;
        node_= node;
        harness_= h;
    }
    
    /** Time at which event occurs */
    public long getTime()
    {
        return time_;
    }
    
    /** Execute this event -- simply return false to indicate end of simulation */
    public boolean execute()
    {
        makeCCNRequest(node_,id_);
        harness_.scheduleRequestEvent(node_);
        return true;
    }

    /** Make the request to the CCN system */
    void makeCCNRequest(CCNNode n, int id)
    {
        Logger.getLogger("log").logln(USR.STDOUT,
            "At "+time_+" Node "+n.getName()+" "+n.getHostName()+":"+
            n.getPort()+" requests document "+id);
    }
    
    /** 
     * Event info as string 
     */
    
    public String asString()
    {
        return new String ("RequestEvent at "+time_+" for "+id_+" from "+
            node_.getName());
    }
    
    /** Type of event */
    public String getType()
    {
        return new String("RequestEvent");
    }
    
}
