//**************************************************************
//  Copyright (c) 2002-2003,2011 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

/*
**  This class implements a method to validate patch levels
**  for add on products (i.e. Fathom, Replication).
**  This class expects the system property "Install.Dir"
**  to be defined. This is done using the java start
**  argument -DInstall.Dir=<dir name>
**
*/

package com.progress.common.util;

import java.lang.*;
import java.util.*;

public class PatchValidation
{
private String ret_str = null;
private String curVer  = null;
private String minVer  = null;
private String retCode = null;

   /**
   * A native method defined in procfg.dll (wsprocfg.c) that
   * implements the validation of and addon product to core (v9)
   */
   public native String validatePatchlevel(String addOn_dir, String core_dir);

   /**
   * This in the java front-end to the native method - validatePatchlevel.
   * It returns a formatted string as follows:
   * "retCode+currentVersion+minimumVersion"
   * where: retCode is the return code of the function. Interpreted as 
   *        1: passed validation, 0: failed validation, <0: failed
   */
   public boolean validPatchlevel(String addOn_dir, String core_dir)
   {
     boolean f_rc;
     int ret = 0;

       ret_str = validatePatchlevel(addOn_dir, core_dir);

       if (ret_str == null)
       {
         f_rc = false;
       }
       else
       {
          StringTokenizer t = new StringTokenizer(ret_str, "+");
          int cnt = t.countTokens();
          if (cnt != 3)
            f_rc = false;
          else
          {
            retCode = (String)t.nextToken(); 
            curVer  = (String)t.nextToken();
            minVer  = (String)t.nextToken();
            try {
                  ret = Integer.parseInt(retCode);
                } catch (NumberFormatException nfe)
                  {
                    f_rc = false;
                  }
             f_rc = (ret == 1);
           }
       }
       return f_rc;
   }

   public String getCurVer()
   {
      return curVer;
   }

   public String getMinVer()
   {
      return minVer;
   }

   
   static
   {
     InstallPath iPath = new InstallPath();

     try
     {
         System.load(iPath.fullyQualifyFile("procfg.dll"));
     }
     catch (UnsatisfiedLinkError e)
     {
         /* If the load failed we may be running 32-bit Eclipse in a 64-bit
         ** OE installation. Try the 32-bit library in $DLC/bin32.
         */
         System.load(iPath.fullyQualifyFile("procfg.dll", "bin32"));
     }
   }
}
 
