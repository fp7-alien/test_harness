package ccntester.testharness;

import ccntester.logging.*;
import ccntester.network.*;
import ccntester.hosts.*;
import java.io.*;
import ccntester.logging.*;
import rgc.probdistributions.*;
import ccntester.event.*;
import java.util.*;
import rgc.probdistributions.*;

/** TestHarness is the main test harness to set up CCN, test and run
 * them
 */
public class TestHarness {

/** Main entry point
 *
 */
    CCNNetwork network_= null;  // Network of CCNNodes
    CCNNode root_= null;        // Root node in CCN network
    TestOptions options_= null;  // Options read from XML file
    TestBed testbed_= null;     // Machines used for output
    Logger logger;              // Used for logging errors
    TreeMap <Double,Content> contentList_= null;
    double totPopularity_= 0.0; // Total popularity for all content
    Schedule schedule_= null;  // Scheduler
    long startTime_= 0;    // Start time (in absolute UTC millisecs)
    long currentTime_= 0;  // Current simulation time (0 start time)
    long maxLag_= 10000;  // Maximum lag in simulation (10 seconds)

    /** Initialise the test harness */
    public TestHarness(TestOptions opt)
    {
        options_= opt;
        network_= options_.getNetwork();
        root_= network_.getRoot();
        testbed_= options_.getTestbed();
        contentList_= new TreeMap <Double,Content> ();
        schedule_= new Schedule();
    }
    
    /** Main work loop*/
    public void start()
    {
        logger = Logger.getLogger("log");
        initCCNNodes();
        fillContent();
        initEvents();
        logger.logln(USR.STDOUT,"Starting real time test");
        Event nextEvent;
        startTime_= System.currentTimeMillis();
        long nextTime;  // time of next event
        long simulationTime= 0;
        while(true) {
            nextEvent= schedule_.removeEvent();
            nextTime= nextEvent.getTime();
            simulationTime= System.currentTimeMillis();
            while (simulationTime < startTime_+ nextTime) {
                try {
                    synchronized (this) {
                        wait(startTime_+ nextTime - simulationTime);
                    }
                } catch(InterruptedException e) {
                    logger.logln(USR.ERROR,"Wait interrupted");
                    System.exit(-1);
                }
                simulationTime= System.currentTimeMillis();
            }
            //System.out.println("Lag = "+(simulationTime - (startTime_ + nextTime)));
            if ((simulationTime - (startTime_ + nextTime)) > maxLag_) {
                Logger.getLogger("log").logln(USR.ERROR, 
                        "Simulation lagging too much, slow down events");
                System.exit(-1);
            }

            if (nextEvent == null) {
                logger.logln(USR.ERROR,"Run out of events in schedule");
                break;
            }
            currentTime_= nextEvent.getTime();
            try {
                if (nextEvent.execute() == false)
                    break;
            } catch (EventException e) {
                logger.logln(USR.ERROR,"Could not execute event");
                logger.logln(USR.ERROR,e.getMessage());
                System.exit(-1);
            }
        }
        logger.logln(USR.STDOUT,"Simulation ended: shutting down nodes");
        shutDownCCNNodes();
    }
    
    /** Set the CCN nodes going and connect them*/
    private void initCCNNodes()
    {
        // Initialise the CCN nodes
        logger.logln(USR.STDOUT,"Starting nodes");
        for (CCNNode node: network_.getNodeList()) {
            Host free= testbed_.getFreeHost();
            int port= 0;
            try {
                port= free.nextPort();
            }
            catch (IOException e) {
                logger.logln(USR.ERROR,"Out of free ports on hosts");
                logger.logln(USR.ERROR,e.getMessage());
                return;
            }
            try {
                startCCNDaemon(node,free,port);
            } catch (IOError e) {
                logger.logln(USR.ERROR,"Cannot start daemon");
                logger.logln(USR.ERROR,e.getMessage());
            }
            node.setHostAndPort(free,port);
        }
        // Connect nodes
        logger.logln(USR.STDOUT,"Connecting CCN Nodes");
        connectNodes(root_);
    }
    
