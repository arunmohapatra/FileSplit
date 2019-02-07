/*************************************************************/
/* Copyright (c) 2003-2010 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/*                                                                   */
/* Module : ubRMIWatchDog                                            */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;


/*********************************************************************/
/* This class is used by the UBroker to determine if we are still    */
/* registered with the RMI server controlled by the AdminServer.      */
/* If not, we need to re-register with the RMI server.                */
/*********************************************************************/


import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.ubroker.broker.ubListenerThread;
import com.progress.ubroker.util.ubThread;
import com.progress.message.jbMsg;

import java.net.MalformedURLException;
import java.rmi.*;

/*********************************************************************/
/*                                                                   */
/* Class ubRMIWatchDog                                               */
/*                                                                   */
/*********************************************************************/

public class ubRMIWatchDog
    implements ubConstants, IWatchable
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/


/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private IAppLogger         m_log;
private ubListenerThread   m_listener;
private String             m_rmiUrl;
private boolean            m_rmiAvailable;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public ubRMIWatchDog (ubListenerThread listener,
                      String rmiUrl,
                      IAppLogger log )
    {
    m_listener         = listener;
    m_log              = log;
    m_rmiUrl           = rmiUrl;
    m_rmiAvailable     = true;
    }

/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void watchEvent()
{
    /* this method periodically tests to see if the specified broker is   */
    /* still registered with the RMI server.  If not, then we re-register */
    /* with the RMI server.  This may be necessary if the AdminServer     */
    /* crashes and is restarted.                                          */
   
    try
    {
        ubThread.lookupService(m_rmiUrl);
    }
    catch (RemoteException re)
    {
        /* RMI is not available */
        if (m_rmiAvailable)
        {
            m_rmiAvailable = false;  // only output message once

            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC, 
                               jbMsg.jbMSG167,  // "RMI unavailable - lost contact with AdminServer. ",
                               new Object[] {  });
        }
    }
    catch (MalformedURLException ue)
    {
        /* Exception binding broker (bad URL) : %s<ExceptionMsg_string> */
        m_log.logError(jbMsg.jbMSG109,
                       new Object[] { ue.getMessage() }
                      );
    }
    catch (NotBoundException be)
    {
        /* RMI is available but we are not registered. */
        try
        {
            ubThread.rebindService(m_rmiUrl, (Remote)m_listener);
            m_rmiAvailable = true;

            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC, 
                               jbMsg.jbMSG168, // "RMI available - re-registered with AdminServer. ",
                               new Object[] {  });
        }
        catch (RemoteException r)
        {
            if (!m_rmiAvailable)
            {
                /* Exception binding broker : %s<ExceptionMsg_string> */
                m_log.logError(jbMsg.jbMSG108,
                               new Object[] { r.getMessage() }
                              );
                m_rmiAvailable = true;
            }
        }
        catch (MalformedURLException me)
        {
            if (!m_rmiAvailable)
            {
                /* Exception binding broker (bad URL) : %s<ExceptionMsg_string> */
                m_log.logError(jbMsg.jbMSG109,
                               new Object[] { me.getMessage() }
                              );
                m_rmiAvailable = true;
            }
        }
    }   // end of NotBoundException
}

/**********************************************************************/
/* end of ubRMIWatchDog class                                         */
/**********************************************************************/

}

