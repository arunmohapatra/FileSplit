/*
/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        IMsgOutputStream </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.ubroker.util;

import  java.lang.*;
import  java.io.IOException;

/**
 * The IubMsgOutputStream interface provides the required operations to support
 * MsgOutputStream capabilities the uBroker's BrokerSystem object.  The BrokerSystem
 * object encapsulates the AppServer protocol between clients and AppServers.
 *
 */
public interface IubMsgOutputStream
{
    /*
     * CLASS Constants
     * private static final <type>  <name> = <value>;
     */


    /*
     * PUBLIC METHODS:
     */

    /**
     * <!-- writeMsg() -->
     * <p>The writeMsg method writes a complete AppServer protocol message via
     * the network protocol to the AppServer.
     * </p>
     * <br>
     * @param ubMsg is a ubMsg object that encapsulates the AppServer protocol
     * message to transmit.
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     * @exception   NetworkProtoclException
     */
    public void         writeMsg(ubMsg msg)
        throws IOException, NetworkProtocolException;

    /**
     * <!-- flushMsg() -->
     * <p>The flush method insures that all of the AppServer protocol message
     * is transmitted to the network and not held up in some internal buffer
     * waiting to optomize the network transmission.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         flushMsg() throws IOException, NetworkProtocolException;

    /**
     * <!-- close() -->
     * <p>The close method indicates that the MsgOutputStream is not longer needed
     * as the connection to the AppServer is being broken.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         close() throws IOException;

    /**
     * <!-- setMsgBufferSize() -->
     * <p>The setMsgBufferSize allow the object's caller to override the
     * MsgOutputStream's buffer sizing and set it to a specific size.  This may
     * be used to pre-extend the MsgOutputStream buffer.
     * </p>
     * <br>
     * @param newBufferSize is an int value, greater than zero, that indicates
     * the new buffer size.  If the internal buffer is already that size or
     * larger, a resizing operation is not performed.  If a resizing opertion
     * is performed, a data copy operation is performed in order to not loose
     * data.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void         setMsgBufferSize(int newBufferSize) throws Exception;


    /**
     * <!-- getMsgBufferSize() -->
     * <p>The getMsgBufferSize returns the size of the MsgOutputStream's internal
     * buffer.
     * </p>
     * <br>
     * @return  int
     */
    public int          getMsgBufferSize();


    /**
     * <!-- setLoggingTraceLevel() -->
     * <p>The setLoggingTraceLevel sets the specific MsgOutputStream object's
     * Progress tracing level if it different from the parent Protocol's
     * tracing level that the MsgOutputStream class inherits.
     * </p>
     * <br>
     * @param newTraceLevel is an int value (1 - 6) that indicates the new
     * log tracing level for this object.
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void         setLoggingTraceLevel(int newTraceLevel) throws Exception;


    /**
     * <!-- getLoggingTraceLevel() -->
     * <p>This method will return the current trace level used for log output.
     * </p>
     * <br>
     * @return  int
     */
    public int          getLoggingTraceLevel();


}