    // start Daemon
    private void startCCNDaemon(CCNNode node, Host free, int port) throws IOError
    {            
        logger.logln(USR.STDOUT,"Starting daemon for "+node.getName()+
                        " on "+free.getName()+":"+port+" bufferlen "+
                        node.getBufferLen());
    }
    
    /** Connect CCN nodes*/
    private void connectNodes(CCNNode node)
    {
        for (CCNNode n: node.getDownstream()) {
            makeConnection(node, n);
            connectNodes(n);
        }
    }
    
    /** Make CCN connection where nodeA is closer to root and nodeB
     * is further from root */
    private void makeConnection(CCNNode nodeA, CCNNode nodeB) throws IOError
    {
        logger.logln(USR.STDOUT,"Connecting "+nodeA.getName()+" "+
                nodeA.getHostName()+":"+nodeA.getPort()+ " to "+
                nodeB.getName()+" "+
                nodeB.getHostName()+":"+nodeB.getPort());
    }
    
    /** Place content in CCN cache*/
    private void fillContent()
    {
        int noItems= options_.getNoCCNDocs();
        ProbDistribution lenDist= options_.getDocsLenDist();
        ProbDistribution popDist= options_.getDocsPopDist();
        for (int i= 0; i < noItems;i++) {
            try {
                int length= (int)Math.ceil(lenDist.getVariate());
                double pop= popDist.getVariate();
                if (pop <= 0) {
                    System.err.println("getVariate returned content with zero or -ve popularity");
                    System.exit(-1);
                }
                totPopularity_+= pop;
                Content c= new Content(i,length,pop, totPopularity_);
                contentList_.put(totPopularity_,c);
                insertContent(i, length);
            } catch (Exception e) {
                logger.logln(USR.ERROR,"Error in getVariate routine");
                System.exit(-1);
            }
        }
    }
    
    /** Insert content to root CCN node */
    private void insertContent(int id, int len)
    {
        logger.logln(USR.STDOUT,"Insert content Id: "+id+" length "+len+
            " in "+root_.getName()+" "+
           root_.getHostName()+":"+root_.getPort());
    }

    /** Insert initial events into scheduler */
    private void initEvents()
    {
        // Mult time by 1000 to get into miliseconds
        EndEvent e= new EndEvent(currentTime_+(long)(1000*options_.getEndTime()));
        schedule_.addEvent(e);
        for (CCNNode n: network_.getNodeList()) {
            if (n == root_)
                continue;
            scheduleRequestEvent(n);
        }
    }

    /** Add a request for content to the schedule */
    public void scheduleRequestEvent(CCNNode n)
    {
        double rate= n.getRequestRate()*network_.getRateScale();
        long interval= (long)(ProbElement.exponentialVariate(1.0/rate)*1000);
        double rand= Math.random()*totPopularity_;
        Content c= (Content)contentList_.ceilingEntry(rand).getValue();
        if (c == null) {
            logger.logln(USR.ERROR,"Content list error\n");
            System.exit(-1);
        }
        RequestEvent re= new RequestEvent(currentTime_+interval, c.getId(), n, this);
        schedule_.addEvent(re);
    }

    /** Send shutdown to CCN nodes */
    private void shutDownCCNNodes()
    {
        for (CCNNode n: network_.getNodeList()) {
            shutDownSingleNode(n);
        }
    }
    
    /** Send shutdown to single CCN node */
    private void shutDownSingleNode(CCNNode n)
    {
        logger.logln(USR.STDOUT,"Sending shutdown request to "+n.getName()+
            " "+n.getHostName()+":"+n.getPort());
    }
    

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Command line must specify "+
                               "XML file to read and nothing else.");
            System.exit(-1);
        }
        Logger lg = Logger.getLogger("log");

        lg.addOutput(System.err, new BitMask(USR.ERROR));
        lg.addOutput(System.out, new BitMask(USR.STDOUT));

        // Read test options
        TestOptions opt= new TestOptions();
        
        try {
            opt.parse(args[0]);
        } catch (Exception e) {
            lg.logln(USR.ERROR,"Cannot parse options");
            lg.logln(USR.ERROR,e.getMessage());
            return;
        }
        TestHarness th= new TestHarness(opt);
        th.start();

    }
    



}
