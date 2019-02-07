//
//  All rights reserved.  No part of this program or document
//  may be reproduced in any form or by any means without
//  permission in writing from Progress Software Corporation.

/*-----------------------------------------------------------*/


package com.progress.common.util;
import com.progress.international.resources.ProgressResources;

public class ProgressVersionInfo
{
public native int  getBuildNumber ();
public native int  getMajorNumber ();
public native int  getMinorNumber ();
public native String  getMaintenanceLevel ();
public native int  getServicePackNumber ();
public native int  getTemporaryFixNumber ();
public native String getBaseVersion ();
public native String getVersion ();
public native String getFullVersion ();
public native String validatePatch (String addOnProduct_dir);


/*
** Public methods used to access the JNI routines.
*/

public int  getBuildNumberJNI ()
{
    return getBuildNumber();
}

public int  getMajorNumberJNI ()
{
    return getMajorNumber();
}

public int  getMinorNumberJNI ()
{
    return getMinorNumber();
}

public String  getMaintenanceLevelJNI ()
{
    return getMaintenanceLevel();
}

public int  getServicePackNumberJNI ()
{
    return getServicePackNumber();
}

public int  getTemporaryFixNumberJNI ()
{
    return getTemporaryFixNumber();
}

public String getBaseVersionJNI ()
{
    return getBaseVersion();
}

public String getVersionJNI ()
{
    return getVersion();
}

public String getFullVersionJNI ()
{
    return getFullVersion();
}

public String validatePatchJNI (String addOnProduct_dir)
{
    return validatePatch(addOnProduct_dir);
}


   static
   {
     InstallPath iPath = new InstallPath();
     System.load(iPath.fullyQualifyFile("versioninfo.dll"));
   }
}

