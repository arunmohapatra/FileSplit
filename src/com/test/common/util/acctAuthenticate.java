//**************************************************************
//  Copyright (c) 1999-2015 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************




package com.progress.common.util;

import java.net.InetAddress;
import java.util.Random;

import com.progress.chimera.log.SecurityLog;
import com.progress.common.exception.ExceptionMessageAdapter;
import com.progress.common.log.Subsystem;
import com.progress.message.asMsg;
import com.progress.message.jaMsg;
import com.progress.osmetrics.OSMetricsImpl;
import com.progress.system.SystemPlugIn;

/**
 * This class implements methods to verify a user name and password. It does
 * the verification (via JNI)* by accesses the underlying OS account management
 * api's. The user/password must be a valid OS-level account on the system
 * where server is running. correctly a -d startup argument is required. <br>
 * This class expects the system property "Install.Dir"* to be defined. This is
 * done using the java start argument -DInstall.Dir=&lt;dir name&gt;
 * <br> For Fathom
 * compatibility. To keep the external methods used by Fathom the same between
 * 91C and 91D the following 91D methods were changed to be non-static. The
 * change was introduced with service pack 1. verifyUser, authenticateUser,
 * passwdPropmpt, promptForPasswd, promptForUP. bug: 20020703-023.
 */
public class acctAuthenticate implements asMsg
{
   public  static Subsystem         m_securitySubsystem = SecurityLog.get();
   private boolean                  m_noauditsuccauth = false;
   private static boolean           m_traceSSO = false;
   
   /*
   ** authorizeUser - returns "1..." if user/password is valid, and user is
   ** found in a group, returns "0..." otherwize
   */
   public static native String authorizeUser(String user, String password,
                                             String groups);

  /**
   * Do Not use this functions, use authorizeUser instead
   */
   public native boolean verifyUser(String user, String password);
   public native boolean authenticateUser(String user, String password, boolean name_only);


   public static native String validateGroups(String groups);
   /*
   ** passwdPrompt - returns a text string from keyboard input
   */
   public native String passwdPrompt(String promptStr);

   /*
    * Get the account owner of the current process.
    */
   public static native String whoami();

   /*
    * Get a comma separated list of user-groups for the specified account.  If
    * it returns null, the account cannot be found.
    */
   public static native String getUserGroups(String userAccountName);



    /*
     * Jacket methods used to call native code.  These are needed due to
     * use of ClassLoaders which require all JNI access to be
     * called from a common point.  These methods must be used instead
     * of calling the native code directly.
     */

    public static String authorizeUserJNI(String user,
                                          String password,
                                          String groups)
    {
        return authorizeUser(user, password, groups);
    }

    public boolean verifyUserJNI(String user,
                                        String password)
    {
        return verifyUser(user, password);
    }

    public boolean authenticateUserJNI(String user,
                                              String password,
                                              boolean name_only)
    {
        return authenticateUser(user, password, name_only);
    }

    public static String validateGroupsJNI(String groups)
    {
        return validateGroups(groups);
    }

    public String passwdPromptJNI(String promptStr)
    {
        return passwdPrompt(promptStr);
    }

    public static String whoamiJNI()
    {
        return whoami();
   }

    public static String getUserGroupsJNI(String userAccountName)
    {
        return getUserGroups(userAccountName);
    }




    /*
     * one-time class initialization done at load-time.
     */
    static
    {
        String t_str = System.getProperty("tracesso");
        InstallPath iPath = new InstallPath();
        System.load(iPath.fullyQualifyFile("auth.dll"));
        if ( null != t_str ) 
            m_traceSSO = true;
    }

   /*
   ** Function     : validateGroupList
   ** Description  : checkes if all groups exist
   ** Parameters   : comma separated list of groups
   ** Returns      : boolean
   ** History      : created
    */
    public boolean validateGroupList(String groups)
    {
        String s = validateGroups(groups);
        boolean status = parseAndLogAuthInfoStr(s);
        return status;
    }

