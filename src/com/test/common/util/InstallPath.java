package com.progress.common.util;

import java.util.*;
import com.progress.common.text.*;

/**
 * This class provides services to determine where Progress
 * was installed and resolve path names relative to it. By
 * default the property "Install.Dir" is checked to determine
 * the install path. It is set using the java start
 * argument -DInstall.Dir=<dir name>
 * 
 * The usage of Install.Dir can be overriden by calling 
 * setInstallDir. This method was implemented to support the
 * Progress Explorer MMC snapin. In it Progress Explorer is 
 * loaded as a DLL into MMC and not stated with jvmstart. 
 * Progress Explorer calls setInstallDir passing it the value 
 * of DLC as defined in the registry.
 * 
 * If setInstallDir is not called, the behavior of this class 
 * is unchanged.
 *
 * The original implementation of this class provided no static
 * methods. Logically all methods in here could have been
 * static. This would have been clearer where setInstallPath is
 * static. This however is too entrenced to change. So beware,
 * setInstallPath sets it for whoever calls getInstallPath.
 *  
 * While debugging, the "debug path" may be passed to the
 * constructor which will override the "Install.Dir"
 * mechanism.  In this case, caller must include all file
 * separators in path string.
 *
 * old examples:
 *
 *      // use Install.Dir directory to find dll
 *      InstallPath iPath = new InstallPath();
 *      System.load(iPath.fullyQualifyFile("Environment.dll"));
 *
 *      // use explicit path to find dll
 *      InstallPath iPath = new InstallPath("d:\\brent\\Environment\\debug\\");
 *      System.load(iPath.fullyQualifyFile("Environment.dll"));
 * 
 * @author unknown (creator)
 * @author Jim Arsenault (setInstallPath mods)
 */
public class InstallPath
{
   private static String installPath = null; 
   private        String debugPath   = null;
   
   /**
    * Sets the Progress install path. This method provides a way to override
    * the default install path determination (-DIntall.Dir). 
    */
   public static void setInstallPath (String path)
   {
       installPath = path;
   }
    
   /**
    * Returns the Progress install path. If the install path was not set
    * by a call to setInstallPath(), the install path will be determined
    * by the setting of -DInstall.Dir.
    */
   public String getInstallPath ()
   {
       if (installPath == null)
       {
            installPath = new UnquotedString(System.getProperty("Install.Dir")).toString();
       }
       return (installPath);
   }
    
   /**
    * Returns a fully resolved file name relative to DLC.
    * 
    * @param baseName Name of file (ex. "proexp.dll")
    * @param dirName Sub directory of DLC for file (ex. "bin")
    */
   public String fullyQualifyFile(String baseName, String dirName)
   {
        String dlc = getInstallPath();
        String fileSep = System.getProperty("file.separator");
        return (dlc + fileSep + dirName + fileSep + baseName);
   }   
   
   /**
    * Returns a fully resolved file name relative to DLC for files
    * which exist in the "bin" subdirectory. Note, this method is not
    * static to support original behavior where InstallPath could
    * be constructed with a debug path.
    * 
    * @param baseName Name of file (ex. "proexp.dll")
    */ 
   public String fullyQualifyFile(String baseName)
   {
       String retString;

       if (debugPath == null)
       {
           retString = fullyQualifyFile (baseName, "bin");
       }
       else
       {
           retString = debugPath + baseName;
       }
       return retString;
  }

   /**
    * Constructors
    */
   public InstallPath()         { debugPath = null; }
   public InstallPath(String s) { debugPath = s; }
}
 
// END OF FILE
 
