
/*************************************************************/
/* Copyright (c) 1984-2012 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubConstants                                              */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

/*********************************************************************/
/*                                                                   */
/* Class ubConstants                                                 */
/*                                                                   */
/*********************************************************************/

public interface ubConstants
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/* this flag MUST be set false except for debugging */
public static final boolean DEBUG                    = true;

public static final int    SERVICE_PRIORITY          = Thread.NORM_PRIORITY+1;
public static final int    CLIENT_THREAD_PRIORITY    = Thread.NORM_PRIORITY;
public static final int    SERVER_THREAD_PRIORITY    = Thread.NORM_PRIORITY;

public static final String WIRECODEPAGE              = "UTF8";
public static final String PROGRESS_WIRECODEPAGE     = "utf-8";
public static final String LOCALHOST                 = "localhost";

public static final int    SERVERTYPE_APPSERVER      = 0x00;
public static final int    SERVERTYPE_WEBSPEED       = 0x01;
public static final int    SERVERTYPE_DATASERVER_OD  = 0x02;
public static final int    SERVERTYPE_DATASERVER_OR  = 0x03;
public static final int    SERVERTYPE_ADAPTER        = 0x04;
public static final int    SERVERTYPE_DATASERVER_MSS = 0x05;
public static final int    SERVERTYPE_ADAPTER_CC     = 0x06;
public static final int    SERVERTYPE_ADAPTER_SC     = 0x07;
public static final int    SERVERTYPE_PASOE          = 0x08;

public static final int    SERVERMODE_STATELESS      = 0;
public static final int    SERVERMODE_STATE_AWARE    = 1;
public static final int    SERVERMODE_STATE_RESET    = 2;
public static final int    SERVERMODE_STATE_FREE     = 3;

public static final int    REGMODE_IP             = 0;
public static final int    REGMODE_LOCALHOST      = 1;
public static final int    REGMODE_HOSTNAME       = 2;

public static final int    SELECTIONSCHEME_FIFO   = 0;
public static final int    SELECTIONSCHEME_LIFO   = 1;
public static final int    SELECTIONSCHEME_AFFINITY = 2;

public static final String[] STRING_SERVER_TYPES =
  {
  "AS"
, "WS"
, "OD"
, "OR"
, "AD"
, "MS"
, "CC"
, "SC"
  };


public static final int    MSG_INPUT_STREAM_BUFSIZE  = 10240 ;
public static final int    MSG_OUTPUT_STREAM_BUFSIZE = /* 10240 */ 1024;

public static final int    MSG_DEF_BUFSIZE = 8192;      // 8K
public static final int    MSG_MIN_BUFSIZE = (1*1024);  // 1K
public static final int    MSG_MAX_BUFSIZE = (60*1024); // 60 K

public static final int    RQ_CLOSE_OK     = 1;
public static final int    RQ_CLOSE_ABEND  = 2;
public static final int    RQ_CLOSE_STOP   = 3;

public static final long   INVALID_TIMESTAMP    = -1;

/* from cs.h */
public static final int    CSNET_CLIENT_DISCONNECT   = -4006;
public static final int    CSNET_ERR_RECEIVE_FAILURE = -4008;

public static final int    SERVER_STATE_IDLE   = 0;
public static final int    SERVER_STATE_BUSY   = 1;
public static final int    SERVER_STATE_LOCKED = 2;
public static final int    SERVER_STATE_OTHER  = 3;

/* The two arrays below hardwire AppServer capability information.
 * Each piece of capability info consists a Type and a string value.
 * Note that the two arrays go together. Entries between the two arrays
 * are paired up by the same index. Adding or dropping an entry
 * must be done to both at the same time.
 */

public static final short[] APPSRVCAPINFO_TYPE =
  {
    3001,  /* PROP_APSVCL_VERS */
    2001,  /* PROP_UB_MSG_VERS */
    2002,  /* PROP_CSNET_VERS */
    2003,  /* PROP_CSNET_MSG_VERS */
    2004,  /* PROP_OE_MAJOR_VERS */
    2005,  /* PROP_OE_MINOR_VERS */
    2006,  /* PROP_OE_MAINT_VERS */
    2007,  /* PROP_STREAM_VERS */
    2008,  /* PROP_CLIENT_TYPE */
  };

public static final String[] APPSRVCAPINFO_VALUE =
  {
    "2",    /* APSVCL_CAP_CUR_VER */
    "109",  /* UBMSG_PROTOCOL_VERSION */
    "102",  /* CSNET_PROTOCOL_VERSION */
    "102",  /* CSMSSG_CUR_PROTOCOL_VERSION */
    "11",   /* VERSION_MAJOR */
    "7",    /* VERSION_MINOR */
    "0",    /* VERSION_MAINT */
    "82",   /* STREAM_VERSION */
    "2"     /* TYPE_CLIENT_JAVA */
  };

public static final int  IPVER_IPV4   = 0;
public static final int  IPVER_IPV6   = 1;

/* These constants also appear in the following locations:       */
/*     $RDLRH/capability.h                                       */
/* *** These lists MUST BE KEPT IN SYNC if new values are added  */

public static final int  TYPE_CLIENT_ABL         = 0x0001;
public static final int  TYPE_CLIENT_JAVA        = 0x0002;
public static final int  TYPE_CLIENT_DOTNET      = 0x0004;
public static final int  TYPE_CLIENT_AIA         = 0x0008;
public static final int  TYPE_CLIENT_WSA         = 0x0010;
public static final int  TYPE_CLIENT_ESB         = 0x0020;
public static final int  TYPE_SERVER_APPSERVER   = 0x0040;
public static final int  TYPE_SERVER_WSAGENT     = 0x0080;
public static final int  TYPE_CLIENT_BPMADAPTER  = 0x0100;
public static final int  TYPE_CLIENT_REST		 = 0x0200;
public static final int  TYPE_SERVER_MTAPSV		 = 0x0400;

public static final int[] CLIENT_TYPES =
  {
        TYPE_CLIENT_ABL
      , TYPE_CLIENT_JAVA
      , TYPE_CLIENT_DOTNET
      , TYPE_CLIENT_AIA
      , TYPE_CLIENT_WSA
      , TYPE_CLIENT_ESB
      , TYPE_SERVER_APPSERVER
      , TYPE_SERVER_WSAGENT
      , TYPE_CLIENT_BPMADAPTER
      , TYPE_CLIENT_REST
      , TYPE_SERVER_MTAPSV
  };


/*********************************************************************/
/*                                                                   */
/*********************************************************************/

} /* class ubConstants */