   /*
   ** Function     : promptForPassword
   ** Description  : prompts for password
   ** Parameters   : user name.
   ** Returns      : String
   ** History      : created
    */
    public String promptForPassword(String username)
    {
        return promptForUP(username)[1];
    }

   /*
   ** Function     : promptForUP
   ** Description  : prompts for password
   ** Parameters   : user name.
   ** Returns      : String
   ** History      : created
    */
    public String[] promptForUP(String user)
    {
        String[]    userPassword = new String[2] ;
        byte[]      buffer       = new byte[50] ;
        String      tmp          = null ;
        String      passWord     = null ;
        String      userName     = null ;
        int         bytes        = 0 ;
        String      prompt ;

        if (user == null)
        {
        	//PROMSG: enter user name:
            prompt = ExceptionMessageAdapter.getMessage (jaMsg.jaMSG017, new Object[0] );
            System.out.print( prompt );
            try
            {
                bytes = System.in.read(buffer) ;
                tmp = new String(buffer) ;
                userName = tmp.trim() ;
                userPassword[0] = userName ;
            }
            catch (Exception e)
            {
            	//PROMSG: Error reading standard input: %s
                System.err.println( ExceptionMessageAdapter.getMessage (jaMsg.jaMSG018, 
                        new Object[] { e.getMessage() } ));
            }
        }
        else
            userPassword[0] = user;

        /**
         * Enter password for user <userName> :
         */
        
		String pwPrompt = ExceptionMessageAdapter.getMessage(jaMsg.jaMSG030, new Object[] { userPassword[0] });
        try
        {
            passWord = passwdPrompt(pwPrompt);
            userPassword[1] = passWord.trim();
        }
        catch (Exception e)
        {
        	//PROMSG: Error reading standard input: %s
            System.err.println( ExceptionMessageAdapter.getMessage (jaMsg.jaMSG018, 
                    new Object[] { e.getMessage() } ));
        }

        return(userPassword);
   }

   /*
   ** Function     : validate
   ** Description  : validates user account
   ** Parameters   : user name, password and group list.
   ** Returns      : String
   ** History      : created
    */
    public boolean validate(String username,
                            String password,
                            String groups)
    {
        if (System.getProperty("os.name").startsWith("Windows 9") &&
            username.equals (System.getProperty ("user.name")))
	     return true;

        boolean generated = isGenerated(password);
        boolean status;

        /**
	     * 1.  auth.c needs "\" for group checking on windows
         * proadsv.bat can handle only "/" so we do the replace
         * 2.  Generator/Decoder maps "\" and "/" to the same number
         * and so should work fine, although it may be a potential
         * problem
         * 3.  I am not sure there is no other issues with "\" and "/"
         * Mark Berkovich 01.08.02
         */
        username = username.replace('/', '\\');

        if (m_traceSSO) {
            if (generated ) {
                    System.out.println("***** Auto-validating " + username + " (" + password + ")");
            } else {
                    System.out.println("***** Validating " + username );
            }
	    }
	    
        if (generated && !validateAutoPassword(username, password))
        {
            status = false;
        }
        else
        {
            String vu = authorizeUser(username, password, groups);
            status = parseAndLogAuthInfoStr(vu);
        }
        return status;
    }

    public String generatePassword(String username)
    {
        //
        // BUG 20020716-033
        // Limit input length to 21 characters when generating a single sign-on
        // password.
        //
        
        String uid = makeAutoPasswordUsername(username);
        if (m_traceSSO) 
            System.out.println("***** Generating auto-password using " + username );

        String psw = String.copyValueOf(clientGeneratePassword(uid.toCharArray()));
        
        if (m_traceSSO) 
            System.out.println("*****    returning auto-password " + psw);

        return psw;
    }

    public void setNoAuditSuccAuth() {m_noauditsuccauth=true;}

    //
    // BUG 20020716-033
    // Limit input length to 21 characters when generating a single sign-on
    // password.
    //
    public String   makeAutoPasswordUsername(String username)
    {
        return((username.length() < 21) ? username : username.substring(username.length() -20));
    }
    
