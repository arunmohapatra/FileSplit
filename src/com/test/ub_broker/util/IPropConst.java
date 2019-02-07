//*************************************************************
//  Copyright (c) 1984-2010 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// @(#)IPropConst.java   3.4  10/02/2000
//
// Defines the property group and names used in SkyWalker Unified Broker
// Tool and Name server tool set.
//
// These definitions are keywords that must be used in a property file
// for the unified broker and name server.  For example, the property file
// for a WebSpeed broker, foo, may look like the following:
//
//  [UBroker]
//
//  hostName=somewhere
//  portNumber=3000
//
//  [UBroker.WS.foo]
//
//  hostName=bar
//  workdir=c:/temp
//  srvrAppMode=Production
//  srvrDebug=Disabled
//
// @author: Edith Tan,  12/1/97
//
// History:
//
//   10/02/00  t2   Changed version string from 91b to 91c.
//
//   08/28/00  est  Added support for two new AIA properties: allowAiaCmds and adminIPList.
//
//   08/08/00  est  Added a new AIA property: soReadTimeout.
//
//   08/04/00  est  Changed the default and max value of nsClientPortRetryInterval and
//                  nsClientPortRetry.
//
//   07/12/00  est  Added definition for 4 new nsClientPort related properties. These
//                  properties are added to [WebSpeed.Messengers] as well as [AIA].
//                  [AIA.nameServerClientPort] is removed. In its place, two more properties
//                  are added: minNSClientPort and maxNSClientPort.
//
//   06/30/00  est  Added support for MSS DataServer Broker.
//
//   04/12/00  nmt  Added stuff for AppServer Internet Adapter
//
//   03/30/00  est  Added useConnID property to the Messenger group.
//
//   03/24/00  nmt  Changed references to JMS to SonicMQ adapter
//
//   02/15/00  est  Added property definition to support JMS broker.
//
//   12/29/99  est  Added new ubroker property: registrationMode
//
//   12/07/99  est  Create a dummy parent for Preference group.
//
//   10/13/99  est  Added UB_MONITOR_INTERVAL.
//
//    6/22/99  est  Rename cgiipScriptFile to msgrScriptFile
//
//    4/13/99  est  upgrade CURRENT_VERSION
//
//    4/2/99   est  Move the default property and schema filename definition to
//                  IPropFilename.java.
//
//    3/24/99  est  Added missing definition for messenger-specific properties
//
package com.progress.ubroker.util;

import com.progress.ubroker.tools.MSGuiPlugin;
import com.progress.ubroker.tools.NSGuiPlugin;
import com.progress.ubroker.tools.UBGuiPlugin;
import com.progress.ubroker.tools.adapter.AdapterGuiPlugin;
import com.progress.ubroker.tools.aia.AiaGuiPlugin;
import com.progress.ubroker.tools.rest.RestGuiPlugin;
import com.progress.ubroker.tools.wsa.WsaGuiPlugin;
import com.progress.ubroker.util.IPropFilename;

public interface IPropConst extends IPropFilename
{
  //
  // Move to defined in IPropFilename
  // Default property filename
  //
  //public static final String DEF_UBROKER_PROP_FILENAME          = "ubroker.properties";
  //public static final String DEF_UBROKER_SCHEMA_FILENAME        = "ubroker.schema";
  //public static final String DEF_UBROKER_PROP_FILENAME_TEMPLATE = "ubroker.properties.template";
  //
  // versioning info
  //
  public static String GROUP_SEPARATOR = ".";
  public static String VERSION_TAG     = "%% version";
  public static String CURRENT_VERSION = "b001";
  public static int    UB_MONITOR_INTERVAL = 2001;

