/*************************************************************/
/* Copyright (c) 2003-2009 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/

/*********************************************************************/
/*                                                                   */
/* Module : ubFileWatchDog                                           */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;


/*********************************************************************/
/* This class is used by the UBroker to determine when a new server  */
/* log file needs to be created for AppServer and WebSpeed when a    */
/* log size threshold is specified.                                  */
/*                                                                   */
/* A sequence number that can go from 1 to 999,999 (inclusive) is    */
/* used to define the log file name as we roll over the logs. The    */
/* format of the log file is: file-name.999999.log.                  */
/* When a log threshold size is specified, the first log file name   */
/* will have sequence number 0000001.                                */
/*                                                                   */
/* This is the flow of the processing of this thread:                */
/* We will figure out if there are any rolled over log files under   */
/* the same location defined for the srvrLogFile property in the     */
/* ubroker.properties file. If there are more files then defined by  */
/* the srvrNumLogFiles property, we will delete the oldest ones until*/
/* we reach the number of logs desired. We base the decision by      */
/* looking at the timestamp of the files and getting the oldest log. */
/* Once we know what is the newest log file, we will set its name to */
/* the log file name property, so the broker sends the correct name  */
/* to the server processes. Every time a file is rolled over, the    */
/* property is updated so the new started servers always get the     */
/* most recent file.                                                 */
/*                                                                   */
/* As we switch to a new log file, we will watch for its size every  */
/* <n> seconds, where <n> is set through a property. We will also    */
/* keep track of what the next log file name will be. We will delete */
/* the next log file if it exists before we create a new log file    */
/* because the servers will keep looking for the next log file to    */
/* decide if they should switch to the new log file, hence the next  */
/* log file must NOT exist or the server may end up in a loop or     */
/* switch to that log file when it shouldn't.                        */    
/* Once the current log file grows beyond the size limit, we will    */
/* create the new log file.                                          */
/*                                                                   */
/* This class uses the Logger logging mechanism and only logs        */
/* diagnostic messages at level LOGGING_DEBUG. When ther broker code */
/* is migrated to use the new logging infrastructure, the messages   */
/* here should only be logged at the highest level and they can go   */
/* with the default entry type.                                      */
/*********************************************************************/

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Arrays;
import com.progress.common.ehnlog.IAppLogger;
import com.progress.common.ehnlog.UBrokerLogContext;
import com.progress.common.ehnlog.ILogEvntHandler;

/*********************************************************************/
/*                                                                   */
/* Class ubFileWatchDog                                              */
/*                                                                   */
/*********************************************************************/

