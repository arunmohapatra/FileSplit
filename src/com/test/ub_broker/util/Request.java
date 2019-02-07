
/*************************************************************/
/* Copyright (c) 1984-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: Request.java,v 1.3 1999/10/28 14:27:11 lecuyer Exp $
 */

/*********************************************************************/
/* Module : Request                                                  */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

/* utility imports */
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;

/*********************************************************************/
/*                                                                   */
/* Class Request                                                     */
/*                                                                   */
/*********************************************************************/

public class Request
        implements ubConstants
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private Object msg;
private Queue rspQueue;
private long    tsCreation;
private long    tsEnqueue;
private long    tsDequeue;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public Request(Object msg, Queue rspQueue)
    {
    this.msg = msg;
    this.rspQueue = rspQueue;

    tsCreation = System.currentTimeMillis();
    tsEnqueue = INVALID_TIMESTAMP;
    tsDequeue = INVALID_TIMESTAMP;
    }

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Object getMsg()
    {
    return msg;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setMsg(Object msg)
    {
    this.msg = msg;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Queue getRspQueue()
    {
    return rspQueue;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setRspQueue(Queue rspQueue)
    {
    this.rspQueue = rspQueue;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public long gettsCreation()
    {
    return tsCreation;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public long gettsEnqueue()
    {
    return tsEnqueue;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void settsEnqueue()
    {
    tsEnqueue = System.currentTimeMillis();
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public long gettsDequeue()
    {
    return tsDequeue;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void settsDequeue()
    {
    tsDequeue = System.currentTimeMillis();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void logStats(IAppLogger log)
    {
    long queue_time;


    if ((tsDequeue == INVALID_TIMESTAMP) || (tsEnqueue == INVALID_TIMESTAMP))
        {
        log.logError(this + ".logStats() ERROR :" +
                     " tsEnqueue= " + tsEnqueue +
                     " tsDequeue= " + tsDequeue 
                    );
        return;
        }

    queue_time = tsDequeue - tsEnqueue;

    if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
        {
        log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                 this + ".logStats() : tsEnqueue= " + tsEnqueue +
                 " tsDequeue= " + tsDequeue + 
                 " queue_time= " + queue_time
                 );
        }

    if (log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_STATS,
    		             UBrokerLogContext.SUB_V_UB_STATS))
        {
        log.logVerbose(UBrokerLogContext.SUB_V_UB_STATS,
                     this + ".logStats(): queue_time= " + queue_time
                     );
        }
    }


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/* Private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/


}  /* end of Request */

