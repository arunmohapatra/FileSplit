/*
** ISSLParams 
** This interface is used to a store a vendor specific SSL parameter set.  
** It is used in conjunction with the ISSLSocketUtils interface to remove 
** vendor specific SSL code in creating a SSL enabled Socket object and 
** institute the opening SSL handshake operation.
**
** Copyright (C) 2002-2004,	Progress Software Corp.
**
** This library is free software; you can redistribute it and/or modify it 
** under the terms of the GNU Lesser Public License as published by the Free
** Software Foundation; either version 2 of the License, or (at your option)
** any later version.
**
** This library is distributed in the hope that it will be useful, but 
** WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
** or FITNESS FOR A PARTICULAR PURPOSE.  See the BNU Lesser General Public
** License for more details.  
**
** You should have received a copy of the GNU Lesser Public License along with
** this library; if not, write to the Free Software Foundation, Inc., 59
** Temple Place, Suite 330, Boston, MA 02111-1307 USA
**
*/

package com.progress.ubroker.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This interface provides the definition for all SSL parameter handling
 * classes.  It is bases on the RSA SSLParams class, but does not extend it.
 * The reason being that all RSA classes require byte obfuscation according
 * to the RSA contract.  The SSL parameter classes provided by Progress make
 * the bridge from open calls to the obfuscated calls.
 * <p>
 */
public interface ISSLParams
{
    /*
     * CLASS Constants
     */
     /** <description> */
     // private static final <type>  <name> = <value>;

    /** Set the client authentication options.
     *  They can be one of: "none"; "requested"; or "required". */
    public static final     String      CLIENT_AUTH = "psc.ssl.auth.client";
    public static final     String      CLIENT_AUTH_OFF = "none";
    public static final     String      CLIENT_AUTH_REQUESTED = "requested";
    public static final     String      CLIENT_AUTH_REQUIRED = "required";
    /** Set the additional SSL authentication performed on the client side.
     * The default is the standard PKI - CRL list checking.. */
    public static final     String      SERVER_AUTH = "psc.ssl.auth.client";
    /* Default PKI (-CRL) digital certificate validation. */
    public static final     String      SERVER_AUTH_DEFAULT = "default";
    /** Add server digital certificate subject fild check to validate the
     *  https server's domain is in it. */
    public static final     String      SERVER_AUTH_DOMAIN = "domain";

    /** Set the supported SSL versions to support.  These can be a list of
     * names: "sslv2","sslv3","tlsv1".  Use commas so separate multiple entries.
     * DONOT include whitespace. */
    public static final     String      SSL_VERSIONS = "psc.ssl.versions";
    /** Use only SSL version 2 */
    public static final     String      SSL_VERSION_SSLV2 = "sslv2";
    /** Use only SSL version 3 */
    public static final     String      SSL_VERSION_SSLV3 = "sslv3";
    /** Use only TLS version 1 */
    public static final     String      SSL_VERSION_TLSV1 = "tlsv1";
    /** Use only TLS version 1.1 */
    public static final     String      SSL_VERSION_TLSV1_1 = "tlsv1.1";
    /** Use only TLS version 1.2 */
    public static final     String      SSL_VERSION_TLSV1_2 = "tlsv1.2";
    /** Use all SSL and TLS versions */
    public static final     String      SSL_VERSION_ALL = "sslv2,sslv3,tlsv1";

    /** SSL comression types: "none".  For future use. */
    public static final     String      SSL_COMPRESSION = "psc.ssl.compression";
    /** No data compression. (default) */
    public static final     String      SSL_COMPRESSION_NONE = "none";

