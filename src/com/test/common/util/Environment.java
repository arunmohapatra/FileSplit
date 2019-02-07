
/*
**  This class implements a method to retrieve system 
**  environment variables (e.g. getenv).   It is used by
**  the PropertyManager to resolved values for keywords
**  that include an environment variable. For example:
**  This class expects the system property "Install.Dir"
**  to be defined. This is done using the java start
**  argument -DInstall.Dir=<dir name>
**
**     SrvrExeFile=$DLC/bin/_progres
**
*/

package com.progress.common.util;

import java.lang.*;
import java.util.*;

public class Environment
{
   public native void setEnvironmentValue(String name_value);
   public native String getEnvironmentValue(String name);
   public native String expandPropertyValue(String name);
   public native int    query_PID(int pid, boolean isPrivileged);
   public native int    getCurrent_PID(int isOK);



   /*
   ** Public methods used to access the JNI routines.
   */

   public void setEnvironmentValueJNI(String name_value)
   {
       setEnvironmentValue(name_value);
   }

   public String getEnvironmentValueJNI(String name)
   {
       return getEnvironmentValue(name);
   }

   public String expandPropertyValueJNI(String name)
   {
       return expandPropertyValue(name);
   }

   public int query_PID_JNI(int pid, boolean isPrivileged)
   {
       return query_PID(pid, isPrivileged);
   }
 
   public int getCurrent_PID_JNI(int isOK)
   {
       return getCurrent_PID(isOK);
   }

   static
   {
     InstallPath iPath = new InstallPath();
     System.load(iPath.fullyQualifyFile("environ.dll"));
   }
}

