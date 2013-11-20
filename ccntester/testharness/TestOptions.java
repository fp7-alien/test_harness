package ccntester.testharness;

import ccntester.logging.*;
import ccntester.hosts.*;
import rgc.xmlparse.*;
import rgc.probdistributions.*;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.*;
import java.io.*;
import ccntester.network.CCNNetwork;

/** TestOptions is the options read from the XML file for the test harness
 *
 */
public class TestOptions {
/** Class variables */

//Variables related to documents
    private int noCCNDocs_;      // Number of CCN Documents
    private ProbDistribution docsLenDist_;     // Distribution of document lengths
    private ProbDistribution docsPopDist_;     // Distribution of document popularity

//Variables related to network
    private CCNNetwork network_= null;      //Network of CCN nodes
    int endTime_= 0;        // time to end simulation

    //Variables related to physical hosts
    private TestBed testbed_= null;        // Physical hosts available

/** Constructor is blank*/
    TestOptions() {
    };

/** Initialise class variables */
    void init() {

    }

    void parse(String fName) throws IOException {

        try { DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
              Document doc = docBuilder.parse (new File(fName));

              // normalize text representation
              doc.getDocumentElement ().normalize ();
              String basenode= doc.getDocumentElement().getNodeName();
              if (!basenode.equals("CacheTester")) {
                  throw new IOException("Base tag should be CacheTester");
              }
              NodeList nlist= doc.getElementsByTagName("DocumentProperties");
              processDocuments(nlist);
              nlist= doc.getElementsByTagName("Network");
              processNetwork(nlist);
              nlist= doc.getElementsByTagName("Host");
              processHosts(nlist);



              // Check all tags are processed
              // Check for other unparsed tags
              Element el= doc.getDocumentElement();
              NodeList rest= el.getChildNodes();
              for (int i= 0; i < rest.getLength(); i++) {
                  Node n= rest.item(i);
                  if (n.getNodeType() == Node.ELEMENT_NODE) {
                      throw new IOException("XML unrecognised tag "+n.getNodeName());
                  }

              }} catch (java.io.FileNotFoundException e) {
            throw new IOException("Cannot find file "+fName);
        } catch (SAXParseException err) {
            throw new IOException("Parsing error" + ", line "
                                  + err.getLineNumber () + ", uri " + err.getSystemId ()+ " " + err.getMessage ());
        } catch (SAXException err) {
            throw new IOException("SAXException parsing "+fName+" " + err.getMessage ());
        }

        catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new IOException("Exception in SAX XML parser "+e.getMessage());

        }
    }

/**
 * Process the DocumentProperties tag which contains information about the number and
 * distribution of documents
 */
    void processDocuments(NodeList nlist) throws IOException, SAXException
    {
        if (nlist.getLength() > 1) {
            throw new IOException("Only one DocumentProperties tag allowed.");
        }
        if (nlist.getLength() == 0)
            throw new IOException("DocumentProperties tag must be present.");
        Node docNode= nlist.item(0);
        try {
            noCCNDocs_= ReadXMLUtils.parseSingleInt(docNode, "NoDocuments","DocumentProperties",false);
            ReadXMLUtils.removeNode(docNode,"NoDocuments","DocumentProperties");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
            throw new IOException("NoDocuments tag must be present in DocumentProperties");
        }

        try {
            docsLenDist_= ProbDistribution.parseProbDist(docNode,"LengthDistribution");
            ReadXMLUtils.removeNode(docNode,"LengthDistribution","DocumentProperties");
        } catch (SAXException e) {
            throw new IOException("LengthDistribution tag failed to parse -- "+e.getMessage());
        } catch (ProbException e) {
            throw new IOException("Improper probability distribution in LengthDistribution "+
                                  e.getMessage());
        } catch (XMLNoTagException e) {
            throw new IOException("Missing tag in LengthDistribution "+e.getMessage());
        }
        if (docsLenDist_ == null) {
            throw new SAXException ("Must specify LengthDistribution tag with valid Prob Dist in DocumentProperties");
        }

        try {
            docsPopDist_= ProbDistribution.parseProbDist(docNode,"PopDistribution");
            ReadXMLUtils.removeNode(docNode,"PopDistribution","DocumentProperties");
        } catch (SAXException e) {
            throw new IOException("PopDistribution tag failed to parse -- "+e.getMessage());
        } catch (ProbException e) {
            throw new IOException("Improper probability distribution in PopDistribution "+
                                  e.getMessage());
        } catch (XMLNoTagException e) {
            throw new IOException("Missing tag in PopDistribution "+e.getMessage());
        }
        if (docsPopDist_ == null) {
            throw new SAXException ("Must specify PopDistribution tag with valid Prob Dist in DocumentProperties");
        }

        NodeList nl= docNode.getChildNodes();
        for (int i= 0; i < nl.getLength(); i++) {
            Node n= nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                throw new IOException("DocumentProperties XML unrecognised tag "+n.getNodeName());
            }

        }
        docNode.getParentNode().removeChild(docNode);          // Tidy up by deleting
    }

