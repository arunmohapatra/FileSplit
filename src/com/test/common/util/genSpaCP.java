/*
** <p>Copyright (c) 2013 Progress Software Corporation, All rights reserved.</p>
** <br>
** <p>Class:      genSpaCP:    </p>
** <br>
** All rights reserved.  No part of this program or document
** may be  reproduced in  any form  or by  any means without
** permission  in  writing  from  Progress Software Corporation.
**
**
*/
package com.progress.common.util;

import com.progress.auth.ClientPrincipal;
import com.progress.auth.ClientPrincipalException;
import com.progress.auth.IClientPrincipalDefs;
import com.progress.common.util.*;

public class genSpaCP 
{

    private static final int OPT_ENCRYPT = 10;
    private static final int OPT_ROLE  = 20;
    private static final int OPT_DOMAIN  = 30;
    private static final int OPT_FILE  = 40;
    private static final int OPT_HELP    = 50;
    private static final int OPT_USER = 60;
    private static final int UNKOPT      = (int) '?';
    
    private static final String DEFAULT_ROLE = "SPAClient";
    private static final String DEFAULT_DOMAIN = "OESPA";
    private static final String DEFAULT_FILE = "oespaclient.cp";
    private static final String DEFAULT_USER = "BPSServer";

    /**
	 * @param args
	 */
	public static void main(String[] args) 
	{

		System.out.println("genspacp 1.0");
		Getopt.GetoptList[] optArray = 
		{
			new Getopt.GetoptList("password:", OPT_ENCRYPT),
			new Getopt.GetoptList("role:", OPT_ROLE),
			new Getopt.GetoptList("domain:", OPT_DOMAIN),
			new Getopt.GetoptList("file:", OPT_FILE),
			new Getopt.GetoptList("user:", OPT_USER),
			new Getopt.GetoptList("help",  OPT_HELP),

			new Getopt.GetoptList("", 0)
		};

		if (args.length == 0)
		{
			usage();
		}  

		// Parse any options passed in
		Getopt options = new Getopt(args);

		int option;
		String opt_passwd = null;
		String en_passwd = null;
		String role = null;
		String guid = null;
		String user = null;
		String domain = null;
		String filen = null;
	      
		while ((option = options.getOpt(optArray)) != Getopt.NONOPT)
		{
			switch(option)
			{
				case OPT_ENCRYPT:
					opt_passwd = options.getOptArg();
					break;

				case OPT_ROLE:
					role = options.getOptArg();
					break;

				case OPT_DOMAIN:
					domain = options.getOptArg();
					break;

				case OPT_FILE:
					filen = options.getOptArg();
					break;

				case OPT_USER:
					user = options.getOptArg();
					break;

				case OPT_HELP:
					usage();
					break;

				case UNKOPT:
					usage();
					break;
			}
		}
		
		if (null == opt_passwd)
		{
			System.out.println("No password specified");
			usage();
		}
		
		if (null == role)
			role = DEFAULT_ROLE;
		
		if (null == domain)
			domain = DEFAULT_DOMAIN;
		
		if (null == filen)
			filen = DEFAULT_FILE;
		
        if (null == user)
            user = DEFAULT_USER;

		guid = new CompactUUID().toString();
		en_passwd = "oech1::" + (new crypto()).encrypt(opt_passwd);
        if ( 0 < domain.length() &&
             -1 == user.indexOf('@') )
        {
            user = user + "@" + domain;
        }
		
		ClientPrincipal cp = null;
		try 
		{
				cp = new ClientPrincipal(user, guid, null, "");
				cp.setRoles(role);
				cp.seal(opt_passwd);
				byte[] b = cp.exportPrincipal();
				ClientPrincipal.writeBinaryFile(b, filen);
		}
		catch (ClientPrincipalException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		byte[] b = ClientPrincipal.loadBinaryFile(filen);
		try 
		{
			cp = new ClientPrincipal(b);
		} 
		catch (ClientPrincipalException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Generated sealed Client Principal...");
		System.out.println("    User: " + cp.getQualifiedUserID());
		System.out.println("    Id: " + cp.getSessionID());
		System.out.println("    Role: " + cp.getRoles());
		System.out.println("    Encoded Password: " + en_passwd);
		System.out.println("    File: " + filen);
		System.out.println("    State: " + cp.getStateDetail());
		try
		{
			cp.validateSeal(opt_passwd);
			System.out.println("    Seal is valid");
		}
		catch(ClientPrincipalException e)
		{
			System.out.println("    Seal is not valid!");
			
		}
	}

   private static void usage()
   {
      System.out.println("usage: genspacp -password <text> [-user <text> -role <text> -domain <text> -file <text>]");
      System.exit(1);
   }
}
