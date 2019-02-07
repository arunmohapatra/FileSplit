
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubMsg                                                    */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.exception.ProException;

/*********************************************************************/
/*                                                                   */
/* Class ubMsg                                                       */
/*                                                                   */
/*********************************************************************/

public abstract class ubMsg
    implements ubConstants
{

/*********************************************************************/
/* embedded classes                                                  */
/*********************************************************************/

/*********************************************************************/
/* MsgFormat Exceptions                                              */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class MsgFormatException extends ProException
    {
        public MsgFormatException(String detail)
        {
            super("MsgFormat", new Object[] { detail } );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class InvalidMsgVersionException extends MsgFormatException
    {
        public InvalidMsgVersionException(String detail)
        {
            super("InvalidMsgVersion[" + detail + "]");
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class InvalidHeaderLenException extends MsgFormatException
    {
        public InvalidHeaderLenException(String detail)
        {
            super("InvalidHeaderLen[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class InvalidServerTypeException extends MsgFormatException
    {
        public InvalidServerTypeException(String detail)
        {
            super("InvalidServerType[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class InvalidTlvBufferException extends MsgFormatException
    {
        public InvalidTlvBufferException(String detail)
        {
            super("InvalidTlvBuffer[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class WrongServerTypeException extends MsgFormatException
    {
        public WrongServerTypeException(String detail)
        {
            super("WrongServerType[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/* TLV Exceptions                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class TlvAccessException extends ProException
    {
        public TlvAccessException(String detail)
        {
            super("TlvAccessException", new Object[] { detail } );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class TlvFieldNotFoundException extends TlvAccessException
    {
        public TlvFieldNotFoundException(String detail)
        {
            super("TlvFieldNotFound[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

    public static class TlvFieldAlreadyExistsException extends TlvAccessException
    {
        public TlvFieldAlreadyExistsException(String detail)
        {
            super("TlvFieldAlreadyExists[" + detail + "]" );
        };


        public String getDetail()
        {
            return (String)getArgument(0);
        }
    }

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/* format of V0 message */
/*
   ub header format

    short  ubVer       version of this header 
    byte   ubType      format of this message 
    byte   ubSrc       source of this message 
    int    ubRq        request code
    int    ubRqExt     request code extension
    int    ubRsp       response code
    int    ubRspExt    response code extension
*/

/* format of V1 message */
/*
   ub header format

    short  ubVer       version of this header 
    byte   ubType      format of this message 
    byte   ubSrc       source of this message 
    int    ubRq        request code
    short  ubTlvLen    length of tlv buffer
    short  ubRqExt     request code extension
    int    ubRsp       response code
    int    ubRspExt    response code extension
*/


public static final int UBHDRLEN            = 20;

public static final int OFST_UBVER          = 0;
public static final int OFST_UBTYPE         = 2;
public static final int OFST_UBSRC          = 3;
public static final int OFST_UBRQ           = 4;
public static final int OFST_UBRQEXT        = 8;
public static final int OFST_UBTLVLEN       = 8; /* upper word of rqext */
public static final int OFST_UBRSP          = 12;
public static final int OFST_UBRSPEXT       = 16;

public static final short UBMSG_PROTOCOL_V0        = 0x6C;
public static final short UBMSG_PROTOCOL_V1        = 0x6D;
public static final short MIN_UBVER                = UBMSG_PROTOCOL_V0;
public static final short MAX_UBVER                = UBMSG_PROTOCOL_V1;

public static final short DEF_TLVBUFLEN            = 0;

/* UBTYPE field values */

public static final byte UBTYPE_INVALID            = 0x00;
public static final byte UBTYPE_ADMIN              = 0x01;
public static final byte UBTYPE_APPSERVER          = 0x02;
public static final byte UBTYPE_WEBSPEED           = 0x03;
public static final byte UBTYPE_NAMESERVER         = 0x04;
public static final byte UBTYPE_ADAPTER            = 0x05;
public static final byte UBTYPE_AIA                = 0x06;

public static final byte MAX_UBTYPE                = 0x07;

public static final String[] DESC_UBTYPE =
  {
  "UBTYPE_INVALID"
, "UBTYPE_ADMIN"
, "UBTYPE_APPSERVER"
, "UBTYPE_WEBSPEED"
, "UBTYPE_NAMESERVER"
, "UBTYPE_ADAPTER"
, "UBTYPE_AIA" };

/* UBSRC field values */

public static final byte DEF_UBSRC                 = 0x00;
public static final byte UBSRC_BROKER              = 0x01;
public static final byte UBSRC_NAMESERVER          = 0x02;
public static final byte UBSRC_CLIENT              = 0x03;
public static final byte UBSRC_SERVER              = 0x04;
public static final byte UBSRC_ADAPTER             = 0x05;
public static final byte UBSRC_AIA                 = 0x06;

public static final String[] DESC_UBSRC =
  {
  "DEFAULT_UBSRC"
, "UBSRC_BROKER"
, "UBSRC_NAMESERVER"
, "UBSRC_CLIENT"
, "UBSRC_SERVER"
, "UBSRC_ADAPTER"
, "UBSRC_AIA"
  };


/* UBRQ field values */

public static final int DEF_UBRQ                    = 0x00;
public static final int UBRQ_ADMIN                  = 0x01;
public static final int UBRQ_XID                    = 0x02;
public static final int UBRQ_CONNECT                = 0x03;
public static final int UBRQ_WRITEDATA              = 0x04;
public static final int UBRQ_WRITEDATALAST          = 0x05;
public static final int UBRQ_DISCONNECT             = 0x06;
public static final int UBRQ_SETSTOP                = 0x07;
public static final int UBRQ_SHUTDOWN               = 0x08;
public static final int UBRQ_SRVR_STATE_CHG         = 0x09;
public static final int UBRQ_SRVR_LOG_MSG           = 0x0A;
public static final int UBRQ_CONNECT_DIRECT         = 0x0B;
public static final int UBRQ_RSPDATA                = 0x0C;
public static final int UBRQ_RSPDATALAST            = 0x0D;
public static final int UBRQ_RSPCONN                = 0x0E;
public static final int UBRQ_RSPDISC                = 0x0F;
public static final int UBRQ_INIT_RQ                = 0x10;
public static final int UBRQ_FINISH_RQ              = 0x11;
public static final int UBRQ_BROKER_STATUS          = 0x12;
public static final int UBRQ_BROKER_STATUS_RSP      = 0x13;
public static final int UBRQ_SEND_EMPTY_MSG         = 0x14;
public static final int UBRQ_RSPXID                 = 0x15;
public static final int UBRQ_ASKPING_RQ             = 0x16;
public static final int UBRQ_ASKPING_RSP            = 0x17;
public static final int UBRQ_WEBSTREAM_RSP          = 0x18;
public static final int UBRQ_WEBSTREAM_RSPLAST      = 0x19;

public static final String[] DESC_UBRQ =
  {
  "DEF_UBRQ"
, "UBRQ_ADMIN"
, "UBRQ_XID"
, "UBRQ_CONNECT"
, "UBRQ_WRITEDATA"
, "UBRQ_WRITEDATALAST"
, "UBRQ_DISCONNECT"
, "UBRQ_SETSTOP"
, "UBRQ_SHUTDOWN"
, "UBRQ_SRVR_STATE_CHG"
, "UBRQ_SRVR_LOG_MSG"
, "UBRQ_CONNECT_DIRECT"
, "UBRQ_RSPDATA"
, "UBRQ_RSPDATALAST"
, "UBRQ_RSPCONN"
, "UBRQ_RSPDISC"
, "UBRQ_INIT_RQ"
, "UBRQ_FINISH_RQ"
, "UBRQ_BROKER_STATUS"
, "UBRQ_BROKER_STATUS_RSP"
, "UBRQ_SEND_EMPTY_MSG"
, "UBRQ_RSPXID"
, "UBRQ_ASKPING_RQ"
, "UBRQ_ASKPING_RSP"
, "UBRQ_WEBSTREAM_RSP"
, "UBRQ_WEBSTREAM_RSPLAST"
 };

/* Rq UBRQEXT field values */
public static final int DEF_UBRQEXT                   = 0x00;
public static final int UBRQEXT_BOUND                 = 0x01;

public static final int UBRQEXT_SUMMARY_STATUS        = 0x00;
public static final int UBRQEXT_DETAIL_STATUS         = 0x01;

public static final int UBRQEXT_NEED_NEW_CONNID       = 0x01;  /* 4/18/00, e.tan */

/* UBRSP field values */

public static final int DEF_UBRSP                     = 0x00;
public static final int UBRSP_OK                      = 0x00;
public static final int UBRSP_ERROR                   = 0x01;
public static final int UBRSP_PROTOCOL_ERROR          = 0x02;
public static final int UBRSP_UNSUPPORTED_RQ          = 0x03;
public static final int UBRSP_IOEXCEPTION             = 0x04;
public static final int UBRSP_BROKEREXCEPTION         = 0x05;
public static final int UBRSP_SERVERIPCEXCEPTION      = 0x06;
public static final int UBRSP_NO_AVAILABLE_SERVERS    = 0x07;
public static final int UBRSP_MSGFORMATEXCEPTION      = 0x08;
public static final int UBRSP_ABNORMAL_EOF            = 0x09;
public static final int UBRSP_FATAL                   = 0x0A;
public static final int UBRSP_CONN_REFUSED            = 0x0B;
public static final int UBRSP_TOO_MANY_CLIENTS        = 0x0C;
public static final int UBRSP_INVALID_SERVERMODE      = 0x0D;

public static final String[] DESC_UBRSP =
  {
  "UBRSP_OK"
, "UBRSP_ERROR"
, "UBRSP_PROTOCOL_ERROR"
, "UBRSP_UNSUPPORTED_UBRQ"
, "UBRSP_IOEXCEPTION"
, "UBRSP_BROKEREXCEPTION"
, "UBRSP_SERVERIPCEXCEPTION"
, "UBRSP_NO_AVAILABLE_SERVERS"
, "UBRSP_MSGFORMATEXCEPTION"
, "UBRSP_ABNORMAL_EOF"
, "UBRSP_FATAL"
, "UBRSP_CONN_REFUSED"
, "UBRSP_TOO_MANY_CLIENTS"
, "UBRSP_INVALID_SERVERMODE"
  };

public static final String[] DESC_UBRSP_EXT =
  {
  "Ok"
, "Unspecified Error"
, "Protocol Error"
, "Unsupported Request"
, "IO Exception"
, "Broker Exception"
, "ServerIPC Exception"
, "No Available Servers"
, "Invalid Message Format"
, "Abnormal End Of File from Server"
, "Fatal Error from Server"
, "Server refused connection"
, "Max client connections has been reached"
, "client operatingMode does not match broker operatingMode"
  };

/* UBRSPEXT field values ... these are BITFLAGS */

public static final int DEF_UBRSPEXT                  = 0x00;

/*
public static final int UBRSPEXT_MOREDATA             = 0x00000000;
public static final int UBRSPEXT_NOMOREDATA           = 0x00000001;
public static final int UBRSPEXT_STATEAWARE           = 0x40000002;
*/

public static final int UBRSPEXT_BOUND                = 0x00000001;
public static final int UBRSPEXT_CONNID_SENT          = 0x00000002; /* response contains new connid 4/21/00 e.tan*/
public static final int UBRSPEXT_BINMAXSIZE_AWARE     = 0x00000004; /* only 10.1A broker/agents (or later) */

public static final String[] DESC_UBRSPEXT =
  {
  "DEF_UBRSPEXT"
  };

public static final int DEF_BUFSIZE                 = 0x00;
public static final int DEF_BUFLEN                  = 0x00;

/* TLV Types */

public static final short TLVTYPE_RQID            = 0x01;
public static final short TLVTYPE_UNCMPRLEN       = 0x02; 
public static final short TLVTYPE_CMPRLEVEL       = 0x03; 
public static final short TLVTYPE_CMPRMINSZ       = 0x04;
public static final short TLVTYPE_CMPRCAPABLE     = 0x05; 

public static final short TLVTYPE_WHATEVER        = 0x06; /* artifact??? */
public static final short TLVTYPE_CLIENT_CODEPAGE = 0x07;
public static final short TLVTYPE_CONNECTION_ID   = 0x08;
public static final short TLVTYPE_CLIENT_CONTEXT  = 0x09;

public static final short TLVTYPE_ASKPING_VER     = 0x0A;
public static final short TLVTYPE_ASKPING_CAPS    = 0x0B;
public static final short TLVTYPE_ASKPING_RQST_ACK= 0x0C;
public static final short TLVTYPE_LG_HEADER       = 0x0D;  /* Actional Manifest */
public static final short TLVTYPE_CLIENT_HOSTNAME = 0x0E;
public static final short TLVTYPE_PROCEDURE_NAME  = 0x0F;
public static final short TLVTYPE_CLIENTPRINCIPAL = 0x10;
public static final short TLVTYPE_NETBUFFERSIZE   = 0x11;

// NxGAS
public static final short TLVTYPE_BROKERSESSIONID = 0x20;
public static final short TLVTYPE_ABLSESSIONID    = 0x21;
public static final short TLVTYPE_SESSIONTYPE     = 0x22;
public static final short TLVTYPE_PASAGENTID      = 0x23;
public static final short TLVTYPE_PASTHREADID     = 0x24;
public static final short TLVTYPE_PASSESSIONID    = 0x25;
public static final short TLVTYPE_ADAPTERTYPE     = 0x26;

public static final short TLVTYPE_UB_MSG_VERS     = 2001; 
public static final short TLVTYPE_CSNET_VERS      = 2002; 
public static final short TLVTYPE_CSNET_MSG_VERS  = 2003; 
public static final short TLVTYPE_OE_MAJOR_VERS   = 2004; 
public static final short TLVTYPE_OE_MINOR_VERS   = 2005; 
public static final short TLVTYPE_OE_MAINT_VERS   = 2006; 
public static final short TLVTYPE_STREAM_VERS     = 2007; 
public static final short TLVTYPE_CLIENT_TYPE     = 2008; 

public static final short TLVTYPE_APSVCL_VERS     = 3001; 

/* ASK Version  */
/* major version (upper 16 bits) = 0x0001 */
/* minor version (lower 16 bits) = 0x0000 */
public static final int INVALID_ASK_VERSION   = 0x00000000;

public static final int ASK_MAJOR_VER         = 0x0001;
public static final int ASK_MINOR_VER         = 0x0000;
public static final int ASK_VERSION           = 
                                        (ASK_MAJOR_VER << 16) | ASK_MINOR_VER;

/* ASK capabilities bit flags */
public static final int ASK_DISABLED              = 0x00000000;
public static final int ASK_SERVERASK_ENABLED     = 0x00000001;
public static final int ASK_CLIENTASK_ENABLED     = 0x00000002;
public static final int ASK_DEFAULT               = ASK_DISABLED;

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private byte [] ubmsghdr;

private byte [] tlvbuf;     /* v1 only */
private HashMap tlvmap;

private int     buflen;
private byte [] msgbuf;


/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubMsg(short ubver, byte ubtype)
    {
    initUBMsg(ubver, ubtype, DEF_BUFSIZE);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubMsg(short ubver, byte ubtype, int bufsize)
    {
    initUBMsg(ubver, ubtype, bufsize);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this constructor added for efficiency ... it is a bit unsafe since */
/* the constructor does not validate that the header fields contain   */
/* "reasonable" data .. it could, but we're looking for speed here    */

public ubMsg(byte[] ubhdr, byte[] tlvbuffer)
    throws
        InvalidMsgVersionException
    {
    short tlvlen;
 
    /* set the ubhdr */
    ubmsghdr       = ubhdr;

    switch (getubVer()) /* can we call object method in constructor??? */
        {
        case UBMSG_PROTOCOL_V0:
            if (tlvbuffer != null)
                throw new InvalidMsgVersionException(
                           "tlvbuf not valid for ver= (0x" + 
                            Integer.toString(UBMSG_PROTOCOL_V0, 16) + ")" );
            tlvmap = null;
            tlvbuf = null;
            break;

        case UBMSG_PROTOCOL_V1:
        default:
            if (tlvbuffer == null)
            {
                tlvmap = null;
                tlvbuf = null;
                tlvlen = 0;
            }
            else
            {
                tlvlen = (short)tlvbuffer.length;
                tlvbuf = tlvbuffer;
                // defer this until first access to buffer
                // tlvmap = tlvbuf2map(tlvbuffer, 0, tlvlen);
            }

            setubTlvBuflen(tlvlen);
            break;
        }

    buflen         = 0;
    msgbuf         = new byte[DEF_BUFSIZE];
    }

/*********************************************************************/
/* Public Static Methods                                             */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function creates a new byte array in the "wire"   */
/* codepage from a string                                 */

public static byte[] newNetByteArray(String val)
    {
    byte[] tmp = null;

    if (val == null)
        return null;

    if (val.length() == 0)
        return new byte[0];

    try
        {
        tmp = val.getBytes(WIRECODEPAGE);
        }
    catch (UnsupportedEncodingException e)
        {
        tmp = val.getBytes();
        }

    return tmp;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function creates a byte array in TLV format */

/*  2 byte length + string data + null terminator */

public static byte[] newTlvByteArray(String val)
    {
    byte[] tmp = ubMsg.newNetByteArray(val);
    int len = (tmp == null) ? 2 : tmp.length+3;
    byte[] ret = new byte[len];

    setTlvString(ret, 0, tmp);
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function stores a string in TLV format */

/*  2 byte length + string data + null terminator */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int setTlvString(byte[] s, int idx, String val)
    {
    byte[] tmp = ubMsg.newNetByteArray(val);
    return setTlvString(s, idx, tmp);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function stores a byte array in TLV format */

/*  2 byte length + char data + null terminator   */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int setTlvString(byte[] s, int idx, byte val[])
    {
    int len;
    int n;
    int i;

    n = (val == null) ? 0 : (val.length + 1);
    setNetShort(s, idx, (short) n);
    idx += 2;

    if (val != null)
        {
        len = val.length;
        System.arraycopy(val, 0, s, idx, len);
        s[idx+len] = 0;
        idx += n;
        }

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function gets a field in TLV format as a byte arrary */

/*  2 byte length + string data + null terminator          */
/*  note that length includes null terminator              */
/*  note that null terminator IS included in length, value */

public static byte[] getTlvArray(byte[] s, int idx)
    {
    byte[] ret;
    int len;

    len = getNetShort(s, idx);
    idx += 2;

    ret = new byte[len];
    System.arraycopy(s, idx, ret, 0, len);

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function gets a string in TLV format */

/*  2 byte length + string data + null terminator        */
/*  note that length includes null terminator            */
/*  note that value does NOT include null terminator     */

public static String getTlvString(byte[] s, int idx)
    {
    String ret;
    int len;

    len = getNetShort(s, idx);
    idx += 2;
    ret = newNetString(s, idx, len-1);

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*  this function returns the offset after skipping */
/*  over a string in TLV format                     */

/*  2 byte length + string data + null terminator */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int skipTlvString(byte[] s, int idx)
    {
    int len;

    len = getNetShort(s, idx);

    idx += (len + 2);

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static String getTlvDesc(short tlvType)
    {
        final short[] VALS_TLVTYPES =
        {
            TLVTYPE_RQID
          , TLVTYPE_UNCMPRLEN
          , TLVTYPE_CMPRLEVEL
          , TLVTYPE_CMPRMINSZ
          , TLVTYPE_CMPRCAPABLE
          , TLVTYPE_WHATEVER
          , TLVTYPE_CLIENT_CODEPAGE
          , TLVTYPE_CONNECTION_ID
          , TLVTYPE_CLIENT_CONTEXT
          , TLVTYPE_ASKPING_VER
          , TLVTYPE_ASKPING_CAPS
          , TLVTYPE_ASKPING_RQST_ACK
          , TLVTYPE_LG_HEADER
          , TLVTYPE_CLIENT_HOSTNAME
          , TLVTYPE_PROCEDURE_NAME
          , TLVTYPE_CLIENTPRINCIPAL
          , TLVTYPE_NETBUFFERSIZE
          , TLVTYPE_BROKERSESSIONID
          , TLVTYPE_ABLSESSIONID
          , TLVTYPE_SESSIONTYPE
          , TLVTYPE_UB_MSG_VERS
          , TLVTYPE_CSNET_VERS
          , TLVTYPE_CSNET_MSG_VERS
          , TLVTYPE_OE_MAJOR_VERS
          , TLVTYPE_OE_MINOR_VERS
          , TLVTYPE_OE_MAINT_VERS
          , TLVTYPE_STREAM_VERS
          , TLVTYPE_CLIENT_TYPE
          , TLVTYPE_APSVCL_VERS
        };

        final String[] DESC_TLVTYPES =
        {
            "TLVT_RQID"
          , "TLVT_UNCMPRLEN"
          , "TLVT_CMPRLEVEL"
          , "TLVT_CMPRMINSZ"
          , "TLVT_CMPRCAPABLE"
          , "TLVT_WHATEVER"
          , "TLVT_CLIENT_CODEPAGE"
          , "TLVT_CONNECTION_ID"
          , "TLVT_CLIENT_CONTEXT"
          , "TLVT_ASKPING_VER"
          , "TLVT_ASKPING_CAPS"
          , "TLVT_ASKPING_RQST_ACK"
          , "TLVT_LG_HEADER"
          , "TLVT_CLIENT_HOSTNAME"
          , "TLVT_PROCEDURENAME"
          , "TLVT_CLIENTPRINCIPAL"
          , "TLVT_NETBUFFERSIZE"
          , "TLVT_BROKERSESSIONID"
          , "TLVT_ABLSESSIONID"
          , "TLVT_SESSIONTYPE"
          , "TLVT_UB_MSG_VERS"
          , "TLVT_CSNET_VERS"
          , "TLVT_CSNET_MSG_VERS"
          , "TLVT_OE_MAJOR_VERS"
          , "TLVT_OE_MINOR_VERS"
          , "TLVT_OE_MAINT_VERS"
          , "TLVT_STREAM_VERS"
          , "TLVT_CLIENT_TYPE"
          , "TLVT_APSVCL_VERS"
        };

        String ret = null;
        
        for (int i = 0; i < VALS_TLVTYPES.length; i++)
        {
            if (VALS_TLVTYPES[i] == tlvType)
            {
                ret = DESC_TLVTYPES[i];
                break;
            }
        }
        
        if (ret == null)
            ret = "TLVT= " + tlvType;
        
        return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function creates a string from a network byte array */
/* in the "wire" codepage format                            */

public static String newNetString(byte[] s, int idx, int len)
    {
    String ret = null;

    if (len > 0)
        {
        try
            {
            ret = new String(s, idx, len, WIRECODEPAGE);
            }
        catch (UnsupportedEncodingException e)
            {
            ret = new String(s, idx, len);
            }
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static int checkubVer(byte[] hdr)
    throws
        InvalidMsgVersionException
       ,InvalidHeaderLenException
    {
    int ret;

    if (hdr.length < 2)
        throw new InvalidHeaderLenException("len=(" +
                                             hdr.length +
                                             ")");

    ret = getNetShort(hdr, OFST_UBVER);

    if ((ret < MIN_UBVER) || (ret > MAX_UBVER))
        throw new InvalidMsgVersionException(
                       "got ver= (0x" + Integer.toString(ret, 16) + ")" +
                       ": expecting ver=" +
                       " (0x" + Integer.toString(MIN_UBVER, 16) + " - " +
                       "0x" + Integer.toString(MAX_UBVER, 16) + ")" );
    return ret; 
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this routine interrogates a message "header" to see if it is */
/* valid, and then return the ubType field.  This is allow the  */
/* construction of a message of the proper type based on input  */

public static int getubType(byte[] hdr)
    throws
        InvalidMsgVersionException
       ,InvalidHeaderLenException
       ,InvalidServerTypeException
    {
    int ret;

    /* check length and ubVer */


    if (hdr.length != UBHDRLEN)
        throw new InvalidHeaderLenException("len=(" +
                                             hdr.length +
                                             ")");

/*  Why do we do this, and then call checkubVer()? isn't this redundant ?? */
/*
    if (getNetShort(hdr, OFST_UBVER) != CURRENT_UBVER)
        throw new InvalidMsgVersionException("got ver=(" +
                                             getNetShort(hdr, OFST_UBVER) +
                                             ") ... expecting ver=(" +
                                             CURRENT_UBVER +
                                             ")" );

*/
    
    /* check ver */
    checkubVer(hdr);

    ret = hdr[OFST_UBTYPE];

    /* we REALLY need a "lookup" to determine validity */
    /* for now, we'll settle for a range check         */
    if (ret > MAX_UBTYPE)
        {
        throw new InvalidServerTypeException("serverType=(" + ret + ")");
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static short getubTlvBuflen(byte[] hdr)
    throws
        InvalidMsgVersionException
       ,InvalidHeaderLenException
    {
    short ret;
    int ubver;

    ubver = checkubVer(hdr);

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            throw new InvalidMsgVersionException(
                       "tlvbuf not valid for ver= (0x" + 
                        Integer.toString(ubver, 16) + ")" );

        case UBMSG_PROTOCOL_V1:
        default:
            ret = getNetShort(hdr, OFST_UBTLVLEN);
            break;
        }

    return ret; 
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static void setNetShort(byte[] s, int idx, short val)
    {
    int x;
    int i;

    for (i = 1, x = val; i >= 0; i--)
        {
    s[idx+i] = (byte) (x & 0xFF);
        x = (x >> 8);
    }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static void setNetInt(byte[] s, int idx, int val)
    {
    int x;
    int i;

    for (i = 3, x = val; i >= 0; i--)
        {
    s[idx+i] = (byte) (x & 0xFF);
        x = (x >> 8);
    }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static short getNetShort(byte[] s, int idx)
    {
    int ret;
    int i;

    for (i = 0, ret = 0; i < 2; i++)
        {
    ret = (ret << 8) | (s[idx+i] & 0xFF);
    }

    return (short) ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static int getNetUShort(byte[] s, int idx)
    {
    int ret;
    int i;

    for (i = 0, ret = 0; i < 2; i++)
        {
        ret = (ret << 8) | (s[idx+i] & 0xFF);
        }

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static int getNetInt(byte[] s, int idx)
    {
    int ret;
    int i;

    for (i = 0, ret = 0; i < 4; i++)
        {
    ret = (ret << 8) | (s[idx+i] & 0xFF);
    }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static String getubRspDesc(int ubrsp)
    {
    String desc;

    desc = ((ubrsp < 0) || (ubrsp > DESC_UBRSP_EXT.length)) ?
                  new String("error=" + ubrsp)              :
                  new String(DESC_UBRSP_EXT[ubrsp])         ;

    return desc;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean getNeedNewConnID()
    {
      return ((getubRqExt() & UBRQEXT_NEED_NEW_CONNID) > 0);
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static ubMsg newMsg(byte[] msgbuf, int ofst, int len, IAppLogger log)
    throws IOException, MsgFormatException
    {
    MsgInputStream bytestream;
    ubMsg msg;

    bytestream = new MsgInputStream(
                            new ByteArrayInputStream(msgbuf, ofst, len),
                            MSG_INPUT_STREAM_BUFSIZE,
                            SERVERTYPE_APPSERVER, log);

    msg = bytestream.readMsg();
    return msg;
    }

/*********************************************************************/
/* accessor methods                                                  */
/*********************************************************************/

/**********************************************************************/
/* START of UBHDR                                                     */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void set_ubhdr(
            short ubver,
            byte  type,
            byte  src,
            int   rq,
            int   rqExt,
            int   rsp,
            int   rspExt
            )
    {
    /* ub header */
    setNetShort(ubmsghdr, OFST_UBVER, ubver);
    ubmsghdr[OFST_UBTYPE] = type;
    ubmsghdr[OFST_UBSRC] = src;
    setNetInt(ubmsghdr, OFST_UBRQ, rq);
    setNetInt(ubmsghdr, OFST_UBRQEXT, rqExt);
    setNetInt(ubmsghdr, OFST_UBRSP, rsp);
    setNetInt(ubmsghdr, OFST_UBRSPEXT, rspExt);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubVer()
    {
    return getNetShort(ubmsghdr, OFST_UBVER);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getubhdr()
    {
    // this method may be called during serialization
    // we must make sure that the tlvbuflen is up to date
    // prior to returning this reference
    
    if (tlvbuf == null)
    {
        tlvbuf = map2tlvbuf(tlvmap);
        setubTlvBuflen((tlvbuf == null) ? 0 : (short) tlvbuf.length);
    }
    
    return ubmsghdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubhdr(byte[] hdr)
    {
    ubmsghdr = hdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubVer(int version)
    {
    setNetShort(ubmsghdr, OFST_UBVER, (short) version);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubType()
    {
    return ubmsghdr[OFST_UBTYPE];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubType(int type)
    {
    ubmsghdr[OFST_UBTYPE] = (byte) type;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubSrc()
    {
    return ubmsghdr[OFST_UBSRC];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubSrc(int src)
    {
    ubmsghdr[OFST_UBSRC] = (byte) src;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubRq()
    {
    return getNetInt(ubmsghdr, OFST_UBRQ);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubRq(int rq)
    {
    setNetInt(ubmsghdr, OFST_UBRQ, rq);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubRqExt()
    {
    int ret;
    int  ubver = getubVer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            ret = getNetInt(ubmsghdr, OFST_UBRQEXT);
            break;

        case UBMSG_PROTOCOL_V1:
        default:
            ret = getNetShort(ubmsghdr, OFST_UBRQEXT+2);
            break;
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubRqExt(int rqExt)
    {
    int  ubver = getubVer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            setNetInt(ubmsghdr, OFST_UBRQEXT, rqExt);
            break;

        case UBMSG_PROTOCOL_V1:
        default:
            setNetShort(ubmsghdr, OFST_UBRQEXT+2, (short)rqExt);
            break;
        }

    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubRsp()
    {
    return getNetInt(ubmsghdr, OFST_UBRSP);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubRsp(int rsp)
    {
    setNetInt(ubmsghdr, OFST_UBRSP, rsp);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getubRspExt()
    {
    return getNetInt(ubmsghdr, OFST_UBRSPEXT);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubRspExt(int rspExt)
    {
    setNetInt(ubmsghdr, OFST_UBRSPEXT, rspExt);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getubTlvBuf()
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    int  ubver;
    byte[] ret = null;

    ubver = getubVer();  // TEST TEST TEST
//  ubver = validateTlvBuffer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            throw new InvalidMsgVersionException(
                       "tlvbuf not valid for ver= (0x" + 
                        Integer.toString(ubver, 16) + ")" );

        case UBMSG_PROTOCOL_V1:
        default:
            if (tlvbuf == null)
            {
                tlvbuf = map2tlvbuf(tlvmap);
                setubTlvBuflen((tlvbuf == null) ? 0 : (short) tlvbuf.length);
            }
            ret = tlvbuf;
            break;
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubTlvBuf(byte[] tlvbuf)
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    int  ubver;

    ubver = getubVer();           // TEST TEST TEST
//  ubver = validateTlvBuffer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            throw new InvalidMsgVersionException(
                       "tlvbuf not valid for ver= (0x" + 
                        Integer.toString(ubver, 16) + ")" );

        case UBMSG_PROTOCOL_V1:
        default:
            this.tlvbuf = tlvbuf;
            setubTlvBuflen((short)tlvbuf.length);
            
//          defer loading the map until needed
//          tlvmap = (tlvbuf == null) ? null :
//                    tlvbuf2map(tlvbuf, 0, tlvbuf.length);
            tlvmap = null;
            
            break;
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void resetubTlvBuf()
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    setubTlvBuf(new byte[0]);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public short getubTlvBuflen()
    throws 
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    int  ubver;
    short  ret = 0;

    ubver = getubVer();           // TEST TEST TEST
//  ubver = validateTlvBuffer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            throw new InvalidMsgVersionException(
                       "tlvbuf not valid for ver= (0x" + 
                        Integer.toString(ubver, 16) + ")" );

        case UBMSG_PROTOCOL_V1:
        default:
            ret = getNetShort(ubmsghdr, OFST_UBTLVLEN);
            if (ret == 0)
            {
                if (tlvmap != null)
                {
                    tlvbuf = map2tlvbuf(tlvmap);
                    setubTlvBuflen((tlvbuf == null) ? 0 : (short) tlvbuf.length);
                    ret = getNetShort(ubmsghdr, OFST_UBTLVLEN);
                }
                
            }
            break;
        }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int validateTlvBuffer()
    throws InvalidTlvBufferException
    {
    int  ubver;
    short  tlvbuflen;

    ubver = getubVer();

    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            if (tlvbuf != null)
                throw new InvalidTlvBufferException(
                           "tlvbuf is not null ... length=" + tlvbuf.length );
            break;

        case UBMSG_PROTOCOL_V1:
        default:
            if (tlvbuf == null)
            {
                tlvbuf = map2tlvbuf(tlvmap);
                setubTlvBuflen((tlvbuf == null) ? 0 : (short) tlvbuf.length);
            }
            
            tlvbuflen = getNetShort(ubmsghdr, OFST_UBTLVLEN);

            if ((tlvbuf == null) && (tlvbuflen != 0))
            {
                throw new InvalidTlvBufferException(
                           "tlvbuf is null and tlvbuflen = (" + tlvbuf.length + ")");
            }
            
            if ((tlvbuf != null) && (tlvbuf.length != tlvbuflen))
            {
                throw new InvalidTlvBufferException(
                           "tlvbuf.length(" + tlvbuf.length + ")" +
                           " != tlvbuflen(" + tlvbuflen + ")" );
            }
            break;
        }

    return ubver;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int extractRqInfo(ubRqInfo rqInfo)
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    byte[] tlvbuf     = getubTlvBuf();
    short  tlvbuflen  = getubTlvBuflen();

    int    nFound;
    int    idx;
    int    type;

    for (nFound = 0,idx = 0; idx < tlvbuflen; idx = skipTlvString(tlvbuf, idx))
        {
        /* get the type ... reserve the upper 4 bits for the future */
        type = getNetShort(tlvbuf, idx) & 0xFFFFFF;

        /* point to start of value */
        idx += 2;

        /* if we've got a field we care about, put it into the rqInfo object */
        if (ubRqInfo.isDefined(type))
            {
            rqInfo.setField(new Integer(type), getTlvArray(tlvbuf, idx));
            nFound++;
            }
        }

    return nFound;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int augmentTlvInfo(ubRqInfo rqInfo)
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    /* Iterator<Integer> itr; */
    Iterator itr;
    int    nFound;
    Integer typ;
    String fld;

	// if rqInfo is Null then nothing is filled there to augment TLV buffer.
	// so return.
	if (rqInfo == null) return 0;

    // add any fields from rqInfo that are NOT already in the buffer
    for (nFound = 0, itr = rqInfo.keys(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer) itr.next();
        if (!tlvmap_contains(typ))
            {
            fld = rqInfo.getStringField(typ);
            tlvmap_put(typ, fld);
            nFound++;
            }
        }

    return nFound;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

// FIX FIX FIX ... this method does not throw these exceptions
// however, removing this declaration would require that all
// callers be modified.  leave this for now as there is no actual
// penalty 
public String getTlvField(short tlv_type)
    throws
        TlvFieldNotFoundException
       ,InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    return tlvmap_get(tlv_type);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getTlvField_NoThrow(short tlv_type)
    {
    return tlvmap_get(tlv_type);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void appendTlvField(short tlv_type, String val)
    throws
        TlvFieldAlreadyExistsException
       ,InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    if (tlvmap_contains(tlv_type))
        {
        /* if we get this far, then the field already exists, so */
        /* we want to throw an exception                         */
        throw new TlvFieldAlreadyExistsException(getTlvDesc(tlv_type));
        }

    /* the field doesn't already exist, so we can safely add it */
    tlvmap_put(tlv_type, val);
    
    // return  getubTlvBuflen();  TEST TEST TEST
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void updateTlvField(short tlv_type, String val)
    throws
        InvalidMsgVersionException
       ,InvalidTlvBufferException
    {
    // update the field even if it already exists
    tlvmap_put(tlv_type, val);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void appendToTlvField(short tlv_type, String val)
    {
    String fld = tlvmap_get(tlv_type);

    fld = (fld == null) ? val : fld + val;

    tlvmap_put(tlv_type, fld);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean testubRspExtBits(int rspExtBits)
    {
    return ((getubRspExt() & rspExtBits) > 0);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setubRspExtBits(int rspExtBits)
    {
    int rspExt = getubRspExt();
    setubRspExt(rspExt | rspExtBits);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean cmpMsg(ubMsg msg)
    {
    boolean ret;
    int i, len;

    ret = (
        ( getubVer()         == msg.getubVer()      ) &&
        ( getubSrc()         == msg.getubSrc()      ) &&
        ( getubRq()          == msg.getubRq()       ) &&
        ( getubRqExt()       == msg.getubRqExt()    ) &&
        ( getubRsp()         == msg.getubRsp()      ) &&
        ( getubRspExt()      == msg.getubRspExt()   ) &&
        ( getBuflen()        == msg.getBuflen()     )
       );

    if (ret)
    {
        len = getBuflen();
        for (i = 0; ret && i < len; i++)
        ret = msgbuf[i] == msg.msgbuf[i];
    }

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void print(String title, int lvl, int indexEntryType, IAppLogger log)
    {
    int ubver;
    short tlvbuflen = 0;

    ubver = getubVer();

    log.logWithThisLevel(lvl,indexEntryType, title);

    log.logWithThisLevel(lvl,indexEntryType,
                         " version= " + ubver);
    log.logWithThisLevel(lvl,indexEntryType,
                         " type= " + getubType() + " " + DESC_UBTYPE[getubType()]);
    log.logWithThisLevel(lvl,indexEntryType,
                         " src= " + getubSrc() + " " + DESC_UBSRC[getubSrc()]);
    log.logWithThisLevel(lvl,indexEntryType,
                         " rq= 0x" + Integer.toString(getubRq(),16) +
                         " " + DESC_UBRQ[getubRq()]);

    try
        {
        tlvbuflen = getubTlvBuflen();
        log.logWithThisLevel(lvl,indexEntryType,
                             " tlvBuflen= 0x" + Integer.toString(tlvbuflen,16));
        }
    catch (MsgFormatException e)
        {
        /* ignore this */
        }

    log.logWithThisLevel(lvl,indexEntryType,
                         " rqExt= 0x" + Integer.toString(getubRqExt(),16));

    log.logWithThisLevel(lvl,indexEntryType,
                         " rsp= 0x" + Integer.toString(getubRsp(),16) +
                         " " + DESC_UBRSP[getubRsp()]);
    log.logWithThisLevel(lvl,indexEntryType,
                         " rspExt= 0x" + Integer.toString(getubRspExt(),16));


    if (tlvbuflen > 0)
        {
        log.logDump(lvl, indexEntryType,
                    " tlvbuf[" + tlvbuflen + "]= ", tlvbuf, tlvbuflen);
        printTlvFields(log, lvl, indexEntryType);
        }

    printSrvHeader(lvl, indexEntryType, log);

    int len = getBuflen();

    log.logWithThisLevel(lvl,indexEntryType,
                         " buflen= " + len);

    if (len > 0)
        log.logDump(lvl, indexEntryType,
                     " msgbuf[" + len + "]= ", msgbuf, len);
    }

public void printTlvFields(IAppLogger log, int lvl, int indexEntryType)
    {
    Integer typ;
    String  val;
    int    leng;

    if (tlvmap == null)
    {
        if (tlvbuf == null)
        {
            log.logWithThisLevel(lvl,indexEntryType,
                                 "tlv buffer is null");
            return;
        }
        else tlvmap = tlvbuf2map(tlvbuf, 0, tlvbuf.length);
    }
    
    for (Iterator itr = tlvmap.keySet().iterator(); itr.hasNext(); )
        {
        typ = (Integer)itr.next();
        val = tlvmap_get(typ);
        
        leng = (val == null) ? 0 : val.length();
        
        if (leng == 0)
            log.logWithThisLevel(lvl,indexEntryType,
                                 " type= " + typ + "  length= " + leng + "  val= null");
        else
            {
            log.logWithThisLevel(lvl,indexEntryType,
                                 " type= " + typ + "  length= " + leng + "  val= " + val);
            }
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getBuflen()
    {
    return buflen;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setBuflen(int len)
    {
    buflen = len;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getMsgbuf()
    {
    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] setMsgbuf(byte[] msg)
    {
    msgbuf = msg;
    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] setMsgbuf(String msgString)
    {
    msgbuf = msgString.getBytes();
    setBuflen(msgbuf.length);
    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] setMsgbuf(byte[] msg, int len)
    {
    msgbuf = msg;
    setBuflen(len);
    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] appendMsgbuf(byte[] msg, int len)
    {
    int curbuflen = getBuflen();

    if ((curbuflen + len) > getMsgbuf().length)
        {
        /* must create a new buffer big enough for the two parts */

        byte[] newbuf = new byte[curbuflen + len];

        /* copy the current contents to new buffer */
        System.arraycopy(msgbuf, 0, newbuf, 0, curbuflen);

        /* copy the new contents at the end */
        System.arraycopy(msg, 0, newbuf, curbuflen, len);

        /* hook the new array onto this object */
        setMsgbuf(newbuf, curbuflen + len);
        }
    else
        {
        /* there's room enough in the current buffer ... just copy in */
        /* the data and increment the buffer length                   */

        /* copy the new contents at the end */
        System.arraycopy(msg, 0, getMsgbuf(), curbuflen, len);

        setBuflen(curbuflen+len);
        }

    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] appendMsgbuf(int msg)
    {
    int curbuflen = getBuflen();
    int len = 4;

    if ((curbuflen + len) > getMsgbuf().length)
        {
        /* must create a new buffer big enough for the two parts */

        byte[] newbuf = new byte[curbuflen + len];

        /* copy the current contents to new buffer */
        System.arraycopy(msgbuf, 0, newbuf, 0, curbuflen);

        /* copy the new contents at the end */
        setNetInt(newbuf, curbuflen, msg);

        /* hook the new array onto this object */
        setMsgbuf(newbuf, curbuflen + len);
        }
    else
        {
        /* there's room enough in the current buffer ... just copy in */
        /* the data and increment the buffer length                   */

        /* copy the new contents at the end */
        setNetInt(msgbuf, curbuflen, msg);

        setBuflen(curbuflen+len);
        }

    return msgbuf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getubTypeDesc()
    {
    int msgType = getubType();
    String desc;

    try
        {
        desc = DESC_UBTYPE[msgType];
        }
    catch (Exception e)
        {
        desc = "[TYPE= 0x" + Integer.toString(msgType,16) + "]";
        }

    return desc;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getubRqDesc()
    {
    int rq = getNetInt(ubmsghdr, OFST_UBRQ);
    String desc;

    try
        {
        desc = DESC_UBRQ[rq];
        }
    catch (Exception e)
        {
        desc = "UBRQ= 0x" + Integer.toString(rq,16);
        }

    return desc;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getSerializedLength()
    {
    int srvhdrlen;
    int tlvlen;
    int msglen;

    try
    {
        tlvlen = getubTlvBuflen();
    }
    catch (MsgFormatException e)
    {
        tlvlen = 0;
    }

    srvhdrlen = getSrvHeaderlen();
    msglen = UBHDRLEN + tlvlen + srvhdrlen + buflen;

    return msglen;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] serializeMsg()
    {
    int srvhdrlen;
    int tlvlen;
    int msglen;
    byte[] ret;
    byte[] tlv_buf;

    try
        {
        tlv_buf = getubTlvBuf();
        tlvlen = getubTlvBuflen();
        }
    catch (MsgFormatException e)
        {
        tlv_buf = null;
        tlvlen = 0;
        }

    srvhdrlen = getSrvHeaderlen();
    msglen = UBHDRLEN + tlvlen + srvhdrlen + buflen;
    ret = new byte[msglen];

    /* insert the ubheader */
    System.arraycopy(ubmsghdr, 0, ret, 0, UBHDRLEN);
    
    /* insert the tlvbuffer */
    if (tlv_buf != null)
        System.arraycopy(tlv_buf, 0, ret, UBHDRLEN, tlvlen);
    
    /* insert the server header */
    System.arraycopy(getSrvHeader(), 0, ret, UBHDRLEN+tlvlen, srvhdrlen);
    
    /* insert the msgbuf */
    System.arraycopy(msgbuf, 0, ret, UBHDRLEN+tlvlen+srvhdrlen, buflen);
    
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public ByteBuffer wrapMsg(ByteBuffer buf)
    {
    int tlvlen;

    try
        {
        tlvlen = getubTlvBuflen();
        }
    catch (MsgFormatException e)
        {
        tlvlen = 0;
        }

    /* insert the ubheader */
    buf.put(ubmsghdr, 0, UBHDRLEN);
    
    /* insert the tlvbuffer */
    if (tlvbuf != null)
        buf.put(tlvbuf, 0, tlvlen);
    
    /* insert the server header */
    buf.put(getSrvHeader(), 0, getSrvHeaderlen());
    
    /* insert the msgbuf */
    buf.put(msgbuf, 0, buflen);
    
    return buf;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String toString()
    {
    String ret;
    String ubMsgType  = getubTypeDesc();
    String ubMsgDesc  = getubRqDesc();
    String srvMsgDesc = getSrvMsgDesc();
    int    msglen     = getMsglen();

    ret = // ubMsgType   + "[" + 
          ubMsgDesc   + " " + 
          srvMsgDesc  + " datalen= " + 
          msglen + "]" 
          // + super.toString() 
    ;

    return ret;
    }

/**********************************************************************/
/* Abstract methods                                                   */
/**********************************************************************/

public abstract byte[] getSrvHeader();
public abstract int getSrvHeaderlen();
public abstract void printSrvHeader();
public abstract void printSrvHeader(int lvl, int indexEntryType, IAppLogger lg);

public abstract int getMsglen();
public abstract String getSrvMsgDesc();

/*********************************************************************/
/* internal methods                                                  */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void initUBMsg(
             short ubver
            ,byte  type
            ,int   bufsize
            )
    {
    /* ub header */
    ubmsghdr = new byte[UBHDRLEN];
    setNetShort(ubmsghdr, OFST_UBVER, ubver);
    ubmsghdr[OFST_UBTYPE] = type;

    /* tlv buffer */
    switch (ubver)
        {
        case UBMSG_PROTOCOL_V0:
            tlvbuf = null;
            tlvmap = null;
            break;

        case UBMSG_PROTOCOL_V1:
        default:
            tlvmap = new HashMap();
            tlvbuf = new byte[0];
            setubTlvBuflen((short)tlvbuf.length);
            break;
        }

    /* payload */
    buflen    = 0;
    msgbuf    = new byte[bufsize];
    }
 
/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void setubTlvBuflen(short len)
    {
    setNetShort(ubmsghdr, OFST_UBTLVLEN, len );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getTlvField_NoThrow(byte[] tlvbuf, short tlvbuflen, short tlv_type)
{
    String ret;
    
    ret = tlvmap_get(tlv_type);
            
    return ret;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String remTlvField_NoThrow(short tlv_type)
{
    String ret;
    int type;

    ret = (String) tlvmap_get(tlv_type);
    tlvmap_remove(tlv_type);    
    this.tlvbuf = null;
    
    return ret;
}

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

// convert the tlvmap to a serialized byte array

/* this routine assumes that the buffer provided is big enough */
/* caller needs to calculated the space requirements!!!        */
private synchronized byte[] map2tlvbuf(HashMap map)
    {
    byte[] tlvbuf = null;
    String val;
    int len;
    int idx;
    int fldlen;
    Integer typ;

    if (tlvmap == null)
        return null;
    
    len = serializedLength(map);
    tlvbuf = new byte[len];
    
    idx = 0;
    
    /* for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) */
    for (Iterator itr = map.keySet().iterator(); itr.hasNext(); )
        {
        /* typ = itr.next(); */
        typ = (Integer)itr.next();
        val = (String)map.get(typ);
        fldlen = (val == null) ? 0 : val.length() + 1;

        /* store the field type */
        ubMsg.setNetShort(tlvbuf, idx, (short)typ.intValue());
        idx += 2;

        /* store the field length */
        ubMsg.setNetShort(tlvbuf, idx, (short)fldlen);
        idx += 2;

        /* store the length and value with NULL terminator */
        if (val != null)
            {
            byte[] vb = newNetByteArray(val);
            int    vl = vb.length;
            
            System.arraycopy(vb, 0, tlvbuf, idx, vl);
            tlvbuf[idx+vl] = '\0';
            }
        
        idx += fldlen;
        }

    return tlvbuf;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* private int serializedLength(Set<Integer> keys) */
private int serializedLength(HashMap map)
    {
    int i;
    int buflen;
    int fldlen;
    String val;
    Integer typ;

    if (map == null)
        return 0;
    
    buflen = 0;
    for (Iterator itr = map.keySet().iterator(); itr.hasNext(); )
        {
        typ = (Integer)itr.next();
        val = (String)map.get(typ);
        fldlen = (val == null) ? 0 : val.length() + 1;
        buflen += (2 +       // type
                   2 +       // length
                   fldlen);  // value
        }

    return buflen;
    }

// convert a serialized tlvbuf to a map

private static HashMap tlvbuf2map(byte[] tlvbuf, int idx, int tlvbuflen)
{
    HashMap ret = new HashMap();
    int type;
    String val;
    
    for (idx = 0; idx < tlvbuflen; idx = skipTlvString(tlvbuf, idx))
    {
        /* get the type ... reserve the upper 4 bits for the future */
        type = getNetShort(tlvbuf, idx) & 0xFFFF;

        /* point to start of value */
        idx += 2;

        val = getTlvString(tlvbuf, idx);
        
        ret.put(type, val);
    }
    
    return ret;
}

private void tlvmap_put(int tlvType, String val)
{
    if (tlvmap == null)
    {
        tlvmap = tlvbuf2map(tlvbuf, 0, tlvbuf.length);
    }
    
    tlvmap.put(tlvType, val);
    tlvbuf = null;
    setubTlvBuflen((short)0);  // TEST TEST TEST
}

private String tlvmap_remove(int tlvType)
{
    String ret;
    
    if (tlvmap == null)
    {
        tlvmap = tlvbuf2map(tlvbuf, 0, tlvbuf.length);
    }
    
    ret = (String)tlvmap.remove(tlvType);
    
    return ret;
}

private String tlvmap_get(int tlvType)
{
    String ret;
    
    if (tlvmap == null)
    {
        tlvmap = tlvbuf2map(tlvbuf, 0, tlvbuf.length);
    }
    
    ret = (String)tlvmap.get(tlvType);
    
    return ret;
}

private boolean tlvmap_contains(int tlvType)
{
    boolean ret;
    
    if (tlvmap == null)
    {
        tlvmap = tlvbuf2map(tlvbuf, 0, tlvbuf.length);
    }
    
    ret = tlvmap.containsKey(tlvType);
    
    return ret;
}

}  /* end of ubMsg */

