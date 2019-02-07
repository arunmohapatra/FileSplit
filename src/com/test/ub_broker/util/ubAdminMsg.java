
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubAdminMsg                                               */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class ubAdminMsg                                                  */
/*                                                                   */
/*********************************************************************/

public class ubAdminMsg
    extends ubMsg
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

public static final byte DEF_SRC           = 0;

/* ubAdmin data transmission format

    byte src;          who sent this message
    byte rq;           what type of message
    int  ret;          return code
    int  msg_len;      length of follow on message
*/

private static final int ADMSGHDRLEN         = 10;

private static final int OFST_SRC            = 0;
private static final int OFST_RQ             = 1;
private static final int OFST_RSP            = 2;
private static final int OFST_MSGLEN         = 6;

/* ADSRC field values */

public static final byte DEF_ADSRC                   = 0x00;
public static final byte ADSRC_LISTNER               = 0x01;
public static final byte ADSRC_WATCHDOG              = 0x02;
public static final byte ADSRC_SERVER                = 0x03;
public static final byte ADSRC_CLIENT                = 0x04;
public static final byte ADSRC_ADMIN                 = 0x05;

public static final String[] DESC_ADSRC =
  {
  "DEFAULT_ADSRC"
, "ADSRC_LISTENER"
, "ADSRC_WATCHDOG"
, "ADSRC_SERVER"
, "ADSRC_CLIENT"
  };

/* ADRQ field values */

public static final byte DEF_ADRQ                    = 0x00;
public static final byte ADRQ_THREAD_STARTUP         = 0x01;
public static final byte ADRQ_THREAD_SHUTDOWN        = 0x02;
public static final byte ADRQ_THREAD_TERMINATE       = 0x03;
public static final byte ADRQ_SERVER_TERMINATE       = 0x04;
public static final byte ADRQ_CLIENT_CONNECT         = 0x05;
public static final byte ADRQ_CLIENT_DISCONNECT      = 0x06;
public static final byte ADRQ_IOEXCEPTION            = 0x06;
public static final byte ADRQ_THREAD_WAKEUP          = 0x07;
public static final byte ADRQ_TIMEOUT                = 0x08;
public static final byte ADRQ_PROCSTATS              = 0x09;
public static final byte ADRQ_ASK_ACTIVITY_TIMEOUT   = 0x0A;
public static final byte ADRQ_ASK_RESPONSE_TIMEOUT   = 0x0B;
public static final byte ADRQ_PROPERTY_FILE_UPDATE   = 0x0C;
public static final byte ADRQ_MESSAGE_FORMAT_ERROR   = 0x0D;
public static final byte ADRQ_SOCKET_TIMEOUT         = 0x0E;
public static final byte ADRQ_NETWORK_PROTOCOL_ERROR = 0x0F;

public static final String[] DESC_ADRQ =
  {
  "DEF_ADRQ"
, "ADRQ_THREAD_STARTUP"
, "ADRQ_THREAD_SHUTDOWN"
, "ADRQ_THREAD_TERMINATE"
, "ADRQ_SERVER_TERMINATE"
, "ADRQ_CLIENT_CONNECT"
, "ADRQ_CLIENT_DISCONNECT"
, "ADRQ_THREAD_WAKEUP"
, "ADRQ_TIMEOUT"
, "ADRQ_PROCSTATS"
, "ADRQ_ASK_ACTIVITY_TIMEOUT"
, "ADRQ_ASK_RESPONSE_TIMEOUT"
, "ADRQ_PROPERTY_FILE_UPDATE"
, "ADRQ_READMSG_ERROR"
, "ADRQ_SOCKET_TIMEOUT"
, "ADRQ_NET_PROTOCOL_ERROR"
  };

/* ADRSP field values */

public static final int DEF_ADRSP                    = 0x00;
public static final int ADRSP_OK                     = 0x00;
public static final int ADRSP_ERROR                  = 0x01;
public static final int ADRSP_UNABLE_TO_START_SERVER = 0x02;
public static final int ADRSP_UNABLE_TO_STOP_SERVER  = 0x03;
public static final int ADRSP_UNABLE_TO_INIT_CLIENT  = 0x04;

public static final String[] DESC_ADRSP =
  {
  "ADRSP_OK"
, "ADRSP_ERROR"
, "ADRSP_UNABLE_TO_START_SERVER"
, "ADRSP_UNABLE_TO_STOP_SERVER"
, "ADRSP_OK"
  };

