
//**************************************************************
//  Copyright (c) 1984-2005 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in any form or by any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// @(#)ICfgConst   0.1  10/2/98
//
// author:  N. Terwilliger
//
// Defines constants for the config utilities
//
// NOTE: to verify a new property, add it here and to the
//        method in UBPropValidate.valAllProps()
//
//  History:
//  
//  05/12/00 - Added support for new AppServer Internet Adapter (AIA)
//
//  03/09/00 - added support for registrationMode, and new adapter/SonicMQ
//
//  12/15/99 - added support for OR/OD specific validations - srvrStartupParam
//
//
//
//
package com.progress.common.util;

import com.progress.ubroker.tools.UBCfgPropTableEntry;
import com.progress.ubroker.tools.UBCfgValTableEntry;

public interface ICfgConst
{

    public static final long  NUMPROPS = 70;
   
    
    // property name strings
    public static final String ALLOWMSNGRCMDS              = "AllowMsngrCmds";
    public static final String APPLICATIONURL              = "applicationURL";
    public static final String APPSERVER                   = "AppServer";
    public static final String APPSERVICENAMELIST          = "appserviceNameList";
    public static final String AUTOSTART                   = "autoStart";
    public static final String BROKERKEEPALIVETIMEOUT      = "brokerKeepAliveTimeout";
    public static final String BROKERLOGFILE               = "brokerLogFile";
    public static final String CLASSMAIN                   = "classMain";
    public static final String COMPONENTS                  = "Components";
    public static final String CONTROLLINGNAMESERVER       = "controllingNameServer";
    public static final String DEBUGGERENABLED             = "DebuggerEnabled";
    public static final String DEFAULTCOOKIEDOMAIN         = "defaultCookieDomain";
    public static final String DEFAULTCOOKIEPATH           = "defaultCookiePath";
    public static final String DEFAULTSERVICE              = "defaultService";
    public static final String DESCRIPTION                 = "description";
    public static final String ENVIRONMENT                 = "environment";
    public static final String GROUPNAME                   = "groupName";
    public static final String HTTPSENABLED                = "httpsEnabled";
    public static final String HOSTNAME                    = "hostName";
    public static final String IDLECONNTIMEOUT             = "idleConnectionTimeout";
    public static final String IDLETIMEOUT                 = "idleTimeout";
    public static final String INFOVERSION                 = "infoVersion";
    public static final String INITIALSRVRINSTANCE         = "initialSrvrInstance";
    public static final String LOCATION_PROP               = "location";
    public static final String LOGAPPEND                   = "logAppend";
    public static final String LOGFILE                     = "logFile";
    public static final String LOGFILEMODE                 = "logFileMode";
    public static final String LOGGINGLEVEL                = "loggingLevel";
    public static final String MAXNSCLIENTPORT             = "maxNSClientPort";
    public static final String MAXSRVRINSTANCE             = "maxSrvrInstance";
    public static final String MINNSCLIENTPORT             = "minNSClientPort";
    public static final String MINSRVRINSTANCE             = "minSrvrInstance";
    public static final String MSNGREXECFILE               = "msngrExecFile";
    public static final String MSNGRSCRIPTFILE             = "msngrScriptFile";
    public static final String MULTICASTGROUP              = "multiCastGroup";
    public static final String MULTICASTTTL                = "multiCastTTL";
    public static final String NAMESERVER                  = "Name Server";
    public static final String NSCLIENTPORTRETRY           = "nsClientPortRetry";
    public static final String NSCLIENTPORTRETRYINT        = "nsClientPortRetryInterval";
    public static final String NEIGHBORNAMESERVERS         = "neighborNameServers";
    public static final String ODBC                        = "ODBC DataServer";
    public static final String OPERATINGMODE               = "operatingMode";
    public static final String ORACLE                      = "Oracle DataServer";
    public static final String PASSWORD                    = "Password";
    public static final String PORTNUMBER                  = "portNumber";
    public static final String PRIORITYWEIGHT              = "priorityWeight";
    public static final String PROPATH                     = "PROPATH";
    public static final String REGISTRATIONMODE            = "registrationMode";
    public static final String REGISTRATIONRETRY           = "registrationRetry";
    public static final String ROOTPATH                    = "RootPath";
    public static final String SCRIPTPATH                  = "ScriptPath";
    public static final String SECUREPORT                  = "securePort";
    public static final String SRVRACTIVATEPROC            = "srvrActivateProc";
    public static final String SRVRAPPMODE                 = "srvrAppMode";
    public static final String SRVRCONNECTPROC             = "srvrConnectProc";
    public static final String SRVRDEACTIVATEPROC          = "srvrDeactivateProc";
    public static final String SRVRDEBUG                   = "srvrDebug";
    public static final String SRVRDISCONNPROC             = "srvrDisconnProc";
    public static final String SRVREXECFILE                = "srvrExecFile";
    public static final String SRVRLOGFILE                 = "srvrLogFile";
    public static final String SRVRMAXPORT                 = "srvrMaxPort";
    public static final String SRVRMINPORT                 = "srvrMinPort";
    public static final String SRVRSHUTDOWNPROC            = "srvrShutdownProc";
    public static final String SRVRSTARTUPPARAM            = "srvrStartupParam";
    public static final String SRVRSTARTUPPROC             = "srvrStartupProc";
    public static final String SRVRSTARTUPPROCPARAM        = "srvrStartupProcParam";
    public static final String USERNAME                    = "userName";
    public static final String UUID_NUM                    = "uuid";
    public static final String WEBSPEED                    = "WebSpeed";
    public static final String WORKDIR                     = "workDir";
    public static final String WSROOT                      = "wsRoot";
    public static final String COLLECTSTATSDATA            = "collectStatsData";
    public static final String WSA_URL                     = "wsaUrl";
    public static final String WEB_SERVER_AUTH             = "webServerAuth";
    public static final String PROXY_HOST                  = "proxyHost";
    public static final String PROXY_PASSWORD              = "proxyPassword";
    public static final String PROXY_PORT                  = "proxyPort";
    public static final String PROXY_USERNAME              = "proxyUsername";
    public static final String ADMINSOAPACTION             = "adminSoapAction";
    public static final String HTTPERRORPAGE               = "httpErrorPage";
    public static final String ENABLEWSDL                  = "enableWsdl";
    public static final String ENABLEWSDLLISTINGS          = "enableWsdlListings";
    public static final String WSDLLISTINGPAGE             = "wsdlListingPage";
    public static final String NOWSDLPAGE                  = "noWsdlPage";
    public static final String ADMINAUTH                   = "adminAuth";
    public static final String WSDLAUTH                    = "wsdlAuth";
    public static final String APPAUTH                     = "appAuth";
    public static final String ADMINROLES                  = "adminRoles";
    public static final String WEBAPPENABLED               = "webAppEnabled";
    public static final String ADMINENABLED                = "adminEnabled";
    public static final String APPPROTOCOL                 = "appProtocol";
    public static final String DEBUGCLIENTS                = "debugClients";
    public static final String LOGMSGTHRESHOLD             = "logMsgThreshold";