  //*********************************************************************
  //*  keywords: parent group and child group name, and property names
  //*********************************************************************
  //
  // Parent Group Name property keyword
  //
  // This keyword drives the general organization of the property
  // file for various personalites of the Unified broker and name
  // server instances for a single installation. The property name
  // and value pair in this group has special meaning. The property names
  // within the group serve as tree node labels that are displayed
  // in the tree view panel of the Chimera framework window. The value
  // of a specific property name serves as the base property group name
  // for a specific Unified broker personality.  For example, for
  // the following property name value pair:
  //
  //    WebSpeed=UBroker.WS
  //
  // the property name, WebSpeed will be used to display in the
  // tree view panel, the property child group for all WebSpeed unified
  // broker should start with the property group name, [UBroker.WS],
  // For example, if we want to define properties for a WebSpeed unified
  // broker, wsbroker1, then the property child group name will be
  // [UBroker.WS.wsbroker1].
  //
  // The following section should be found to lead to the rest of the
  // content of the property file:
  //
  //
  // [ParentGroups]
  //   WebSpeed=UBroker.WS
  //   AppServer=UBroker.AS
  //   Oracle DataServer=UBroker.OR
  //   ODBC DataServer=UBroker.OD
  //   Name Server=NameServer
  //   Messengers=WebSpeed.Messengers
  //   SonicMQ Adapter=Adapter
  //   AppServer Internet Adapter=AIA
  //
  public static String PARENT_GROUPS        = "ParentGroup";
  
  
  /**
   * webspeed display name
   */
  public static String WS_PERSONALITY       = UBGuiPlugin.WEBSPD_ID;
  /**
   * appserver display name
   */
  public static String AS_PERSONALITY       = UBGuiPlugin.APPSVR_ID;
  
  /**
   * oracle dataserver display name
   */
  public static String OR_PERSONALITY       = UBGuiPlugin.OR_ID;
  
  /**
   * odbc dataserver display name
   */
  public static String OD_PERSONALITY       = UBGuiPlugin.OD_ID;
  /**
   * mss dataserver display name
   */
  public static String MSS_PERSONALITY      = UBGuiPlugin.MSS_ID;
  
  /**
   * nameserver display name
   */
  public static String NS_PERSONALITY       = NSGuiPlugin.PLUGIN_ID;
  
  /**
   * messenger display name
   */
  public static String MSNGR_PERSONALITY   = MSGuiPlugin.PLUGIN_ID;
  
  /**
   * sonic adapter disply anme
   */
  public static String ADAPTER_PERSONALITY  = AdapterGuiPlugin.PLUGIN_ID;
  
  /**
   * aia display name
   */
  public static String AIA_PERSONALITY      = AiaGuiPlugin.PLUGIN_ID;
  
  /**
   * web services adapter display name
   */
  public static String WSA_PERSONALITY      = WsaGuiPlugin.PLUGIN_ID;
  
  /**
   * rest manager display name
   */
  public static String REST_PERSONALITY     = RestGuiPlugin.PLUGIN_ID;
  
  /**
   * oe web server display name
   */
  public static String RESTMGR_PERSONALITY  = "OE Web Server";
  
  /**
   * PAS display name
   */
  public static String PAS_PERSONALITY = "Progress Application Server";
  

  /**
   * database display name
   */
  public static String DATABASE_PERSONALITY = "Database";

  /**
   * scripted managed database display name
   */
  public static String SMDATABASE_PERSONALITY = "SMDatabase";
  
  //
  // Known Parent Group names
  //
  public static String BROKER_GROUP          = "UBroker";
  public static String NAME_SRVR_GROUP       = "NameServer";
  public static String ENV_GROUP_PARENT      = "Environment";
  public static String WS_GROUP_PARENT       = "WebSpeed";  // for messenger really
  public static String PREFERENCEROOT_GROUP  = "PreferenceRoot";
  public static String PREFERENCE_GROUP      = "Preference";
  public static String ADAPTER_GROUP         = "Adapter";
  public static String AS_GROUP              = "UBroker.AS";
  public static String WS_GROUP              = "UBroker.WS";
  public static String NS_GROUP              = "NameServer";
  public static String MS_GROUP              = "UBroker.MS";
  public static String OD_GROUP              = "UBroker.OD";
  public static String OR_GROUP              = "UBroker.OR";
  public static String MSNGR_GROUP           = "WebSpeed.Messengers";
  public static String AIA_GROUP             = "AIA";
  public static String WSA_GROUP             = "WSA";
  public static String REST_GROUP            = "REST";
  public static String ADMIN_ROLE_GROUP      = "AdminRole";
  public static String PAS_GROUP			 = "PAS";
  public static String DATABASE_GROUP        = "database";
  public static String SMDATABASE_GROUP      = "smdatabase";