/**
 * Process the Network tag which contains information about the geometry
 * of the CCN nodes
 */
    void processNetwork(NodeList nlist) throws IOException, SAXException
    {
        if (nlist.getLength() > 1) {
            throw new IOException("Only one Network tag allowed.");
        }
        if (nlist.getLength() == 0)
            throw new IOException("Network tag must be present.");
        Node docNode= nlist.item(0);
        String fName= null;
        try {
            fName= ReadXMLUtils.parseSingleString(docNode,"FileName","Network",true);
            ReadXMLUtils.removeNode(docNode,"FileName","DocumentProperties");
        } catch (SAXException e) {
            throw new IOException("Network FileName tag failed to parse -- "+e.getMessage());
        } catch (XMLNoTagException e) {
        }
        if (fName != null) {
            try {
                network_= new CCNNetwork(fName);

            } catch (IOException e) {
                throw(e);
            }
        }
        try {
            endTime_= ReadXMLUtils.parseSingleInt(docNode,"EndTime","Network",false);
            ReadXMLUtils.removeNode(docNode,"EndTime","Network");
        } catch (SAXException e) {
            throw new IOException("Could not parse EndTime tag for Network");
        } catch (XMLNoTagException e) {
            throw new IOException("EndTime tag must be present within Network");
        }
        try {
            double rs= ReadXMLUtils.parseSingleDouble(docNode,"RateScale","Network",true);
            ReadXMLUtils.removeNode(docNode,"RateScale","Network");
            if (rs <= 0) {
                throw new IOException("RateScale must be positive in Network");
            }
            network_.setRateScale(rs);
        } catch (SAXException e) {
            throw new IOException("Could not parse RateScale Tag for Network");
        } catch (XMLNoTagException e) {
        }

        if (network_ == null) {
            throw new IOException ("CCN Network topology must be specified within Network tag");

        }
        // Check for any nodes left unprocessed
        NodeList nl= docNode.getChildNodes();
        for (int i= 0; i < nl.getLength(); i++) {
            Node n= nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                throw new IOException("Network XML unrecognised tag "+n.getNodeName());
            }

        }
        docNode.getParentNode().removeChild(docNode);          // Tidy up by deleting
    }
    
    /** Process the Host tags which tells the processor about machines
     * on the testbed
     **/
    private void processHosts(NodeList nlist) throws IOException, SAXException
    {
        testbed_= new TestBed();
        if (nlist.getLength() == 0)
            throw new IOException("At least one Host tag must be present.");
        while (nlist.getLength() != 0) {
            Node docNode= nlist.item(0);
            Host newHost= processOneHost(docNode);
            testbed_.addHost(newHost);
        }
        
    }
    
    /** Process a single host tag */
    private Host processOneHost (Node docNode) throws IOException, SAXException
    {
        Host newhost= null;
        try {
            String name= ReadXMLUtils.parseSingleString(docNode,"Name","Host",false);
            ReadXMLUtils.removeNode(docNode,"Name","Host");
            newhost= new Host(name);
        } catch (SAXException e) {
            throw new IOException("Could not parse Name tag for Host");
        } catch (XMLNoTagException e) {
            throw new IOException("Name tag must be present within Host");
        }
        try {
            int lowPort= ReadXMLUtils.parseSingleInt(docNode,"LowPort","Host",false);
            ReadXMLUtils.removeNode(docNode,"LowPort","Host");
            newhost.setLowPort(lowPort);
        } catch (SAXException e) {
            throw new IOException("Could not parse lowPort tag for Host");
        } catch (XMLNoTagException e) {
            throw new IOException("Name tag must be present within Host");
        }
        try {
            int highPort= ReadXMLUtils.parseSingleInt(docNode,"HighPort","Host",false);
            ReadXMLUtils.removeNode(docNode,"HighPort","Host");
            newhost.setHighPort(highPort);
        } catch (SAXException e) {
            throw new IOException("Could not parse HighPort tag for Host");
        } catch (XMLNoTagException e) {
            throw new IOException("HighPort tag must be present within Host");
        }
         // Check for any nodes left unprocessed
        NodeList nl= docNode.getChildNodes();
        for (int i= 0; i < nl.getLength(); i++) {
            Node n= nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                throw new IOException("Network XML unrecognised tag "+n.getNodeName());
            }

        }
        docNode.getParentNode().removeChild(docNode);  
        return newhost;
    }
    
    /** Accessor function for testbed*/
    public TestBed getTestbed()
    {
        return testbed_;
    }
    
    /** Accessor function for network structure */
    public CCNNetwork getNetwork() {
        return network_;
    }
    
    /** Accessor function for no CC docs */
    public int getNoCCNDocs() {
        return noCCNDocs_;
    }
    
    /** Accessor function for length distribution */
    public ProbDistribution getDocsLenDist() {
        return docsLenDist_;   
    }
   
    /** Accessor function for popularity distribution*/
    public ProbDistribution getDocsPopDist() {
        return docsPopDist_; 
    }
    
    /** Accessor function for end time*/
    public int getEndTime() {
        return endTime_;
    }
}
