
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 */

/*********************************************************************/
/* Module : ubWebSpeedMsg                                            */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class ubWebSpeedMsg                                               */
/*                                                                   */
/*********************************************************************/

public class ubWebSpeedMsg
    extends ubMsg
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*
   WebSpeed header format

    int   who          who sent this message
    int   msg_type     webspeed message identifier
    int   msg_time     msg timestamp (not used)
    int   msg_len      length of follow on message
    int   version      webspeed version of message
*/

private static final int WSHDRLEN            = 20;

/* header structure */
private static final int OFST_WHO            = 0;
private static final int OFST_MSGTYPE        = 4;
private static final int OFST_TIMESTAMP      = 8;
private static final int OFST_MSGLEN         = 12;
private static final int OFST_VERSION        = 16;

/* WHO field values */

public static final int WEB_NOBODY   = 0x00;
public static final int WEB_PROGRESS = 0x01; /* is an Agent                 */
public static final int WEB_CGIIP    = 0x02; /* Messenger: CGI-based        */
public static final int WEB_WSISA    = 0x04; /* Messenger: ISAPI-based      */
public static final int WEB_WSNSA    = 0x08; /* Messenger: NSAPI-based      */
public static final int WEB_MSNGR    = 0x10; /* Message Type is "Messenger" */
public static final int WEB_WTB      = 0x20; /* Broker Service              */
public static final int WEB_WSASP    = 0x40; /* Messenger: ASP-based        */
public static final int WEB_WTBMAN   = 0x100;/* must be last in list ????   */

/* msg_types */

public static final int DEF_MSGTYPE           = 0x00; /* invalid             */
public static final int WEB_FORM_INPUT        = 0x01; /* cgiip -> form input */
public static final int WEB_CUR_ENV_STRING    = 0x02; /* The current env */
public static final int WEB_PRG_TO_WTB_ALIVE  = 0x03; /* agent alive */
public static final int WEB_CGIIP_GET_PROG    = 0x04; /* CGIIP PORT request !*/
public static final int WEB_PRG_TO_WTB_AV     = 0x05; /* agent available */
public static final int WEB_PRG_TO_WTB_BU     = 0x06; /* agent busy */
public static final int WEB_PRG_TO_WTB_LI     = 0x07; /* agent limbo */
public static final int WEB_PRG_TO_WTB_LO     = 0x08; /* agent locked */
public static final int WEB_SHUTDOWN          = 0x09; /* broker shutdown */
public static final int WEB_MAN_TO_WTB_START  = 0x0a; /* broker agent startup */
public static final int WEB_MAN_TO_WTB_SHUT   = 0x0b; /* broker agent shutdown*/
public static final int WEB_MAN_TO_WTB_STOP   = 0x0c; /* broker shutdown */
public static final int WEB_STATUS            = 0x0d; /* broker status request*/
public static final int WEB_PRG_TO_WTB_LOG    = 0x0e; /* log agent message */
public static final int WEB_MAN_TO_WTB_STOPPID= 0x0f; /* broker agent stop pid*/
public static final int WEB_RECONNECT         = 0x10; /* broker->agent connect*/
public static final int WEB_CUR_ENV_STRING_10 = 0x11; /* 10.1A cgiip to progress - current env */
public static final int WEB_ADMIN             = 0x12; /* broker->agent administrative request */

public static final int[] WSMSGTYPE =
    {
        DEF_MSGTYPE
    ,   WEB_FORM_INPUT
    ,   WEB_CUR_ENV_STRING
    ,   WEB_PRG_TO_WTB_ALIVE
    ,   WEB_CGIIP_GET_PROG
    ,   WEB_PRG_TO_WTB_AV
    ,   WEB_PRG_TO_WTB_BU
    ,   WEB_PRG_TO_WTB_LI
    ,   WEB_PRG_TO_WTB_LO
    ,   WEB_SHUTDOWN
    ,   WEB_MAN_TO_WTB_START
    ,   WEB_MAN_TO_WTB_SHUT
    ,   WEB_MAN_TO_WTB_STOP
    ,   WEB_STATUS
    ,   WEB_PRG_TO_WTB_LOG
    ,   WEB_MAN_TO_WTB_STOPPID
    ,   WEB_RECONNECT
    ,   WEB_CUR_ENV_STRING_10
    ,   WEB_ADMIN
    };

