/*
**  This class implements  methods to verify a user name
**  and password.  It does the verification (via JNI)
**  by accesses the underlying OS account management
**  api's.  The user/password must be a valid OS-level
**  account on the system where server is running.
**  correctly a -d startup argument is required.
**  This class expects the system property "Install.Dir"
**  to be defined. This is done using the java start
**  argument -DInstall.Dir=<dir name>
**
*/

package com.progress.common.util;

import java.lang.*;
import java.util.*;

public class Authenticate
{
   /*
   ** authorizeUser - returns "1..." if user/password is valid, and user is
   ** found in a group, returns "0..." otherwize
   */
   private static native String authorizeUser(String user, String password,
                                             String groups);

 /**
   * Do Not use this functions, use authorizeUser instead
   */
   private static native boolean verifyUser(String user, String password);
   private static native boolean authenticateUser(String user, String password, boolean name_only);


   private static native String validateGroups(String groups);
   /*
   ** passwdPrompt - returns a text string from keyboard input
   */
   private static native String passwdPrompt(String promptStr);

   /*
    * Get the account owner of the current process.
    */
   private static native String whoami();

   /*
    * Get a comma separated list of user-groups for the specified account.  If
    * it returns null, the account cannot be found.
    */
   private static native String getUserGroups(String userAccountName);



   /*
    * Jacket methods used to call native code.  These are needed due to
    * use of ClassLoader(s) which require all JNI access to be synchronized
    * and called from a common point.  These methods must be used instead
    * of calling the native code directly.
    */

   public static synchronized String authorizeUserJNI(String user, 
                                                      String password,
                                                      String groups)
   {
       return authorizeUser(user, password, groups);
   }

   public static synchronized boolean verifyUserJNI(String user, 
                                                    String password)
   {
       return verifyUser(user, password);
   }

   public static synchronized boolean authenticateUserJNI(String user, 
                                                          String password, 
                                                          boolean name_only)
   {
       return authenticateUser(user, password, name_only);
   }

   public static synchronized String validateGroupsJNI(String groups)
   {
       return validateGroups(groups);
   }

   public static synchronized String passwdPromptJNI(String promptStr)
   {
       return passwdPrompt(promptStr);
   }

   public static synchronized String whoamiJNI()
   {
       return whoami();
   }

   public static synchronized String getUserGroupsJNI(String userAccountName)
   {
       return getUserGroups(userAccountName);
   }




   /*
    * one-time class initialization done at load-time.
    */
   static
   {
     InstallPath iPath = new InstallPath();
     System.load(iPath.fullyQualifyFile("auth.dll"));
   }

}
