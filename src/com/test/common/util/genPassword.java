
/*************************************************************/
/* Copyright (c) 1984-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id:
 */


package com.progress.common.util;

import java.lang.*;
import java.util.*;
import com.progress.common.util.*;

public class genPassword
{
      private static final int OPT_ENCRYPT = 10;
      private static final int OPT_VERIFY  = 20;
      private static final int OPT_HELP    = 30;
      private static final int UNKOPT      = (int) '?';

   public static void main(String[] args)
   { 

      Getopt.GetoptList[] optArray = 
      {
        new Getopt.GetoptList("password:", OPT_ENCRYPT),
        new Getopt.GetoptList("verify:", OPT_VERIFY),
        new Getopt.GetoptList("help",  OPT_HELP),

        new Getopt.GetoptList("", 0)
      };

      if (args.length == 0)
      {
        usage();
      }  

      // Parse any options passed in to plugin
      Getopt options = new Getopt(args);

      int option;
      String en_passwd = null;
      String de_passwd = null;
      boolean doVerify = false;
      
      while ((option = options.getOpt(optArray)) != Getopt.NONOPT)
      {
        switch(option)
        {
          case OPT_ENCRYPT:
               en_passwd = options.getOptArg();
           break;

          case OPT_VERIFY:
               de_passwd = options.getOptArg();
               doVerify = true;
           break;

          case OPT_HELP:
               usage();
           break;

          case UNKOPT:
               usage();
           break;
        }
      }
      if (!doVerify)
      {
        System.out.println( (new crypto()).encrypt(en_passwd) );
      }
      else if ( (en_passwd != null) && (de_passwd != null) )
      {
        if ( (new crypto()).encrypt(en_passwd).equals(de_passwd) )
        {
          System.out.println("The passwords match.");
        }
        else
        {
          System.out.println("The passwords do not match.");  
        }  
      }
      else
      {
        usage();  
      }
   }

   private static void usage()
   {
      System.out.println("usage: genpassword -password <text>");
      System.out.println("    or genpassword -password <text> -verify <encrypted password>");
      System.exit(1);
   }
}
 
