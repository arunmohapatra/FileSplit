//**************************************************************
//  Copyright (c) 1984-2009 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)DynamicPropertyValues.java  0.2  2/4/00
//
//  All static methods defined in this class are called by the gui loading code
//
//  @author: Edith Tan,  4/20/99
//
//  History:
//
//   02/04/00 est  Added getRegistrationMode(). Changed all in-line constants to
//                 reference via IPropConst.
//
//   6/4/99   est  Location values are in all lower cases and getNameServerList
//                 returns NS1 by default until we put in the real implementation
//                 of the method.
//
//   4/26/99  est  Added getNSLocationChoices()
//
//   4/23/99  est  Added getASOperatingMode() and changed getWSSrvrAppMode() to
//                 getSrvrAppMode().
//
//
package com.progress.ubroker.util;  // If package name is changed, then the reference of this
                                    // package name in the schema file should also be updated.

import com.progress.common.util.Environment;
import com.progress.ubroker.util.IPropConst;
import java.util.Vector;
import java.util.Hashtable;

//****************************************NOTE**********************************************
// All methods defined in this class are also referenced directly in the ubroker.schema file,
// by method names.  Therefore if the method name is changed for any reason, the reference to
// the method in the schema file must be changed accordingly.
//******************************************************************************************
//
public class DynamicPropertyValues implements IPropConst
{
  //
  // Returns a list of application mode options in a vector
  //
  public static Object getSrvrAppMode(String propFullSpec)
  {
    Vector appModeList = new Vector();
    appModeList.addElement((Object)APP_MODE_DEVO);
    appModeList.addElement((Object)APP_MODE_PROD);
    return((Object)appModeList);
  }

  //
  // Returns a list of debugging options in a vector
  //
  public static Object getWSSrvrDbgOptions(String propFullSpec)
  {
    Vector dbgOptionVector = new Vector();
    dbgOptionVector.addElement((Object)DEBUG_MODE_ENABLE);
    dbgOptionVector.addElement((Object)DEBUG_MODE_DISABLE);
    dbgOptionVector.addElement((Object)DEBUG_MODE_DEFAULT);
    return((Object)dbgOptionVector);
  }

  public static Object getWSOperatingMode(String propFullSpec)
  {
    Vector opModeVector = new Vector();
    opModeVector.addElement((Object)OP_MODE_STATELESS);
    //The following options are not supported:
    //opModeVector.addElement((Object)OP_MODE_STATE_AWARE);
    //opModeVector.addElement((Object)OP_MODE_STATE_RESET);
    //opModeVector.addElement((Object)OP_MODE_STATE_FREE);
    return((Object) opModeVector);
  }

  public static Object getASOperatingMode(String propFullSpec)
  {
    Vector opModeVector = new Vector();
    opModeVector.addElement((Object)OP_MODE_STATELESS);
    opModeVector.addElement((Object)OP_MODE_STATE_AWARE);
    opModeVector.addElement((Object)OP_MODE_STATE_RESET);
    opModeVector.addElement((Object)OP_MODE_STATE_FREE);
    return((Object) opModeVector);
  }

  public static Object getASKeepaliveMode(String propFullSpec)
  {
    Vector opModeVector = new Vector();
    opModeVector.addElement((Object)ASK_MODE_NONE);
    opModeVector.addElement((Object)ASK_MODE_SERVERASK);
    opModeVector.addElement((Object)ASK_MODE_CLIENTASK);
    opModeVector.addElement((Object)ASK_MODE_BOTH);
    return((Object) opModeVector);
  }

  //
  //  2BEDONE - are these two methods even being used???
  //
  public static Object getOROperatingMode(String propFullSpec)
  {
    Vector opModeVector = new Vector();
    opModeVector.addElement((Object)"State-aware");
    return((Object) opModeVector);
  }

  public static Object getODOperatingMode(String propFullSpec)
  {
    Vector opModeVector = new Vector();
    opModeVector.addElement((Object)"State-aware");
    return((Object) opModeVector);
  }

  public static Object getMSSOperatingMode(String propFullSpec)
  {
    return getODOperatingMode(propFullSpec);
  }

  public static Object getDataServerSrvrMinPort(String propFullSpec)
  {
      int port = 3000;
      if ( isUnix() )
          port = 1025;
      return new Integer( port );
  }

  public static Object getDataServerSrvrMaxPort(String propFullSpec)
  {
      int port = 5000;
      if ( isUnix() )
          port = 2000;
      return new Integer( port );
  }

  public static Object getNSLocationChoices(String propFullSpec)
  {
    Vector locationChoices = new Vector();
    locationChoices.addElement((Object)LOCATION_LOCAL);
    locationChoices.addElement((Object)LOCATION_REMOTE);
    return((Object)locationChoices);
  }

  public static Object getWsaLocationChoices(String propFullSpec)
  {
    Vector locationChoices = new Vector();
    locationChoices.addElement((Object)LOCATION_LOCAL);
    locationChoices.addElement((Object)LOCATION_REMOTE);
    return((Object)locationChoices);
  }

  public static Object getRegistrationMode(String propFullSpec)
  {
    Vector regModeList = new Vector();
    regModeList.addElement((Object)REG_IP);
    regModeList.addElement((Object)REG_LOCALHOST);
    regModeList.addElement((Object)REG_HOSTNAME);
    return ((Object)regModeList);
  }