public class ubFileWatchDog
    extends Thread
    implements ubConstants, IWatchable
{

/*********************************************************************/
/* Constants                                                         */
/*********************************************************************/

static final int MAX_SEQUENCE_NUMBER = 999999;
static final int SEQNUMSIZE          = 6;
static final int NUMLOGFILES_LOWER   = 2;

/*********************************************************************/
/* Instance Data                                                     */
/*********************************************************************/

private static DecimalFormat    fmt1;

private int          m_sequencenum_pos;
private int          m_watchdogInterval;
private int          m_logThreshold;
private int          m_numLogFiles;
private boolean      m_appendMode;
private int          m_numFilesOnSystem;
private int          m_nextNumSequence;
private String       m_currLogFileName;
private String       m_rolledFiles;
private StringBuffer m_nextLogFileName;
private StringBuffer m_fileNameToDelete;
private ubWatchDog   m_fileWatchdog;
private IAppLogger   m_log;

private File         m_File;
private ubProperties m_properties;
private ILogEvntHandler event_handler;

private static final int LOG_THRESHOLD_MIN = 500000;
private static final int LOG_THRESHOLD_MAX = 2147438647;

static 
  {
  fmt1 = new DecimalFormat("000000");
  }

/*********************************************************************/
/* Constructors                                                      */
/*********************************************************************/

public ubFileWatchDog (ubProperties prop,
                       IAppLogger log )
   {
   m_properties        = prop;
   m_logThreshold      = m_properties.getValueAsInt(ubProperties.PROPNAME_SRVRLOGTHRESHOLD);
   m_numLogFiles       = m_properties.getValueAsInt(ubProperties.PROPNAME_SRVRNUMLOGFILES);
   m_appendMode        = (m_properties.getValueAsInt(ubProperties.PROPNAME_SRVRLOGAPPEND) == 1);
   m_numFilesOnSystem  = 0;
   m_nextNumSequence   = 1;
   m_nextLogFileName   = null;
   m_fileNameToDelete  = null;

   /* this propety is in seconds, but ubWatchDog receives milliseconds*/
   m_watchdogInterval = m_properties.getValueAsInt(ubProperties.PROPNAME_SRVRLOGFILEWATCHDOG) * 1000;
   
   m_File    = null;   
   
   this.m_log          = log;

   /* we only run this if logthreshold was specified */    
   if (m_logThreshold >= LOG_THRESHOLD_MIN &&
       m_logThreshold <= LOG_THRESHOLD_MAX )
      {
      /* process existing files */
       processExistingFiles();

       /* assign current file and properties too */
       /* update property name so servers open the correct file */
       m_properties.adjustServerLogFileValue(m_currLogFileName);
       
      }
   
   }
/*********************************************************************/
/* public methods                                                    */
/*********************************************************************/

public void run()
    {
    boolean ret;
    IAppLogger  tmpLog;

    /* we only run this if logthreshold was specified */    
    if (m_logThreshold >= LOG_THRESHOLD_MIN )
       {
       if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
           {
           m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                          "Starting fileWatchDog thread ...");
           }

       /* keep track of next file */
       m_nextLogFileName = new StringBuffer(m_currLogFileName);
       m_nextLogFileName.replace(m_sequencenum_pos, 
                                 m_sequencenum_pos + SEQNUMSIZE,
                                 fmt1.format(m_nextNumSequence));
    

       /* create new log file in case it does not exist */
       tmpLog = m_properties.initServerLog(m_log, false);
       m_File = new File(m_currLogFileName);
       
       /* the next file can NEVER exist */
       deleteNextLogFile();
       
       /* check for file size, in case current log file is already
         bigger than threshold */
       checkFileSize();

       /* delete old files */
       deleteLogFiles();

       /* OE00129357 - handler for log file roll over */
       event_handler = new LogEvntHandler(m_properties, m_log);
       ((LogEvntHandler)event_handler).setMainLog(false);

       /* now we got to the real thing ! */
       m_fileWatchdog = new ubWatchDog( "fileWatchdog",
                                       this,
                                       m_watchdogInterval,
                                       ubWatchDog.DEF_PRIORITY,
                                       m_log);
       m_fileWatchdog.start();
       }

    }

   
/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public void watchEvent()
   {
    /* this method periodically tests to see if the server log file       */
    /* size is over the limit imposed by the srvrLogThreshold property    */
    /* for AppServer and WebSpeed and swithes to the next log file        */
    /* it also deletes the oldest log files based on the srvrNumLogFiles  */  
    /* property.                                                          */
   checkFileSize();
   deleteLogFiles();
   }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/
   
public void close()
   {

   /* we only run this if logthreshold was specified */
   if (m_logThreshold >= LOG_THRESHOLD_MIN)
      {
       /*stop WatchDog thread */
       if (m_fileWatchdog != null)
           m_fileWatchdog.setInterval(0);
       /* last chance to delete old log files that failed to delete */
       deleteLogFiles();
       }
   event_handler = null;
   }   

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public int getCurrentSequenceNumber()
   {
    /* returns the sequence number of the log file we are currently monitoring 
       If we are not monitoring anything, it will return 0
    */
    return (m_nextNumSequence - 1);
   }
   