    /** Set the session id cache maximum size (default 256) */
    public static final     String      SSL_SESSION_CACHE_SIZE = "psc.ssl.cache.size";
    public static final     int         SSL_SESSION_CACHE_SIZE_DEFAULT = 256;
    /** Set the session id cache timeout (-1 == infinite, 0 = no-caching, +n == milliseconds)
     *  (Default 120000 == 2 minutes)
     *  Client caching also includes SSL session reuse.  Turn caching off to disable
     *  SSL session reuse. */
    public static final     String      SSL_SESSION_CACHE_TIMEOUT = "psc.ssl.cache.timeout";
    public static final     int         SSL_SESSION_CACHE_TIMEOUT_DEFAULT = 120000;

    /** Set the buffered output on "true" or off ("false") (default is off) */
    public static final     String      SSL_BUFFERED_OUTPUT = "psc.ssl.bufferedoutput";
    /** Turn buffered output on */
    public static final     String      SSL_BUFFERED_OUTPUT_ON = "true";
    /** Turn buffered output off */
    public static final     String      SSL_BUFFERED_OUTPUT_OFF = "false";

    /** Set the maximum input buffer size 16000 to 64000 */
    public static final     String      SSL_MAX_INPUT_BUFFER = "psc.ssl.inputbuffer";
    public static final     int         SSL_DEF_INPUT_BUFFER = 32768;

