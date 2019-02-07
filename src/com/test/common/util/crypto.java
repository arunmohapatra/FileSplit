
/*************************************************************/
/* Copyright (c) 1984-2012 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id:
 */

package com.progress.common.util;



public class crypto
{
	public static final String WEBSPEED_NAME = "PROGRESS";
	public static final boolean DEBUG_TRACE = false;
	
	/**
	 * optional string prefix used to identify encoded strings 
	 */
	public static final String PREFIX = "oech1::";



    /*
    * Turns array of bytes into a string representing each byte as
    * unsigned hex number. The returned string length will always
    * be even. The asBytes() method undoes this.
    */
    private String asHex (byte hash[])
    {
      StringBuffer buf = new StringBuffer(hash.length * 2);
      int i;

      for (i = 0; i < hash.length; i++)
      {
        if (((int) hash[i] & 0xff) < 0x10)
          buf.append("0");

        buf.append(Long.toString((int) hash[i] & 0xff, 16));
      }
      return buf.toString();
    }



    /*
    * Turns array of bytes into a plain ol' String
    */
    private String asStr(byte str[])
    {
        return(new String(str));
    }

    /*
    * Turns a String, represented as a series of hex digit pairs,
    * into an array of bytes. This does the reverse of asHex().
    */
    private byte[] asBytes(String str)
    {
      int i;
      int j;
      int ct;
      int str_len = str.length();
      byte src_bytes[];

       /*
       ** Check to be sure str_len is even.
       */

       if ((str_len & 1) == 0)
         src_bytes = new byte[str_len/2];
       else
         src_bytes = new byte[str_len+1/2];

       for (i = 0, j = 0; i < str_len; i += 2, ++j)
       {
          short val = Short.parseShort(str.substring(i, i+2), 16);
          src_bytes[j] = (byte)(val & 0xFF);
       }
       if (DEBUG_TRACE)
         for (ct = 0; ct < src_bytes.length; ++ct)
            System.out.println("in asBytes() src_bytes " + src_bytes[ct]);

      return src_bytes;
    }

    private byte[] do_crypto(String Key, byte Src[])
    {
     int ct;
     int key_ct;
     byte dest_bytes[];
     byte key_bytes[];
     int key_len;
     int src_len;

      key_bytes = Key.getBytes();
      key_len = key_bytes.length;
      src_len = Src.length;
      dest_bytes = new byte[src_len];

      if (DEBUG_TRACE)
        for (ct = 0; ct < Src.length; ++ct)
           System.out.println(" Src " + Src[ct]);

      for (ct = 0, key_ct = 0; ct < src_len; ++ct, ++key_ct)
      {
        if (key_ct >= key_len)
          key_ct = 0;

	if (DEBUG_TRACE)
          System.out.println(" ct =      " + ct +
                             " key_ct =  " + key_ct +
			     " src_len = " + src_len +
			     " key_len = " + key_len);

        dest_bytes[ct] = (byte) (Src[ct] ^ key_bytes[key_ct]);
      }

      if (DEBUG_TRACE)
        for (ct = 0; ct < dest_bytes.length; ++ct)
           System.out.println(" dest_bytes " + dest_bytes[ct]);

      return (dest_bytes);
    }

    /*
    ** Function	: decrypt
    ** Description	: decodes the specified string
    ** Parameters	: string to decrypt. This must be
    **                    in the hex string format created by
    **                    encrypt.  It may optionally begin with oech1:: prefix
    ** Returns	: decrypted string
    */
    public String decrypt(String str)
    {
      if ((str == null) || (str.length() == 0))
    	  return str;
      
      if (str.startsWith(PREFIX)) {
    	  String val = str.substring(crypto.PREFIX.length());
    	  return decryptOECH1(val);
      }
      
      return decryptOECH1(str);
    }
    
    /*
    ** Function	: decrypt
    ** Description	: decodes the specified string
    ** Parameters	: string to decrypt. This must be
    **                    in the hex string format created by
    **                    encrypt.
    ** Returns	: decrypted string
    */    
	private String decryptOECH1(String str) {
		if ((str == null) || (str.length() == 0))
			return str;

		return asStr(do_crypto(WEBSPEED_NAME, asBytes(str)));
	}

    /*
    ** Function	: encrypt
    ** Description	: encodes the specified string
    ** Parameters	: string to encrypt
    ** Returns	: encrypted string. Note this string is a series
    **            of hex digit pairs.  This allows for storing this
    **            value in a text file.
    */
    public String encrypt(String str)
    {
      if ((str == null) || (str.length() == 0))
	return str;
      return asHex(do_crypto(WEBSPEED_NAME, str.getBytes()));
    }

/*
    public static void main(String[] args)
    {
        String NAME = "NT-AUTORITÄT\\SYSTEM";
        String NAME1 = "Ä";
        String NAME2 = "ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜáíóúñÑ";

        try
        {
            crypto c = new crypto();

            // Test single character
            String en = c.encrypt(NAME1);
            String n = c.decrypt(en);
            if (n.equals(NAME1))
               System.out.print("equal  ");
            System.out.println(NAME1 + " " + en + " " + n);

            // Test original string from bug 20020719-013
            en = c.encrypt(NAME);
            n = c.decrypt(en);
            if (n.equals(NAME))
               System.out.print("equal  ");
            System.out.println(NAME + " " + en + " " + n);

            // Test string of "european" letters
            en = c.encrypt(NAME2);
            n = c.decrypt(en);
            if (n.equals(NAME2))
               System.out.print("equal  ");
            System.out.println(NAME2 + " " + en + " " + n);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
*/
}


