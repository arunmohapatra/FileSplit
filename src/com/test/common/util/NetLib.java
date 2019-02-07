//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//  NetLib.java
//
//  This class allows JNI access to the Network Library routines
//  getservbyname and gethostbyname. The framework is available for the
//  rest of the network routines. But has not been implemented.
//  The servent and hostent structures have been abstracted as classes
//  with private fields corresponding to the address of the native servent and
//  and hostent structs respectively. Not all accessors have been written for
//  the servent and hostent fields.
//
//

//  History:
//
//      07/07/98    S. Nair   Created class.
//


package com.progress.common.util;


public class NetLib
{

    private static native long getServByNameJNI(String sname, String protname);
    private static native long getHostByNameJNI(String hname);
    private static native int getPortNumberJNI(long address);
    private static native String getHostNameJNI(long address);
	private static native int getPortNumByNameJNI(String sname, String protname);


    public static class Servent
    {

        private long address;

        private Servent(long add)
        {
            address = add;
        }

        public int getPortNumber()
        {
            return getPortNumberJNI(address);
        }


    }


    public static class Hostent
    {

	private long address;

	private Hostent(long add)
	{
	    address = add;
	}

        public String getHostName()
        {
	    return getHostNameJNI(address);
        }
    }


    public static Servent getServByName(String name, String protname)
    {

        long l = getServByNameJNI(name, protname);

        Servent serv;

        if (l != 0)
            serv = new Servent(l);
        else
           serv = null;

        return serv;

    }

    public static Hostent getHostByName(String hostname)
    {
	long l = getHostByNameJNI(hostname);

	Hostent host;

	if (l != 0)
	    host = new Hostent(l);
	else
	    host = null;

	return host;
   }
	public static int getPortNumByName(String name, String protname)
	{

	    int port = getPortNumByNameJNI(name, protname);
		return port;
	}

	static 
	{
		InstallPath iPath = new InstallPath();
		System.load(iPath.fullyQualifyFile("jni_util.dll"));
	}
}
