
/*************************************************************/
/* Copyright (c) 1984-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubAppServerMsg                                           */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import com.progress.common.ehnlog.IAppLogger;

/*********************************************************************/
/*                                                                   */
/* Class ubAppServerMsg                                              */
/*                                                                   */
/*********************************************************************/

public class ubAppServerMsg
    extends ubMsg
    implements ubConstants
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*
   AppServer header format

    short csnetver     csnet version of message
    int   seqnum       csnet sequence number
    short msg_len      length of follow on message
    short cssmsgver    cssmssg version of message
    byte  handle       who sent this message
    byte  msg_code     what type of message 
*/

private static final int CSHDRLEN            = 12;
public  static final int CSMSSGHDRLEN        = 4;

private static final int OFST_CSNETVER       = 0;
private static final int OFST_SEQNUM         = 2;
private static final int OFST_MSGLEN         = 6;
private static final int OFST_CSMSSGVER      = 8;
private static final int OFST_HANDLE         = 10;
private static final int OFST_MSGCODE        = 11;

/* this MUST match constant in csnet.c */

private static final short CUR_CSNET_VER            = 0x66; /* 102 */
private static final int   DEF_SEQNUM               = 0x00;
private static final short DEF_MSGLEN               = 0x00;

/* this must match constant in css_scaffold.c */
/* should be kept same as CSMSSG_CUR_PROTOCOL_VERSION */
private static final short CUR_CSMSSG_VER           = 0x66; /* 102 */
private static final byte  DEF_HANDLE               = 0x00;


public static final int CSMSSG_CONNECT              = 10;
public static final int CSMSSG_CONNECT_ACK          = 11;
public static final int CSMSSG_DISCONN              = 20;
public static final int CSMSSG_DISCONN_ACK          = 21;
public static final int CSMSSG_SHUTDOWN             = 30;
public static final int CSMSSG_SHUTDOWN_ACK         = 31;
public static final int CSMSSG_STOP                 = 40;
public static final int CSMSSG_OPEN4GL              = 70;
public static final int CSMSSG_OPEN4GL_ACK          = 71;
public static final int CSMSSG_CLIENT_CONNECT       = 80;
public static final int CSMSSG_CLIENT_DISCONNECT    = 81;
public static final int CSMSSG_PROCSTATS            = 82;
public static final int CSMSSG_ADMIN                = 90;
public static final int CSMSSG_ADMIN_ACK            = 91;

public static final byte   DEF_MSGCODE              = CSMSSG_OPEN4GL;

public static final int[] CSMSSG_MSGCODES =
  {
  CSMSSG_CONNECT
, CSMSSG_CONNECT_ACK
, CSMSSG_OPEN4GL
, CSMSSG_OPEN4GL_ACK
, CSMSSG_DISCONN
, CSMSSG_DISCONN_ACK
, CSMSSG_SHUTDOWN
, CSMSSG_SHUTDOWN_ACK
, CSMSSG_STOP
, CSMSSG_CLIENT_CONNECT
, CSMSSG_CLIENT_DISCONNECT
, CSMSSG_PROCSTATS
, CSMSSG_ADMIN
, CSMSSG_ADMIN_ACK
 };

public static final String[] DESC_MSGCODE =
  {
  "CSMSSG_CONNECT"
, "CSMSSG_CONNECT_ACK"
, "CSMSSG_OPEN4GL"
, "CSMSSG_OPEN4GL_ACK"
, "CSMSSG_DISCONN"
, "CSMSSG_DISCONN_ACK"
, "CSMSSG_SHUTDOWN"
, "CSMSSG_SHUTDOWN_ACK"
, "CSMSSG_STOP"
, "CSMSSG_CLIENT_CONNECT"
, "CSMSSG_CLIENT_DISCONNECT"
, "CSMSSG_PROCSTATS"
, "CSMSSG_ADMIN"
, "CSMSSG_ADMIN_ACK"
, null
 };

