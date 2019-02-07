/*************************************************************/
/* Copyright (c) 1984-2011 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/* Module : ubProperties                                             */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.progress.common.ehnlog.AppLogger;
import com.progress.common.ehnlog.DefaultLogHandler;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.exception.ExceptionMessageAdapter;
import com.progress.common.exception.ProException;
import com.progress.common.licensemgr.LicenseMgr;
import com.progress.common.networkevents.EventBroker;
import com.progress.common.property.PropertyManager;
import com.progress.common.util.Environment;
import com.progress.common.util.Getopt;
import com.progress.common.util.InstallPath;
import com.progress.common.util.PromsgsFile;
import com.progress.common.util.PropertyFilter;
import com.progress.message.jbMsg;
import com.progress.ubroker.broker.JavaServices;
import com.progress.ubroker.broker.ubListenerThread;
import com.progress.ubroker.debugger.ubDebuggerThread;
import com.progress.ubroker.ssl.ClientParams;
import com.progress.ubroker.tools.UBToolsMsg;

/*********************************************************************/
/*                                                                   */
/* Class ubProperties                                                */
/*                                                                   */
/*********************************************************************/

public class ubProperties
    extends PropertyManager
    implements ubConstants
{


/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

/*********************************************************************/
/* Property names                                                    */
/*********************************************************************/

/*********************************************************************/
/* The following constants are used to access the property value.    */
/*                                                                   */
/* To add a new property, first add a new definition of the property */
/* to the schema.  Be sure to include a default value in the new     */
/* schema definition.  Then add a new constant that defines the name */
/* of the new property to the list below.  To access the value of    */
/* the new property, use one of the following methods in this class: */
/*                                                                   */
/*  getValueAsString(ubProperties.PROPNAME_xxx)                      */
/*                             value of property xxx as String       */
/*  getValueAsInt(ubProperties.PROPNAME_xxx)                         */
/*                             value of property xxx as int          */
/*  getValueAsBoolean(ubProperties.PROPNAME_xxx)                     */
/*                             value of property xxx as boolean      */
/*  getValueAsStringArray(ubProperties.PROPNAME_xxx)                 */
/*                             value of property xxx as String[]     */
/*                                                                   */
/* If the property is not specified in the property file, the default*/
/* value specified in the schema will be used as the property value. */
/*                                                                   */
/* If the property can be dynamically updated, add a new "update_xxx"*/
/* routine at the end of this file that updates the property.  See   */
/* the comments at the end of this file for details.                 */
/*********************************************************************/

public static final String PROPNAME_PORTNUM        = "portNumber";
public static final String PROPNAME_REGMODE        = "registrationMode";
public static final String PROPNAME_HOSTNAME       = "hostName";
public static final String PROPNAME_MINSERVERS     = "minSrvrInstance";
public static final String PROPNAME_MAXSERVERS     = "maxSrvrInstance";
public static final String PROPNAME_INITIALSERVERS = "initialSrvrInstance";
public static final String PROPNAME_SERVERMODE     = "operatingMode";
public static final String PROPNAME_SERVERPARMS    = "srvrStartupParam";
public static final String PROPNAME_QUEUELIMIT     = "queueLimit";
public static final String PROPNAME_LOGFILENAME    = "logFile";
public static final String PROPNAME_LOGGINGLEVEL   = "loggingLevel";
public static final String PROPNAME_BRKRLOGGINGLEVEL  = "brkrLoggingLevel";
public static final String PROPNAME_BRKRLOGAPPEND     = "brkrLogAppend";
public static final String PROPNAME_BRKRLOGTHRESHOLD  = "brkrLogThreshold";
public static final String PROPNAME_BRKRLOGENTRYTYPES = "brkrLogEntryTypes";
public static final String PROPNAME_BRKRNUMLOGFILES   = "brkrNumLogFiles";
public static final String PROPNAME_SRVRLOGGINGLEVEL  = "srvrLoggingLevel";
public static final String PROPNAME_SRVRLOGAPPEND     = "srvrLogAppend";
public static final String PROPNAME_SRVRLOGTHRESHOLD  = "srvrLogThreshold";
public static final String PROPNAME_SRVRLOGENTRYTYPES = "srvrLogEntryTypes";
public static final String PROPNAME_SRVRNUMLOGFILES   = "srvrNumLogFiles";
public static final String PROPNAME_SRVRSELECTIONSCHEME = "srvrSelectionScheme";
public static final String PROPNAME_SRVRLOGFILEWATCHDOG = "srvrLogWatchdogInterval";
public static final String PROPNAME_LOGFILEMODE    = "logFileMode";
public static final String PROPNAME_MAXCLIENTS     = "maxClientInstance";
public static final String PROPNAME_USERNAME       = "userName";
public static final String PROPNAME_PASSWORD       = "userPassword";
public static final String PROPNAME_WORKINGDIR     = "workDir";
public static final String PROPNAME_SERVERLOGFILE  = "srvrLogFile";
public static final String PROPNAME_BROKERLOGFILE  = "brokerLogFile";
public static final String PROPNAME_SERVERSTARTUPPARMS = "serverStartupParms";
public static final String PROPNAME_MINSERVERPORT  = "srvrMinPort";
public static final String PROPNAME_MAXSERVERPORT  = "srvrMaxPort";
public static final String PROPNAME_SERVEREXENAME  = "srvrExecFile";
public static final String PROPNAME_SERVERSTARTUPPROC    = "srvrStartupProc";
public static final String PROPNAME_SERVERSHUTDOWNPROC   = "srvrShutdownProc";
public static final String PROPNAME_SERVERCONNECTIONPROC = "srvrConnectProc";
public static final String PROPNAME_SERVERDISCONNECTPROC = "srvrDisconnProc";
public static final String PROPNAME_SERVERACTIVATEPROC   = "srvrActivateProc";
public static final String PROPNAME_SERVERDEACTIVATEPROC = "srvrDeactivateProc";
public static final String PROPNAME_CONTROLLINGNS    = "controllingNameServer";
public static final String PROPNAME_REGNAMESERVER    = "registerNameServer";
public static final String PROPNAME_UUID             = "uuid";
public static final String PROPNAME_NSWEIGHT         = "priorityWeight";
public static final String PROPNAME_NSTIMEOUT        = "registrationRetry";
public static final String PROPNAME_DEFAULTSERVICE   = "defaultService";
public static final String PROPNAME_APPLSERVICENAMES = "appserviceNameList";
public static final String PROPNAME_ASKCAPABILITIES  = "appServerKeepaliveCapabilities";
public static final String PROPNAME_SASKACTIVITYTIMEOUT = "serverASKActivityTimeout";
public static final String PROPNAME_SASKRESPONSETIMEOUT = "serverASKResponseTimeout";
public static final String PROPNAME_SERVERLIFESPAN      = "serverLifespan";
public static final String PROPNAME_SERVERLIFESPANPADDING = "serverLifespanPadding";
public static final String PROPNAME_BRKRSPININTERVAL   = "brkrSpinInterval";
public static final String PROPNAME_SRVRSTARTUPTIMEOUT = "srvrStartupTimeout";
public static final String PROPNAME_REQUESTTIMEOUT     = "requestTimeout";
public static final String PROPNAME_CONNECTINGTIMEOUT  = "connectingTimeout";
public static final String PROPNAME_AUTOTRIMTIMEOUT    = "autoTrimTimeout";
public static final String PROPNAME_IPVER = "ipver";
private static final String PROPNAME_ALLOWRUNTIMEUPDATES = "allowRuntimeUpdates";
public static final String PROPNAME_AGENTDETAILTIMEOUT = "agentDetailTimeout";
public static final String PROPNAME_BRKRNETBIOSENABLE = "brkrNetbiosEnable";
public static final String PROPNAME_KILLAGENTAFTERTIMEOUT = "killAgentAfterTimeOut";

/* properties not in the schema */
public static final String PROPNAME_MAXIDLESERVERS = "maxIdleServers";
public static final String PROPNAME_MINIDLESERVERS = "minIdleServers";
public static final String PROPNAME_SOTIMEOUT      = "soTimeout";
public static final String PROPNAME_RMIWATCHDOGINTERVAL    = "rmiWatchdogInterval";
public static final String PROPNAME_PARENTWATCHDOGINTERVAL = "parentWatchdogInterval";
public static final String PROPNAME_WATCHDOGTHREADPRIORITY = "watchdogThreadPriority";
public static final String PROPNAME_CLIENTTHREADPRIORITY   = "clientThreadPriority";
public static final String PROPNAME_SERVERTHREADPRIORITY   = "serverThreadPriority";
public static final String PROPNAME_LISTENERTHREADPRIORITY = "listenerThreadPriority";

/* SSL properties */
public static final String PROPNAME_SSLENABLE        = "sslEnable";
public static final String PROPNAME_CERTSTOREPATH    = "certStorePath";
public static final String PROPNAME_KEYALIAS         = "keyAlias";
public static final String PROPNAME_KEYALIASPASSWD   = "keyAliasPasswd";
public static final String PROPNAME_KEYSTOREPATH     = "keyStorePath";
public static final String PROPNAME_KEYSTOREPASSWD   = "keyStorePasswd";
public static final String PROPNAME_NOSESSIONCACHE   = "noSessionCache";
public static final String PROPNAME_SESSIONTIMEOUT   = "sessionTimeout";
public static final String PROPNAME_SSLALGORITHMS    = "sslAlgorithms";

/* name server interface */
public static final String PROPNAME_NSHOST           = "hostName";
public static final String PROPNAME_NSPORTNUM        = "portNumber";

/* adapter server threads */
public static final String PROPNAME_MINADPTRTHREADS  = "minAdptrThreads";
public static final String PROPNAME_MAXADPTRTHREADS  = "maxAdptrThreads";
public static final String PROPNAME_INITADPTRTHREADS = "initialAdptrThreads";
public static final String PROPNAME_CLASSMAIN        = "classMain";

/* additions to AdminServerPlugins.properties */
public static final String PROPNAME_MQJVMEXE          = "jvmexe";
public static final String PROPNAME_MQJVMARGS         = "jvmargs";
public static final String PROPNAME_MQSECURITYPOLICY  = "policyfile";
public static final String PROPNAME_MQCLASSPATH       = "pluginclasspath";
public static final String PROPNAME_MQSTARTUPPARMS    = "mqStartupParms";

/* additions to [UBroker] section for starting MQ ServerConnect Adapter */
public static final String PROPNAME_MQPORT              = "mqPort";
public static final String PROPNAME_MQPID               = "mqPid";
public static final String PROPNAME_MQBRKRLOGAPPEND     = "mqBrkrLogAppend";
public static final String PROPNAME_MQENABLE            = "mqEnabled";
public static final String PROPNAME_MQBRKRLOGENTRYTYPES = "mqBrkrLogEntryTypes";
public static final String PROPNAME_MQBRKRLOGGINGLEVEL  = "mqBrkrLoggingLevel";
public static final String PROPNAME_MQBRKRLOGTHRESHOLD  = "mqBrkrLogThreshold";
public static final String PROPNAME_MQBRKRNUMLOGFILES   = "mqBrkrNumLogFiles";
public static final String PROPNAME_MQBROKERLOGFILE     = "mqBrokerLogFile";
public static final String PROPNAME_MQSRVRLOGAPPEND     = "mqSrvrLogAppend";
public static final String PROPNAME_MQSRVRLOGENTRYTYPES = "mqSrvrLogEntryTypes";
public static final String PROPNAME_MQSRVRLOGGINGLEVEL  = "mqSrvrLoggingLevel";
public static final String PROPNAME_MQSRVRLOGTHRESHOLD  = "mqSrvrLogThreshold";
public static final String PROPNAME_MQSRVRNUMLOGFILES   = "mqSrvrNumLogFiles";
public static final String PROPNAME_MQSERVERLOGFILE     = "mqServerLogFile";

/* Fathom Data */
public static final String PROPNAME_COLLECT_STATS_DATA = "collectStatsData";

/* properties from the Preferences section */
public static final String PROPNAME_ADMRETRY         = "admSrvrRegisteredRetry";
public static final String PROPNAME_ADMRETRY_INTERVAL= "admSrvrRegisteredRetryInterval";

/* DataServer property */
public static final String PROPNAME_SRVRDSLOGFILE   = "srvrDSLogFile";

public static final String PROPNAME_DEBUGGER_ENABLED    = "debuggerEnabled";

/* Broker debugger properties */

public static final String PROPNAME_BROKER_DEBUGGER_ENABLED            = "brkrDebuggerEnabled";
public static final String PROPNAME_BROKER_DEBUGGER_PORT               = "brkrDebuggerPortNumber";
public static final String PROPNAME_BROKER_DEBUGGER_PASSPHRASE         = "brkrDebuggerPassphrase";
public static final String PROPNAME_BROKER_DEBUGGER_SSL_ENABLE         = "brkrDebuggerSSLEnable";
public static final String PROPNAME_BROKER_DEBUGGER_USE_BROKER_ALIAS   = "brkrDebuggerUseBrokerAlias";
public static final String PROPNAME_BROKER_DEBUGGER_KEYALIAS           = "brkrDebuggerKeyAlias";
public static final String PROPNAME_BROKER_DEBUGGER_KEYALIAS_PASSWORD  = "brkrDebuggerKeyAliasPassword";


/*********************************************************************/
/* End of property names                                             */
/*********************************************************************/



/* property file section names */
public static final String NAMESERVER_SECTION_NAME   = "NameServer";
public static final String PREFERENCES_SECTION_NAME  = "PreferenceRoot.Preference";
public static final String UBROKER_SECTION_NAME      = "UBroker";
public static final String ADAPTER_SECTION_NAME      = "Adapter";
public static final String ENVIRONMENT_SECTION_NAME      = "Environment";
public static final String ADMINSERVER_ADAPTER_SECTION_NAME = "PluginPolicy.Progress.SonicMQ";

/* default values used for public values and properties not in the schema */
public static final int    DEF_ADMRETRY          = 6;
public static final int    DEF_ADMRETRY_INTERVAL = 3000;
public static final int    DEF_SELECTIONSCHEME   = SELECTIONSCHEME_FIFO;
public static final int    DEF_QUEUE_LIMIT       = 0;
public static final int    DEF_SOTIMEOUT         = 0;
public static final int    DEF_BRKRSPININTERVAL  = 3000;/* in ms  */
public static final int    DEF_REQUESTTIMEOUT    = 15;  /* in sec */
public static final int    DEF_CONNECTINGTIMEOUT = 60;  /* in sec */
public static final int    DEF_SRVRSTARTUPTIMEOUT= 3;   /* in sec */
public static final int    DEF_LOGFILE_WATCHDOG  = 60;  /* in sec */
public static final int    DEF_CLIENTTHREADPRIORITY = CLIENT_THREAD_PRIORITY;
public static final int    DEF_SERVERTHREADPRIORITY = SERVER_THREAD_PRIORITY;
public static final int    DEF_LISTENERTHREADPRIORITY = SERVICE_PRIORITY;
public static final int    DEF_WATCHDOGTHREADPRIORITY = ubWatchDog.DEF_PRIORITY;
public static final int    DEF_PARENTWATCHDOGINTERVAL = 3000;  /* in ms */
public static final int    DEF_RMIWATCHDOGINTERVAL = 60;       /* in sec */
public static final String DEF_ASKCAPABILITIES  = "denyClientASK,denyServerASK";
public static final int    DEF_SASKACTIVITYTIMEOUT = 60;
public static final int    DEF_SASKRESPONSETIMEOUT = 60;
public static final int    MIN_SASKACTIVITYTIMEOUT = 30;
public static final int    MIN_SASKRESPONSETIMEOUT = 30;
public static final int    DEF_SERVERLIFESPAN = 0;
public static final int    DEF_SERVERLIFESPANPADDING = 5;
public static final int    DEF_IPVER = IPVER_IPV4;
public static final int    DEF_NUMLOGFILES = 3;
public static final String DEF_KILLAGENTAFTERTIMEOUT = "true";

/* invalid values */
private static final String INVALID_STRING        = null;
private static final int    INVALID_INT           = -1;

/* license limits */
private static final int RESTRICTED_SERVER_LICENSE_LIMIT = 2;
private static final int RESTRICTED_ADAPTER_SERVER_LICENSE_LIMIT = 30;

/* system properties relating to IPv6 support */
private static final String  SYSPROP_PREFERIPV4STACK= "java.net.preferIPv4Stack";
private static final String  SYSPROP_PREFERIPV6ADDRS= "java.net.preferIPv6Addresses";

private static final int[] INT_SERVER_MODES =
{
  SERVERMODE_STATELESS
, SERVERMODE_STATE_AWARE
, SERVERMODE_STATE_RESET
, SERVERMODE_STATE_FREE
};

private static final String[] STRING_SERVER_MODES =
{
  "Stateless"
, "State-aware"
, "State-reset"
, "State-free"
};

private static final int[] INT_REG_MODES =
{
  REGMODE_IP
, REGMODE_LOCALHOST
, REGMODE_HOSTNAME
};

private static final String[] STRING_REG_MODES =
{
  "Register-IP"
, "Register-LocalHost"
, "Register-HostName"
};

private static final int[] INT_SERVER_TYPES =
{
  SERVERTYPE_APPSERVER
, SERVERTYPE_WEBSPEED
, SERVERTYPE_DATASERVER_OD
, SERVERTYPE_DATASERVER_OR
, SERVERTYPE_ADAPTER
, SERVERTYPE_DATASERVER_MSS
, SERVERTYPE_ADAPTER_CC
, SERVERTYPE_ADAPTER_SC
};

private static final String[] STRING_SERVER_TYPES =
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

private static final int[] INT_IPVERSIONS =
{
  IPVER_IPV4
, IPVER_IPV6
};

private static final String[] STRING_IPVERSIONS =
{
  "IPv4"
, "IPv6"
};

private static final int LOGFILEMODE_NEWFILE = 0;
private static final int LOGFILEMODE_APPEND  = 1;

private static final String SEPARATOR = "|";

/* command line switches */
public static final char OPT_BROKERNAME = 'i';
public static final char OPT_PROPFILENAME = 'f';
public static final char OPT_SERVERTYPE = 't';
public static final char OPT_PORTNUM = 'p';
public static final char OPT_INSTALLDIR = 'd';
public static final char OPT_PARENTPID  = 'c';
public static final char OPT_MQLOGGINGPARAMS = 'm';
public static final char OPT_RMIURL = 'r';

public static final String VALID_OPTIONS = "i:f:t:r:p:d:c:m:";

/*********************************************************************/
/* Static Data                                                       */
/*********************************************************************/

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

public String propertiesfilename;
public String brokerName;
public int    portNum;
public int    regMode;
public int    serverType;
public int    serverMode;
public String controllingNS;
public String nsHost;
public int    nsPortnum;
public String dblquote;
public Environment env;
public int    brokerPid;
public String localHost;
public String canonicalName;
public int    parentPID;
public int    ipver;
public String rmiURL;
public boolean debuggerIsConnected;

private String installDir;
private String osName;
private String osVersion;
private String javaVersion;
private String javaClassPath;
private String userDirectory;
private String javaPreferIPv4Stack;
private String javaPreferIPv6Addresses;

/* SSL support */
private ClientParams sslClientParams;

/* data for support of dynamic property update */
private Hashtable activeProps;
private Vector changedPropList;
private String propGroupName;
private PropertyFilter propFilter;
private ubListenerThread listenerThread;
private ubDebuggerThread debuggerThread;

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public ubProperties()
{
    this(null);
}

public ubProperties(EventBroker eb)
{
    super (eb, false);

    propertiesfilename    = INVALID_STRING;
    brokerName            = INVALID_STRING;
    installDir            = INVALID_STRING;
    portNum               = INVALID_INT;
    regMode               = INVALID_INT;
    serverType            = INVALID_INT;
    serverMode            = INVALID_INT;
    rmiURL                = INVALID_STRING;

    controllingNS         = INVALID_STRING;
    nsHost                = INVALID_STRING;
    nsPortnum             = INVALID_INT;

    dblquote              = isNT() ? "\"" : "";
    propFilter            = new PropertyFilter(this);
    changedPropList       = new Vector();
    activeProps           = getHashtable();
    listenerThread        = null;
    debuggerThread		  = null;

    env                   = null;
    brokerPid             = INVALID_INT;
    localHost             = INVALID_STRING;

    osName                = INVALID_STRING;
    osVersion             = INVALID_STRING;
    javaVersion           = INVALID_STRING;
    javaClassPath         = INVALID_STRING;

    javaPreferIPv4Stack   = INVALID_STRING;
    javaPreferIPv6Addresses = INVALID_STRING;
    canonicalName         = INVALID_STRING;
    sslClientParams       = null;
    parentPID             = INVALID_INT;
    ipver                 = DEF_IPVER;
    }

/*********************************************************************/
/* Public Methods                                                    */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private IAppLogger processAdapterArgs(String[] args, IAppLogger log, String propGroup)
    {
    Getopt options = new Getopt(args);
    int opt;
    int tmp;

    if (portNum == INVALID_INT)  /* not overridden from cmd line */
        {
        if ((portNum = getIntProperty(
                    propGroup + PROPNAME_PORTNUM,
                INVALID_INT
                )) == INVALID_INT)
            missingProperty(log, PROPNAME_PORTNUM);
        }

    if ((serverMode = getEnumProperty(
                        getValueAsString(PROPNAME_SERVERMODE),
                        STRING_SERVER_MODES,
                        INT_SERVER_MODES,
            INVALID_INT
            )) == INVALID_INT)
       serverMode = SERVERMODE_STATELESS;
    
    /* override if we're a serverconnect adapter */
    if (serverType == SERVERTYPE_ADAPTER_SC)
        {
        putValueAsString(PROPNAME_SRVRLOGGINGLEVEL,  getValueAsString(PROPNAME_MQSRVRLOGGINGLEVEL));
        putValueAsString(PROPNAME_SRVRLOGAPPEND,     getValueAsString(PROPNAME_MQSRVRLOGAPPEND));
        putValueAsString(PROPNAME_SRVRLOGTHRESHOLD,  getValueAsString(PROPNAME_MQSRVRLOGTHRESHOLD));
        putValueAsString(PROPNAME_SRVRLOGENTRYTYPES, getValueAsString(PROPNAME_MQSRVRLOGENTRYTYPES));
        putValueAsString(PROPNAME_SRVRNUMLOGFILES,   getValueAsString(PROPNAME_MQSRVRNUMLOGFILES));
        putValueAsString(PROPNAME_SERVERLOGFILE,     getValueAsString(PROPNAME_MQSERVERLOGFILE));
        }
    else
    	{
        String serverlogfilename = getValueAsString(PROPNAME_SERVERLOGFILE);
        if (serverlogfilename.equals(INVALID_STRING))
            missingProperty(log, PROPNAME_SERVERLOGFILE);

        /* if we're a ClientConnect adapter and a valid parent pid has been */
        /* received, then we need to adapt the logfile name with the pid of */
        /* the parent since each parent will have its own adapter           */

        if ((serverType == SERVERTYPE_ADAPTER_CC)   &&
            (parentPID != INVALID_INT) )
            {
            serverlogfilename = bldCCLogFilename(serverlogfilename, parentPID);
            adjustServerLogFileValue(serverlogfilename);
            }
    	}

    setServerMode(SERVERMODE_STATELESS);
    putValueAsInt(PROPNAME_SRVRSELECTIONSCHEME, SELECTIONSCHEME_FIFO);
    putValueAsInt(PROPNAME_SRVRLOGFILEWATCHDOG, DEF_LOGFILE_WATCHDOG);
    putValueAsInt(PROPNAME_NSWEIGHT, 0);
    putValueAsInt(PROPNAME_DEFAULTSERVICE, 0);
    putValueAsInt(PROPNAME_MAXIDLESERVERS, getValueAsInt(PROPNAME_MAXADPTRTHREADS));
    putValueAsInt(PROPNAME_MINIDLESERVERS, getValueAsInt(PROPNAME_MINADPTRTHREADS));
    putValueAsInt(PROPNAME_MAXSERVERS,     getValueAsInt(PROPNAME_MAXADPTRTHREADS));
    putValueAsInt(PROPNAME_INITIALSERVERS, getValueAsInt(PROPNAME_INITADPTRTHREADS));

    // Values not in the schema for [Adapter]
    putValueAsInt(PROPNAME_SERVERLIFESPAN, DEF_SERVERLIFESPAN);
    putValueAsInt(PROPNAME_SERVERLIFESPANPADDING, DEF_SERVERLIFESPANPADDING);
    putValueAsInt(PROPNAME_QUEUELIMIT, DEF_QUEUE_LIMIT);
    putValueAsInt(PROPNAME_BRKRSPININTERVAL, DEF_BRKRSPININTERVAL);
    putValueAsInt(PROPNAME_SRVRSTARTUPTIMEOUT, DEF_SRVRSTARTUPTIMEOUT);
    putValueAsInt(PROPNAME_REQUESTTIMEOUT, DEF_REQUESTTIMEOUT);
    putValueAsInt(PROPNAME_CONNECTINGTIMEOUT, DEF_CONNECTINGTIMEOUT);
    putValueAsString(PROPNAME_KILLAGENTAFTERTIMEOUT, DEF_KILLAGENTAFTERTIMEOUT);

    canonicalName = System.getProperty("CanonicalName");

return log;
}


/*****************************************************************/
/*                                                               */
/*****************************************************************/

public IAppLogger processArgs(String[] args)
    {
    Getopt options = new Getopt(args);
    int opt;
    int tmp;
    int rmiBindRetry = INVALID_INT;
    int rmiBindRetryInterval = INVALID_INT;
    String propGroup;
    String nspropGroup;
    String prefpropGroup;
    String tmpServerType = null;
    IAppLogger log = null;

    // Log initial errors to System.err
    try
        {
        log = new AppLogger(new DefaultLogHandler(), 
                            IAppLogger.LOGGING_ERRORS, 
                            0,
                            UBrokerLogContext.DEFAULT_EXEC_ENV_ID,
                            "UBroker");
        }
    catch (IOException e)
        {
        log = new AppLogger();
        }

    if (log != null)
        {
        /* set properties for this applogger so logging is on and */
	/* we log messages to the screen*/
        log.setExecEnvId(UBrokerLogContext.DEFAULT_EXEC_ENV_ID);
        log.setLoggingLevel(IAppLogger.LOGGING_BASIC);
        log.setLogEntries(UBrokerLogContext.SUB_M_DEFAULT,
                          false,
                          new byte[IAppLogger.MAXLOGENTRIES]);
        }

    while ((opt = options.getOpt(VALID_OPTIONS)) != Getopt.NONOPT)
        {
        switch(opt)
            {
            case OPT_PROPFILENAME:
                propertiesfilename = options.getOptArg();
                break;

            case OPT_BROKERNAME:
                brokerName = options.getOptArg();
                break;

            case OPT_INSTALLDIR:
                installDir = options.getOptArg();
                break;

            case OPT_SERVERTYPE:
                tmpServerType = options.getOptArg();
                break;

            case OPT_RMIURL:
                rmiURL = options.getOptArg();
                break;

            case OPT_PORTNUM:
                try
                    {
                    tmp = Integer.parseInt(options.getOptArg());
                    portNum = tmp;
                    }
                catch (NumberFormatException e)
                    {
                    /* Specified port number is not numeric
                       : %s<NumberFormatExceptionMsg_string>. */

                    fatalError(log,
                               jbMsg.jbMSG092,
                               new Object[] { e.getMessage() }
                              );
                    }
                break;

            case OPT_PARENTPID:
                try
                    {
                    tmp = Integer.parseInt(options.getOptArg());
                    parentPID = tmp;
                    }
                catch (NumberFormatException e)
                    {
                    /* Specified port number is not numeric
                       : %s<NumberFormatExceptionMsg_string>. */

                    /* NEED TO ADD NEW MESSAGE */
                	fatalError(log,
                               "Specified parent PID is not numeric : " +
                               e.getMessage()
                              );
                    }
                break;

            case OPT_MQLOGGINGPARAMS:
                unpackSCLoggingProperties(options.getOptArg());
                break;

            case Getopt.UNKOPT  :
            default             :
                /* Error processing command : %s<command> */
                fatalError(log,
                           jbMsg.jbMSG093,
                           new Object[] { options.getOptArg() }
                          );
                break;
            }
        }

    /* set the installDir system property */
    /* to do this, we have to fetch the whole mess, and update */
    /* the value for Install.Dir, and reset the them           */

    if (installDir != INVALID_STRING)
        {
        Properties sp = System.getProperties();
        sp.put("Install.Dir", installDir);
        System.setProperties(sp);
        }
    else
        {
        installDir = System.getProperty("Install.Dir");
        }

    canonicalName = System.getProperty("CanonicalName");

    if (DEBUG)
    {
    try
        {
        ExceptionMessageAdapter.setMessageSubsystem(new PromsgsFile());
        }
    catch (PromsgsFile.PromsgsFileIOException ioe)
        {
        fatalError("Unable to load promsgs file : " +
                           ioe + " : " + ioe.getMessage());
        }
    }

    /* Read in property file ... get fqn if not entered from cmdline */
    if (propertiesfilename == INVALID_STRING)
        {
        propertiesfilename = PropFilename.getFullPath();
        }

    /* check to see if the -i parameter is defined */
    if (brokerName == INVALID_STRING)
        {
        /* Required -i <brokerName> argument not specified. */
        fatalError(log,
                   jbMsg.jbMSG094,
                   new Object[] { }
                  );
        }

    /* check to see if the -t parameter is defined */
    if (tmpServerType == INVALID_STRING)
        {
        /* Required -t <serverType> argument not specified. */
        fatalError(log,
                   jbMsg.jbMSG095,
                   new Object[] { }
                  );
        }

    /* check to see if the -t parameter is "reasonable" */
    if (( serverType = getEnumProperty(
                                tmpServerType,
                                STRING_SERVER_TYPES,
                                INT_SERVER_TYPES,
                                INVALID_INT
                            )) == INVALID_INT)
        {
        /* Unknown serverType (%s<serverType>) specified. */
        fatalError(log,
                   jbMsg.jbMSG096,
                   new Object[] { tmpServerType }
                  );
        }

    /* make sure we got a -m parameter if we're a serverconnect adapter */
    if ((serverType == SERVERTYPE_ADAPTER_SC) &&
        (getValueAsString(PROPNAME_MQBROKERLOGFILE) == INVALID_STRING))
        {
    	/* "The -m parameter is missing for adaptertype = SC. */
    	fatalError(log,
                   jbMsg.jbMSG135,
                   new Object[] { });
        }

    /* set The filter */
    setGetPropertyFilter(propFilter);

    /* load the configuration file */
    try
        {        
        /* load the schema so we can pick up schema-based defaults */
        if ((serverType != SERVERTYPE_ADAPTER_SC) &&
            (serverType != SERVERTYPE_ADAPTER_CC))
            {
            loadSchema( "ubroker.schema" );
            }
        else
            {
            unrestrictRootGroups();// JavaTools.properties has other groups we don't need
            loadSchema( "adapter.schema" );
            }

        load(propertiesfilename);
        }
    catch (ProException e)
        {
        /* Error loading properties file (%s<filename>)
                %s<ProException_string> : %s<ProExceptionMsg_string> */
        fatalError(log,
                   jbMsg.jbMSG097,
                   new Object[] { propertiesfilename,
                                  e.toString(),
                                  e.getMessage() }
                  );
        }

    /* create the section name from broker prefix and type */
    if (tmpServerType.equals("AD"))
        propGroup = ADAPTER_SECTION_NAME;
    else
	if ( (tmpServerType.equals("CC")) ||
              (tmpServerType.equals("SC"))  )
            propGroup = ADAPTER_SECTION_NAME + "." + tmpServerType;
        else
            propGroup = UBROKER_SECTION_NAME + "." + tmpServerType;
 
    try{
    	String envSection = ENVIRONMENT_SECTION_NAME + "." + brokerName;
    	checkValidEnvSection(log, envSection);
        activeProps = cloneProperties(envSection);
    }
    catch(NoSuchGroupException exp) {
    	//TO DO print some error
    }
    
    propGroup += "." + brokerName;
    checkValidSection(log, propGroup);

    propGroupName = propGroup;
    propGroup += ".";

    /*
     * Create the hashtable that will hold the "active" properties
     */
    activeProps = cloneProperties(propGroupName);
    //activeProps = cloneProperties(ENVIRONMENT_SECTION_NAME + "." + brokerName);
    if (activeProps == null)
        /* Error loading properties file (%s<filename>)
        %s<ProException_string> : %s<ProExceptionMsg_string> */
	fatalError(log,
           jbMsg.jbMSG097,
           new Object[] { propertiesfilename }
          );

    /* first, we open the log file to record possible config errors */

    if (serverType == SERVERTYPE_ADAPTER_SC)
        {
        putValueAsString(PROPNAME_BRKRLOGGINGLEVEL,  getValueAsString(PROPNAME_MQBRKRLOGGINGLEVEL));
        putValueAsString(PROPNAME_BRKRLOGAPPEND,     getValueAsString(PROPNAME_MQBRKRLOGAPPEND));
        putValueAsString(PROPNAME_BRKRLOGTHRESHOLD,  getValueAsString(PROPNAME_MQBRKRLOGTHRESHOLD));
        putValueAsString(PROPNAME_BRKRLOGENTRYTYPES, getValueAsString(PROPNAME_MQBRKRLOGENTRYTYPES));
        putValueAsString(PROPNAME_BRKRNUMLOGFILES,   getValueAsString(PROPNAME_MQBRKRNUMLOGFILES));
        putValueAsString(PROPNAME_BROKERLOGFILE,     getValueAsString(PROPNAME_MQBROKERLOGFILE));
        }
    else
        {
	String brokerlogfilename = getValueAsString(PROPNAME_BROKERLOGFILE);
        if (brokerlogfilename.equals(INVALID_STRING))
            missingProperty(log, PROPNAME_BROKERLOGFILE);

        /* if we're a ClientConnect adapter and a valid parent pid has been */
    	/* received, then we need to adapt the logfile name with the pid of */
    	/* the parent since each parent will have its own adapter           */

        if ((serverType == SERVERTYPE_ADAPTER_CC)   &&
            (parentPID != INVALID_INT) )
            {
            brokerlogfilename = bldCCLogFilename(brokerlogfilename, parentPID);
            putValueAsString(PROPNAME_BROKERLOGFILE, brokerlogfilename);
            }
        }

    /* open up the logfile */
    try
        {
        IAppLogger tmplog = new AppLogger(getValueAsString(PROPNAME_BROKERLOGFILE),
                                          getValueAsInt(PROPNAME_BRKRLOGAPPEND),
                                          getValueAsInt(PROPNAME_BRKRLOGGINGLEVEL),
                                          getValueAsInt(PROPNAME_BRKRLOGTHRESHOLD),
                                          getValueAsInt(PROPNAME_BRKRNUMLOGFILES),
                                          getValueAsString(PROPNAME_BRKRLOGENTRYTYPES),
                                          UBrokerLogContext.DEFAULT_EXEC_ENV_ID,
                                          "UBroker");

        /* hang onto new log object if no exception occurred */
        log = tmplog;
        }
    catch (IOException e)
        {
        /* Could not open %s<brokerlogfilename> : %s<IOException_string>
           : %s<IOExceptionMsg_string> */
        fatalError(log,
                   jbMsg.jbMSG098,
                   new Object[] { getValueAsString(PROPNAME_BROKERLOGFILE),
                                  e.toString(),
                                  e.getMessage() }
                  );
        }

    if ((tmpServerType.equals("AD")) ||
        (tmpServerType.equals("CC")) ||
        (tmpServerType.equals("SC")))
        {
        /* Call our processAdapterArgs() routine here, then bail to the
         * point in the orginal processArgs() function where we need
         * to proceed from.
         */
        processAdapterArgs(args, log, propGroup);

        /* make sure that parent PID has been specified   */
        /* when when we're a mqConnect adapter            */

    	if ( ((serverType == SERVERTYPE_ADAPTER_CC)   ||
    	      (serverType == SERVERTYPE_ADAPTER_SC))  &&
             (parentPID == INVALID_INT) )
            {
            /*  The parent PID parameter (-c) was not specified for -t =
                               %s<serverType> */
    	    fatalError(log,
                       jbMsg.jbMSG136,
                       new Object[] { tmpServerType }
                      );
            }

        /* make sure that parent PID is not been specified */
        /* unless we're a mqConnect adapter                */

    	if ( (serverType != SERVERTYPE_ADAPTER_CC)   &&
    	     (serverType != SERVERTYPE_ADAPTER_SC)   &&
             (parentPID != INVALID_INT) )
            {
            /* An unexpected parent PID parameter (-c) was specified for -t
                          = %s<serverType> */
    	    fatalError(log,
                       jbMsg.jbMSG137,
                       new Object[] { tmpServerType }
                  );
            }

    	/* if we're a *connect adapter, then force the portNum to zero.  this */
    	/* will cause us to dynamically generate a random listener port later */
    	if ( (serverType == SERVERTYPE_ADAPTER_CC)   ||
      	     (serverType == SERVERTYPE_ADAPTER_SC)   )
            {
            portNum = 0;
            putValueAsInt(PROPNAME_PORTNUM, 0);
            }
        }
else  // not SonicMQ Adapter
    {
    if (portNum == INVALID_INT)  /* not overridden from cmd line */
    {
        if ((portNum = getIntProperty(
                    propGroup + PROPNAME_PORTNUM,
                INVALID_INT
                )) == INVALID_INT)
            missingProperty(log, PROPNAME_PORTNUM);
    }

    if ((serverMode = getEnumProperty(
                        getValueAsString(PROPNAME_SERVERMODE),
                        STRING_SERVER_MODES,
                        INT_SERVER_MODES,
            INVALID_INT
            )) == INVALID_INT)
        {
        /* Unknown %s<propName_ServerMode> (%s<serverMode>) specified. */
        fatalError(log,
                   jbMsg.jbMSG099,
                   new Object[] { PROPNAME_SERVERMODE, getValueAsString(PROPNAME_SERVERMODE) }
                  );
        }


    /* MQServerConnect properties */

    /* load the properties from the AdminServerPlugins.properties file */
    /* if MQ ServerConnect is enabled                                  */
    if (getValueAsInt(PROPNAME_MQENABLE) > 0)
        {
        processAdminServerPluginsArgs(
                            bldAdminServerPluginsFilename(propertiesfilename),
                            ADMINSERVER_ADAPTER_SECTION_NAME,
                            log);
        String mqStartupParms = bldServerConnectArgs(propertiesfilename);
        putValueAsString(PROPNAME_MQSTARTUPPARMS, mqStartupParms);
        }
    
    /* these are not in the schema */
    putValueAsInt(PROPNAME_MAXIDLESERVERS, getValueAsInt(PROPNAME_MAXSERVERS));
    putValueAsInt(PROPNAME_MINIDLESERVERS, getValueAsInt(PROPNAME_MINSERVERS));

    } /* END of !ADAPTER */

    if ((ipver = getEnumProperty(
            getValueAsString(PROPNAME_IPVER),
            STRING_IPVERSIONS,
            INT_IPVERSIONS,
            INVALID_INT
        )) == INVALID_INT)
       {
       /* Unknown %s<propName_ipver> (%s<ipver>) specified. */
       fatalError(log,
                  jbMsg.jbMSG161, 
                  new Object[] { PROPNAME_IPVER, getValueAsString(PROPNAME_IPVER) }
                  );
       }

    if ((regMode = getEnumProperty(
            getValueAsString(PROPNAME_REGMODE),
            STRING_REG_MODES,
            INT_REG_MODES,
            INVALID_INT
        )) == INVALID_INT)
        {
        /* Unknown %s<propName_ServerMode> (%s<serverMode>) specified. */
        fatalError(log,
                   jbMsg.jbMSG099,
                   new Object[] { PROPNAME_REGMODE, getValueAsString(PROPNAME_REGMODE) }
                  );
        }

    /* NameServer stuff */

    /* first get the NameServer name from the broker definition */
    if ((controllingNS = getProperty(
                propGroup + PROPNAME_CONTROLLINGNS,
                INVALID_STRING
            )) == INVALID_STRING)
        missingProperty(log, PROPNAME_CONTROLLINGNS);


    if ((tmp = getIntProperty(
                propGroup + PROPNAME_REGNAMESERVER,
                INVALID_INT
            )) == INVALID_INT)
        missingProperty(log, PROPNAME_REGNAMESERVER);


    /* The controllingNS is an optional parameter now 11/5/01 */

    if ((controllingNS.length() > 0) && (tmp != 0))
        {
        /* check that the NameServer section exits */
        checkValidSection(log, NAMESERVER_SECTION_NAME + "." + controllingNS);

        /* create the section name from NameServer prefix and type */
        nspropGroup = NAMESERVER_SECTION_NAME + "." + controllingNS + ".";

        if ((nsHost = getProperty(
                        nspropGroup + PROPNAME_NSHOST,
                    INVALID_STRING
                    )) == INVALID_STRING)
            missingProperty(log, PROPNAME_NSHOST);

        if ((nsPortnum = getIntProperty(
                        nspropGroup + PROPNAME_NSPORTNUM,
                    INVALID_INT
                    )) == INVALID_INT)
            missingProperty(log, PROPNAME_PORTNUM);
        }

    if ( (serverType == SERVERTYPE_ADAPTER_CC) ||
         (serverType == SERVERTYPE_ADAPTER_SC)  )
        {
    	/* set these to defaults since we're not using rmi for these adapters */
    	rmiBindRetry = DEF_ADMRETRY;
    	rmiBindRetryInterval = DEF_ADMRETRY_INTERVAL;
        }
    else
        {
        /* check that the Preferences section exits */
        checkValidSection(log, PREFERENCES_SECTION_NAME );

        /* create the section name from NameServer prefix and type */
        prefpropGroup = PREFERENCES_SECTION_NAME + ".";

        rmiBindRetry = getIntProperty(
                        prefpropGroup + PROPNAME_ADMRETRY,
                        DEF_ADMRETRY
                    );

        rmiBindRetryInterval = getIntProperty(
                        prefpropGroup + PROPNAME_ADMRETRY_INTERVAL,
                        DEF_ADMRETRY_INTERVAL
                    );
        }
    putValueAsInt(PROPNAME_ADMRETRY, rmiBindRetry);
    putValueAsInt(PROPNAME_ADMRETRY_INTERVAL, rmiBindRetryInterval);

    /* log some basic system information BUG# 20000807-009 */
    osName = System.getProperty("os.name");
    osVersion = System.getProperty("os.version");
    javaVersion = System.getProperty("java.version");
    javaClassPath = System.getProperty("java.class.path");
    userDirectory = System.getProperty("user.dir");

    /* to get IPv6 to work, we need to set some */
    /* system properties.  we only do this if   */
    /* the user has not already set the values  */

//  OE00156044  This does not work the same way on all platforms
//              so we are removing it.  if these properties are
//              needed, then they must be set on the cmd line
//              (or via the jvmArgs property)
//
//  adjustIPVerSystemProperties(log, ipver);
//
    /* capture these properties for logging */
    javaPreferIPv4Stack = System.getProperty(SYSPROP_PREFERIPV4STACK);
    javaPreferIPv6Addresses = System.getProperty(SYSPROP_PREFERIPV6ADDRS);

    /* AppServerKeepalive properties */

    if (serverType == SERVERTYPE_APPSERVER ||
  	serverType == SERVERTYPE_ADAPTER)
        {
        checkValidASKTimeouts(log);
        }
    else
        {
        putValueAsString(PROPNAME_ASKCAPABILITIES,  DEF_ASKCAPABILITIES); 
	putValueAsInt(PROPNAME_SASKACTIVITYTIMEOUT, DEF_SASKACTIVITYTIMEOUT); 
	putValueAsInt(PROPNAME_SASKRESPONSETIMEOUT, DEF_SASKRESPONSETIMEOUT); 
        }

    /* these parameters are not exposed to the user */
    /* (these are not in the schema) */
    putValueAsInt(PROPNAME_SOTIMEOUT, DEF_SOTIMEOUT);
    putValueAsInt(PROPNAME_PARENTWATCHDOGINTERVAL, DEF_PARENTWATCHDOGINTERVAL);
    putValueAsInt(PROPNAME_CLIENTTHREADPRIORITY,   DEF_CLIENTTHREADPRIORITY);
    putValueAsInt(PROPNAME_SERVERTHREADPRIORITY,   DEF_SERVERTHREADPRIORITY);
    putValueAsInt(PROPNAME_LISTENERTHREADPRIORITY, DEF_LISTENERTHREADPRIORITY);
    putValueAsInt(PROPNAME_WATCHDOGTHREADPRIORITY, DEF_WATCHDOGTHREADPRIORITY);
    if (getValueAsInt(PROPNAME_RMIWATCHDOGINTERVAL) == INVALID_INT)
        putValueAsInt(PROPNAME_RMIWATCHDOGINTERVAL,DEF_RMIWATCHDOGINTERVAL);

    /* make sure that the user is licensed to use the product */

    int maxServers = adjustMaxServerCnt(log, getValueAsInt(PROPNAME_MAXSERVERS));
    putValueAsInt(PROPNAME_MAXSERVERS, maxServers); 

    int initialServers = adjustInitialServerCnt(getValueAsInt(PROPNAME_INITIALSERVERS), maxServers);
    putValueAsInt(PROPNAME_INITIALSERVERS, initialServers); 

    /* for webspeed override the serverMode ... make  */
    /* it stateAware regardless of the propfile       */

    if (serverType == SERVERTYPE_WEBSPEED)
        {
	setServerMode(SERVERMODE_STATE_AWARE);
        }

    /* for dataservers override the serverMode .. make*/
    /* it stateAware regardless of the propfile       */
    if ((serverType == SERVERTYPE_DATASERVER_OD)  ||
        (serverType == SERVERTYPE_DATASERVER_OR)  ||
        (serverType == SERVERTYPE_DATASERVER_MSS)   )
        {
        setServerMode(SERVERMODE_STATE_AWARE);
        }

    /* set up the environment info */
    env                   = new Environment();
    brokerPid             = env.getCurrent_PID_JNI(0);

    localHost = brokerHostName(regMode, log);

    // SSL properties
    processSSLArgs( propGroup, log );

    /*
     * Enable the PropertyManager watchdog to watch for changes
     * to the properties file.
     * 
     * Dynamic updating of properties is only supported for
     * Appserver, WebSpeed, Dataservers, and the SonicMQ Broker Adapter.
     * 
     * It is not supported for ClientConnect or ServerConnect adapters 
     * since the MQxxx properties are passed on the command line when 
     * the adapter process is started.  In addition, any changed properties 
     * would apply to all xxxConnect adapter processes since there
     * is only one instance definition allowed in JavaTools.properties.
     */
    if ((serverType != SERVERTYPE_ADAPTER_SC) &&
        (serverType != SERVERTYPE_ADAPTER_CC) &&
	 getIntProperty(propGroup + PROPNAME_ALLOWRUNTIMEUPDATES, 0) != 0)
      startPropertyFileMonitor();   
    
    /* if we got this far, then we're ready to go ... return the logfile*/
    return log;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setServerMode(int serverMode)
    {
    this.serverMode = serverMode;
    putValueAsInt(PROPNAME_SERVERMODE, serverMode); 
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setListenerThread(ubListenerThread lt)
    {
    this.listenerThread = lt;
    }

public void setDebuggerThread(ubDebuggerThread dt) {
	this.debuggerThread = dt;
}

/*********************************************************************/
/*                                                                   */
/*********************************************************************/
public Hashtable list()
    {
    // all properties
    return this.list(null,null);
    }

public Hashtable list(String namePattern)
    {
    return this.list(namePattern,null);
    }

public Hashtable list(String namePattern, String valuePattern)
    {
    String dynFlag;
    String name;
    Property p;
    String value;
    Hashtable v = new Hashtable(activeProps.size());

    // for each property, add it to the vector if it matches the pattern
    for (Enumeration e = activeProps.keys() ; e.hasMoreElements() ;)
        {
        name = (String)e.nextElement();
        value = getValueAsString(name);
        p = (Property)activeProps.get(name.toLowerCase());

        // special cases
        if (name.equals(PROPNAME_REGMODE.toLowerCase()))
          value = STRING_REG_MODES[ getEnumProperty(getValueAsString(PROPNAME_REGMODE),
                                    STRING_REG_MODES,INT_REG_MODES,INVALID_INT)];

        if (name.equals(PROPNAME_BRKRLOGGINGLEVEL.toLowerCase()))
          value = getValueAsString(PROPNAME_BRKRLOGGINGLEVEL) +
                  " (0x" + Integer.toString(getValueAsInt(PROPNAME_BRKRLOGGINGLEVEL), 16) + ")";

        if (name.equals(PROPNAME_DEFAULTSERVICE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_DEFAULTSERVICE));
        
        if (name.equals(PROPNAME_SSLENABLE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_SSLENABLE));
        
        if (name.equals(PROPNAME_NOSESSIONCACHE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_NOSESSIONCACHE));
        
        if (name.equals(PROPNAME_REGNAMESERVER.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_REGNAMESERVER));
        
        /*
         *  If the property is dynamic, mark it.
         */
        if (isDynamicProperty(name))
            dynFlag = "(*)";
        else
            dynFlag = "";

        /*
         *  Format the property/value string and add it to the vector
         *  only if it matches the specified pattern.
         */
        try
            {
            if (namePattern == null && valuePattern == null)
                v.put(p.getName()+dynFlag, value);
            else
        	if (namePattern != null && p.getName().matches(namePattern))
                  v.put(p.getName()+dynFlag, value);
            else
        	if (valuePattern != null && value.matches(valuePattern))
                        v.put(p.getName()+dynFlag, value);
            }
        catch (Exception exp)
            {
            // bad pattern - don't add property
            }
        }

    return v;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void print(IAppLogger log, int lvl, int indexEntryType)
    {
    String name;
    Property p;
    String value;
    Vector v = new Vector(activeProps.size());

    // Output header and jvm information directly to the log file.

    log.logWithThisLevel(lvl,indexEntryType,
	  "**************************************");

    log.logWithThisLevel(lvl, indexEntryType,
          "Property Name        :  Property Value");

    log.logWithThisLevel(lvl, indexEntryType,
          "--------------------------------------");

    log.logWithThisLevel(lvl, indexEntryType,
          "os.name              : " + osName);

    log.logWithThisLevel(lvl, indexEntryType,
          "os.version           : " + osVersion);

    log.logWithThisLevel(lvl, indexEntryType,
          "java.version         : " + javaVersion);

    log.logWithThisLevel(lvl, indexEntryType,
          "java.class.path      : " + javaClassPath);

    log.logWithThisLevel(lvl, indexEntryType,
          "java.net.preferIPv4Stack    : " + javaPreferIPv4Stack);

    log.logWithThisLevel(lvl, indexEntryType,
          "java.net.preferIPv6Addresses : " + javaPreferIPv6Addresses);

    log.logWithThisLevel(lvl, indexEntryType,
          "user.dir             : " + userDirectory);

    /*
     * Create a Vector where each entry is one line of formatted
     * text to be output.  The Vector is then sorted and output
     * to the log file.
     */

    // First - add the information held in public storage
    v.add("installDir           : " + installDir);
    v.add("properties file      : " + propertiesfilename);
    v.add("broker               : " + brokerName);
    v.add("serverType           : " + STRING_SERVER_TYPES[serverType]);
    v.add("rmiURL               : " + rmiURL);
    v.add("nameServer host      : " + nsHost);
    v.add("nameServer port      : " + nsPortnum);
    v.add("localHost            : " + localHost);
    v.add("mqLoggingParams      : " + getMQLoggingParams());
        
    // Now add each property from the active property table
    for (Enumeration e = activeProps.keys() ; e.hasMoreElements() ;)
        {
        name = (String)e.nextElement();
        value = getValueAsString(name);
        p = (Property)activeProps.get(name.toLowerCase());

        // make sure all property values start on the same column.
        int column = 20;  // column to start values in
        StringBuilder sb = new StringBuilder(p.getName());
        sb.ensureCapacity(column);

        for (int len = column - name.length(); len > 0; len--)
            sb.append(" ");
        sb.append(" : ");


        // handle special formatting cases
        if (name.equals(PROPNAME_REGMODE.toLowerCase()))
            value = STRING_REG_MODES[ getEnumProperty(getValueAsString(PROPNAME_REGMODE),
                                            STRING_REG_MODES,INT_REG_MODES,INVALID_INT)];

        if (name.equals(PROPNAME_BRKRLOGGINGLEVEL.toLowerCase()))
            value = getValueAsString(PROPNAME_BRKRLOGGINGLEVEL) +
                    " (0x" + Integer.toString(getValueAsInt(PROPNAME_BRKRLOGGINGLEVEL), 16) + ")";

        if (name.equals(PROPNAME_DEFAULTSERVICE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_DEFAULTSERVICE));
        
        if (name.equals(PROPNAME_SSLENABLE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_SSLENABLE));
        
        if (name.equals(PROPNAME_NOSESSIONCACHE.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_NOSESSIONCACHE));
        
        if (name.equals(PROPNAME_REGNAMESERVER.toLowerCase()))
            value = Boolean.toString(getValueAsBoolean(PROPNAME_REGNAMESERVER));
        
        // format the property/value string and add it to the vector
        v.add(sb.toString() + value);
        }

    // Sort the vector and output the sorted list to the log file
    Collections.sort(v);

    for (int i=0; i < v.size(); i++)
        log.logWithThisLevel(lvl, indexEntryType, (String)v.elementAt(i));
    	
    log.logWithThisLevel(lvl,indexEntryType,
	  "*********************************************");
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getMQLoggingParams()
    {
    return packSCLoggingProperties();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String getServerTypeString()
    {
    return STRING_SERVER_TYPES[serverType];
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public IAppLogger initServerLog(IAppLogger brokerLog, boolean forceCreate)
    {
    IAppLogger serverLog;
    String serverlogfilename = getValueAsString(PROPNAME_SERVERLOGFILE);
    int srvrLogAppend = getValueAsInt(PROPNAME_SRVRLOGAPPEND);

    if ((serverType == SERVERTYPE_WEBSPEED) ||
        (serverType == SERVERTYPE_APPSERVER))
        {

         /* Application Server and WebSpeed */
         /*
          * if the log append flag is not on for the server's,
          * then delete the old logfile as we want to start a new one!
          */

          /* create instance of File */
          File f = new File(serverlogfilename);
          if (forceCreate || (srvrLogAppend == LOGFILEMODE_NEWFILE))
             {
             /* call method of delete on file instance */
             if (f.exists())
               {
               if (f.delete())
                  if (brokerLog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                      brokerLog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                         serverlogfilename + " REMOVED.");
               else
                  if (brokerLog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                      brokerLog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                         serverlogfilename + " NOT REMOVED.");
               }
             }

          /* create new file if it does not exist */
          try
            {
            f.createNewFile();
            }
          catch (IOException ioe)
            {
            if (brokerLog.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                brokerLog.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                   "Exception during creation of " + serverlogfilename +
                                   " " + ioe.getMessage());

            }
          serverLog = brokerLog;
        }
    else if ((serverType == SERVERTYPE_ADAPTER)    ||
             (serverType == SERVERTYPE_ADAPTER_CC) ||
             (serverType == SERVERTYPE_ADAPTER_SC) )
    {
        // In the adapter case, the JavaServices object takes care of
        //  opening and closing the server log
        serverLog = brokerLog;
    }
    else
	    serverLog = brokerLog;

    return serverLog;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String serverTypeString(int serverType)
    {
    String ret =
        ((serverType >= 0) && (serverType < STRING_SERVER_TYPES.length))
             ? STRING_SERVER_TYPES[serverType] : "";
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String serverModeString(int serverMode)
    {
    String ret =
        (serverType == SERVERTYPE_WEBSPEED) ? "Stateless" :
            ((serverMode >= 0) && (serverMode < STRING_SERVER_MODES.length))
                 ? STRING_SERVER_MODES[serverMode] : "";
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public String ipverString()
    {
    return ipverString(this.ipver);
    }

public String ipverString(int ipver)
    {
    String ret =
            ((ipver >= 0) && (ipver < STRING_IPVERSIONS.length))
                 ? STRING_IPVERSIONS[ipver] : "";
    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void adjustIPVerSystemProperties(IAppLogger log, int ipver)
    {
    /* get the current values of these properties */
    String s_preferIPv4Stack = System.getProperty(SYSPROP_PREFERIPV4STACK);
    String s_preferIPv6Addrs = System.getProperty(SYSPROP_PREFERIPV6ADDRS);
    String logmsg = "";

    /* we only adjust the system property if the user has not already */
    /* set it on the process startup                                  */

    switch(ipver)
        {
        case IPVER_IPV4:

            if (s_preferIPv4Stack == null)
                {
                System.setProperty(SYSPROP_PREFERIPV4STACK, "true");
                logmsg = SYSPROP_PREFERIPV4STACK + " property set to true.";
                }
            else logmsg = SYSPROP_PREFERIPV4STACK + " property already set to "+
                          s_preferIPv4Stack + ".";

            if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_UBNET,
                               UBrokerLogContext.SUB_V_UB_UBNET))
                log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET, logmsg);

            if (s_preferIPv6Addrs == null)
                {
                System.setProperty(SYSPROP_PREFERIPV6ADDRS, "false");
                logmsg = SYSPROP_PREFERIPV6ADDRS + " property set to false.";
                }
            else logmsg = SYSPROP_PREFERIPV6ADDRS + " property already set to "+
                          s_preferIPv6Addrs + ".";

            if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_UBNET,
                               UBrokerLogContext.SUB_V_UB_UBNET))
                log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET, logmsg);

            break;

        case IPVER_IPV6:

            if (s_preferIPv4Stack == null)
                {
                System.setProperty(SYSPROP_PREFERIPV4STACK, "false");
                logmsg = SYSPROP_PREFERIPV4STACK + " property set to false.";
                }
            else logmsg = SYSPROP_PREFERIPV4STACK + " property already set to "+
                          s_preferIPv4Stack + ".";

            if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_UBNET,
                               UBrokerLogContext.SUB_V_UB_UBNET))
                log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET, logmsg);


            if (s_preferIPv6Addrs == null)
                {
                System.setProperty(SYSPROP_PREFERIPV6ADDRS, "true");
                logmsg = SYSPROP_PREFERIPV6ADDRS + " property set to true.";
                }
            else logmsg = SYSPROP_PREFERIPV6ADDRS + " property already set to "+
                          s_preferIPv6Addrs + ".";

            if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_UBNET,
                               UBrokerLogContext.SUB_V_UB_UBNET))
                log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET, logmsg);

            break;

        default:
            fatalError(log,
                   "Unknown ipver (" + ipver + ") specified."
                  );
        }
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void adjustServerLogFileValue(String newValue)
   {
   putValueAsString(PROPNAME_SERVERLOGFILE, newValue);
   }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void setSSLClientParams( ClientParams sslClientParams )
   {
   this.sslClientParams = sslClientParams;
   }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public ClientParams getSSLClientParams()
   {
   return sslClientParams;
   }

