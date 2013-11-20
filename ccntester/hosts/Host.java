package ccntester.hosts;

import ccntester.logging.*;
import java.io.*;

public class Host {
    String name_;
    boolean isLocal_;
    int lowPort_= 8000;       // Lowest port to use
    int highPort_= 9000;      // Highest port to use
    int usedPort_= 0;                   // Port to use

/** Constructor for host*/
    public Host(String name) {
        name_= name.trim();
        if (name_.equals("localhost") || name_.equals("127.0.0.1")) {
            isLocal_= true;
        } else {
            isLocal_= false;
        }
    }

    /** Return next port to be used or throw error */
    public int nextPort() throws IOException
    {
        if (usedPort_ > highPort_) {
            throw new IOException("Out of ports on host:"+name_);
        }
        usedPort_++;
        return (usedPort_-1);

    }

    /** return the percentage of free ports*/
    public double getFreePercent()
    {
        int free= highPort_+1 - usedPort_;
        int pool= highPort_+1 - lowPort_;
        return (double) free/pool;
    }

/** Accessor for lowPort*/
    public int getLowPort()
    {
        return lowPort_;
    }

/** Setter for lowPort -- also resets used Port*/

    public void setLowPort(int port)
    {
        lowPort_= port;
        usedPort_= lowPort_;
    }

/** Accessor for highPort*/
    public int getHighPort()
    {
        return highPort_;
    }

/** Setter for highPort */
    public void setHighPort(int port)
    {
        highPort_= port;
    }

    /** Accessor for name */
    public String getName()
    {
        return name_;
    }

}