    /** Set the SSL ciphers to use.  One entry per property (comma separated
     * strings is hard to work with and debug)
     * psc.ssl.cipher.1=
     * psc.ssl.cipher.2=
     *
     * all values are additive.
     *
     */
    public static final     String      SSL_CIPHER = "psc.ssl.cipher.";
    /** Set the SSL ciphers to use. */
    public static final     String      SSL_CIPHERS = "psc.ssl.ciphers";
    public static final     String      SERVER_NAME = "psc.ssl.servername";
    /* Cipher suite format:  PKEA[_PKSA]_with_DEA_MAC
     *
     *   (PKEA)           (PKSA)           (DEA)               (MAC)
     * Public-Key       Public-Key      Symmetric           Message
     * Exchange         Signature       Data encryption     Digest
     * Algorithm        Algorithm       Algorithm           Algorithm
     *------------------------------------------------------------------------
     *
     */
    public  static final    String      SSL_CIPHER_NULL_WITH_NULL_NULL = "Null_With_Null_Null";
    public  static final    String      SSL_CIPHER_RSA_WITH_RC4 = "RSA_With_RC4";
    //public  static final    String      SSL_CIPHER_RSA_WITH_NULL_MD5 = "RSA_With_Null_MD5";
    //public  static final    String      SSL_CIPHER_RSA_WITH_NULL_SHA = "RSA_With_Null_SHA";
    public  static final    String      SSL_CIPHER_RSA_WITH_RC4_MD5 = "RC4-MD5";
    public  static final    String      SSL_CIPHER_RSA_WITH_RC4_SHA = "RC4-SHA";
    public  static final    String      SSL_CIPHER_RSA_WITH_RC2_CBC_MD5 = "RSA_With_RC2_CBC_MD5";
    public  static final    String      SSL_CIPHER_RSA_WITH_3DES_EDE_CBC_MD5 = "RSA_With_3DES_EDE_CBC_MD5";
    public  static final    String      SSL_CIPHER_RSA_WITH_3DES_EDE_CBC_SHA = "RSA_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_RSA_WITH_DES_CBC_MD5 = "RSA_With_DES_CBC_MD5";
    public  static final    String      SSL_CIPHER_RSA_WITH_DES_CBC_SHA = "RSA_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_RSA_EXPORT_WITH_DES_40_CBC_SHA = "RSA_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_CIPHER_RSA_EXPORT_WITH_RC4_40_MD5 = "RSA_Export_With_RC4_40_MD5";
    public  static final    String      SSL_CIPHER_RSA_EXPORT_WITH_RC2_40_CBC_MD5 = "RSA_Export_With_RC2_40_CBC_MD5";
    public  static final    String      SSL_CIPHER_DHE_RSA_WITH_3DES_EDE_CBC_SHA = "DHE_RSA_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_DHE_RSA_WITH_DES_CBC_SHA = "DHE_RSA_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_DHE_RSA_EXPORT_WITH_DES_40_CBC_SHA = "DHE_RSA_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_CIPHER_DHE_DSS_WITH_3DES_EDE_CBC_SHA = "DHE_DSS_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_DHE_DSS_WITH_DES_CBC_SHA = "DHE_DSS_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_DHE_DSS_EXPORT_WITH_DES_40_CBC_SHA = "DHE_DSS_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_RSA_WITH_3DES_EDE_CBC_SHA = "DH_RSA_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_RSA_WITH_DES_CBC_SHA = "DH_RSA_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_RSA_EXPORT_WITH_DES_40_CBC_SHA = "DH_RSA_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_DSS_WITH_3DES_EDE_CBC_SHA = "DH_DSS_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_DSS_WITH_DES_CBC_SHA = "DH_DSS_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_DSS_EXPORT_WITH_DES_40_CBC_SHA = "DH_DSS_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_ANON_WITH_RC4_MD5 = "DH_Anon_With_RC4_MD5";
    public  static final    String      SSL_CIPHER_DH_ANON_WITH_3DES_EDE_CBC_SHA = "DH_Anon_With_3DES_EDE_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_ANON_WITH_DES_CBC_SHA = "DH_Anon_With_DES_CBC_SHA";
    public  static final    String      SSL_CIPHER_DH_ANON_EXPORT_WITH_RC4_40_MD5 = "DH_Anon_Export_With_RC4_40_MD5";
    public  static final    String      SSL_CIPHER_DH_ANON_EXPORT_WITH_DES_40_CBC_SHA = "DH_Anon_Export_With_DES_40_CBC_SHA";
    public  static final    String      SSL_AES128_SHA = "AES128-SHA";
    public  static final    String      SSL_AES256_SHA = "AES256-SHA";
    public  static final    String      SSL_DHE_DSS_WITH_AES_128_CBC_SHA   =  "DHE-DSS-AES128-SHA";
    public  static final    String      SSL_DHE_DSS_WITH_AES_256_CBC_SHA   =  "DHE-DSS-AES256-SHA";
    public  static final    String      SSL_DHE_RSA_WITH_AES_128_CBC_SHA   =  "DHE-RSA-AES128-SHA";
    public  static final    String      SSL_DHE_RSA_WITH_AES_256_CBC_SHA   =  "DHE-RSA-AES256-SHA";
    public  static final    String      SSL_DH_RSA_WITH_AES_128_CBC_SHA    =  "DH-RSA-AES128-SHA";
    public  static final    String      SSL_DH_RSA_WITH_AES_256_CBC_SHA    =  "DH-RSA-AES256-SHA";
    public  static final    String      SSL_DH_DSS_WITH_AES_128_CBC_SHA    =  "DH-DSS-AES128-SHA";
    public  static final    String      SSL_DH_DSS_WITH_AES_256_CBC_SHA    =  "DH-DSS-AES256-SHA";
    //TLSv1.2
    public  static final    String		SSL_AECDH_RC4_SHA = "AECDH-RC4-SHA";
    public  static final    String		SSL_AECDH_AES128_SHA = "AECDH-AES128-SHA";
    public  static final    String		SSL_ADH_AES128_SHA256 = "ADH-AES128-SHA256";
    public  static final    String		SSL_ADH_AES128_SHA = "ADH-AES128-SHA";
    public  static final    String      SSL_AECDH_DES_CBC3_SHA = "AECDH-DES-CBC3-SHA";
    //public  static final    String      SSL_AECDH_NULL_SHA = "AECDH-NULL-SHA";
    /* new tlsv1.2 ciphers */
    public  static final String SSL_ECDHE_RSA_AES256_GCM_SHA384 = "ECDHE-RSA-AES256-GCM-SHA384";
    public  static final String SSL_ECDHE_ECDSA_AES256_GCM_SHA384 = "ECDHE-ECDSA-AES256-GCM-SHA384";
    public  static final String SSL_DHE_RSA_AES128_SHA256 = "DHE-RSA-AES128-SHA256"; //tested
    public  static final String SSL_DHE_RSA_AES128_GCM_SHA256 = "DHE-RSA-AES128-GCM-SHA256";//tested
    public  static final String SSL_DHE_DSS_AES128_SHA256  = "DHE-DSS-AES128-SHA256";
    public  static final String SSL_DHE_DSS_AES128_GCM_SHA256 = "DHE-DSS-AES128-GCM-SHA256";
    public  static final String SSL_ECDHE_RSA_AES128_GCM_SHA256  = "ECDHE-RSA-AES128-GCM-SHA256";
    public  static final String SSL_DHE_RSA_AES256_SHA256 = "DHE-RSA-AES256-SHA256";//tested
    public  static final String SSL_AES256_SHA256 = "AES256-SHA256";//tested
    public  static final String SSL_AES128_GCM_SHA256 = "AES128-GCM-SHA256";//tested
    public  static final String SSL_AES128_SHA256 = "AES128-SHA256"; //tested
    public  static final String SSL_ECDHE_RSA_AES128_SHA256 = "ECDHE-RSA-AES128-SHA256";
    public  static final String SSL_DHE_DSS_AES256_SHA256 = "DHE-DSS-AES256-SHA256";
    public  static final String SSL_ECDHE_RSA_AES256_SHA384 = "ECDHE-RSA-AES256-SHA384";
    public  static final String SSL_ECDHE_ECDSA_AES256_SHA384 = "ECDHE-ECDSA-AES256-SHA384";
    public  static final String SSL_ECDHE_ECDSA_AES128_GCM_SHA256 = "ECDHE-ECDSA-AES128-GCM-SHA256";
    public  static final String SSL_ECDHE_ECDSA_AES128_SHA256 = "ECDHE-ECDSA-AES128-SHA256";
    public  static final String SSL_DHE_DSS_AES256_GCM_SHA384 = "DHE-DSS-AES256-GCM-SHA384";
    public  static final String SSL_DHE_RSA_AES256_GCM_SHA384 = "DHE-RSA-AES256-GCM-SHA384";
    public  static final String SSL_ECDH_RSA_AES256_GCM_SHA384 = "ECDH-RSA-AES256-GCM-SHA384";
    public  static final String SSL_ECDH_ECDSA_AES256_GCM_SHA384 = "ECDH-ECDSA-AES256-GCM-SHA384";
    public  static final String SSL_ECDH_RSA_AES256_SHA384 = "ECDH-RSA-AES256-SHA384";
    public  static final String SSL_ECDH_ECDSA_AES256_SHA384 = "ECDH-ECDSA-AES256-SHA384";
    public  static final String SSL_ECDH_RSA_AES128_GCM_SHA256 = "ECDH-RSA-AES128-GCM-SHA256";
    public  static final String SSL_ECDH_ECDSA_AES128_GCM_SHA256 = "ECDH-ECDSA-AES128-GCM-SHA256";
    public  static final String SSL_ECDH_RSA_AES128_SHA256 = "ECDH-RSA-AES128-SHA256";
    public  static final String SSL_ECDH_ECDSA_AES128_SHA256 = "ECDH-ECDSA-AES128-SHA256";
    public  static final String SSL_AES256_GCM_SHA384 = "AES256-GCM-SHA384";  
    public  static final String SSL_ADH_AES256_SHA256 = "ADH-AES256-SHA256";//Arun
    public  static final String SSL_ADH_AES128_GCM_SHA256 = "ADH-AES128-GCM-SHA256";
    public  static final String SSL_DH_RSA_AES128_GCM_SHA256 = "DH-RSA-AES128-GCM-SHA256";
    public  static final String SSL_DH_RSA_AES256_SHA256 = "DH-RSA-AES256-SHA256";
    public  static final String SSL_DH_RSA_AES128_SHA256 = "DH-RSA-AES128-SHA256";
    public  static final String SSL_DH_DSS_AES128_GCM_SHA256 = "DH-DSS-AES128-GCM-SHA256";
    //public  static final String SSL_NULL_SHA256 = "NULL-SHA256";
    public  static final String SSL_DH_DSS_AES256_SHA256 = "DH-DSS-AES256-SHA256";
    public  static final String SSL_DH_DSS_AES128_SHA256 ="DH-DSS-AES128-SHA256";
    public  static final String SSL_ADH_AES256_GCM_SHA384 = "ADH-AES256-GCM-SHA384";
    public  static final String SSL_DH_RSA_AES256_GCM_SHA384 = "DH-RSA-AES256-GCM-SHA384";
    public  static final String SSL_DH_DSS_AES256_GCM_SHA384 = "DH-DSS-AES256-GCM-SHA384";
    
