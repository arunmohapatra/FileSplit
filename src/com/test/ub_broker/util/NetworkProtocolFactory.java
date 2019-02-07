/*
 * <p>Copyright 2000-2004,2011 Progress Software Corportation, All rights reserved.</p>
 * <br>
 * <p>Class:        NetworkProtocolFactory    </p>
 * <br>
 *
 */

package com.progress.ubroker.util;

import java.net.MalformedURLException;

import com.progress.common.ehnlog.IAppLogger;

/**
 ** <p>
 * The NetworkProtocolFactory is used to generate classes that implement the
 * INetworkProtocol interface (XXXX{Client|Server}NetworkProtocol). Those
 * classes provide transparent input and output stream support for various types
 * of network protocols. This factory can create one of those classes.
 */
public final class NetworkProtocolFactory {
	/*
	 * CLASS Constants
	 */

	/* Constant for protocol class for ssl full RSA package */
	private static String m_HttpsFullPackage = "com.progress.ubroker.ssl.HttpsClientProtocol";

	/*
	 * NOTE: The string class names in this array correspond to the
	 * INetworkProtocol PROTOCOL_XXXXXX declarations for client end-point types.
	 */
	private static String[] m_clientClasses = { "com.progress.ubroker.client.TcpClientProtocol",
			"com.progress.ubroker.client.TcpClientProtocol", "com.progress.ubroker.client.HttpClientProtocol",
			"HttpsClientProtocol", "com.progress.ubroker.client.TcpClientProtocol",
			"com.progress.ubroker.ssl.SSLClientProtocol", "com.progress.ubroker.ssl.SSLClientProtocol", "", "", "" };
	/*
	 * NOTE: The string class names in this array correspond to the
	 * INetworkProtocol PROTOCOL_XXXXXX declarations for Server end-point types.
	 */
	private static String[] m_serverClasses = { "", "", "", "", "", "", "", "", "", "" };
	/*
	 * NOTE: The string class names in this array correspond to the
	 * INetworkProtocol PROTOCOL_XXXXXX declarations for Listener end-point
	 * types.
	 */
	private static String[] m_listenerClasses = { "", "", "", "", "", "", "", "", "", "" };

	/*
	 * Constructors...
	 */

	/**
	 * <!-- NetworkProtocolFactory() -->
	 * <p>
	 * The default class constructor.
	 * </p>
	 * <br>
	 * return void
	 */
	public NetworkProtocolFactory() {
	}

	/*
	 * PUBLIC METHODS:
	 */
	/**
	 * <!-- create() -->
	 * <p>
	 * Create a class that implements an INetworkProtocol interface for a
	 * specific type of network protocol.
	 * </p>
	 * <br>
	 * 
	 * @param endPointType
	 *            an int constant (INetworkProtocol::END_POINT_XXXX) that
	 *            indicates whether this is a client or server end-point
	 * @param connectionInfo
	 *            a SocketConnectionInfo object that contains the information to
	 *            determine which type of network protocol will be used.
	 * @param loggingObject
	 *            a AppLogger object to use in tracing operations, errors and
	 *            3rd party exceptions.
	 * @param loggingDestingation
	 *            an int that holds a AppLogger.DEST_XXXXXX value that sets the
	 *            destination for logging. <br>
	 * @return INetworkProtocol <br>
	 * @exception MalformedURLException
	 *                is thrown when the connectionInfo argument cannot return a
	 *                valid protocol
	 * @exception NetworkProtocolException
	 *                is thown when a network protocol handler class cannot be
	 *                created for any reason.
	 * @exception NullPointerException
	 *                thrown when a null connectionInfo argument is passed.
	 */
	public static INetworkProtocol create(int endPointType, SocketConnectionInfoEx connectionInfo,
			IAppLogger loggingObject, int loggingDestination) throws MalformedURLException, NullPointerException,
			NetworkProtocolException

	{
		INetworkProtocol returnValue = null;
		String protocolName = null;

		if (null == connectionInfo) {
			throw new NullPointerException("Must supply a non-null SocketConnectionInfo object");
		}

		if (null == connectionInfo.getProtocol()) {
			throw new MalformedURLException("The URL must include a network protocol value");
		}

		return (create(endPointType, connectionInfo.getProtocol(), loggingObject, loggingDestination));
	}