  //
  // Generic Child Group name
  //
  public static String WEBSPEED_BROKER   = "WS";
  public static String APPSRVR_BROKER    = "AS";
  public static String ORACLE_DS_BROKER  = "OR";
  public static String ODBC_DS_BROKER    = "OD";
  public static String MSS_DS_BROKER     = "MS";
  public static String ADAPTER_BROKER    = "AD";
  public static String AIA_BROKER        = "AIA";

  //
  // Special service type for start up parameter
  //
  public static String NAME_SERVER_TYPE  = "NS";

  public static String MSNGR_TYPE = "MSNGR";

  public static String ADAPTER_TYPE = "AD";

  public static String AIA_TYPE    = "AIA";

  public static String WSA_STR_TYPE = "WSA";

  public static String REST_STR_TYPE = "REST";
  
  //
  // unified broker service type enumeration
  //
  public final static int COMMON_TYPE = 0;
  public final static int WS_TYPE     = 1;  // WebSpeed
  public final static int AS_TYPE     = 2;  // AppServer
  public final static int OR_TYPE     = 3;  // Oracle DataServer
  public final static int OD_TYPE     = 4;  // ODBC DataServer
  public final static int MSS_TYPE    = 5;  // MSS Dataserver

  //
  //  Additional service types:
  //
  public final static int NS_TYPE     = 6;  // NameServer
  public final static int MS_TYPE     = 7;  // messengers
  public final static int AD_TYPE     = 8;  // SonicMQ Adapter
  public final static int AIA_INT_TYPE = 9; // AppServer Internet Adapter (AIA)
  public final static int WSA_TYPE    = 10; // Web Services Adapter (WSA)
  public final static int REST_TYPE   = 11; // AppServer REST Adapter (REST)
  public final static int PAS_TYPE    = 12;


  public static String[] childGroupNames =
  {
    "",
    WEBSPEED_BROKER, APPSRVR_BROKER,
    ORACLE_DS_BROKER, ODBC_DS_BROKER,
    MSS_DS_BROKER
  };

  public static String PREFERENCE_CHILD_GROUP_SPEC =
     PREFERENCEROOT_GROUP + GROUP_SEPARATOR + PREFERENCE_GROUP;
  //
  // Generic UBroker Group path
  //
  public static String[] ubGroupPath =
  {
    BROKER_GROUP,
    BROKER_GROUP + GROUP_SEPARATOR + WEBSPEED_BROKER,
    BROKER_GROUP + GROUP_SEPARATOR + APPSRVR_BROKER,
    BROKER_GROUP + GROUP_SEPARATOR + ORACLE_DS_BROKER,
    BROKER_GROUP + GROUP_SEPARATOR + ODBC_DS_BROKER,
    BROKER_GROUP + GROUP_SEPARATOR + MSS_DS_BROKER
  };

  //
  // UBroker's presentation category names
  //
  public static String[] ubCategories =
  {
    "",
    "WebSpeed", "AppServer",
    "OracleDataServer", "ODBCDataServer",
    "MSSDataServer"
  };


  //
  // Messenger property group and property names
  //
  public final static String MSNGR_GROUP_PATH = "Messengers";
  public final static String WS_MSNGR_GROUP_PATH = WS_GROUP_PARENT + GROUP_SEPARATOR +
                                                   MSNGR_GROUP_PATH;
  //
  // Various messenger names.  This is displayed as a tree leaf name in the tree view
  // of the explorer window, and it is also being used as the last part of property
  // group name for looking up properties of a specific messenger.
  //
  public final static String CGIIP_MSNGR = "CGIIP";
  public final static String WSISA_MSNGR = "WSISA";
  public final static String WSNSA_MSNGR = "WSNSA";
  public final static String WSASP_MSNGR = "WSASP";

  //
  // messenger properties
  //
  public final static String  MSNGR_COMPONENTS_FLAG = "Components";
  public final static String  ALLOW_WSMADMIN    = "AllowMsngrCmds";
  public final static String  MSNGR_SCRIPT_PATH = "ScriptPath";
  public final static String  MSNGR_EXEC_FILE   = "msngrExecFile";
  public final static String  ROOT_PATH         = "rootPath";
  public final static String  CGIIP_SCRIPT_FILE = "msngrScriptFile";
  public final static String  MSNGR_USE_CONNID  = "useConnID";
  public final static String  WSM_ADM_IPLIST    = "wsmAdmIPList";