    // property numbers
    public static final long N_ALLOWMSNGRCMDS              = 10;
    public static final long N_APPLICATIONURL              = 20;
    public static final long N_APPSERVER                   = 30;
    public static final long N_APPSERVICENAMELIST          = 40;
    public static final long N_AUTOSTART                   = 50;
    public static final long N_BROKERKEEPALIVETIMEOUT      = 60;
    public static final long N_BROKERLOGFILE               = 70;
    public static final long N_CLASSMAIN                   = 80;
    public static final long N_COMPONENTS                  = 90;
    public static final long N_CONTROLLINGNAMESERVER       = 100;
    public static final long N_DEBUGGERENABLED             = 110;
    public static final long N_DEFAULTCOOKIEDOMAIN         = 120;
    public static final long N_DEFAULTCOOKIEPATH           = 130;
    public static final long N_DEFAULTSERVICE              = 140;
    public static final long N_DESCRIPTION                 = 150;
    public static final long N_ENVIRONMENT                 = 160;
    public static final long N_GROUPNAME                   = 170;
    public static final long N_HOSTNAME                    = 180;
    public static final long N_HTTPSENABLED                = 181;
    public static final long N_IDLECONNTIMEOUT             = 182;
    public static final long N_IDLETIMEOUT                 = 190;
    public static final long N_INFOVERSION                 = 200;
    public static final long N_INITIALSRVRINSTANCE         = 210;
    public static final long N_LOCATION                    = 220;
    public static final long N_LOGAPPEND                   = 230;
    public static final long N_LOGFILE                     = 231;
    public static final long N_LOGFILEMODE                 = 240;
    public static final long N_LOGGINGLEVEL                = 250;
    public static final long N_MAXNSCLIENTPORT             = 251;
    public static final long N_MAXSRVRINSTANCE             = 260;
    public static final long N_MINNSCLIENTPORT             = 261;
    public static final long N_MINSRVRINSTANCE             = 270;
    public static final long N_MSNGREXECFILE               = 280;
    public static final long N_MSNGRSCRIPTFILE             = 290;
    public static final long N_MULTICASTGROUP              = 300;
    public static final long N_MULTICASTTTL                = 310;
    public static final long N_NAMESERVER                  = 320;
    public static final long N_NEIGHBORNAMESERVERS         = 330;
    public static final long N_NSCLIENTPORTRETRY           = 331;
    public static final long N_NSCLIENTPORTRETRYINT        = 332;
    public static final long N_ODBC                        = 340;
    public static final long N_OPERATINGMODE               = 350;
    public static final long N_ORACLE                      = 360;
    public static final long N_PASSWORD                    = 365;
    public static final long N_PORTNUMBER                  = 370;
    public static final long N_PRIORITYWEIGHT              = 380;
    public static final long N_PROPATH                     = 390;
    public static final long N_REGISTRATIONMODE            = 391;
    public static final long N_REGISTRATIONRETRY           = 400;
    public static final long N_ROOTPATH                    = 410;
    public static final long N_SCRIPTPATH                  = 420;
    public static final long N_SECUREPORT                  = 421;
    public static final long N_SRVRACTIVATEPROC            = 430;
    public static final long N_SRVRAPPMODE                 = 440;
    public static final long N_SRVRCONNECTPROC             = 450;
    public static final long N_SRVRDEACTIVATEPROC          = 460;
    public static final long N_SRVRDEBUG                   = 470;
    public static final long N_SRVRDISCONNPROC             = 480;
    public static final long N_SRVREXECFILE                = 490;
    public static final long N_SRVRLOGFILE                 = 500;
    public static final long N_SRVRMAXPORT                 = 510;
    public static final long N_SRVRMINPORT                 = 520;
    public static final long N_SRVRSHUTDOWNPROC            = 530;
    public static final long N_SRVRSTARTUPPARAM            = 540;
    public static final long N_SRVRSTARTUPPROC             = 550;
    public static final long N_SRVRSTARTUPPROCPARAM        = 560;
    public static final long N_USERNAME                    = 570;
    public static final long N_UUID_NUM                    = 580;
    public static final long N_WEBSPEED                    = 590;
    public static final long N_WORKDIR                     = 600;
    public static final long N_WSROOT                      = 610;
    public static final long N_WSAURL                      = 620;
    public static final long N_WEBSERVERAUTH               = 630;
    public static final long N_PROXYHOST                   = 640;
    public static final long N_PROXYPASSWORD               = 650;
    public static final long N_PROXYPORT                   = 660;
    public static final long N_PROXYUSERNAME               = 670;
    public static final long N_ADMINSOAPACTION             = 680;
    public static final long N_HTTPERRORPAGE               = 690;
    public static final long N_ENABLEWSDL                  = 700;
    public static final long N_ENABLEWSDLLISTINGS          = 710;
    public static final long N_WSDLLISTINGPAGE             = 720;
    public static final long N_NOWSDLPAGE                  = 730;
    public static final long N_ADMINAUTH                   = 740;
    public static final long N_WSDLAUTH                    = 750;
    public static final long N_APPAUTH                     = 760;
    public static final long N_ADMINROLES                  = 770;
    public static final long N_WEBAPPENABLED               = 780;
    public static final long N_ADMINENABLED                = 790;
    public static final long N_APPPROTOCOL                 = 800;
    public static final long N_DEBUGCLIENTS                = 810;
    public static final long N_LOGMSGTHRESHOLD             = 820;
    public static final long N_COLLECTSTATSDATA            = 830;