    /* others*/
    public static final String SSL_DHE_DSS_AES256_SHA = "DHE-DSS-AES256-SHA";
    public static final String SSL_DH_DSS_AES128_SHA = "DH-DSS-AES128-SHA";

    /** Include all Export level ciphers */

    public  static final    int         SSL_CIPHER_MAX_SUITES = 76;
    /*
     * Super Object Properties.
     */
     /** <description> */
     //  protected       <type>          <name> = <value>;

    /*
     * Object Instance Properties.
     */
     // private         <type>          <name> = <value>;
    public static final    int         DEFAULT_SSL_SESSION_CACHE_TIMEOUT = 12000;



    /*
     * CLASS Properties.
     */
     /** <description> */
     // public static        <type>  <name> = <value>;

    /*
     * ACCESSOR METHODS:
     */


    /**
     * <!-- setProtocolName() -->
     * <p>Set the name of the protocol to report in exceptions.
     * </p>
     * <br>
     * @param protocolName is a String that should hold either the value "https"
     * or the value "SSL" or the value "TLS"
     * <br>
     */
    public  void    setProtocolName(String protocolName);

    /**
     * <!-- getProtocolName() -->
     * <p>Get the curent protocol name that is reported in exceptions.
     * </p>
     * <br>
     * @return  String
     */
    public  String  getProtocolName();