  //
  // A list of broker property names usually registered under the parent group or
  // generic child group.
  //

  //
  //  General Properties
  //
  public static String INFO_VERSION    = "infoVersion";  // 9010
  public static String OPERATING_MODE  = "operatingMode";
  public static String PORT            = "portNumber";
  public static String WORKDIR         = "workDir";
  public static String SRVRLOG         = "srvrLogFile";
  public static String BRKRLOG         = "brokerLogFile";
  public static String STARTUP_PARAM   = "srvrStartupParam";
  public static String INI_SRVR_INST   = "initialSrvrInstance";
  public static String MIN_SRVR_INST   = "minSrvrInstance";
  public static String MAX_SRVR_INST   = "maxSrvrInstance";
  public static String SRVR_MIN_PORT   = "srvrMinPort";
  public static String SRVR_MAX_PORT   = "srvrMaxPort";
  public static String AUTO_START      = "autoStart";
  public static String ENVIRONMENT     = "environment";
  public static String PROPATH         = "PROPATH";
  public static String SERVICE_NAMES   = "appserviceNameList";
  public static String UUID            = "uuid";
  public static String DESCRIPTION     = "description";
  public static String SRVR_EXECFILE   = "srvrExecFile";
  public static String SRVR_LOGLEVEL   = "srvrLoggingLevel";
  public static String SRVR_LOGAPPEND  = "srvrLogAppend";
  public static String JVMARGS         = "jvmArgs";
  public static String JVMSTARTARGS         = "jvmStartArgs";
  public static String AUTO_TRIM_TIMEOUT	= "autoTrimTimeout";
  public static String FOURGL_SRC_COMPILE   = "4glSrcCompile";
  public static String REG_MODE        = "registrationMode";
  public static String COLL_STATS_DATA = "collectStatsData";

  //
  //     Owner Info
  //
  public static String USERNAME   = "userName";
  public static String PASSWORD   = "password";
  public static String GROUPNAME  = "groupName";  // = password??

  //
  //  Advanced Property set starts here
  //
  public static String CNTL_NAME_SRVR       = "controllingNameServer";
  public static String PRIORITY_WEIGHT      = "priorityWeight";
  public static String REGIS_RETRY          = "registrationRetry";
  public static String BRKR_LOGLEVEL        = "brkrLoggingLevel";
  public static String BRKR_LOGAPPEND       = "brkrLogAppend";
  public static String SRVR_STARTUP_TIMEOUT = "srvrStartupTimeout";
  public static String REQUEST_TIMEOUT      = "requestTimeout";
  public static String MAX_CLIENT_INST      = "maxClientInstance";

  //
  // Internal Propert set, and currently displayable in the tool
  //
  public static String UB_CLASSMAIN    = "classMain";

  //
  //  WebSpeed Broker-specific properties
  //
  public static String APP_MODE         = "srvrAppMode";
  public static String DEBUG_MODE       = "srvrDebug";
  public static String APP_URL          = "applicationURL";
  public static String APP_DIRECTORY    = "applicationDirectory";
  public static String DEFAULT_SERVICE  = "defaultService";
  public static String DEFCOOKIE_PATH   = "defaultCookiePath";
  public static String DEFCOOKIE_DOMAIN = "defaultCookieDomain";
  public static String WSROOT           = "wsRoot";
  public static String FILE_UPLOAD_DIR  = "fileUploadDirectory";
  public static String BIN_UPLOAD_MAXSIZE = "binaryUploadMaxSize";

  //
  // AppServer Broker  properties
  //
  public static String STARTUP_PROC        = "srvrStartupProc";
  public static String STARTUP_PROC_PARAM  = "srvrStartupProcParam";
  public static String CONNECT_PROC        = "srvrConnectProc";
  public static String SHUTDOWN_PROC       = "srvrShutdownProc";
  public static String DISCONN_PROC        = "srvrDisconnProc";
  public static String ACTIVATE_PROC       = "srvrActivateProc";
  public static String DEACTIVATE_PROC     = "srvrDeactivateProc";


  //
  // NameServer-specific Properties
  //
  //
  public static String LOCATION          = "location";
  public static String BRKR_KEEP_ALIVE_TIMEOUT = "brokerKeepAliveTimeout";
  public static String HOST              = "hostName";
  public static String DEBUGGER_ENABLED  = "DebuggerEnabled";
  public static String LOGLEVEL          = "loggingLevel";
  public static String LOGAPPEND         = "logAppend";
  public static String NEIGHBOR_NS       = "neighborNameServers";