/*********************************************************************/
/* private methods                                                   */
/*********************************************************************/
private void checkFileSize()
   {
     IAppLogger tmpLog;
   
     if ((m_File != null) && (m_File.length() > m_logThreshold))
        {
        /* assign next file name*/
        assignNextLogFile();

        /* change property so new servers open new file */
        m_properties.adjustServerLogFileValue(m_currLogFileName);

        if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
            {
            m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                           "Switching to new log file ... " +
                           m_currLogFileName);
            }

        /* create new log file */
        tmpLog = m_properties.initServerLog(m_log, true);
        
        /* new File object to check for file size */
        m_File = new File(m_currLogFileName);
        
        
        /* OE00129357 - now let's post an event for the log file name change if we have a handler */
        if (event_handler != null)
            event_handler.sendFileNameChangedEvent(m_currLogFileName);

        /* increment number of files on system */
        m_numFilesOnSystem++;
        
        /* add current file to the list */
        m_rolledFiles = m_rolledFiles + 
                        m_currLogFileName.substring(m_sequencenum_pos,
                                                    m_sequencenum_pos + SEQNUMSIZE) + ",";
        }
   }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

 private void deleteNextLogFile()
 {
   boolean ret;
   
     /* remove the next log file as it can NEVER exist or the server will loop
        forever trying to find the file in case we rolled over the sequence number 
        or it will be writing to the wrong file as far as the broker is concerned */
        ret = removeLog(m_nextLogFileName.toString());
        if (!ret && 
            m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
            {
            m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                           "File deletion for " + m_nextLogFileName + " returned " + ret);
            }

       /* if file was for any reason already in the list of rolled files, we 
          need to remove it from there and decrement the number of logs on the
          system */
          /* OE00156697 - check if m_rolledFiles is set */
          if (m_rolledFiles != null)
          {
              int pos;
              if ((pos = m_rolledFiles.indexOf(fmt1.format(m_nextNumSequence))) != -1)
              {
               m_numFilesOnSystem--;   
               int next = SEQNUMSIZE + 1;
               try
                  {
                  if (pos == 0)
                     m_rolledFiles = m_rolledFiles.substring(next);
                  else
                     {
                     String tmpStr;
                     tmpStr = m_rolledFiles.substring(0,pos) + m_rolledFiles.substring(pos + next);
                     m_rolledFiles = tmpStr;
                     }
                  }
               catch(Exception e)   
                  {
                  if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                      {
                      m_log.logStackTrace(UBrokerLogContext.SUB_V_UB_DEBUG,
                                          "Exception: ", e);
                      }
                  }
              }
          } /* if m_rolledFiles */
 }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/
   
private void deleteLogFiles()
   {
   int tmpNum = m_numFilesOnSystem;
   int failedToDelete = 0;

   /* don't do anything is we are not keeping track of number of logs */
   if (m_numLogFiles < 2)
       return;
   
   if (m_numLogFiles < tmpNum)
     {
     int initpos = 0;
     int pos = 0;
     String savedName;
     StringBuffer notDeleted = new StringBuffer();

     if (m_fileNameToDelete == null)
         m_fileNameToDelete = new StringBuffer(m_currLogFileName);

     try
        {
        /* go through the list and remove all files we can until we reach the
          numLogFiles property */
        while (m_numLogFiles < tmpNum)
            {
            /* get the sequence number from the list */
            savedName = m_rolledFiles.substring(initpos,initpos + SEQNUMSIZE);
      
            /* replace it in the string */
            m_fileNameToDelete.replace(m_sequencenum_pos, 
                                       m_sequencenum_pos + SEQNUMSIZE,
                                       savedName);

            /* decrement files on system for now*/
            tmpNum--;

            /* delete oldest file */
            if (!removeLog(m_fileNameToDelete.toString()))
               {
               if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                   m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                  "Failed to delete " + m_fileNameToDelete.toString());

               failedToDelete++;
               notDeleted.append(savedName + ",");
               }
            else
               if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                   m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                  "Deleted " + m_fileNameToDelete.toString());
         
            /* next entry in the list */
            initpos = initpos + SEQNUMSIZE + 1;
            } /* while */

        /* if we failed to delete any file, need to leave them at the beginning
           of the list, so we try to delete them again later */
        if (failedToDelete > 0)
           {
           m_rolledFiles = notDeleted.toString() + m_rolledFiles.substring(initpos);
           }
        else
           m_rolledFiles = m_rolledFiles.substring(initpos);
        }
     catch(Exception e)   
        {
        if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
            m_log.logStackTrace(UBrokerLogContext.SUB_V_UB_DEBUG,
                               "Exception: ", e);
        }
           
            
        /* adjust m_numFilesOnSystem with number of files that we could not
           delete */
        m_numFilesOnSystem = tmpNum + failedToDelete;
     } /* if */
     
   } /* end of deleteLogFiles() */

/*********************************************************************/
/*                                                                   */
/*********************************************************************/
   