/*                                                                   */
/* Additional fields in the DATA portion of the message buffer       */
/* are accessible through the following constants.                   */
/*                                                                   */
/* ofst       size   field              Desc                         */
/* ----       ----   -----              ----                         */
/*    0        1      cond_code         4GL condition code           */
/*    1        2      error_code        4GL error code               */
/*    3        2      err_msg           error msg len  ( X )         */
/*    5        X-1    error message     error msg text               */
/*  5+X-1      1      null terminator   msg terminator (incl in X)   */
/*  5+X        1      reconnect flag    conn_ack flags               */
/*  6+X        4      service ID                                     */
/* 10+X        4      log ID                                         */
/* 14+X        2      connID length     ( Y )                        */
/* 16+X        Y-1    connID            Connection ID                */
/* 16+X+Y-1    1      null terminator   (incl in Y)                  */
/* 17+X+Y      2      connCntxt length  ( Z )                        */
/* 19+X+Y      Z-1    connCntxt         Connection Context text      */
/* 20+X+Y+Z-1  1      null terminator   (incl in Z)                  */
/*                                                                   */
/* WARNING:  The accessor methods for these fields must only be      */
/*           called when the caller is SURE that the fields exist    */
/*           in the msgbuf.  No checking is provided.                */
/*                                                                   */

private static final int OFST_4GL_COND_CODE      = 0;
private static final int OFST_4GL_ERROR_CODE     = 1;
private static final int OFST_4GL_ERR_MSG        = 3;

public static final byte CS_COND_NONE            = 0;
public static final byte CS_COND_ERROR           = 1;
public static final byte CS_COND_STOP            = 2;
public static final byte CS_COND_QUIT            = 3;
public static final byte CS_COND_DEBUG           = 4;

public static final byte CS_CONN_OKTODEBUG       = 2;

