//**************************************************************
//  Copyright (c) 1984-2004 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//**************************************************************
//
//
//  @(#)ToolRemoteCmdStatus.java  2.1  12/23/99
//
//  This class describes the status of a remote command performed by a remote plugin
//  on behalf of its tool instance.
//
//  @author: Edith Tan,  12/15/98
//
//  History:
//
//   12/23/99  est  Added support for GET_NS_LOCATION.
//
//   07/27/99  est  Added support for VALIDATE_ONE_PROPERTY and NS_INST_REFERENCES.
//
//   4/29/99   est  Changed fetchGetGUISchemaFnStatus to fetchGetGUISchemaPropFnStatus.
//                  We only only get schema filename but also the property filename.
//
//   4/22/99   est  Instead of fetching PropertyTransferObject, we are only passing 
//                  reference to PropertyManagerRemoteObject.
//
//   4/5/99    est  Added command setter and getter methods for dealing with the GUI
//                  schema loading and PropertyTransferObject fetching.
//
//   12/29/98  est  added setValidateSaveCfgStatus() and fetchValidateSaveCfgStatus().
//
//
//***********************************************************************************
// NOTE: Please do the following for future enhancement of this class:
// This class is for releases up to 91a.  This class serves as a base class. 
// Any future enhancement for this class must be done in its extended class.
// This basically means that if you need to add new data members or new methods
// to this class, you need to create a new class that extends this base class, 
// and add all the new definitions to the newly extended class. 
//
// There are two companion classes: IToolCmdConst.java and ToolRemoteCmdDescriptor.java
// that may need to be extended as well.  The same rule applies to these two classes.
//***********************************************************************************
//
package com.progress.ubroker.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import com.progress.common.property.IPropertyManagerRemote;
import com.progress.nameserver.NameServer.AppService;

public class ToolRemoteCmdStatus implements Serializable, IToolCmdConst
{
	
	// don't change this.  Its set to be compatible with 11.3.
	// changing it means classes won't work if new methods are added.
	private static final long serialVersionUID = -5935286109935882372L;	
	
  public int     m_command = 0;
  public int     m_statusCode = 0;
  public String  m_detailErrorMsg = null;
  public Vector  m_resultSet = null;

  public ToolRemoteCmdStatus()
  {
    m_resultSet = new Vector();
  }

  public ToolRemoteCmdStatus(int cmd)
  {
    m_command = cmd;
    m_resultSet = new Vector();
  }

  public int getCommand()
  {
    return (m_command);
  }

  public int getStatusCode()
  {
    return(m_statusCode);
  }

  public String getDetailStatusMsg()
  {
    return (m_detailErrorMsg);
  }

  public Enumeration getResultSet()
  {
    return (m_resultSet.elements());
  }

  //
  //  The set value helper methods
  //
  public void setCommand(int cmdValue)
  {
     m_command = cmdValue;
  }

  public void setStatusCode(int statusCode)
  {
    m_statusCode = statusCode;
  }


  public void setDetailStatusMsg(String errorMsg)
  {
    m_detailErrorMsg = errorMsg;
  }


  public void addResultSet(Object resultData)
  {
    m_resultSet.addElement(resultData);
  }

  public void addResultSet(Object[] resultSet)
  {
    for (int i = 0; i < resultSet.length; i++)
    {
      addResultSet(resultSet[i]);
    }
  }

  public void resetResultset()
  {
    if (m_resultSet.size() > 0)
      m_resultSet = new Vector();
  }


  public void setDetailErrMsg(String errmsg)
  {
    if (errmsg == null)
      setErrorStatus(CMD_FAILED);
    else
      setErrorStatus(CMD_FAILED, errmsg);
  }

  public void setSuccessOrFailureStatus(boolean cmdStatus)
  {
    if (cmdStatus)
      setSuccessStatus();
    else
      setErrorStatus(CMD_FAILED);    
  }

  //
  // The command-specific setter methods
  //
  public  void setGetSvcCfgStatus(char[] propertiesStream)
  {
    if (propertiesStream != null)
    {
      setSuccessStatus();
      addResultSet((Object) propertiesStream);
    } else
      setErrorStatus(CMD_FAILED);
  }