private void assignNextLogFile()
   {
   boolean ret;
  
   /* we are going to roll over the sequence number back to 1 */
   if (m_nextNumSequence >= MAX_SEQUENCE_NUMBER)
      {
      m_nextNumSequence = 0;
      }

   /* assign current file name */
   m_currLogFileName = m_nextLogFileName.toString();

   /* increment sequence number */
   m_nextNumSequence++;

   try
     {
     /* save now next log file name */
     m_nextLogFileName.replace(m_sequencenum_pos, 
                               m_sequencenum_pos + SEQNUMSIZE,
                               fmt1.format(m_nextNumSequence));
     }
   catch(Exception e)   
     {
     if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
        m_log.logStackTrace(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "Exception: ", e);
     }
   
   deleteNextLogFile();
   }

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private void processExistingFiles()
    {
    int      extpos;
    int      validBkpFileNameLen;
    int      numRemove;
    File     abspath;
    String[] list;
    String   extension;
    String   fname;
    String   path;
    String   filename;
    File     baseFile;
    boolean  no_numLogFiles = false;
    StringBuffer   logRolledFiles = new StringBuffer();

    try
      {
      /* remember if we are not counting the number of log files */
      if (m_numLogFiles < NUMLOGFILES_LOWER)
         no_numLogFiles = true;
           
      /* we will use the base name to figure out the existing log files names */     
      baseFile = new File(m_properties.getValueAsString(ubProperties.PROPNAME_SERVERLOGFILE));
        
      /* try to figure out the file name and the extension, if any */
      abspath = new File(baseFile.getAbsolutePath());

      path = abspath.getParent();   /* get the directory name */
      filename = abspath.getName(); /* get the file name and extension */
  
      extpos = filename.lastIndexOf('.'); /* -1 if not found */

      /* remember the extension position where we are going to include
         the sequence number later */
      String filePath = abspath.getAbsolutePath();
      if (extpos == -1)
          m_sequencenum_pos = filePath.length() + 1;
       else
          m_sequencenum_pos = filePath.lastIndexOf('.') + 1;
          
      /* based on the filename and the size of the sequence number of a file, 
         keep track of the size of a valid file name */
      validBkpFileNameLen = filename.length() + SEQNUMSIZE + 1;

      /* now remember what is the offset of the extension for later processing */
      if (extpos == -1)
         {
         fname = filename;   
         extension = "";
         extpos = filename.length() + 1;
         }
      else
         {  
         fname = filename.substring(0,extpos);
         extension = filename.substring(extpos);
         extpos = extpos + 1;
         }

      /* get a list of the files */        
      list = new File(path).list(new DirFilter(path,fname,extension));
      
	   /* sort the files */
       Arrays.sort(list);
       
      m_numFilesOnSystem = list.length;
   
      int startList = 0;
      int seqNum = 0;
      int tmpSeq = 0;
      
      for (int count = 0; count < list.length; count++)
        {
        try
          {
          tmpSeq = Integer.parseInt(list[count].substring(extpos,extpos + SEQNUMSIZE));
          }
        catch (Exception e)
          {
          tmpSeq = 1;
          }

        /* if there is a gap, it must mean that the last sequence number we read
           is for the last log file we wrote to. The start of the
           rolled over files list is this entry, and the last sequence number is
           the sequence number of the file we should open this time */
        if (tmpSeq > (seqNum + 1))
        {
           startList = count;
           m_nextNumSequence = seqNum;
        }
                
        seqNum = tmpSeq;
        } /* for int count */

        /* if there was no gap, then the last sequence number read is the current
           one */
        if (startList == 0)
            m_nextNumSequence = seqNum;

          
      if (no_numLogFiles)
         numRemove = 0; /* not counting logs */
      else
         numRemove = m_numFilesOnSystem - m_numLogFiles; 
      
      /* if user set NO for append mode, then we are going to get rid of all the
         existing bckup log files and start from the scratch */
      if (!m_appendMode)
         numRemove = m_numFilesOnSystem;
        
      if (numRemove < 0)
          numRemove = 0; /* just for consistency */

      if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
          m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                         "fileWatchDog: Need to remove " + numRemove + " files.");

      /* if we found log files */ 
      if (list.length > 0)
         {
  
         /* now start with what we think the start of the list is,
            so we delete the oldest files first*/
         for(int i = startList, count = 0; count < list.length ; count++,i++)
            {
            if (count < numRemove)
               {
	           // System.err.println("removing " + list[i]);
	           removeLog(list[i]);
               if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                   m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                  "fileWatchDog: removed: " + list[i]);
	           }
	        else
	           {
	           /* keep track of files found */
	           if (!no_numLogFiles)
	              logRolledFiles.append(list[i].substring(extpos,extpos + SEQNUMSIZE) + ",");
	           }

            /* if we hit the last entry in the list, go to the first one, because
               we must have rolled over the sequence number */
            if (i == (list.length - 1))
                i = -1;
        
            } /* FOR */

          } /* if list.length() > 0 */

      m_numFilesOnSystem -= numRemove;

      /* in case there are no log file, add the first one to the list */   
      if (m_numFilesOnSystem == 0)
         {
         m_nextNumSequence = 1;
         try 
           {
            /* if the file which name is the base name exists, we have to try to rename it
               to the sequence 1 first..if we can't, never mind..leave it as it is.. */
            String newName;
            if (extpos == -1)
               newName = filename + "." + fmt1.format(m_nextNumSequence);
            else
               newName = fname + "." + fmt1.format(m_nextNumSequence) + extension;

            if (baseFile.exists())
               {
               /* if user set NO for append mode, then we are going to get rid of all the
                  existing bckup log files and start from the scratch */
               if (m_appendMode)
                  {
                  File newFile = new File(newName);
                  if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
                      m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                                     "Renaming base file to " + newName);
         
                  baseFile.renameTo(newFile);
                  }
               else
                  {
                  /* just remove it if not in append mode */
                  baseFile.delete();
                  }
               }
            }
         catch (Exception e)
            { /* don't do anything */
            }

         logRolledFiles.append(fmt1.format(m_nextNumSequence) + ",");
         m_numFilesOnSystem = 1;
         }

      /* get current log file name */
      if (extension.length() == 0)
         {
          m_currLogFileName = filePath + "." + fmt1.format(m_nextNumSequence);
         }
      else
         {
          m_currLogFileName = filePath.substring(0,m_sequencenum_pos) +
                              fmt1.format(m_nextNumSequence) + extension;
         }
     
      m_nextNumSequence++;
      
      if (m_nextNumSequence > MAX_SEQUENCE_NUMBER)
          m_nextNumSequence = 1;
        
      if (!no_numLogFiles)
         {
         m_rolledFiles = logRolledFiles.toString();
         if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
             m_log.logBasic(UBrokerLogContext.SUB_V_UB_DEBUG,
                            "fileWatchDog: Existing files: " + m_rolledFiles);
         }

      }
    catch(Exception e)   
      {
      if (m_log.ifLogBasic(UBrokerLogContext.SUB_M_UB_DEBUG,UBrokerLogContext.SUB_V_UB_DEBUG))
          m_log.logStackTrace(UBrokerLogContext.SUB_V_UB_DEBUG,
                              "fileWatchDog: ", e);
      }
    } /* end of processExistingFiles */

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

private boolean removeLog(String fid)
     {
     File f = new File(fid);
        
     /* if the file does not exist, return true so we don't mess up with
        the rolled over log files list */
     if (f != null && f.exists())
         return (f.delete());
     else
         return true;
     }


/*********************************************************************/
/* Class DirFilter                                                   */
/*********************************************************************/

protected class DirFilter implements FilenameFilter 
{
  private String filename;
  private String extension;
  private int extlen;
  private int validBkpFileNameLen;

  public DirFilter(String Name, String fname, String ext) 
  {
     filename = fname;
     extension = ext;
     extlen = extension.length();
     validBkpFileNameLen = filename.length() + extlen + ubFileWatchDog.SEQNUMSIZE + 1;
  }

  public boolean accept(File dir, String name) 
  {

	  if (extlen > 0) 
	     if (!name.endsWith(extension) ) 
		 return false;

  	  if (name.startsWith(filename + ".") && 
			(name.length() == validBkpFileNameLen) )
			return true;

      return false;

  }
} 

/**********************************************************************/
/* end of DirFilter protected class                                   */
/**********************************************************************/

/**********************************************************************/
/* end of ubFileWatchDog class                                        */
/**********************************************************************/

}

