/*
/* <p>Copyright 2000-2001 Progress Software Corportation, All rights reserved.</p>
/* <br>
/* <p>Class:        Password    </p>
/* <br>
/* All rights reserved.  No part of this program or document
/* may be  reproduced in  any form  or by  any means without
/* permission  in  writing  from  Data Language Corporation.
/*
*/

package com.progress.common.util;

import  java.lang.*;
import  java.util.Random;
import  com.progress.common.util.Base64;

/**
 * The Password class provides the ability to securely store a password (or
 * any other String based value) in an obfuscated form.
 */
public class PasswordString extends Object
{
    /*
     * CLASS Constants
     * private static final <type>  <name> = <value>;
     */

    /*
     * CLASS Properties.
     * public static        <type>  <name> = <value>;
     */

    /*
     * Super Object Properties.
     *  protected       <type>          <name> = <value>;
     */

    /*
     * Object Instance Properties.
     *  private         <type>          <name> = <value>;
     */
    private                     byte[]          m_data = null;
    private                     byte[]          m_key = null;
    private                     long            m_seed = System.currentTimeMillis();
    private                     Random          m_generator = new Random(m_seed);

    /*
     * Constructors...
     */

    /**
     * <!-- PasswordString() -->
     * <p>The default class constructor.  It will create and use a unique
     * encryption key to obscure the data.
     * </p>
     * <br>
     */
    public PasswordString()
    {
    }

    /**
     * <!-- PasswordString() -->
     * <p>Constuctor that sets the encryption key to a fixed value.
     * </p>
     * @param key is a long integer that is the encryption key seed.  (Not the
     * encryption key itself)
     * <br>
     */
    public PasswordString(long key)
    {
    }

