//**************************************************************
//  Copyright (c) 1984-2007 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//  PromsgsFile.java
//
//  This class allows JNI access to the Progress PROMSGS file
//  through the jutil/jupromsgs.c interface.  An instance of this
//  class represents an open promsgs file.  The file is closed
//  when an instance of this class is finalized.
//
//  To create the .h file for this class (on NT, anyway) that can
//  be used with the associated .c file, do the following:
//
//      cd x:/com/progress/common/util
//      javah [-v] -jni PromsgsFile
//
//  to run the class REQUIRES the java startup option -DInstall.Dir=<DIR>.
//  where <DIR> is top-level "installation" directory.  The JNI loaded DLL
//  is expected to live in <DIR>/bin.

//  History:
//
//      01/28/98    B. Sadler   Created class.
//      06/15/98    B. Sadler   Changed message id from int to long.
//
//  To do:
//      - fill in the rest of the getProLocale function to
//        translate java.lang.Locale instances to Progress 3-char
//        locale identifiers.
//

package com.progress.common.util;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.progress.common.message.*;


public class PromsgsFile implements IProMessage
{
    //
    //      Exception raised by this class.
    //
    // note that this cannot be a ProException subclass!  Since ProException tries
    // to open the PromsgsFile to read a message, an infinite loop would occur if
    // PromsgsFileIOException were thrown.  (there may be a better way to keep the
    // infinite loop from occurring, like carefully rearranging the code in
    // ProMessageAddapter, but we'll stay with this restriction for now.)
    //
    public static class PromsgsFileIOException extends IProMessage.ProMessageException
    {
        public PromsgsFileIOException()         { super(""); }
        public PromsgsFileIOException(String s) { super(s); }
    }


    //
    //      Class Variables
    //
    // this class has two class variables:  an opaque "handle" to
    // the context block used in the jupromsgs.c code, and the 
    // address of the c context block.
    //
    // prmHandle was an instance variable before, however, when an 
    // context is created for each instance of PromsgsFile, there's 
    // shared memory blocks among these contexts (which might be error
    // in itself), so cleaning up one of these contexts can cause 
    // access violation in others.  A solution to this problem is that
    // create one context block instead.  This solution is correct
    // because by current design, 1. there's only one promsgs file
    // available to the users, 2. only one promsgs is used by all
    // components 3. only one PromsgsFile instance can be in existance
    // per session -- if there's others, they will be garbage-collected.
    // (because of the static variables and methods in ExceptionMessageAdaptor).
    //
    private static JNIHandle prmHandle = null;
    private static long hdl = 0;
    private Lock lock = new ReentrantLock(true);//fair flag is true

    //
    //      Public Methods.
    //
    // these constructors open a promsgs file.  If a java Locale is given, it
    // is converted to a progress locale string before passing it to promsgs.c
    //
    public PromsgsFile(String fileName)     throws PromsgsFileIOException
        {
            openFile (fileName, null);
        }
    public PromsgsFile(Locale locale)       throws PromsgsFileIOException
        {
            openFile (null, locale);
        }
    public PromsgsFile()                    throws PromsgsFileIOException
        {
            openFile (null, null);
        }

    //
    // return JNI handle
    //
    public JNIHandle getHandle()
    {
        return prmHandle;
    }


    public String getMessage(long messageId) throws PromsgsFileIOException
    {
    	
    	String message = "";
     	lock.lock();
         // full message ID's are 64 bits long.  the top 32 bits contain the
        // facility code, in ascii and the bottom 32 bits contain the actual
        // binary message number, as it exists in the promsgs file.
        //
        // strip off the top 32 bits to get the message id that promsgs wants.
        //
        int intMessageId = (int) messageId;
        //
        // lookup a message in the message file and return the string to caller
        //
        try{
        	message = juPrmLookup (PromsgsFileIOException.class, prmHandle.getAddr(), intMessageId);
        }
        finally {
        	lock.unlock();
        }
        return message;
    }


    //
    //      Finalizer Method.
    //
    // before an instance of this class is garbage collected, the promsgs
    // file should be closed.
    //
    protected void finalize() throws Throwable
    {
        // Since prmHandle is static now (one and only one per session, and shared
        // among instances of PromsgsFile), there's no need to 
        // clean it up when an instance of PromsgsFile is being garbage 
        // collected.   
        //juPrmClose (PromsgsFileIOException.class, prmHandle.getAddr());
        
        super.finalize();
        // the call to super.finalize is not absolutly necessary since we are
        // simply a subclass of Object, but it's good practice anyway.
    }


    //
    //      Private Methods.
    //
    public void openFile(String fileName, Locale locale) throws PromsgsFileIOException
    {
        // open the promsgs file and save handle in private instance variable.
        // if filename was given, try to open that file.
        // if filename and locale are both null, open default promsgs file
        // if filename is null and locale is not, open promsgs file for given locale
        //
        if ( hdl == 0 )
        {
            hdl = juPrmOpen (PromsgsFileIOException.class,
                             fileName,
                             getProLocale(locale));
        }
        if ( prmHandle == null )
        {
            prmHandle = new JNIHandle();
            prmHandle.setAddr(hdl);
        }
    }

    private String getProLocale (Locale locale)         throws PromsgsFileIOException
    {
        //
        // function to translate from a Java Locale to a progress promsgs
        // locale string.  see jutil/incl/juprmpub.h for a list of known
        // progress locale strings.
        //
        if (locale == null)
            return null;

        if      (locale.equals (Locale.US))                  { return "ame"; }
        else if (locale.equals (Locale.TRADITIONAL_CHINESE)) { return "tch"; }
        //
        // ... fill in rest of algorithm here ...
        //
        else
        {
            // ??? should this be a different exception, say,
            // UnsupportedLocaleException?
            throw new PromsgsFileIOException("unsupported locale");
        }
    }


    //
    //      Private Native Methods.
    //
    // these functions are implemented in PromsgsFile.c
    //
    private native long   juPrmOpen   (Class e, String fileName, String proLocale)
                                                throws PromsgsFileIOException;
    private native String juPrmLookup (Class e, long handle, int messageId)
                                                throws PromsgsFileIOException;
    private native void   juPrmClose  (Class e, long handle)
                                                throws PromsgsFileIOException;

    //
    //      JNI library loader for this JNI class
    //
    static
    {
        InstallPath iPath = new InstallPath(); //("d:\\junipervc\\promsgsfile\\debug\\");

        // shared libary for jutil is called jutil.dll only on NT.
        // we still haven't figured out an unified machanism dealing
        // with loading shared lib's on other platforms (Unix).
        //
        // the reason for loading jutil here is because we want to fully 
        // qualifies the path to jutil.dll vs. jni_util.dll "statically"
        // links to jutil.dll at runtime as it does now in the Makefile.
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) 
        {
            System.load(iPath.fullyQualifyFile("jutil.dll"));
        }
        
        System.load(iPath.fullyQualifyFile("jni_util.dll"));
    }
}

 
// END OF FILE
 