/*********************************************************************/
/* Private methods                                                   */
/*********************************************************************/

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String brokerHostName(int regMode, IAppLogger log)
    {
    String ret;
    boolean regByHost = false;
    String  host = null;
    String hostName = getValueAsString(PROPNAME_HOSTNAME);

    InetAddress   local;
    InetAddress[] addrs;
    int           naddrs;
    int           i;

    try
        {

        switch ( regMode )
            {
            case REGMODE_LOCALHOST:
                local = InetAddress.getLocalHost();
                host = local.getHostName();
                ret = host;
                break;

            case REGMODE_IP:
                local = InetAddress.getLocalHost();
                host = local.getHostName();
                ret = (ipver == IPVER_IPV6) ? brokerHostIPv6(log)    :
                                              local.getHostAddress() ;
                break;

            case REGMODE_HOSTNAME:
                ret = hostName;
                host = hostName;
                break;

            default:
                /* this should not be possible, but code for it anyway */
                fatalError(log,
                           "Unable to get localhost name : " +
                           "Invalid REGMODE parameter"
                          );
                ret = null;
            }

            if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_UBNET,
                               UBrokerLogContext.SUB_V_UB_UBNET))
                {
                try
                    {
                    addrs = InetAddress.getAllByName(host);
                    naddrs = addrs.length;

                    log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET,
                                 naddrs + " local interface(s) for host= " + 
                                 host);
                    for (i = 0; i < naddrs; i++)
                        {
                        log.logBasic(UBrokerLogContext.SUB_V_UB_UBNET,
                                     "address[" + i + "] = " +
                                       addrs[i].getHostName() +
                                       " / " + addrs[i].getHostAddress() );
                        }
                    }
                catch (IOException ioex)
                    {
                    log.logBasic(
                        UBrokerLogContext.SUB_V_UB_UBNET,
                        "unable to get local interface(s) for host= " +
                         host + " : " + ioex.toString() );
                    }
                }
        }
    catch (IOException ioe)
        {
        /* Unable to get localhost name : %s<IOException_string>
                                          %s<IOExceptionMsg_string> */
        fatalError(log,
                   jbMsg.jbMSG083,
                   new Object[] { ioe.toString(),
                                  ioe.getMessage() }
                  );
        ret = null;
        }

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private int getEnumProperty(String propValue,
                            String[] enumPropNames,
                            int[] enumPropValues,
                            int defPropValue)
{
int i;
int limit = enumPropNames.length;

if (propValue == null)
    return defPropValue;

for (i = 0;
        (i < limit) && (propValue.compareTo(enumPropNames[i]) != 0) ;
            i++)
    ;

return (i < limit) ? enumPropValues[i] : defPropValue;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void checkValidSection(IAppLogger log,
                               String sectionName)
    {
    /* find out if the section name is valid */

    try
        {
        groups(sectionName, true, true, true);
        }
    catch (NoSuchGroupException ge)
        {
        /* Section (%s<sectionName>) not found in
           properties file (%s<propertiesfilename>). */
        fatalError(log,
                   jbMsg.jbMSG100,
                   new Object[] { sectionName,
                                  propertiesfilename }
                  );
        }
    }

private void checkValidEnvSection(IAppLogger log,
        String sectionName) throws NoSuchGroupException
{
	/* find out if the section name is valid */
	try
	{
		groups(sectionName, true, true, true);
	}
	catch (NoSuchGroupException ge)
	{
		throw ge;
	}
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void checkValidASKTimeouts(IAppLogger log)
    {
    int serverASKActivityTimeout = getValueAsInt(PROPNAME_SASKACTIVITYTIMEOUT);
    int serverASKResponseTimeout = getValueAsInt(PROPNAME_SASKRESPONSETIMEOUT);

    if (serverASKActivityTimeout < MIN_SASKACTIVITYTIMEOUT)
        {
        /* The serverASKActivityTimeout must be at least */
        /* %d<minTimeout> seconds.                       */
        fatalError(log,
                   jbMsg.jbMSG156,
                   new Object[] { new Integer(MIN_SASKACTIVITYTIMEOUT) }
                  );
        }

    if (serverASKResponseTimeout < MIN_SASKRESPONSETIMEOUT)
        {
        /* The serverASKResponseTimeout must be at least */
        /* %d<minTimeout> seconds.                       */
        fatalError(log,
                   jbMsg.jbMSG157,
                   new Object[] { new Integer(MIN_SASKRESPONSETIMEOUT) }
                  );
        }
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int adjustMaxServerCnt(IAppLogger log, int maxServerCnt)
{
    LicenseMgr licMgr;
    boolean fAllowed = false;
    int serverMax = maxServerCnt;

    // create an instance of a LicenseMgr object
    try
    {
        licMgr = new LicenseMgr();
    }
    catch (LicenseMgr.CannotContactLicenseMgr Ex_LicenseMgr)
    {
        /* ERROR: Cannot contact License Manager : %s<LicenseMgrMsg_string> */
        fatalError(log,
                   jbMsg.jbMSG101,
                   new Object[] { Ex_LicenseMgr.getMessage() }
                  );
        return -1;
    }

    // If we are a Dataserver, check for proper license
    try
    {
        if (serverType == SERVERTYPE_DATASERVER_OD)
            fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_ODBC_GATEWAY);

        if (serverType == SERVERTYPE_DATASERVER_OR)
            fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_ORACLE_DATASERVER);

        if (serverType == SERVERTYPE_DATASERVER_MSS)
            fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_MSS_DATASERVER);

        // Return without server limitations
        if (fAllowed)
            return maxServerCnt;
    }
    catch (LicenseMgr.NotLicensed e)
    {
        /* ERROR : not licensed to use this product :
        %s<NotLicensedExceptionMsg_string> */
        fatalError(log,
            jbMsg.jbMSG102,
            new Object[] { }
            );
        return -1;
    }


    // Check OpenEdge Server Licensed Tiers
    try
    {
        // Test for OpenEdge Enterprise server
        fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_OESERV_ENT);
        // Return without server limitations
        return(maxServerCnt);
    }
    catch (LicenseMgr.NotLicensed e)
    {
    }

    try
    {
	// Test for OpenEdge Development server
        fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_OESERV_DEV);
        // Enforce server limitations
        if ((serverType != SERVERTYPE_ADAPTER)     &&
            (serverType != SERVERTYPE_ADAPTER_CC)  &&
            (serverType != SERVERTYPE_ADAPTER_SC))

            serverMax = RESTRICTED_SERVER_LICENSE_LIMIT;
    }
    catch (LicenseMgr.NotLicensed e)
    {
        if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
            UBrokerLogContext.SUB_V_UB_BASIC))
        {
            log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                "Licence Exception: " + e.toString());
        }        
    }

    if (fAllowed == false)
    {
        try
	    {
	    // Test for OpenEdge Basic server
            fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_OESERV_BAS);
            // Return without server limitations
            if ((serverType != SERVERTYPE_ADAPTER)     &&
                (serverType != SERVERTYPE_ADAPTER_CC)  &&
                (serverType != SERVERTYPE_ADAPTER_SC))
                    serverMax = RESTRICTED_SERVER_LICENSE_LIMIT;
            else
                serverMax = RESTRICTED_ADAPTER_SERVER_LICENSE_LIMIT;
	    }
	    catch (LicenseMgr.NotLicensed e)
	    {
        }
    }

    // If we are a ClientConnect adapter, then no license restriction
    if (serverType == SERVERTYPE_ADAPTER_CC)
        fAllowed = true;

    if (!fAllowed)
    {
        /* ERROR : not licensed to use this product :
        %s<NotLicensedExceptionMsg_string> */
        fatalError(log,
            jbMsg.jbMSG102,
            new Object[] { }
            );
        return -1;
    }

    // Log any enforcement that adjusts max server count
    if (serverMax < maxServerCnt)
    {
        /* CR: OE00212081 Warning: Licence limitations */
         
        maxServerCnt = serverMax;
        /*** CR: OE00212081 **/
        if (log.ifLogBasic(UBrokerLogContext.SUB_M_UB_BASIC,
            UBrokerLogContext.SUB_V_UB_BASIC))
        {
            log.logBasic(UBrokerLogContext.SUB_V_UB_BASIC,
                jbMsg.jbMSG170, new Object[]{maxServerCnt});
        }
    }

    return maxServerCnt;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int adjustInitialServerCnt(int initialServers, int maxServerCnt)
    {
    if (initialServers > maxServerCnt)
        initialServers = maxServerCnt;
    return initialServers;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void missingProperty(IAppLogger log, String propname)
    {
    /* ERROR: property (%s<propertyName>) is not defined for broker
              (%s<brokerName>) in property file (%s<propertyfilename>). */

    fatalError(log,
               jbMsg.jbMSG104,
               new Object[] { propname,
                              brokerName,
                              propertiesfilename  }
              );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void missingProperty(IAppLogger log,
                             String     propname,
                             String     sectionName,
                             String     propertiesfilename)
    {
    /* ERROR: property %s<propertyName> is not defined for
            %s<sectionName> in property file %s<propertyFileName> */
    fatalError(log,
               jbMsg.jbMSG138,
               new Object[] { propname,
                              sectionName,
                              propertiesfilename }
              );
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void fatalError(String errMsg)
    {
    System.err.println(errMsg);
    System.exit(1);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void fatalError(IAppLogger log, String errMsg)
    {
    log.logError(errMsg);

    System.exit(1);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private void fatalError(IAppLogger log, long msgid, Object[] parms)
    {
    log.logError( msgid, parms );

    System.exit(1);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean isNT()
    {
    String sOS = System.getProperty("os.name");
    boolean fNT = sOS.startsWith("Windows");

    return fNT;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void processSSLArgs( String propGroup, IAppLogger log )
    {
    // Make sure SSL enable property is there.  If SSL is off, no need to
    // do the rest of the properties.
    boolean sslEnable = getRequiredBoolean( propGroup, PROPNAME_SSLENABLE, log );
    if (!sslEnable)
        return;

    String certStorePath = getOptionalString( propGroup, PROPNAME_CERTSTOREPATH );
    String keyAlias = getOptionalString( propGroup, PROPNAME_KEYALIAS );
    String keyAliasPasswd = getOptionalString( propGroup, PROPNAME_KEYALIASPASSWD );
    String keyStorePath = getOptionalString( propGroup, PROPNAME_KEYSTOREPATH );
    String keyStorePasswd = getOptionalString( propGroup, PROPNAME_KEYSTOREPASSWD );
    boolean noSessionCache = getOptionalBoolean( propGroup, PROPNAME_NOSESSIONCACHE );
    int sessionTimeout = getOptionalInteger( propGroup, PROPNAME_SESSIONTIMEOUT )
        * 1000;
    String sslAlgorithms = getOptionalString( propGroup, PROPNAME_SSLALGORITHMS );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean getRequiredBoolean( String propGroup, String name, IAppLogger log )
    {
    int intProperty = getValueAsInt( name );

    if (intProperty == INVALID_INT)
        missingProperty( log, name );

    return (intProperty != 0);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean getOptionalBoolean( String propGroup, String name )
    {
    int intProperty = getValueAsInt( name );
    if (intProperty == INVALID_INT)
	intProperty = 0;
    return (intProperty != 0);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String getOptionalString( String propGroup, String name )
    {
    return getValueAsString( name );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private int getOptionalInteger( String propGroup, String name )
    {
    return getValueAsInt( name );
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private int getIntValue( String strval, int defval )
    {
    int ret;

    try
        {
        ret = Integer.parseInt(strval);
        }
    catch (NumberFormatException nfe)
        {
        ret = defval;
        }

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String packSCLoggingProperties()
    {
    StringBuffer buff = new StringBuffer(100);

    buff.append(getValueAsString(PROPNAME_MQBRKRLOGAPPEND)     + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQBRKRLOGENTRYTYPES) + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQBRKRLOGGINGLEVEL)  + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQBRKRLOGTHRESHOLD)  + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQBRKRNUMLOGFILES)   + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQBROKERLOGFILE)     + SEPARATOR);

    buff.append(getValueAsString(PROPNAME_MQSRVRLOGAPPEND)     + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQSRVRLOGENTRYTYPES) + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQSRVRLOGGINGLEVEL)  + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQSRVRLOGTHRESHOLD)  + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQSRVRNUMLOGFILES)   + SEPARATOR);
    buff.append(getValueAsString(PROPNAME_MQSERVERLOGFILE)     + SEPARATOR);

    return buff.toString();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void unpackSCLoggingProperties(String propsStr)
    {
    StringTokenizer props   = new StringTokenizer(propsStr, SEPARATOR, false);

    putValueAsString(PROPNAME_MQBRKRLOGAPPEND,     props.nextToken());
    putValueAsString(PROPNAME_MQBRKRLOGENTRYTYPES, props.nextToken());
    putValueAsString(PROPNAME_MQBRKRLOGGINGLEVEL,  props.nextToken());
    putValueAsString(PROPNAME_MQBRKRLOGTHRESHOLD,  props.nextToken());
    putValueAsString(PROPNAME_MQBRKRNUMLOGFILES,   props.nextToken());
    putValueAsString(PROPNAME_MQBROKERLOGFILE,     props.nextToken());

    putValueAsString(PROPNAME_MQSRVRLOGAPPEND,     props.nextToken());
    putValueAsString(PROPNAME_MQSRVRLOGENTRYTYPES, props.nextToken());
    putValueAsString(PROPNAME_MQSRVRLOGGINGLEVEL,  props.nextToken());
    putValueAsString(PROPNAME_MQSRVRLOGTHRESHOLD,  props.nextToken());
    putValueAsString(PROPNAME_MQSRVRNUMLOGFILES,   props.nextToken());
    putValueAsString(PROPNAME_MQSERVERLOGFILE,     props.nextToken());
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void processAdminServerPluginsArgs(String propertiesfilename,
                                           String propGroup,
                                           IAppLogger log)
    {
    String tmp;
    PropertyManager pm = new PropertyManager();
    String installPath = (new InstallPath()).getInstallPath();
    String fs          = System.getProperty("file.separator");
    String os_arch     = System.getProperty("os.arch");
    String mqJvmArgs, mqJvmExe, mqSecurityPolicy, mqClassPath;

    /* Read in property file ... get fqn if not entered from cmdline */
    if (propertiesfilename == INVALID_STRING)
        {
        propertiesfilename = installPath + fs +
		                     "properties" + fs +
							 "AdminServerPlugins.properties";
        }

    /* set The filter */

    pm.setGetPropertyFilter(new PropertyFilter(this));

    /* load the configuration file */
    try
    {
        pm.load(propertiesfilename);
    }
    catch (ProException e)
    {
        /* Error loading properties file (%s<filename>)
                %s<ProException_string> : %s<ProExceptionMsg_string> */
        fatalError(log,
                   jbMsg.jbMSG097,
                   new Object[] { propertiesfilename,
                                  e.toString(),
                                  e.getMessage() }
                  );
    }


    if ((mqJvmExe = pm.getProperty(
                    propGroup + "." + PROPNAME_MQJVMEXE,
                    INVALID_STRING
                )) == INVALID_STRING)
        {
/*  THIS DOESN'T WORK ON ALL PLATFORMS
        mqJvmExe = installPath + fs +
		           "jre" + fs +
				   "bin" + fs +
				   "java" +
				   (isNT() ? ".exe" :"");
*/
        if (os_arch.equals("sparcv9") || os_arch.equals("IA64W") 
			||  os_arch.equals("PA_RISC2.0W")) 
              {
              mqJvmExe = System.getProperty("java.home") + fs +
				  "bin" + fs + os_arch + fs +  "java" ;
              } 
        else
              mqJvmExe = System.getProperty("java.home") + fs +
				         "bin" + fs +
				         "java" +
				         (isNT() ? ".exe" :"");
        }
  
    /* we also need to add jvmargs from ubroker.properties section */
    if ((mqJvmArgs = pm.getProperty(
                    propGroup + "." + PROPNAME_MQJVMARGS,
                    INVALID_STRING
            )) == INVALID_STRING)
        missingProperty(log,
        		        PROPNAME_MQJVMARGS,
						propGroup,
						propertiesfilename);


    if ((mqSecurityPolicy = pm.getProperty(
                    propGroup + "." + PROPNAME_MQSECURITYPOLICY,
                    INVALID_STRING
            )) == INVALID_STRING)
        missingProperty(log,
        		        PROPNAME_MQSECURITYPOLICY,
						propGroup,
						propertiesfilename);

    if ((tmp = pm.getProperty(
                    propGroup + "." + PROPNAME_MQCLASSPATH,
                    INVALID_STRING
            )) == INVALID_STRING)
        missingProperty(log,
        		        PROPNAME_MQCLASSPATH,
						propGroup,
						propertiesfilename);
    mqClassPath = bldClasspath(tmp);

    putValueAsString(PROPNAME_MQJVMEXE, mqJvmExe);
    putValueAsString(PROPNAME_MQJVMARGS, mqJvmArgs);
    putValueAsString(PROPNAME_MQSECURITYPOLICY, mqSecurityPolicy);
    putValueAsString(PROPNAME_MQCLASSPATH, mqClassPath);
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String bldClasspath(String csvCP)
    {
    final String csvSeparator = ",";
    final String cpSeparator = System.getProperty("path.separator");
    StringTokenizer cp   = new StringTokenizer(csvCP, csvSeparator, false);
    StringBuffer buff = new StringBuffer(100);

    if (cp.hasMoreTokens())
        {
   	    buff.append(cp.nextToken());
	    while(cp.hasMoreTokens())
            {
    	    buff.append(cpSeparator + cp.nextToken());
            }
        }

    return buff.toString();
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String bldAdminServerPluginsFilename(String ubpropfilename)
    {
    String ret;
    File   ubpropfile = new File(ubpropfilename);
    String path = ubpropfile.getParent();
    File   adminpropfile = new File(path,"AdminServerPlugins.properties");

    ret = adminpropfile.getAbsolutePath();

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String bldServerConnectArgs(String ubpropfilename)
    {
    String ret;
    File   ubpropfile = new File(ubpropfilename);
    String path = ubpropfile.getParent();
    File   javatoolspropfile = new File(path,"JavaTools.properties");

    ret = "-t SC -i sc1 -f " + javatoolspropfile.getAbsolutePath();

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private String bldCCLogFilename(String logfileName, int parentPID)
    {
    String ret;
    File   logfile = new File(logfileName);
    String path = logfile.getParent();
    String name = logfile.getName();
    String newName;
    File   ccLog;
    int    idxDot;

    idxDot = name.lastIndexOf('.');
    /* name doesn't contain a dot ... just at -pid to the end */
    if (idxDot == -1)
        {
        newName = logfileName + "-" + parentPID;
        }
    else
        {
        /* the name contains a dot ... add the -pid before the last dot */
        newName = name.substring(0,idxDot) +
                  "-" + parentPID +
                  name.substring(idxDot);
        }


    ccLog = new File(path,newName);
    ret = ccLog.getAbsolutePath();

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/* this method attempts to find the IPv6 global address for the localHost */

private String brokerHostIPv6(IAppLogger log)
    {
    String ret = null;
    String  hostName = null;

    InetAddress   localHost;
    InetAddress[] addrs;
    Inet6Address  v6Address = null;
    int           naddrs;
    int           i;

    try
        {
        localHost = InetAddress.getLocalHost();
        hostName = localHost.getHostName();

        addrs = InetAddress.getAllByName(hostName);
        naddrs = addrs.length;

        if (log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_UBNET,
                             UBrokerLogContext.SUB_V_UB_UBNET))
            log.logVerbose(UBrokerLogContext.SUB_V_UB_UBNET,
                           naddrs + " interface(s) for host= " + 
                           hostName);

        /* log all the addresses and their types */
        if (log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_UBNET,
                             UBrokerLogContext.SUB_V_UB_UBNET))
            {
            for (i = 0; i < naddrs; i++)
                {
                if (addrs[i] instanceof Inet6Address)
                    {
                    Inet6Address v6addr = (Inet6Address) addrs[i];

                    log.logVerbose(UBrokerLogContext.SUB_V_UB_UBNET,
                                     "address[" + i + "] = " +
                                      v6addr.getHostAddress() +
                                     "  INET6" +
                                     "  linkLocal=" + 
                                     v6addr.isLinkLocalAddress() +
                                     "  siteLocal=" + 
                                     v6addr.isSiteLocalAddress());
                    }
                else
                    {
                    log.logVerbose(UBrokerLogContext.SUB_V_UB_UBNET,
                                     "address[" + i + "] = " +
                                      addrs[i].getHostAddress() +
                                     "  INET4");
                    }
                }
            }

        /* loop through the list looking for a global IPv6 address. */
        /* if none is found, select the first IPv6 address found.   */

        for (i = 0; i < naddrs; i++)
            {
            if (addrs[i] instanceof Inet6Address)
                {
                Inet6Address v6addr = (Inet6Address) addrs[i];

                if (v6Address == null)
                    v6Address = v6addr;

                if (!v6addr.isLinkLocalAddress() && 
                    !v6addr.isSiteLocalAddress())
                    {
                    v6Address = v6addr;
                    break;
                    }
                }
            }

        /* if we get here and v6address is null, then there are no */
        /* IPv6 addresses, so we return the first address found    */

        ret = (v6Address == null) ? addrs[0].getHostAddress()   :
                                    v6Address.getHostAddress()  ;
        }
    catch (IOException ioex)
        {
        log.logError(
            "unable to get local interface(s) for host= " +
             hostName + " : " + ioex.toString() );
        }

    return ret;
    }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

/*********************************************************************/
/*  Support for dynamic updating of properties                       */
/*********************************************************************/

/*
 * Create our "active" property values hashtable.
 * Each entry has:
 *   key   - the property name in lowercase
 *   value - a Property object (defined in PropertyManager.java)
 */
private Hashtable cloneProperties(String propGroup)
{
    if (activeProps == null)
	activeProps = getHashtable();
    
    Hashtable tmpProps = null;
    
    try
    {
	PropertyManager.PropertyCollection pc = properties(propGroup, true, true);	
	tmpProps = pc.getProperties();
    }
    catch ( NoSuchGroupException e)
    {
    }

    if (tmpProps == null)
	return null;

    /* 
     * We must create our own copy of each property object to
     * prevent them from being automatically changed when a 
     * new copy of the property file is loaded.  This allows us
     * to control what properties are allowed to be dynamically 
     * updated since some properties are immutable.
     */
    for (Enumeration e = tmpProps.keys() ; e.hasMoreElements() ;)
    {
	String name = (String)e.nextElement();
	Property p = (Property)tmpProps.get(name);
	Object val = p.clone();
	activeProps.put(name, val);
    }

    return activeProps;
}

/*
 * Update the current value of a property.
 */
private boolean putValueAsString(String name, String value)
{
    if (name == null || value == null) 
	return false;

    if (activeProps == null)
	activeProps = getHashtable();

    String propName = name.toLowerCase();
    Property tmpVal = (Property)activeProps.get(propName);

    try
    {
        // Add any property that does not exist.
        if (tmpVal == null)
        {
	    tmpVal = new Property(name, value);
	    activeProps.put(propName, tmpVal);
        }
        else
            tmpVal.setValue(value);  // existing property, update its value
    }
    catch (Exception e)
    {
        return false;
    }

    return true;
}

public boolean putValueAsInt(String name, int value)
{
    return putValueAsString(name, Integer.toString(value));
}

/*
 * Get the current value of a property.
 */
public String getValueAsString(String name)
{
    if (name == null || activeProps == null) 
	return INVALID_STRING;

    Property tmpVal = (Property)activeProps.get(name.toLowerCase());
    if (tmpVal == null)
	return INVALID_STRING;

    String ret = tmpVal.getValueOrDefault();
    if (ret != null)
	ret = propFilter.filterValue(propGroupName, name, ret);

    return ret;
}

public int getValueAsInt(String name)
{
    int ret = INVALID_INT;
    int radix = 10;

    String val = getValueAsString(name);
    if (val == null || val.length() == 0)
	return INVALID_INT;

    try
    {
        if (val.toLowerCase().startsWith("0x"))
        {
            radix = 16;
            val = val.substring(2);
        }
        ret = Integer.parseInt(val,radix);
    }
    catch (NumberFormatException e)
    {
	return INVALID_INT;
    }
    return ret;
}

public boolean getValueAsBoolean(String name)
{
    int val = getValueAsInt(name);
    return (val != 0);
}

public String[] getValueAsStringArray(String name)
{
    String val = getValueAsString(name);

    if (val == null)
	return null;
    
    // Call the property manager to parse the
    // string and trim spaces from each entry.
    return parseArrayProperty(val,",");
}

/*
 * Generate a list of properties that have changed.
 * This is done by creating a PropertyCollection of the new properties
 * and comparing each entry in the collection against the "active" properties.
 */
private void generatePropertyDiffList()
{
    Hashtable tmpProps = null;
    String name, newVal, oldVal;
    Property p = null;
    changedPropList.clear();  // clear out old values

    try
    {
	PropertyManager.PropertyCollection pc = properties(propGroupName, true, true);	
	tmpProps = pc.getProperties();
    }
    catch ( NoSuchGroupException e)
    {
        return;
    }

    if (tmpProps == null)  // property group is empty!!
	return;

    for (Enumeration e = activeProps.keys() ; e.hasMoreElements() ;)
    {
        name = (String)e.nextElement();
        p = (Property)tmpProps.get(name);

        if (p == null)  // property not in property file
            continue;

        oldVal = getValueAsString(name);
        newVal = p.getValueOrDefault();

        if (newVal != null)
    	    newVal = propFilter.filterValue(propGroupName, name, newVal);

        if (!newVal.equals(oldVal))
        {
            /* 
             * brkrLoggingLevel is a special case.  Changing this property
             * changes what messages are output to the log file.  If this 
             * property is being changed, add it to the front of the list
             * so it will get processed first.
             */
            if (name.equals(PROPNAME_BRKRLOGGINGLEVEL.toLowerCase()))
                changedPropList.add(0,name);
            else
                changedPropList.add(name);
        }
    }
}

/*
 * Update any changed properties.  
 * This is done by generating a list of what properties have changed and
 * then invoking a specific method for each changed property.  It is the
 * responsibility of the invoked method to do whatever is necessary to
 * update the property to the new value.
 * 
 * This method is invoked whenever the properties file has been changed.
 */
public boolean updateUbProperties(IAppLogger log)
{
    boolean propertyUpdated = false;
    Class[] params = new Class[] {String.class, IAppLogger.class};
    Class me = this.getClass();

    generatePropertyDiffList();
    if (changedPropList.size() == 0)
        return false;    /* none of our properties have changed - nothing to do! */

    /*
     * for each property that has changed, invoke its update method
     */
    for (int i=0; i < changedPropList.size(); i++)
    {
	String propertyName = (String)changedPropList.get(i);
        String methodName = "update_" + propertyName;
        Boolean retValue = Boolean.FALSE;
        try
        {
            Method methodToRun = me.getDeclaredMethod( methodName, params );
            if (methodToRun != null)
            {
        	methodToRun.setAccessible(true);
        	retValue = (Boolean)methodToRun.invoke(this, new Object[] {propertyName, log});
            }
            if (retValue.equals(Boolean.TRUE))
        	propertyUpdated = true;
        }
        catch (NoSuchMethodException e)
        {
            // property is immutable
            continue;
        }
        catch (Exception e)
        {
            // "Error updating property " + propertyName + " : " + e.getMessage() );
            if (log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_BASIC,UBrokerLogContext.SUB_V_UB_BASIC))
                log.logVerbose(UBrokerLogContext.SUB_V_UB_BASIC,
                             jbMsg.jbMSG162, 
                             new Object[]{propertyName, e.getMessage()});
        }
    }
    return propertyUpdated;
}

/*********************************************************************/
/* Determine if a property is dynamic.                               */
/*********************************************************************/
private boolean isDynamicProperty(String propertyName)
{
    Class[] params = new Class[] {String.class, IAppLogger.class};
    Class me = this.getClass();
    String methodName = "update_" + propertyName.toLowerCase();

    try
    {
        Method methodToRun = me.getDeclaredMethod( methodName, params );
        if (methodToRun != null)
            return true;
    }
    catch (Exception e)
    {
    }

    return false;
}

/*********************************************************************/
/* The following methods are used to get the NEW value of the        */
/* property from the reloaded property file.                         */
/*********************************************************************/

/*  not currently needed...
private boolean putNewValueAsString(String name, String value)
{
    try
    {
	PropertyManager.PropertyCollection pc = properties(propGroupName, true, true);	
	Property newValue = (Property)pc.get(name);

        if (newValue == null)
            return false;

        newValue.setValue(value);
    }
    catch ( NoSuchGroupException e)
    {
	return false;
    }
    catch ( PropertyValueException e)
    {
	return false;
    }

    return true;
}
*/

private String getNewValueAsString(String name)
{
    String value = INVALID_STRING;
    try
    {
	PropertyManager.PropertyCollection pc = properties(propGroupName, true, true);	
	Property newValue = (Property)pc.get(name);

	if (newValue == null)
	    return INVALID_STRING;
	
        value = newValue.getValueOrDefault();
//        if (value != null)
//    	    value = propFilter.filterValue(propGroupName, name, value);
    }
    catch ( NoSuchGroupException e)
    {
	return INVALID_STRING;
    }

    return value;
}

private int getNewValueAsInt(String name)
{
    int ret = INVALID_INT;
    int radix = 10;

    String val = getNewValueAsString(name);
    if (val == INVALID_STRING || val.length() == 0)
	return INVALID_INT;

    try
    {      
        if (val.toLowerCase().startsWith("0x"))
        {
            radix = 16;
            val = val.substring(2);
        }

        ret = Integer.parseInt(val,radix);
    }
    catch (NumberFormatException e)
    {
	return INVALID_INT;
    }
    return ret;
}

private boolean getNewValueAsBoolean(String name)
{
    int val = getNewValueAsInt(name);
    return (val != 0);
}


/*********************************************************************/
/* Add any methods needed to update individual properties after this */
/* point.  All methods must use the following convention:            */
/*                                                                   */
/*  Method name:  update_<property name>  (name is in lowercase)     */
/*  Parameters:   String propertyName     (name of property)         */
/*                IAppLogger log          (log to output messages)   */
/*********************************************************************/

/*********************************************************************/
/* The following methods are used to update "value" properties.      */
/* These are properties that are referenced each time they are needed*/
/* and may be changed by just updating their value in the "active"   */
/* property hashtable.  A generic method - updateValueProperty - is  */
/* provided to do this and other methods may call this method to do  */
/* their work.                                                       */
/*********************************************************************/
private boolean updateValueProperty(String name, IAppLogger log)
{
    String newValue;
    String oldValue;
    try
    {
        Property p = (Property)activeProps.get(name.toLowerCase());
        oldValue = p.getValueOrDefault();
	newValue = getNewValueAsString(name);
	p.setValue(newValue);

	// "Property " + p.getName() + " has been updated from " + oldValue + " to " + newValue);
	if (log.ifLogVerbose(UBrokerLogContext.SUB_M_UB_BASIC,UBrokerLogContext.SUB_V_UB_BASIC))
            log.logVerbose(UBrokerLogContext.SUB_V_UB_BASIC,
                           jbMsg.jbMSG163, 
                           new Object[]{p.getName(), oldValue, newValue});
    }
    catch (PropertyValueException e)
    {
	return false;
    }
    return true;
}

/*********************************************************************/
/* The following methods are used to update "value" properties.      */
/*********************************************************************/

private boolean update_agentdetailtimeout     (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_collectstatsdata       (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_connectingtimeout      (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_flushstatsdata         (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_priorityweight         (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_requesttimeout         (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvractivateproc       (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrconnectproc        (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrdeactivateproc     (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrdisconnproc        (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrexecfile           (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrmaxport            (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrminport            (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrstartupparam       (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrstartupproc        (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrstartupprocparam   (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrstartuptimeout     (String name, IAppLogger log) {return updateValueProperty(name,log);}
private boolean update_srvrshutdownproc       (String name, IAppLogger log) {return updateValueProperty(name,log);}

/*********************************************************************/
/* The following methods are used to update "logging" properties.    */
/*********************************************************************/

/* comment out the following until updating this property is supported  
private boolean update_brokerlogfile(String name, IAppLogger log)
{
    int logAppend = getValueAsInt(PROPNAME_BRKRLOGAPPEND);
    String newValue = getNewValueAsString(name);
    if (newValue == null)
        return false;

    // Since this is a filename we need to filter it's value
    // to account for any registry/environment reference.

    newValue = propFilter.filterValue(propGroupName, name, newValue);

    if (log.switchLogFile(newValue, logAppend) == true)
        return updateValueProperty(name, log);

    return false;
}
*/

private boolean update_brkrlogginglevel(String name, IAppLogger log)
{
    boolean ret = false;
    int oldValue = getValueAsInt(name);
    int newValue = getNewValueAsInt(name);
    if (newValue == INVALID_INT)
	return false;

    /*
     * If we are going to a lower logging level
     * (i.e. VERBOSE to BASIC), log the message
     * first before changing the logging level.
     * If not (i.e. BASIC to VERBOSE), change
     * the logging level first.
     */
    if (oldValue > newValue)
    {
        ret = updateValueProperty(name,log);
	log.setLoggingLevel(newValue);
    }
    else
    {
	log.setLoggingLevel(newValue);
        ret = updateValueProperty(name,log);
    }	

    return ret;
}

private boolean update_brkrlogentrytypes(String name, IAppLogger log)
{
    String value = getNewValueAsString(name);
    log.resetLogEntries(value);
    return updateValueProperty(name,log);
}

private boolean update_srvrlogginglevel(String name, IAppLogger log) 
{
    int newValue = getNewValueAsInt(name);
    if (newValue == INVALID_INT)
        return false;

    JavaServices.setLoggingLevel(newValue);
    return updateValueProperty(name,log);
}

private boolean update_srvrlogentrytypes (String name, IAppLogger log) 
{
    return updateValueProperty(name,log);
}
    
/*********************************************************************/
/* The following methods are used to update "behavioral" properties. */
/* These are properties that require taking additional action beyond */
/* just updating their value.                                        */
/*********************************************************************/

private boolean update_autotrimtimeout(String name, IAppLogger log)
{
    if ( (serverType == SERVERTYPE_ADAPTER_CC) ||
         (serverType == SERVERTYPE_ADAPTER_SC) ||
         (serverType == SERVERTYPE_ADAPTER))
       return false;

    int newValue = getNewValueAsInt(name);
    if (newValue == INVALID_INT || listenerThread == null)
        return false;

    listenerThread.setAutoTrimTimeout(newValue);
    return updateValueProperty(name,log);
}

private boolean update_brkrdebuggerenabled(String name, IAppLogger log) 
{
    boolean newValue = getNewValueAsBoolean(name);
    if (debuggerThread == null)
        return false;

    debuggerThread.setDebuggerEnabled(newValue);
    return updateValueProperty(name, log);
}

private boolean update_brkrdebuggerportnumber(String name, IAppLogger log) 
{
    int newValue = getNewValueAsInt(name);
    if (debuggerThread == null) 
        return false;

    debuggerThread.setDebuggerPort(newValue);
    return updateValueProperty(name, log);
}

private boolean update_brkrdebuggerpassphrase(String name, IAppLogger log) 
{
    String newValue = getNewValueAsString(name);
    if (debuggerThread == null) 
        return false;

    debuggerThread.setDebuggerPassphrase(newValue);
    return updateValueProperty(name, log);
}

private boolean update_brkrdebuggersslenable(String name, IAppLogger log) {
	
	 boolean newValue = getNewValueAsBoolean(name);
    if (debuggerThread == null) 
        return false;

    debuggerThread.setDebuggerSSLEnable(newValue);
    return updateValueProperty(name, log);	
}

private boolean update_brkrdebuggerusebrokeralias(String name, IAppLogger log) {

	boolean newValue = getNewValueAsBoolean(name);
	if (debuggerThread == null)
		return false;

	debuggerThread.setDebuggerUseBrokerAlias(newValue);
	return updateValueProperty(name, log);
}

private boolean update_brkrdebuggerkeyalias(String name, IAppLogger log) {

	String newValue = getNewValueAsString(name);
	if (debuggerThread == null)
		return false;

	debuggerThread.setDebuggerKeyAlias(newValue);
	return updateValueProperty(name, log);
}

private boolean update_brkrdebuggerkeyaliaspassword(String name, IAppLogger log) {

	String newValue = getNewValueAsString(name);
	if (debuggerThread == null)
		return false;

	debuggerThread.setDebuggerKeyAliasPassword(newValue);
	return updateValueProperty(name, log);
}

private boolean update_rmiwatchdoginterval(String name, IAppLogger log)
{
    if ( (serverType == SERVERTYPE_ADAPTER_CC) ||
         (serverType == SERVERTYPE_ADAPTER_SC))
        return false;

    int newValue = getNewValueAsInt(name);
    if (newValue == INVALID_INT || listenerThread == null)
        return false;

    listenerThread.setRmiWatchDogInterval(newValue);
    return updateValueProperty(name,log);
}

} /* class ubProperties */