public static final String[] DESC_WSMSGTYPE =
    {
        "DEF_MSGTYPE"
    ,   "WEB_FORM_INPUT"
    ,   "WEB_CUR_ENV_STRING"
    ,   "WEB_PRG_TO_WTB_ALIVE"
    ,   "WEB_CGIIP_GET_PROG"
    ,   "WEB_PRG_TO_WTB_AV"
    ,   "WEB_PRG_TO_WTB_BU"
    ,   "WEB_PRG_TO_WTB_LI"
    ,   "WEB_PRG_TO_WTB_LO"
    ,   "WEB_SHUTDOWN"
    ,   "WEB_MAN_TO_WTB_START"
    ,   "WEB_MAN_TO_WTB_SHUT"
    ,   "WEB_MAN_TO_WTB_STOP"
    ,   "WEB_STATUS"
    ,   "WEB_PRG_TO_WTB_LOG"
    ,   "WEB_MAN_TO_WTB_STOPPID"
    ,   "WEB_RECONNECT"
    ,   "WEB_CUR_ENV_STRING_10"
    ,   "WEB_ADMIN"
    ,   null
    };

/* timestamp values */

public static final int DEF_TIMESTAMP         = 0x00;

/* msg_len values */

public static final int DEF_MSGLEN            = 0x00;


/* version ... must match WEBSPEED_VERSION in wscommon.h */
/* we may want to bump this for ub version */