  //
  // AIA-specific properties
  //
  public static String LOGFILE          = "logFile";
  public static String IDDLECON_TIMEOUT = "idleConnectionTimeout";
  public static String HTTPSENABLED     = "httpsEnabled";
  public static String SECUREPORT       = "securePort";
  public static String SO_READ_TIMEOUT  = "soReadTimeout";
  public static String ALLOW_AIA_CMDS   = "allowAiaCmds";
  public static String ADMIN_IP_LIST    = "adminIPList";


  //
  // nameServer client port-related properties. These properties are defined in the
  // following groups: [WebSpeed.Messengers] and [AIA]
  //
  public static String MIN_NSCLIENTPORT     = "minNSClientPort";
  public static String MAX_NSCLIENTPORT     = "maxNSClientPort";
  public static String NSCLIENTPORT_RETRY_INTERVAL     = "nsClientPortRetryInterval";
  public static String NSCLIENTPORT_RETRY              = "nsClientPortRetry";

  // REST-specific properties
  public static String RESTURL = "restUrl";
  public static final int maxRestLoggingLevel = 7;

  // WSA-specific properties
  public static String WSAURL = "wsaUrl";
  public static String WEBSERVERAUTH = "webServerAuth";
  public static String PROXYHOST     = "proxyHost";
  public static String PROXYPASSWORD = "proxyPassword";
  public static String PROXYPORT     = "proxyPort";
  public static String PROXYUSERNAME = "proxyUsername";

  //
  // NSClientPort-specific range spec and default values
  //
  public static int NSCLIENTPORT_MINVAL     = 1024;
  public static int NSCLIENTPORT_MAXVAL     = 65535;
  public static int NSCLIENTPORT_RETRY_INTERVAL_DEF    = 250;  // in ms.
  public static int NSCLIENTPORT_RETRY_DEF             = 4;
  public static int NSCLIENTPORT_RETRY_INTERVAL_MAX    = 10000;// in ms.
  public static int NSCLIENTPORT_RETRY_MAX             = 500;

  // max logging level
  public static final int    maxWsaLoggingLevel         = 7;

  //
  // Preference properties
  //
  public static String TOOL_GET_SVC_STATUS_RETRY = "toolGetSvcStatusRetry";
  public static String TOOL_PING_SVC_RETRY       = "toolPingSvcRetry";
  public static String TOOL_SHUTDOWN_SVC_CONFIRM_RETRY          = "toolShutdownSvcConfirmRetry";
  public static String TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL = "toolShutdownSvcConfirmRetryInterval";
  public static String TOOL_CONNECT_SVC_RETRY                   = "toolConnectSvcRetry";
  public static String TOOL_CONNECT_SVC_RETRY_INTERVAL          = "toolConnectSvcRetryInterval";
  public static String ADMSRVR_REGISTER_RETRY                   = "admSrvrRegisteredRetry";
  public static String ADMSRVR_REGISTER_RETRY_INTERVAL          = "admSrvrRegisteredRetryInterval";


  //
  // default preference property values
  //
  public static int TOOL_WIDTH_DEF                               = 500;
  public static int TOOL_HEIGHT_DEF                              = 400;
  public static int TOOL_GET_STATUS_RETRY_DEF                    = 12;
  public static int TOOL_PING_RETRY_DEF                          = 4;
  public static int TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_DEF          = 10;
  public static int TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL_DEF = 3000;
  public static int TOOL_CONNECT_SVC_RETRY_DEF                   = 20;
  public static int TOOL_CONNECT_SVC_RETRY_INTERVAL_DEF          = 3000;
  public static int ADMSRVR_REGISTER_RETRY_DEF                   = 6;
  public static int ADMSRVR_REGISTER_RETRY_INTERVAL_DEF          = 3000;


  // **************************************
  // * valid choices for some properties
  // **************************************
  //
  // Location choices
  //
  public static String LOCATION_LOCAL     = "local";
  public static String LOCATION_REMOTE    = "remote";

  //
  // Application mode choices
  //
  public static String APP_MODE_DEVO      = "Development";
  public static String APP_MODE_PROD      = "Production";

