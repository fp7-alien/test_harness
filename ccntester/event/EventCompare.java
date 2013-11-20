package ccntester.event;

import java.util.*;

public class EventCompare implements Comparator <Event> {
    
    public int compare(Event a, Event b) {
        if (a.getTime() < b.getTime())
            return -1;
        if (a.getTime() > b.getTime())
            return 1;
        return 0;
    }
    
    public int equals(Event a, Event b) {
        if (a.getTime() == b.getTime())
            return 1;
        return 0;
    }
}