private static final int CUR_WS_VERSION     = 9010;/* Version: Major/Minor */

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private byte [] wsmsghdr;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubWebSpeedMsg(short ubver)
    {
    super(ubver, ubMsg.UBTYPE_WEBSPEED);
    initWSMsg(
              WEB_NOBODY
             ,DEF_MSGTYPE
             ,DEF_TIMESTAMP
             ,DEF_MSGLEN
             ,CUR_WS_VERSION
	     );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubWebSpeedMsg(short ubver, int bufsize)
    {
    super(ubver, ubMsg.UBTYPE_WEBSPEED, bufsize);
    initWSMsg(
              WEB_NOBODY
             ,DEF_MSGTYPE
             ,DEF_TIMESTAMP
             ,DEF_MSGLEN
             ,CUR_WS_VERSION
	     );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this constructor added for efficiency ... it is a bit unsafe since */
/* the constructor does not validate that the header fields contain   */
/* "reasonable" data .. it could, but we're looking for speed here    */

public ubWebSpeedMsg(byte[] ubhdr, byte[] tlvbuf, byte[] wshdr)
    throws
        ubMsg.InvalidMsgVersionException
    {
    super(ubhdr, tlvbuf);
    wsmsghdr = wshdr;
    }

/*********************************************************************/
/* Public Static Methods                                             */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function stores a string in "WebSpeed" format */

/*  string data + null terminator */

public static int setNetString(byte[] s, int idx, String val)
    {
    int len;
    int n;
    int i;

    n = (val == null) ? 0 : val.length();

    if (val != null)
        System.arraycopy(val.getBytes(), 0, s, idx, n);

    idx += n;
    s[idx] = 0;

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function gets a string in "WebSpeed" format */

/*  string data + null terminator */

public static String getNetString(byte[] s, int idx)
    {
    String ret;
    int len;

    for (len = 0; s[idx+len] != 0; len++)
        ;

    ret = (len == 0) ? null : new String(s, idx, len);

    return ret;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static int getSrvHdrlen()
    {
    return WSHDRLEN;
    }

/*********************************************************************/
/* accessor methods                                                  */
/*********************************************************************/

/**********************************************************************/
/* START of webspeed headers                                          */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getwshdr()
    {
    return wsmsghdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwshdr(byte[] hdr)
    {
    wsmsghdr = hdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsHeader(int who,
                         int msgtype,
                         int msg_len)
    {
    setwsWho( who );
    setwsMsgtype(msgtype);
    setwsTimestamp( DEF_TIMESTAMP );
    setwsMsglen(msg_len);
    setwsVersion( CUR_WS_VERSION );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getwsWho()
    {
    return getNetInt(wsmsghdr, OFST_WHO);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsWho(int who)
    {
    setNetInt(wsmsghdr, OFST_WHO, who);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getwsMsgtype()
    {
    return getNetInt(wsmsghdr, OFST_MSGTYPE);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsMsgtype(int msg_type)
    {
    setNetInt(wsmsghdr, OFST_MSGTYPE, msg_type);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getwsTimestamp()
    {
    return getNetInt(wsmsghdr, OFST_TIMESTAMP);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsTimestamp(int timestamp)
    {
    setNetInt(wsmsghdr, OFST_TIMESTAMP, timestamp);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getwsMsglen()
    {
    return getNetInt(wsmsghdr, OFST_MSGLEN);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsMsglen(int msglen)
    {
    setNetInt(wsmsghdr, OFST_MSGLEN, (short) msglen);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getwsVersion()
    {
    return getNetInt(wsmsghdr, OFST_VERSION);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setwsVersion(int ver)
    {
    setNetInt(wsmsghdr, OFST_VERSION, ver);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean cmpMsg(ubWebSpeedMsg msg)
    {
    boolean ret;
    int i, len;

	ret = super.cmpMsg(msg);

	if (ret)
      ret = (
    	( getwsWho()           == msg.getwsWho()        ) &&
        ( getwsMsgtype()       == msg.getwsMsgtype()    ) &&
    	( getwsTimestamp()     == msg.getwsTimestamp()  ) &&
        ( getwsMsglen()        == msg.getwsMsglen()     ) &&
        ( getwsVersion()       == msg.getwsVersion()    ) 
	   );

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader()
    {
    System.err.println(" who= " + getwsWho() );
    System.err.println(" msgtype= " + getwsMsgtype());
    System.err.println(" timestamp= " + getwsTimestamp() );
    System.err.println(" msglen= " + getwsMsglen());
    System.err.println(" version= " + getwsVersion() );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader(int lvl, int indexEntryType, IAppLogger lg)
    {
    lg.logWithThisLevel(lvl, indexEntryType,
                 " who= " + getwsWho() );
    lg.logWithThisLevel(lvl, indexEntryType,
                 " msgtype= " + getwsMsgtype());
    lg.logWithThisLevel(lvl, indexEntryType,
                 " timestamp= " + getwsTimestamp() );
    lg.logWithThisLevel(lvl, indexEntryType,
                 " msglen= " + getwsMsglen());
    lg.logWithThisLevel(lvl, indexEntryType,
                 " version= " + getwsVersion() );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getSrvHeader()
    {
    return getwshdr();
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getSrvHeaderlen()
    {
    return WSHDRLEN;
    }

/**********************************************************************/
/* Abstract methods                                                   */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getSrvMsgDesc()
    {
    String ret;
    int msgtype = getwsMsgtype();

    try
        {
        ret = DESC_WSMSGTYPE[msgtype];
        }
    catch (Exception e)
        {
        ret = "WSMSGTYPE= 0x" + Integer.toString(msgtype,16);
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getMsglen()
    {
        return getwsMsglen();
    }

/*********************************************************************/
/* internal methods                                                  */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void initWSMsg(
        int who,
	int msg_type,
        int timestamp,
	int msg_len,
	int version
	)
    {
    wsmsghdr = new byte[WSHDRLEN];

    setNetInt(wsmsghdr, OFST_WHO, who);
    setNetInt(wsmsghdr, OFST_MSGTYPE, msg_type);
    setNetInt(wsmsghdr, OFST_TIMESTAMP, timestamp);
    setNetInt(wsmsghdr, OFST_MSGLEN, msg_len);
    setNetInt(wsmsghdr, OFST_VERSION, version);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/*
private String msgcodeDesc(int code)
    {
    String ret;
    int i;

    for (i = 0, ret = DESC_MSGCODE[i];
             ret != null;
                  ret = DESC_MSGCODE[++i])
        {
        if (code == CSMSSG_MSGCODES[i])
            break;
        }

    return ret;
    }
*/
/**********************************************************************/
/*                                                                    */
/**********************************************************************/

}  /* end of ubWebSpeedMsg.java */

