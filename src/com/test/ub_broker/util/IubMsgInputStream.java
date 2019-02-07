/*

/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>

/* <br>

/* <p>Class:        IubMsgInputStream </p>

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

 * The IubMsgInputStream interface provides the required operations to support

 * MsgInputStream capabilities the uBroker's BrokerSystem object.  The BrokerSystem

 * object encapsulates the AppServer protocol between clients and AppServers.

 *

 */

public interface IubMsgInputStream

{

    /*

     * CLASS Constants

     * private static final <type>  <name> = <value>;

     */





    /*

     * PUBLIC METHODS:

     */



    /**

     * <!-- readMsg() -->

     * <p>The readMsg method obtains a complete AppServer protocol message from

     * the network.

     * </p>

     * <br>

     * @return  ubMsg

     * <br>

     * @exception   IOException,

     * @exception   NetworkProtocolException,

     * @exception   ubMsg.MsgFormatException

     */

    public ubMsg        readMsg()

        throws IOException , ubMsg.MsgFormatException, NetworkProtocolException;



    /**

     * <!-- available() -->

     * <p>The available method indicates whether any AppServer protocol message

     * data is available for reading.

     * </p>

     * <br>

     * @return  int

     * <br>

     * @exception   IOException

     */

    public int          available() throws IOException;



    /**

     * <!-- close() -->

     * <p>The close method indicates that the MsgInputStream is no longer needed

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

     * MsgInputStream's buffer sizing and set it to a specific size.  This may

     * be used to pre-extend the MsgInputStream buffer.

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

     * <p>The getMsgBufferSize returns the size of the MsgInputStream's internal

     * buffer.

     * </p>

     * <br>

     * @return  int

     */

    public int          getMsgBufferSize();



    /**

     * <!-- setLoggingTraceLevel() -->

     * <p>The setLoggingTraceLevel sets the specific MsgInputStream object's

     * Progress tracing level if it different from the parent Protocol's

     * tracing level that the MsgInputStream class inherits.

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