private static final int DEF_MSGLEN         = 0;

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private byte [] admsghdr;
private Object [] admparm;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAdminMsg()
    {
    super(ubMsg.MAX_UBVER, ubMsg.UBTYPE_ADMIN);
    initADMsg(
	        DEF_ADSRC,
		DEF_ADRQ,
		DEF_ADRSP,
		DEF_MSGLEN );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAdminMsg(int bufsize)
    {
    super(ubMsg.MAX_UBVER, ubMsg.UBTYPE_ADMIN, bufsize);
    initADMsg(
	        DEF_ADSRC,
		DEF_ADRQ,
		DEF_ADRSP,
		DEF_MSGLEN
		);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAdminMsg(byte rq)
    {
    super(ubMsg.MAX_UBVER, ubMsg.UBTYPE_ADMIN);
    initADMsg(	DEF_ADSRC,
		DEF_ADRQ,
		DEF_ADRSP,
		DEF_MSGLEN );

    setubRq(ubMsg.UBRQ_ADMIN);
    setadRq(rq);
    }

/**********************************************************************/
/* Public static methods                                              */
/**********************************************************************/

/**********************************************************************/
/* accessor methods                                                   */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getadMsghdr()
    {
    return admsghdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setadMsghdr(byte[] hdr)
    {
    admsghdr = hdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte getadSrc()
    {
    return admsghdr[OFST_SRC];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setadSrc(byte src)
    {
    admsghdr[OFST_SRC] = src;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte getadRq()
    {
    return admsghdr[OFST_RQ];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getadDesc()
    {
    int rq = getadRq();
    String desc;

    try
        {
        desc = DESC_ADRQ[rq];
        }
    catch (Exception e)
        {
        desc = "ADRQ= 0x" + Integer.toString(rq,16);
        }

    return desc;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setadRq(byte adRq)
    {
    admsghdr[OFST_RQ] = adRq;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getadRsp()
    {
    return getNetInt(admsghdr, OFST_RSP);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setadRsp(int rsp)
    {
    setNetInt(admsghdr, OFST_RSP, rsp);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getMsglen()
    {
    return getNetInt(admsghdr, OFST_MSGLEN);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setMsglen(int msglen)
    {
    setNetInt(admsghdr, OFST_MSGLEN, msglen);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/


public boolean cmpMsgHdr(ubMsg ubmsg)
    {
    boolean ret;
    int i;
    int len;

    ubAdminMsg msg = (ubAdminMsg) ubmsg;

    ret = (
        (getadSrc()           == msg.getadSrc())                 &&
        (getadRq()            == msg.getadRq())                 &&
        (getadRsp()           == msg.getadRsp())                 &&
        (getMsglen()          == msg.getMsglen())
	   );

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public Object[] getadParm()
    {
    return admparm;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setadParm(Object[] parm)
    {
    admparm = parm;
    }

/**********************************************************************/
/* Abstract methods                                                   */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getSrvHeader()
    {
    return getadMsghdr();
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getSrvHeaderlen()
    {
    return ADMSGHDRLEN;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader()
    {
    int len;

    System.err.println(" src= " + getadSrc() );
    System.err.println(" adRq= " + getadRq() + 
                            " " + DESC_ADRQ[getadRq()]);
    System.err.println(" adRq= " + getadRsp() + 
                            " " + DESC_ADRSP[getadRsp()]);
    System.err.println(" msglen= " + getMsglen());

    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader(int lvl, int indexEntryType, IAppLogger lg)
    {
    int len;

    lg.logWithThisLevel(lvl, indexEntryType,
                 " src= " + getadSrc() );
    lg.logWithThisLevel(lvl, indexEntryType,
           " adRq= " + getadRq() + " " + DESC_ADRQ[getadRq()]);
    lg.logWithThisLevel(lvl, indexEntryType,
           " rsp= " + getadRsp() + " " + DESC_ADRSP[getadRsp()]);
    lg.logWithThisLevel(lvl, indexEntryType,
                 " msglen= " + getMsglen());
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getSrvMsgDesc()
    {
    String ret;
    int adrq = getadRq();

    try
        {
        ret = DESC_ADRQ[adrq];
        }
    catch (Exception e)
        {
        ret = "[ADRQ= 0x" + Integer.toString(adrq,16) + "]";
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/*********************************************************************/
/* Internal methods                                                  */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void initADMsg(	byte src,
			byte Rq,
			int  rsp,
			int  msglen
			)
    {
    admsghdr = new byte[ADMSGHDRLEN];

    admsghdr[OFST_SRC] = src;
    admsghdr[OFST_RQ] = Rq;
    setNetInt(admsghdr, OFST_RSP, rsp);
    setNetInt(admsghdr, OFST_MSGLEN, msglen);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

}  /* end of ubAdminMsg */

