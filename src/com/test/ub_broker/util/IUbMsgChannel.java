/*************************************************************/
/* Copyright (c) 1984-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission in writing from Progress Software Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : UbMsgChannel                                             */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import  java.io.IOException;
import  com.progress.ubroker.util.ubMsg;
import  com.progress.ubroker.util.NetworkProtocolException;


/*********************************************************************/
/*                                                                   */
/* Interface IUbMsgChannel                                           */
/*                                                                   */
/*********************************************************************/

/**
 * The IUbMsgChannel interface provides the required operations to support
 * UbMsgChannel capabilities of the uBroker interface.
 */

public interface IUbMsgChannel
{
/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*********************************************************************/
/* Public methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

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

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

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

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    /**
     * <!-- close() -->
     * <p>The close method indicates that the UbMsgChannel is no longer needed
     * as the connection to the AppServer is being broken.
     * </p>
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public void         close() throws IOException;

}

