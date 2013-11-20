package ccntester.event;

/**
 * An EventException is thrown if event parsing fails
 */
public class EventException extends Exception {
    /**
     * Construct a EventException
     */
    public EventException() {
	super();
    }

    /**
     * Construct a EventException
     */
    public EventException(String s) {
	super(s);
    }


}