    /**
     * <!-- setServerDomainAuthentication() -->
     * <p>Turn server domain authentication on or off.
     * </p>
     * <br>
     * @param domain    is a String object that holds the server DNS domain name
     * or dot-formated ip address to verifiy as part of the server digital
     * certificate's subject DN field.  If a non-null and non-zero length field
     * value is passed, domain authentication is performed.  If a null or zero
     * length value is passed, domain authentication is not performed
     */
    public void setServerDomainAuthentication(String domain);

    /**
     * <!-- getServerDomainAuthentication() -->
     * <p>Return the server domain name to verify is part of the server digital
     * certificate's subject DN field.
     * </p>
     * <br>
     * @return  String this value may be null in the case that server domain
     * authentication is disabled.
     */
    public  String getServerDomainAuthentication();



    /* * PUBLIC METHODS: */

    /**
     * <!-- init() -->
     * <p>Initialize the defaults and any optional settings for this
     * SSL parameters object.
     * </p>
     * <br>
     * @param initialSettings is an optional Properties object that will have
     * any SSL parameters extracted from it to override the defaults.  If null,
     * only the defaults are instituted.
     * @param debugStream is an OutputStream which when non-null specifies 
     * that debug logging messages should be output.
     * @param debugLevel is an int in the range of 0 to 6, with 6 being the most
     * verbose, that indicates the amount of debug information to be output to
     * debugWriter.  This parameter is ignored when debugWriter is null.
     * <br>
     * @exception   Exception
     */
    public void init(Properties     initialSettings,
                     OutputStream   debugStream,
                     int            debugLevel)
        throws Exception;

    /**
     * <!-- getVendorParams() -->
     * <p>Get an abstract copy of the SSLParams class
     * </p>
     * <br>
     * @return  Object  (this may be null)
     */
    public  Object getVendorParams();

