package ccntester.network;

/** Class represents a piece of content held by the network */

public class Content {
    int length_;  // Length of this piece of content
    double pop_;  // Relative popularity of content
    int id_;  // id No
    double totPop_= 0.0;
    
    public Content(int id, int len, double pop, double totPop)
    {
        id_= id;
        length_= len;
        pop_= pop;
        totPop_= totPop;
    }
    
    /** set cumulative popularity -- not normalised */
    public void setTotPop(double pop)
    {
        totPop_= pop;
    }
    
    /** Get cumulatvie total popularity */
    public double getTotPop()
    {
        return totPop_;
    }
    
    /** get content id */
    public int getId()
    {
        return id_;
    }
    
} 