  public void setPingSvcStatus(long secSinceActive)
  {
    setSuccessStatus();
    addResultSet((Object)(new Long(secSinceActive)));
  }


  public void setGetPrefPropStatus(char[] propertiesStream)
  {
    if (propertiesStream != null)
    {
      setSuccessStatus();
      addResultSet((Object) propertiesStream);
    }
    else
      setErrorStatus(CMD_FAILED);
  }

 
  public void setGetSummaryStatuslblStatus(Enumeration summaryLblData)
  { 
    addResultSet_setStatus((Object)summaryLblData);
  }

  public  void setGetSummaryStatusDataStatus(Enumeration summaryData)
  {
    addResultSet_setStatus((Object)summaryData);
  }

  public void setGetDetailStatusLblStatus(Enumeration statusLblData)
  {
    addResultSet_setStatus((Object)statusLblData);
  }

  public void setGetDetailStatusDataStatus(Enumeration statusData)
  {
    addResultSet_setStatus((Object)statusData);
  }

  public void setTrimSrvrByStatus(int cmdStatusCode)
  {
    if (cmdStatusCode == 0)
    {
      setSuccessStatus();
      addResultSet((Object)( new Integer(cmdStatusCode)) );
    }
    else
      setErrorStatus(CMD_FAILED); 

  }

  public void setAddNewSrvrsStatus(int cmdStatusCode)
  {
    if (cmdStatusCode == 0)
    {
      setSuccessStatus();
      addResultSet((Object)( new Integer(cmdStatusCode)) );
    }
    else
      setErrorStatus(CMD_FAILED); 
  }

  public void setGetNSSummaryStatus(Object[][] statusData)
  {
    addResultSet_setStatus((Object)statusData);
  }

  public void setGetNSDetailStatus(Object[] statusData)
  {
    addResultSet_setStatus((Object)statusData);
  }

  public void setValidateSaveCfgStatus(CfgValidateErrs valErrObj)
  {
    addResultSet_setStatus((Object)valErrObj);    
  }
  
  public void setLoadGUISchemaStatus(char[] schemaStream)
  {
    addResultSet_setStatus((Object)schemaStream);
  }
  
  public void setLoadGUISchemaStatus(Object statusObj)
  {
    addResultSet_setStatus(statusObj);
  }

  public void setGetGUISchemaPropFnStatus(String[] fnList)
  {
    addResultSet_setStatus((Object)fnList);
  } 
  
  public void setFetchRPMStatus(IPropertyManagerRemote rpm)
  {
    addResultSet_setStatus((Object)rpm);
  }
    
  public void setNSInstRefStatus(int refCount)
  {
    addResultSet_setStatus((Object)new Integer(refCount));
  }
  
  public void setValidateValidOnePropStatus(CfgValidateErrs valErrObj)
  {
    addResultSet_setStatus((Object)valErrObj);    
  }
  
  public void setNSLocPropStatus(String locChoice)
  {
    addResultSet_setStatus((Object)locChoice);
  }
  
  //
  //  the command-specific getter methods
  //
  public char[] fetchGetSvcCfgStatus()
  {
    try
    {
      return( (char[])(getResultSet().nextElement()) );
    } catch (Exception e)
    {
    }
    return null;
  }


  public boolean fetchSaveSvcCfgStatus()
  {
    return(getSuccessOrFailure());
  }


  public boolean fetchStartSvcStatus()
  {
    return(getSuccessOrFailure());
  }


  public boolean fetchStopSvcStatus()
  {
    return(getSuccessOrFailure());
  }


  public boolean fetchPingSvcStatus()
  {
    try
    {
      if (getSuccessOrFailure())
      {
        return( ((long)(((Long)(getResultSet().nextElement())).longValue())) > 0);
      }
    } catch(Exception e)
    {
    }
    return false;
  }

