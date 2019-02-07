//  Copyright (c) 1984-2007 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be reproduced in any form or by any means without
//  permission in writing from Progress Software Corporation.

/*-----------------------------------------------------------

 This file is a generated java source file that contains build
 specific information from the system on which the build was run
 as well as the $DLC/version file information.  Any tool may
 utilize this information.

 The template file that is used to generate the final java
 source file is: $RDLSC/ProgressVersion.template.  The script
 that does the transformations is: $RDLSC/makejversion.

 -----------------------------------------------------------*/

package com.progress.common.util;
import java.lang.*;
import java.util.*;
import com.progress.international.resources.ProgressResources;

public class ProgressVersion
{


    // Resource bundle for ProxyGen's UI
    private static ProgressResources m_resources = 
        (ProgressResources)ProgressResources.getBundle("com.progress.international.messages.ProgressBundle");

    private static String m_buildVersion = "11.6.0";
    private static String m_buildMachine = "solbuild14";
    private static String m_buildTime    = "Dec  6 18:45:17 EDT 2015";
    private static String m_buildOS      = "solaris 5.9";
    private static String m_minVer       = null;
    private static ProgressVersionInfo vInfo = new ProgressVersionInfo();

    // Exposed methods
    public static String getVersion()
    {
        return vInfo.getVersionJNI();
    }
    
    public static String getVersionString()
    {
        return (m_resources.getTranString("PR_Version") + " " + vInfo.getVersionJNI());
    }
    
    public static String getMachine()
    {
	return "";
    }
    
    public static String getMachineString()
    {
	return "";
    }
   
    public static String getTime()
    {
	return "";
    }
    
    public static String getTimeString()
    {
	return "";
    }
    
    public static String getOS()
    {
	return "";
    }
    
    public static String getOSString()
    {
	return "";
    }
   
    public static String getFullVersion()
    {
        return vInfo.getFullVersionJNI();
    }

    public static String getFullVersionString()
    {
        return (m_resources.getTranString("PR_Version") + " " + 
                vInfo.getFullVersionJNI());
    }



public static int  getBuildNumber ()
{
 return vInfo.getBuildNumberJNI();
}

public static int  getMajorNumber ()
{
 return vInfo.getMajorNumberJNI();
}

public static int  getMinorNumber ()
{
 return vInfo.getMinorNumberJNI();
}

public static String  getMaintenanceLevel ()
{
 return vInfo.getMaintenanceLevelJNI();
}

public static int  getServicePackNumber ()
{
 return vInfo.getServicePackNumberJNI();
}

public static int  getTemporaryFixNumber ()
{
 return vInfo.getTemporaryFixNumberJNI();
}


public static String getBaseVersion ()
{
 return vInfo.getBaseVersionJNI ();
}


/**
* This in the java front-end to the native method - validatePatchlevel.
* It returns a formatted string as follows:
* "retCode+currentVersion+minimumVersion"
* where: retCode is the return code of the function. Interpreted as 
*        1: passed validation, 0: failed validation, <0: failed
*/
public static boolean validPatchlevel (String addOnProduct_dir)
{
String m_retCode = null;
String m_curVer = null;
boolean f_rc;
int ret = 0;

       String m_retStr = vInfo.validatePatchJNI(addOnProduct_dir);

       if (m_retStr == null)
       {
         f_rc = false;
       }
       else
       {
          StringTokenizer t = new StringTokenizer(m_retStr, "+");
          int cnt = t.countTokens();
          if (cnt != 3)
            f_rc = false;
          else
          {
            m_retCode = (String)t.nextToken(); 
            m_curVer  = (String)t.nextToken();
            m_minVer  = (String)t.nextToken();
            try {
                  ret = Integer.parseInt(m_retCode);
                } catch (NumberFormatException nfe)
                  {
                    f_rc = false;
                  }
             f_rc = (ret == 1);
           }
       }
       return f_rc;
}

public static String getMinVer()
{
      return m_minVer;
}


}

