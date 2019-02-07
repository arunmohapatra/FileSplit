//**************************************************************
//  Copyright (c) 1984-1998 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
//  @(#)CfgValidateErrs.java  2.1  8/18/99
//
//  This serializable class contains a list of problems that are found
//  after the configuration of a nameserver or ubroker instance is validated.
//  The validation process usually takes place in the AdminServer side.
//  As the result of the validation, this error object is exported to the tool panel
//  side for gui presentation of the detail of the problems.
//
//  @author: Edith Tan,  11/19/98
//
//  History:
//
//   8/18/99   est  Make Cfg/Validate error a separate object that encapsulates bad property
//                  name, the associated error code and error message.
//
//   1/25/99   est  Added support for catching invalid environment variables.
//
//   1/06/99   est  Added constant for invalid instance name; Added state access methods:
//                  isCfgValidated() and foundInvalidProp().
//
//   12/29/98  est  Change state flag to int so we can represent the true state of the
//                  object.
//
package com.progress.ubroker.util;

import java.rmi.*;
import java.util.*;
import java.util.Enumeration;
import java.io.*;

public class CfgValidateErrs implements Serializable
{
  public static final int GET_PROP_LIST = 1;
  public static final int GET_MSG_LIST  = 2;
  public static final int NOT_VALIDATED = 0;
  public static final int HAVE_INVALID_PROPERTIES = 1;
  public static final int VALIDATED = 2;
  public static final String INSTANCE_NAME = "instName";
  public static final String NAME_NOT_UNIQUE = "New instance name is not unique.";

  public int             m_validated = NOT_VALIDATED;
  Vector                 m_cfgErrors;
  Hashtable              m_envVarErrs;

  public CfgValidateErrs()
  {
    m_cfgErrors = new Vector();
    m_envVarErrs = new Hashtable();
    m_validated = NOT_VALIDATED;
  }

  public void add(String propertyName, String errorMsg)
  {
    ValidateErrObj errObj = new ValidateErrObj(propertyName, errorMsg);
    m_cfgErrors.addElement((Object)errObj);
    m_validated = HAVE_INVALID_PROPERTIES;
  }
  
  public void add(String propertyName, String errMsg, long errCode)
  {
    ValidateErrObj errObj = new ValidateErrObj(propertyName, errMsg, errCode);
    m_cfgErrors.addElement((Object)errObj);
    m_validated = HAVE_INVALID_PROPERTIES;
  }

  public void addEnvVarErrs(String propertyName, String errorMsg)
  {
    m_envVarErrs.put(propertyName, errorMsg);
    m_validated = HAVE_INVALID_PROPERTIES;
  }

  public int size()
  {
    return m_cfgErrors.size();
  }

  public int envVarErrsSize()
  {
    return m_envVarErrs.size();
  }

  public String[] getInvalidProplist()
  {
    String[] badPropList = null;
    if (size() > 0)
    {
      badPropList = new String[m_cfgErrors.size()];
      Enumeration badPropErrList = m_cfgErrors.elements();
      int i = 0;
    
      while (badPropErrList.hasMoreElements())
      {
        ValidateErrObj errObj = (ValidateErrObj)(badPropErrList.nextElement());
        badPropList[i++] = errObj.getPropName();
      }
    }
    return(badPropList); 
  }

  public String[] getErrMsgList()
  {
    String[] errMsgList = null;
    if (size() > 0)
    {
      errMsgList = new String[m_cfgErrors.size()];
      Enumeration badPropErrList = m_cfgErrors.elements();
      int i = 0;
    
      while (badPropErrList.hasMoreElements())
      {
        ValidateErrObj errObj = (ValidateErrObj)(badPropErrList.nextElement());
        errMsgList[i++] = errObj.getDetailErrMsg();
      }
    }
    return(errMsgList); 
  }
  public Integer[] getErrCodeList()
  {
    Integer[] errCodeList = null;
    if (size() > 0)
    {
      errCodeList = new Integer[m_cfgErrors.size()];
      Enumeration badPropErrList = m_cfgErrors.elements();
      int i = 0;
    
      while (badPropErrList.hasMoreElements())
      {
        ValidateErrObj errObj = (ValidateErrObj)(badPropErrList.nextElement());
        errCodeList[i++] = new Integer( errObj.getErrCode());
      }
    }
    return(errCodeList); 
  }
  
  public String[] getBadEnvVarPropList()
  {
    return(getKeyOrElementlist(GET_PROP_LIST, m_envVarErrs));
  }

  public String[] getBadEnvVarMsgList()
  {
    return(getKeyOrElementlist(GET_MSG_LIST, m_envVarErrs));
  }

  public boolean isEmpty()
  {
    boolean hasError = m_cfgErrors.size() == 0 && m_envVarErrs.size() == 0;
//System.out.println("no Errors? " + hasError);
    return(hasError);
  }

  public boolean isEnvVarErrsEmpty()
  {
    return (m_envVarErrs.size() == 0);
  }

  public void setValidated(int value)
  {
    m_validated = value;
  }

  public void didValidation()
  {
//System.out.println("propety is validated");    
    setValidated(VALIDATED);
  }

  public boolean isCfgValidated()
  {
    return (m_validated == VALIDATED || foundInvalidProp());
  }

  public boolean foundInvalidProp()
  {
    return (m_validated == HAVE_INVALID_PROPERTIES);
  }

  public void setDupNameError()
  {
    add(INSTANCE_NAME, NAME_NOT_UNIQUE);
  }

  private String[] getKeyOrElementlist(int getType, Hashtable errList)
  {
    int theSize = errList.size();
    if (theSize > 0)
    {
      Enumeration enumList;
      if (getType == GET_PROP_LIST)
        enumList = errList.keys();
      else
        enumList = errList.elements();
      String[] targetList = new String[theSize];
      int i = 0;
      while (enumList.hasMoreElements())
      {
        targetList[i++] = (String)(enumList.nextElement());
      }
      return targetList;
    }
    else
      return (null);
  }
  
  class ValidateErrObj implements Serializable
  {
    String badPropertyName = null;
    String detailErrMsg = null;
    long  proErrCode = 0;
    ValidateErrObj(String propName, String errMsg)
    {
      badPropertyName = propName;
      detailErrMsg = errMsg;
    }
   
    ValidateErrObj(String propName, String errMsg, long errCode)
    {
      badPropertyName = propName;
      detailErrMsg = errMsg;
      proErrCode = errCode;
    }
    
    public String getPropName()
    {
      return (badPropertyName);
    }
    
    public String getDetailErrMsg()
    {
      return (detailErrMsg);
    }
    
    public int getErrCode()
    {
      return ((new Long(proErrCode)).intValue());
    }
   
  }
}






