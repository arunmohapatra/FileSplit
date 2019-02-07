//**************************************************************
 //  Copyright (c) 1984-2015 by Progress Software Corporation
 //  All rights reserved.  No part of this program or document
 //  may be  reproduced in  any form  or by  any means without
 //  permission in writing from Progress Software Corporation.
 //*************************************************************
  //
  //
  //  @(#)PropMgrUtils.java  3.4    8/28/2000
  //
  //  Provides property management functions for the Unified Broker
  //  and Named Server Tool set
  //
  //  @author: Edith Tan,  1/19/99
  //
  //  History:
  //
  //   08/28/00  est   Added support for two new AIA properties: allowAiaCmds and adminIPList.
  //
  //   08/08/00  est   Register new AIA property: soReadTimeout.
  //
  //   07/12/00  est   Register 4 nameServer client port related properties to Messenger and AIA groups.
  //
  //   07/07/00  nmt   Added caching of port numbers including new nameServerClientPort.
  //
  //   06/30/00  est   Added support for MSS DataServer plugin. Removed unused code in
  //                   UBRegProperties class.
  //
  //   04/14/00  nmt   Added support for AppServer Internet Adapter (AIA)
  //
  //   03/30/00  est   Added support for new messenger property, MSNGR_USE_CONNID.
  //
  //   03/24/00  nmt   Changed references to JMS to Adapter - for SonicMQ adapter name change.
  //
  //   03/13/00  nmt   Added getJMSServices support and fixed an error in getSvcTypeStr
  //                   for the messenger type:  need to look for WS_GROUP_PARENT to
  //                   identify the messenger, not MSNGR_GROUP_PATH.  Fixed matchSvcName
  //                   so that it wouldn't succeed if the given svcName was only a part
  //                   of an actual name:  asbroker should not give asbroker1.
  //
  //   02/04/00  est   Added support for managing properties for JMS gateway instances.
  //
  //   01/17/00  est   If there is any problem during the process of adding a new instance,
  //                   scratch any evidence of the new instance from the property file. Also
  //                   allow controlling name server having no value.
  //
  //   12/29/99  est   Added registration code for ubroker property :
  //                   registrationMode and hostName.
  //
  //   12/23/99  est   Added handleNSAddNew().
  //
  //   12/07/99  est   Added support for a dummy parent for Preference group.
  //
  //   11/02/99  est   Preference properties toolWidth and tool are no longer needed.
  //                   Any exception received during the file loading phase, throw file
  //                   load exception.  The only exception is version mismatch.
  //
  //   10/18/99  est   Reset class-level data member in case of AdminServer warm startup.
  //
  //   10/13/99  est   Specify a monitor interval when loading the property file so we
  //                   can track manual editing of the file vs the ProExp editing session.
  //
  //    9/27/99  est   Added support for fetching the log filename info of
  //                   an ubroker, nameServer or messenger instance,getLogFNList().
  //
  //    9/8/99   est   Redirect all loggins to UBToolsMsg.
  //
  //    8/25/99  est   Added support for validateProperties().
  //
  //    8/19/99  est   Return a CfgValidateErrs object when validating a single property.
  //

package com.progress.ubroker.util;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.progress.chimera.common.Tools;
import com.progress.common.exception.ProException;
import com.progress.common.log.ProLog;
import com.progress.common.property.PropertyManager;
import com.progress.common.property.PropertyManager.EnumProperty;
import com.progress.common.property.PropertyManager.IPropertyValueFilter;
import com.progress.common.property.PropertyManager.IntProperty;
import com.progress.common.property.PropertyManager.LoadFileNotFoundException;
import com.progress.common.property.PropertyManager.Property;
import com.progress.common.property.PropertyManager.PropertyCollection;
import com.progress.common.property.PropertyManager.PropertyGroup;
import com.progress.common.property.PropertyManager.PropertySyntaxException;
import com.progress.common.property.PropertyManager.PropertyValueException;
import com.progress.common.property.PropertyManager.PropertyVersionException;
import com.progress.common.util.PropertyFilter;
import com.progress.common.util.UUID;
/* needed to use Java bundles */
import com.progress.international.resources.ProgressResources;
import com.progress.ubroker.tools.UBPropValidate;
import com.progress.ubroker.tools.UBToolsMsg;
import com.progress.ubroker.tools.UbrokerPropertyManager;