	/**
	 * <!-- create() -->
	 * <p>
	 * Create a class that implements an INetworkProtocol interface for a
	 * specific type of network protocol.
	 * </p>
	 * <br>
	 * 
	 * @param endPointType
	 *            an int constant (INetworkProtocol::END_POINT_XXXX) that
	 *            indicates whether this is a client or server end-point
	 * @param protocolType
	 *            a String that contains the the network protocol name to use.
	 * @param loggingObject
	 *            a AppLogger object to use in tracing operations, errors and
	 *            3rd party exceptions.
	 * @param loggingDestingation
	 *            an int that holds a AppLogger.DEST_XXXXXX value that sets the
	 *            destination for logging. <br>
	 * @return INetworkProtocol <br>
	 * @exception NetworkProtocolException
	 */
	public static INetworkProtocol create(int endPointType, String protocolType, IAppLogger loggingObject,
			int loggingDestination) throws NetworkProtocolException {
		INetworkProtocol returnValue = null;
		int targetEndPointType = -1;
		int targetProtocolType = -1;
		String targetEndPointName = INetworkProtocol.m_endPointTypeNames[INetworkProtocol.END_POINT_UNSUPPORTED];
		String targetProtocolName = INetworkProtocol.m_protocolTypeNames[INetworkProtocol.PROTOCOL_UNSUPPORTED];
		String targetClass = null;
		String matchProtocolType = protocolType;

		/* Locate and validate the end-point type string name. */
		if (INetworkProtocol.END_POINT_MIN <= endPointType && INetworkProtocol.END_POINT_MAX >= endPointType) {
			targetEndPointType = endPointType;
			targetEndPointName = INetworkProtocol.m_endPointTypeNames[endPointType];
		}

		/* Set a default of "tcp" as a default. */
		if (null == protocolType) {
			matchProtocolType = "tcp";
		}

		/* Locate and validate the protocol type name. */
		for (int i = INetworkProtocol.PROTOCOL_MIN; i <= INetworkProtocol.PROTOCOL_MAX; i++) {
			if (INetworkProtocol.m_protocolTypeNames[i].equalsIgnoreCase(matchProtocolType)) {
				targetProtocolType = i;
				targetProtocolName = INetworkProtocol.m_protocolTypeNames[i];
				break;
			}
		}

		/* if we have invalid arguments, end this now! */
		if (-1 == targetEndPointType || -1 == targetProtocolType) {
			if (null != loggingObject) {
				loggingObject.logError("NetworkProtocolFactory detected invalid " + "end-point(" + targetEndPointType
						+ ")/target-protocol(" + targetProtocolType + ") types.");
			}

			throw new NetworkProtocolException(NetworkProtocolException.UNSUPPORTED_PROTOCOL, targetProtocolName,
					targetEndPointName);
		}

		/* Now see if we have a corresponding support class */
		if (INetworkProtocol.END_POINT_CLIENT == targetEndPointType) {
			targetClass = m_clientClasses[targetProtocolType];
		} else if (INetworkProtocol.END_POINT_SERVER == targetEndPointType) {
			targetClass = m_serverClasses[targetProtocolType];
		} else if (INetworkProtocol.END_POINT_LISTENER == targetEndPointType) {
			targetClass = m_listenerClasses[targetProtocolType];
		}

		/* If we don't have a class we can create, no need to go further. */
		if (null == targetClass || 0 == targetClass.length()) {
			if (null != loggingObject) {
				loggingObject.logError("NetworkProtocolFactory could not find a "
						+ "XxxxClientProtocol.class file for the " + "end-point(" + targetEndPointType
						+ ")/target-protocol(" + targetProtocolType + ") types.");
			}
			throw new NetworkProtocolException(NetworkProtocolException.UNSUPPORTED_PROTOCOL, targetProtocolName,
					targetEndPointName);
		}

		/*
		 * Next see if we actually have the class loadable. If we can't load it,
		 * no need to continue.
		 */
		try {
			if (targetClass.startsWith("Https")) {
				// Try the SSL full package first...
				/*
				 * if (null != loggingObject) {
				 * loggingObject.LogMsgln(loggingDestination,
				 * Logger.LOGGING_TRACE, Logger.TIMESTAMP,
				 * "NetworkProtocolFactory attempting " + "handler class " +
				 * m_HttpsFullPackage); }
				 */

				returnValue = (INetworkProtocol) Class.forName(m_HttpsFullPackage).newInstance();
			} else {
				/*
				 * Everything is a "go" so far, so now go out and create the
				 * class.
				 */
				/*
				 * if (null != loggingObject) {
				 * loggingObject.LogMsgln(loggingDestination,
				 * Logger.LOGGING_TRACE, Logger.TIMESTAMP,
				 * "NetworkProtocolFactory creating protocol " +
				 * "handler class " + targetClass + "..."); }
				 */

				returnValue = (INetworkProtocol) Class.forName(targetClass).newInstance();
			}
			/*
			 * if (null != loggingObject) {
			 * loggingObject.LogMsgln(loggingDestination, Logger.LOGGING_TRACE,
			 * Logger.TIMESTAMP,
			 * "NetworkProtocolFactory successfully created protocol " +
			 * "handler class " + targetClass); }
			 */
		} catch (Throwable e) {
			if (null != loggingObject) {
				loggingObject.logStackTrace("NetworkProtcolFactory.create()", e);
			}
			throw new NetworkProtocolException(NetworkProtocolException.CANNOT_FIND_PROTOCOL_LIBRARY,
					targetProtocolName, e.toString());
		}

		return (returnValue);
	}

}
