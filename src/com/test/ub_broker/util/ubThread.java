
/*************************************************************/
/* Copyright (c) 1984-2008 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: ubThread.java,v 1.10 1999/12/07 16:50:41 lecuyer Exp $
 */

/*********************************************************************/
/* Module : ubThread                                                 */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RMISecurityManager;
import java.text.DecimalFormat;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.util.PscURLParser;
import com.progress.common.networkevents.IEventBroker;
import com.progress.common.rmiregistry.RegistryManager;
import com.progress.ubroker.tools.IAdminRemote;
import com.progress.chimera.adminserver.IAdministrationServer;
import com.progress.chimera.adminserver.IAdminServerConnection;

import com.progress.ubroker.util.ubMsgTrace;

/*********************************************************************/
/*                                                                   */
/* Class ubThread                                                    */
/*                                                                   */
/*********************************************************************/

public abstract class ubThread
	extends Thread
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*
public static final int UBTHREAD_STATE_IDLE               = 0x40000000;
public static final int UBTHREAD_STATE_READY              = 0x20000000;
public static final int UBTHREAD_STATE_BUSY               = 0x10000000;
public static final int UBTHREAD_STATE_BOUND              = 0x08000000;
public static final int UBTHREAD_STATE_DEAD               = 0x04000000;
*/

public static final int UBTHREAD_STATE_IDLE               = 0x00000000;
public static final int UBTHREAD_STATE_READY              = 0x00000001;
public static final int UBTHREAD_STATE_BUSY               = 0x00000002;
public static final int UBTHREAD_STATE_BOUND              = 0x00000003;
public static final int UBTHREAD_STATE_DEAD               = 0x00000004;

public static final String[] DESC_POOL_STATE =
  {
  "POOLSTATE_IDLE"
, "POOLSTATE_READY"
, "POOLSTATE_BUSY"
, "POOLSTATE_BOUND"
, "POOLSTATE_DEAD"
  };

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

private static DecimalFormat fmt4;

/*********************************************************************/
/* Static initializer block                                          */
/*********************************************************************/