  //
  // debug mode choices
  //
  public static String DEBUG_MODE_DEFAULT = "Default";
  public static String DEBUG_MODE_DISABLE = "Disabled";
  public static String DEBUG_MODE_ENABLE  = "Enabled";

  //
  // State-aware choices
  //
  public static String OP_MODE_STATELESS    = "Stateless";
  public static String OP_MODE_STATE_RESET  = "State-reset";
  public static String OP_MODE_STATE_AWARE  = "State-aware";
  public static String OP_MODE_STATE_FREE   = "State-free";

  //
  // AppServer Keepalive choices
  //
  public static String ASK_MODE_NONE      = "denyClientASK,denyServerASK";
  public static String ASK_MODE_SERVERASK = "denyClientASK,allowServerASK";
  public static String ASK_MODE_CLIENTASK = "allowClientASK,denyServerASK";
  public static String ASK_MODE_BOTH      = "allowClientASK,allowServerASK";

  //
  // Server executable filename choices
  //
  public static String SRVR_EXEC_DIR_NT   = "@{Startup\\DLC}\\bin\\";
  public static String SRVR_EXEC_DIR_UNIX = "$DLC/bin/";

  public static String KEYSTORE_DIR_NT    = "@{Startup\\DLC}\\keys\\";
  public static String KEYSTORE_DIR_UNIX  = "$DLC/keys/";

  public static String CERTSTORE_DIR_NT   = "@{Startup\\DLC}\\certs\\";
  public static String CERTSTORE_DIR_UNIX = "$DLC/certs/";

  public static String PSCCERTSTORE_NT    = "@{Startup\\DLC}\\certs\\psccerts.jar";
  public static String PSCCERTSTORE_UNIX  = "$DLC/certs/psccerts.jar";

  public static String WS_SRVR_EXEC_FN    = "_progres.exe";
  public static String WS_SRVR_EXEC_UNIX  = "_progres";

  public static String APP_SRVR_EXEC_FN   = "_proapsv.exe";
  public static String APP_SRVR_EXEC_UNIX = "_proapsv";

  public static String OD_SRVR_EXEC_FN    = "_odbsrv.exe";
  public static String OD_SRVR_EXEC_UNIX  = "_odbsrv";

  public static String MS_SRVR_EXEC_FN    = "_msssrv.exe";
  public static String MS_SRVR_EXEC_UNIX  = "_msssrv";

  /* OE00159050- 10.1C- Changing the default value of Oracle
                        server from _ora7srv to _orasrv */

  public static String OR_SRVR_EXEC_FN    = "_orasrv.exe";
  public static String OR_SRVR_EXEC_UNIX  = "_orasrv";

  public static String CGIIP_EXEC_FN      = "cgiip.exe";
  public static String CGIIP_EXEC_UNIX    = "cgiip";

  public static String WSISA_EXEC_FN      = "wsisa.dll";
  public static String WSNSA_EXEC_FN      = "wsnsa.dll";
  public static String WSASP_EXEC_FN      = "wsasp.dll";

  public static String CGIIP_SCRIPT_UNIX  = "wspd_cgi.sh";

  //
  // LogFileMode choices
  //
  public static String LOGFILE_MODE_NEW     = "New";
  public static String LOGFILE_MODE_APPEND  = "Append";


  //
  // String property of the boolean properties
  //
  public static String PROP_ON_VAL   = "1";
  public static String PROP_OFF_VAL  = "0";

//
  // JVMARGS platform-specific default values
  //
  public static String JVMARGS_DEF_NT   = "${JAVA\\JVMARGS}";
  public static String JVMARGS_DEF_UNIX = "$JVMARGS";

  //
  // WORKDIR platform-specific default values
  //
  public static String WORKDIR_DEF_NT   = "@{WorkPath}";
  public static String WORKDIR_DEF_UNIX = "$WRKDIR";

  //
  // PROPATH platform-specific default values
  //
  public static String PROPATH_DEF_NT   = "@{WinChar Startup\\PROPATH};@{WorkPath}";
  public static String PROPATH_DEF_UNIX = "${PROPATH}:${WRKDIR}";