    //property name/number table
    public static final UBCfgPropTableEntry[] propNameToNumTable =
  {
    new UBCfgPropTableEntry(ALLOWMSNGRCMDS,        N_ALLOWMSNGRCMDS),
    new UBCfgPropTableEntry(APPLICATIONURL,        N_APPLICATIONURL),
    new UBCfgPropTableEntry(APPSERVER,             N_APPSERVER),
    new UBCfgPropTableEntry(APPSERVICENAMELIST,    N_APPSERVICENAMELIST),
    new UBCfgPropTableEntry(AUTOSTART,             N_AUTOSTART),
    new UBCfgPropTableEntry(BROKERKEEPALIVETIMEOUT,N_BROKERKEEPALIVETIMEOUT),
    new UBCfgPropTableEntry(BROKERLOGFILE,         N_BROKERLOGFILE),
    new UBCfgPropTableEntry(CLASSMAIN,             N_CLASSMAIN),
    new UBCfgPropTableEntry(COMPONENTS,            N_COMPONENTS),
    new UBCfgPropTableEntry(CONTROLLINGNAMESERVER, N_CONTROLLINGNAMESERVER),
    new UBCfgPropTableEntry(DEBUGGERENABLED,       N_DEBUGGERENABLED),
    new UBCfgPropTableEntry(DEFAULTCOOKIEDOMAIN,   N_DEFAULTCOOKIEDOMAIN),
    new UBCfgPropTableEntry(DEFAULTCOOKIEPATH,     N_DEFAULTCOOKIEPATH),
    new UBCfgPropTableEntry(DEFAULTSERVICE,        N_DEFAULTSERVICE),
    new UBCfgPropTableEntry(DESCRIPTION,           N_DESCRIPTION),
    new UBCfgPropTableEntry(ENVIRONMENT,           N_ENVIRONMENT),
    new UBCfgPropTableEntry(GROUPNAME,             N_GROUPNAME),
    new UBCfgPropTableEntry(HOSTNAME,              N_HOSTNAME),
    new UBCfgPropTableEntry(HTTPSENABLED,          N_HTTPSENABLED),
    new UBCfgPropTableEntry(IDLETIMEOUT,           N_IDLETIMEOUT),
    new UBCfgPropTableEntry(IDLECONNTIMEOUT,       N_IDLECONNTIMEOUT),
    new UBCfgPropTableEntry(INFOVERSION,           N_INFOVERSION),
    new UBCfgPropTableEntry(INITIALSRVRINSTANCE,   N_INITIALSRVRINSTANCE),
    new UBCfgPropTableEntry(LOCATION_PROP,         N_LOCATION),
    new UBCfgPropTableEntry(LOGAPPEND,             N_LOGAPPEND),
    new UBCfgPropTableEntry(LOGFILEMODE,           N_LOGFILEMODE),
    new UBCfgPropTableEntry(LOGFILE,               N_LOGFILE),
    new UBCfgPropTableEntry(LOGGINGLEVEL,          N_LOGGINGLEVEL),
    new UBCfgPropTableEntry(MAXNSCLIENTPORT,       N_MAXNSCLIENTPORT),
    new UBCfgPropTableEntry(MAXSRVRINSTANCE,       N_MAXSRVRINSTANCE),
    new UBCfgPropTableEntry(MINNSCLIENTPORT,       N_MINNSCLIENTPORT),
    new UBCfgPropTableEntry(MINSRVRINSTANCE,       N_MINSRVRINSTANCE),
    new UBCfgPropTableEntry(MSNGREXECFILE,         N_MSNGREXECFILE),
    new UBCfgPropTableEntry(MSNGRSCRIPTFILE,       N_MSNGRSCRIPTFILE),
    new UBCfgPropTableEntry(MULTICASTGROUP,        N_MULTICASTGROUP),
    new UBCfgPropTableEntry(MULTICASTTTL,          N_MULTICASTTTL),
    new UBCfgPropTableEntry(NAMESERVER,            N_NAMESERVER),
    new UBCfgPropTableEntry(NEIGHBORNAMESERVERS,   N_NEIGHBORNAMESERVERS),
    new UBCfgPropTableEntry(NSCLIENTPORTRETRY,     N_NSCLIENTPORTRETRY),
    new UBCfgPropTableEntry(NSCLIENTPORTRETRYINT,  N_NSCLIENTPORTRETRYINT),
    new UBCfgPropTableEntry(ODBC,                  N_ODBC),
    new UBCfgPropTableEntry(OPERATINGMODE,         N_OPERATINGMODE),
    new UBCfgPropTableEntry(ORACLE,                N_ORACLE),
    new UBCfgPropTableEntry(PORTNUMBER,            N_PORTNUMBER),
    new UBCfgPropTableEntry(PRIORITYWEIGHT,        N_PRIORITYWEIGHT),
    new UBCfgPropTableEntry(PROPATH,               N_PROPATH),
    new UBCfgPropTableEntry(REGISTRATIONMODE,      N_REGISTRATIONMODE),  
    new UBCfgPropTableEntry(REGISTRATIONRETRY,     N_REGISTRATIONRETRY),
    new UBCfgPropTableEntry(ROOTPATH,              N_ROOTPATH),
    new UBCfgPropTableEntry(SCRIPTPATH,            N_SCRIPTPATH),
    new UBCfgPropTableEntry(SECUREPORT,            N_SECUREPORT),
    new UBCfgPropTableEntry(SRVRACTIVATEPROC,      N_SRVRACTIVATEPROC),
    new UBCfgPropTableEntry(SRVRAPPMODE,           N_SRVRAPPMODE),
    new UBCfgPropTableEntry(SRVRCONNECTPROC,       N_SRVRCONNECTPROC),
    new UBCfgPropTableEntry(SRVRDEACTIVATEPROC,    N_SRVRDEACTIVATEPROC),
    new UBCfgPropTableEntry(SRVRDEBUG,             N_SRVRDEBUG),
    new UBCfgPropTableEntry(SRVRDISCONNPROC,       N_SRVRDISCONNPROC),
    new UBCfgPropTableEntry(SRVREXECFILE,          N_SRVREXECFILE),
    new UBCfgPropTableEntry(SRVRLOGFILE,           N_SRVRLOGFILE),
    new UBCfgPropTableEntry(SRVRMAXPORT,           N_SRVRMAXPORT),
    new UBCfgPropTableEntry(SRVRMINPORT,           N_SRVRMINPORT),
    new UBCfgPropTableEntry(SRVRSHUTDOWNPROC,      N_SRVRSHUTDOWNPROC),
    new UBCfgPropTableEntry(SRVRSTARTUPPARAM,      N_SRVRSTARTUPPARAM),
    new UBCfgPropTableEntry(SRVRSTARTUPPROC,       N_SRVRSTARTUPPROC),
    new UBCfgPropTableEntry(SRVRSTARTUPPROCPARAM,  N_SRVRSTARTUPPROCPARAM),
    new UBCfgPropTableEntry(UUID_NUM,              N_UUID_NUM),
    new UBCfgPropTableEntry(USERNAME,              N_USERNAME),
    new UBCfgPropTableEntry(WEBSPEED,              N_WEBSPEED),
    new UBCfgPropTableEntry(WORKDIR,               N_WORKDIR),
    new UBCfgPropTableEntry(WSROOT,                N_WSROOT),
    new UBCfgPropTableEntry(COLLECTSTATSDATA,      N_COLLECTSTATSDATA),
    new UBCfgPropTableEntry(WSA_URL,               N_WSAURL),
    new UBCfgPropTableEntry(WEB_SERVER_AUTH,         N_WEBSERVERAUTH),
    new UBCfgPropTableEntry(PROXY_HOST,             N_PROXYHOST),
    new UBCfgPropTableEntry(PROXY_PASSWORD,         N_PROXYPASSWORD),
    new UBCfgPropTableEntry(PROXY_PORT,             N_PROXYPORT),
    new UBCfgPropTableEntry(PROXY_USERNAME,         N_PROXYUSERNAME),
    new UBCfgPropTableEntry(ADMINSOAPACTION,       N_ADMINSOAPACTION),
    new UBCfgPropTableEntry(HTTPERRORPAGE,         N_HTTPERRORPAGE),
    new UBCfgPropTableEntry(ENABLEWSDL,            N_ENABLEWSDL),
    new UBCfgPropTableEntry(ENABLEWSDLLISTINGS,    N_ENABLEWSDLLISTINGS),
    new UBCfgPropTableEntry(WSDLLISTINGPAGE,       N_WSDLLISTINGPAGE),
    new UBCfgPropTableEntry(NOWSDLPAGE,            N_NOWSDLPAGE),
    new UBCfgPropTableEntry(ADMINAUTH,             N_ADMINAUTH),
    new UBCfgPropTableEntry(WSDLAUTH,              N_WSDLAUTH),
    new UBCfgPropTableEntry(APPAUTH,               N_APPAUTH),
    new UBCfgPropTableEntry(ADMINROLES,            N_ADMINROLES),
    new UBCfgPropTableEntry(WEBAPPENABLED,         N_WEBAPPENABLED),
    new UBCfgPropTableEntry(ADMINENABLED,          N_ADMINENABLED),
    new UBCfgPropTableEntry(APPPROTOCOL,           N_APPPROTOCOL),
    new UBCfgPropTableEntry(DEBUGCLIENTS,          N_DEBUGCLIENTS),
    new UBCfgPropTableEntry(LOGMSGTHRESHOLD,       N_LOGMSGTHRESHOLD)
    

  };