    /**
     * <!-- PasswordString() -->
     * <p>Create a copy of the data.   The data will be encrypted under a
     * new key.
     * </p>
     * @param copy is a reference to an existing Password object to copy the
     * data from
     * <br>
     */
    public PasswordString(PasswordString copy)
    {
        if (null != copy)
        {
            try
            {
                // copy if it has data.
                if (0 < copy.length())
                {
                    setValue(copy.toString());
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * <!-- PasswordString() -->
     * <p>The constructor that stores an obfuscated String value.  The value
     * will be encrytped under a unique encryption key.
     * </p>
     * @param src is a reference to an existing String object that contains
     * raw (unencrypted) password characters
     * <br>
     */
    public PasswordString(String src)
    {
        try
        {
            setValue(src);
        }
        catch (Exception e)
        {
        }
    }

    /*
     * Final cleanup.
     */
    protected void finalize() throws Throwable
    {
        empty();
        // Destroy the seed.
        m_seed = 0;
        // Destroy the key bytes.
        if (null != m_key)
        {
            for (int i = 0; i < m_key.length; i++)
            {
                m_key[i] = 0;
            }
        }
    }

    /*
     * ACCESSOR METHODS:
     */

    /**
     * <!-- setValue() -->
     * <p>Obfuscate and store the specified string value.
     * </p>
     * <br>
     * @param src is the String value to obfuscate and store
     * <br>
     * @exception   NullPointerException
     */
    public void setValue(String src) throws NullPointerException
    {
        if (null == src)
        {
            throw new NullPointerException("Null required argument");
        }

        // Wipe out anything that existed.
        //
        empty();

        int srcLength = src.length();
        if (0 < srcLength)
        {
            // Create key storage.
            m_key = new byte[srcLength];

            // Generate a new key
            //
            m_generator.nextBytes(m_key);

            // Load the StringBuffer object
            //
            m_data = src.getBytes();

            // Flip the bits.
            //
            flipBits();
        }
    }


    /*
     * PUBLIC METHODS:
     */

    /**
     * <!-- length() -->
     * <p>
     * </p>
     * <br>
     * @param
     * <br>
     * @return  int
     * <br>
     * @exception   Exception
     */
    public  int length() throws Exception
    {
        return((null == m_data) ? 0 : m_data.length);
    }

    /**
     * <!-- empty() -->
     * <p>
     * </p>
     * <br>
     * @param
     * <br>
     * @return  void
     */
    public void empty()
    {
        // Erase memory.
        if (null != m_data)
        {
            int len = m_data.length;
            if (0 < len)
            {
                int i = 0;
                for (; i < len; i++)
                {
                    m_data[i] = 0;
                }

                // Empty the StringBuffer object.
                //
                m_data = null;
            }
        }

    }

    /**
     * <!-- toString() -->
     * <p>Dump the clear-text value.
     * </p>
     * <br>
     * @return  String
     */
    public  String toString()
    {
        return(getValue());
    }

    /**
     * <!-- exportPassword() -->
     * <p>Export an encrypted password in String format.
     * </p>
     * <br>
     * @return  String
     * <br>
     * @exception   Exception
     */
    public  String exportPassword() throws Exception
    {
        String                  returnValue = null;
        StringBuffer            tmp = new StringBuffer();

        tmp.append(Long.toHexString(m_seed).toUpperCase());
        tmp.append("=");

        if (null != m_data && 0 < m_data.length)
        {
            tmp.append(Base64.encode(m_data));
        }
        returnValue = new String(tmp.toString());

        return(returnValue);
    }
    /**
     * <!-- importPassword() -->
     * <p>Import an encrypted password and store it internally.
     * </p>
     * <br>
     * @param inPwd is a String holding an encrpyted password
     * <br>
     * @return  void
     * <br>
     * @exception   Exception
     */
    public void importPassword(String inPwd) throws Exception
    {
        if (null == inPwd)
        {
            throw new NullPointerException("input required");
        }

        int         offset = inPwd.indexOf("=");

        if (-1 == offset)
        {
            // This is illegal
            throw new Exception("Invalid encoded value.");
        }
        else
        {
            empty();

            // Extract and reconstruct the seed.
            m_seed = Long.parseLong(inPwd.substring(0, offset++), 16);
            // New random generator
            m_generator = new Random(m_seed);
            if (offset < inPwd.length())
            {
                m_data = Base64.decode(inPwd.substring(offset));
                // Create key storage.
                m_key = new byte[m_data.length];
                // Re-generate the key
                m_generator.nextBytes(m_key);
            }
        }
    }

    /**
     * <!-- test() -->
     * <p>Test the password for a string match.
     * </p>
     * <br>
     * @param testPassword is a String object holding the password string to
     * match
     * <br>
     * @return  boolean
     */
    public  boolean test(String testPassword)
    {
        boolean     returnValue = false;

        if (null != testPassword)
        {
            if (null != m_data)
            {
                if (0 < m_data.length)
                {
                    byte[] testData = testPassword.getBytes();
                    if (m_data.length == testData.length)
                    {
                        // Right lenght, now test the bytes.

                        // run the new stuff through the key.
                        for (int index = 0; index < testData.length; index++)
                        {
                            testData[index] = (byte)(((int)testData[index]) ^ ((int)m_key[index]));
                        }

                        returnValue = true;

                        // test the bytes one at a time.  The first mismatch
                        // looses.
                        for (int index = 0; index < testData.length; index++)
                        {
                            if (testData[index] != m_data[index])
                            {
                                returnValue = false;
                                break;
                            }
                        }

                        // erase temp store.
                        for (int index = 0; index < testData.length; index++)
                        {
                            testData[index] = 0;
                        }
                    }
                }
                else
                {
                    // zero length data is a blank password, so a blank string matches.
                    //
                    if (0 == testPassword.length())
                    {
                        returnValue = true;
                    }
                }
            }
            else
            {
                // null data is a blank password, so a blank string matches.
                //
                if (0 == testPassword.length())
                {
                    returnValue = true;
                }
            }
        }

        return(returnValue);
    }

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * <!-- getValue() -->
     * <p>Retreive an clear text copy of the obfuscated value.
     * </p>
     * <br>
     * @return  String
     */
    protected  String getValue()
    {
        String      returnValue = null;


        if (null != m_data && 0 < m_data.length)
        {
            try
            {
                // Run a loop xor'ing all the bytes to get clear text.
                //
                flipBits();

                // Make a clear text copy.
                //
                returnValue = new String(m_data);

                // Now obfuscate the value again.
                //
                flipBits();
            }
            catch (Exception e)
            {
                // Return an empty string.
                returnValue = new String("");
            }
        }
        else
        {
            // Return an empty string.
            //
            returnValue = new String("");
        }

        return(returnValue);
    }

    /*
     * <!-- flipBits() -->
     * <p>
     * </p>
     * <br>
     * @param
     * <br>
     * @return  void
     */
    protected void flipBits()
    {
        int len = m_data.length;

        if (0 < len)
        {
            for (int index = 0; index < len; index++)
            {
                m_data[index] = (byte)(((int)m_data[index]) ^ ((int)m_key[index]));
            }
        }
    }

    /*
     * PRIVATE METHODS:
     */

}