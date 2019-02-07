/*

 *

 * Copyright 2000-2001 Progress Software Corportation, All rights reserved.

 *

 * <p>Class:       Base64</p>

 */



package com.progress.ubroker.util;



/**

 * The Base64 class provides base 64 encoding and decoding operations per the

 * RFC 1421 specification.  It is used generally for transport of binary data

 * between dissimilar hardware platforms or through text only transports.

 */

public class Base64

{



     private static final char [ ] b2c = {

       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0 to 7

       'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 8 to 15

       'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 16 to 23

       'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 24 to 31

       'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 32 to 39

       'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 40 to 47

       'w', 'x', 'y', 'z', '0', '1', '2', '3', // 48 to 55

       '4', '5', '6', '7', '8', '9', '+', '/' }; // 56 to 63



    static final char pad = '=';

    static byte[] c2b = null;



    /**

     * <!-- Base64() -->

     * <p>The class default constructor.

     * </p>

     * <br>

     * <p>Parameters:</p>

     * <UL>

     * </UL>

     * <br>

     * <p>Returns:  void

     * <br>

     * <p>Exceptions:</p>

     * <UL>

     * </UL>

     */

    public Base64()

    {



    }



    /**

     * <!-- encode() -->

     * <p>Encode a byte array using base 64 techniques according to RFC 1421.

     * </p>

     * <br>

     * <p>Parameters:</p>

     * <UL>

     * <li>octetString      is the byte array to encode.

     * </UL>

     * <br>

     * <p>Returns:  String

     * <br>

     * <p>Exceptions:</p>

     * <UL>

     * <LI>Exception

     * </UL>

     */

    public static String encode( byte [ ] octetString )

    {

        int bits24;

        int bits6;



        char [ ] out = new char [ ( ( octetString.length - 1 ) / 3 + 1 ) * 4 ];



        int outIndex = 0;

        int i = 0;



        while ( ( i + 3 ) <= octetString.length )

        {

            // store the octets

            bits24 = ( octetString [ i++ ] & 0xFF ) << 16;

            bits24 |= ( octetString [ i++ ] & 0xFF ) << 8;

            bits24 |= ( octetString [ i++ ] & 0xFF ) << 0;



            bits6 = ( bits24 & 0x00FC0000 ) >> 18;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x0003F000 ) >> 12;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x00000FC0 ) >> 6;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x0000003F );

            out [ outIndex++ ] = b2c [ bits6 ];

        }



        if ( octetString.length - i == 2 )

        {

            // store the octets

            bits24 = ( octetString [ i ] & 0xFF ) << 16;

            bits24 |= ( octetString [ i + 1 ] & 0xFF ) << 8;



            bits6 = ( bits24 & 0x00FC0000 ) >> 18;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x0003F000 ) >> 12;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x00000FC0 ) >> 6;

            out [ outIndex++ ] = b2c [ bits6 ];



            // padding

            out [ outIndex++ ] = '=';

        }

        else if ( octetString.length - i == 1 )

        {

            // store the octets

            bits24 = ( octetString [ i ] & 0xFF ) << 16;



            bits6 = ( bits24 & 0x00FC0000 ) >> 18;

            out [ outIndex++ ] = b2c [ bits6 ];

            bits6 = ( bits24 & 0x0003F000 ) >> 12;

            out [ outIndex++ ] = b2c [ bits6 ];



            // padding

            out [ outIndex++ ] = '=';

            out [ outIndex++ ] = '=';

        }



        return new String ( out );

    }



    /**

     * <!-- decode() -->

     * <p>Encode a byte array using base 64 techniques according to RFC 1421.

     * </p>

     * <br>

     * <p>Parameters:</p>

     * <UL>

     * <li>s    is a String object holding the Base64 encoded value.

     * </UL>

     * <br>

     * <p>Returns:  byte[]

     * <br>

     * <p>Exceptions:</p>

     * <UL>

     * <LI>Exception

     * </UL>

     */

    public static byte[] decode(String s)

    {

        if (c2b==null)

	    {

          c2b = new byte[256];

          for (byte b=0;b<64;b++) c2b[(byte)b2c[b]]=b;

        } // end if



        byte[] decode = null;

		int outLen = (s.length() / 4) * 3;

        byte[] nibble = new byte[4];

		if (s.endsWith("=="))

		{

            decode = new byte[outLen - 2];

		}

		else if (s.endsWith("="))

		{

            decode = new byte[outLen - 1];

		}

        else

		{

            decode = new byte[outLen];

		}



        int d=0;

        int n=0;

        byte b;

        for (int i=0;i<s.length();i++)

		{

            char c = s.charAt(i);

            nibble[n] = c2b[(int)c];



            if (c==pad) break;



            switch(n)

		    {

            case 0:

                n++;

                break;



            case 1:

                b=(byte)(nibble[0]*4 + nibble[1]/16);

                decode[d++]=b;

                n++;

                break;



            case 2:

                b=(byte)((nibble[1]&0xf)*16 + nibble[2]/4);

                decode[d++]=b;

                n++;

                break;



            default:

                b=(byte)((nibble[2]&0x3)*64 + nibble[3]);

                decode[d++]=b;

                n=0;

                break;

            }

        }





        return decode;

    }

}