  //
  // WS_SRVR_STARTUP_PARAM platform-specific default values
  //
  public static String WS_SRVR_STARTUP_PARAM_DEF_NT   = "-p web\\objects\\web-disp.p -weblogerror";
  public static String WS_SRVR_STARTUP_PARAM_DEF_UNIX = "-p web/objects/web-disp.p -weblogerror";

  //
  // UBroker registrationMode property values
  //
  public static String REG_IP         = "Register-IP"; // default: use the ip address to register the broker inst.
  public static String REG_LOCALHOST  = "Register-LocalHost";  // use local host name to register
  public static String REG_HOSTNAME   = "Register-HostName";  // use specified name string to register


  //
  // various default settings
  //
  // InfoVersion
  //
  public static String INFO_VERSION_DEF = "9010";

  public final static String HOST_DEF = "localhost";
  public final static String PORT_DEF = "3001";

  public final static String MIN_PORTNUM_DEF  = "3202";
  public final static String MAX_PORTNUM_DEF  = "3502";

  public final static String WSROOT_DEF       = "/webspeed";

  public static String SRVR_LOG_FN            = "server.log";
  public static String BRKR_LOG_FN            = "broker.log";

  public static String WS_STARTUP_PARAM_DEF   = "-p web/objects/web_disp.p cpstream iso8859-1 -weblogerror";
  public static String APPS_STARTUP_PARAM_DEF = " ";
  public static String STARTUP_PROC_DEF       = " ";
  public static String STARTUP_PROC_PARAM_DEF = " ";
  public static String CONNECT_PROC_DEF       = " ";
  public static String SHUTDOWN_PROC_DEF      = " ";
  public static String DISCONN_PROC_DEF       = " ";
  public static String ACTIVATE_PROC_DEF      = " ";
  public static String DEACTIVATE_PROC_DEF    = " ";
  public static String FILE_UPLOAD_DIR_DEF    = " ";

  public static Integer INI_SRVR_INST_DEF_VAL = new Integer(5);
  public static Integer MIN_SRVR_INST_DEF_VAL = new Integer(2);
  public static Integer MAX_SRVR_INST_DEF_VAL = new Integer(10);
  public static Integer PRIO_WEIGHT_DEF_VAL   = new Integer(0);
  public static Integer REG_RETRY_DEF_VAL     = new Integer(30);
  public static Integer IDLE_TIMEOUT_VAL      = new Integer(60);
  public static Integer BIN_UPLOAD_MAXSIZE_DEF = new Integer(0);

  public static Integer BRKR_KEEP_ALIVE_TIMEOUT_VAL = new Integer(60);

  public static Integer MAX_CLIENT_INST_DEF_VAL = new Integer(512);

  public static Integer BRKR_LOGLEVEL_DEF_VAL        = new Integer(2);
  public static String  BRKR_LOGAPPEND_DEF_VAL       = PROP_OFF_VAL;
  public static Integer LOGLEVEL_DEF_VAL             = new Integer(2);
  public static String  LOGAPPEND_DEF_VAL            = PROP_OFF_VAL;
  public static String  HTTPSENABLED_DEF_VAL         = PROP_OFF_VAL;
  public static String  FOURGL_SRC_COMPILE_DEF_VAL   = PROP_OFF_VAL;
  public static Integer SRVR_LOGLEVEL_DEF_VAL        = new Integer(2);
  public static String  SRVR_LOGAPPEND_DEF_VAL       = PROP_OFF_VAL;
  public static Integer REQUEST_TIMEOUT_DEF_VAL      = new Integer(15);
  public static Integer IDDLECON_TIMEOUT_DEF_VAL     = new Integer(0);
  public static Integer SRVR_STARTUP_TIMEOUT_DEF_VAL = new Integer(3);
  public static Integer IDEL_THRESHOLD_DEF_VAL       = new Integer(0);
  public static Integer AUTO_TRIM_TIMEOUT_DEF_VAL    = new Integer(1800);

  public static Integer SECUREPORT_DEF               = new Integer(443);
  public static Integer NSCLIENTPORT_DEF             = new Integer(0);

  public static String ALLOW_AIA_CMDS_DEF_VAL       = PROP_OFF_VAL;


  //
  //  default values for name server properties
  //
  public static Integer LOGLEVEL_DEF         = new Integer(2);
  public static String  LOGAPPEND_DEF        = PROP_ON_VAL;
  public static String  NEIGHBOR_NS_DEF      = " ";