    /**
     * <!-- loadAuthenticationCertificates() -->
     * <p> Load the digital certificates used to validate peer Digital Certificates.
     * </p>
     * <br>
     * @param loadCmd is a String that holds a certificate loader's
     * command line arguments 
     * @return  int indicating the number of certificates loaded
     * <br>
     * @exception   IOException
     */
    public int loadAuthenticationCertificates( String loadCommand )
        throws IOException;

    /**
     * <!-- loadPrivateIdentity() -->
     * <p>Load a private identity (private key and digital certificate) to
     * send to the SSL peer for authentication.
     * </p>
     * <br>
     * @param keyPath is a String pointing to the location of the private key
     * @param keyPwd is a String holding the clear-text password to the private
     * key storage
     * @param keyFormat is a String holding the format of the private key file.
     * It may be one of "BINARY_PKCS5", "BINARY_PKCS8", "PEM_PKCS8", (TBD).
     * <br>
     * @exception   IOException
     */
    public  void        loadPrivateIdentify(String keyPath,
                                            String  keyPwd,
                                            String  keyFormat,
                                            String  certPath,
                                            String  certPwd,
                                            String  certFormat)
        throws IOException;

    /**
     * <!-- unloadPrivateIdentity() -->
     * <p>Unload any private identity (private key and digital certificate) that
     * has been loaded.
     * </p>
     */
    public  void        unloadPrivateIdentity();

    /**
     * <!-- setSSLVersions() -->
     * <p>Set the SSL versions to support.
     * </p>
     * <br>
     * @param versions is a String that contains a [comma separated] list of
     * SSL_VERSION_XXX values that represent the SSL versions to support.
     * <br>
     * @exception   IOException
     */
    public  void setSSLVersions(String versions) 
        throws IOException;

    /**
     * <!-- getSSLVersions() -->
     * <p>Get a comma separated list of the SSL protocol versions supported.
     * </p>
     * <br>
     * @return  String may return null.
     */
    public  String getSSLVersions();
    
    /**
     * 
     * @param m_sslClient
     * Set the parameter m_sslClient to identify if it is client or server. True if it is client.
     */
    
    public void setSSLClient(boolean m_sslClient);

    /**
     * <!-- setSessionIdCacheTime() -->
     * <p>Set the maximum amount of time a session id may remain valid in the
     * cache.
     * </p>
     * <br>
     * @param milliseconds is the maximum cache entry lifetime.  Default 12000.
     * Negative numbers less than -1 results in an error.  Minus 1 (-1) will
     * disable session id time expiration.  A zero value turns off caching
     * (equivilent to setSessionIdCache(off)).
     * <br>
     * @exception   IOException
     */
    public  void        setSessionIDCacheTime(long  milliseconds)
        throws IOException;

    /**
     * <!-- getSessionIdCacheTime() -->
     * <p>Get the SSL session id cache timeout value.
     * </p>
     * <br>
     * @return  long with the number of milliseconds of
     */
    public  long        getSessionIDCacheTime();

    /**
     * <!-- setSessionIDCacheSize() -->
     * <p>Set the maximum number of entries the SSL session id cache can hold.
     * When full, it first purges old entries, tries again and if still full,
     * drops the new id.
     * </p>
     * <br>
     * @param size is a positive integer that should be in the range of 100 to
     * 1024. Default 256
     * <br>
     * @exception   IOException
     */
    public  void        setSessionIDCacheSize(int size)
        throws IOException;

    /**
     * <!-- getSessionIDCacheSize() -->
     * <p>Get the current maximum number of entries that can be held in the
     * SSL Session id cache.
     * </p>
     * <br>
     * @return  int
     */
    public  int         getSessionIDCacheSize();

    /**
     * <!-- purgeSessionIDCache() -->
     * <p>Empty all recorded SSL session ids from the cache.
     * </p>
     * <br>
     * @exception   IOException
     */
    public  void        purgeSessionIdCache() 
        throws IOException;

