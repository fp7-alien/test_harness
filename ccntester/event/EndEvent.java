package ccntester.event;

/** Event indicating end of simulation */

public class EndEvent implements Event {
    long time_;
    
    public EndEvent (long time) 
    {
        time_= time;
    }
    
    /** Time of execution of event*/
    public long getTime()
    {
        return time_;
    }
    
    /** Execute this event -- simply return false to indicate end of simulation */
    public boolean execute()
    {
        
        return false;
    }
    
    /** 
     * Event info as string 
     */
    
    public String asString()
    {
        return new String ("EndEvent at "+time_);
    }
    
    /** Type of event */
    public String getType()
    {
        return new String("EndEvent");
    }
    
}