public class PropMgrUtils
        implements IPropConst, IPUMessages, IUBLogfileInfo
{
    //
    // Within one jvm, this class would only have one Property Manager object.
    // We only do registration for all known properties once.
    //
    public static UBProperties m_propMgr;
    public static PropertyFilter m_propValFilter = null; //new com.progress.common.util.PropertyFilter();
    public static UBRegProperties m_regPropObj = null;

    public static final boolean DEBUG_TRACE = false;

    static PropertyCollection m_parentGroups = null;

    //
    // instance data members
    //
    String[] m_parentGroupNames = null;
    String[] m_parentGroupValues = null;
    String[] m_ubWSservices = null;
    String[] m_ubASservices = null;
    String[] m_ubORservices = null;
    String[] m_ubODservices = null;
    String[] m_ubMSSservices = null;
    String[] m_msngrServices = null;
    String[] m_nsServices = null;
    String[] m_adapterServices = null;
    String[] m_svcList = null;
    String[] m_aiaServices = null;
    String[] m_wsaServices = null;
    String[] m_restServices = null;
    String[] m_ubGroupPath = null;
    String m_currentGroupName = null;
    String m_propFilePath = null;
    UBPreferenceProperties m_preferences = null;
    boolean m_loadStatus = false;
    boolean m_checkVersion = true;
    int m_nextPortNumber = new Integer(PORT_DEF).intValue();
    Hashtable m_portNumberTable = null;

    //
    // Special getters and setters for the property manager UpdateUtility.
    //
    private static boolean m_updateUtility = false;

    public static void setUpdateUtility(boolean value)
    {
        m_updateUtility = value;
    }

    /**
     * Is this class being called by MergeProp?
     *
     * @return boolean
     */
    public static boolean isUpdateUtility()
    {
        return m_updateUtility;
    }

    //
    // Exceptions throwed in this class
    //
    public static class CantLoadPropertiesRemote
            extends ProException
    {
        private String m_parentObjectException;

        public CantLoadPropertiesRemote(String newOrKnown, String ubrokerName,
                                        String parentExceptStr)
        {
            super(CANT_LOAD_PROPERTIES_REMOTE, new Object[]
                  {newOrKnown, ubrokerName});
            m_parentObjectException = parentExceptStr;
        }

        public String getParentObjectExecption()
        {
            return (m_parentObjectException);
        }
    }


    public static class EnumGroupError
            extends ProException
    {
        public EnumGroupError(String parentExceptionStr)
        {
            super(ENUM_GRP_PROP_ERROR, new Object[]
                  {parentExceptionStr});
        }
    }


    public static class LoadPropFileError
            extends ProException
    {
        String m_parentExceptionStr;

        public LoadPropFileError(String filename, String parentExceptionStr)
        {
            super(CANT_LOAD_PROP_FILE, new Object[]
                  {filename});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }

    }


    public static class CantFindGroup
            extends ProException
    {
        String m_parentExceptionStr;

        public CantFindGroup(String groupName, String parentExceptionStr)
        {
            super(CANT_FIND_PROP_GRP, new Object[]
                  {groupName});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantGetPropCollection
            extends ProException
    {
        String m_parentExceptionStr;

        public CantGetPropCollection(String svcName, String parentExceptionStr)
        {
            super(CANT_GET_PROP_COLLECTION, new Object[]
                  {svcName});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantPutProperty
            extends ProException
    {
        String m_parentExceptionStr;

        public CantPutProperty(String propName, String newValue, String parentExceptionStr)
        {
            super(CANT_PUT_PROPERTY, new Object[]
                  {propName, newValue});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantGetPropertyValue
            extends ProException
    {
        String m_parentExceptionStr;

        public CantGetPropertyValue(String propName, String parentExceptionStr)
        {
            super(CANT_GET_PROPERTY_VALUE, new Object[]
                  {propName});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantPutPropertyValue
            extends ProException
    {
        String m_parentExceptionStr;

        public CantPutPropertyValue(String propName, String newValue, String parentExceptionStr)
        {
            super(CANT_PUT_PROPERTY_VALUE, new Object[]
                  {propName, newValue});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantPutProperties
            extends ProException
    {
        public CantPutProperties(String parentExceptionStr)
        {
            super(CANT_PUT_PROPERTIES, new Object[]
                  {parentExceptionStr});
        }
    }


    public static class SaveGroupError
            extends ProException
    {
        String m_parentExceptionStr;
        public SaveGroupError(String groupPath, String filename, String parentExceptionStr)
        {
            super(SAVE_GROUP_ERROR, new Object[]
                  {groupPath, filename});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class SaveAllError
            extends ProException
    {
        public SaveAllError(String parentExceptionStr)
        {
            super(SAVE_ALL_ERROR, new Object[]
                  {parentExceptionStr});
        }
    }


    public static class SaveGroupForRemoteError
            extends ProException
    {
        String m_parentExceptionStr;

        public SaveGroupForRemoteError(String groupPath, String parentExceptionStr)
        {
            super(SAVE_GROUP_REMOTE_ERROR, new Object[]
                  {groupPath});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class CantGetParentGroup
            extends ProException
    {
        String m_parentExceptionStr;

        public CantGetParentGroup(String groupPath, String parentExceptionStr)
        {
            super(CANT_GET_PARENT_GROUPS, new Object[]
                  {groupPath});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static class RemoveGroupError
            extends ProException
    {
        String m_parentExceptionStr;

        public RemoveGroupError(String groupPath, String parentExceptionStr)
        {
            super(REMOVE_GROUP_ERROR, new Object[]
                  {groupPath});
            m_parentExceptionStr = parentExceptionStr;
        }

        public String getParentExceptionStr()
        {
            return m_parentExceptionStr;
        }
    }


    public static void setPropertyFilter()
    {
        m_propValFilter = new PropertyFilter(m_propMgr);
    }


    //
    // utility method : getSvcName(String svcGroupStr)
    //
    // Given a fully-qualified property group name, i.e. down to the lowest
    // level of sub group name, this method returns the service name
    // of the group name.  For example, if [UBroker.WS.wsbroker1] is given,
    // then the service type string, wsbroker1, is returned.
    //
    // NOTE - this is for temporary use. It will be replaced when the property
    // manager utility class is implemented.
    //
    // No error checking whatsoever.
    //
    public static String getSvcName(String svcGroupStr)
    {
        int nameStartPos = svcGroupStr.lastIndexOf(GROUP_SEPARATOR);
        if (nameStartPos == -1)
        {
            return ((String)null);
        }
        else
        {
            return (svcGroupStr.substring(nameStartPos + 1));
        }
    }

    //
    // static utility method : getSvcTypeStr(String svcGroupStr)
    //
    // Given a fully-qualified property group name, i.e. down to the lowest
    // level of sub group name, this method returns the service type string
    // of the group name.  For example, if [UBroker.WS.wsbroker1] is given,
    // then the service type string, WS, is returned.  This method ignores
    // property group Environment.
    //
    // No error checking whatsoever.
    //
    public static String getSvcTypeStr(String svcGroupStr)
    {
        int typeStartPos = svcGroupStr.indexOf(GROUP_SEPARATOR);

        if (typeStartPos == -1)
        {
            if (svcGroupStr.equalsIgnoreCase(NAME_SRVR_GROUP))
            {
                return (NAME_SERVER_TYPE);
            }
            return ((String)null);
        }
        else
        {
            String svcGroupName = svcGroupStr.substring(0, typeStartPos);
            if (svcGroupName.equalsIgnoreCase(BROKER_GROUP))
            {
                int typeEndPos = svcGroupStr.indexOf(GROUP_SEPARATOR,
                        typeStartPos + 1);
                if (typeEndPos == -1)
                {
                    return (svcGroupStr.substring(typeStartPos + 1));
                }
                else
                {
                    return (svcGroupStr.substring(typeStartPos + 1, typeEndPos));
                }
            }

            if (svcGroupName.equalsIgnoreCase(NAME_SRVR_GROUP))
            {
                return (NAME_SERVER_TYPE);
            }

            if (svcGroupName.equalsIgnoreCase(WS_GROUP_PARENT))
            {
                return (MSNGR_TYPE);
            }

            if (svcGroupName.equalsIgnoreCase(ADAPTER_GROUP))
            {
                return (ADAPTER_TYPE);
            }

            if (svcGroupName.equalsIgnoreCase(AIA_GROUP))
            {
                return (AIA_TYPE);
            }

            if (svcGroupName.equalsIgnoreCase(WSA_GROUP))
            {
                return (WSA_STR_TYPE);
            }

            if (svcGroupName.equalsIgnoreCase(REST_GROUP))
            {
                return (REST_STR_TYPE);
            }

            return (null);
        }
    }

    //
    // static utility method : getSvcGroupPath(String svcTypeStr)
    //
    // Given a service type string, e.g. "WS", this method returns to the caller
    // fully-qualified service group path, e.g. "UBroker.WS".
    //
    // NOTE - this is for temporary use. It will be replaced when the property
    // manager utility class is implemented.
    //
    //
    public static String getSvcGroupPath(String svcTypeStr)
    {
        String group = null;
        if ((svcTypeStr.equals(WEBSPEED_BROKER) == true) ||
            (svcTypeStr.equals(IPropConst.WS_PERSONALITY ) == true))
        {
            group = ubGroupPath[WS_TYPE];
        }
        else if ((svcTypeStr.equals(APPSRVR_BROKER) == true) ||
                 (svcTypeStr.equals(IPropConst.AS_PERSONALITY ) == true))
        {
            group = ubGroupPath[AS_TYPE];
        }
        else if ((svcTypeStr.equals(NAME_SERVER_TYPE) == true) ||
                 (svcTypeStr.equals(IPropConst.NS_PERSONALITY ) == true))
        {
            group = NAME_SRVR_GROUP;
        }
        else if ((svcTypeStr.equals(ORACLE_DS_BROKER) == true) ||
                 (svcTypeStr.equals(IPropConst.OR_PERSONALITY ) == true))
        {
            group = ubGroupPath[OR_TYPE];
        }
        else if ((svcTypeStr.equals(ODBC_DS_BROKER) == true) ||
                 (svcTypeStr.equals(IPropConst.OD_PERSONALITY ) == true))
        {
            group = ubGroupPath[OD_TYPE];
        }
        else if ((svcTypeStr.equals(MSS_DS_BROKER) == true) ||
                 (svcTypeStr.equals(IPropConst.MSS_PERSONALITY ) == true))
        {
            group = ubGroupPath[MSS_TYPE];
        }
        else if ((svcTypeStr.equals(MSNGR_TYPE) == true) ||
                 (svcTypeStr.equals(IPropConst.MSNGR_PERSONALITY ) == true))
        {
            group = WS_MSNGR_GROUP_PATH;
        }
        else if ((svcTypeStr.equals(ADAPTER_TYPE) == true) ||
                 (svcTypeStr.equals(IPropConst.ADAPTER_PERSONALITY ) == true))
        {
            group = ADAPTER_GROUP;
        }
        else if ((svcTypeStr.equals(AIA_TYPE) == true) ||
                 (svcTypeStr.equals(IPropConst.AIA_PERSONALITY ) == true))
        {
            group = AIA_GROUP;
        }
        else if ((svcTypeStr.equals(WSA_STR_TYPE) == true) ||
                 (svcTypeStr.equals(IPropConst.WSA_PERSONALITY ) == true))
        {
            group = WSA_GROUP;
        }
        else if ((svcTypeStr.equals(REST_STR_TYPE) == true) ||
                (svcTypeStr.equals(IPropConst.REST_PERSONALITY ) == true))
       {
           group = REST_GROUP;
       }

        return group;
    }

    public static String getSvcGroupPath(int svcType)
    {
        if (svcType == WS_TYPE)
        {
            return (ubGroupPath[WS_TYPE]);
        }
        else if (svcType == AS_TYPE)
        {
            return (ubGroupPath[AS_TYPE]);
        }
        else if (svcType == NS_TYPE)
        {
            return (NAME_SRVR_GROUP);
        }
        else if (svcType == OR_TYPE)
        {
            return (ubGroupPath[OR_TYPE]);
        }
        else if (svcType == OD_TYPE)
        {
            return (ubGroupPath[OD_TYPE]);
        }
        else if (svcType == MSS_TYPE)
        {
            return (ubGroupPath[MSS_TYPE]);
        }
        else if (svcType == MS_TYPE)
        {
            return (WS_MSNGR_GROUP_PATH);
        }
        else if (svcType == AD_TYPE)
        {
            return (ADAPTER_GROUP);
        }
        else
        {
            return ((String)null);
        }
    }


    //
    // static utility method to check if the user name is required
    // to start the service sub-process
    //
    public static boolean reqUserName(String SvcGrpPath)
    {
        int type = getSvcTypeFromSvcGrpPath(SvcGrpPath);
        return (type == WS_TYPE || type == AS_TYPE || type == AD_TYPE);
    }

    //
    // static utility method to return the ubroker type in integer form
    // for a give broker group path.
    //
    // For example, if svcGrpPath = "UBroker.WS.wsbroker1", this method
    // return the constant service type value, WS_TYPE, to the caller
    //
    public static int getSvcTypeFromSvcGrpPath(String SvcGrpPath)
    {
        if (SvcGrpPath.indexOf(ubGroupPath[WS_TYPE]) >= 0)
        {
            return (WS_TYPE);
        }
        else if (SvcGrpPath.indexOf(ubGroupPath[AS_TYPE]) >= 0)
        {
            return (AS_TYPE);
        }
        else if (SvcGrpPath.indexOf(NAME_SRVR_GROUP) >= 0)
        {
            return (NS_TYPE);
        }
        else if (SvcGrpPath.indexOf(ubGroupPath[OR_TYPE]) >= 0)
        {
            return (OR_TYPE);
        }
        else if (SvcGrpPath.indexOf(ubGroupPath[OD_TYPE]) >= 0)
        {
            return (OD_TYPE);
        }
        else if (SvcGrpPath.indexOf(ubGroupPath[MSS_TYPE]) >= 0)
        {
            return (MSS_TYPE);
        }
        else if (SvcGrpPath.indexOf(WS_GROUP_PARENT) >= 0)
        {
            return (MS_TYPE);
        }
        else if (SvcGrpPath.indexOf(ADAPTER_GROUP) >= 0)
        {
            return (AD_TYPE);
        }
        else if (SvcGrpPath.indexOf(AIA_GROUP) >= 0)
        {
            return (AIA_INT_TYPE);
        }
        else if (SvcGrpPath.indexOf(WSA_GROUP) >= 0)
        {
            return (WSA_TYPE);
        }
        else if (SvcGrpPath.indexOf(REST_GROUP) >= 0)
        {
            return (REST_TYPE);
        }
        else
        {
            return ( -1);
        }
    }


    //
    //  For a given ubroker service name, and personality string, return the caller
    //  the full spec of the property group name. For example, if the service name is
    //  "wsbroker", the personality string is "UBroker.WS", then this method returns
    //  the full group spec, "UBroker.WS.wsbroker".
    //
    public static String getFullPropPath(String svcName, String personalityStr)
    {
        return (personalityStr + GROUP_SEPARATOR + svcName);
    }

    //
    //  For a given ubroker service name, and personality type, return the caller
    //  the full spec of the property group name. For example, if the service name is
    //  "NS1", the personality type is 5, then this method returns
    //  the full group spec, "NameServer.NS1".
    //
    public static String getFullPropPath(String svcName, int personalityType)
    {
        String personalityStr = "";
        switch (personalityType)
        {
            case ((int) WS_TYPE):
            case ((int) AS_TYPE):
            case ((int) OR_TYPE):
            case ((int) OD_TYPE):
            case ((int) MSS_TYPE):
                personalityStr = ubGroupPath[personalityType];
                break;
            case ((int) NS_TYPE):
                personalityStr = NAME_SRVR_GROUP;
                break;
            case ((int) MS_TYPE):
                personalityStr = WS_MSNGR_GROUP_PATH;
                break;
            case ((int) AD_TYPE):
                personalityStr = ADAPTER_GROUP;
                break;
            case ((int) AIA_INT_TYPE):
                personalityStr = AIA_GROUP;
                break;
            default:
                break;
        }

        if (personalityStr.length() > 0)
        {
            return (personalityStr + GROUP_SEPARATOR + svcName);
        }
        else
        {
            return null;
        }
    }

    /*
     //
     //
     //
     public static UBPropAttributes setUBPropAttrObj()
     {
       if (m_ubPropAttrObj == null)
         m_ubPropAttrObj = new UBPropAttributes();
       return m_ubPropAttrObj;
     }

     //
     // Fetching the data type for the specified property
     //
     public static String getPropertyDataType(String propertyFullSpec)
     {
        try
        {
          return(m_ubPropAttrObj.getPropAttributes(propertyFullSpec));
        }
        catch (Exception e)
        {
        }
        return (null);
     }
     */

    //******************************************************************
     // Default constructor
     //
     public PropMgrUtils()
             throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
     {
         m_parentGroups = null;
         m_propFilePath = null;
         m_currentGroupName = null;
         try
         {
             m_propMgr = new UBProperties();
         }
         catch (PropertyManager.PropertyException e)
         {
             Tools.px(e, "UBroker:  Invaid property.");
         }
         catch (Throwable t)
         {
             Tools.px(t, "UBroker:  Invaid property.");
         }
     }

    //
    // This is so we can bypass the version check when we are loading the property collection in
    // the container side.
    //

    public PropMgrUtils(boolean versionChkOpt)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {
        this();
        m_propMgr.setVersionCheck(versionChkOpt);
    }

    //
    // This is so we can bypass the version check when we are loading the property collection in
    // the container side.
    //
    public PropMgrUtils(boolean versionChkOpt, String propFilePath)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {
        this();
        m_propFilePath = propFilePath;
        m_propMgr.setVersionCheck(versionChkOpt);
        try
        {
            m_loadStatus = loadPropFile(propFilePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LoadPropFileError(m_propFilePath, e.toString());
        }
    }


    // Construct my class also load the content of the specified property files
    //
    public PropMgrUtils(String propFilePath)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {
        this(propFilePath, true, true);
    }

    //
    // Constructor my class specifying the property filename to load, whether the
    // file should be loaded now, and whether the property manager instance should be
    // instantiated with the eventbroker object.
    //
    public PropMgrUtils(String propFilePath, boolean loadNow, boolean useEvtBrkr)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {
        //System.out.println("in PropMgrUtils.constructor...");
        m_parentGroups = null;
        m_propFilePath = propFilePath;
        m_currentGroupName = null;
        try
        {
            m_propMgr = new UBProperties(useEvtBrkr);
        }
        catch (PropertyManager.PropertyException e)
        {
            Tools.px(e, "PropMgrUtils: " + e.getMessage() );
        }
        if (loadNow)
        {
            try
            {
                m_loadStatus = loadPropFile(m_propFilePath, true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LoadPropFileError(m_propFilePath, e.toString());
            }
        }
    }

    //
    // Constructor my class specifying the property filename to load, whether the
    // file should be loaded now, and whether the property manager instance should be
    // instantiated with the eventbroker object.
    //
    public PropMgrUtils(String propFilePath, boolean loadNow)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {
        this(propFilePath, loadNow, true);
    }


    //
    // Construct my class also load the content of the specified property files
    // and add a property filter
    //
    public PropMgrUtils(String propFilePath, IPropertyValueFilter propFilter)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, CantGetPropCollection
    {

        m_parentGroups = null;
        m_propFilePath = propFilePath;
        m_currentGroupName = null;
        try
        {
            m_propMgr = new UBProperties();
        }
        catch (PropertyManager.PropertyException e)
        {
            Tools.px(e, "UBroker:  Invaid property.");
        }
        // Install property filter
        m_propMgr.setGetPropertyFilter(propFilter);

        try
        {
            m_loadStatus = loadPropFile(m_propFilePath, true );
        }
        catch (Exception e)
        {
            throw new LoadPropFileError(m_propFilePath, e.toString());
        }
    }

    public void updateCollectStatsDefault(String newDefault)
    {
        try
        {
            String oldDefault = m_propMgr.getProperty("UBroker.collectStatsData");
            if (newDefault.equals(oldDefault))
            {
                return;
            }

            String flushDefault = m_propMgr.getProperty("UBroker.flushStatsData");
            if (newDefault.equals("1") && flushDefault.equals("0"))
            {
                m_propMgr.putProperty("UBroker.flushStatsData", "255");
            }

            m_propMgr.putProperty("UBroker.collectStatsData", newDefault);
            m_propMgr.save(m_propFilePath, "Update default for UBroker.collectStatsData");
        }
        catch (Exception e)
        {}
    }

    public String getPropFilePath()
    {
        return (m_propFilePath);
    }

    // loads the port numbers from the current properties file into a hash
    // table if they have not already been loaded.  This method is used in
    // validations.
    public Hashtable getPortNumbers()
    {
        if (this.m_portNumberTable == null)
        {
            loadPortNumberTable();
        }
        return (m_portNumberTable);
    }

    private void loadPortNumberTable()
    {

    }

    //
    // This method must guard against customer loading incompatible version of property file.
    // If such a file is detected, then we need to invoke the conversion tool to convert the property
    // file to the correct version so that we can finally load into the property manager.
    //
    public boolean loadPropFile(String propFilePath)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, PropertyVersionException,
            PropertyValueException, PropertySyntaxException
    {
        return loadPropFile(propFilePath, false);
    }

    public boolean loadPropFile(String propFilePath, boolean saveComments)
            throws EnumGroupError, LoadPropFileError, CantGetParentGroup, PropertyVersionException,
            PropertyValueException, PropertySyntaxException
    {
        try
        {
            UBToolsMsg.logMsg(5, "PropMgrUtils.loadPropfile(" + propFilePath + ") at " + (new Date()).toString());
            try
            {
                m_propMgr.load(propFilePath, saveComments, UB_MONITOR_INTERVAL);
            }
            catch (PropertyVersionException e)
            {
                if (DEBUG_TRACE)
                {
                    UBToolsMsg.logMsg(" file load failed with PropertyVersionException");
                }
            }
            catch (PropertySyntaxException exp)
            {
//System.out.println("PropertySyntaxException...");
                throw new LoadPropFileError(propFilePath, exp.toString());
            }
            catch (LoadFileNotFoundException lfnfe)
            {
                try
                {
                    m_propMgr.createPropertiesFileFromSchema(propFilePath);
                }
                catch (Exception e)
                {
                    throw new LoadPropFileError(propFilePath, e.toString());
                }
            }
            catch ( Throwable t ) // tolerate bad value for now.
            {
                t.printStackTrace();
            }

            try
            {
                try
                {
//System.out.println("load s1:" );
                    m_parentGroups = getProperties(PARENT_GROUPS);
                }
                catch (Exception e)
                {
                    ProLog.logdErr("PropMgrUtils", e.getMessage());
                }
//System.out.println("load s2:" );

                m_svcList = m_propMgr.groups();
                m_ubWSservices = getWSSvcGrp();
                m_ubASservices = getASSvcGrp();
                m_nsServices = getNSSvcGrp();
                m_msngrServices = getMsngrSvcGrp();
                m_ubORservices = getORSvcGrp();
                m_ubODservices = getODSvcGrp();
                m_ubMSSservices = getMSSSvcGrp();
                m_adapterServices = getAdapterSvcGrp();
                m_aiaServices = getAiaSvcGrp();
                m_wsaServices = getWsaSvcGrp();
                m_restServices = getRestSvcGrp();
                fetchPreferences();
                m_loadStatus = true;
//System.out.println("load completed at " +  (new Date()).toString());
                return (true);
            }
            catch (Exception e)
            {
//System.out.println("load failure 10:" + e.toString());
                throw new EnumGroupError(e.toString());
            }
        }
        catch (Exception e)
        {
//System.out.println("load failure 20:" + e.toString());
            throw new LoadPropFileError(propFilePath, e.toString());
        }
//    return(false);
    }

    //
    // fetch all the parent group property names
    //
    // Minimal error checkings, method returns null if property file is
    // not loaded, or if for any reason that the parent group tag cannot be found
    // in the property file. (E. Tan, 2/8/98)
    //
    public String[] getParentGroupNames()
            throws CantGetParentGroup, CantGetPropCollection
    {
        if (m_parentGroupNames != null)
        {
            return (m_parentGroupNames);
        }

        //
        // 2BEDONE - error checking
        //
        if (m_loadStatus == true)
        {
            if (m_parentGroups == null)
            {
                try
                {
                    m_parentGroups = getProperties(PARENT_GROUPS);
                }
                catch (Exception e)
                {
                    throw new CantGetParentGroup(PARENT_GROUPS, e.toString());
                }

            }
            else
            {
                m_parentGroups.reset();
            }

            if (m_parentGroups != null)
            {
                Property parentGrpProp;
                int next = 0;
                m_parentGroupNames = new String[m_parentGroups.size()];
                while (m_parentGroups.hasMoreElements())
                {
                    parentGrpProp = (Property) (m_parentGroups.nextElement());
                    if (parentGrpProp != null)
                    {
                        m_parentGroupNames[next++] = parentGrpProp.getName();
                    }
                }
                return m_parentGroupNames;
            }
        }
        return (null);
    }

    //
    // fetch all the parent group property values
    //
    // Minimumal error checkings, method returns null if property file is
    // not loaded, or if for any reason that the parent group tag cannot be found
    // in the property file. (E. Tan, 2/8/98)
    //
    public String[] getParentGroupValues()
            throws CantGetParentGroup, CantGetPropCollection
    {
        if (m_parentGroupValues != null)
        {
            return (m_parentGroupValues);
        }

        //
        // 2BEDONE - error checking
        //
        if (m_loadStatus == true)
        {
            if (m_parentGroups == null)
            {
                try
                {
                    m_parentGroups = getProperties(PARENT_GROUPS);
                }
                catch (Exception e)
                {
                    throw new CantGetParentGroup(PARENT_GROUPS, e.toString());
                }
            }
            else
            {
                m_parentGroups.reset();
            }

            if (m_parentGroups != null)
            {
                Property parentGrpProp;
                int next = 0;
//        System.out.println("# of parentGroups = " + m_parentGroups.size());
                Vector parentGroups = new Vector();

                while (m_parentGroups.hasMoreElements())
                {
                    parentGrpProp = (Property) (m_parentGroups.nextElement());
                    if (parentGrpProp != null)
                    {
                        String propValue = parentGrpProp.getValue();
//            System.out.println(propValue);
//            if ((propValue.indexOf(ORACLE_DS_BROKER) < 0) &&
//               (propValue.indexOf(ODBC_DS_BROKER) < 0) )
                        parentGroups.addElement((Object) propValue);
                    }
                }
                m_parentGroupValues = new String[parentGroups.size()];

                parentGroups.copyInto(m_parentGroupValues);
                return (m_parentGroupValues);
            }
        }
        return (null);
    }


    //
    // fetch the property value of a specified parent group property name
    //
    // Minimumal error checkings, method returns null if property file is
    // not loaded, or if for any reason that the parent group tag cannot be found
    // in the property file. (E. Tan, 2/8/98)
    //
    public String getParentGroupValue(String parentGroupName)
            throws CantGetParentGroup, CantGetPropCollection
    {
        //
        // 2BEDONE - error checking
        //
        if (m_loadStatus == true)
        {
            if (m_parentGroups == null)
            {
                try
                {
                    m_parentGroups = getProperties(PARENT_GROUPS);
                }
                catch (Exception e)
                {
                    throw new CantGetParentGroup(PARENT_GROUPS, e.toString());
                }
            }
            else
            {
                m_parentGroups.reset();
            }

            if (m_parentGroups != null)
            {
//System.out.println("getParentGroupValue(): parentGroupName = " + parentGroupName);
                return (getPropValueFromCollection(parentGroupName, m_parentGroups));
            }
        }
        return (null);
    }


    public String[] getServicesList()
    {
        return (m_svcList);
    }

    public String[] getWSServices()
    {
        return (m_ubWSservices);
    }

    public String[] getASServices()
    {
        return (m_ubASservices);
    }


    public String[] getNSServices()
    {
        return (m_nsServices);
    }


    public String[] getORServices()
    {
        return (m_ubORservices);
    }

    public String[] getAdapterServices()
    {
        return (m_adapterServices);
    }

    public String[] getODServices()
    {
        return (m_ubODservices);
    }

    public String[] getMsngrServices()
    {
        return (m_msngrServices);
    }

    public String[] getMSSServices()
    {
        return (m_ubMSSservices);
    }

    public String[] getAiaServices()
    {
        return (m_aiaServices);
    }

    public String[] getWsaServices()
    {
        return (m_wsaServices);
    }

    public String[] getRestServices()
    {
        return (m_restServices);
    }

    public String[] getSvcGrp(String svcGroupPath,
                              boolean recursive, boolean includeParentName,
                              boolean includeParentGroup)
            throws CantFindGroup
    {
        String[] svcGrpList = null;

        try
        {
            svcGrpList = m_propMgr.groups(svcGroupPath, recursive,
                                          includeParentName, includeParentGroup);
            return svcGrpList;
        }
        catch (Exception e)
        {
            throw new CantFindGroup(svcGroupPath, e.toString());
        }
    }

    //
    // For a given parent group name, return a list of fully-qualified group path
    // name of the parent group.
    //
    // For example, given "WebSpeed", it returns a list of Webspeed broker name
    // strings fully-qualified with parent groups, such as "UBroker.WS.wsbroker1",
    // or "UBroker.WS.wsbroker2".
    //
    public String[] getSvcGrpForParentGrp(String parentGrpProp)
            throws CantFindGroup
    {
        String[] svcList = null;
        String svcGrpPath = null;
        try
        {
            svcGrpPath = getParentGroupValue(parentGrpProp);
//System.out.println("property file is loaded = " + m_loadStatus);
//System.out.println("parentGrpProp = " + parentGrpProp + ", svcGrpPath = " +
//                    svcGrpPath);
            svcList = getSvcGrp(svcGrpPath,
                                false, // no recursion,
                                true, // do includeParentName,
                                false // don't includeParentGroup
                      );
        }
        catch (Exception e)
        {
            throw new CantFindGroup(svcGrpPath, null);
        }
        return svcList;
    }

    //
    // For a given parent group name, return a list of of service names of the
    // parent group.
    //     String svcGrpPath = getParentGroupValue(parentGrpProp);

    // For example, given "WebSpeed", it returns a list of Webspeed broker name
    // strings, such as "wsbroker1".
    //
    public String[] getSvcNameForParentGrp(String parentGrpProp)
            throws CantFindGroup
    {
        String[] svcList = null;

        if (parentGrpProp != null)
        {
            try
            {
                svcList = getSvcGrp(parentGrpProp,
                                    false, // no recursion,
                                    false, // do includeParentName,
                                    false // don't includeParentGroup
                          );

            }
            catch (Exception e)
            {
                throw new CantFindGroup(parentGrpProp, null);
            }
        }
        return svcList;
    }

    //
    //  For a property Groupservice type String, returns a list of service instances
    //  for the service type.
    //  For example, given typeStr = UBroker.WS, if the default ubroker.properties
    //  is loaded, this method returns a single-element string array: UBroker.WS.wsbroker1.
    //
    private String[] getServiceInstanceByType(String propGroupSpec)
            throws CantFindGroup
    {
        String[] svcList = null;
        try
        {
            svcList = getSvcGrp(propGroupSpec,
                                false, // no recursion,
                                true, // do includeParentName,
                                false // don't includeParentGroup
                      );

        }
        catch (Exception e)
        {
            throw new CantFindGroup(propGroupSpec, null);
        }
        return svcList;
    }

    public String[] getWSSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ubGroupPath[WS_TYPE]));
    }


    public String[] getASSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ubGroupPath[AS_TYPE]));
    }

    public String[] getORSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ubGroupPath[OR_TYPE]));
    }

    public String[] getODSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ubGroupPath[OD_TYPE]));
    }

    public String[] getMSSSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ubGroupPath[MSS_TYPE]));
    }

    public String[] getNSSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(NAME_SRVR_GROUP));
    }

    public String[] getMsngrSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(WS_MSNGR_GROUP_PATH));
    }

    public String[] getAdapterSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(ADAPTER_GROUP));
    }

    public String[] getAiaSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(AIA_GROUP));
    }

    public String[] getWsaSvcGrp()
            throws CantFindGroup
    {
        return (getServiceInstanceByType(WSA_GROUP));
    }

    public String[] getRestSvcGrp()
            throws CantFindGroup
    {
        return null; // until REST Adapter is supported
        //return (getServiceInstanceByType(REST_GROUP));
    }

    //
    //  get*SvcGrpAll() methods returns a list of the property groups
    //  that including the property group in the hierarchy. For example
    //  calling getWSSvcGrpAll() with a default ubroker.properties files loaded,
    //  it returns the following list of property groups:
    //        UBroker
    //        UBroker.WS
    //        UBroker.WS.wsbroker1
    //
    public String[] getWSSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ubGroupPath[WS_TYPE]));
    }

    public String[] getASSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ubGroupPath[AS_TYPE]));
    }

    public String[] getORSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ubGroupPath[OR_TYPE]));
    }

    public String[] getODSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ubGroupPath[OD_TYPE]));
    }

    public String[] getMSSSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ubGroupPath[MSS_TYPE]));
    }

    public String[] getNSSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(NAME_SRVR_GROUP));
    }

    public String[] getAdapterSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(ADAPTER_GROUP));
    }

    public String[] getAiaSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(AIA_GROUP));
    }

    public String[] getWsaSvcGrpAll()
            throws CantFindGroup
    {
        return (getSvcGrpAll(WSA_GROUP));
    }

    public String[] getRestSvcGrpAll()
            throws CantFindGroup
    {
        return null;  // until REST Adapter is supported
        //return (getSvcGrpAll(REST_GROUP));
    }

    private String[] getSvcGrpAll(String groupName)
            throws CantFindGroup
    {
        String[] svcList = null;
        try
        {
            svcList = getSvcGrp(groupName,
                                true, // do recursion
                                true, // do includeParentName,
                                true // do includeParentGroup
                      );

        }
        catch (Exception e)
        {
            throw new CantFindGroup(groupName, null);
        }
        return svcList;

    }


    public Vector getAllSvcInstances()
    {
        Vector svcVect = new Vector();
        int next;

        /* We need to test if the variable is null before looping through it */

        if (m_ubWSservices != null)
        {
            for (next = 0; next < m_ubWSservices.length; next++)
            {
                svcVect.addElement(m_ubWSservices[next]);
            }
        }

        if (m_ubASservices != null)
        {
            for (next = 0; next < m_ubASservices.length; next++)
            {
                svcVect.addElement(m_ubASservices[next]);
            }
        }

        if (m_ubORservices != null)
        {
            for (next = 0; next < m_ubORservices.length; next++)
            {
                svcVect.addElement(m_ubORservices[next]);
            }
        }

        if (m_ubODservices != null)
        {
            for (next = 0; next < m_ubODservices.length; next++)
            {
                svcVect.addElement(m_ubODservices[next]);
            }
        }

        if (m_nsServices != null)
        {
            for (next = 0; next < m_nsServices.length; next++)
            {
                svcVect.addElement(m_nsServices[next]);
            }
        }

        if (m_adapterServices != null)
        {
            for (next = 0; next < m_adapterServices.length; next++)
            {
                svcVect.addElement(m_adapterServices[next]);
            }
        }

        if (m_aiaServices != null)
        {
            for (next = 0; next < m_aiaServices.length; next++)
            {
                svcVect.addElement(m_aiaServices[next]);
            }
        }

        if (m_ubMSSservices != null)
        {
            for (next = 0; next < m_ubMSSservices.length; next++)
            {
                svcVect.addElement(m_ubMSSservices[next]);
            }
        }
        if (m_wsaServices != null)
        {
            for (next = 0; next < m_wsaServices.length; next++)
            {
                svcVect.addElement(m_wsaServices[next]);
            }
        }
        if (m_restServices != null)
        {
            for (next = 0; next < m_restServices.length; next++)
            {
                svcVect.addElement(m_restServices[next]);
            }
        }

        return svcVect;
    }


    //
    // method for fetching the property collection of an instance
    // of a service.
    //
    public PropertyCollection getProperties(String propGroupName)
            throws CantGetPropCollection
    {
        try
        {
            PropertyCollection properties = m_propMgr.properties(propGroupName, true, true);
            return (properties);
        }
        catch (Exception e)
        {
            throw new CantGetPropCollection(propGroupName, e.toString());
        }
    }

    //
    // method for fetching the property collection of an instance
    // of a service.
    //
    public PropertyCollection getPropertiesNoAncestor(String svcName)
            throws CantGetPropCollection
    {
        try
        {
            PropertyCollection properties = m_propMgr.properties(svcName, false, false);
            return (properties);
        }
        catch (Exception e)
        {
            throw new CantGetPropCollection(svcName, e.toString());
        }
    }

    //
    // For a service name, find its parent property group.
    // We search thru our cached list of service group path:
    //  m_ubWSservices, m_ubASservices,
    //  m_ubORservices, m_ubODservices for a match.  This method
    // assumes that service names are unique.  So the first matching
    // property group name's personality string is returned.
    //
    // For example, given svcName = wsborker1, we found the property group in
    // UBroker.WS.wsbroker1, this method returns two pieces of information to the
    // caller:
    //  1. WS as a personality string for ubroker wsbroker1.
    //  2. The fully-qualified property group path for wsbroker1: UBroker.WS.wsbroker1.
    //
    public PropGroupDescriptor findUBPersonStrForSvcName(String svcName)
    {
        String[] retList = new String[2];
        String grpPath = null;

        if (m_ubWSservices != null)
        {
            grpPath = matchSvcName(m_ubWSservices, svcName);

            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_ubASservices != null)
        {
            grpPath = matchSvcName(m_ubASservices, svcName);

            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_nsServices != null)
        {
            grpPath = matchSvcName(m_nsServices, svcName);

            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_msngrServices != null)
        {
            grpPath = matchSvcName(m_msngrServices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_ubORservices != null)
        {
            grpPath = matchSvcName(m_ubORservices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_ubODservices != null)
        {
            grpPath = matchSvcName(m_ubODservices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_adapterServices != null)
        {
            grpPath = matchSvcName(m_adapterServices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_aiaServices != null)
        {
            grpPath = matchSvcName(m_aiaServices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_ubMSSservices != null)
        {
            grpPath = matchSvcName(m_ubMSSservices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_wsaServices != null)
        {
            grpPath = matchSvcName(m_wsaServices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        if (m_restServices != null)
        {
            grpPath = matchSvcName(m_restServices, svcName);
            if (grpPath != null)
            {
                return new PropGroupDescriptor(grpPath);
            }
        }

        return (null);
    }

    //
    // updateSvcNameCache
    //
    // For a specific ubroker personality, update our internal service name
    // cache.
    //
    public void updateSvcNameCache(String svcGroupPath)
            throws EnumGroupError
    {
        int svcType = getSvcTypeFromSvcGrpPath(svcGroupPath);

        try
        {
            switch (svcType)
            {
                case ((int) WS_TYPE):
                    m_ubWSservices = getWSSvcGrp();
                    break;
                case ((int) AS_TYPE):
                    m_ubASservices = getASSvcGrp();
                    break;
                case ((int) OR_TYPE):
                    m_ubORservices = getORSvcGrp();
                    break;
                case ((int) OD_TYPE):
                    m_ubODservices = getODSvcGrp();
                    break;
                case ((int) MSS_TYPE):
                    m_ubMSSservices = getMSSSvcGrp();
                    break;
                case ((int) NS_TYPE):
                    m_nsServices = getNSSvcGrp();
                    break;
                case ((int) AD_TYPE):
                    m_adapterServices = getAdapterSvcGrp();
                    break;
                case ((int) AIA_INT_TYPE):
                    m_aiaServices = getAiaSvcGrp();
                    break;
                case ((int) WSA_TYPE):
                    m_wsaServices = getWsaSvcGrp();
                    break;
                case ((int) REST_TYPE):
                    m_restServices = getRestSvcGrp();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            throw new EnumGroupError(e.toString());
        }
    }

    //
    //*********************************************************
     // methods for retrieving and saving individual property
     //**********************************************************
      //

      //
      //
      public String getPropValueFromCollection(String propName,
                                               PropertyCollection theCollection)
      {
          String propValue = null;
          if ( theCollection != null )
          {
              theCollection.reset();
              Property prop = theCollection.get(propName);
              if (prop != null)
              {
                  propValue = prop.getValue();
              }
          }
          return propValue;
      }


    public String getPropDefaultValueFromCollection(String propName,
            PropertyCollection theCollection)
    {
        String propValue = null;
        if ( theCollection != null )
        {
            theCollection.reset();
            Property prop = theCollection.get(propName);
            if (prop != null)
            {
                propValue = prop.getDefaultValue();
            }
        }
          return propValue;
    }

    public String getPropValueOrDefaultFromCollection(String propName,
            PropertyCollection theCollection)
    {
        String propValue = null;
        if ( theCollection != null )
        {
            theCollection.reset();
            Property prop = theCollection.get(propName);
            if (prop != null)
            {
                propValue = prop.getValueOrDefault();
            }
        }
        return propValue;
    }

    public String[] getPropEnumListFromCollection(String propName,
                                                  PropertyCollection theCollection)
    {
//System.out.println("getting Enum property for " + propName);
        EnumProperty prop = (EnumProperty) (theCollection.get(propName));
        if (prop != null)
        {
            return (prop.getEnum());
        }
        else
        {
            return (null);
        }
    }

    public String getIntPropValueFromCollection(String propName,
                                                PropertyCollection theCollection)
    {
        IntProperty prop = (IntProperty) (theCollection.get(propName));
        if (prop != null)
        {
            return (prop.getValue());
        }
        else
        {
            return (null);
        }
    }


    //
    // This method returns a string array that is the value of the property.
    // This value string in the property file is stored in the format of
    // a list of values listed in a string and delimited by a comma.
    // For example if a property foo, has the following property value:
    //
    //   foo=n1,n2,n3
    //
    // This method return a string array x with :
    //
    //   x[0] = "n1"
    //   x[1] = "n2".. so on.
    //
    public String[] getArrayPropValue(String propertyName)
    {
//System.out.println("propertyName = " + propFullPath(propertyName));

        return m_propMgr.getArrayProperty(propFullPath(propertyName));
    }

    //
    // Update the array property with a list of new names.  PropertyManager
    // object will handle putting the name list together separated each with
    // the delimiter character. The resulting string is the new value of
    // the specified property.
    //
    public void putArrayPropValue(String propertyName, String[] newList)
            throws CantPutProperties
    {
        try
        {
//System.out.println("propFullPath(propertyName) = " + propFullPath(propertyName));
//for(int i = 0; i < newList.length; i++)
//  System.out.println("newList[" + i + "] = " + newList[i]);
            m_propMgr.putArrayProperty(propFullPath(propertyName), newList);
        }
        catch (Exception putPropException)
        {
            throw new CantPutProperties(putPropException.toString());
        }
    }

    //
    // for a list of properties and their new values, update the property manager
    // on the new values, directly.
    //
    public void putProperties(String[] propNames, String[] newValues)
            throws CantPutProperties
    {
        int numProperties = propNames.length;

        try
        {
            for (int i = 0; i < numProperties; i++)
            {
                boolean putStatus = m_propMgr.putProperty(propFullPath(propNames[i]), newValues[i]);
            }
        }
        catch (Exception putPropException)
        {
            throw new CantPutProperties(putPropException.toString());
        }
    }

    public void putProperties(String[] propNames, String[] newValues,
                              PropertyCollection theCollection)
            throws CantPutProperties
    {
        int numProperties = propNames.length;
        try
        {
            for (int i = 0; i < numProperties; i++)
            {
                putProperty(propFullPath(propNames[i]), newValues[i], theCollection);
            }
        }
        catch (Exception e)
        {
            throw new CantPutProperties(e.toString());
        }
    }

    public boolean putProperty(String propName, String newValue,
                               PropertyCollection theCollection)
            throws CantPutProperty
    {
        Property prop = theCollection.get(propName);

        if (prop != null)
        {
            try
            {
                prop.setValue(newValue);
                return (true);
            }
            catch (Exception expt)
            {
                throw new CantPutProperty(propName, newValue, expt.toString());
//        return (false);
            }
        }
        else
        {
            return (false);
        }

    }


    public String getPropertyValue(String propName)
            throws CantGetPropertyValue
    {
        return getPropertyValue(propName, true);
    }

    public String getPropertyValue(String propName, boolean doFilter)
            throws CantGetPropertyValue
    {
        try
        {
            String propValue = m_propMgr.getProperty(propFullPath(propName), doFilter);
            return propValue;
        }
        catch (Exception getPropException)
        {
            throw new CantGetPropertyValue(propName, getPropException.toString());
        }
    }

    public void putPropertyValue(String propName, String newValue)
            throws CantPutPropertyValue
    {
        try
        {
//System.out.println("putting " + propName + " = " + newValue);
            m_propMgr.putProperty(propFullPath(propName), newValue);
        }
        catch (Exception getPropException)
        {
            throw new CantPutPropertyValue(propName, newValue, getPropException.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // getFullPathPropertyValue
    //
    // Gets the value of a property given it's full path name.
    // For example, if you wanted port number for asbroker1,
    // provide a string like this Ubroker.AS.asbroker.portNumber
    // returns the property value, null or throws an exception.
    public String getFullPathPropertyValue(String fullPropPath)
            throws CantGetPropertyValue
    {
        try
        {
            String propValue = m_propMgr.getProperty(fullPropPath);
            return propValue;
        }
        catch (Exception getPropException)
        {
            throw new CantGetPropertyValue(fullPropPath, getPropException.toString());
        }
    }


    public String propFullPath(String propName)
    {
        if (m_currentGroupName != null)
        {
            return (m_currentGroupName + GROUP_SEPARATOR + propName);
        }
        else
        {
            return (null);
        }
    }

    public void setCurrentGroupName(String groupName)
    {
        m_currentGroupName = groupName;
    }

    public String getCurrentGroupName()
    {
        return (m_currentGroupName);
    }

    public void saveProperties(String groupPath, Hashtable properties)
            throws Exception
    {
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements())
        {
            String name = (String) keys.nextElement();
            String fullName = groupPath + "." + name;
            Object newValObject = properties.get(name);
            String newVal = newValObject.toString();
            String oldVal = null;
            // Only save the property if it has changed.
            try
            {
                oldVal = m_propMgr.getProperty(fullName);
            }
            catch (Exception e)
            {
                ; // no value
            }
            if (newVal == null)
            {
                newVal = "";
            }
            if (oldVal == null)
            {
                oldVal = "";
            }
            if (!newVal.equals(oldVal))
            {
                try
                {
                    m_propMgr.putProperty(fullName, newVal);
                    if (newValObject instanceof Integer)
                    {
                        m_propMgr.putIntProperty(fullName, ((Integer) newValObject).intValue());
                    }
                    else
                    {
                        m_propMgr.putProperty(fullName, newVal);
                    }
                }
                catch (Exception e)
                {
                    ProLog.logdErr("Ubroker.properties", 3, "Could not save property " + fullName);
                }
            }
        }
        m_propMgr.save(m_propFilePath, "Update property for " + groupPath);
    }

    public void saveAll(String saveFilePath, String header)
            throws SaveAllError
    {
        try
        {
//System.out.println("saving property file changes for " + saveFilePath+ "," + header);
            m_propMgr.save(saveFilePath, header);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SaveAllError(e.toString());
        }
    }

    public void saveGroup(String saveFilePath, String header, String parentGroupName,
                          boolean recursiveOpt, boolean includeRegOpt,
                          boolean includeAncestorOpt)
            throws SaveGroupError
    {
        try
        {
            m_propMgr.save(saveFilePath, header, parentGroupName, recursiveOpt, includeRegOpt,
                           includeAncestorOpt);
        }
        catch (Exception e)
        {
            throw new SaveGroupError(parentGroupName, saveFilePath, e.toString());
        }
    }

    public void removeGroup(String svcName, String svcGroupPath)
            throws RemoveGroupError
    {
        removeGroup(svcGroupPath + GROUP_SEPARATOR + svcName);
        return;
    }

    public void removeGroup(String propGroupPath)
            throws RemoveGroupError
    {
        try
        {
//System.out.println("Removing property group: " + propGroupPath);
            m_propMgr.removeGroup(propGroupPath);
            updateInstanceList(propGroupPath);
        }
        catch (Exception e)
        {
            throw new RemoveGroupError(propGroupPath, e.toString());
        }

    }


    //
    //  Methods, saveGroupPropForRemote() and loadRemoteGroupProp()
    //  are mainly used to make it more efficient to manipulate a property
    //  collection of a specific unified broker instance. The property
    //  file usually lives on the same host where AdminServer runs. The
    //  Gui tool very likely runs on a different remote host.
    //
    //  To avoid turning accesses to property collection into rmi calls,
    //  we opted to export the part of the property file that is relevant
    //  to the remote object into a char array.  Upon the invocation
    //  of the tool's remote object, the char array for its properties
    //  will be created.  This piece of information will be sent from the
    //  remote object to the GUI object on the other side of the wire via
    //  rmi object serialization.
    //
    //  During the instantiation of the GUI object, this property char array
    //  is sent to the local Property Manager to load, just as if its properties
    //  is read from a property file.
    //

    //
    // Saving the sections of the property file for a specific group name
    // to a Char Array.
    //
    // WORKAROUND ALERT: In order to maintain the hierarchical order of the
    // property groups for a specific instance of property group within the
    // char-array stream, we need to save property groups for all level.
    // For examples, if we are trying to save a property group name,
    // [UBroker.WS.wsbroker1], we need to save the properties for the following
    // groups:
    //     [UBroker.WS.wsbroker1]
    //     [UBroker.WS]
    //     [UBroker]
    //
    // If envSubKey is non-null, we also save properties for the relevant
    // environment group and subgroup to the stream.
    //
    public char[] saveGroupPropForRemote(String groupPathName, String envSubKey)
            throws SaveGroupForRemoteError
    {
        //
        // 2BEDONE - tone the writer's initial size
        //
        CharArrayWriter myPropWriter = new CharArrayWriter(500);
        try
        {
            boolean recurse = true;
            do
            {
                m_propMgr.save((Writer) myPropWriter,
                               "Exporting properties for [" + groupPathName + "]",
                               groupPathName,
                               recurse, // recursive,
                               false, // not includeAllRegistered,
                               false, // not includeAncestors
                               false // not save comments
                        );
//System.out.println("new property stream to save for " + groupPathName + "(recurse = " + recurse + ")");
                groupPathName = m_propMgr.getParentName(groupPathName);
                recurse = false;
            }
            while (groupPathName != null);

//System.out.println("new property stream to save :\n" + myPropWriter.toString());
//System.out.println("envSubKey = " + envSubKey);
            if (envSubKey != null)
            {
                recurse = true;
                String envGroupPath = ENVIRONMENT + GROUP_SEPARATOR + envSubKey;
                do
                {
//System.out.println("Saving properties for " + envGroupPath);
                    m_propMgr.save((Writer) myPropWriter,
                                   "",
                                   envGroupPath,
                                   recurse, // recursive,
                                   false, // not includeAllRegistered,
                                   false, // not includeAncestors
                                   false); // not save comments
                    envGroupPath = m_propMgr.getParentName(envGroupPath);
                    recurse = false;
                }
                while (envGroupPath != null);
            }

            //
            // Add in the Preference group.
            //
            m_propMgr.save((Writer) myPropWriter,
                           "",
                           PREFERENCEROOT_GROUP,
                           false, // recursive,
                           false, // not includeAllRegistered,
                           false, // not includeAncestors
                           false); // not save comments

//System.out.println("new property stream to save :\n" + myPropWriter.toString());
            return (myPropWriter.toCharArray());
        }
        catch (Exception e)
        {
//System.out.println("saveGroupPropForRemote exception " + e.toString());
            throw new SaveGroupForRemoteError(groupPathName, e.toString());
//      return(null);
        }

    }

    public char[] savePrefPropForRemote()
            throws SaveGroupForRemoteError
    {
        //
        // 2BEDONE - tone the writer's initial size
        //
        CharArrayWriter myPropWriter = new CharArrayWriter(500);
        try
        {
            m_propMgr.save((Writer) myPropWriter,
                           "",
                           PREFERENCEROOT_GROUP,
                           false, // recursive,
                           false, // not includeAllRegistered,
                           false, // not includeAncestors
                           false); // not save comments
//System.out.println("new property stream to save :\n" + myPropWriter.toString());
            return (myPropWriter.toCharArray());
        }
        catch (Exception e)
        {
            throw new SaveGroupForRemoteError(PREFERENCEROOT_GROUP, e.toString());
        }
    }

    /*
      // for debugging purpose only
      public String saveGroupPropToString(String groupPathName)
      {
        //
        // 2BEDONE - tone the writer's initial size
        //
        CharArrayWriter myPropWriter = new CharArrayWriter(500);
        try
        {
          m_propMgr.save((Writer)myPropWriter,
                          "Exporting properties for [" + groupPathName + "]",
                          groupPathName,
                          true,                  // recursive,
                          false,                  // includeAllRegistered
                          true                   // includeAncestors
                        );
          return(myPropWriter.toString());
        }catch (Exception e)
        {
          System.out.println(e.toString());
        }
        return(null);

      }
     */
    //
    //
    // Load the property of a specific group name from a char array of
    // properties.
    //
    public void loadRemoteGroupProp(char[] remoteGroupProperties, String newOrKnown, String ubrokerName)
            throws CantLoadPropertiesRemote
    {
        try
        {
            CharArrayReader propReader = new CharArrayReader(remoteGroupProperties);
            m_propMgr.load((Reader) propReader);
//System.out.println("successfully loading properties for " + ubrokerName + " from remote stream");
        }
        catch (Exception e)
        {
//System.out.println("failed to load properties from stream");
            throw new CantLoadPropertiesRemote(newOrKnown, ubrokerName,
                                               e.toString());
            //System.out.println(e.toString());
        }

    }

    public void updateRemoteGroupProp(char[] remoteGroupProperties, String newOrKnown,
                                      String ubrokerName)
            throws CantLoadPropertiesRemote
    {
        try
        {
            CharArrayReader propReader = new CharArrayReader(remoteGroupProperties);
            m_propMgr.update((Reader) propReader);
//System.out.println("successfully updating properties for " + ubrokerName + " from remote stream");
        }
        catch (Exception e)
        {
            throw new CantLoadPropertiesRemote(newOrKnown, ubrokerName,
                                               e.toString());
        }
    }


    //
    // validate a list of property changes
    // An error object is returned to the caller
    //
    public CfgValidateErrs validateProperties(PropertiesSaveDescriptor changesObj)
    {
        try
        {
            String propGroupFullSpec = changesObj.getPropGroupName() + GROUP_SEPARATOR + changesObj.getSvcName();
//System.out.println("validating property for " + propGroupFullSpec);
            Vector propVector = changesObj.getPropList();
            if (propVector.size() > 0)
            {
                String[] propList = new String[propVector.size()];
                propVector.copyInto(propList);
                Vector valueVector = changesObj.getValueList();
                String[] valueList = new String[valueVector.size()];
                valueVector.copyInto(valueList);
//System.out.println(" validating " + propList.length + " properties.");
//
// for (int j = 0;  j < propList.length; j++)
// {
//     System.out.println("validating " + propList[j] + " = " + valueList[j]);
// }
                UBPropValidate propValidator = new UBPropValidate(this, propGroupFullSpec,
                        propList, valueList);
                propValidator.validatePropList();
                CfgValidateErrs valErrs = propValidator.getValMessageObject();
                String[] badProps = valErrs.getInvalidProplist();
                Integer[] badErrCodes = valErrs.getErrCodeList();
                String[] msgs = valErrs.getErrMsgList();
                /*
                      if (badProps.length > 0)
                      {
                        for (int i = 0; i < badProps.length; i++)
                        {
                          System.out.println("found bad " + badProps[i] + " - " + msgs[i]);
                        }
                      }
                 System.out.println(" Has validation done?" + valErrs.isCfgValidated());
                 */
                return valErrs;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new CfgValidateErrs();
    }


    //
    //  validate a single property value
    //
    public CfgValidateErrs validateOneProperty(String propGrpFullSpec, String propertyName,
                                               String newValue)
    {
        UBPropValidate propValidator = new UBPropValidate(this, propGrpFullSpec,
                propertyName, newValue);
        propValidator.validateProp();

        CfgValidateErrs valErrs = propValidator.getValMessageObject();

        return valErrs;
    }

    //
    // This method serves as a wrapper method for saveGroupPropForRemote(), so
    // that the detail of how the fully-qualified groupname is formed is not exposed.
    //
    // Each broker property class may have a linkage to a subgroup under [Environment].
    // When getting the property stream for a specific instance, this method must
    // figure out whether the linkage exists.  If it does, then it must also save the
    // properties of the Environment subclass to the same stream.
    //
    // For example: if we are getting properties for [UBroker.WS.wsbroker1], and
    // the following environment property exists in this instance of broker:
    //
    //     environment=wsbroker1
    //
    // Then this method must also cause the property [Environment.wsbroker1] and
    // [Environment] to be saved in the stream.
    //
    public char[] getPropertiesStream(String ubtGroupPath, String ubtName)
            throws SaveGroupForRemoteError
    {
        String fullGroupPath = null;

        if (ubtName != null)
        {
            fullGroupPath = ubtGroupPath + GROUP_SEPARATOR + ubtName;
        }
        else
        {
            fullGroupPath = ubtGroupPath;
        }
        return (getPropertiesStream(fullGroupPath));
    }

    public char[] getPropertiesStream(String ubtFullGroupPath)
            throws SaveGroupForRemoteError
    {
//System.out.println("getPropetiesStream(" + ubtFullGroupPath + ")");
        String envSubKeyStr = getEnvSubKeyStr(ubtFullGroupPath);
        return (saveGroupPropForRemote(ubtFullGroupPath, envSubKeyStr));
    }


    public String getEnvSubKeyStr(String ubtFullGroupPath)
    {
        String envSubKeyStr = null;
        try
        {
//System.out.println(" getEnvSubKeyStr() ubtFullGroupPath = " + ubtFullGroupPath);
            envSubKeyStr = m_propMgr.getProperty(ubtFullGroupPath +
                                                 GROUP_SEPARATOR + ENVIRONMENT);
            if (envSubKeyStr.trim().length() == 0)
            {
                envSubKeyStr = null;
            }
        }
        catch (Exception e)
        {
            //System.out.println("Cannot get Environment property for " + ubtFullGroupPath);
        }
//System.out.println(" getEnvSubKeyStr("+ ubtFullGroupPath + ")= " + envSubKeyStr);
        return envSubKeyStr;
    }

    public String getFullEnvSubKeyStr(String svcName, int svcType)
    {
        return (ENV_GROUP_PARENT + GROUP_SEPARATOR +
                getEnvSubKeyStr(getSvcGroupPath(svcType) + GROUP_SEPARATOR + svcName));
    }


    public boolean isPropertyFileLoaded()
    {
        return (m_loadStatus);
    }

    //
    // For a property collection of a given ubroker instance, this method returns the
    // corresponding property collection for its customized environment group.
    //
    // Under a property group of a specific broker, there is an environment property:
    //    ...
    //    environment=wsbroker1
    //    ...
    //
    // The value for the envionment property: wsbroker1, is then used to form the
    // property subgroup name under the Environment parent property group, as:
    //    [Environment.wsbroker1]
    //
    // This method then fetches the property collection for the environment sub group
    // that is relevant to the broker instance.  As the example described above, the
    // property collection for "Environment.wsbroker1" is returned to the caller.
    //
    // If for any reason, the given property collection does not contain the key environment
    // property, or we fail to fetch the property collection for the environment subgroup,
    // then null is returned.
    //
    public PropertyCollection getCustomizedEnvironment(PropertyCollection ubProperties)
    {
        String envSubGroupName = getPropValueFromCollection(ENVIRONMENT, ubProperties);
//	   System.out.println("Getting Environment: " + envSubGroupName);
        PropertyCollection ubEnvProperties = null;

        if (envSubGroupName != null)
        {
            try
            {
                String envPropGroupSpec = ENV_GROUP_PARENT + GROUP_SEPARATOR + envSubGroupName;
//		     System.out.println(" envPropGroupSpec: " + envPropGroupSpec);
                ubEnvProperties = getProperties(envPropGroupSpec);
                m_currentGroupName = ENV_GROUP_PARENT + GROUP_SEPARATOR + envSubGroupName;
//         System.out.println("Setting GroupName: " + m_currentGroupName);
            }
            catch (CantGetPropCollection e)
            {
                // do nothing at the moment
            }
            catch (Exception e)
            {
                // do nothing at the moment
            }
        }
        return (ubEnvProperties);
    }


    //////////////////////////////////////////////////////////////////////////
    // getCustomizedEnvironment
    //
    // (overloaded version: accepts the name of the environment collection
    //  instead of extracting it from a property collection passed in.)
    //
    // For a property collection of a given ubroker instance, this method returns the
    // corresponding property collection for its customized environment group.
    //
    // Under a property group of a specific broker, there is an environment property:
    //    ...
    //    environment=wsbroker1
    //    ...
    //
    // The value for the envionment property: wsbroker1, is then used to form the
    // property subgroup name under the Environment parent property group, as:
    //    [Environment.wsbroker1]
    //
    // This method then fetches the property collection for the environment sub group
    // that is relevant to the broker instance.  As the example described above, the
    // property collection for "Environment.wsbroker1" is returned to the caller.
    //
    // If for any reason, the given property collection does not contain the key environment
    // property, or we fail to fetch the property collection for the environment subgroup,
    // then null is returned.
    //
    public PropertyCollection getCustomizedEnvironment(String envSubGroupName)
    {
        //String envSubGroupName = getPropValueFromCollection(ENVIRONMENT, ubProperties);
        //	   System.out.println("Getting Environment: " + envSubGroupName);
        PropertyCollection ubEnvProperties = null;

        if (envSubGroupName != null)
        {
            try
            {
                String envPropGroupSpec = ENV_GROUP_PARENT + GROUP_SEPARATOR + envSubGroupName;
//		     System.out.println(" envPropGroupSpec: " + envPropGroupSpec);
                ubEnvProperties = getProperties(envPropGroupSpec);
                m_currentGroupName = ENV_GROUP_PARENT + GROUP_SEPARATOR + envSubGroupName;
                //System.out.println("Setting GroupName: " + m_currentGroupName);
            }
            catch (CantGetPropCollection e)
            {
                // do nothing at the moment
            }
            catch (Exception e)
            {
                // do nothing at the moment
            }
        }
        return (ubEnvProperties);
    }


    //
    // For a given property collection of a specified unified broker or
    // nameServer, this method figures out the environment key for the service
    // and then use the key to fetch the relevant environment section. It
    // returns a two dimension array to represent the name and value pair
    // of the environment variables that are defined in the specific
    // environment property group. Each row of the two-dimensional array
    // represents a name and value string pair.  Column 0 of the row is the
    // name of the environment variable, and column 1 is the designated value
    // of the environment variable.
    //
    public synchronized String[][] getCustomizedEnvVars(PropertyCollection ubProperties)
    {
        return (getCustomizedEnvVars(ubProperties, false));
    }

    public synchronized String[][] getCustomizedEnvVars(PropertyCollection ubProperties,
            boolean expandVarOpt)
    {
        // Get CustomizedEnvironment changes current group to Environmen.*
        // This is required to get filtered property values
        String saveGroup = m_currentGroupName;
        PropertyCollection envProperties = null;
        try
        {
            envProperties = getCustomizedEnvironment(ubProperties);
        }
        catch (Exception e)
        {
        }
//System.out.println("envProperties = null? " + (envProperties == null));
        if (envProperties != null)
        {
            String[][] envVars;
            if (expandVarOpt)
            {
                envVars = expandEnvVars(envProperties, false);
            }
            else
            {
                envVars = getEnvVars(envProperties);
            }
            m_currentGroupName = saveGroup;
            return (envVars);
        }
        else
        {
            return null;
        }
    }

    public synchronized String[][] getCustomizedEnvVars(String envSubGroupName)
    {
        return getCustomizedEnvVars(envSubGroupName, true);
    }

    public synchronized String[][] getCustomizedEnvVars(String envSubGroupName,
            boolean expandVarOpt)
    {
        // Get CustomizedEnvironment changes current group to Environmen.*
        // This is required to get filtered property values
        String saveGroup = m_currentGroupName;
        PropertyCollection envProperties = null;
        try
        {
            envProperties = getCustomizedEnvironment(envSubGroupName);
        }
        catch (Exception e)
        {
        }
//System.out.println("envProperties = null? " + (envProperties == null));
        if (envProperties != null)
        {
            String[][] envVars;
            if (expandVarOpt)
            {
                envVars = expandEnvVars(envProperties, true);
            }
            else
            {
                envVars = getEnvVars(envProperties);
            }
            m_currentGroupName = saveGroup;
            return (envVars);
        }
        else
        {
            return null;
        }
    }

    private String[][] getEnvVars(PropertyCollection envProperties)
    {
        int numEnvVars = envProperties.size();
        String[][] envVars = new String[numEnvVars][2];
        int nextVar = 0;
        while (envProperties.hasMoreElements())
        {
            Property envProp = (Property) envProperties.nextElement();
            envVars[nextVar][0] = envProp.getName();
            try
            {
                envVars[nextVar][1] = getPropertyValue(envVars[nextVar][0]);
//System.out.println("env: " + envVars[nextVar][0] + " = " + envVars[nextVar][1]);
            }
            catch (Exception e)
            {
                envVars[nextVar][1] = null;
            }
            nextVar++;
        }
        return envVars;
    }

    private String[][] expandEnvVars(PropertyCollection envProperties, boolean nullIfCantExpand)
    {
        int numEnvVars = envProperties.size();
        String[][] envVars = new String[numEnvVars][2];
        int nextVar = 0;
        m_propMgr.setGetPropertyFilter(m_propValFilter);
        while (envProperties.hasMoreElements())
        {
            Property envProp = (Property) envProperties.nextElement();
            envVars[nextVar][0] = envProp.getName();
            try
            {
                envVars[nextVar][1] = getPropertyValue(envVars[nextVar][0]); //envProp.getValue();
//System.out.println("env: " + envVars[nextVar][0] + " = " + envVars[nextVar][1]);
            }
            catch (Exception e)
            {
                if (nullIfCantExpand)
                {
                    envVars[nextVar][1] = null;
                }
                else
                {
                    envVars[nextVar][1] = envProp.getValue();
                }
            }
            nextVar++;
        }
        m_propMgr.setGetPropertyFilter(null);
        return envVars;
    }


    public boolean EnvPairsCountValid(String propGroupFullSpec)
    {
        boolean validateStatus = true;

        try
        {
            String envSubKeyStr = getEnvSubKeyStr(propGroupFullSpec);
            if (envSubKeyStr != null)
            {
                PropertyCollection envProperties = getCustomizedEnvironment(envSubKeyStr);
//        String envGroupPath = ENVIRONMENT + GROUP_SEPARATOR + envSubKeyStr;
//        PropertyCollection envProperties = getProperties(envGroupPath);
                if (envProperties != null)
                {
                    int envPairsCount = (int) (envProperties.size());
                    envProperties = null;
                    return (envPairsCount <= ENV_VARS_MAX);
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return validateStatus;
    }


    public String getUBWorkDir()
    {
        return (setGroupGetProperty(BROKER_GROUP, WORKDIR));
    }

    public synchronized String getExpandedUBWorkDir()
    {
        String saveCurrentGroupName = m_currentGroupName;
        m_currentGroupName = BROKER_GROUP;

        String propVal = null;
        try
        {
            setGetPropertyFilter();
            propVal = getPropertyValue(WORKDIR);
//System.out.println("workDir = " + propVal);
            m_propMgr.setGetPropertyFilter(null);
        }
        catch (Exception e)
        {
        }
        m_currentGroupName = saveCurrentGroupName;
        return (propVal);

    }

    public synchronized String getExpandedPropertyValue(String propName,
            String fullPropGroupPath)
    {
        return getExpandedPropertyValue(propName, fullPropGroupPath, true);
    }

    public synchronized String getExpandedPropertyValue(String propName,
            String fullPropGroupPath,
            boolean doFilter)
    {
        String saveCurrentGroupName = m_currentGroupName;
        m_currentGroupName = fullPropGroupPath;

        String propVal = null;
        try
        {
            setGetPropertyFilter();
            propVal = getPropertyValue(propName, doFilter);
//System.out.println(fullPropGroupPath + "." + propName + " = " + propVal);
            m_propMgr.setGetPropertyFilter(null);
        }
        catch (Exception e)
        {
        }
        m_currentGroupName = saveCurrentGroupName;
        return (propVal);
    }


    public String getNSLocation(String nsName)
    {
        return (setGroupGetProperty(nsName, LOCATION));
    }

    public String[] getNameServerInstances()
            throws CantFindGroup
    {
        String[] svcList = null;

        try
        {
            svcList = getSvcGrp(NAME_SRVR_GROUP,
                                false, // no recursion,
                                false, // do includeParentName,
                                false // don't includeParentGroup
                      );
//for (int i = 0; i < svcList.length; i++)
//  System.out.println("svcList[" + i + "] = " + svcList[i]);
        }
        catch (Exception e)
        {
            throw new CantFindGroup(NAME_SRVR_GROUP, null);
        }
        return svcList;
    }

    public String[] getAdminRoles()
            throws CantFindGroup
    {
        String[] svcList = null;

        try
        {
            svcList = getSvcGrp(ADMIN_ROLE_GROUP,
                                false, // no recursion,
                                false, // do includeParentName,
                                false // don't includeParentGroup
                      );
//for (int i = 0; i < svcList.length; i++)
//  System.out.println("svcList[" + i + "] = " + svcList[i]);
        }
        catch (Exception e)
        {
            throw new CantFindGroup(ADMIN_ROLE_GROUP, null);
        }
        return svcList;
    }

    //
    // For a given new instance name, we need to update the instance list cache we held
    // locally in this class.
    //
    public void updateInstanceList(String svcGrpPath)
    {
        int svcType = getSvcTypeFromSvcGrpPath(svcGrpPath);
        try
        {
//System.out.println("updating " + svcGrpPath + " to service list");
            switch (svcType)
            {
                case ((int) WS_TYPE):
                    m_ubWSservices = getWSSvcGrp();
                    break;
                case ((int) AS_TYPE):
                    m_ubASservices = getASSvcGrp();
                    break;
                case ((int) NS_TYPE):
                    m_nsServices = getNSSvcGrp();
                    break;
                case ((int) OR_TYPE):
                    m_ubORservices = getORSvcGrp();
                    break;
                case ((int) OD_TYPE):
                    m_ubODservices = getODSvcGrp();
                    break;
                case ((int) MSS_TYPE):
                    m_ubMSSservices = getMSSSvcGrp();
                    break;
                case ((int) AD_TYPE):
                    m_adapterServices = getAdapterSvcGrp();
                    break;
                case ((int) AIA_INT_TYPE):
                    m_aiaServices = getAiaSvcGrp();
                    break;
                case ((int) WSA_TYPE):
                    m_wsaServices = getWsaSvcGrp();
                    break;
                case ((int) REST_TYPE):
                    m_restServices = getRestSvcGrp();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        { //
            // do nothing at the moment.
        }
    }

    public String getAutoStartValue(String propInstanceName)
    {
        return (setGroupGetProperty(propInstanceName, AUTO_START));
    }

    public boolean isNameServerPersonality(String personStr)
    {
        return (personStr.equals(NAME_SRVR_GROUP));
    }


    public UBPreferenceProperties getPreferences()
    {
        return (m_preferences);
    }


    public int getPreferenceIntProperty(String propName)
    {
        return (m_propMgr.getIntProperty(PREFERENCE_CHILD_GROUP_SPEC + GROUP_SEPARATOR
                                         + propName));
    }


    public void putPreferences(UBPreferenceProperties newPreferences)
    {
        String prefGroupPrefix = PREFERENCE_CHILD_GROUP_SPEC + GROUP_SEPARATOR;
        //
        // put the preference properties as specified to the property Manager.
        //
        try
        {
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_CONNECT_SVC_RETRY,
                                     newPreferences.m_toolConnectSvcRetry);
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_CONNECT_SVC_RETRY_INTERVAL,
                                     newPreferences.m_toolConnectSvcRetryInterval);
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_PING_SVC_RETRY,
                                     newPreferences.m_toolPingSvcRetry);
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_SHUTDOWN_SVC_CONFIRM_RETRY,
                                     newPreferences.m_toolShutdownSvcConfirmRetry);
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL,
                                     newPreferences.m_toolShutdownSvcConfirmRetryInterval);
            m_propMgr.putIntProperty(prefGroupPrefix + TOOL_GET_SVC_STATUS_RETRY,
                                     newPreferences.m_toolGetSvcStatusRetry);
            m_propMgr.putIntProperty(prefGroupPrefix + ADMSRVR_REGISTER_RETRY,
                                     newPreferences.m_admSrvrRegisteredRetry);
            m_propMgr.putIntProperty(prefGroupPrefix + ADMSRVR_REGISTER_RETRY_INTERVAL,
                                     newPreferences.m_admSrvrRegisteredRetryInterval);
            m_preferences = newPreferences;
        }
        catch (Exception e)
        {
        }
    }


    public synchronized void reloadProperties(String fullPropFileSpec)
            throws EnumGroupError
    {
        try
        {
//System.out.println("Start update: " + fullPropFileSpec + ", " + (new Date()).toString());
            m_propMgr.update(fullPropFileSpec);
//System.out.println("Done update: " + fullPropFileSpec + ", " + (new Date()).toString());

            try
            {
                m_parentGroups = getProperties(PARENT_GROUPS);
            }
            catch (Exception e)
            {
                // don't do anything right now.
            }
            m_svcList = m_propMgr.groups();
            m_ubWSservices = getWSSvcGrp();
            m_ubASservices = getASSvcGrp();
            m_nsServices = getNSSvcGrp();
            m_msngrServices = getMsngrSvcGrp();
            m_ubORservices = getORSvcGrp();
            m_ubODservices = getODSvcGrp();
            m_adapterServices = getAdapterSvcGrp();
            m_ubMSSservices = getMSSSvcGrp();
            m_wsaServices = getWsaSvcGrp();
            m_restServices = getRestSvcGrp();
            refreshPreferences();
            m_loadStatus = true;
        }
        catch (Exception e)
        {
            throw new EnumGroupError(e.toString());
        }
    }

    private String setGroupGetProperty(String groupName, String propertyName)
    {
        String saveCurrentGroupName = m_currentGroupName;
        m_currentGroupName = groupName;

        String propVal = null;
        try
        {
            propVal = getPropertyValue(propertyName);
        }
        catch (Exception e)
        {
        }
        m_currentGroupName = saveCurrentGroupName;
        return (propVal);
    }

    public void fetchPreferences()
    {
        if (m_preferences == null)
        {
            m_preferences = new UBPreferenceProperties(this);
        }
        else
        {
            m_preferences.refetchAll(this);
        }
    }


    public void refreshPreferences()
    {
        fetchPreferences();
    }

    public boolean removeProperty(String fullPropPathSpec)
    {
        boolean removeStatus = false;
        try
        {
            m_propMgr.removeProperty(fullPropPathSpec);
            removeStatus = true;
        }
        catch (Exception e)
        {
        }
        return (removeStatus);
    }

    public boolean uniqueSvcName(String svcNameToVerify)
    {
        if (findUBPersonStrForSvcName(svcNameToVerify) == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Given a property value of type
     * {@Workpath}/logfileName,
     * this method prepends to the log file name, the given
     * instance name as follows:
     * {@Workpath}/<instanceName>.logfileName
     *
     * @param groupName
     * @param instanceName
     * @param propertyName
     * @return    true if the log file name was properly adjusted;
     *            false otherwise.
     */
    private boolean updateLogFileName(String groupName, String instanceName, String propertyName)
    {
        boolean propAdded = false;
        try
        {
            // Get the default value
            String logFileName = m_propMgr.getProperty(groupName + "." + propertyName);
            String newLogFileName = adjustLogFilename(logFileName, instanceName);
            m_propMgr.putProperty(groupName + "." + instanceName + "." + propertyName, newLogFileName);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            // if we got an exception, then the property wasn't
            // added so there's no need to remove it!
            propAdded = false;
        }
        return propAdded;
    }

    private String adjustLogFilename(String logFileName, String instanceName)
    {
        int lastfileSeptr = 0;
        String newFileName = null;
        String fileSep = System.getProperty("file.separator");

        if (logFileName.indexOf(instanceName) < 0)
        {
            lastfileSeptr = logFileName.lastIndexOf(fileSep);
            newFileName = logFileName.substring(0, lastfileSeptr + 1) +
                          instanceName + "." +
                          logFileName.substring(lastfileSeptr + 1);
        }
        return newFileName;
    }


    //
    //  handleAddNew()
    //
    //  For 9.1A and greater releases, under the framework of the MMC-based Progress Explorer,
    //  adding a new instance takes place in the following steps:
    //  1. We prompt the user for the name of the new instance.  The new instance name
    //     is sent to the plugin side.
    //  2. The plugin remote object sends the new instance information to PropMgrPlugin, hence
    //     PropMgrUtil.handleAddNew(), to add a new property group in the property file.
    //  3. After the new property group is added, for each type of the new instance,
    //     ubroker or nameServer instances, a set of property values must be fixed up:
    //
    //     - ubroker(WS, AS, OR, OD, MS) instance
    //       uuid, srvrLogFile, brokerLogFile, appserviceNameList, controllingNameServer.
    //
    //     - nameServer instance
    //       srvrLogFile
    //
    //     - AIA instance
    //       logFile
    //
    public boolean handleAddNew(String groupName, String instanceName)
    {
        boolean propAdded = false;
        int newInstType = getSvcTypeFromSvcGrpPath(groupName);

        if (newInstType == NS_TYPE)
        {
            //
            // We should never need to get to this code, but just in case, location is set
            // to local by default.
            //
            return handleNSAddNew(groupName, instanceName, LOCATION_LOCAL);
        }
        if (newInstType == AIA_INT_TYPE)
        {
            return handleAiaAddNew(groupName, instanceName);
        }

        // Fix MQ property file names for properties:
        //  - mqBrokerLogFile
        //  - mqServerLogFile
        if ((newInstType == AS_TYPE) || (newInstType == WS_TYPE))
        {
            updateLogFileName(groupName, instanceName, "mqBrokerLogFile");
            updateLogFileName(groupName, instanceName, "mqServerLogFile");
        }

        //
        // Fix up common ubroker properties:
        //  - srvrLogFile
        //  - brokerLogFile
        //
        updateLogFileName(groupName, instanceName, SRVRLOG);
        updateLogFileName(groupName, instanceName, BRKRLOG);

        //System.out.println("in handleAddNew(" +groupName + "." + instanceName + ")");
        String fullPropSpec = getFullPropPath(instanceName, groupName);
        //System.out.println("fullPropSpec = " + fullPropSpec);
        boolean putStatus;
        try
        {
            //
            // ubroker specific property fixup
            //
            String newUUID = (new UUID()).toString();
            String[] nslist = getNameServerInstances();
            String cntlNS = null;

            if ((nslist != null) && (nslist.length > 0))
            {
                cntlNS = nslist[0];
            }

            putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + UUID,
                                              newUUID);
            if (cntlNS != null)
            {
                putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + CNTL_NAME_SRVR,
                                                  cntlNS);
            }

            if (newInstType == AD_TYPE)
            {
                // bug #20051201-016
                PropertyGroup group = m_propMgr.findGroup(ADAPTER_GROUP);
                Property property = group.findRegisteredProperty(SERVICE_NAMES.toLowerCase());
                String appSvcNameListValue = property.getDefaultValue();

                putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + SERVICE_NAMES,
                                                  appSvcNameListValue);
            }
            else
            {
                putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + SERVICE_NAMES,
                                                  instanceName);
            }
            saveAll(m_propFilePath, fullPropSpec + " is added");
//System.out.println("new instance " + fullPropSpec  + " is added");
            return true;

        }
        catch (Exception e)
        {
            //
            //  Something went wrong, and we need to scratch the new group completely, as if
            //  the new group is never added.
            //
            e.printStackTrace();
            if (propAdded)
            {
                try
                {
                    removeGroup(fullPropSpec);
                }
                catch (Exception exp)
                {}
            }
        }
        return false;
    }

    public boolean handleAiaAddNew(String propGroupSpec, String newSvcName)
    {

        boolean propAdded = false;
        String fullPropSpec = getFullPropPath(newSvcName, propGroupSpec);

        boolean putStatus;
        //
        // Fix up the properties: logFile first.
        try
        {
            String logFilename = m_propMgr.getProperty(propGroupSpec + GROUP_SEPARATOR + LOGFILE);
            logFilename = adjustLogFilename(logFilename, newSvcName);

            putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOGFILE, logFilename);
            //System.out.println(" srvrLogFilename put status = " + putStatus);
            propAdded = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            try
            {
                removeGroup(fullPropSpec);
            }
            catch (Exception exp)
            {}
            ;
            return false;
        }

        try
        {
            //
            // ubroker specific property fixup
            //
            String[] nslist = getNameServerInstances();
            String cntlNS = null;

            if ((nslist != null) && (nslist.length > 0))
            {
                cntlNS = nslist[0];
            }

            if (cntlNS != null)
            {
                putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + CNTL_NAME_SRVR,
                                                  cntlNS);
            }
            propAdded = true;

            saveAll(m_propFilePath, fullPropSpec + " is added");

            return true;

        }
        catch (Exception e)
        {
            //
            //  Something went wrong, and we need to scratch the new group completely, as if
            //  the new group is never added.
            //
            e.printStackTrace();
            if (propAdded)
            {
                try
                {
                    removeGroup(fullPropSpec);
                }
                catch (Exception exp)
                {}
            }
        }
        return false;

    }

    public boolean handleRestAddNew(String propGroupSpec, String newSvcName,
            String locationChoice, String url)
    {
        boolean propAdded = false;
        String fullPropSpec = getFullPropPath(newSvcName, propGroupSpec);
        boolean putStatus;
        //
        // Fix up the properties: logFile, etc. first.
        //
        try
        {
            String logFilename = m_propMgr.getProperty(propGroupSpec + GROUP_SEPARATOR + LOGFILE);
            logFilename = adjustLogFilename(logFilename, newSvcName);
            putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOGFILE, logFilename);

            propAdded = true;
            if (getSvcTypeFromSvcGrpPath(propGroupSpec) == REST_TYPE)
            {
                try
                {
                    if (locationChoice.equals(LOCATION_REMOTE))
                    {
                        putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOCATION, locationChoice);
                    }
                    putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + RESTURL, url);

                }
                catch (Exception e)
                {
                }
                saveAll(m_propFilePath, fullPropSpec + " is added");
                return true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            if (propAdded)
            {
                try
                {
                    removeGroup(fullPropSpec);
                }
                catch (Exception exp)
                {
                }
            }
        }

        return false;
        }

    public boolean handleWsaAddNew(String propGroupSpec, String newSvcName,
                                   String locationChoice, String url)
    {
        boolean propAdded = false;
        String fullPropSpec = getFullPropPath(newSvcName, propGroupSpec);
        boolean putStatus;
        //
        // Fix up the properties: logFile, etc. first.
        //
        try
        {
            String logFilename = m_propMgr.getProperty(propGroupSpec + GROUP_SEPARATOR + LOGFILE);
            logFilename = adjustLogFilename(logFilename, newSvcName);
            putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOGFILE, logFilename);

            propAdded = true;
            if (getSvcTypeFromSvcGrpPath(propGroupSpec) == WSA_TYPE)
            {
                try
                {
                    if (locationChoice.equals(LOCATION_REMOTE))
                    {
                        putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOCATION, locationChoice);
                    }
                    putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + WSAURL, url);

                }
                catch (Exception e)
                {
                }
                saveAll(m_propFilePath, fullPropSpec + " is added");
                return true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            if (propAdded)
            {
                try
                {
                    removeGroup(fullPropSpec);
                }
                catch (Exception exp)
                {
                }
            }
        }

        return false;
    }

    public boolean handleNSAddNew(String propGroupSpec, String newSvcName,
                                  String locationChoice)
    {
        boolean propAdded = false;
//System.out.println("in handleNSAddNew(" +propGroupSpec + "." + newSvcName + "," + locationChoice + ")");
        String fullPropSpec = getFullPropPath(newSvcName, propGroupSpec);
//System.out.println("fullPropSpec = " + fullPropSpec);
        boolean putStatus;
        //
        // Fix up the properties: portNumber and srvrLogFile, first. These are common to
        // both ubroker and nameServer instances.
        //
        try
        {
            String srvrLogFilename = m_propMgr.getProperty(propGroupSpec + GROUP_SEPARATOR + SRVRLOG);
            srvrLogFilename = adjustLogFilename(srvrLogFilename, newSvcName);
            putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + SRVRLOG, srvrLogFilename);

            propAdded = true;
            if (getSvcTypeFromSvcGrpPath(propGroupSpec) == NS_TYPE)
            {
                if (locationChoice.equals(LOCATION_REMOTE))
                {
                    try
                    {
//System.out.println("setting location property to " + locationChoice);
                        putStatus = m_propMgr.putProperty(fullPropSpec + GROUP_SEPARATOR + LOCATION,
                                locationChoice);
                    }
                    catch (Exception e)
                    {
                    }
                }
                saveAll(m_propFilePath, fullPropSpec + " is added");
                return true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            if (propAdded)
            {
                try
                {
                    removeGroup(fullPropSpec);
                }
                catch (Exception exp)
                {}
            }
        }

        return false;
    }


    public int getNSInstRefCnt(String nsInstName)
    {
        int refCount = 0;

        if (m_ubWSservices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_ubWSservices, CNTL_NAME_SRVR);
        }

        if (m_ubASservices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_ubASservices, CNTL_NAME_SRVR);
        }

        if (m_ubORservices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_ubORservices, CNTL_NAME_SRVR);
        }

        if (m_ubODservices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_ubODservices, CNTL_NAME_SRVR);
        }
        if (m_msngrServices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_msngrServices, CNTL_NAME_SRVR);
        }

        if (m_nsServices.length > 0)
        {
            refCount += getNSRefCnt(nsInstName, m_nsServices, NEIGHBOR_NS);
        }

        return refCount;

    }

    public Hashtable getLogFNList(String propGrpFullSpec)
    {
        int instType = getSvcTypeFromSvcGrpPath(propGrpFullSpec);
        Hashtable fnlist = new Hashtable();
        String srvrLogFNValue = null;
        String brkrLogFNValue = null;
        String logFNValue = null;

        switch (instType)
        {
            case (WS_TYPE):
            case (AS_TYPE):
            case (AD_TYPE):
                srvrLogFNValue = getExpandedPropertyValue(SRVRLOG, propGrpFullSpec);
                brkrLogFNValue = getExpandedPropertyValue(BRKRLOG, propGrpFullSpec);
                fnlist.put(ProgressResources.retrieveTranString("com.progress.international.messages.MMCMsgBundle",
                        "SRVR_LOG_DISP_NAME"), srvrLogFNValue);
                fnlist.put(ProgressResources.retrieveTranString("com.progress.international.messages.MMCMsgBundle",
                        "BRKR_LOG_DISP_NAME"), brkrLogFNValue);
                break;
            case (NS_TYPE):
                srvrLogFNValue = getExpandedPropertyValue(SRVRLOG, propGrpFullSpec);
                fnlist.put(ProgressResources.retrieveTranString("com.progress.international.messages.MMCMsgBundle",
                        "SRVR_LOG_DISP_NAME"), srvrLogFNValue);
                break;
            case (OR_TYPE):
            case (OD_TYPE):
            case (MSS_TYPE):
                brkrLogFNValue = getExpandedPropertyValue(BRKRLOG, propGrpFullSpec);
                fnlist.put(ProgressResources.retrieveTranString("com.progress.international.messages.MMCMsgBundle",
                        "BRKR_LOG_DISP_NAME"), brkrLogFNValue);
                break;
            case (MS_TYPE):
                break;
            case (AIA_INT_TYPE):
            case (WSA_TYPE):
            case (REST_TYPE):
                logFNValue = getExpandedPropertyValue(LOGFILE, propGrpFullSpec);
                fnlist.put(ProgressResources.retrieveTranString("com.progress.international.messages.MMCMsgBundle",
                        "LOG_DISP_NAME"), logFNValue);
                break;

            default:
                break;
        }

        return fnlist;
    }

    //
    //  private methods
    //
    private int getNSRefCnt(String nsInstName, String[] svcList, String lookupPropName)
    {
        int next = 0;
        int refCount = 0;

        for (next = 0; next < svcList.length; next++)
        {
            try
            {
                String propStr = m_propMgr.getProperty(svcList[next] + GROUP_SEPARATOR + lookupPropName);
                if (lookupPropName.equals(CNTL_NAME_SRVR))
                {
                    if (nsInstName.equals(propStr))
                    {
                        refCount++;
                    }
                }
                else
                {
                    if (nsInstName.indexOf(propStr) > 0)
                    {
                        refCount++;
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
        return refCount;
    }


    private synchronized int getNextFreePortNumber()
    {
        m_nextPortNumber += 100;
        return (m_nextPortNumber);
    }


    //
    // private methods
    //
    private void setGetPropertyFilter()
    {
        if (m_propValFilter == null)
        {
            PropMgrUtils.setPropertyFilter();
        }
        m_propMgr.setGetPropertyFilter(m_propValFilter);
    }


    //---------------------------------------------------------------------------
    // OBSOLETE : the schema file contains all these info
    //
    // Inner class to handle the property registration for various personalities
    // of the unified broker.
    //
    //---------------------------------------------------------------------------
    //
    class UBRegProperties
    {
        boolean m_regParentGroups = false;
        boolean m_regUBCommonProp = false;
        boolean m_regUBWSProp = false;
        boolean m_regUBASProp = false;
        boolean m_regNSProp = false;
        boolean m_regEnvGroup = false;
        boolean m_regWebSpeedGroup = false;
        boolean m_regORDSProp = false;
        boolean m_regODDSProp = false;
        boolean m_regMSSDSProp = false;
        boolean m_regPreferences = false;
        boolean m_regAdapterGroup = false;
        boolean m_regAiaGroup = false;


        public UBRegProperties()
        {
            try
            {
                regParentGroups();
                regUBCommonProperties();
                regUBWSProperties();
                regUBASProperties();
                regNSProperties();
                regEnvPropGroup();
                regWebSpeedGroup();
                regORDSProperties();
                regODDSProperties();
                regMSSDSProperties();
                regAdapterProperties();
                regAiaProperties();
                regPreferences();
            }
            catch (Exception e)
            {
                UBToolsMsg.logException("Failed to register properties: " + e.toString());
            }
        }

        private void regParentGroups()
                throws PropertyValueException
        {
            Property[] propList =
                    {
                    new Property(WS_PERSONALITY, ubGroupPath[WS_TYPE]),
                    new Property(AS_PERSONALITY, ubGroupPath[AS_TYPE]),
                    new Property(OR_PERSONALITY, ubGroupPath[OR_TYPE]),
                    new Property(OD_PERSONALITY, ubGroupPath[OD_TYPE]),
                    new Property(MSS_PERSONALITY, ubGroupPath[MSS_TYPE]),
                    new Property(NS_PERSONALITY, NAME_SRVR_GROUP),
                    new Property(MSNGR_PERSONALITY, WS_MSNGR_GROUP_PATH),
                    new Property(ADAPTER_PERSONALITY, ADAPTER_GROUP),
                    new Property(AIA_PERSONALITY, AIA_GROUP)

            };
            PropMgrUtils.m_propMgr.registerGroup(
                    PARENT_GROUPS, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );
            m_regParentGroups = true;
        }


        private void regUBCommonProperties()
                throws PropertyValueException
        {
            String[] opModeList =
                                  {
                                  OP_MODE_STATELESS, OP_MODE_STATE_RESET, OP_MODE_STATE_AWARE};
            String[] onOffList =
                                 {
                                 PROP_ON_VAL, PROP_OFF_VAL};
            String jvmArgsDefault = ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
                                    JVMARGS_DEF_UNIX : JVMARGS_DEF_NT;
            Property[] propList =
                    {
                    (Property) (new EnumProperty(OPERATING_MODE, OP_MODE_STATELESS, opModeList, COMMON_TYPE)),
                    (Property) (new IntProperty(PORT, PORT_DEF, COMMON_TYPE)),
                    new Property(USERNAME, "", COMMON_TYPE),
                    new Property(PASSWORD, "", COMMON_TYPE),
                    new Property(GROUPNAME, "", COMMON_TYPE),
                    new Property(WORKDIR, " ", COMMON_TYPE),
                    new Property(SRVRLOG, " ", COMMON_TYPE),
                    new Property(BRKRLOG, " ", COMMON_TYPE),
                    (Property) (new IntProperty(BRKR_LOGLEVEL, BRKR_LOGLEVEL_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new EnumProperty(BRKR_LOGAPPEND, BRKR_LOGAPPEND_DEF_VAL, onOffList, COMMON_TYPE)),
                    (Property) (new EnumProperty(FOURGL_SRC_COMPILE, FOURGL_SRC_COMPILE_DEF_VAL, onOffList, COMMON_TYPE)),
                    (Property) (new IntProperty(SRVR_LOGLEVEL, SRVR_LOGLEVEL_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new EnumProperty(SRVR_LOGAPPEND, SRVR_LOGAPPEND_DEF_VAL, onOffList, COMMON_TYPE)),
                    (Property) (new IntProperty(SRVR_STARTUP_TIMEOUT, SRVR_STARTUP_TIMEOUT_DEF_VAL.toString(),
                                                COMMON_TYPE)),
                    (Property) (new IntProperty(REQUEST_TIMEOUT, REQUEST_TIMEOUT_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new EnumProperty(AUTO_START, PROP_OFF_VAL, onOffList, COMMON_TYPE)),
                    (Property) (new EnumProperty(DEFAULT_SERVICE, PROP_OFF_VAL, onOffList, COMMON_TYPE)),
                    (Property) (new Property(STARTUP_PARAM, WS_STARTUP_PARAM_DEF, COMMON_TYPE)),
                    (Property) (new IntProperty(INI_SRVR_INST, INI_SRVR_INST_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new IntProperty(MIN_SRVR_INST, MIN_SRVR_INST_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new IntProperty(MAX_SRVR_INST, MAX_SRVR_INST_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new IntProperty(SRVR_MIN_PORT, MIN_PORTNUM_DEF, COMMON_TYPE)),
                    (Property) (new IntProperty(SRVR_MAX_PORT, MAX_PORTNUM_DEF, COMMON_TYPE)),
                    new Property(CNTL_NAME_SRVR, " ", COMMON_TYPE),
                    (Property) (new IntProperty(PRIORITY_WEIGHT, PRIO_WEIGHT_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new IntProperty(REGIS_RETRY, REG_RETRY_DEF_VAL.toString(), COMMON_TYPE)),
                    new Property(SRVR_EXECFILE, " ", COMMON_TYPE),
                    new Property(PROPATH, " ", COMMON_TYPE),
                    new Property(ENVIRONMENT, " ", COMMON_TYPE),
                    (Property) (new IntProperty(INFO_VERSION, INFO_VERSION_DEF, COMMON_TYPE)),
                    new Property(SERVICE_NAMES, " ", COMMON_TYPE),
                    new Property(UUID, " ", COMMON_TYPE),
                    new Property(DESCRIPTION, " ", COMMON_TYPE),
                    new Property(JVMARGS, jvmArgsDefault, COMMON_TYPE),
                    (Property) (new IntProperty(MAX_CLIENT_INST, MAX_CLIENT_INST_DEF_VAL.toString(), COMMON_TYPE)),
                    (Property) (new IntProperty(AUTO_TRIM_TIMEOUT, AUTO_TRIM_TIMEOUT_DEF_VAL.toString(), COMMON_TYPE)),
                    new Property(REG_MODE, REG_IP, COMMON_TYPE),
                    new Property(HOST, " ", COMMON_TYPE)
            };
            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[COMMON_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );
            m_regUBCommonProp = true;
        }

        private void regUBWSProperties()
                throws PropertyValueException
        {
            //
            // 2BEDONE - fetch these enum values from resource bundle, instead
            //
            String[] opModeList =
                                  {
                                  OP_MODE_STATELESS};
            String[] appModeList =
                                   {
                                   APP_MODE_DEVO, APP_MODE_PROD};
            String[] dbgModeList =
                                   {
                                   DEBUG_MODE_DEFAULT, DEBUG_MODE_ENABLE, DEBUG_MODE_DISABLE};
            String[] onOffList =
                                 {
                                 PROP_ON_VAL, PROP_OFF_VAL};

            Property[] propList =
                    {
                    (Property) (new EnumProperty(OPERATING_MODE, OP_MODE_STATELESS, opModeList, COMMON_TYPE)),
                    new Property(SRVR_EXECFILE, WS_SRVR_EXEC_FN, WS_TYPE), // 2BEDONE - handle os platform difference
                    (Property) (new EnumProperty(APP_MODE, APP_MODE_DEVO, appModeList, WS_TYPE)),
                    (Property) (new EnumProperty(DEBUG_MODE, DEBUG_MODE_DEFAULT, dbgModeList, WS_TYPE)),
                    (Property) (new EnumProperty(DEFAULT_SERVICE, PROP_OFF_VAL, onOffList, WS_TYPE)),
                    new Property(APP_URL, " ", WS_TYPE),
                    new Property(UB_CLASSMAIN, WS_UB_CLASSMAIN_DEF, WS_TYPE),
                    new Property(DEFCOOKIE_PATH, " ", WS_TYPE),
                    new Property(DEFCOOKIE_DOMAIN, " ", WS_TYPE),
                    new Property(WSROOT, WSROOT_DEF, WS_TYPE),
                    new Property(FILE_UPLOAD_DIR, FILE_UPLOAD_DIR_DEF, WS_TYPE),
                    (Property) (new IntProperty(BIN_UPLOAD_MAXSIZE, BIN_UPLOAD_MAXSIZE_DEF.toString(), WS_TYPE))
            };
            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[WS_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );

            m_regUBWSProp = true;
        }

        private void regUBASProperties()
                throws PropertyValueException
        {
            String[] onOffList =
                                 {
                                 PROP_ON_VAL, PROP_OFF_VAL};

            Property[] propList =
                    {
                    new Property(STARTUP_PROC, STARTUP_PROC_DEF, AS_TYPE),
                    new Property(STARTUP_PROC_PARAM, STARTUP_PROC_PARAM_DEF, AS_TYPE),
                    new Property(CONNECT_PROC, CONNECT_PROC_DEF, AS_TYPE),
                    new Property(SHUTDOWN_PROC, SHUTDOWN_PROC_DEF, AS_TYPE),
                    new Property(DISCONN_PROC, DISCONN_PROC_DEF, AS_TYPE),
                    new Property(ACTIVATE_PROC, ACTIVATE_PROC_DEF, AS_TYPE),
                    new Property(DEACTIVATE_PROC, DEACTIVATE_PROC_DEF, AS_TYPE),
                    new Property(SRVR_EXECFILE, APP_SRVR_EXEC_FN, AS_TYPE), // 2BEDONE - handle os platform difference
                    (Property) (new EnumProperty(DEFAULT_SERVICE, PROP_ON_VAL, onOffList, AS_TYPE)),
                    new Property(UB_CLASSMAIN, AS_UB_CLASSMAIN_DEF, AS_TYPE),
                    (Property) (new EnumProperty(DEBUGGER_ENABLED, PROP_OFF_VAL, onOffList, AS_TYPE))
            };

            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[AS_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );

            m_regUBASProp = true;
        }

        private void regNSProperties()
                throws PropertyValueException
        {
            String[] locationList =
                                    {
                                    LOCATION_LOCAL, LOCATION_REMOTE};
            String[] onOffList =
                                 {
                                 PROP_OFF_VAL, PROP_ON_VAL};
            String jvmArgsDefault = ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
                                    JVMARGS_DEF_UNIX : JVMARGS_DEF_NT;

            Property[] propList =
                    {
                    new Property(INFO_VERSION, INFO_VERSION_DEF, NS_TYPE),
                    new Property(HOST, HOST_DEF, NS_TYPE),
                    new Property(WORKDIR, " ", NS_TYPE),
                    new Property(UB_CLASSMAIN, NS_CLASSMAIN_DEF, NS_TYPE),
                    new Property(PORT, "", NS_TYPE),
                    new Property(BRKR_KEEP_ALIVE_TIMEOUT, BRKR_KEEP_ALIVE_TIMEOUT_VAL.toString(), NS_TYPE),
                    new Property(ENVIRONMENT, " ", NS_TYPE),
                    new Property(SRVRLOG, " ", NS_TYPE),
                    new Property(NEIGHBOR_NS, NEIGHBOR_NS_DEF, NS_TYPE),
                    (Property) (new EnumProperty(LOCATION, LOCATION_LOCAL, locationList, NS_TYPE)),
                    (Property) (new EnumProperty(AUTO_START, PROP_OFF_VAL, onOffList, NS_TYPE)),
                    (Property) (new EnumProperty(LOGAPPEND, LOGAPPEND_DEF, onOffList, NS_TYPE)),
                    (Property) (new IntProperty(LOGLEVEL, LOGLEVEL_DEF.toString(), NS_TYPE)),
                    new Property(JVMARGS, jvmArgsDefault, NS_TYPE)
            };
            PropMgrUtils.m_propMgr.registerGroup(
                    NAME_SRVR_GROUP, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    propList
                    );
            m_regNSProp = true;
        }

        private void regEnvPropGroup()
                throws PropertyValueException
        {
            //
            // register this group with the switch, saveEvenIfEmpty, so
            // that when we update the property file, the property manager knows
            // enough to output parent property groups that do not have any properties
            // define.
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    ENV_GROUP_PARENT, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    null, // no properties to register
                    true // saveEvenIfEmpty
                    );
            m_regEnvGroup = true;
        }

        private void regWebSpeedGroup()
                throws PropertyValueException
        {
            String[] onOffList =
                                 {
                                 PROP_OFF_VAL, PROP_ON_VAL};
            Property[] wsPropList =
                    {
                    (Property) (new Property(INFO_VERSION, INFO_VERSION_DEF, MS_TYPE)),
                    (Property) (new Property(MSNGR_COMPONENTS_FLAG, "", MS_TYPE)),
                    (Property) (new Property(MSNGR_SCRIPT_PATH, "", MS_TYPE)),
                    (Property) (new Property(ROOT_PATH, "", MS_TYPE)),
            };

            Property[] msPropList =
                    {
                    (Property) (new Property(CNTL_NAME_SRVR, "")),
                    (Property) (new EnumProperty(ALLOW_WSMADMIN, PROP_OFF_VAL, onOffList)),
                    (Property) (new Property(MSNGR_EXEC_FILE, "")),
                    (Property) (new Property(CGIIP_SCRIPT_FILE, "")),
                    (Property) (new Property(WORKDIR, "")),
                    (Property) (new EnumProperty(MSNGR_USE_CONNID, PROP_OFF_VAL, onOffList)),
                    (Property) (new IntProperty(MIN_NSCLIENTPORT, NSCLIENTPORT_DEF.toString())),
                    (Property) (new IntProperty(MAX_NSCLIENTPORT, NSCLIENTPORT_DEF.toString())),
                    (Property) (new IntProperty(NSCLIENTPORT_RETRY_INTERVAL, NSCLIENTPORT_DEF.toString())),
                    (Property) (new IntProperty(NSCLIENTPORT_RETRY, NSCLIENTPORT_DEF.toString()))
            };

            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    WS_GROUP_PARENT, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    wsPropList, // property to register
                    true // saveEvenIfEmpty
                    );

            PropMgrUtils.m_propMgr.registerGroup(
                    WS_GROUP_PARENT + GROUP_SEPARATOR + MSNGR_PERSONALITY, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    msPropList, // property to register
                    true // saveEvenIfEmpty
                    );

            m_regWebSpeedGroup = true;

        }

        private void regORDSProperties()
        {
            Property[] propList =
                    {
                    (Property) (new Property(UB_CLASSMAIN, OR_UB_CLASSMAIN_DEF, OR_TYPE)),
            };
            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[OR_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    propList, // property to register
                    true // saveEvenIfEmpty
                    );
            m_regORDSProp = true;
        }

        private void regODDSProperties()
        {
            Property[] propList =
                    {
                    new Property(UB_CLASSMAIN, OD_UB_CLASSMAIN_DEF, OD_TYPE),
            };
            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[OD_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    propList, // property to register
                    true // saveEvenIfEmpty
                    );

            m_regODDSProp = true;
        }

        private void regMSSDSProperties()
        {
            Property[] propList =
                    {
                    new Property(UB_CLASSMAIN, MSS_UB_CLASSMAIN_DEF, MSS_TYPE),
            };
            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    ubGroupPath[MSS_TYPE], // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    false, // restrictProperties
                    propList, // property to register
                    true // saveEvenIfEmpty
                    );

            m_regMSSDSProp = true;
        }

        private void regAdapterProperties()
                throws PropertyValueException
        {
            String[] onOffList =
                                 {
                                 PROP_ON_VAL, PROP_OFF_VAL};
            String jvmArgsDefault = ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
                                    JVMARGS_DEF_UNIX : JVMARGS_DEF_NT;
            Property[] propList =
                    {
                    (Property) (new IntProperty(PORT, ADAPTER_PORT_DEFAULT, AD_TYPE)),
                    new Property(USERNAME, "", AD_TYPE),
                    new Property(PASSWORD, "", AD_TYPE),
                    new Property(GROUPNAME, "", AD_TYPE),
                    new Property(WORKDIR, " ", AD_TYPE),
                    new Property(SRVRLOG, " ", AD_TYPE),
                    new Property(BRKRLOG, " ", AD_TYPE),
                    (Property) (new IntProperty(BRKR_LOGLEVEL, BRKR_LOGLEVEL_DEF_VAL.toString(), AD_TYPE)),
                    (Property) (new EnumProperty(BRKR_LOGAPPEND, BRKR_LOGAPPEND_DEF_VAL, onOffList, AD_TYPE)),
                    (Property) (new IntProperty(SRVR_LOGLEVEL, SRVR_LOGLEVEL_DEF_VAL.toString(), AD_TYPE)),
                    (Property) (new EnumProperty(SRVR_LOGAPPEND, SRVR_LOGAPPEND_DEF_VAL, onOffList, AD_TYPE)),
                    new Property(CNTL_NAME_SRVR, " ", AD_TYPE),
                    (Property) (new IntProperty(REGIS_RETRY, REG_RETRY_DEF_VAL.toString(), AD_TYPE)),
                    new Property(ENVIRONMENT, " ", AD_TYPE),
                    (Property) (new IntProperty(INFO_VERSION, INFO_VERSION_DEF, AD_TYPE)),
                    new Property(SERVICE_NAMES, " ", AD_TYPE),
                    new Property(UUID, " ", AD_TYPE),
                    new Property(DESCRIPTION, " ", AD_TYPE),
                    new Property(JVMARGS, jvmArgsDefault, AD_TYPE),
                    (Property) (new IntProperty(MAX_CLIENT_INST, MAX_CLIENT_INST_DEF_VAL.toString(), AD_TYPE)),
                    new Property(REG_MODE, REG_IP, AD_TYPE),
                    new Property(HOST, " ", AD_TYPE),
                    new Property(UB_CLASSMAIN, ADAPTER_CLASSMAIN_DEF, AD_TYPE),
                    new Property(STARTUP_PARAM, "", AD_TYPE)
            };

            PropMgrUtils.m_propMgr.registerGroup(
                    ADAPTER_GROUP, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );
            m_regAdapterGroup = true;
        }

        private void regAiaProperties()
                throws PropertyValueException
        {
            String[] onOffList =
                                 {
                                 PROP_ON_VAL, PROP_OFF_VAL};
            String jvmArgsDefault = ((System.getProperty("os.name")).indexOf("Windows") < 0) ?
                                    JVMARGS_DEF_UNIX : JVMARGS_DEF_NT;
            Property[] propList =
                    {
                    new Property(LOGFILE, " ", AIA_INT_TYPE),
                    (Property) (new IntProperty(LOGLEVEL, LOGLEVEL_DEF_VAL.toString(), AIA_INT_TYPE)),
                    (Property) (new EnumProperty(LOGAPPEND, LOGAPPEND_DEF_VAL, onOffList, AIA_INT_TYPE)),
                    new Property(CNTL_NAME_SRVR, " ", AIA_INT_TYPE),
                    (Property) (new EnumProperty(HTTPSENABLED, HTTPSENABLED_DEF_VAL, onOffList, AIA_INT_TYPE)),
                    (Property) (new IntProperty(IDDLECON_TIMEOUT, IDDLECON_TIMEOUT_DEF_VAL.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(MIN_NSCLIENTPORT, NSCLIENTPORT_DEF.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(MAX_NSCLIENTPORT, NSCLIENTPORT_DEF.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(NSCLIENTPORT_RETRY_INTERVAL, NSCLIENTPORT_DEF.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(NSCLIENTPORT_RETRY, NSCLIENTPORT_DEF.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(SECUREPORT, SECUREPORT_DEF.toString(), AIA_INT_TYPE)),
                    (Property) (new IntProperty(SO_READ_TIMEOUT, SO_READ_TIMEOUT_DEFAULT.toString(), AIA_INT_TYPE)),
                    (Property) (new EnumProperty(ALLOW_AIA_CMDS, ALLOW_AIA_CMDS_DEF_VAL, onOffList, AIA_INT_TYPE)),
                    (Property) (new Property(ADMIN_IP_LIST, " ", AIA_INT_TYPE))
            };

            PropMgrUtils.m_propMgr.registerGroup(
                    ADAPTER_GROUP, // groupName
                    false, // restrictChildren
                    false, // restictGrandChildren
                    true, // restrictProperties
                    propList
                    );
            m_regAdapterGroup = true;
        }


        private void regPreferences()
                throws PropertyValueException
        {
            Property[] propList =
                    {
                    (Property) (new IntProperty(TOOL_GET_SVC_STATUS_RETRY,
                                                (new Long(TOOL_GET_STATUS_RETRY_DEF)).toString())),
                    (Property) (new IntProperty(TOOL_PING_SVC_RETRY, (new Long(TOOL_PING_RETRY_DEF)).toString())),
                    (Property) (new IntProperty(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY,
                                                (new Long(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_DEF)).toString())),
                    (Property) (new IntProperty(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL,
                                                (new Long(TOOL_SHUTDOWN_SVC_CONFIRM_RETRY_INTERVAL_DEF)).toString())),
                    (Property) (new IntProperty(TOOL_CONNECT_SVC_RETRY,
                                                (new Long(TOOL_CONNECT_SVC_RETRY_DEF)).toString())),
                    (Property) (new IntProperty(TOOL_CONNECT_SVC_RETRY_INTERVAL,
                                                (new Long(TOOL_CONNECT_SVC_RETRY_INTERVAL_DEF)).toString())),
                    (Property) (new IntProperty(ADMSRVR_REGISTER_RETRY,
                                                (new Long(ADMSRVR_REGISTER_RETRY_DEF)).toString())),
                    (Property) (new IntProperty(ADMSRVR_REGISTER_RETRY_INTERVAL,
                                                (new Long(ADMSRVR_REGISTER_RETRY_INTERVAL_DEF)).toString()))
            };

            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    PREFERENCEROOT_GROUP, // groupName
                    false, // restrictChildren
                    true, // restictGrandChildren
                    true, // restrictProperties
                    propList, // property to register
                    true // saveEvenIfEmpty
                    );

            //
            // set saveEvenIfEmpty, see comments in regEnvPropGroup().
            //
            PropMgrUtils.m_propMgr.registerGroup(
                    PREFERENCE_CHILD_GROUP_SPEC, // groupName
                    true, // restrictChildren
                    true, // restictGrandChildren
                    true, // restrictProperties
                    null, // property to register
                    true // saveEvenIfEmpty
                    );

            m_regPreferences = true;
        }
    }


    //
    // inner class: UBProperties -  extends the base class of PropertyManager
    // so that the order of the property groups defined in the property file
    // is preserved.
    //
    public static final String m_schemaFile = "ubroker.schema";
    public class UBProperties
            extends UbrokerPropertyManager
    {
        boolean m_checkVersion = true;
        boolean m_versionMatched = false;
        int m_numFileLoad = 0;
        boolean hasEventBrkr = false;

        public UBProperties()
                throws PropertyManager.PropertyException
        {
            this(true);
//System.out.println("Instantiating UBProperties...done");
        }

        public UBProperties(boolean useEventBrkr)
                throws PropertyManager.PropertyException
        {
            super(m_schemaFile, useEventBrkr ?
                  (com.progress.common.networkevents.EventBroker)
                  com.progress.ubroker.tools.AbstractGuiPlugin.getEventBroker()
                  : null);
            hasEventBrkr = useEventBrkr;
        }

        public boolean hasEventBrkr()
        {
            return hasEventBrkr;
        }

        public Hashtable getCategories(String groupPath)
        {
            String[] cats = m_metaSchema.getArrayProperty("Group." + groupPath + ".Categories");
            if (cats != null && cats.length > 0)
            {
                Hashtable table = new Hashtable();
                for (int i = 0; i < cats.length; i++)
                {
                    String cName = cats[i];
                    table.put(cName, getCategoryAttributeHashtable(cName));
                }
                return table;
            }

            return new Hashtable();
        }

        public void setVersionCheck(boolean versionChkOpt)
        {
            m_checkVersion = versionChkOpt;
        }

        public boolean versionMatched()
        {
            if (m_checkVersion)
            {
                return m_versionMatched;
            }
            else
            {
                return true;
            }
        }

        /*
         * these methods override their super methods
         * defined in the PropertyManager class.
         */
        protected boolean chkPropertyVersion(String line)
        {
            m_numFileLoad++;

            // Always return true.  Version checking has been replaced
            // with backwards compatible schemas.
            m_versionMatched = true;
            return m_versionMatched;
        }
    }


    private String matchSvcName(String[] grpPathList, String svcName)
    {
        boolean noMatch = true;

        String nextName = null;
        int index;
        int next = 0;

        while (noMatch && next < grpPathList.length)
        {
            index = grpPathList[next].indexOf(GROUP_SEPARATOR + svcName);
            if (index > 0)
            {
                // make sure it matches exactly, and isn't just the first part of an existing
                // name...
                // for example svcName asbroker should not be valid if there is
                // only an asbroker1 in the properties file
                nextName = grpPathList[next].substring(index + 1);
                if (nextName.equals(svcName))
                {
                    //if (grpPathList[next].indexOf(GROUP_SEPARATOR + svcName) > 0)
                    noMatch = false;
                }
                else
                {
                    next++;
                }
            }
            else
            {
                next++;
            }
        }
        if (!noMatch)
        {
            return (grpPathList[next]);
        }
        else
        {
            return (null);
        }
    }


}

