package ccntester.event;

public interface Event {
    
    /** Time of event*/
    public long getTime();
    
    /** Do what event says -- return false if simulation should end*/
    public boolean execute() throws EventException;
    
    /** 
     * Return printable event type for debugging
     */
    public String getType();


    /** Return event as string */
    public String asString();
}