    /**
     * <!-- setBufferedOutput() -->
     * <p>Set output buffering on or off.
     * </p>
     * <br>
     * @param fOnOff is a boolean value turning buffering on (true) or off (false)
     */
    public  void        setBufferedOutput(boolean fOnOff);

    /**
     * <!-- getBufferedOutput() -->
     * <p>Get the output buffering state.
     * </p>
     * <br>
     * @return  boolean that holds on (true) or off (false)
     */
    public  boolean     getBufferedOutput();

    /**
     * <!-- setCompressionTypes() -->
     * <p>Set the data compression to use.
     * </p>
     * <br>
     * @param String holding the compression types supported (currently none).
     * <br>
     * @exception   IOException
     */
    public  void        setCompressionTypes(String typeNames)
        throws IOException;

    /**
     * <!-- getCompressionTypes() -->
     * <p>Return the (comma separated) list of supported compression types.
     * </p>
     * <br>
     * @return  String
     */
    public  String      getCompressionTypes();

    /**
     * <!-- getCiphers() -->
     * <p>This method will get the cipher suites that are allowed to be used.
     * </p>
     * @return  String[] of SSL_CIPHER_XXXX values.
     * <br>
     * @exception   IOException
     */
    public String[] getCiphers() 
        throws IOException;

    /**
     * <!-- setCiphers() -->
     * <p>This method will set the cipher suites that are allowed to be used.
     * </p>
     * <br>
     * @param cipherSuites is an array of strings (SSL_CIPHER_XXXXX) holding the
     * ciphers to support.  A null argument is ignored.  Calling multiple times
     * will not result in an additive effect.
     * <br>
     * @return  void
     * <br>
     * @exception   IOException
     */
    public  void        setCiphers(String[] cipherSuites)
        throws IOException;

    /**
     * <!-- <getSupportedCipherSuites>() -->
     * <p> Get the list of supported cipher suites.
     * </p>
     * <br>
     *return  String[] of CIPHER_SUITE_XXXX strings
     */
    public  String[]    getSupportedCipherSuites();

    /**
     * <!-- setClientAuth() -->
     * <p>Set the SSL client authentication steps with one
     * of values (CLIENT_AUTH_XXXX).
     * </p>
     * <br>
     * @param authType is a String holding a comma separated list of authenication
     * steps to perform.
     * <br>
     * @exception   IOException
     */
    public  void        setClientAuth(String authType)
        throws IOException;

    /**
     * <!-- getClientAuthentication() -->
     * <p>Get the comma separated list of SSL client authentication steps to
     * perform.
     * </p>
     * <br>
     * @return  String
     */
    public  String      getClientAuth();

    /**
     * <!-- setServerAuth() -->
     * <p>Set the SSL server authentication steps with one
     * of values (CLIENT_AUTH_XXXX).
     * </p>
     * <br>
     * @param authType is a String holding the client authenication
     * steps to perform.
     * <br>
     * @exception   IOException
     */
    public  void        setServerAuth(String authType)
        throws IOException;

    /**
     * <!-- getServerAuth() -->
     * <p>Get the list of SSL server authentication steps to
     * perform.
     * </p>
     * <br>
     * @return  String
     */
    public  String      getServerAuth();

    /**
     * <!-- setMaxInputBufferSize() -->
     * <p>Set the maximum input buffer size (the default is 16 KB).
     * </p>
     * <br>
     * @param maxSize is a positive integer in the range 16,000 to 64,0000.
     * <br>
     * @exception   IOException
     */
    public  void        setMaxInputBufferSize(int maxSize)
        throws IOException;

    /**
     * <!-- getMaxInputBufferSize() -->
     * <p>Return the maximum input buffer size currently set.
     * </p>
     * <br>
     * @return  int
     */
    public  int         getMaxInputBufferSize();

    /*
     * PROTECTED (SUPER) METHODS:
     */

    /*
     * PRIVATE METHODS:
     */

}
