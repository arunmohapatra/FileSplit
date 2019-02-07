
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/
/*
 */

/*********************************************************************/
/* Module : MsgReader                                                */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.Socket;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;

/*********************************************************************/
/*                                                                   */
/* Class MsgReader                                                   */
/*                                                                   */
/*********************************************************************/

public class MsgReader
    implements Runnable
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/* session states */
public static final byte STATE_READY  = 0x00;
public static final byte STATE_CLOSED = 0x01;

static final String[] DESC_STATE =
  {
  " STATE_READY "
, " STATE_CLOSED "
  };

static final String[] DESC_STATE_EXT =
  {
  " READY "
, " CLOSED "
  };

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/


/*********************************************************************/
/* Static Methods                                                    */
/*********************************************************************/

public static MsgReader newMsgReader(String         parent,
    	                             Socket         sock,
    	                             MsgInputStream is,
                                     RequestQueue   destQueue,
                                     IAppLogger     log)
    {
    MsgReader  sockReader;
    Thread     sockThread;
    String     sockDesc;

    /* create the reader object */
    sockReader = new MsgReader(parent, sock, is, destQueue, log);

    /* create a new thread to power this reader */
    sockThread = new Thread(sockReader);

    /* make the thread a daemon */
    sockThread.setDaemon(true);

    /* name the thread so we can debug it more easily */
    sockDesc = sock.getInetAddress().getHostAddress() + ":" + sock.getPort();
    sockThread.setName(parent + "-" + sockDesc);

    /* store a reference to the thread in the object */
    sockReader.setThread(sockThread);

    /* start the thread up */
    sockThread.start();

    return sockReader;
    }

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private MsgInputStream m_inMsgStream;
private IAppLogger     m_log;
private RequestQueue   m_requestQueue;
private Thread         m_readerThread;
private Socket         m_sock;
private String         m_parent;

private int            m_current_state;
private boolean        m_shutdownRequested;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public MsgReader(String         parent,
    	         Socket         sock,
    	         MsgInputStream is,
                 RequestQueue   destQueue,
                 IAppLogger     log)
    {
    m_parent = parent;
    m_sock = sock;
    m_inMsgStream = is;
    m_log = log;
    m_requestQueue = destQueue;
    m_current_state = STATE_READY;
    m_shutdownRequested = false;
    }

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void run()
    {
    try
        {
        mainline();
        }
    catch ( Throwable rte )
        {
        if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
                           UBrokerLogContext.SUB_V_UB_BASIC))
            m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                           "Error reading from msg input stream = " +
                           rte);
        }
    
    if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
                       UBrokerLogContext.SUB_V_UB_BASIC))
        m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                       this.getThread().getName() + " done.");
    
    /* we're on the way out, so mark ourselves as closed */
    m_current_state = STATE_CLOSED;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String toString()
    {
    String ret;
   
    ret = getThread().getName();
    
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public Thread getThread()
    {
    return m_readerThread;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int getState()
    {
    return m_current_state;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public boolean isClosed()
    {
    return m_current_state == STATE_CLOSED;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void close()
    {
    m_current_state = STATE_CLOSED;
    }

/*********************************************************************/
/* Private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void mainline()
    {
    ubMsg      inMsg    = null;
    Request    rq       = null;
    ubAdminMsg adminMsg = null;
    boolean    fContinue;
    
    for (fContinue = true; fContinue; inMsg = null)
        {
        /* wait for a message from the input stream */
        /* this will block us forever               */
        try
            {
            inMsg = m_inMsgStream.readMsg();
            }
        catch (SocketTimeoutException timeoutException)
            {
            if (m_log.ifLogExtended(UBrokerLogContext.SUB_M_UB_BASIC,
                                    UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logExtended(UBrokerLogContext.SUB_V_UB_BASIC,
                                  "Timeout reading msg from " + 
                                   m_inMsgStream + " = " + timeoutException);

            /* fabricate an internal message indicating the error */
            adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_SOCKET_TIMEOUT);
            adminMsg.setadParm( 
                        new Object[] { timeoutException } 
                        );
            inMsg = (ubMsg) adminMsg;
            }
        catch (SocketException eIO)
            {
            /* "SocketClosed received on client connection: " + getFullName() */

            if (m_log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_BASIC,
                                   UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logVerbose(UBrokerLogContext.SUB_V_UB_BASIC,
                                  "SocketException reading msg from " + 
                                    m_inMsgStream + " = " + eIO);
            close();
            fContinue = false;
            }
        catch (IOException eIO)
        {
        /* "EOF received on client connection: " + getFullName() */

        if (m_log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_BASIC,
                               UBrokerLogContext.SUB_V_UB_BASIC))
            m_log.logVerbose(UBrokerLogContext.SUB_V_UB_BASIC,
                              "IOException reading msg from " + 
                                m_inMsgStream + " = " + eIO);

        adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_IOEXCEPTION);
        adminMsg.setadParm( 
                    new Object[] { eIO } 
                    );
        inMsg = (ubMsg) adminMsg;
        fContinue = false;
        }
        catch (ubMsg.MsgFormatException e)
            {
            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
        	                 UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                               "Error reading msg from " + 
                                m_inMsgStream + " = " + e);

            /* fabricate an internal message indicating the error */
            /* adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_READMSG_ERROR); */
            adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_MESSAGE_FORMAT_ERROR);
            adminMsg.setadParm( 
                        new Object[] { e }  
                        );

            inMsg = (ubMsg) adminMsg;
            fContinue = false;
            }
/*
        catch (NetworkProtocolException e)
            {
            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
                                 UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                               "Error reading msg from " + 
                                m_inMsgStream + " = " + e);

            // fabricate an internal message indicating the error 
            adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_NETWORK_PROTOCOL_ERROR);
            adminMsg.setadParm( 
                        new Object[] { e } 
                        );

            inMsg = (ubMsg) adminMsg;
            fContinue = false;
            }
*/
        catch (Exception readMsgException)
            {
            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
                                 UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                               "Error reading msg from " + 
                                m_inMsgStream + " = " + readMsgException);

            /* fabricate an internal message indicating the error */
            /* adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_READMSG_ERROR); */
            adminMsg = new ubAdminMsg(ubAdminMsg.ADRQ_IOEXCEPTION);
            adminMsg.setadParm( 
                        new Object[] { readMsgException } 
                        );

            inMsg = (ubMsg) adminMsg;
            fContinue = false;
            }

        /* if we've been closed, we've done ... don't send the message */
        if (m_current_state == STATE_CLOSED)
            {
            fContinue = false;
            continue;
            }
        
        /* make this a request object */
        rq = new Request(inMsg, null);

        /* enqueue the request to the destination queue */
        try
            {
            m_requestQueue.enqueueRequest(rq);
            }
        catch ( Queue.QueueException qe )
            {
            if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
                                 UBrokerLogContext.SUB_V_UB_BASIC))
                m_log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                               "Error enqueuing rq to " + 
                                m_requestQueue + " = " + qe);
            fContinue = false;
            }
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void setThread(Thread t)
    {
    m_readerThread = t;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

}  /* end of MsgReader */

