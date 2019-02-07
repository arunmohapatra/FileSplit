
/*************************************************************/
/* Copyright (c) 2008 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: LogEvntHandler.java
 */

/*********************************************************************/
/* Module : LogEvntHandler                                                 */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;

import com.progress.common.networkevents.IEventBroker;
import com.progress.common.rmiregistry.RegistryManager;
import com.progress.ubroker.management.events.EUbrokerLogFileNameChanged;
import com.progress.chimera.adminserver.IAdminServerConnection;
import com.progress.chimera.adminserver.IAdministrationServer;
import com.progress.common.ehnlog.ILogEvntHandler;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;

/*********************************************************************** 
 * This class is used by the ubroker layer to handle a log file name change
 * so that we notify OE Management, so it can support log file monitoring
 * when the user specified a log threshold.
 *
 * For the broker log file, this get registered with the enhanced logging 
 * layer so that it calls a method on this class to notify us when it switches
 * to a new log file name, so we can post the event.
 *
 * For the server log file, we come here through the file-watchdog thread
 * (ubFileWatchDog), when the broker creates a new server log file. Again,
 * when we get called, we post the event.
 *
 * The intanstiator of this class must call setMainLog when not working with 
 * the main log file. The main log file is the broker log file, for instance. 
 * The server log file is also managed by the broker, but it's not its main 
 * log file, so we pass a different log type to OE Management so it can 
 * distinguish between the two log types.
 *
 * To sum it up, the flow is:
 *
 * For broker: ubListenerThread instantiate this object, and pass it to the 
 *             log writer (via registerThresholdEventHandler). The log writer
 *             will then call our sendFileNameChangedEvent to post the event.
 *
 * For server log: ubFileWatchDog instantiate this class and calls our 
 *                 sendFileNameChangedEvent to post the event.
 *
 * This was added to fix OE00129357.
 ***********************************************************************/

/*********************************************************************/
/*                                                                   */
/* Class LogEvntHandler                                              */
/*                                                                   */
/*********************************************************************/

public class LogEvntHandler
       implements ILogEvntHandler
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/
private ubProperties m_properties;
private IAppLogger   m_log;
private String       m_logType;


/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public LogEvntHandler(ubProperties properties, IAppLogger log)
    {
    m_properties = properties;
    m_log = log;

    /* the default is that this is for THE log file (not a server log file for a AS/WS broker */
    m_logType = EUbrokerLogFileNameChanged.MAIN_LOG_FILE;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

 /**
 * Set whether this is for the main log or not (i.e. server log file)
 */
 public void setMainLog(boolean isMain)
 {
    if (isMain)
        m_logType = EUbrokerLogFileNameChanged.MAIN_LOG_FILE;
    else
        m_logType = EUbrokerLogFileNameChanged.SERVER_LOG_FILE;
 }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

 /**
 * Post an event about the log file name change (for OE Management)
 */
 public void sendFileNameChangedEvent(String fileName)
 {
    IEventBroker eb = ubThread.findAdminServerEventBroker(m_properties.rmiURL,
                                                          m_properties.brokerName,
                                                          m_log);
    if(eb != null)
    {
        try
        {
            EUbrokerLogFileNameChanged e =
                 new EUbrokerLogFileNameChanged (m_properties.brokerName,
                                                 fileName, 
                                                 m_logType,
                                                 m_properties.canonicalName);
            eb.postEvent(e);
            if (m_log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_BASIC,UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logVerbose(UBrokerLogContext.SUB_V_UB_BASIC,
                               "Posted EUbrokerLogFileNameChanged for broker: " + 
                               m_properties.brokerName +
                               " (" + fileName + ")");
        }
        catch( RemoteException remoteException )
        {
            m_log.logError("Failed to post EUbrokerLogFileNameChanged event for " +
                           m_properties.brokerName  + " file: " + 
                           fileName +  " (" + remoteException.toString() + ")");                        
        }
    }
 }


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/



} /* class LogEvntHandler */