static
    {
    fmt4 = new DecimalFormat("0000");
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

protected ubThreadStats stats;
protected IAppLogger    log;
protected RequestQueue  rcvQueue;
private   int           threadId;
private   String        poolId;
private   String        fullName;
private   int           threadPoolState;
protected ubMsgTrace    trace;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubThread(String poolId, int threadId, IAppLogger log)
    {
    this.poolId = poolId;
    this.threadId = threadId;
    this.log = log;
    threadPoolState = UBTHREAD_STATE_IDLE;
    rcvQueue = null;
    fullName = new String(poolId + "-" + fmt4.format(threadId));
    setName(fullName); 
    stats = new ubThreadStats(fullName);
    }

/*********************************************************************/
/* Abstract Methods                                                  */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

abstract public int getConnState();

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

abstract public String getFSMState();

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

abstract public String getRemoteSocketDesc();

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String toString()
    {
    return new String(fullName);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int getPoolState()
    {
    return threadPoolState;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getPoolStateDesc()
    {
    int state = threadPoolState;

    return new String(DESC_POOL_STATE[state]);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized int setPoolState(int newPoolState)
    {
    int oldPoolState = threadPoolState;
    threadPoolState = newPoolState;
    return oldPoolState;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int getThreadId()
    {
    return threadId;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getFullName()
    {
    return /* new String(fullName) */  fullName;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public RequestQueue getRcvQueue()
    {
    return rcvQueue;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubThreadStats getLastStats()
    {
    return stats;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setLastStats(ubThreadStats stats)
    {
    this.stats = stats;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public synchronized ubThreadStats getStats()
    {
    ubThreadStats ret = new ubThreadStats(stats);
    ret.setMaxQueueDepth(rcvQueue.getMaxQueueDepth());
    ret.setEnqueueWaits(rcvQueue.getEnqueueWaits());
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* double check to see if this is being called anywhere

public synchronized ubThreadStats resetStats()
    {
    ubThreadStats ret = getStats();
    rcvQueue.resetMaxQueueDepth();
    rcvQueue.resetEnqueueWaits();
    stats.resetStats();
    return ret;
    }
*/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static Remote lookupService(String url)
        throws java.net.MalformedURLException,
               java.rmi.RemoteException,
               java.rmi.NotBoundException
    {
    /* BUG 20031016-018 */
    /* we replace the call to the Naming class in order to avoid */
    /* their internal URL parsing.  In JDK 1.4.2+, The internal  */
    /* URL parsing mechanism does not allow the rmi host name to */
    /* have an underscore in it.  Since some of our customers    */
    /* rely on this, we use the PscURLParser instead             */

        PscURLParser urlParser;
        String       rmiHost;
        int          rmiPort;
        String       rmiSvc;
        Registry     reg;
        Remote       remote;

        urlParser = new PscURLParser(url);
        urlParser.setScheme(null);  /* necessary kludge */
        rmiHost = urlParser.getHost();
        rmiPort = urlParser.getPort();
        rmiSvc  = urlParser.getService();
        reg = LocateRegistry.getRegistry(rmiHost, rmiPort);

        remote = (rmiSvc == null) ? reg : reg.lookup(rmiSvc);

        return remote;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static void rebindService(String rmiURL, Remote remote)
        throws java.net.MalformedURLException
              ,java.rmi.RemoteException
    {
        /* BUG 20031016-018 */
        /* we replace the call to the Naming class in order to avoid */
        /* their internal URL parsing.  In JDK 1.4.2+, The internal  */
        /* URL parsing mechanism does not allow the rmi host name to */
        /* have an underscore in it.  Since some of our customers    */
        /* rely on this, we use the PscURLParser instead             */

        PscURLParser urlParser;
        String       rmiHost;
        int          rmiPort;
        String       rmiSvc;
        Registry     reg;

        urlParser = new PscURLParser(rmiURL);
        urlParser.setScheme(null);  /* necessary kludge */
        rmiHost = urlParser.getHost();
        rmiPort = urlParser.getPort();
        rmiSvc  = urlParser.getService();
        reg = LocateRegistry.getRegistry(rmiHost, rmiPort);

        reg.rebind(rmiSvc, remote);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static void unbindService(String rmiURL)
        throws java.net.MalformedURLException
              ,java.rmi.RemoteException
              ,java.rmi.NotBoundException
    {
        /* BUG 20031016-018 */
        /* we replace the call to the Naming class in order to avoid */
        /* their internal URL parsing.  In JDK 1.4.2+, The internal  */
        /* URL parsing mechanism does not allow the rmi host name to */
        /* have an underscore in it.  Since some of our customers    */
        /* rely on this, we use the PscURLParser instead             */

        PscURLParser urlParser;
        String       rmiHost;
        int          rmiPort;
        String       rmiSvc;
        Registry     reg;

        urlParser = new PscURLParser(rmiURL);
        urlParser.setScheme(null);  /* necessary kludge */
        rmiHost = urlParser.getHost();
        rmiPort = urlParser.getPort();
        rmiSvc  = urlParser.getService();
        reg = LocateRegistry.getRegistry(rmiHost, rmiPort);

        reg.unbind(rmiSvc);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubMsgTrace getMsgTrace()
    {
    return trace;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

  public IEventBroker findAdminServerEventBroker(String rmiURL,
                                                 String brokerName)
  {
     /* this uses our own log object */
     return findAdminServerEventBroker(rmiURL,brokerName, log);    
  }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static IEventBroker findAdminServerEventBroker(String rmiURL,
                                                      String brokerName,
                                                      IAppLogger logger)
  {
    IAdminServerConnection adminserver = null;
    IEventBroker eventbroker = null;
    try
    {
      String url = rmiURL;
      RMISecurityManager securityManager = new RMISecurityManager();
      System.setSecurityManager(securityManager);

      adminserver = (IAdminServerConnection)
          ubThread.lookupService(url.substring(0, url.lastIndexOf(brokerName)) +
                             RegistryManager.DEFAULT_PRIMARY_BINDNAME);

      eventbroker = ((IAdministrationServer)adminserver).getEventBroker();
    }
    catch(Exception ex)
    {
      if(rmiURL != null)
      {
         if (logger != null)
             logger.logStackTrace("Cannot locate AdminServer's EventBroker",
                                  ex);
      }
    }
    return eventbroker;
  }

/**********************************************************************/
/* Abstract methods                                                   */
/**********************************************************************/

/*********************************************************************/
/* Private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/* protected methods                                                 */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/



}  /* end of ubThread */

