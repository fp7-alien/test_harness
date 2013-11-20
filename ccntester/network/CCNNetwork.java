package ccntester.network;

import ccntester.logging.*;
import java.io.*;
import java.util.*;

/*** CCNNetwork is a class representing a network of CCN nodes
 */

public class CCNNetwork {
    ArrayList <CCNNode> nodes_;
    HashMap <String, CCNNode> nodeMap_;
    CCNNode root_= null;
    double rateScale_= 1.0;

/** construct network from file */
    public CCNNetwork(String fName) throws IOException {
        init();
        BufferedReader reader;
        try {
            reader=new BufferedReader(new FileReader(fName));
        } catch (FileNotFoundException e) {
            throw new IOException("Cannot read file "+fName);
        }
        try {
            while(true) {
                String newString= reader.readLine();
                if (newString == null) {
                    break;
                }
                newString= newString.trim();
                if (newString.length() == 0)
                    continue;
                String[] parsed= newString.split(",");
                if (parsed.length != 5) {
                    throw new IOException("In "+fName+" cannot parse line "+newString);
                }
                int buffLen= Integer.parseInt(parsed[4].trim());
                double arriveRate= Double.parseDouble(parsed[3].trim()) +
                                   Double.parseDouble(parsed[2].trim());
                CCNNode newNode;
                String name= parsed[0].trim();
                if (parsed[1].trim().equals("none")) {
                    if (root_ != null) {
                        throw (new IOException("Two root nodes defined in "+fName));
                    }
                    newNode= new CCNNode(name,null, arriveRate,
                                         buffLen);
                    root_= newNode;
                } else {
                    CCNNode upstream= findNode(parsed[1].trim());
                    if (upstream == null) {
                        throw (new IOException("Cannot find node "+parsed[1]+
                                               " while parsing "+newString+
                                               " in file "+fName));

                    }
                    newNode= new CCNNode(name, upstream, arriveRate,
                                         buffLen);
                    upstream.addDownstream(newNode);
                }
                nodes_.add(newNode);
                if (nodeMap_.put(name, newNode) != null) {
                    throw new IOException("Node "+name+" exists twice in file "+fName);
                }

            }
        } catch (IOException e) {
            throw(new IOException("Error while reading "+fName+" "+e.getMessage()));
        }
    }

    /** Initialise local variables */
    void init() {
        nodes_= new ArrayList<CCNNode>();
        nodeMap_= new HashMap<String, CCNNode>();
    }

    /** Return node or null if no such node */
    public CCNNode findNode(String nodeName)
    {
        return nodeMap_.get(nodeName);
    }
    
    /** Accessor function for root node */
    public CCNNode getRoot()
    {
        return root_;
    }
    
    /** Accessor function for node list */
    public ArrayList<CCNNode> getNodeList()
    {
        return nodes_;
    }
    
    /** Setter function for rate scale */
    public void setRateScale(double r) 
    {
        rateScale_= r;
    }
    
    /** Accessor function for rate scale */
    public double getRateScale()
    {
        return rateScale_;
    }
}