  public static Object getPropathDefault(String propFullSpec)
  {
      return ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
             PROPATH_DEF_UNIX : PROPATH_DEF_NT;
  }

  public static Object getWorkDirDefault(String propFullSpec)
  {
      return ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
             WORKDIR_DEF_UNIX : WORKDIR_DEF_NT;
  }

  public static Object getUbBrokerLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             BRKR_LOG_FN;
  }

  public static Object getUbSrvrLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             SRVR_LOG_FN;
  }

  public static Object getAdapterSrvrLogFileDefault(String propFullSpec)
  {
      return getUbSrvrLogFileDefault(propFullSpec);
  }

  public static Object getAdapterBrkrLogFileDefault(String propFullSpec)
  {
      return getUbBrokerLogFileDefault(propFullSpec);
  }

  public static Object getAiaLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             "aia.log";
  }

  public static Object getMessengersLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             "msgr.log";
  }

  public static Object getNsSrvrLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             "ns.log";
  }

  public static Object getWsaLogFileDefault(String propFullSpec)
  {
      Environment env = new Environment();
      String workDir = env.expandPropertyValue((String)getWorkDirDefault(propFullSpec));
      return workDir +  System.getProperty("file.separator") + "wsa.log";
  }

  public static Object getWsSrvrStartupParamDefault(String propFullSpec)
  {
      if (isUnix())
          return WS_SRVR_STARTUP_PARAM_DEF_UNIX;

      return WS_SRVR_STARTUP_PARAM_DEF_NT;
  }

  public static Object getWsRootDefault(String propFullSpec)
  {
      return "";
  }

  public static Object getWsSrvrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + WS_SRVR_EXEC_UNIX;

      return "\"" + SRVR_EXEC_DIR_NT + WS_SRVR_EXEC_FN + "\"";
  }

  public static Object getAsSrvrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + APP_SRVR_EXEC_UNIX;

      return "\"" + SRVR_EXEC_DIR_NT + APP_SRVR_EXEC_FN + "\"";
  }

  public static Object getOrSrvrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + OR_SRVR_EXEC_UNIX;

      return "\"" + SRVR_EXEC_DIR_NT + OR_SRVR_EXEC_FN + "\"";
  }

  public static Object getOdSrvrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + OD_SRVR_EXEC_UNIX;

      return "\"" + SRVR_EXEC_DIR_NT + OD_SRVR_EXEC_FN + "\"";
  }

  public static Object getMsSrvrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + MS_SRVR_EXEC_UNIX;

      return "\"" + SRVR_EXEC_DIR_NT + MS_SRVR_EXEC_FN + "\"";
  }

  public static Object getCGIIPMsngrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + CGIIP_EXEC_UNIX;

      return SRVR_EXEC_DIR_NT + CGIIP_EXEC_FN;
  }

  public static Object getWSISAMsngrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return "";

      return SRVR_EXEC_DIR_NT + WSISA_EXEC_FN;
  }

  public static Object getWSNSAMsngrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + WSNSA_EXEC_FN;

      return SRVR_EXEC_DIR_NT + WSNSA_EXEC_FN;
  }

  public static Object getWSASPMsngrExecFileDefault(String propFullSpec)
  {
      if (isUnix())
          return "";

      return SRVR_EXEC_DIR_NT + WSASP_EXEC_FN;
  }

  public static Object getCGIIPMsngrScriptFileDefault(String propFullSpec)
  {
      if (isUnix())
          return SRVR_EXEC_DIR_UNIX + CGIIP_SCRIPT_UNIX;

      return "";
  }

  public static Object getKeyStorePathDefault(String propFullSpec)
  {
      if (isUnix())
          return KEYSTORE_DIR_UNIX;

      return KEYSTORE_DIR_NT;
  }

  public static Object getCertStorePathDefault(String propFullSpec)
  {
      if (isUnix())
          return CERTSTORE_DIR_UNIX;

      return CERTSTORE_DIR_NT;
  }

  public static Object getCertStorePscDefault(String propFullSpec)
  {
      if (isUnix())
          return PSCCERTSTORE_UNIX;

      return PSCCERTSTORE_NT;
  }

  public static boolean isUnix()
  {
      return ((System.getProperty("os.name")).indexOf("Windows") < 0);
  }

  public static Object getUbMqBrokerLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             "mqbroker.log";
  }

  public static Object getAsMqBrokerLogFileDefault(String propFullSpec)
  {
      return getUbMqBrokerLogFileDefault( propFullSpec);
  }

  public static Object getWsMqBrokerLogFileDefault(String propFullSpec)
  {
      return getUbMqBrokerLogFileDefault( propFullSpec);
  }

  public static Object getUbMqServerLogFileDefault(String propFullSpec)
  {
      return getWorkDirDefault(propFullSpec) +
             System.getProperty("file.separator") +
             "mqserver.log";
  }

  public static Object getAsMqServerLogFileDefault(String propFullSpec)
  {
      return getUbMqServerLogFileDefault( propFullSpec);
  }

  public static Object getWsMqServerLogFileDefault(String propFullSpec)
  {
      return getUbMqServerLogFileDefault( propFullSpec);
  }
}





// END OF FILE

