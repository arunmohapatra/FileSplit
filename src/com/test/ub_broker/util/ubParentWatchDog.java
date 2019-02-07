/*************************************************************/
/* Copyright (c) 2003-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/*                                                                   */
/* Module : ubParentWatchDog                                         */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;


/*********************************************************************/
/* This class is used by the UBroker to determine if the parent that */
/* started the ClientConnect adapter is still running.  If not, then */
/* we need to shut down the adapter.                                 */
/*********************************************************************/


import com.progress.common.ehnlog.IAppLogger;
import com.progress.ubroker.broker.ubListenerThread;
import com.progress.message.jbMsg;


/*********************************************************************/
/*                                                                   */
/* Class ubParentWatchDog                                            */
/*                                                                   */
/*********************************************************************/

public class ubParentWatchDog
    implements ubConstants, IWatchable
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/


/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private IAppLogger         m_log;
private ubProperties       m_properties;
private ubListenerThread   m_listener;
private int                m_parentPID;


static 
  {
  }

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public ubParentWatchDog (ubListenerThread listener,
		                 ubProperties prop,
                         IAppLogger log )
    {
    m_listener         = listener;
    m_properties       = prop;
    m_log              = log;
    m_parentPID        = m_properties.parentPID;
    }

/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void watchEvent()
    {
    /* this method periodically tests to see if the parent process that   */
    /* started this ClientConnect adapter is still running.  If not, then */
    /* the adapter is shut down.                                          */
   
    if (!parentIsRunning() )
        {
        /* "parent process " + m_properties.parentPID +
	       " terminated ... requesting adapter shutdown.");  */

        m_log.logError(
                   jbMsg.jbMSG134,
                   new Object[] { new Integer(m_properties.parentPID)}
        	);

	    m_listener.requestShutdown();
	    m_parentPID = 0; /* this will prevent repeated shutdown attempts */
        }
    }

/*********************************************************************/
/* private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean parentIsRunning()
    {
    boolean ret = true;
    int isRunning = 0;

    if (m_parentPID > 0)
        {
        isRunning = m_properties.env.query_PID_JNI(m_parentPID, true);

        ret = (isRunning > 0);
        }
    else ret = true;

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/**********************************************************************/
/* end of ubParentWatchDog class                                      */
/**********************************************************************/

}