  public String[] fetchGetNSNamesStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (String[])(getResultSet().nextElement()) );
    } catch(Exception e)
    {
    }
    return null;
  }

  public String[] fetchGetAdminRoleNamesStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (String[])(getResultSet().nextElement()) );
    } catch(Exception e)
    {
    }
    return null;
  }

    public String[] fetchGetSSLAliasNamesStatus()
    {
      try
      {
        if (getSuccessOrFailure())
          return( (String[])(getResultSet().nextElement()) );
      } catch(Exception e)
      {
      }
      return null;
    }

  public boolean fetchReplacePropertiesStatus()
  {
    return(getSuccessOrFailure());
  }

  public char[] fetchGetPrefPropStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (char[])(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }

  public boolean fetchPutPrefPropStatus()
  {
    return(getSuccessOrFailure());
  }
  
  
  public char[] fetchLoadGUISchemaStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (char[])(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }
  
  public String[] fetchGetGUISchemaPropFnStatus()
  {
    try
    {
      if (getSuccessOrFailure())
      {
         return( (String[])(getResultSet().nextElement()) );     
      }
    }
    catch(Exception e)
    {
    }
    return null;
  } 
  
  public IPropertyManagerRemote fetchRPMStatus()
  {
    try
    { 
      boolean cmdStatus = getSuccessOrFailure();
      //System.out.println("fetchRPMStatus : cmdStatus = " + cmdStatus);
      if (cmdStatus)
      {
        IPropertyManagerRemote rpm = (IPropertyManagerRemote)(getResultSet().nextElement());
        //System.out.println("  rpm == null? " + rpm == null);
        return( rpm );
      }
    }
    catch(Exception e)
    {
    }
    return null;
  }

  
  public boolean fetchAddNewGuiToolStatus()
  {
    return(getSuccessOrFailure());
  }

  public boolean fetchRemoveGuiToolStatus()
  {
    return(getSuccessOrFailure());
  }
  
  public Enumeration fetchGetSummaryStatuslblStatus()
  { 
    try
    {
      if (getSuccessOrFailure())
        return( (Enumeration)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }

  public Enumeration fetchGetSummaryStatusDataStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (Enumeration)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }

  public Enumeration fetchGetDetailStatusLblStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (Enumeration)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }

  public Enumeration fetchGetDetailStatusDataStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (Enumeration)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }


  public int fetchTrimSrvrByStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (int)((Integer)(getResultSet().nextElement())).intValue() );
    }
    catch(Exception e)
    {
    }
    return -1;
  }

  public int fetchAddNewSrvrsStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (int)((Integer)(getResultSet().nextElement())).intValue() );
    }
    catch(Exception e)
    {
    }
    return -1;
  }

  public CfgValidateErrs fetchValidOnePropValStatus()
  {
    if (getSuccessOrFailure())
      return( (CfgValidateErrs)(getResultSet().nextElement()) );
    return (new CfgValidateErrs());
   }
  
  
  public int fetchNSInstRefStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (int)((Integer)(getResultSet().nextElement())).intValue() );
    }
    catch(Exception e)
    {
    }
    return 0;
  }

  //
  //  status method specific to nameServer commands
  //
  public Object[][] fetchGetNSSummaryStatStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return((Object[][])(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;
  }


  public AppService[] fetchGetNSDetailStatStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (AppService[])(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;     
  }



  public CfgValidateErrs fetchValidateSaveCfgStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (CfgValidateErrs)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;     
  }

  public String fetchGetNSLocPropStatus()
  {
    try
    {
      if (getSuccessOrFailure())
        return( (String)(getResultSet().nextElement()) );
    }
    catch(Exception e)
    {
    }
    return null;     
  }

  //
  // data member set methods
  //   
  public void setSuccessStatus()
  {
    setStatusCode(CMD_SUCCESS);
  }

  public void setErrorStatus(int statusCode)
  {
    setStatusCode(statusCode);
  }

  public void setErrorStatus(int statusCode, String detailErrorMsg)
  {
    setStatusCode(statusCode);
    setDetailStatusMsg(detailErrorMsg);
  }

  public boolean getSuccessOrFailure()
  {
    return(getStatusCode() == CMD_SUCCESS);
  }

  
  protected void addResultSet_setStatus(Object resultData)
  {
    if (resultData != null)
    {
      setSuccessStatus();
      addResultSet(resultData);
    }else
       setErrorStatus(CMD_FAILED); 
  }
  
}






 
// END OF FILE
 
