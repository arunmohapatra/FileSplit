//**************************************************************
//  Copyright (c) 1984-2003 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//  PrologFile.java
//
//  This class allows JNI access to the Progress Database Log file
//  through the jutil/julog.c interface.  An instance of this
//  class represents an open log file.  The file is closed
//  when an instance of this class is finalized.
//
//  To create the .h file for this class (on NT, anyway) that can
//  be used with the associated .c file, do the following:
//
//      cd x:/com/progress/common/util
//      javah [-v] -jni PrologFile
//
//  to run the class REQUIRES the java startup option -DInstall.Dir=<DIR>.
//  where <DIR> is top-level "installation" directory.  The JNI loaded DLL
//  is expected to live in <DIR>/bin.

//  History:
//
//      01/29/98    B. Sadler   Created class.
//

package com.progress.common.util;


public class PrologFile
{
    //
    //      Exception raised by this class.
    //
    // note this is not a ProException subclass!   (at least for now).  Since
    // ProException tries to open the PromsgsFile and I think we want to open
    // the logfile *before* we open the promsgs file, we could have an infinite
    // loop if we fail to open the log file.  (there may be a better way to keep the
    // infinite loop from occurring, like carefully rearranging the code in
    // ProMessageAddapter, but we'll stay with this restriction for now.)
    //
    public static class PrologFileIOException extends Exception
    {
        public PrologFileIOException()         { super(); }
        public PrologFileIOException(String s) { super(s); }
    }


    //
    //      Instance Variables
    //
    // this class has one instance variable:  an opaque "handle" to
    // the context block used in the julog.c code.
    //
    private JNIHandle logHandle;


    //
    //      Public Methods.
    //
    // this constructor opens a database log file. A file named
    // <dbName>.lg is opened.
    //
    public PrologFile(String dbName, boolean createIfNotFound)
                                            throws PrologFileIOException
        {
            openFile (dbName, createIfNotFound);
        }
    public PrologFile(String dbName)       throws PrologFileIOException
        {
            openFile (dbName, false);
        }

    //
    // return JNI handle
    //
    public JNIHandle getHandle()
    {
        return logHandle;
    }

    //
    // the write functions write a string to the log file, with or without
    // a message prefix, or write the time-date stamp to the file.
    //
    public void write(String msg)           throws PrologFileIOException
    {
        writeFile (null, msg, false);
    }
    public void write(String msgPrefix, String msg)
                                            throws PrologFileIOException
    {
        writeFile (msgPrefix, msg, false);
    }
    public void writeDate()                 throws PrologFileIOException
    {
        writeFile (null, null, true);
    }


    //
    //      Finalizer Method.
    //
    // before an instance of this class is garbage collected, the log
    // file should be closed.
    //
    protected void finalize() throws Throwable
    {
        juLogClose (PrologFileIOException.class, logHandle.getAddr());
        super.finalize();
        // the call to super.finalize is not absolutly necessary since we are
        // simply a subclass of Object, but it's good practice anyway.
    }


    //
    //      Private Methods.
    //
    public void openFile(String dbName, boolean createIfNotFound)
                                                throws PrologFileIOException
    {
        //
        // open the log file and save handle in private instance variable.
        // Since we're opening the file for the process, write a line with the date.
        //
        logHandle = new JNIHandle();

        long hdl = juLogOpen (PrologFileIOException.class,
                              dbName,
                              createIfNotFound);
        logHandle.setAddr(hdl);
    }

    public void writeFile(String msgPrefix, String msg, boolean writeDateLine)
                                                throws PrologFileIOException
    {
        juLogWrite (PrologFileIOException.class, logHandle.getAddr(),
                    msgPrefix, msg, writeDateLine);
    }


    //
    //      Private Native Methods.
    //
    // these functions are implemented in PrologFile.c
    //
    private native long
    juLogOpen (Class e,
               String dbName, boolean createIfNotFound)
                                                throws PrologFileIOException;
    private native void
    juLogWrite (Class e, long handle,
                String msgPrefix, String msg, boolean writeDateLine)
                                                throws PrologFileIOException;
    private native void
    juLogClose (Class e, long handle)           throws PrologFileIOException;


    //
    //      JNI library loader for this JNI class
    //
    static
    {
        InstallPath iPath = new InstallPath(); //("d:\\junipervc\\prologfile\\debug\\");
        System.load(iPath.fullyQualifyFile("jni_util.dll"));
    }
}
