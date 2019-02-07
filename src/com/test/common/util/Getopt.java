
/*************************************************************/
/* Copyright (c) 1984-1996 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id:
 */

/*********************************************************************/
/* Module : Getopt                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.common.util;

import java.io.*;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.DateFormat;

/*********************************************************************/
/*                                                                   */
/* Class Getopt                                                      */
/*                                                                   */
/*********************************************************************/

public class Getopt
{
   /* Inner class used for defining an array of options
   ** the getOpt(GetoptList[]) method
   **
   ** --- sample usage ---
      private static final int OPT_NAME  = 10;
      private static final int OPT_START = 20;
      private static final int OPT_QUERY = 30;
      Getopt.GetoptList[] optArray = 
      {
        new Getopt.GetoptList("name:", OPT_NAME),
        new Getopt.GetoptList("start", OPT_START),
        new Getopt.GetoptList("x",     OPT_START),
	new Getopt.GetoptList("query", OPT_QUERY),
	new Getopt.GetoptList("", 0)
      };
   **
   ** the final int values are completely arbitrary.
   ** this example shows -start and -x are equivalent.
   */
   public static class GetoptList
   {
     public GetoptList(String s, int c)
     {
      optName  = s;
      optConst = c;
     }
     private String optName;
     private int    optConst;
   }



/*********************************************************************/
/* Getopt Constants                                                  */
/*********************************************************************/

public static final boolean DEBUG_TRACE = false;
public static final String cmdChars = "-";	/* add others to this?? */
public static final int NONOPT = -1;
public static final char UNKOPT = '?';
public static final char NEEDS_ARG = ':';
private boolean ignoreCase = true;

/*********************************************************************/
/* Getopt Instance Data                                              */
/*********************************************************************/

private String[] args;
private int index;
private String optarg;

/*********************************************************************/
/* Getopt Constructors                                               */
/*********************************************************************/

public Getopt(String[] args)
    {
    this.args = args;
    index = 0;
    optarg = "";
    }


/**********************************************************************/
/* Getopt Public methods                                              */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getOpt(String optstring)
    {
    int cmdChar;

    if (index >= args.length)
        return NONOPT;

    cmdChar = getCmdChar(index);
    optarg = (cmdChar == NONOPT) ? args[index] : getCmdString(index);
    index++;

    if (!validCmd(cmdChar, optstring))
        cmdChar = UNKOPT;
    else
	{
	if (needsArg(cmdChar, optstring))
	    {
	    if ((index < args.length) && (getCmdChar(index) == NONOPT))
		{
	        optarg = args[index];
	        index++;
	        }
	    else
	    	{
		cmdChar = UNKOPT;
		}
	    }
	}
    return cmdChar;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public void setIgnoreCase( boolean value )
{
    ignoreCase = value;
}

public boolean getIgnoreCase()
{
    return ignoreCase;
}

public int getOpt(GetoptList[] optList)
{
int inx;
int cmdChar;
String cmdName;

    if (index >= args.length)
        return NONOPT;

     cmdName = getCmdName(index);

     if (DEBUG_TRACE)
       System.out.println("getOpt() cmdName = " + cmdName);

     optarg = (cmdName == null) ? args[index] : getCmdString(index);
     index++;

    if ((inx = validCmd(cmdName, optList)) == NONOPT)
        cmdChar = UNKOPT;
    else
	{
	cmdChar = optList[inx].optConst;
	if (needsArg(inx, optList))
	    {
	    if ((index < args.length) && (getCmdChar(index) == NONOPT))
		{
	        optarg = args[index];
	        index++;
	        }
	    else
	    	{
		cmdChar = UNKOPT;
		}
	    }
	}
    return cmdChar;
}


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public String getOptArg()
    {
    return (optarg);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

public int getOptInd()
    {
    return ((index == 0) ? 0 : (index-1));
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

/**********************************************************************/
/* Getopt Private methods                                             */
/**********************************************************************/

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int getCmdChar(int idx)
    {
    char c;
    int ret;

    if ((idx < 0) || (idx >= args.length) || (args[idx].length() < 2))
        ret = NONOPT;

    else
        {
        try
            {
            c = args[idx].charAt(0);
            ret = (cmdChars.indexOf(c) == -1) ? NONOPT : args[idx].charAt(1);
	    }
        catch (StringIndexOutOfBoundsException e)
            {
	    ret = NONOPT;
	    }
	}
    return ret;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private String getCmdName(int idx)
    {
    char c;
    String retStr;

    if ((idx < 0) || (idx >= args.length) || (args[idx].length() < 2))
        retStr = null;

    else
        {
        try
            {
            c = args[idx].charAt(0);
            retStr = (cmdChars.indexOf(c) == -1) ? null : args[idx].substring(1);
	    }
        catch (StringIndexOutOfBoundsException e)
            {
	    retStr = null;
	    }
	}
    return retStr;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private String getCmdString(int idx)
    {
    String ret;

    if ((idx < 0) || (idx >= args.length) || (args[idx].length() < 2))
        ret = "";

    else
        {
        try
            {
            ret = args[idx].substring(1);
	    }
        catch (StringIndexOutOfBoundsException e)
            {
	    ret = "";
	    }
	}
    return ret;
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean validCmd(int cmdChar, String optstring)
    {
    return (cmdChar == NONOPT) ? false : (optstring.indexOf(cmdChar) != -1);
    }

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private int validCmd(String cmdName, GetoptList[] optList)
{
boolean fExactLen;
int i;
int nFound;
int indx;
String nm;
String CompareNm;

     if (DEBUG_TRACE)
       System.out.println("validCmd() cmdName = " + cmdName);

     if (cmdName == null)
       return NONOPT;

     try
     {
      indx = 0;
      nFound = 0;
      i = 0;

      // get option name, stripping off trailing ':' 
      nm = ( optList[i].optName.indexOf((int)NEEDS_ARG) == -1 )
         ? optList[i].optName
	 : optList[i].optName.substring(0, 
	                                optList[i].optName.length()-1);
      while (nm.length() > 0)
      {
        if (DEBUG_TRACE)
          System.out.println(" i =       " + i + 
                            " name =    " + nm + 
			    " optindx = " + optList[i].optConst );

	/*
	**  Create a comparison string this is either a full option
	**  name or a partial one (e.g. -n == -na == -nam == -name)
	*/
        fExactLen = (cmdName.length() == nm.length());
        CompareNm = (cmdName.length() > nm.length())
	          ? nm
		  : nm.substring(0, cmdName.length());

        if (DEBUG_TRACE)
          System.out.println("validCmd() fExactLen = " + fExactLen + " CompareNm = " + CompareNm);

        if ( getIgnoreCase() ? cmdName.equalsIgnoreCase(CompareNm)
                             : cmdName.equals(CompareNm))
        {
          if (DEBUG_TRACE)
            System.out.println("found match " + cmdName + "=" + CompareNm + " at i = " + i);

	  // hold on to this indx into optList, return it to caller
	  indx = i;


	  // if user-entered option matches exactly, we're done
	  // otherwise, count the number of matching options to test for ambiguity.
	  // e.g. -st --> is it -stop or -start ?
	  if (fExactLen)
	  {
	    nFound = 1;
	    break;
	  }
	  else
            ++nFound;

        }
        // get option name, stripping off trailing ':' 
        ++i;
        nm = ( optList[i].optName.indexOf((int)NEEDS_ARG) == -1 )
           ? optList[i].optName
	   : optList[i].optName.substring(0, 
	                                optList[i].optName.length()-1);
      }
     }
     catch(ArrayIndexOutOfBoundsException e)
     {
          if (DEBUG_TRACE)
            System.out.println("optList[] out of bounds exception" + e);
	  return NONOPT;
     }


     /*
     ** If we found zero occurences - unknown option.
     ** If we found more than 1 - ambiguous option.
     */
     if ((nFound == 0) || (nFound > 1))
       indx = NONOPT;

     return indx;
}

/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean needsArg(int cmd, String optstring)
    {
    boolean ret;

    try
        {
        int i = optstring.indexOf(cmd);
	ret = (i == -1) ? false : (optstring.charAt(i+1) == NEEDS_ARG);
	}
    catch (StringIndexOutOfBoundsException e)
        {
	ret = false;
	}

    return ret;
    }


/**********************************************************************/
/*                                                                    */
/**********************************************************************/

private boolean needsArg(int inx, GetoptList[] optList)
    {
boolean ret;
String nm;

    try
        {
        nm = optList[inx].optName;
        int i = nm.indexOf((int)NEEDS_ARG);
	ret = (i == -1) ? false : true;
	}
    catch (StringIndexOutOfBoundsException e)
        {
	ret = false;
	}
    catch (ArrayIndexOutOfBoundsException e)
        {
	ret = false;
	}

    return ret;
    }



/**********************************************************************/
/*                                                                    */
/**********************************************************************/


}  /* end of Getopt */