public static final byte CSO_STREAM_VERSION       = 0x52;
public static final byte CSO_STREAM_TAG_TABLE     = 0x01;
public static final byte CSO_STREAM_TAG_RECORD    = 0x02;
public static final byte CSO_STREAM_TAG_UNKNOWN   = 0x03;
public static final byte CSO_STREAM_TAG_TODAY     = 0x04;
public static final byte CSO_STREAM_TAG_2BYTELEN  = 0x05;
public static final byte CSO_STREAM_TAG_0FLD      = 0x06;
public static final byte CSO_STREAM_TAG_ERROR     = 0x07;
public static final byte CSO_STREAM_TAG_NORMAL    = 0x08;

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private byte [] csmsghdr;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAppServerMsg(short ubver)
    {
    super(ubver, ubMsg.UBTYPE_APPSERVER);
    initCSMsg(
	    CUR_CSNET_VER,
		DEF_SEQNUM,
		DEF_MSGLEN,
		CUR_CSMSSG_VER,
		DEF_HANDLE,
		DEF_MSGCODE
		     );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAppServerMsg(short ubver, int bufsize)
    {
    super(ubver, ubMsg.UBTYPE_APPSERVER, bufsize);
    initCSMsg(
	    CUR_CSNET_VER,
		DEF_SEQNUM,
		DEF_MSGLEN,
		CUR_CSMSSG_VER,
		DEF_HANDLE,
		DEF_MSGCODE
			);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ubAppServerMsg(short ubver,
                      int msgcode,
                      int seqnum,
                      int msglen,
                      int bufsize)
    {
    super(ubver, ubMsg.UBTYPE_APPSERVER, bufsize);
    initCSMsg(
	    CUR_CSNET_VER,
		seqnum,
		DEF_MSGLEN,
		CUR_CSMSSG_VER,
		DEF_HANDLE,
		DEF_MSGCODE
                 );

    setCsHeaders(seqnum, msglen, msgcode);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this constructor added for efficiency ... it is a bit unsafe since */
/* the constructor does not validate that the header fields contain   */
/* "reasonable" data .. it could, but we're looking for speed here    */

public ubAppServerMsg(byte[] ubhdr, byte[] tlvbuf, byte[] cshdr)
    throws
        ubMsg.InvalidMsgVersionException
    {
    super(ubhdr, tlvbuf);
    csmsghdr = cshdr;
    }

/*********************************************************************/
/* Public Static Methods                                             */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function creates a byte array in "AppServer" format */

/*  2 byte length + string data + null terminator */

public static byte[] newNetByteArray(String val)
    {
    byte[] tmp = ubMsg.newNetByteArray(val);
    int len = (tmp == null) ? 2 : tmp.length+3;
    byte[] ret = new byte[len];

    setNetString(ret, 0, tmp);
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function stores a string in "AppServer" format */

/*  2 byte length + string data + null terminator */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int setNetString(byte[] s, int idx, String val)
    {
    byte[] tmp = ubMsg.newNetByteArray(val);
    return setNetString(s, idx, tmp);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function stores a byte array in "AppServer" format */

/*  2 byte length + char data + null terminator   */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int setNetString(byte[] s, int idx, byte val[])
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

/* this function gets a string in "AppServer" format   */
/* assuming that the string contains a null terminator */
/* which we will truncate.                             */

public static String getNetString(byte[] s, int idx)
    {
    return getNetString(s, idx, true);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this function gets a string in "AppServer" format */

/*  2 byte length + string data                             */
/*  note that the string may contain a null terminator that */
/*  we wish to truncate.  The caller will tell us.          */

public static String getNetString(byte[] s, int idx, boolean fTrimNull)
    {
    String ret;
    int len;

    len = getNetShort(s, idx);
    idx += 2;
    
    if (fTrimNull)
        len--;
    
    ret = newNetString(s, idx, len);

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*  this function returns the offset after skipping */
/*  over a string in "AppServer" format             */

/*  2 byte length + string data + null terminator */
/*  note that length includes null terminator     */
/*  the function returns the index of the byte    */
/*  after the null terminator                     */

public static int skipNetString(byte[] s, int idx)
    {
    int len;

    len = getNetShort(s, idx);

    idx += (len + 2);

    return idx;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static byte[] csmssgRspbuf(int cond, int err, String s)
    {
    /* generate a csmssg data stream */
    /* this consists of the following */
    /*
       ofst   len     val
       ----   ---     ---
         0      1     condition code
         1      2     error code
         3      2     error msg string length (incl null terminator)
         5      n     n-byte error msg
       5+n      1     null terminator
    */

    int n;
    byte[] tmp;

if (false)    /* BUG 20000720-010 */
{
    n = (s == null) ? 0 : s.length() + 1;
}
else          /* BUG 20000720-010 */
{
    n = (s == null) ? 0 : s.getBytes().length + 1;

}             /* BUG 20000720-010 */

    tmp = new byte[n + 5];

    tmp[0] = (byte) cond;

    ////////   ubMsg.setNetShort(tmp, 0, (short)err); ///// WRONG!!!!
    ubMsg.setNetShort(tmp, 1, (short)err);

    ubMsg.setNetShort(tmp, 3, (short) n );
    if (s != null)
        {
        System.arraycopy(s.getBytes(), 0, tmp, 5, s.getBytes().length);
        tmp[tmp.length-1] = 0; /* add null terminator */
        }

    return tmp;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public static byte[] csmssgErrorRspbuf(byte cond, String msgstr, String retstr)
    {
    /* generate a csmssg ERROR data stream */
    /* this consists of the following */
    /*
       ofst   len     val
       ----   ---     ---
         0      1     STREAM_VERSION          0x52
         1      2     sizeof response         19 + n + m
         3      1     STREAM_TAG_ERROR        0x07
         4      1     STREAM_TAG_TABLE        0x01
         5      2     not used                0x00 0x00
         7      1     STREAM_TAG_RECORD       0x02
         8      4     condition code          0x05 0x00 0x02 cond (1 byte)
        12      1     msgid (zero value)      0x06
        13      1     flags (zero value)      0x06
        14      4     condition code          0x05 0x00 0x01 stepcount (1 bytes)
        18      n     msgstr                  0x05 nn nn <string>
        21+n    m     retstr                  0x05 mm mm <string>
        24+n+m  1     STREAM_TAG_TABLE        0x01
        25+n+m  2     not used                0x00 0x00

        N.B.  This must be kept in sync with error response
              processing in csopRqstRequest() method in csoReqst.c        
        
    */

    int n;
    int m;
    byte[] tmp;

    n = (msgstr == null) ? 0 : msgstr.getBytes().length;
    m = (retstr == null) ? 0 : retstr.getBytes().length;

    tmp = new byte[27 + n + m];


    tmp[0] = CSO_STREAM_VERSION;                     // STREAM_VERSION
    ubMsg.setNetShort(tmp, 1, (short)(24 + n + m));  // table length
    tmp[3] = CSO_STREAM_TAG_ERROR;                   // STREAM_TAG_ERROR
    
    tmp[4] = CSO_STREAM_TAG_TABLE;                   // STREAM_TAG_TABLE
    ubMsg.setNetShort(tmp, 5, (short)0);             // unused
    
    tmp[7] = CSO_STREAM_TAG_RECORD;                  // STREAM_TAG_RECORD
    
    tmp[8] = CSO_STREAM_TAG_2BYTELEN;                // STREAM_TAG_2BYTELEN
    ubMsg.setNetShort(tmp, 9, (short)1);             // condition length
    tmp[11] = cond;                                  // condition code
    
    tmp[12] = CSO_STREAM_TAG_0FLD;  // msgid         // STREAM_TAG_0FLD
    tmp[13] = CSO_STREAM_TAG_0FLD;  // flags         // STREAM_TAG_0FLD
    
    tmp[14] = CSO_STREAM_TAG_2BYTELEN;               // STREAM_TAG_2BYTELEN
    ubMsg.setNetShort(tmp, 15, (short)1);            // stepcount length (1)
    tmp[17] = (byte) 0x01;                           // stepcount value (1)
    
    tmp[18] = CSO_STREAM_TAG_2BYTELEN;               // STREAM_TAG_2BYTELEN
    ubMsg.setNetShort(tmp, 19, (short) n);           // msgstr length
    if (n > 0)
        System.arraycopy(msgstr.getBytes(), 0, tmp, 21, n);
    
    tmp[21+n] = CSO_STREAM_TAG_2BYTELEN;             // STREAM_TAG_2BYTELEN
    ubMsg.setNetShort(tmp, 22+n, (short) m);         // retstr length
    if (m > 0)
        System.arraycopy(msgstr.getBytes(), 0, tmp, 24+n, m);

    tmp[24+m+n] = CSO_STREAM_TAG_TABLE;              // STREAM_TAG_TABLE
    ubMsg.setNetShort(tmp, 25+n+m, (short)0);        // unused
    
    return tmp;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public static int getSrvHdrlen()
    {
    return CSHDRLEN;
    }

/*********************************************************************/
/* accessor methods                                                  */
/*********************************************************************/

/**********************************************************************/
/* START of CSNET/CSMSSG headers                                      */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getcshdr()
    {
    return csmsghdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setcshdr(byte[] hdr)
    {
    csmsghdr = hdr;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setCsHeaders(int seqnum,
                         int msg_len,
                         int msg_code)
    {
    /* set the csnet header fields */
    setCsnetVer(CUR_CSNET_VER);
    setSeqnum(seqnum);

    /* adjust the csnet header length to include csmssg header */
    msg_len += CSMSSGHDRLEN;

    setMsglen(msg_len);

    /* set the csmssg header fields */
    setCsmssgVer(CUR_CSMSSG_VER);
    setHandle(DEF_HANDLE);
    setMsgcode((byte)msg_code);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public short getCsnetVer()
    {
    return getNetShort(csmsghdr, OFST_CSNETVER);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setCsnetVer(short ver)
    {
    setNetShort(csmsghdr, OFST_CSNETVER, ver);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getSeqnum()
    {
    return getNetInt(csmsghdr, OFST_SEQNUM);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setSeqnum(int seqnum)
    {
    setNetInt(csmsghdr, OFST_SEQNUM, seqnum);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getMsglen()
    {
    return getNetUShort(csmsghdr, OFST_MSGLEN);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setMsglen(int msglen)
    {
    setNetShort(csmsghdr, OFST_MSGLEN, (short) msglen);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public short getCsmssgVer()
    {
    return getNetShort(csmsghdr, OFST_CSMSSGVER);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setCsmssgVer(short ver)
    {
    setNetShort(csmsghdr, OFST_CSMSSGVER, ver);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte getHandle()
    {
    return csmsghdr[OFST_HANDLE];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setHandle(byte handle)
    {
    csmsghdr[OFST_HANDLE] = handle;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte getMsgcode()
    {
    return csmsghdr[OFST_MSGCODE];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setMsgcode(byte msgcode)
    {
    csmsghdr[OFST_MSGCODE] = msgcode;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte get4GLCondCode()
    {
    byte[] msgbuf = getMsgbuf();
    return msgbuf[OFST_4GL_COND_CODE];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void set4GLCondCode(byte condcode)
    {
    byte[] msgbuf = getMsgbuf();
    msgbuf[OFST_4GL_COND_CODE] = condcode;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte get4GLErrCode()
    {
    byte[] msgbuf = getMsgbuf();
    return msgbuf[OFST_4GL_ERROR_CODE];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void set4GLErrCode(short errcode)
    {
    byte[] msgbuf = getMsgbuf();
    setNetShort(msgbuf, OFST_4GL_ERROR_CODE, errcode);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String get4GLErrMsg()
    {
    byte[] msgbuf = getMsgbuf();
    return getNetString(msgbuf, OFST_4GL_ERR_MSG);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte get4GLConnAckFlags()
    {
    byte[] msgbuf = getMsgbuf();
    int idx = 5 + getNetShort(msgbuf,OFST_4GL_ERR_MSG);
    return msgbuf[idx];
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void set4GLConnAckFlags(byte flags)
    {
    byte[] msgbuf = getMsgbuf();
    int idx = 5 + getNetShort(msgbuf,OFST_4GL_ERR_MSG);
    msgbuf[idx] = flags;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public boolean cmpMsg(ubAppServerMsg msg)
    {
    boolean ret;
    int i, len;

	ret = super.cmpMsg(msg);

	if (ret)
      ret = (
    	( getCsnetVer()      == msg.getCsnetVer()   ) &&
        ( getMsglen()        == msg.getMsglen()     ) &&
    	( getCsmssgVer()     == msg.getCsmssgVer()  ) &&
        ( getHandle()        == msg.getHandle()     ) &&
        ( getMsgcode()       == msg.getMsgcode()    ) 
	   );

    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader()
    {
    System.err.println(" CSNET ver= " + getCsnetVer() );
    System.err.println(" seqnum= " + getSeqnum());
    System.err.println(" msglen= " + getMsglen());
    System.err.println(" CSMSSG ver= " + getCsmssgVer() );
    System.err.println(" handle= " + getHandle() );
    System.err.println(" msgcode= " + getMsgcode() + 
                            " " + msgcodeDesc(getMsgcode()));
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void printSrvHeader(int lvl, int indexEntryType, IAppLogger lg)
    {
    lg.logWithThisLevel(lvl, indexEntryType,
                 " CSNET ver= " + getCsnetVer() );
    lg.logWithThisLevel(lvl, indexEntryType,
                 " seqnum= " + getSeqnum());
    lg.logWithThisLevel(lvl, indexEntryType,
                 " msglen= " + getMsglen());
    lg.logWithThisLevel(lvl, indexEntryType,
                 " CSMSSG ver= " + getCsmssgVer() );
    lg.logWithThisLevel(lvl, indexEntryType,
                 " handle= " + getHandle() );
    lg.logWithThisLevel(lvl, indexEntryType,
           " msgcode= " + getMsgcode() + 
           " " + msgcodeDesc(getMsgcode()));
    }

/**********************************************************************/
/* Abstract methods                                                   */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public byte[] getSrvHeader()
    {
    return getcshdr();
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getSrvHeaderlen()
    {
    return CSHDRLEN;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getSrvMsgDesc()
    {
    String ret;
    int msgcode = getMsgcode();

    ret = msgcodeDesc(msgcode);
    if (ret == null)
        {
        ret = "CSMSSGCODE= 0x" + Integer.toString(msgcode,16);
        }

    return ret;
    }

/*********************************************************************/
/* internal methods                                                  */
/*********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/


private void initCSMsg(	short csnetver,
			int   seqnum,
			short msglen,
			short csmssgver,
			byte  handle,
            byte  msgcode
			)
    {
    csmsghdr = new byte[CSHDRLEN];

    /* csnet header */
    setNetShort(csmsghdr, OFST_CSNETVER, csnetver);
    setNetInt(csmsghdr, OFST_SEQNUM, seqnum);
    setNetShort(csmsghdr, OFST_MSGLEN, msglen);

    /* csmssg header */
    setNetShort(csmsghdr, OFST_CSMSSGVER, csmssgver);
    csmsghdr[OFST_HANDLE] = handle;
    csmsghdr[OFST_MSGCODE] = msgcode;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

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

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

}  /* end of ubAppServerMsg.java */