    // Constant property values
    public static final String[] APPSERVER_VAL               = {"UBroker.AS"};
    public static final String[] COMPONENTS_VAL              = {"1111110"};
    public static final String[] INFOVERSION_VAL             = {"9010"};
    public static final String[] LOGFILEMODE_VAL             = {"New", "Append"};
    public static final String[] MULTICASTTTL_VAL            = {"16"};
    public static final String[] NAMESERVER_VAL              = {"NameServer"};
    public static final String[] ODBC_VAL                    = {"UBroker.OD"};
    public static final String[] OPERATINGMODE_VAL           = {"Stateless","State-aware", "State-reset", "State-free"};
    public static final String[] ORACLE_VAL                  = {"Broker.OR"};
    public static final String[] REGISTRATIONMODE_VAL        = {"Register-IP", "Register-LocalHost",
                                                                "Register-HostName"};
    public static final String[] SRVRAPPMODE_VAL             = {"Production", "Development"};
    public static final String[] SRVRDEBUG_VAL               = {"Enabled", "Disabled"};
    public static final String[] WEBSPEED_VAL                = {"UBroker.WS"};
    
    // the following list represents the property file requirements for OR and OD
    //  note that 'X' denotes a placeholder value 
    public static final String[][] OROD_STARTUP_LST         =  {{"-svub",""},   {"-S", "X"},
                                                                {"-N",   "TCP"},{"-U", "X"},
                                                                {"-P",   "X"},  {"-P", "X"},           
                                                                {"-hs",  "0"},  {"-s", "40"}};
    public static final String OROD_DEFAULT                 = "X";
    public static final String ORACLE_HOME                  = "ORACLE_HOME";
    public static final String ORACLE_HOME_DEFAULT          = "C:\\orant";
    public static final String ORACLE_SID                   = "ORACLE_SID";
    public static final String ORACLE_SID_DEFAULT           = "ORCL";
    public static final String ODBC_HOME                    = "ODBC_HOME";
    public static final String ODBC_HOME_NT_DEFAULT         = "/usr/odbnt";
    public static final String ODBC_HOME_UX_DEFAULT         = "/usr/odbunx";

    
    // value/number pairs
     //property name/number table
    public static final UBCfgValTableEntry[] propNumToValTable =
  {
    new UBCfgValTableEntry(N_APPSERVER,           APPSERVER_VAL),
    new UBCfgValTableEntry(N_COMPONENTS,          COMPONENTS_VAL),
    new UBCfgValTableEntry(N_INFOVERSION,         INFOVERSION_VAL),
    new UBCfgValTableEntry(N_LOGFILEMODE,         LOGFILEMODE_VAL),
    new UBCfgValTableEntry(N_MULTICASTTTL,        MULTICASTTTL_VAL),
    new UBCfgValTableEntry(N_NAMESERVER,          NAMESERVER_VAL),
    new UBCfgValTableEntry(N_ODBC,                ODBC_VAL),
    new UBCfgValTableEntry(N_OPERATINGMODE,       OPERATINGMODE_VAL),
    new UBCfgValTableEntry(N_ORACLE,              ORACLE_VAL),
    new UBCfgValTableEntry(N_REGISTRATIONMODE,    REGISTRATIONMODE_VAL),
    new UBCfgValTableEntry(N_SRVRAPPMODE,         SRVRAPPMODE_VAL),
    new UBCfgValTableEntry(N_SRVRDEBUG,           SRVRDEBUG_VAL),
    new UBCfgValTableEntry(N_WEBSPEED,            WEBSPEED_VAL),
  };

  // required properties flags locators
  public static final int FOUND_PORT                    = 0;
  public static final int FOUND_SRVRSTARTUPPARAM        = 1;
  public static final int FOUND_LOGGINGLEVEL            = 2;
  public static final int FOUND_LOGAPPEND               = 3;
  public static final int FOUND_LOGFILENAME             = 4;
  public static final int FOUND_WEBAPPENABLED           = 5;
  public static final int FOUND_ADMINENABLED            = 6;
  public static final int FOUND_ENABLEWSDL              = 7;
  public static final int FOUND_WSAURL                  = 8;
  



} //end ICfgConst class



/* end of ICfgConst */