  public static String UB_CLASSMAIN_DEF         = "com.progress.ubroker.ubroker";
  public static String AS_UB_CLASSMAIN_DEF      = UB_CLASSMAIN_DEF + " -t AS";
  public static String WS_UB_CLASSMAIN_DEF      = UB_CLASSMAIN_DEF + " -t WS";
  public static String OD_UB_CLASSMAIN_DEF      = UB_CLASSMAIN_DEF + " -t OD";
  public static String OR_UB_CLASSMAIN_DEF      = UB_CLASSMAIN_DEF + " -t OR";
  public static String MSS_UB_CLASSMAIN_DEF     = UB_CLASSMAIN_DEF + " -t MS";
  public static String ADAPTER_CLASSMAIN_DEF    = UB_CLASSMAIN_DEF + " -t AD";
  public static String AIA_CLASSMAIN_DEF        = UB_CLASSMAIN_DEF + " -t AIA";
  public static String NS_CLASSMAIN_DEF         = "com.progress.nameserver.NameServer";


  //
  // max length of some of the properties
  //
  public static int MAX_STRING_64          = 64;
  public static int MAX_STRING_128         = 128;
  public static int MAX_STRING_256         = 256;
  public static int SRVR_STARTUP_PARAM_MAX = MAX_STRING_256;
  public static int SRVR_EXEC_FN_MAX       = MAX_STRING_128;
  public static int SRVR_PROC_MAX          = MAX_STRING_64;
  public static int LOG_FILENAME_MAX       = MAX_STRING_128;
  public static int SRVR_PROPATH_MAX       = MAX_STRING_128;
  public static int SVC_NAMELIST_MAX       = MAX_STRING_256;
  public static int HOST_NAME_MAX          = MAX_STRING_128;
  public static int PORT_NUMBER_MAX        = 10;
  public static int DOMAIN_ACCOUNT_MAX     = MAX_STRING_128;
  public static int PASSWORD_MAX           = MAX_STRING_64;
  public static int WS_APPS_URL_MAX        = MAX_STRING_128;
  public static int WS_TRANX_COOKIE_PATH_MAX   = MAX_STRING_64;
  public static int WS_TRANX_COOKIE_DOMAIN_MAX = MAX_STRING_64;

  public static int BRKR_PRIO_WEIGHT_MIN = 0;
  public static int BRKR_PRIO_WEIGHT_MAX = 100;

  //
  // Limit for an environment group entry
  //
  // 20020115-008: Increasing limit from 20 to 32
  public static int ENV_VARS_MAX = 32;

  //
  // Logging level selection string and its value range
  //
  //   Valid range : any positive integer
  //  invalid value: zero or negative integer
  //  visual presented range: 1 to 3, and >3
  //
  //
  public static int    LOGLEVEL_VISUAL_MIN = 1;
  public static int    LOGLEVEL_VISUAL_MAX = 3;
  public static String[] LOGLEVEL_SELECTIONS = {"Error only",
                                                 "Terse",
                                                 "Verbose"};
  public static String[] LOGLEVEL_SELECTIONS_SPECIAL = {"Error only",
                                                         "Terse",
                                                         "Verbose",
                                                         "Other"};

  // These are used by NumServersDialog.java & UBROKER status to identify the text for translated dialogs
  // To change the output, refer to this file and to MMCMsgBundle.java where these translations are managed
  public static String WEBSPEED_SERVER_NAMING = "agents";
  public static String APPSERVER_SERVER_NAMING = "servers";

  //
  // Various default property value for Adapter instance
  //
  public static final String ADAPTER_SERVER_NAME_DEFAULT = "SonicMQ";
  public static final String ADAPTER_APPSERV_SERVICE_NAME_LIST_DEFAULT = "Adapter";
  public static final String ADAPTER_PORT_DEFAULT = "3600";

  //
  // Aia-specific default value
  //

  public static final Integer SO_READ_TIMEOUT_DEFAULT  = new Integer(240);  // in seconds

  //--------------------------------
  // 9.0A properties removed
  //--------------------------------
  //public static String LOGLEVEL          = "loggingLevel";
  //public static String LOGFILEMODE       = "logFileMode";
  //public static String LOGFILEMODE_DEF    = "New";


}

// END OF FILE