  /*************** Password Generator ****************/
  // TODO: move to separate file,
  // TODO: rename acctAuthenticate.java to AcctAuthenticate.java


private static int BASE = 37;
private int m_rem;
private Random m_random = null;
private boolean DEBUG = true;

private int charToInt(char c)
{
		int i = (int)c;

	/* in numb 48...57, out numb 0...9 */
	if (i>=48 && i<=57)
		return i-48;

	/* in a=97, b=98..., z=122, out 10, 11, ..,35*/
	if (i>=97 && i<=122)
		return i-97+10;

	/* in A=65, b=66..., z=90, out 35, 34, ..,10*/
	if (i>=65 && i<=90)
		return  100-i;

	return 36;
}

char intToChar(int i)
{
		if (i<10)
			return (char)(i+48);

		if (i<=35)
			return (char)(i+97-10);

		return ':';
}

int[] charArrToIntArr(char[] in)
{
	int[] out = new int[in.length];

	for (int ii=0; ii<in.length; ii++)
		out[ii] = charToInt(in[ii]);

	return out;
}

int[] stringToIntArr(String in)
{
	int[] out = new int[in.length()];

	for (int ii=0; ii<in.length(); ii++)
		out[ii] = charToInt(in.charAt(ii));

	return out;
}


char[] intArrToCharArr(int[] in)
{
	char[] out = new char[in.length];

	for (int ii=0; ii<in.length; ii++)
		out[ii] = intToChar(in[ii]);

	return out;
}

/**
  * Removes garbage at the end of array
  */
int[] fixIntArr(int[] in)
{
	int ii = 0;

	while (ii<in.length-1 && in[ii]>=0)
		ii++;

	while (ii>=0 && in[ii]<=0)
		ii--;

	int[] out = new int[ii+1];

	while (ii>=0) {
		out[ii] = in[ii];
		ii--;
	}
	return out;
}

String intArrToString(int[] in)
{
	String out = "";;

	for (int ii=0; ii<in.length; ii++)
		out = out + intToChar(in[ii]);

	return out;
}


int[] mult37DigToDig(int d_1, int d_2)
{
	int d = d_1 * d_2;

	if (d>=BASE) {
		int x = (int)(d/BASE);
		int[] result = {d-BASE*x, x};
		return result;
	}
	else {
		int[] result = {d};
		return result;
	}
}


/*
** Function     : shift
** Description  : multiplies by BASE^i
** Parameters   : integers base37 integer
** Returns      : base37
** History      : created
*/
int[] shift(int[] i_arr, int i)
{
	int size = i_arr.length;
	int[] out = new int[i + size];

	for (int ii=0; ii<i; ii++)
		out[ii] = 0;

	for (int ii=i; ii<i+size; ii++)
		out[ii] = i_arr[ii-i];

	return out;
}

/*
** Function     : addDig
** Description  : adds base37 digits
** Parameters   : 2 integers and pointer to integer
** Returns      : integer
** History      : created
*/
private int addDig(int i_1, int i_2)
{
	int d = i_1+i_2+m_rem;

	if( d>=BASE )
	{
		m_rem = 1;
		return d-BASE;
	}
	m_rem = 0;
	return d;
}

/*
** Function     : subDig
** Description  : subtruct base37 digits
** Parameters   : 2 integers and pointer to integer
** Returns      : integer
** History      : created
*/
private int subDig(int i_1, int i_2)
{
	int d = i_1-i_2-m_rem;

	if( d<0 )
	{
		m_rem = 1;
		return d+BASE;
	}
	m_rem = 0;
	return d;
}

/*
** Function     : add37
** Description  : adds two numbers
** Parameters   : 3 base37 numbers
** Returns      : sum
** History      : created
*/
int add37(int[] i_arr, int[] a_arr, int[] out)
{
	int ii;
	int size_i = i_arr.length;
	int size_a = a_arr.length;

	m_rem = 0;
	out[0] = -1;
	ii=0;
	while(true)
	{
		if (ii>=size_i && ii>=size_a)
			break;

		else if (ii>=size_i)
			out[ii] = addDig(0, a_arr[ii]);

		else if (ii>=size_a)
			out[ii] = addDig(i_arr[ii], 0);

		else
			out[ii] = addDig(i_arr[ii], a_arr[ii]);

		ii++;
	}

	if (m_rem>0)
	{
		out[ii] = m_rem;
		ii++;
	}
	m_rem = 0;
	out[ii] = -1;
	return ii;
}

int[] add37(int[] in1, int[] in2)
{
	int[] temp = new int[40];
	int size = add37(in1, in2, temp);

	int[] out = new int[size];
	for (int ii=0; ii<size; ii++)
		out[ii] = temp[ii];
	return out;
}

/*
** Function     : sub37
** Description  : subtracts base37 numbers
** Parameters   : 3 base37 numbers
** Returns      : value
** History      : created
*/
int[] sub37(int[] i_arr, int[] a_arr)
{
	int ii;
	int size_i = i_arr.length;
	int size_a = a_arr.length;
	int[] temp = new int[size_i+1];

	m_rem = 0;
	temp[0] = -1;
	ii=0;

	while(true)
	{
		if (ii>=size_i && ii>=size_a)
			break;

		else if (ii>=size_i)
			temp[ii] = subDig(0, a_arr[ii]);

		else if (ii>=size_a)
			temp[ii] = subDig(i_arr[ii], 0);

		else
			temp[ii] = subDig(i_arr[ii], a_arr[ii]);

		ii++;
	}

	if (m_rem>0)
	{
		temp[ii] = m_rem;
		ii++;
	}
	m_rem = 0;

	return fixIntArr(temp);
}

/*
** Function     : mult37IntArrs
** Description  : multiplies base37 numbers
** Parameters   : 3 base37 numbers
** Returns      : product
** History      : created
*/
int[] mult37IntArrs(int[] i_1, int[] i_2)
{
	int[] result = new int[0];
	int curr[];
	int n_1 = i_1.length;
	int n_2 = i_2.length;

	for (int ii=0; ii<n_1; ii++)
	{
		for (int jj=0; jj<n_2; jj++)
		{
			curr = mult37DigToDig(i_1[ii], i_2[jj]);
 			curr = shift(curr, ii+jj);
			result = add37(result, curr);
		}
	}
	return fixIntArr(result);
}


/*
** Function     : getValue
** Description  : computes value of quadratic expression
** Parameters   : time, user, host, random seed, out
** Returns      : the value of the expression
** History      : created
*/
int[] getValue(int[] time, int[] UserName, int[] HostId, int[] Rand)
{
	int f1[] = add37(UserName, add37(time, Rand));
	int f2[] = add37(HostId, add37(time, Rand));
	int[] out = mult37IntArrs(f1, f2);

	return out;
}

int[] generatePassword(int[] time, int[] UserName, int[] HostId, int[] Rand)
{
	int[] password1 = getValue(time, UserName, HostId, Rand);
	int[] password = add37(password1, Rand);

	return password;
}


/*
** Function     : med37
** Description  : computes (up+lw)/2 in base37 numbers
** Parameters   : two 37 numbers.
** Returns      : the median
** History      : created
*/
int[] med37( int[] up, int[] lw)
{
	int size;
	int[] med = new int[20];

	int[] sum = add37(up, lw);
	size = sum.length;

	m_rem = 0;

	for (int ii=size-1; ii>=0; ii--)
	{
		int m = (sum[ii] + m_rem*BASE)/2;
		med[ii] = m;
		if (2*m != sum[ii] + m_rem * BASE)
			m_rem = 1;
		else
			m_rem = 0;
	}

	if (med[size-1] == 0)
		med[size-1] = -1;
	else
		med[size] = -1;

	return fixIntArr(med);
}


/*
** Function     : compare37
** Description  : compares base37 numbers
** Parameters   : two 37 numbers.
** Returns      : 0 if equal, 1 or more if x>y, -1 or less if x<y
** History      : created
*/
int compare37(int[] x, int[] y)
{
	int size_x = x.length;
	int size_y = y.length;
	int ii;

	if (size_x != size_y)
		return size_x - size_y;

	for (ii=size_x-1; ii>=0; ii--)
	{
		if (x[ii] != y[ii])
			return x[ii] - y[ii];
	}
	return 0;
}

/*
** Function     : decode
** Description  : decodes password by solving a quadratic equation
**                in whole numbers
** Parameters   : password, username,hostid, out_time
** Returns      : out_time
** History      : created
*/
int[] decode(int[] password, int[] UserName, int[] HostId)
{
	int[] p;
	int[] Rand = {};
	int ii;
	int size = (int)((password.length+1)/2);
	int[] up = new int[size];
	int[] lw = new int[0];
	int dbg_count = 0;

	for (ii=0; ii<size; ii++)
		up[ii] = 36;

	int[] time = med37( up, lw);

	while(compare37(time, lw) != 0)
	{
		p = getValue(time, UserName, HostId, Rand);

		if (dbg_count++>105)
		{
			System.out.println("FATAL ERROR in decode, dbg_count++>105");
			return null;
		}

		if (compare37(p, password) > 0) /* v > password) */
			up = time;
		else
			lw = time;

		time = med37( up, lw);
	}

	p = getValue(time, UserName, HostId, Rand);
       	int[] r = sub37(password, p);
	int[] out_time = sub37(time, r);

	return out_time;
}

/*
** Function     : addPattern
** Description  : adds pattern to mark generated password
** Parameters   : int array
** Returns      : NONE
** History      : created
*/
int[] addPattern(int[] p)
{
	int size = p.length;
	int[] out = new int[size + 4];

	for (int ii=0; ii<size; ii++)
		out[ii] = p[ii];

	for (int ii=0; ii<4; ii++)
	{
		int i = p[ii] + ii*(ii+2) + 3;
		out[size+ii] = (i > 36) ? i - 36 : i;
	}

	return out;
}

/*
** Function     : removePattern
** Description  : removes pattern which is used to mark generated password
** Parameters   : int array
** Returns      : NONE
** History      : created
*/
int[] removePattern(int[] p)
{
	int size = p.length;

	if (!isGeneratedInt(p))
	  return p; //What the ????

	p[size-4] = -1;

	return fixIntArr(p);
}

/*
** Function     : isGenerated
** Description  : checks if Password is generated
** Parameters   : char array
** Returns      : TRUE or FALSE
** History      : created
*/
boolean isGenerated(char[] cp)
{
	int p[] = charArrToIntArr(cp);

	return isGeneratedInt(p);
}

boolean isGenerated(String in_s)
{
    if (in_s.length() < 8)
        return false;

    return isGenerated(in_s.toCharArray());
}

/*
** Function     : isGeneratedInt
** Description  : checks if Password is generated
** Parameters   : int array, of length > 8
** Returns      : TRUE or FALSE
** History      : created
*/
boolean isGeneratedInt(int[] p)
{
	int size = p.length;

	for (int ii=0; ii<4; ii++)
	{
		int i = p[ii] + ii*(ii+2) + 3;

		if (p[size-4+ii] != ((i > 36) ? i - 36 : i))
			return false;
	}
	return true;
}


/*
** Function     : getTime
** Description  : gets time, puts it into base37 int array and reverce the array
** Parameters   : base37 integer
** Returns      : time
** History      : created
*/
int[] getTime()
{

	long time = System.currentTimeMillis();
	String s = (new Long(time)).toString();
	int size = s.length();

	int[] tr = stringToIntArr(s);

	int[] out = new int[size];

	for (int ii = 0; ii < size; ii++)
		out[ii]= tr[size-ii-1];

	return out;
}

int[] getRandom(int size)
{
	if (m_random == null)
		m_random = new Random();

	int[] out = new int[size];
	for (int ii=0; ii<size; ii++)
	{
	    int r = m_random.nextInt();
            if (r<0)
                r = -r;
            out[ii] = r - ((int)(r/BASE))*BASE;
        }
	return out;
}

/*
** Function     : clientGeneratePassword
** Description  : generates Password
** Parameters   : 3 strings user, random seed and Password
** Returns      : Password
** History      : created
*/
char[] clientGeneratePassword(char[] UserName)
{
	int		i_user_name[] = charArrToIntArr(UserName);
	int[]	host = getHostName();
	int[]	rand = getRandom(11);
	int[]	i_time = getTime();

	int[] password37 = generatePassword(i_time, i_user_name, host, rand);

	password37 = addPattern(password37);

	char[] Password = intArrToCharArr(password37);

    if (m_traceSSO) 
        System.out.println("*****    client generated password: " + new String(Password));

	return Password;
}


/*
** Function     : decodeTime
** Description  : decodes time out of the Password
** Parameters   : 2 strings Password, user_name and decoded_time
** Returns      : time
** History      : created
*/

long decodeTime(char[] Password, char[] user_name)
{
	int[]	pasw = charArrToIntArr(Password);
	int[]	user = charArrToIntArr(user_name);
	int[] host = getHostName();

	pasw = removePattern(pasw);

	int[]  rev_time = decode(pasw, user, host);

	int size = rev_time.length;
	int[]  time = new int[size];
	for (int ii = 0; ii < size; ii++)
		time[ii]=rev_time[size-ii-1];

	long  l = 0;
	try {
		l = Long.parseLong(new String(intArrToString(time)));
	} catch (NumberFormatException e) {
        if (m_traceSSO) 
            System.out.println("*****    decode time erorr: " + e.getMessage());
		 m_securitySubsystem.log (0, asMSG064, "" + Password+user_name+host );
	}

	return l;
}

private int[] getHostName()
{
	InetAddress     ia = null;
    InetAddress[]   inetAddrArray = null;
    
	try {
	    /* Get all the DNS names for this host.  If it is gt 1, then
	    ** always use the zero'th entry */
		ia = InetAddress.getLocalHost();
        inetAddrArray = InetAddress.getAllByName(ia.getHostName()); 
        if (1 < inetAddrArray.length)
            ia = inetAddrArray[0];
            
	} catch (Exception e) {
	
        if (m_traceSSO) 
            System.out.println("*****    get host name erorr: " + e.getMessage());

		return null;
	}

	    /* Get the full DNS name of this system */
        String host_name = ia.getHostName();
        
        if (m_traceSSO) 
            System.out.println("*****    local host name: " + host_name);

        /* We are only going to use the system's name, and not the full
        ** DNS name so that we can shorten the sso credentials size */
        int i = host_name.indexOf(".");
        if (i > 0)
            host_name = host_name.substring(0, i);

        if (m_traceSSO) 
            System.out.println("*****    resolved local host name: " + host_name);
            
	return charArrToIntArr(host_name.toCharArray());
}

/*
** Function     : getTimeout
** Description  : timeout for generated password im milliseconds
** Parameters   : NONE
** Returns      : system property"pwdtimeout" or 90000
** History      : created
*/
private int getTimeout()
{
    String t_str = System.getProperty("pwdtimeout");
    if (t_str != null)
    try {
        return Integer.parseInt(t_str);
    } catch (NumberFormatException e) { }

    return 90000;
}

/**
 * Function     : validateAutoPassword
 * Description  : checks if time encoded in password is within +/- 
 *                <code>getTimeout()</code> from the current time;
 * @param userName char array of user name
 * @param passwd char array of password
 * @return boolean - true if valid, false otherwise
 * History      : created
 */
boolean validateAutoPassword(char[] userName, char[] passwd)
{
    String time_log = null;
    long timeBeforeDecode = System.currentTimeMillis();
    long decoded_time = decodeTime(passwd, userName);
    long current_time = System.currentTimeMillis();
    long time = current_time - timeBeforeDecode;

    if (m_traceSSO) {
        System.out.println("*****    using decode Time: " + Long.toString(decoded_time));
        System.out.println("*****    using current Time: " + Long.toString(current_time));
        System.out.println("*****    using delta Time (current - decode): " + Long.toString(time));
    }
	
    // + 20060227-018, HP-UX Java time can drift away from system time.
    // After the adminServer has been running a few weeks the logins fail.
    // If the password is out of date using the time obtained from Java
    // double check it using the time from the C RTL. Accept the C RTL time 
    // if it indicates the password is still valid.
    if (time < -getTimeout() || time > getTimeout())
    {
        SystemPlugIn systemPlugIn = null;
        OSMetricsImpl osmetricsImpl = null;
        if ((systemPlugIn = SystemPlugIn.get()) != null)
        {
            if ((osmetricsImpl = systemPlugIn.getOSMetrics()) != null &&
                osmetricsImpl.isInitialized())
            {
                long systemTime = osmetricsImpl.getCurrentTime() * 1000L;
                long diffTime = systemTime - decoded_time;
                if (diffTime >= -getTimeout() && diffTime <= getTimeout())
                {
                    time = diffTime;
                }
            }
        }
    } // - 20060227-018

    
    if (m_traceSSO) {
        System.out.println("*****    check of delta time < : " + Long.toString(-getTimeout()));
        System.out.println("*****    check of delta time > : " + Long.toString(getTimeout()));
    }
    
    if(time < -getTimeout())
    {
        time_log = "<-" + getTimeout()/1000;

        if (m_traceSSO)
            System.out.println("*****    passed < password check with: " + time_log);
    }
    else if (time > getTimeout())
    {
        time_log = (new Long(time/1000)).toString();

        if (m_traceSSO)
            System.out.println("*****    passed < password check with: " + time_log);
    }

    if (time_log != null)
    {
        String log_str = String.valueOf(userName) + ":" + time_log + ":";
        m_securitySubsystem.log (2, asMSG063, log_str);

        if (m_traceSSO)
            System.out.println("*****    failed password check");
            
        return false;
    }
    else
    {
        if (m_traceSSO)
            System.out.println("*****    passed password check");
            
        return true;
    }
}

boolean validateAutoPassword(String userName, String passwd)
{
    //
    // BUG 20020716-033
    // Limit input length to 21 characters when generating a single sign-on
    // password.
    //
    String uid = makeAutoPasswordUsername(userName);
    

        if (m_traceSSO)
            System.out.println("*****    using auto-username " + userName );
	
    return  validateAutoPassword(uid.toCharArray(), passwd.toCharArray());
}

/**
  * Code to convert message from auth.c to a message
  */
  private static long convert(String in)
  {
      long[] ml_msgs =   { asMSG052 , asMSG053 , asMSG054 , asMSG055 ,
                           asMSG056 , asMSG057 , asMSG058 , asMSG059 ,
                           asMSG060 , asMSG061 , asMSG062 , asMSG063 ,
                           asMSG064 , asMSG065};

      String[] ms_msgs = {"asMSG052","asMSG053","asMSG054","asMSG055",
                          "asMSG056","asMSG057","asMSG058","asMSG059",
                          "asMSG060","asMSG061","asMSG062","asMSG063",
                          "asMSG064","asMSG065"};

    for (int ii=0; ii<ms_msgs.length; ii++)
    {
        if (in.equals(ms_msgs[ii]))
            return ml_msgs[ii];
    }

    return ml_msgs[0]; //What the ??
  }

  private boolean parseAndLogAuthInfoStr(String vu)
  {
    int level;
    try {
        level = Integer.parseInt(vu.substring(0, 1));
    } catch (Exception e) {
        level = 0;
    }

    if (m_noauditsuccauth && level == 3)
        return true;

    if (vu.length()<11)
    {
        m_securitySubsystem.log (0, asMSG060, "LOGGING ERROR");
        return (level == 3);
    }


    String msg = vu.substring(1, 9);
    long msgn = convert(msg);

    String auth_str = vu.substring(10);

    /**
     * We assume that empty fields do not cary any
     * important information. If it is needed,
     * return "no user" from auth.c.
     */
    while (auth_str.startsWith(":"))
        auth_str = auth_str.substring(1);

    m_securitySubsystem.log (level, msgn, auth_str);

    return (level == 3);
  }

}




























