package ccntester.event;

import java.util.*;


/** Class has an ordered schedule of Events */
public class Schedule {
    PriorityQueue <Event> schedule_= null;
    Comparator <Event> compare_=null;
    
    public Schedule() {
        compare_= new EventCompare();
        schedule_ = new PriorityQueue <Event> (1024,compare_); // Create a queue with 
    }
    
    /** Add an event to the queue*/
    public void addEvent (Event e)
    {
        schedule_.add(e);
    }
    
    /** Get the time of the first event in the queue */
    public long getTime() 
    {
        return schedule_.peek().getTime();
    }
    
    /** Get the event at the front of the queue */
    public Event removeEvent() 
    {
        return schedule_.poll();
    }
}

