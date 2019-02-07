//
//  NetInfo.java
//

package com.progress.common.util ;

import  java.lang.* ;
import  java.net.* ;
import  com.progress.chimera.common.Tools ;


////////////////////////////////////////////////////////////////////////
public class NetInfo
{

    /**
     *  getIPAddress (String inHostName)
     *
     *  Returns the IP Address of the machine specified in "inHostName".
     *  Returns null if error encountered instantiating the INetaddress object.
     */
    public static String getIPAddress(String inHostName)
    {
        String      ipAddress  = null ;
        InetAddress ina        = null ;

        // First, instantiate an InetAddress object for the requested host name
        try
        {
            ina = InetAddress.getByName(inHostName) ;
        }
        catch (Throwable t)
        {
            Tools.px( t, "Error instantiating InetAddress object for: " + inHostName) ;
        }

        // Obtain the IP Address of the InetAddress object
        if (ina != null)
        {
            try
            {
                ipAddress = ina.getHostAddress() ;
            }
            catch (Throwable t)
            {
                Tools.px( t, "Error obtaining IP Address for: " + inHostName) ;
            }
        }

        return ipAddress ;
    }


    /**
     *  getLocalMachineIPAddress ()
     *
     *  Returns the IP Address of the local machine.
     *  Returns null if error encountered instantiating the INetaddress object.
     */
    public static String getLocalMachineIPAddress()
    {
        String      ipAddress  = null ;
        InetAddress ina        = null ;

        // Instantiate an InetAddress object for the local machine
        try
        {
            ina = InetAddress.getLocalHost();
        }
        catch (Throwable t)
        {
            Tools.px( t, "Error instantiating InetAddress object for local machine" ) ;
        }

        // Obtain the IP Address of the InetAddress object
        if (ina != null)
        {
            try
            {
                ipAddress = ina.getHostAddress() ;
            }
            catch (Throwable t)
            {
                Tools.px( t, "Error obtaining IP Address for local machine" ) ;
            }
        }

        return ipAddress ;
    }


    /**
     *  getLocalMachineHostName ()
     *
     *  Returns the IP Address of the local machine.
     *  Returns null if error encountered instantiating the INetaddress object.
     */
    public static String getLocalMachineHostName()
    {
        String      hostName   = null ;
        InetAddress ina        = null ;

        // Instantiate an InetAddress object for the local machine
        try
        {
            ina = InetAddress.getLocalHost();
        }
        catch (Throwable t)
        {
            Tools.px( t, "Error instantiating InetAddress object for local machine" ) ;
        }

        // Obtain the host name of the InetAddress object
        if (ina != null)
        {
            try
            {
                hostName = ina.getHostName() ;
            }
            catch (Throwable t)
            {
                Tools.px( t, "Error obtaining host name for local machine" ) ;
            }
        }

        return hostName ;
    }

    /**
     *  isLocalHost (String inHostName)
     *
     *  Is "inHostName" the local host (or an alias for the local machine)?
     *  Determines whether system is local by comparing IP addresses.
     *  returns:  true if "inHostName" is "localhost" or an alias for
     *            the local machine. Otherwise, returns false.
     */
    public static boolean isLocalHost (String inHostName)
    {
        boolean isLocalHost     = false ;
        //get the InetAddresses for inHostName and localhost
        InetAddress[] namesOfHost = null;
        InetAddress[] localhostAddresses = null;
        if (inHostName != null && inHostName.equalsIgnoreCase("localhost") )
        {
            isLocalHost = true;
        }
        else if (inHostName != null)
        try
        {
            namesOfHost = InetAddress.getAllByName(inHostName);
            localhostAddresses = InetAddress.getAllByName(InetAddress.
                                                          getLocalHost().
                                                          getHostName());
        }
        catch (UnknownHostException uhe )
        {
            isLocalHost = false;
        }

        //loop through the addresses to see if any match
        if (namesOfHost != null && localhostAddresses != null &&
            namesOfHost.length > 0 && localhostAddresses.length > 0 )
        {
            for ( int newHosts = 0; newHosts < namesOfHost.length; newHosts++)
            {
                for (int localhosts = 0; localhosts < localhostAddresses.length; localhosts++)
                {
                    //only 1 need to match for it to be local
                    if (namesOfHost[newHosts].getHostAddress().equals(
                        localhostAddresses[localhosts].getHostAddress() ) )
                    {
                        isLocalHost = true;
                        break;
                    }
                }
            }
        }


    return isLocalHost;
    }


}

